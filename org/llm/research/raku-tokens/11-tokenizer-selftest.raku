#!/usr/bin/env raku
use v6.d;
use lib $*PROGRAM.parent.add('lib').Str;
use BPE;

#| Structural validation of the Raku BPE implementation. We have no ground-truth
#| token counts to compare against offline (that is the whole reason this is a
#| proxy instrument), so instead we assert the properties a correct byte-level
#| BPE must have. A tokenizer that passes all of these can still disagree with
#| tiktoken on vocabulary, but it cannot be structurally broken.

my $enc = BPE::cl100k();
say "loaded {$enc.name}: {$enc.rank.elems} vocabulary entries";

my $fails = 0;
sub ok($cond, $desc) {
    say ($cond ?? "  ok   " !! "  FAIL ") ~ $desc;
    $fails++ unless $cond;
}

# 1. Vocabulary sanity.
ok $enc.rank.elems > 100_000, "cl100k vocabulary is ~100k entries";
ok $enc.rank{' '}:exists,     "single space is in the vocabulary";
ok $enc.rank{'a'}:exists,     "single 'a' is in the vocabulary";

# 2. Every single byte must be representable, or merging can fail to terminate
#    on arbitrary input.
my $missing-bytes = (^256).grep({ !($enc.rank{ .chr }:exists) }).elems;
ok $missing-bytes == 0, "all 256 single bytes present (missing: $missing-bytes)";

# 3. Pretokenization must be lossless -- the concatenation of all matches has
#    to reproduce the input exactly. This is the property most likely to break
#    if Raku's LTM alternation crept in where ordered alternation was meant.
for 'hello world',
    "def f(x):\n    return x + 1\n",
    'my @a = 1, 2, 3; say @a.sum;',
    "\t\tindented\r\n\r\nblank lines",
    'unicode: café ☃ 日本語',
    'sigils: $x @y %z &f ~~ ==> .^name',
    ''
-> $s {
    ok $enc.verify-roundtrip($s), "pretokenization is lossless: {$s.subst("\n", '\n', :g).substr(0, 34)}";
}

# 4. Counting invariants.
ok $enc.count('') == 0, "empty string is 0 tokens";
ok $enc.count('a') == 1, "'a' is 1 token";
ok $enc.count('hello world') == 2, "'hello world' is 2 tokens (known cl100k value)";

# 5. A token can never be cheaper than 1 per ~byte, and English prose should
#    land near the widely-reported ~4 bytes/token.
my $prose = 'The quick brown fox jumps over the lazy dog. ' x 20;
my $bpt   = $prose.encode('utf-8').bytes / $enc.count($prose);
ok 3.0 < $bpt < 6.0, "English prose is {$bpt.fmt('%.2f')} bytes/token (expected 3-6)";

# 6. Monotonicity: concatenating a string with itself cannot more than double.
my $one = $enc.count($prose);
my $two = $enc.count($prose ~ $prose);
ok $two <= 2 * $one + 1, "counting is sub-additive under concatenation";

say '';
say $fails == 0 ?? "SELFTEST PASS" !! "SELFTEST FAIL ($fails)";
exit($fails == 0 ?? 0 !! 1);
