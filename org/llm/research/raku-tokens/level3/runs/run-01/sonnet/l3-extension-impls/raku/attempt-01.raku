#!/usr/bin/env raku
use v6;

my $dir = 'src/main/resources/META-INF';

my @files = $dir.IO.dir.grep({ .f && .basename.ends-with('.xml') });

my %values;

for @files -> $file {
    my $text = $file.slurp;
    for $text.match(/ 'implementation="' (<-["]>*) '"' /, :g) -> $m {
        %values{$m[0].Str} = True;
    }
}

my @sorted = %values.keys.sort;

say "{@sorted.elems} distinct";
for @sorted -> $full {
    my $simple = $full.contains('.') ?? $full.split('.')[*-1] !! $full;
    say "$simple\t$full";
}
