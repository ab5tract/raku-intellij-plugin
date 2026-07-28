my @totals = 0 xx 6;

for "docs".IO.dir.grep({ .f && .basename.ends-with(".md") }).map(*.Str).sort -> $path {
    my @counts = 0 xx 6;
    for $path.IO.lines -> $line {
        if $line ~~ / ^ ('#' ** 1..6) ' ' / {
            @counts[$0.chars - 1]++;
        }
    }
    say "$path\t" ~ @counts.join(",");
    @totals[$_] += @counts[$_] for ^6;
}

say "TOTAL\t" ~ @totals.join(",");
