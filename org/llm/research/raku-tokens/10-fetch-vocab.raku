#!/usr/bin/env raku
use v6.d;

#| Downloads the public BPE vocabularies the tokenizer needs. They are ~5 MB and
#| perfectly reproducible, so they are gitignored rather than committed.
#|
#| These are OpenAI's published tiktoken encodings, used here as a *proxy* for
#| Claude's tokenizer, which has no offline distribution. See 00-preregistration.md
#| for what that does and does not license.

constant %VOCABS =
    cl100k_base => 'https://openaipublic.blob.core.windows.net/encodings/cl100k_base.tiktoken',
    o200k_base  => 'https://openaipublic.blob.core.windows.net/encodings/o200k_base.tiktoken';

my $dir = $*PROGRAM.parent.add('vocab');
$dir.mkdir;

for %VOCABS.kv -> $name, $url {
    my $dest = $dir.add("$name.tiktoken");
    if $dest.e && $dest.s > 1_000_000 {
        say "$name: already present ({$dest.s} bytes)";
        next;
    }
    say "$name: fetching...";
    my $proc = run('curl', '-fsS', '--max-time', '300', '-o', $dest.Str, $url);
    die "failed to fetch $name" unless $proc.exitcode == 0;
    say "$name: {$dest.s} bytes";
}
