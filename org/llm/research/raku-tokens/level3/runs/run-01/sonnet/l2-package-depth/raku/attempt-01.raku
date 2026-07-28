#!/usr/bin/env raku
use v6;

my %depth-counts;
my $none-count = 0;

sub find-java-files($path) {
    my @files;
    for $path.IO.dir -> $entry {
        if $entry.d {
            @files.append(find-java-files($entry));
        } elsif $entry.f && $entry.Str.ends-with(".java") {
            @files.push($entry);
        }
    }
    @files;
}

for find-java-files("src/main/java").sort -> $file {
    my $found = False;
    for $file.IO.lines -> $line {
        if $line ~~ /^ 'package ' (.*) ';' $/ {
            my $pkg = ~$0;
            $pkg .= trim;
            my $depth = $pkg.split('.').elems;
            %depth-counts{$depth}++;
            $found = True;
            last;
        }
    }
    $none-count++ unless $found;
}

for %depth-counts.keys.sort(+*) -> $d {
    say "$d\t%depth-counts{$d}";
}
say "none\t$none-count";
