my @files = "src/test/kotlin".IO.dir(:test(*.ends-with(".kt")), :r).grep(*.f);

sub dir-recursive(IO::Path $dir) {
    gather for $dir.dir -> $entry {
        if $entry.d {
            take $_ for dir-recursive($entry);
        }
        elsif $entry.f && $entry.basename.ends-with(".kt") {
            take $entry;
        }
    }
}

my @results = dir-recursive("src/test/kotlin".IO).map: -> $file {
    my $max = max 0, $file.lines.map(*.chars);
    ($max, $file.Str)
};

for @results.sort({ (-.[0], .[1]) })[^5] -> ($len, $path) {
    say "$len\t$path";
}
