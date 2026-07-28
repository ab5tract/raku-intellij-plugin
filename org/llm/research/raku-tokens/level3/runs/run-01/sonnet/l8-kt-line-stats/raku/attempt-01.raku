#!/usr/bin/env raku
use v6;

sub find-kt-files($dir) {
    my @files;
    for $dir.dir -> $path {
        if $path.d {
            @files.append: find-kt-files($path);
        } elsif $path.f && $path.Str.ends-with('.kt') {
            @files.push: $path;
        }
    }
    return @files;
}

my $root = 'src/main/java'.IO;
my @files = find-kt-files($root);

my $files-count = @files.elems;
my $lines = 0;
my $nonblank = 0;

for @files -> $f {
    my $text = $f.slurp;
    my @parts = $text.split("\n");
    if $text.ends-with("\n") {
        @parts.pop;
    }
    $lines += @parts.elems;
    for @parts -> $line {
        $nonblank++ unless $line ~~ /^ \s* $/;
    }
}

my $mean = $nonblank / $files-count;
my $rounded = $mean.round(0.01);

print "files\t$files-count\n";
print "lines\t$lines\n";
print "nonblank\t$nonblank\n";
printf "mean\t%.2f\n", $rounded;
