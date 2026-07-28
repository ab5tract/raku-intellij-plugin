#!/usr/bin/env raku
use v6.d;
use nqp;

#| STAGE 10 -- inventory the API surface.
#|
#| Enumerates every type declared in the Rakudo setting sources, resolves each in the
#| *running* Rakudo, and records what each of its methods declares as named
#| parameters. Source supplies the inventory; the live interpreter supplies the facts.
#|
#| Why not just walk `CORE::`? Because it only surfaces 18 types -- most of the setting
#| is not reachable that way. The source has all 977 class/role declarations.
#|
#| Writes scripts/cache/named-args.tsv, whose schema is fixed by scripts/named-args.raku.
#|
#| See org/llm/traces/raku-named-args-corpus.md for the whole pipeline.

my $repo  = $*PROGRAM.parent.parent.parent;
my $cache = $repo.add('scripts/cache');
my $src   = (%*ENV<RAKUDO_SRC> // $*HOME.add('code/raku/x.core/rakudo').Str).IO;

die "no Rakudo checkout at $src -- set RAKUDO_SRC" unless $src.add('src/core.c').d;

sub rakudo-id(--> Str) { $*RAKU.compiler.name ~ '-' ~ $*RAKU.compiler.version }

sub rakudo-commit(--> Str) {
    my $p = run(<git -C>, $src.Str, <rev-parse --short HEAD>, :out, :err);
    my $o = $p.out.slurp(:close).trim;
    $p.err.slurp(:close);
    $o || 'unknown';
}

#| The declared-named facts for one method, unioned across all multi candidates.
#| A proto's own signature is usually `|`, which says nothing -- the real parameter
#| lists live on the candidates.
sub facts($m) {
    my %declared;
    my $catchall = False;
    my @cands = $m.?candidates // ($m,);
    for @cands -> $c {
        for $c.signature.params -> $p {
            # `|c` is not a *named* parameter but absorbs named arguments just as
            # thoroughly as `*%_`. Missing it under-reports risk on exactly the
            # methods (IO::Path.lines, Str.subst) that forward via capture.
            $catchall = True if $p.capture;
            next unless $p.named;
            if $p.slurpy { $catchall = True }
            else {
                %declared{$_} = $p.optional ?? 'optional' !! 'REQUIRED'
                    for $p.named_names;
            }
        }
    }
    %( :%declared, :$catchall, cands => @cands.elems );
}

#| Type names as declared in the setting sources. Also picks up `augment`ed types.
sub source-types() {
    # NOT .dir(:R) -- IO::Path.dir has no recursion adverb, it is silently ignored,
    # and this exact line originally read `.dir(:R)`: it saw 178 files instead of 268
    # and reported success. The bug this whole pipeline exists to catch, committed
    # inside the pipeline. Left documented rather than quietly fixed.
    my @files = <core.c core.d core.e>.map({ $src.add("src/$_") })
                                     .grep(*.d)
                                     .map({ dir-r($_) })
                                     .flat
                                     .grep({ .f && .Str.ends-with('.rakumod') });
    my %names;
    for @files -> $f {
        for $f.lines -> $line {
            # `my class Foo`, `role Bar[::T]`, `augment class Str`
            if $line ~~ / ^ \s* ['my' \s+]? ['augment' \s+]? $<kind>=['class'|'role']
                          \s+ $<name>=[<[A..Za..z_]> <[\w:]>*] / {
                %names{~$<name>}++;
            }
        }
    }
    %names.keys.grep({ !.ends-with(':') }).sort;
}

# IO::Path.dir is not recursive; roll the walk by hand. (This script exists partly
# because that mistake cost four first-attempt failures -- see level3.)
sub dir-r(IO::Path $d) {
    gather for $d.dir -> $p { $p.d ?? (take $_ for dir-r($p)) !! take $p }
}

$cache.mkdir;
my $id     = rakudo-id();
my $commit = rakudo-commit();
note "rakudo $id, setting source at $commit";

my @types = source-types();
note "{@types.elems} type names declared in the setting sources";

my $out = $cache.add('named-args.tsv').open(:w);
$out.say: join "\t", <rakudo type method candidates slurpy declared>;

#| One type's rows. Returns () rather than throwing: some setting types explode on
#| .^methods (parametric-role HOWs, stubs referencing symbols that never got
#| installed). Letting one of those escape aborts the whole sweep -- which it did,
#| silently, at 110 of 732 types, with output that looked plausible.
sub rows-for(Str $name, Str $id) {
    # `::($name)` returns a *Failure* for an unknown symbol rather than throwing, and
    # that Failure detonates later -- in DESTROY, or at the next use -- which is how
    # this sweep silently died at 110 of 732 types while reporting success. Sink it
    # here, explicitly, before it can escape.
    my $t = Nil;
    {
        my \sym = ::($name);
        if nqp::istype(sym, Failure) {
            # Marking it handled is not optional housekeeping. An unhandled Failure
            # detonates later, in DESTROY -- *outside* any CATCH's dynamic scope, so
            # no amount of wrapping catches it. That is what killed this sweep twice,
            # both times leaving a plausible-looking partial file behind.
            sym.Bool;
        }
        else {
            $t = sym;
        }
        CATCH { default { $t = Nil } }
    }
    return () if $t =:= Nil;

    # Ask before calling, rather than calling and catching.
    #
    # Not defensive style -- load-bearing. Some HOWs (NativeRefHOW, several role HOWs)
    # genuinely have no `.methods`. Letting that throw and catching it *poisons the
    # `.^methods` callsite*: every later type failed with "ClassHOW.methods not found"
    # even though ClassHOW plainly has it. The sweep silently lost 762 of 995 types
    # and still reported success. The first legitimate failure was `IntAttrRef`, and
    # everything alphabetically after it was collateral damage.
    return () unless nqp::can($t.HOW, 'methods');

    my @methods;
    {
        @methods = $t.^methods(:local).grep(* ~~ Routine);
        CATCH { default { note "  .^methods failed for $name: {.message.lines.head}"; @methods = () } }
    }
    return () unless @methods;

    my @out;
    for @methods -> $m {
        my $mn = Nil;
        { $mn = $m.name; CATCH { default { $mn = Nil } } }
        # Skip private (!foo), metamethods (^foo) and operator-ish names; this tool is
        # for ordinary `.method(:adverb)` call sites.
        next unless $mn.defined && $mn ~~ / ^ <[a..zA..Z_]> <[\w\-]>* $ /;

        my %f;
        { %f = facts($m); CATCH { default { %f = () } } }
        next unless %f<cands>;

        @out.push: join "\t",
            $id, $name, $mn, %f<cands>,
            (%f<catchall> ?? 'yes' !! 'no'),
            %f<declared>.sort(*.key).map({ "{.key}:{.value}" }).join(',');
    }
    @out;
}

my ($resolved, $rows, $failed) = 0, 0, 0;
for @types -> $name {
    my @r;
    my $err = Nil;
    { @r = rows-for($name, $id); CATCH { default { $err = .message; @r = () } } }
    if $err { $failed++; note "  skipped $name: {$err.lines.head}" if $failed <= 5 }
    next unless @r;
    $resolved++;
    $out.say($_) for @r;
    $rows += @r.elems;
}
$out.close;

# The commit is recorded beside the data rather than in it, so the TSV schema stays
# exactly what scripts/named-args.raku already parses.
$cache.add('SOURCE.txt').spurt("rakudo-id\t$id\nsetting-commit\t$commit\ngenerated\t{DateTime.now.utc.truncated-to('second')}\n");

note "resolved $resolved of {@types.elems} types ($failed errored), wrote $rows method rows";
note "-> scripts/cache/named-args.tsv";

# A sweep that quietly covers a third of the API is worse than one that fails, because
# every later stage inherits the gap as if it were data.
die "sweep looks truncated: only $resolved types resolved" if $resolved < 300;
