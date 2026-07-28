# Working notes

Intermediate material, surprises and dead ends. The tidied findings are in
`../../report/raku-tokens/`.

## H1 was wrong, and wrong in an interesting direction

The prediction was that Raku would be *noticeably* more expensive per byte, because
sigils and operators fragment. Measured, it is barely more expensive at all:

| vocabulary | Raku bytes/token | Python bytes/token | Raku vs Python |
|---|---|---|---|
| cl100k_base | 3.611 | 3.888 | **−7.1%** |
| o200k_base | 3.592 | 3.842 | **−6.5%** |

−7% is a real penalty but it is nowhere near the "minority language falls off a
cliff" story the hypothesis implied. Restricting to `raku-ecosystem` alone — real
third-party module source, the cleanest Raku sample — it is 3.756 vs 3.888, about
**−3.4%**.

Two guesses at why the penalty is so small, neither tested:

- Raku's expensive-looking constructs are built from ASCII punctuation that BPE
  already merges aggressively for *every* language (`::`, `=>`, `->`, `..`). The
  sigils are single common characters, not exotic glyphs.
- Byte-level BPE has no out-of-vocabulary cliff. An unseen language degrades toward
  the per-character floor gradually; it never fails outright. The "minority language
  tax" is real but it is a slope, not a wall.

## The sampling bug that nearly shipped a wrong number

The first three runs disagreed: Raku at −5.7%, −7.2%, −10.3%. Cause:
`@files.pick(*, :seed(SEED))`. **`pick` takes no `:seed`** — it was silently
accepted and ignored, so every run drew a fresh shuffle. The "seeded, reproducible"
comment in the source was simply false, and the spread was wider than the effect.

Fixed with `srand(SEED)` before the pick, verified by running twice and diffing, and
the budget raised 400 kB → 4 MB so sampling noise shrinks below the effect. Final
figure −7.1%, which sits mid-spread — the original number was not *wrong* so much as
unsupported.

Lesson worth keeping: a silently-ignored named argument produces confidently
reproducible-looking output. The only thing that caught it was re-running from cold
and noticing the headline had moved.

**Idiom, from the user:** `(@ = .pick(*))` uses an anonymous state array to freeze a
pick, so repeated evaluation of the same expression reuses the first shuffle. That
solves freezing *within* a process; `srand` is what makes a draw reproduce *across*
processes. Different halves of the problem — this script needed the latter, but the
former is the neater tool when the same sample must be reused inside one run.

## The surprise: Java and Kotlin do *better* than Python

Java 4.91 and Kotlin 4.72 bytes/token against Python's 3.89 — 26% and 21% *cheaper
per byte*. That inverts the naive "Python dominates the training data so Python
tokenizes best" intuition.

The likely explanation is that bytes/token rewards verbosity. Java says the same
thing in more bytes, and those extra bytes are long, highly predictable identifiers
(`getDirectlyDefinedAttributes`, `TextAttributesKey`) that BPE compresses into few
tokens. Cheap *per byte* is not cheap *per unit of work* — Java needs far more bytes.

**This is why bytes/token alone cannot answer the question, and why Level 2 exists.**
Any writeup that stops at Level 1 has measured the wrong thing.

## The actual finding is the cancellation

Level 2, five real tasks, both arms verified byte-identical in output:

- Raku needed **85.5%** of Python's bytes
- Raku needed **99.1%** of Python's tokens

Raku's ~15% concision advantage is almost exactly consumed by its tokenizer penalty.
Within the paired set the per-byte gap is wider than in the corpus (2.82 vs 3.27
bytes/token, −13.7%) because these are short, dense, punctuation-heavy scripts —
precisely the register where Raku's syntax is least like the training distribution.

Net: **a wash**. Which is itself the answer, and a better one than either extreme.

Per-task spread is wide and the n is 5 — `t4-frozen-names` +8.8% against Raku,
`t5-size-histogram` −1.9%, `t1`/`t2` around −8%. Do not read individual tasks as
signal.

## Incidental Level 3 datapoint

Not sought, but it happened and pretending otherwise would be dishonest.

Writing the five paired tasks, the Raku arm had **one first-attempt failure** and the
Python arm had **zero**. The failure: `'src/main/java'.IO.dir(:R)` — invented,
`IO::Path.dir` has no `:R`. It silently matched nothing and printed four zero rows,
caught only because the runner diffs the arms. Fixed with an explicit `gather`/`take`
recursion.

Two things worth keeping from that:

1. It is exactly the fluency penalty H2 predicts, and it cost more tokens to fix than
   the whole per-byte difference on that task.
2. **It was caught only because the two arms had to agree.** A single-arm Raku script
   would have shipped, printing plausible zeros. The differential-testing property is
   worth more than the measurement it was built for.

n=1. It is an anecdote, not a result. Level 3 remains unrun.

## Dead ends and instrument notes

- **count_tokens API** — the right instrument. `api.anthropic.com` reachable but 401;
  no key configured. Everything here is a proxy as a result.
- **Cro::HTTP::Client** not installed, so the vocabulary fetch is `curl`.
- **`<[:Lu :Lt]>` is not Raku.** Unicode properties combine with `+`/`-`:
  `<:Lu +:Lt +:Lm +:Lo +:M>`.
- **`$!attribute` is invisible inside a regex** — regexes are `Cursor` methods. Copy
  to a lexical first (`my $pat = $!pattern;`).
- **`$*PROGRAM` is the entry script, not the module.** Use `$?FILE` to locate files
  relative to a `.rakumod`.
- **`.pick` silently ignores `:seed`.** See above. `srand` is the real control.
- **There is not much Raku to measure.** The ecosystem corpus exhausts at ~466 kB
  across 90 files, while Python's sample is budget-capped at 4 MB with an order of
  magnitude more available. Every Raku figure here therefore carries more sampling
  noise than its Python counterpart, and no budget increase fixes that. The scarcity
  is not a limitation of the method; it is the subject.
- **Ordered vs longest-token alternation** was the one design decision that could
  have silently invalidated everything. tiktoken is Python `re` (ordered); Raku `|`
  is LTM. The patterns use `||`. Nothing in the self-test would have caught this —
  pretokenization would still have been lossless, just wrong.

## What would strengthen this

- Run Level 3 properly, in blind sub-agent arms.
- Get a real `count_tokens` key and re-measure; report the delta between proxy and
  true tokenizer as an error bar on everything here.
- Widen Level 2 to ~20 tasks; five is too few for the per-task spread observed.
- Add a language further outside the training distribution (APL, J, Factor) to see
  whether the −5% slope steepens or whether byte-level BPE really does floor out.
