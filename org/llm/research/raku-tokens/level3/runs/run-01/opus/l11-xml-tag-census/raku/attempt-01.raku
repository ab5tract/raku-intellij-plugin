my %counts;

for "src/main/resources/META-INF".IO.dir(test => *.ends-with(".xml")) -> $file {
    next unless $file.f;
    for $file.slurp ~~ m:g/ '<' (<[A..Za..z]> <[A..Za..z0..9\-_.:]>*) / -> $m {
        %counts{$m[0].Str}++;
    }
}

for %counts.sort({ -.value, .key }).head(10) -> $pair {
    say "{$pair.value}\t{$pair.key}";
}
say "DISTINCT\t{%counts.elems}";
