my @counts;

sub walk($dir) {
    for $dir.dir.sort -> $entry {
        if $entry.d {
            walk($entry);
        }
        elsif $entry.extension eq 'java' | 'kt' {
            my $n = $entry.lines(:enc<utf8-c8>).grep({ .contains('TODO') || .contains('FIXME') }).elems;
            @counts.push($entry.Str => $n) if $n > 0;
        }
    }
}

walk('src/main/java'.IO);

for @counts.sort({ -.value, .key }) -> $p {
    say "{$p.value}\t{$p.key}";
}
say "TOTAL\t{@counts.map(*.value).sum}";
