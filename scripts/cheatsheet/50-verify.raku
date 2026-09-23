#!/usr/bin/env raku
use v6.d;
use MONKEY-SEE-NO-EVAL;

#| STAGE 50 -- run every probe against the *running* Rakudo.
#|
#| This is the honesty check, and it is not decoration. Source analysis can tell you
#| an adverb is passed along; only running it tells you the adverb does anything.
#|
#| The case that motivates the whole stage: `:i`, `:m`, `:s`, `:r`, `:P5` are
#| *compilation* adverbs. Written syntactically (`s:i/a/b/`) they work. Passed as
#| runtime named arguments to `.subst` / `.match` they are accepted, forwarded, never
#| read, and silently do nothing. Every layer of static analysis says they are valid.
#| Only the probe says otherwise.
#|
#| A probe is a Raku expression that is True iff the adverb demonstrably changes
#| behaviour. Verdicts:
#|   verified      probe ran, returned True   -- the adverb does something
#|   inert         probe ran, returned False  -- accepted and ignored
#|   unverifiable  probe threw, or did not return a Bool
#|
#| Writes scripts/cache/verdicts.tsv.

my $repo  = $*PROGRAM.parent.parent.parent;
my $cache = $repo.add('scripts/cache');
my $file  = $cache.add('probes.tsv');
die "run 40-merge.raku first" unless $file.e;

my @probes = $file.lines.skip(1).grep(*.trim.chars).map({ .split("\t") }).grep(*.elems >= 4);
die "no probes to run" unless @probes;

my $out = $cache.add('verdicts.tsv').open(:w);
$out.say: join "\t", <type method adverb verdict detail probe>;

my %tally;
for @probes -> @r {
    my ($type, $method, $adverb, $probe) = @r[0..3];

    my ($verdict, $detail) = do {
        my $result;
        # EVAL is the point here: the probes are data produced by an earlier stage,
        # and running them in-process keeps this to one Rakudo startup instead of
        # several hundred.
        { $result = EVAL($probe); CATCH { default { $result = Nil; $detail = .message.lines.head } } }
        if !$result.defined && $detail        { 'unverifiable', $detail }
        elsif $result ~~ Bool && $result      { 'verified',     '' }
        elsif $result ~~ Bool                 { 'inert',        'probe returned False' }
        else                                  { 'unverifiable', "probe returned {$result.^name}, not Bool" }
    }

    %tally{$verdict}++;
    $out.say: join "\t", $type, $method, $adverb, $verdict, ($detail // ''), $probe;
}
$out.close;

note "ran {@probes.elems} probes";
note sprintf("  %-14s %d", .key, .value) for %tally.sort(*.key);
note "wrote scripts/cache/verdicts.tsv";

# The regression that guards the whole pipeline. If :i on .subst ever comes back
# "verified", either Rakudo changed or a probe is lying -- and the cheat sheet would
# start telling people a silently-inert adverb works.
my @subst-i = $cache.add('verdicts.tsv').lines.skip(1)
    .map({ .split("\t") })
    .grep({ .[1] eq 'subst' && .[2] eq 'i' });
if @subst-i {
    my $v = @subst-i[0][3];
    note $v eq 'inert'
        ?? "regression check: .subst(:i) is inert, as expected"
        !! "REGRESSION: .subst(:i) came back '$v' -- expected inert. Investigate before trusting this corpus.";
}
else {
    note "note: no probe for .subst(:i); the compile-time-adverb trap is unverified in this run";
}
