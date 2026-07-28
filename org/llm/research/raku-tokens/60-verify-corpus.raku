#!/usr/bin/env raku
use v6.d;
use lib $*PROGRAM.parent.add('lib').Str;
use Corpus;
use Rows;

#| Portability check. Answers, on whatever machine it is run: how much of the
#| recorded corpus still exists here, and is it byte-identical to what was
#| measured?
#|
#| It deliberately does *not* assert a match. The Python standard library moves
#| with the OS's Python version and the Raku ecosystem is whatever the running
#| Rakudo has installed, so a clean 100% on another machine would be luck. What
#| portability buys is that the delta is *knowable* -- if this reports 40%
#| present, the numbers in the report were measured on a materially different
#| corpus and should be re-derived rather than trusted.

my $tsv = $*PROGRAM.parent.add('90-corpus-per-file.tsv');
die "run 20-measure-corpus.raku first" unless $tsv.e;

say "corpus roots as resolved on THIS machine";
say '-' x 78;
my %root = Rows::roots($*PROGRAM.IO);
for %root.sort(*.key) -> $r {
    say sprintf("  %-16s %s", $r.key, $r.value ?? $r.value.absolute !! '(unavailable here)');
}

my (%total, %present, %identical);
for Rows::read($tsv, $*PROGRAM.IO) -> %r {
    %total{%r<corpus>}++;
    next unless %r<file> && %r<file>.e;
    %present{%r<corpus>}++;
    my $text = try %r<file>.slurp;
    next without $text;
    %identical{%r<corpus>}++ if Corpus::digest($text) eq %r<digest>;
}

say '';
say sprintf("%-16s %8s %8s %10s %10s", <corpus recorded present identical verdict>);
say '-' x 78;
my ($t, $i) = 0, 0;
for %total.sort(*.key) -> $c {
    my $n = $c.value;
    my $p = %present{$c.key}   // 0;
    my $d = %identical{$c.key} // 0;
    $t += $n; $i += $d;
    say sprintf("%-16s %8d %8d %10d %9.0f%%", $c.key, $n, $p, $d, $d / $n * 100);
}
say '-' x 78;
say sprintf("%-16s %8d %8s %10d %9.0f%%", 'TOTAL', $t, '', $i, $i / $t * 100);
say '';
say $i == $t
    ?? "Every recorded file is present and byte-identical here."
    !! "This machine differs from the one that produced the data. That is expected\n"
     ~ "off-host; treat the published figures as measured elsewhere and re-run\n"
     ~ "20-measure-corpus.raku to get numbers for this machine.";
