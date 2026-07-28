#!/usr/bin/env raku
use v6;

sub find-java-files(IO::Path $path) {
    gather for $path.dir -> $p {
        if $p.d {
            take $_ for find-java-files($p);
        }
        elsif $p.f && $p.Str.ends-with('.java') {
            take $p;
        }
    }
}

my %counts = package => 0, comment => 0, blank => 0, other => 0;

for find-java-files('src/main/java'.IO) -> $file {
    my $line = $file.lines.first // '';

    if $line ~~ /^ \s* $/ {
        %counts<blank>++;
    }
    elsif $line.starts-with('package ') {
        %counts<package>++;
    }
    elsif $line.starts-with('//') || $line.starts-with('/*') {
        %counts<comment>++;
    }
    else {
        %counts<other>++;
    }
}

for <package comment blank other> -> $k {
    say "$k\t%counts{$k}";
}
