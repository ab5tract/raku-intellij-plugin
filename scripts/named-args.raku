#!/usr/bin/env raku
use v6.d;

#| Is that named argument real, or is Raku about to ignore it?
#|
#| Raku methods carry an implicit `*%_`, so an unrecognised named argument is
#| accepted in silence and dropped. `.dir(:recursive)`, `.dir(:R)`, `.pick(:seed)`
#| all typecheck, all run, and all return a confident wrong answer. That failure
#| mode caused 4 of the 4 Raku first-attempt failures in
#| `org/llm/research/raku-tokens/level3/` -- against 0 for Python -- and three of
#| them exited 0 while printing plausible zeros.
#|
#| It asks the *running* Rakudo rather than the docs, because docs drift from the
#| interpreter and this repo already pins expectations to a specific Rakudo.
#|
#| USAGE
#|   raku scripts/named-args.raku IO::Path dir
#|   raku scripts/named-args.raku IO::Path dir recursive r test
#|   raku scripts/named-args.raku --refresh IO::Path dir
#|
#| Exit status is the number of undeclared names, so it composes in a shell test.
#|
#| Answers are cached in `scripts/cache/named-args.tsv`, keyed by Rakudo version.
#| The cache is committed on purpose: it travels with the repo, warms instantly on
#| a fresh machine, and is readable as a plain table. Rows for a different Rakudo
#| are ignored rather than trusted -- the whole point is to match the interpreter
#| actually running.

constant CACHE = $*PROGRAM.parent.add('cache/named-args.tsv');
constant CORPUS = $*PROGRAM.parent.add('cache');

#| Rows of a corpus TSV, header skipped. Missing file is not an error: the corpus is
#| optional enrichment, and this tool must stay useful on a checkout that has never
#| run the cheat-sheet pipeline.
sub corpus(Str $name, Int $cols) {
    my $f = CORPUS.add($name);
    return () unless $f.e;
    $f.lines.skip(1).grep(*.trim.chars).map({ .split("\t") }).grep(*.elems >= $cols);
}

sub rakudo-id(--> Str) { $*RAKU.compiler.name ~ '-' ~ $*RAKU.compiler.version }

#| Returns (declared => %{name => optional|REQUIRED}, slurpy => Bool, cands => Int)
sub introspect(Str $type, Str $method) {
    # A type object is never .defined, so testing definedness here would reject
    # every valid answer. `try` yields Nil when the symbol does not resolve.
    my $t = try ::($type);
    die "no such type: $type" if $t =:= Nil || $t ~~ Failure;

    my $m = $t.^find_method($method) // die "$type has no method '$method'";

    # A proto's own signature is usually just `|`, which says nothing. The real
    # parameter lists live on the candidates, so union across all of them.
    my @cands = $m.?candidates // ($m,);
    my %declared;
    my $slurpy = False;
    for @cands -> $c {
        for $c.signature.params -> $p {
            # `|c` is not a *named* parameter, but it absorbs named arguments just
            # as thoroughly as `*%_` does. Missing it would under-report the risk on
            # exactly the methods (IO::Path.lines, Str.subst) that use it.
            $slurpy = True if $p.capture;
            next unless $p.named;
            if $p.slurpy { $slurpy = True }
            else {
                %declared{$_} = $p.optional ?? 'optional' !! 'REQUIRED'
                    for $p.named_names;
            }
        }
    }
    %( :%declared, :$slurpy, cands => @cands.elems );
}

sub cache-read(Str $type, Str $method) {
    return Nil unless CACHE.e;
    my $id = rakudo-id();
    for CACHE.lines.skip(1) -> $line {
        my ($rk, $ty, $me, $cands, $slurpy, $decl) = $line.split("\t");
        next unless $rk eq $id && $ty eq $type && $me eq $method;
        my %declared;
        for $decl.split(',').grep(*.chars) -> $d {
            my ($n, $o) = $d.split(':');
            %declared{$n} = $o;
        }
        return %( :%declared, slurpy => $slurpy eq 'yes', cands => +$cands );
    }
    Nil;
}

sub cache-write(Str $type, Str $method, %r) {
    CACHE.parent.mkdir;
    CACHE.spurt("rakudo\ttype\tmethod\tcandidates\tslurpy\tdeclared\n") unless CACHE.e;
    CACHE.spurt(join("\t",
        rakudo-id(), $type, $method, %r<cands>,
        %r<slurpy> ?? 'yes' !! 'no',
        %r<declared>.sort(*.key).map({ "{.key}:{.value}" }).join(',')
    ) ~ "\n", :append);
}

sub MAIN(
    Str $type,                #= type name, e.g. IO::Path
    Str $method,              #= method name, e.g. dir
    *@names,                  #= named args to check, without the colon
    Bool :$refresh = False,   #= re-introspect and rewrite the cache entry
) {
    my $cached = $refresh ?? Nil !! cache-read($type, $method);
    my %r = $cached // introspect($type, $method);
    cache-write($type, $method, %r) unless $cached;

    say "$type.$method  --  %r<cands> candidate{ %r<cands> == 1 ?? '' !! 's' }"
        ~ ($cached ?? '  (cached)' !! '');

    say %r<declared>
        ?? "  declares: " ~ %r<declared>.sort(*.key).map({ ":{.key} ({.value})" }).join(', ')
        !! "  declares no named parameters at all";

    # Corpus lookups have to happen before the summary lines, because whether the
    # catch-all warning is even true depends on what the corpus says. Printing
    # "swallowed silently" directly above "STRICT: throws on unexpected adverbs"
    # would be exactly the self-contradicting output this tool exists to prevent.
    my @fwd = corpus('forwarding.tsv', 6).grep({ .[0] eq $type && .[1] eq $method });
    my @str = corpus('strictness.tsv', 6).grep({ .[0] eq $type && .[1] eq $method });
    my %verdict = corpus('verdicts.tsv', 4)
        .grep({ .[0] eq $type && .[1] eq $method })
        .map({ .[2] => .[3] }).Hash;
    my %effective = corpus('effective.tsv', 5)
        .grep({ .[0] eq $type && .[1] eq $method })
        .map({ .[2] => %( kind => .[3], why => .[4] ) }).Hash;

    # This is the entire point, so it goes in the main line, not a footnote --
    # unless the corpus knows this method complains, in which case it is false.
    if %r<slurpy> && !@str {
        say "  NOTE: has a catch-all (*%_, *%opts or |c). Anything not listed above";
        say "        is swallowed silently -- no error, no warning, no effect.";
    }

    # ---- corpus tier: what introspection cannot see ----------------------------
    #
    # Undeclared does NOT mean invalid. Some methods forward their named arguments on:
    # Str.subst declares nothing yet `:g` works, because it hands %options to
    # Str.match. Without the corpus this tool can only hedge; with it, it can answer.

    for @fwd -> @f {
        next if @f[3] eq 'UNKNOWN';
        my $target = (@f[3] eq 'SELF' ?? $type !! @f[3]) ~ '.' ~ @f[4];
        say "  forwards named args via {@f[2]} to $target  ({@f[5]})";
    }
    for @str -> @s {
        say "  STRICT: {@s[2]}s on unexpected adverbs ({@s[3]}) -- this method tells you."
            ~ (@s[4] ?? "  accepts: " ~ @s[4].words.map({ ":$_" }).join(' ') !! '');
    }

    my $forwards = %r<slurpy> && !%r<declared> && !@fwd && !@str && !%effective;
    if $forwards {
        say "  CAUTION: declares nothing AND has a catch-all, and the corpus knows";
        say "           nothing about it -- results below are inconclusive.";
    }

    exit 0 unless @names;
    say '';

    my $bad = 0;
    for @names -> $n {
        # Order matters: a probe that showed the adverb doing nothing outranks every
        # static signal, because that is the case where all the static signals lie.
        if %verdict{$n} && %verdict{$n} eq 'inert' {
            say "  :$n  ACCEPTED BUT INERT -- probed against this Rakudo and it changed";
            say "        nothing. Looks right, does nothing. Do not use it here.";
            $bad++;
        }
        elsif %r<declared>{$n} -> $how {
            say "  :$n  declared here ($how) -- fine";
        }
        elsif %verdict{$n} && %verdict{$n} eq 'verified' {
            say "  :$n  works -- not declared here, but probed and confirmed";
        }
        elsif %effective{$n} -> %e {
            say "  :$n  reachable -- %e<why>"
                ~ (%e<kind> eq 'compilation-only'
                    ?? "; but it is a COMPILATION adverb, inert as a runtime named" !! '');
            $bad++ if %e<kind> eq 'compilation-only';
        }
        elsif @str {
            say "  :$n  not declared -- but this method is strict, so it will tell you";
        }
        else {
            $bad++;
            say "  :$n  not declared here -- "
                ~ (@fwd ?? "check the forwarding target above"
                        !! "silently ignored unless forwarded");
        }
    }

    if $bad && %r<slurpy> && !$forwards {
        say '';
        say "$bad of {+@names} is not in this method's parameter list and will be";
        say "accepted without complaint. If output looks well-formed but empty or";
        say "zero, this is why. Confirm with a one-liner before trusting it.";
    }
    exit $bad;
}
