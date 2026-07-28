#!/usr/bin/env raku
use v6.d;
use lib $*PROGRAM.parent.add('lib').Str;
use BPE;

#| Robustness check. Re-measures the Level 1 corpus against o200k_base -- a
#| second, independently trained byte-level BPE with twice the vocabulary.
#| If a language's position survives a different merge table, the effect is a
#| property of the language's surface syntax and not of one tokenizer's
#| idiosyncrasies. Reads the file list from the cl100k run so both measure
#| exactly the same bytes.

my $enc = BPE::o200k();
note "loaded {$enc.name}: {$enc.rank.elems} entries";

my %bytes; my %tokens;
for $*PROGRAM.parent.add('90-corpus-per-file.tsv').lines.skip(1) -> $line {
    my @f = $line.split("\t");
    my $text = try @f[2].IO.slurp;
    next without $text;
    %bytes{@f[1]}  += $text.encode('utf-8').bytes;
    %tokens{@f[1]} += $enc.count($text);
}

my $out = $*PROGRAM.parent.add('94-robustness-o200k.txt').open(:w);
sub emit($s) { say $s; $out.say($s) }

emit "ROBUSTNESS -- Level 1 re-measured with o200k_base (199998 entries)";
emit '';
emit sprintf("%-10s %9s %9s %9s %10s", <language bytes tokens byte/tok vs_Python>);
emit '-' x 50;
my $base = %bytes<Python> / %tokens<Python>;
for %bytes.keys.sort -> $l {
    my $bpt = %bytes{$l} / %tokens{$l};
    emit sprintf("%-10s %9d %9d %9.3f %10.3f", $l, %bytes{$l}, %tokens{$l}, $bpt, $bpt / $base);
}
$out.close;
