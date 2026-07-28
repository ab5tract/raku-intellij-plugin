sub java-files(IO::Path $dir) {
    gather for $dir.dir -> $entry {
        if $entry.d {
            take $_ for java-files($entry);
        }
        elsif $entry.basename.ends-with('.java') {
            take $entry;
        }
    }
}

my %counts;
my $total = 0;

for java-files('src/main/java'.IO) -> $file {
    for $file.lines -> $line {
        next unless $line.starts-with('import ') && $line.ends-with(';');
        my $name = $line.substr(7, *-1).trim;
        %counts{$name}++;
        $total++;
    }
}

for %counts.sort({ (-.value, .key) }).head(10) -> $pair {
    say "{$pair.value}\t{$pair.key}";
}
say "TOTAL\t$total";
