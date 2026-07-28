#!/usr/bin/env raku
use v6;

sub find-kt-files(IO::Path $dir --> Seq) {
    gather for $dir.dir -> $entry {
        if $entry.d {
            take $_ for find-kt-files($entry);
        } elsif $entry.f && $entry.basename.ends-with('.kt') {
            take $entry;
        }
    }
}

my $root = 'src/test/kotlin'.IO;
my @files = find-kt-files($root);

my @results;
for @files -> $file {
    my @lines = $file.lines;
    my $maxlen = @lines.map(*.chars).max // 0;
    @results.push: ($maxlen, $file.Str);
}

my @sorted = @results.sort: { -$_[0], $_[1] };

for @sorted.head(5) -> $r {
    say "$r[0]\t$r[1]";
}
