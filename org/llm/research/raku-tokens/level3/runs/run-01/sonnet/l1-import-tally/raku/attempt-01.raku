#!/usr/bin/env raku
use v6;

sub find-java-files($dir) {
    my @files;
    for $dir.dir -> $entry {
        if $entry.d {
            @files.append(find-java-files($entry));
        } elsif $entry.f && $entry.basename.ends-with('.java') {
            @files.push($entry);
        }
    }
    return @files;
}

my @java-files = find-java-files('src/main/java'.IO);

my %tally;
my $total = 0;

for @java-files -> $file {
    for $file.lines -> $line {
        if $line.starts-with('import ') && $line.ends-with(';') {
            my $name = $line.substr(7, $line.chars - 7 - 1).trim;
            %tally{$name}++;
            $total++;
        }
    }
}

my @sorted = %tally.sort({ $^a.key cmp $^b.key }).sort({ $^b.value <=> $^a.value });

for @sorted.head(10) -> $pair {
    say "{$pair.value}\t{$pair.key}";
}
say "TOTAL\t$total";
