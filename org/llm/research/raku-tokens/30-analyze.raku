#!/usr/bin/env raku
use v6.d;

#| Aggregates 90-corpus-per-file.tsv into per-corpus and per-language rollups.
#| Reports bytes/token (higher = the tokenizer compresses this language better)
#| and tokens/line (higher = each line of this language costs more to read).

my $tsv = $*PROGRAM.parent.add('90-corpus-per-file.tsv');
die "run 20-measure-corpus.raku first" unless $tsv.e;

my @rows = $tsv.lines.skip(1).map({
    my @f = .split("\t");
    %( corpus => @f[0], language => @f[1], bytes => +@f[3],
       lines => +@f[5], tokens => +@f[6] )
});

sub rollup(@r) {
    my $b = @r.map(*<bytes>).sum;
    my $t = @r.map(*<tokens>).sum;
    my $l = @r.map(*<lines>).sum;
    %( files => @r.elems, bytes => $b, tokens => $t, lines => $l,
       bpt => $t ?? $b / $t !! 0,
       tpl => $l ?? $t / $l !! 0 );
}

my $report = $*PROGRAM.parent.add('91-rollup.txt').open(:w);
sub emit($s) { say $s; $report.say($s) }

emit "LEVEL 1 -- tokenizer penalty over independently-authored corpora";
emit "vocabulary: cl100k_base (public proxy; not Claude's tokenizer)";
emit '';
emit sprintf("%-18s %-8s %6s %9s %9s %8s %8s", <corpus language files bytes tokens byte/tok tok/line>);
emit '-' x 72;

for @rows.categorize(*<corpus>).sort(*.key) -> $c {
    my %s = rollup($c.value);
    emit sprintf("%-18s %-8s %6d %9d %9d %8.3f %8.2f",
        $c.key, $c.value[0]<language>, %s<files>, %s<bytes>, %s<tokens>, %s<bpt>, %s<tpl>);
}

emit '';
emit "by language";
emit '-' x 72;
my %by-lang;
for @rows.categorize(*<language>).sort(*.key) -> $l {
    my %s = rollup($l.value);
    %by-lang{$l.key} = %s;
    emit sprintf("%-18s %-8s %6d %9d %9d %8.3f %8.2f",
        '', $l.key, %s<files>, %s<bytes>, %s<tokens>, %s<bpt>, %s<tpl>);
}

emit '';
emit "tokenizer penalty, relative to Python (bytes/token ratio)";
emit "  <1.00 means the language costs MORE tokens per byte than Python";
emit '-' x 72;
my $base = %by-lang<Python><bpt>;
for %by-lang.sort(*.key) -> $l {
    my $ratio = $l.value<bpt> / $base;
    emit sprintf("  %-10s %6.3f bytes/token   ratio %5.3f   %+6.1f%% vs Python",
        $l.key, $l.value<bpt>, $ratio, ($ratio - 1) * 100);
}

$report.close;
note "wrote 91-rollup.txt";
