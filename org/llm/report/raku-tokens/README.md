# The token cost of being a minority language

**Finding: Raku costs about 7% more tokens per byte than Python and needs about 15%
fewer bytes to do the same work, so the finished program is token-neutral. The real
cost is elsewhere: getting to a *working* program takes 5–12% more, because Raku fails
silently where Python fails loudly — 4 first-attempt failures in 24 blind Raku arms
against 0 in 24 Python arms.**

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

**But this measures the finished program.** It says nothing about what it cost to
arrive at one. That is Level 3.

## Level 3 — the fluency penalty

The cost actually paid in practice is tokens to *working* code: first-attempt
correctness plus every debug round. It cannot be measured in one context, because once
both languages are in context each anchors the other. So: **48 fresh sub-agents** — 12
new tasks × 2 languages × 2 model strengths — each seeing only a task spec and one
language, with every version it ran preserved on disk.

| | first attempt exactly right | mean correction rounds | tokens to working code |
|---|---|---|---|
| Sonnet, Raku | 92% (11/12) | 0.08 | **112.2%** of Python |
| Sonnet, Python | **100%** (12/12) | 0.00 | — |
| Opus, Raku | 75% (9/12) | 0.25 | **105.3%** of Python |
| Opus, Python | **100%** (12/12) | 0.00 | — |

**Python did not fail once in 24 arms. Raku failed 4 times in 24.** Level 2's wash
(99.1%) becomes a 5–12% Raku penalty once the debug rounds are counted. A frontier
model does not erase it — both models scored a clean 100% on Python.

### The failures all have one shape

Three of the four Raku first-attempt failures **exited 0 and printed well-formed,
plausible, wrong output.** Only one crashed.

- `.dir(:r)` and `.dir(:recursive)`, on two different tasks — `IO::Path.dir` is not
  recursive and has no such adverb. Raku methods carry an implicit `*%_`, so the unknown
  named argument is silently absorbed. Both arms walked one level, matched nothing, and
  printed a full set of zeros.
- `max 0, $seq` — the `Seq` arrives as one argument, so `max` returns the `Seq` itself.
- `/[^.]+$/` — `[^...]` is not negation in Raku regex. The substitution silently did
  nothing.

Three of those four are the same defect: **a construct Raku accepts without complaint
and then ignores.** This experiment has now hit that class four separate times without
ever looking for it — `.pick(:seed)` (which nearly shipped a wrong headline),
`.dir(:R)`, and now `:r` and `:recursive`.

**That, not tokenization, is what being outside the training distribution actually
costs here.** And the expensive part is not that it fails — it is that it fails quietly.

### What bounds this

Only the *code* each arm emitted is counted; a sub-agent's reasoning and tool output
are not exposed by the harness. Debugging spends most of its tokens on things that are
not code, so **105–112% is a lower bound, not a measurement.** Blindness was enforced
by fresh context plus instruction rather than a sandbox. n=12 per cell, and 4 failures
is enough to see a pattern but not to size it. Full protocol and caveats:
`../../research/raku-tokens/level3/README.md`.

All 12 tasks came back **4-way unanimous** — two languages and two models agreeing
independently — so no ground truth here rests on a single implementation.

## What this does and does not license

**Supported.** For this repo's Raku-only rule on text processing, *finished-code* token
cost is not an argument either way — it is roughly free. What is not free is the
getting-there: a 5–12% penalty concentrated in a handful of silent failures. That is
small enough that the rule stands comfortably on project coherence, which is where it
belonged anyway. Note this cuts both ways: a result in the other direction would not
have been an argument for Python either, because the rule was never about token cost.

**Supported, and the actionable part.** Raku's expensive failure mode in this corpus is
**silently-ignored named arguments** — `:r`, `:recursive`, `:R`, `:seed`, all absorbed
by the implicit `*%_` and all producing confident zeros. If you take one thing from
this report into daily work, take that, not the percentages.

**Not supported.** Any claim about *absolute* token counts. No Anthropic API key was
available (`count_tokens` returns 401), so the instrument is a public byte-level BPE
proxy of the same family, not Claude's tokenizer. Only relative comparisons hold.

**Not measured.** The arms' *true* token usage. Level 3 counts emitted source only,
because sub-agent reasoning and tool output are not exposed by the harness — so its
figures are a floor. Also unmeasured: whether the slope steepens for a language further
outside the distribution than Raku (APL, J, Factor).

## The methodological keeper

The paired-arm design was built to measure tokens. Its more useful property turned
out to be that **every Raku bug in this experiment was caught only because independent
implementations had to agree.** Level 2 found one that way. Level 3 found three more,
and all three exited 0 while printing plausible zeros — a single-arm script would have
shipped every one of them.

That is the finding with the longest shelf life here, and it outranks the token
numbers that motivated the work: **differential testing against a second implementation
is worth more than the measurement it was built for.** Worth remembering the next time
a one-off script's output looks reasonable.
