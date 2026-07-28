#!/usr/bin/env raku
use v6.d;
use lib $*PROGRAM.parent.add('lib').Str;
use BPE;
use Rows;

#| Robustness check. Re-measures the Level 1 corpus against o200k_base -- a
#| second, independently trained byte-level BPE with twice the vocabulary.
#| If a language's position survives a different merge table, the effect is a
#| property of the language's surface syntax and not of one tokenizer's
#| idiosyncrasies. Reads the file list from the cl100k run so both measure
#| exactly the same bytes.

my $enc = BPE::o200k();
note "loaded {$enc.name}: {$enc.rank.elems} entries";

my %bytes; my %tokens; my $skipped = 0;
for Rows::read($*PROGRAM.parent.add('90-corpus-per-file.tsv'), $*PROGRAM.IO) -> %r {
    my $text = %r<file> ?? (try %r<file>.slurp) !! Str;
    without $text { $skipped++; next }
    %bytes{%r<language>}  += $text.encode('utf-8').bytes;
    %tokens{%r<language>} += $enc.count($text);
}
note "  $skipped recorded files unreadable here -- run 60-verify-corpus.raku" if $skipped;

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
