#!/usr/bin/env raku
use v6.d;

#| STAGE 40 -- merge the agent fragments and compute the effective adverb sets.
#|
#| Inputs:
#|   scripts/cache/named-args.tsv       what each method *declares*      (stage 10)
#|   scripts/cache/adverb-tables.tsv    Rakudo's own adverb tables       (stage 20)
#|   scripts/cache/fragments/*.tsv      forwarding / strictness / probes (agents)
#|
#| Outputs:
#|   scripts/cache/forwarding.tsv   deduped edges, with slice provenance
#|   scripts/cache/strictness.tsv   methods that complain instead of dropping silently
#|   scripts/cache/probes.tsv       runnable checks, for stage 50
#|   scripts/cache/effective.tsv    declared + transitively forwarded, with provenance
#|
#| `effective.tsv` is the payload. Every row says *why* it is believed, because an
#| adverb list without provenance is indistinguishable from a guess.

my $repo  = $*PROGRAM.parent.parent.parent;
my $cache = $repo.add('scripts/cache');
my $frag  = $cache.add('fragments');

die "run 10-inventory.raku first" unless $cache.add('named-args.tsv').e;
die "run 20-harvest.raku first"   unless $cache.add('adverb-tables.tsv').e;
die "no fragments -- run the agent fan-out (see org/llm/traces/raku-named-args-corpus.md)"
    unless $frag.d && $frag.dir(test => *.ends-with('.tsv'));

sub rows(IO::Path $f, Int $cols) {
    return () unless $f.e;
    $f.lines.grep(*.trim.chars).map({ .split("\t") }).grep({ .elems >= $cols });
}

# ------------------------------------------------- collect agent fragments

sub collect(Str $kind, Int $cols) {
    my @all;
    for $frag.dir(test => *.ends-with("-$kind.tsv")).sort(*.basename) -> $f {
        my $slice = $f.basename.subst(/'-' $kind '.tsv' $/, '');
        for rows($f, $cols) -> @r {
            @all.push: %( slice => $slice, cells => @r );
        }
    }
    @all;
}

my @forwarding = collect('forwarding', 6);
my @strictness = collect('strictness', 6);
my @probes     = collect('probes',     4);

note "fragments: {@forwarding.elems} forwarding, {@strictness.elems} strictness, {@probes.elems} probes";

sub write-merged(Str $name, @header, @recs) {
    my $out = $cache.add($name).open(:w);
    $out.say: join "\t", |@header, 'slice';
    my %seen;
    my $n = 0;
    for @recs -> %r {
        my $key = %r<cells>.join("\t");
        next if %seen{$key}++;
        $out.say: join "\t", |%r<cells>, %r<slice>;
        $n++;
    }
    $out.close;
    note "  wrote $n rows to $name";
    $n;
}

write-merged('forwarding.tsv', <type method idiom target_type target_method evidence>, @forwarding);
write-merged('strictness.tsv', <type method behaviour exception valid_set evidence>, @strictness);
write-merged('probes.tsv',     <type method adverb probe>,                            @probes);

# ------------------------------------------------------------ declared sets

my %declared;   # "Type.method" => Set of adverb names
my %catchall;
for rows($cache.add('named-args.tsv'), 6).skip(1) -> @r {
    my ($rk, $type, $method, $cands, $slurpy, $decl) = @r;
    my $key = "$type.$method";
    %declared{$key} = $decl.split(',').grep(*.chars).map(*.split(':')[0]).Set;
    %catchall{$key} = $slurpy eq 'yes';
}
note "declared sets for {%declared.elems} methods";

# ---------------------------------------------------------- forwarding graph

my %edges;   # "Type.method" => [ "Target.method", ... ]
for rows($cache.add('forwarding.tsv'), 6).skip(1) -> @r {
    my ($type, $method, $idiom, $tt, $tm, $ev) = @r;
    next if $tt eq 'UNKNOWN' || $tm eq 'UNKNOWN';
    my $from = "$type.$method";
    my $to   = ($tt eq 'SELF' ?? $type !! $tt) ~ ".$tm";
    next if $from eq $to;
    %edges{$from}.push: $to;
}
note "{%edges.elems} methods forward somewhere";

#| Adverbs reachable from a method: its own declared set, plus everything declared by
#| whatever it forwards to, transitively. Returns name => shortest provenance chain.
sub reachable(Str $start) {
    my %found;
    # Hashes, not nested arrays: `[$node, ()]` silently flattens the empty list away,
    # so the shape of a queue entry changed depending on how long the path was, and
    # $node came back undefined once paths got deep enough.
    # `.item`: assigning a Hash into an Array flattens it into Pairs, so a bare
    # `= %(...)` would leave the queue holding `node => "Str.subst"` rather than the
    # record itself. `.item` seals it into a single element, which says what is meant
    # directly instead of routing around the flattening with a push.
    my @queue = %( node => $start, path => [] ).item;
    my %seen  = $start => True;
    while @queue {
        my %cur  = @queue.shift;
        my $node = %cur<node>;
        my @path = |%cur<path>;
        for (%declared{$node} // ∅).keys -> $adverb {
            %found{$adverb} //= @path ?? "via {@path.join(' -> ')} -> $node" !! 'declared';
        }
        for (%edges{$node} // ()).list -> $next {
            # Guard against cycles: Cool.subst -> Str.subst -> Cool.subst is real.
            next if %seen{$next}++;
            @queue.push: %( node => $next, path => (|@path, $node) );
        }
    }
    %found;
}

# ------------------------------------------- construct-level adverb metadata

# Rakudo's own tables are keyed by syntax construct, not by method. Map them across
# so `.subst(:i)` inherits the s/// knowledge that :i is compilation-only.
my %construct-of =
    'Str.subst'  => 's///',  'Cool.subst'  => 's///',
    'Str.match'  => 'm//',   'Cool.match'  => 'm//',   'Any.match' => 'm//',
    'Str.trans'  => 'tr///', 'Cool.trans'  => 'tr///',
;
my %kind-of;   # "construct\tadverb" => kind
for rows($cache.add('adverb-tables.tsv'), 6).skip(1) -> @r {
    my ($construct, $adverb, $canon, $implies, $kind, $source) = @r;
    %kind-of{"$construct\t$adverb"} = $kind;
}

# ------------------------------------------------------------------ effective

my $out = $cache.add('effective.tsv').open(:w);
$out.say: join "\t", <type method adverb kind provenance>;

my $rows = 0;
my @interesting = %edges.keys.Slip, %declared.keys.grep({ %declared{$_}.elems }).Slip;
for @interesting.unique.sort -> $key {
    my ($type, $method) = $key.split('.', 2);
    my %found = reachable($key);

    # Adverbs Rakudo's construct tables know about, that signatures never mention.
    if my $construct = %construct-of{$key} {
        for %kind-of.keys.grep(*.starts-with("$construct\t")) -> $k {
            my $adverb = $k.split("\t")[1];
            %found{$adverb} //= "$construct table";
        }
    }

    next unless %found;
    for %found.keys.sort -> $adverb {
        my $construct = %construct-of{$key} // '';
        my $kind = $construct ?? (%kind-of{"$construct\t$adverb"} // 'runtime') !! 'runtime';
        $out.say: join "\t", $type, $method, $adverb, $kind, %found{$adverb};
        $rows++;
    }
}
$out.close;

note "wrote $rows rows to effective.tsv";

# A corpus that silently came out tiny is worse than one that failed: every later
# stage would treat the gap as fact.
die "effective.tsv looks empty -- did the fan-out produce fragments?" if $rows < 50;
