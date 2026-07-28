# The token cost of being a minority language

**Finding: Raku costs about 7% more tokens per byte than Python, and needs about 15%
fewer bytes to do the same work. The two roughly cancel. Choosing Raku over Python
for text processing is close to token-neutral.**

Method, raw data and caveats: `org/llm/research/raku-tokens/`.
Design and hypotheses stated in advance: `../../research/raku-tokens/00-preregistration.md`.

---

## Why this was measured

Raku is a minority language. BPE merge tables are learned from corpora where Python
is enormously over-represented and Raku is nearly absent, so a byte of Raku might
cost more tokens simply because the merges that would compress it were never learned.
Against that, Raku is denser and needs fewer bytes to say the same thing.

The question is which wins. Not "which language is better" — the quantity of interest
is the tax a language pays for living outside the training distribution, and whether
its own expressiveness pays that tax off.

## Level 1 — the tokenizer penalty

Tokens per byte over code **nobody in this experiment wrote**: the Python standard
library, third-party Raku modules from the ecosystem, and this repo's Raku, Kotlin
and Java, with English prose as a baseline. Authoring bias cannot contaminate a
corpus that predates the question.

| language | files | bytes | tokens | bytes/token | vs Python |
|---|---:|---:|---:|---:|---:|
| Java | 839 | 1,753,980 | 356,927 | 4.914 | +26.4% |
| Kotlin | 253 | 756,136 | 160,283 | 4.718 | +21.3% |
| English prose | 19 | 112,748 | 27,904 | 4.041 | +3.9% |
| Python | 317 | 4,018,092 | 1,033,543 | 3.888 | — |
| **Raku** | 900 | 552,946 | 153,123 | **3.611** | **−7.1%** |

Higher bytes/token = the tokenizer compresses that language better.

**The minority-language penalty is real but modest.** −7.1% overall. Restricted to
`raku-ecosystem`, the cleanest third-party Raku sample, it is −3.4% (3.756 vs 3.888).
Byte-level BPE has no out-of-vocabulary cliff: an unseen language degrades toward the
per-character floor gradually. The tax is a slope, not a wall.

**Replicates across vocabularies.** Re-measured against `o200k_base`, an
independently trained BPE with twice the vocabulary, the ranking is unchanged and
Raku sits at −6.5%. This is a property of the languages, not of one merge table.

### Error bar, and where it comes from

At an earlier 400 kB-per-corpus budget, three different samples put Raku at −5.7%,
−7.2% and −10.3%. That spread is wider than the effect, so the budget was raised to
consume essentially the whole available population. **Treat −7% as ±2, not as three
significant figures.**

The residual noise is asymmetric and the reason is itself the subject of the report:
Python's sample is capped by budget at 4.0 MB with far more available, while
**Raku's corpus is exhausted at 553 kB** — that is all the Raku on this machine.
There is less Raku to measure, which is what being a minority language means.

### Reproducing this off-host

Two of the three corpus roots are machine-specific *in kind*: the Python standard
library moves with the OS's Python version, and the Raku ecosystem is whatever the
running Rakudo has installed. Roots are therefore resolved at run time
(`lib/Corpus.rakumod`, overridable via `CORPUS_PYTHON_STDLIB` /
`CORPUS_RAKU_ECOSYSTEM`), recorded in `95-corpus-manifest.txt`, and every path in the
data is relative to its own root with an FNV-1a content digest beside it.

`60-verify-corpus.raku` reports, on any machine, how much of the recorded corpus is
present and byte-identical. It deliberately does not assert a match — off-host, a
clean 100% would be luck. What portability buys is that **the delta is knowable**: if
it reports 40% present, these figures were measured against a materially different
corpus and should be re-derived rather than trusted. On the host that produced them
it reports 2329/2329.

### The trap in this table

Java and Kotlin score *better than Python*, which inverts the naive intuition. The
reason is that bytes/token rewards verbosity: Java's long, predictable identifiers
(`getDirectlyDefinedAttributes`) compress into very few tokens, but Java needs far
more bytes to do anything.

**Cheap per byte is not cheap per unit of work.** Level 1 alone answers the wrong
question. That is why there is a Level 2.

## Level 2 — net cost per unit of work

Five real text-processing tasks from actual work in this repo, implemented in both
languages, with the runner asserting **byte-identical output** so the comparison is
between working programs rather than sketches.

| task | Raku bytes | Raku tokens | Python bytes | Python tokens | token diff |
|---|---:|---:|---:|---:|---:|
| tally test XML | 184 | 65 | 229 | 71 | −8.5% |
| extract `[SKIPPED]` | 179 | 62 | 240 | 67 | −7.5% |
| scheme effect colors | 360 | 111 | 364 | 107 | +3.7% |
| frozen external names | 211 | 74 | 224 | 68 | +8.8% |
| file-size histogram | 371 | 151 | 469 | 154 | −1.9% |
| **total** | **1,305** | **463** | **1,526** | **467** | **−0.9%** |

**Raku needed 85.5% of Python's bytes and 99.1% of its tokens.**

Raku's ~15% concision advantage is almost exactly consumed by its tokenizer penalty.
The per-byte gap is wider here than in the corpus (2.82 vs 3.27 bytes/token) because
these are short, dense, punctuation-heavy scripts — the register where Raku's syntax
is least like the training distribution.

**Net: a wash.** Which is a better answer than either extreme, and not the one the
hypothesis predicted.

## What this does and does not license

**Supported.** For this repo's Raku-only rule on text processing, token cost is not
an argument either way. It is roughly free. The rule can rest entirely on project
coherence — which is where it belonged anyway. Note that this cuts both ways: had
the result gone the other direction it would not have been an argument for Python
either, because the rule was never about token cost. The measurement removes an
objection; it does not supply a justification.

**Not supported.** Any claim about *absolute* token counts. No Anthropic API key was
available (`count_tokens` returns 401), so the instrument is a public byte-level BPE
proxy of the same family, not Claude's tokenizer. Only relative comparisons hold.

**Not measured.** The fluency penalty — tokens to *working* code, including debug
rounds. This is the cost actually paid in practice, and it is the one place a
minority language plausibly still hurts. It cannot be measured in a single context,
because once both languages are in context each anchors the other; it needs blind
sub-agent arms. Design is written up and ready to run.

One incidental datapoint, recorded because it happened rather than because it was
sought: writing the five paired tasks, the Raku arm had one first-attempt failure
(`IO::Path.dir(:R)`, invented — it silently matched nothing) and the Python arm had
none. n=1. An anecdote, not a result. But it cost more tokens to fix than the entire
per-byte difference on that task, which is a hint about where the real cost lives.

## The methodological keeper

The paired-arm design was built to measure tokens. Its more useful property turned
out to be that **the Raku bug was caught only because two independent implementations
had to agree**. A single-arm script would have shipped, printing four plausible zero
rows. Differential testing against a second implementation is worth more than the
measurement it was built for — and is worth remembering next time a one-off script's
output looks reasonable.
