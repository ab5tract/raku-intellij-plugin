#!/usr/bin/env raku
use v6;

my $stopwords = set <that this with from have what which when they then than been were will into more only does also some such>;

my $dir = 'org/llm/traces';

my @files = $dir.IO.dir.grep({ .f && .basename.ends-with('.md') });

my %counts;

for @files -> $file {
    my $text = $file.slurp.lc;
    for $text.match(/ <[a..z]> ** 4..* /, :g) -> $m {
        my $word = ~$m;
        next if $stopwords{$word};
        %counts{$word}++;
    }
}

my @top = %counts.sort({ $^b.value <=> $^a.value or $^a.key cmp $^b.key }).head(12);

for @top -> $pair {
    say "{$pair.value}\t{$pair.key}";
}
