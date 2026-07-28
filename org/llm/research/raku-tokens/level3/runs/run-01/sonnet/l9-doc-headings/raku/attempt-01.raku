#!/usr/bin/env raku
use v6;

my $dir = 'docs';

my @files = $dir.IO.dir(test => *.ends-with('.md'))
    .grep(*.f)
    .map(*.basename)
    .sort;

my @totals = 0 xx 6;

for @files -> $fname {
    my $path = "$dir/$fname";
    my @counts = 0 xx 6;

    for $path.IO.lines -> $line {
        if $line ~~ /^ $<hashes>=(<[#]>+) / {
            my $n = $<hashes>.chars;
            if $n <= 6 && $line.substr($n, 1) eq ' ' {
                @counts[$n - 1]++;
                @totals[$n - 1]++;
            }
        }
    }

    say "$path\t" ~ @counts.join(',');
}

say "TOTAL\t" ~ @totals.join(',');
