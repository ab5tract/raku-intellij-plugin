sub kt-files(IO::Path $dir) {
    gather for $dir.dir -> $entry {
        if $entry.d {
            take $_ for kt-files($entry);
        }
        elsif $entry.f && $entry.basename.ends-with(".kt") {
            take $entry;
        }
    }
}

my @results = kt-files("src/test/kotlin".IO).map: -> $file {
    my $max = max 0, |$file.lines.map(*.chars);
    ($max, $file.Str)
};

for @results.sort({ (-$_[0], $_[1]) })[^5] -> ($len, $path) {
    say "$len\t$path";
}
