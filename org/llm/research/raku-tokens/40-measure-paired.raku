#!/usr/bin/env raku
use v6.d;
use lib $*PROGRAM.parent.add('lib').Str;
use BPE;

#| LEVEL 2 of the experiment: net cost per unit of work.
#|
#| Level 1 asks what a *byte* of each language costs to tokenize. That is only
#| half the question, because languages differ in how many bytes they need to
#| say the same thing. Here the task is held fixed and both arms must produce
#| byte-identical output; what varies is how many tokens it took to say it.
#|
#| Contamination warning, recorded honestly: both arms were written by one
#| model in one context, so the second-written arm is anchored on the first.
#| See 00-preregistration.md -- this is why Level 2 is reported as indicative
#| and Level 1 (which no author touched) carries the load.

my $repo = $*PROGRAM.parent.parent.parent.parent.parent;
my $enc  = BPE::cl100k();
note "loaded {$enc.name}";

my $out = $*PROGRAM.parent.add('92-paired.tsv').open(:w);
$out.say: join "\t", <task lang bytes lines tokens agreed>;

my @summary;

for $*PROGRAM.parent.add('paired').dir.grep(*.d).sort(*.Str) -> $dir {
    my $task = $dir.basename;
    my $rk   = $dir.add('impl.raku');
    my $py   = $dir.add('impl.py');
    next unless $rk.e && $py.e;

    my $rk-out = run(<raku>, $rk.absolute, :cwd($repo), :out, :err).out.slurp(:close);
    my $py-out = run(<python3>, $py.absolute, :cwd($repo), :out, :err).out.slurp(:close);
    my $agreed = $rk-out eq $py-out;

    note "  $task: " ~ ($agreed ?? 'arms agree' !! 'ARMS DISAGREE');
    unless $agreed {
        $dir.add('DISAGREEMENT.txt').spurt("--- raku ---\n$rk-out\n--- python ---\n$py-out\n");
    }

    my %row;
    for (raku => $rk, python => $py) -> (:key($lang), :value($f)) {
        my $src = $f.slurp;
        my %m = bytes  => $src.encode('utf-8').bytes,
                lines  => $src.lines.grep(*.trim.chars).elems,
                tokens => $enc.count($src);
        %row{$lang} = %m;
        $out.say: join "\t", $task, $lang, %m<bytes>, %m<lines>, %m<tokens>, $agreed ?? 'yes' !! 'NO';
    }
    @summary.push: %( :$task, :$agreed, raku => %row<raku>, python => %row<python> );
}
$out.close;

my $rep = $*PROGRAM.parent.add('93-paired-rollup.txt').open(:w);
sub emit($s) { say $s; $rep.say($s) }

emit "LEVEL 2 -- tokens to express the same task (both arms verified equivalent)";
emit '';
emit sprintf("%-20s %7s %7s %7s   %7s %7s %7s   %8s",
    'task', 'rk_byt', 'rk_lin', 'rk_tok', 'py_byt', 'py_lin', 'py_tok', 'tok_diff');
emit '-' x 84;
for @summary -> %s {
    emit sprintf("%-20s %7d %7d %7d   %7d %7d %7d   %+7.1f%%",
        %s<task>, %s<raku><bytes>, %s<raku><lines>, %s<raku><tokens>,
        %s<python><bytes>, %s<python><lines>, %s<python><tokens>,
        (%s<raku><tokens> / %s<python><tokens> - 1) * 100);
}
emit '-' x 84;
my $rt = @summary.map(*<raku><tokens>).sum;
my $pt = @summary.map(*<python><tokens>).sum;
my $rb = @summary.map(*<raku><bytes>).sum;
my $pb = @summary.map(*<python><bytes>).sum;
emit sprintf("%-20s %7d %7s %7d   %7d %7s %7d   %+7.1f%%",
    'TOTAL', $rb, '', $rt, $pb, '', $pt, ($rt / $pt - 1) * 100);
emit '';
emit sprintf("Raku needed %.1f%% of Python's bytes and %.1f%% of its tokens.",
    $rb / $pb * 100, $rt / $pt * 100);
emit sprintf("Raku bytes/token %.3f vs Python %.3f in this set -- both far below their",
    $rb / $rt, $pb / $pt);
emit "Level 1 corpus figures (see 91-rollup.txt), because short punctuation-dense";
emit "scripts are the register where every language tokenizes worst.";
emit '';
emit "all arms agreed: " ~ (@summary.all.<agreed> ?? 'yes' !! 'NO -- results invalid');
$rep.close;
note "wrote 92-paired.tsv, 93-paired-rollup.txt";
