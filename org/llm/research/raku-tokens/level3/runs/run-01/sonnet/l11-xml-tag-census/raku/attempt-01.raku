#!/usr/bin/env raku
use v6;

my $dir = 'src/main/resources/META-INF';

my @files = $dir.IO.dir.grep({ .f && .basename.ends-with('.xml') });

my %counts;

for @files -> $file {
    my $text = $file.slurp;
    for $text.match(:g, /'<' (<[A..Za..z]> <[A..Za..z0..9\-_.:]>*)/) -> $m {
        %counts{$m[0].Str}++;
    }
}

my @sorted = %counts.sort: { $^b.value <=> $^a.value || $^a.key cmp $^b.key };

for @sorted[0..9] -> $pair {
    say "{$pair.value}\t{$pair.key}";
}

say "DISTINCT\t{%counts.elems}";
