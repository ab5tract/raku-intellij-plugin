# The named-argument cheat sheet: how it is built, and how to rebuild it

Read this before touching `scripts/named-args.raku`, `scripts/cheatsheet/`, or
`scripts/cache/`.

## Why this exists

Every Raku method carries an **implicit `*%_`**, so an unrecognised named argument is
accepted, ignored, and never reported. `IO::Path.dir(:recursive)` returns one level and
exits 0.

That is not a theoretical concern here. `org/llm/research/raku-tokens/level3/` ran 24
blind Raku arms against 24 Python arms on identical tasks: Raku failed 4 first attempts,
Python 0, and **three of the four Raku failures exited 0 while printing plausible
zeros**. Three were this exact defect (`.dir(:r)`, `.dir(:recursive)`, and historically
`.pick(:seed)` and `.dir(:R)`).

`scripts/named-args.raku` answers "does this method declare that adverb?" by asking the
running Rakudo. That is enough for 13% of the API. For the other 87% — methods that
declare nothing but have a catch-all — introspection can only shrug, because what
happens to an undeclared named argument is decided in the method *body*. The corpus is
what closes that gap.

## The pipeline

Deterministic stages, one agent fan-out in the middle. Every stage is re-runnable and
writes only to `scripts/cache/`.

```
10-inventory.raku   live introspection of every type declared in the setting source
20-harvest.raku     parse Rakudo's own adverb tables (no agents, fully deterministic)
30-slices.raku      carve the 268 setting files into 14 agent-sized slices
   [agent fan-out]  forwarding edges + strictness sites + runnable probes
40-merge.raku       dedupe fragments, compute transitive closure -> effective.tsv
50-verify.raku      run every probe against the live Rakudo -> verdicts.tsv
60-render.raku      docs/raku-named-args.md
```

Full rebuild:

```bash
eval "$(~/.rakubrew/bin/rakubrew init Zsh)" && rakubrew switch moar-2026.03
for s in 10-inventory 20-harvest 30-slices; do raku scripts/cheatsheet/$s.raku; done
# ... agent fan-out (below) ...
for s in 40-merge 50-verify 60-render; do raku scripts/cheatsheet/$s.raku; done
```

`RAKUDO_SRC` overrides the Rakudo checkout (default `~/code/raku/x.core/rakudo`).

## Division of labour, and why it is drawn there

| question | answered by | why |
|---|---|---|
| what does this method *declare*? | live introspection | version-correct by construction; docs drift from the interpreter |
| which adverbs does `m//` / `s///` / `tr///` allow? | Rakudo's own tables | it already hard-codes them; inference would be strictly worse |
| where do undeclared nameds *go*? | agents reading method bodies | not introspectable, not greppable — needs judgement |
| does the adverb actually *do* anything? | running a probe | the only source that cannot be fooled |

**Live introspection wins over source.** The Rakudo checkout happens to be exactly
`2026.03`, matching the running Rakudo, so the two agree today. `scripts/cache/SOURCE.txt`
records both the Rakudo id and the setting commit, so a future divergence is visible.

## The agent fan-out

`30-slices.raku` writes `scripts/cache/slices.tsv` (slice → file). Fourteen slices of
~20 files each, ordered so related types land together — a forwarding edge is usually
resolved by reading its target in the same slice.

Launch one agent per slice. Each writes exactly three headerless TSVs into
`scripts/cache/fragments/`:

| file | columns |
|---|---|
| `<slice>-forwarding.tsv` | type, method, idiom, target_type, target_method, evidence |
| `<slice>-strictness.tsv` | type, method, behaviour, exception, valid_set, evidence |
| `<slice>-probes.tsv` | type, method, adverb, probe |

Non-negotiables in the prompt, each of which was earned:

- **`evidence` is always `path:line`** relative to the Rakudo checkout, on every row.
  A corpus claim you cannot audit is a rumour.
- **`UNKNOWN` rather than a guess** for unresolvable forwarding targets.
- **A probe is a single self-contained Raku expression, True iff the adverb changes
  behaviour.** No file I/O, no processes, no cwd dependence — stage 50 runs them all
  in one process via `EVAL`.
- **Agents must test probes before writing them.** A probe that passes trivially is
  worse than a missing one.
- Slices with known hotspots get told so: `Str.rakumod` (s10) holds the whole
  `subst → !SUBST → match` chain and the `fetch-short-long` machinery; `IO/Path.rakumod`
  (s05) and `IO/Handle.rakumod` (s06) hold the double-hop `|c` forwards; the slice files
  (s12, s14) hold the strictest code in the setting.

To re-run one slice, delete its three fragments and relaunch that agent alone.

**`scripts/cache/fragments/` is committed on purpose.** Stages 10, 20 and 30 regenerate
from nothing in seconds; the fragments are ~14 agent-hours and cannot. Deleting them
does not "rebuild the corpus", it re-spends the fan-out. Treat them as source.

**Derive hotspot hints from `slices.tsv`, do not hand-write them.** The first run told
s06 it held `IO/Handle.rakumod`; the manifest actually puts that in s05. The s06 agent
noticed, said so, and worked its real slice — but only because the prompt also told it
to read the manifest itself. A prompt that asserts file locations without the agent
re-deriving them is a prompt that can silently mis-scope a slice.

## Reading the corpus

- `named-args.tsv` — declared parameters per method. Schema is fixed by
  `scripts/named-args.raku`; do not change it casually.
- `adverb-tables.tsv` — construct → adverb, canonical spelling, `implies`, and **kind**.
  `compilation-only` is the important one.
- `forwarding.tsv` / `strictness.tsv` — agent output, deduped, with evidence.
- `effective.tsv` — declared ∪ transitively forwarded, each row carrying its provenance
  chain.
- `verdicts.tsv` — `verified` / `inert` / `unverifiable` per probe.

## The trap worth internalising

`:i :ignorecase :m :ignoremark :r :ratchet :s :sigspace :P5` are **compilation**
adverbs. Written into the construct they work:

```raku
say "AAA".subst(/a/, "b", :i);   # "AAA" -- accepted, forwarded, never read
say S:i/a/b/ with "AAA";         # works
```

Every static signal says `:i` is valid on `.subst` — it is in Rakudo's own
`%SUBST_ALLOWED_ADVERBS`, and it is forwarded all the way down. Only running it reveals
that nothing reads it. **This is why stage 50 is not optional**, and why
`50-verify.raku` fails loudly if `.subst(:i)` ever stops coming back `inert`.

## Rakudo and Raku gotchas hit while building this

Five bugs, four of them silent. Recorded because each cost real time and each will
recur.

1. **A caught exception poisons the `.^methods` callsite.** Some HOWs (`NativeRefHOW`,
   parametric-role HOWs) genuinely have no `.methods`. Letting that throw and catching
   it made *every later type* fail with "ClassHOW.methods not found" — 762 of 995 types
   lost, sweep still reporting success. Fix: `nqp::can($t.HOW, 'methods')` **before**
   calling. Ask, don't try-and-catch.
2. **`::($name)` returns a Failure, not an exception**, for an unknown symbol. It
   detonates later in `DESTROY` — *outside* any `CATCH`'s dynamic scope, so no amount of
   wrapping catches it. Mark it handled (`.Bool`) at the point of use.
3. **A type object is never `.defined`**, so definedness cannot distinguish "resolved"
   from "failed". Use an explicit `Nil` sentinel.
4. **Itemization, in both directions.** These are the same rule seen from two sides,
   and both bit:
   - *Too much flattening.* A Hash or List assigned into an Array flattens —
     `my @queue = %(:node, :path)` gives you two Pairs, not one record. **Use `.item`
     to seal it:** `my @queue = %(node => $start, path => []).item`. (Idiom from the
     user; the first version routed around the problem with `@queue.push:` instead,
     which works but hides the intent.)
   - *Too little flattening.* A List already stored **in** a hash element is itemized,
     and `.flat` will not descend into it. `(%h<a>, %h<b>).flat` yielded two long
     strings instead of 26 adverbs — and the single-set case worked, which is how it
     stayed invisible. Use `|` slips there.
5. **`when EXPR` smartmatches against `$_`**, not against the boolean you wrote. Inside
   a `for` loop with an unrelated topic it silently produced inverted results. Use
   `if`/`elsif` when testing conditions rather than matching a topic.

And, inevitably: the first version of `10-inventory.raku` used **`.dir(:R)`** to walk
the setting — the exact bug this pipeline exists to catch, committed inside the
pipeline. It saw 178 files instead of 268 and reported success. The comment marking
that line is deliberate; leave it there.

## Two upstream findings that fell out of this

Neither was sought; both are recorded because they are real.

**`.pick(:seed)` forwards nothing at all.** Not "declares nothing" — `List.pick`,
`List.roll`, `Map.pick/roll` and `Mixy.roll` have no `|%_`, no capture, and no private
hop (`src/core.c/List.rakumod:1020,1035,1042,1093`). Verified at runtime: the adverb
binds cleanly and is dropped. The absence of a forwarding row is itself the finding.

**An unknown adverb on a 6.e multislice hangs Rakudo 2026.03.**
`hash_multislice.rakumod` declares `:$exists, :$delete, :$k, :$kv, :$p, :$v` with no
slurpy, so `:foo` fails that candidate and falls through to the
`(\initial-SELF, \value, *%_)` candidate at line 6, which calls `.List` on something
already a List and re-dispatches to itself. `postcircumfix:<{; }>(%h, ("a",), :foo)`
runs past two minutes without erroring. Its array twin declares `*%_` and silently
drops the same adverb instead. Not encoded as a strictness row — it is neither throw,
warn, nor failure.

## What this corpus is not

It is **advisory**. Because the implicit `*%_` is universal, a declared-nameds set is a
whitelist of understood adverbs, never an accept/reject boundary. Nothing here can
prove an adverb invalid — only that this method does not appear to understand it. The
rendered cheat sheet says so on its face, and it must keep saying so.

Related: `org/llm/report/raku-tokens/` (the measurement that motivated this),
`org/llm/research/raku-tokens/level3/README.md` (the blind-arm protocol).
