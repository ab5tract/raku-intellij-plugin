# Pre-registration: the token cost of being a minority language

## Provenance, stated honestly

The **hypotheses and design below were stated before any measurement was taken** —
they come from a conversation in which the question was raised and the experiment
argued about. This *document* was written alongside the code rather than strictly
before it, so treat it as a faithful record of a prior commitment, not as a
timestamped pre-registration. Where a result contradicts the hypothesis, that is
noted rather than quietly reframed. One did.

## The question

Raku is a minority language. Tokenizers are BPE merge tables learned from a corpus
in which Python is enormously over-represented and Raku is close to absent. So a
byte of Raku plausibly costs more tokens than a byte of Python, purely because the
merges that would compress it were never learned.

Against that, Raku is a denser language: it needs fewer bytes to say the same thing.

The question is which wins, and by how much. **Not** "which language is better" —
the interesting quantity is the tax a language pays for being outside the training
distribution, and whether its own expressiveness pays that tax off.

## Hypotheses (stated in advance)

- **H1** — Raku source is *fewer characters* but *more tokens* per unit of work than
  Python; sigils, twigils, `~~`, `.^methods`, `==>` and quoted-string regex literals
  fragment badly compared to Python identifiers that are single tokens.
- **H2** — Raku takes *more correction rounds* to get right on first write.
- **H3** — The tokenizer penalty is a property of the language, not of one
  vocabulary, and will replicate across independently trained merge tables.

## Design

Three levels, deliberately separated by how much authoring bias each can carry.

### Level 1 — tokenizer penalty (load-bearing)

Tokens per byte over **bodies of code nobody in this experiment wrote**: the Python
standard library, third-party Raku modules installed from the ecosystem, and this
repo's own Raku/Kotlin/Java, plus English prose as a baseline. Authoring bias cannot
contaminate a corpus that predates the question. Per-corpus byte budget of 400 kB,
seeded sampling, files over 60 kB excluded so no single file dominates.

This level carries the weight of any conclusion.

### Level 2 — net cost per unit of work (indicative)

Five real text-processing tasks taken from actual work in this repo, implemented in
both languages. **Both arms must produce byte-identical output**, verified by the
runner, so the comparison is between working programs rather than sketches.

**Known contamination:** both arms were written by one model in one context, so the
second-written arm is anchored on the first. The Raku tally written earlier in this
session was visibly a transliteration of Python that had just been written
(`slurp.substr(0,600)` mirroring `open(p).read(600)`), which is exactly the effect.
Mitigated by writing each arm in its own idiom rather than translating, and by
reporting Level 2 as indicative. Not eliminated.

### Level 3 — fluency penalty (BLOCKED, not run)

Tokens-to-*working*-code: first-attempt correctness plus every debug round. This is
the cost actually paid in practice and it is the one H2 speaks to.

It cannot be run in a single context, because "throw away the Python and continue
with Raku" is not achievable by deleting a file — once Python is in context it
anchors everything downstream. Real discard requires a session boundary: each arm in
a fresh agent that never sees the other, with only measurements returned, and the
arm order randomised or run blind.

**What to do to run it:** N tasks × 2 arms × fresh sub-agents, each given only the
task spec and the repo; record first-attempt pass/fail against the expected output,
number of correction rounds, and total tokens emitted until the arm passes.

One incidental Level 3 datapoint fell out of Level 2 and is recorded because it
happened, not because it was sought: see `99-notes.md`, `t5`.

## Instrument

`lib/BPE.rakumod` — a byte-level BPE tokenizer in Raku. Written in Raku on purpose;
measuring the cost of processing text in a minority language with a Python harness
would be a joke told at our own expense.

**It is not Claude's tokenizer.** `api.anthropic.com/v1/messages/count_tokens` is the
correct instrument and was the first choice, but no API key is configured in this
environment (the endpoint returns 401). The fallback is `cl100k_base`, a public
byte-level BPE of the same family, with `o200k_base` as a second vocabulary for H3.

Consequences, which bound every claim made from this data:

- **Absolute token counts are not Claude's.** Only *relative* comparisons between
  languages are meaningful.
- Both proxies are trained on web+code corpora with the same Python saturation that
  motivates the question, which is what makes them usable as proxies at all.
- Validated structurally by `11-tokenizer-selftest.raku`: 100,256 entries loaded, all
  256 single bytes present, pretokenization lossless over ASCII/Unicode/whitespace
  edge cases, `hello world` = 2 tokens (known cl100k value), English prose at 4.48
  bytes/token (matches the widely reported ~4).

A correctness note that matters: tiktoken's patterns are Python `re`, which is
**ordered** alternation. Raku's `|` is longest-token match. The patterns use `||`
throughout; `|` would silently produce a different and wrong pretokenization.

## The Python carve-out

`CLAUDE.md` forbids Python for text processing in this repo, with no "quick
one-liner" exception. This experiment necessarily generates Python as an
experimental arm.

**Scope of the exemption:** Python may exist under `org/llm/research/raku-tokens/`
*as measured artifact only* — a thing the experiment is about, never a thing the
experiment runs on. Every harness, tokenizer, measurement and analysis script here
is Raku. No Python may be introduced anywhere else in the repo, and nothing here is
precedent for delivered work.

Without that written boundary the next agent reads `paired/*/impl.py` as permission.

## Outputs

| file | what |
|---|---|
| `lib/BPE.rakumod` | tokenizer |
| `11-tokenizer-selftest.raku` | instrument validation |
| `20-measure-corpus.raku` | Level 1 measurement → `90-corpus-per-file.tsv` |
| `30-analyze.raku` | Level 1 rollup → `91-rollup.txt` |
| `40-measure-paired.raku` | Level 2, verifies arms agree → `92-`, `93-` |
| `50-robustness-o200k.raku` | H3 check → `94-robustness-o200k.txt` |
| `99-notes.md` | working notes, surprises, dead ends |
| `../../report/raku-tokens/` | findings |
