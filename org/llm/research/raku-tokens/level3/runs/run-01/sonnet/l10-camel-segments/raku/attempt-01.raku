#!/usr/bin/env raku
use v6;

sub find-java-files($dir) {
    my @files;
    for $dir.dir -> $entry {
        if $entry.d {
            @files.append: find-java-files($entry);
        } elsif $entry.f && $entry.basename.ends-with('.java') {
            @files.push: $entry;
        }
    }
    return @files;
}

my $split-re = / [ <?after <[a..z0..9]>> <?before <[A..Z]>> ]
                 | [ <?after <[A..Z]>>    <?before <[A..Z]><[a..z]>> ] /;

my @java-files = find-java-files('src/main/java'.IO);

my %counts;

for @java-files -> $f {
    my $basename = $f.basename;
    $basename ~~ s/'.java'$//;
    my $marked = $basename.subst($split-re, "\x[1]", :g);
    my @segments = $marked.split("\x[1]");
    for @segments -> $seg {
        next unless $seg.chars;
        %counts{$seg.lc}++;
    }
}

my @sorted = %counts.sort: { $^b.value <=> $^a.value || $^a.key cmp $^b.key };

for @sorted[0..14] -> $pair {
    print "{$pair.value}\t{$pair.key}\n";
}
