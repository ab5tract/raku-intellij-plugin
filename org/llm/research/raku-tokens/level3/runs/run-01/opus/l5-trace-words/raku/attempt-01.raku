my $stopwords = set <
    that this with from have what which when they then than been
    were will into more only does also some such
>;

my %counts;
for "org/llm/traces".IO.dir(test => *.ends-with(".md")).grep(*.f) -> $file {
    for $file.slurp.lc.comb(/ <[a..z]>+ /) -> $word {
        next if $word.chars < 4 || $stopwords{$word};
        %counts{$word}++;
    }
}

for %counts.sort({ -.value, .key }).head(12) -> $pair {
    say "{$pair.value}\t{$pair.key}";
}
