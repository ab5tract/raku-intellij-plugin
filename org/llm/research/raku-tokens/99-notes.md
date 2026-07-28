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

n=1. It is an anecdote, not a result. **Level 3 has since been run properly — see
below, where this anecdote turns out to have been representative.**

## Level 3, run properly: H2 holds, and the failures have one shape

48 fresh sub-agent arms: 12 new tasks × Raku/Python × Sonnet/Opus, each arm seeing only
its spec and one language. Protocol and controls in `level3/README.md`, numbers in
`97-level3-rollup.txt`.

| | first attempt exactly right | mean correction rounds |
|---|---|---|
| Sonnet, Raku | 92% (11/12) | 0.08 |
| Sonnet, Python | **100%** (12/12) | 0.00 |
| Opus, Raku | 75% (9/12) | 0.25 |
| Opus, Python | **100%** (12/12) | 0.00 |

**Python did not fail once in 24 arms. Raku failed 4 times in 24.** H2 predicted this
and H2 was right, which is worth saying plainly given H1 was wrong.

The cost shows up where Level 2 could not see it:

| | final code | tokens to *working* code |
|---|---|---|
| Sonnet | Raku 103.5% of Python | **112.2%** |
| Opus | Raku 85.1% of Python | **105.3%** |

Level 2's headline was 99.1% — a wash. Counting the debug rounds, the wash becomes a
5–12% Raku penalty. **The tokenizer was never where the cost lived.**

### All four failures were silent

This is the part worth keeping. Three of the four Raku first-attempt failures exited 0
with well-formed, plausible, wrong output; only one crashed.

- `.dir(:r)` and `.dir(:recursive)` — twice, on different tasks. **`IO::Path.dir` is not
  recursive and has no such adverb.** Raku methods carry an implicit `*%_`, so the
  unknown named argument is silently absorbed. Both arms then walked one level, matched
  nothing, and printed a full set of zeros.
- `max 0, $file.lines.map(*.chars)` — the `Seq` goes in as *one* argument, so `max`
  compares `0` against the whole `Seq` and returns the `Seq`. Every output row's first
  column became the file's entire list of line lengths. Needed `|` to flatten.
- `/\.[^.]+$/` — **`[^...]` is not negation in Raku regex**; it matches the literal
  characters `^` and `.`. The `subst` was a silent no-op, so basenames kept their
  extensions.

Three of those four are the *same bug*: **a named argument or construct that Raku
accepts without complaint and then ignores.** The experiment has now hit this class
four separate times without looking for it — `.pick(:seed)` (which nearly shipped a
wrong headline, above), `.dir(:R)` (the Level 2 anecdote), and now `:r` and
`:recursive`. It is the single most expensive thing about writing Raku in this corpus,
and it has nothing to do with tokens.

Python's stdlib answer to all of these is `os.walk`, which is one obvious thing that
either works or raises.

### The countermeasure, and why it is not the docs

The obvious response to a silent-named-argument bug is to look the method up. Two
sources were considered and both rejected:

- **Cloning the Raku docs repo.** Prose drifts from the interpreter, and this repo
  already pins expectations to a specific Rakudo (`CLAUDE.md`, and the `.perl`
  deprecation test that got "fixed" wrongly once). A docs answer that disagrees with
  the running Rakudo is worse than no answer.
- **Reading `rakudo/src`.** It does not exist on this machine, and grepping core
  source for a signature is slower and less reliable than asking the object.

The running Rakudo answers directly, which is version-correct by construction:
`scripts/named-args.raku`.

**The naive version of this does not work**, which is why it is worth writing down.
`IO::Path.^find_method('dir').signature` returns `(IO::Path $:: |)` — a bare capture
that declares nothing, so a signature check reports even the *valid* `:test` as
undeclared. The real parameter lists are on `.candidates`, and the union across
candidates is what you want:

```
IO::Path.dir   2 candidates   declares :test   + catch-all  -> :recursive is bogus
List.pick      6 candidates   declares nothing + catch-all  -> :seed is bogus
```

Two limitations, both real:

- **`|c` absorbs nameds too**, not just `*%_`. `IO::Path.lines` has one, so a check
  that only looked for named slurpies would under-report the risk on exactly the
  methods that use captures. The tool detects `.capture` as well.
- **Undeclared is not invalid.** `Str.subst` declares no named parameters at all, yet
  `:g` works — it forwards `*%options` to `Str.match`. Verified:
  `"aaa".subst("a","b",:g)` gives `bbb` while `:bogus` is dropped. So the tool reports
  "may be forwarded, verify by running it" rather than a verdict whenever a method
  declares nothing and has a catch-all. A tool built to prevent confident wrong claims
  must not make one.

Small irony worth recording: writing the checker, the first version rejected every
valid type, because `::($type)` returns a **type object and type objects are never
`.defined`**. Same family of bug as the one being tooled against — Raku accepting
something and quietly meaning a different thing than expected.

### The model axis, which was not in the original design

Opus scored *worse* on first-attempt Raku than Sonnet (75% vs 92%) while writing
markedly terser Raku (2144 vs 2685 final tokens). The plausible reading is that the
stronger model reaches for more of the language — `.dir` adverbs, `max` over a `Seq` —
and more of the language is more surface area to be silently wrong about. It is a
guess. 3 failures versus 1 at n=12 is well inside noise, and nothing here supports
"Opus is worse at Raku" as a claim.

What the model axis does establish: **a frontier model does not erase the penalty.**
Both models scored a clean 100% on Python.

### Honest limits on this run

- **Emitted-code tokens are not context tokens.** Only the code each arm wrote is
  counted; reasoning and tool output are not exposed by the harness. Since debugging
  spends most of its tokens on things that are not code, the 105–112% figures
  *understate* the effect. Lower bound, not measurement.
- **Blindness was fresh context plus instruction, not a sandbox.** The other arm's
  directory was reachable by absolute path. Fresh context is the control that matters
  and it was real; the filesystem hole was not closed.
- **Two arms self-reported minor protocol slips**, and both are recorded because they
  volunteered them: one Python arm ran an `awk` length check on one file *after*
  writing and running its program, and one Raku arm suspected its `:r` was wrong before
  running it but ran it unedited as the protocol required. Neither changes a count. No
  arm was excluded.
- All 48 arms eventually reached the correct answer, and all 12 tasks came back
  **4-way unanimous** on final output — 2 languages × 2 models agreeing independently.
  No hand adjudication was needed, which is a better ground truth than this experiment
  has had at any previous level.

## Making the data portable

First cut wrote absolute paths (`/usr/lib/python3.14/asyncio/events.py`) into the
results. That is unreadable on any other machine, and `org/llm/` exists precisely
because it travels.

Rewriting the strings would not have been enough: two of the three roots are
machine-specific *in kind*, not just in path. The Python stdlib moves with the OS's
Python version; the Raku ecosystem is whatever the running Rakudo has installed. So
roots resolve at run time and paths are recorded relative to them.

The neat part is `$*EXECUTABLE.parent.parent/share/perl6/site/sources` — that finds
the ecosystem under whichever Rakudo is *running*, which is the one the rakubrew
preamble selected. The corpus follows the version switch for free.

The important design decision was that **`60-verify-corpus.raku` does not assert a
match.** Off-host, a clean 100% would be luck, and a green check that can only pass
on one machine is worse than none. It reports how much of the recorded corpus is
present and byte-identical, so the delta is knowable — 100% here, and anything much
lower elsewhere means re-derive rather than trust.

Gotcha: FNV-1a's 64-bit offset basis `0xcbf29ce484222325` exceeds Raku's *signed*
native int and dies with "Cannot unbox 64 bit wide bigint into native integer".
Dropping to the 32-bit variant keeps the loop in native ints; Int arithmetic would
work but drags bigint maths through a per-byte pass over the whole corpus.

**And it reintroduced the reproducibility bug in a new place.** Resolving the Python
root by "most top-level `.py` files" used `.max`, and `/usr/lib64` is a symlink to
`/usr/lib` here, so two equivalent-but-differently-spelled roots tied. Whichever won
depended on `.dir`'s filesystem order; the paths then sorted differently, the seeded
sample drew different files, and the headline moved 3.888 → 3.881. Fixed with
`.resolve` to collapse symlinks, `.unique`, and an explicit `sort({ (-.value,
.key.absolute) })` instead of `.max`.

That is twice in one experiment that a plausible-looking result was actually
nondeterministic, and both times the tell was the same: **a number that moved when
nothing had changed.** The habit worth keeping is running the pipeline three times
and diffing before believing any figure — cheap, and it caught both.

Minor self-reference worth naming: the `prose-markdown` baseline reads
`org/llm/traces/*.md`, which includes docs edited in this same session. It is a
baseline, not load-bearing, but it is not a fully independent corpus either.

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

- ~~Run Level 3 properly, in blind sub-agent arms.~~ Done — see above.
- Get a real `count_tokens` key and re-measure; report the delta between proxy and
  true tokenizer as an error bar on everything here.
- Capture arms' *actual* token usage rather than emitted-code tokens, which would turn
  the Level 3 figures from a lower bound into a measurement.
- Widen Level 2 to ~20 tasks; five is too few for the per-task spread observed.
- Re-run Level 3 with a second, independent task set. Four failures is enough to see a
  pattern and not enough to size it.
- Add a language further outside the training distribution (APL, J, Factor) to see
  whether the −5% slope steepens or whether byte-level BPE really does floor out.
