#!/usr/bin/env raku

sub collect(IO::Path $dir) {
    gather for $dir.dir -> $entry {
        if $entry.d {
            take slip collect($entry);
        } elsif $entry.f && ($entry.path.ends-with('.java') || $entry.path.ends-with('.kt')) {
            take $entry;
        }
    }
}

my @results;
my $total = 0;

for collect("src/main/java/".IO) -> $file {
    my $count = 0;
    for $file.lines -> $line {
        if $line.contains('TODO') || $line.contains('FIXME') {
            $count++;
        }
    }
    if $count >= 1 {
        @results.push: ($count, $file.path);
        $total += $count;
    }
}

for @results.sort({ -$^a[0] <=> -$^b[0] || $^a[1] cmp $^b[1] }) -> $r {
    say "$r[0]\t$r[1]";
}

say "TOTAL\t$total";
