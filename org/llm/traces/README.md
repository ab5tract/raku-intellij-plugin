# org/llm/traces — agent-authored investigation traces & planning

This directory is durable, in-repo context written **by AI agents, for AI agents**
(and curious humans). It captures the *history and intent* behind non-obvious work:
root causes, dead ends, design decisions, and the environment knowledge you need to
be productive here.

## Why this exists (and why it's committed)

Agents keep per-machine memory under `~/.claude/…/memory/`. That memory does **not**
travel across computers. This project is worked on from multiple machines, so the
subset of knowledge worth sharing across all of them lives **here, in the repo**,
where it travels with `git`. If you're an agent starting cold on this repo, read this
directory first — it's the fastest path to the context your predecessors paid for.

When you finish a non-trivial investigation, add a trace here. Keep memory for the
machine-local / user-preference stuff; put transmissible engineering history here.

## Start here (recommended reading order)

1. **`test-harness-and-environment.md`** — how to run tests at all (`rakubrew init`
   **plus `rakubrew switch "${RAKUBREW_RAKU_VERSION:-moar-2026.03}"`**, note the
   `moar-` prefix; logged-errors-as-failures; skipping when a Raku module isn't
   installed). Read this before running anything: on the wrong Rakudo the suite fails
   in ways that impersonate plugin bugs, and `--rerun` is what stops gradle reporting
   a green `UP-TO-DATE` build that ran nothing.
1b. **`test-harness-project-reuse.md`** — why the suite used to take ~69 minutes and
   produce flaky, symptom-diverse failures, and what fixed it. Read it if any older
   note tells you the highlighting pipeline is broken or to run a "checkpoint subset";
   both claims are obsolete.
2. **`parser-generated-lexer-architecture.md`** — `MAINBraid.java` is *generated*;
   its grammar source is *external*; how the runtime machine works; how to instrument
   and debug it; the mirror-fix discipline. **Read before touching `parsing/`.**

## Investigation traces (worked examples)

- **`parser-reduce-metaop-mislexing.md`** — `[and]`/`[[]]` reduce metaops mis-lexed,
  collapsing highlighting from mid-file to EOF. Two root causes, one principle
  ("a metaop wraps a *complete* base operator regardless of precedence"). PR #47.
- **`inspection-private-role-methods.md`** — `MissingRoleMethodInspection` wrongly
  required private (`!`) role methods; Raku only enforces *public* yada-stubs. Settled
  by asking rakudo directly. PR #47.
- **`inspection-redeclared-imported-symbol.md`** — a golden test red since Sept 2024
  because the feature it asserted had been commented out on purpose. Reimplemented as
  `RedeclaredImportedSymbolInspection` rather than re-enabled.
- **`stub-building-index-queries.md`** — a whole class of latent bug: querying the
  stub index during stub building (silent in prod, hard failure under test). One path
  fixed (Option A); a second, more pervasive path documented as known-open — **that
  second path is now fixed too**, see the doc.
- **`test-harness-project-reuse.md`** — the light project was rebuilt for every test.
  ~69 min → ~2 min, and three of the "pre-existing failures" evaporated.
- **`highlighter-kotlin-and-fallbacks.md`** — the highlighter package was already
  using `TextAttributesKey` fallbacks; the bundled `colorSchemes/*.xml` were what
  defeated them. Also: one concept split across three hand-maintained lists, and
  the "Hash Composer" row that edited array composers.
- **`raku-metaoperators-and-user-infixes.md`** — one plain `sub infix:<smoosh>`
  declaration gets you `smoosh=`, `[smoosh]`, `Xsmoosh`, `Zsmoosh`, `Rsmoosh`,
  `<<smoosh>>` for free, no multi required. Verified table, plus two non-obvious
  constraints (`!op` needs iffiness inherited via `is equiv`, which then blocks
  `[op]`). Companion to `parser-reduce-metaop-mislexing.md`: the lexer must recognise
  a metaop shell around an operator name it has never seen.
- **`raku-named-args-corpus.md`** — Raku silently swallows named arguments it does not
  understand (`.dir(:recursive)` returns one level and exits 0). Measured: 4 of 4
  first-attempt failures in a blind trial. Documents `scripts/named-args.raku`, the
  pipeline that builds its corpus from the Rakudo source, and five silent Raku/Rakudo
  gotchas hit while building it — including a caught exception that poisons the
  `.^methods` callsite and loses 762 of 995 types while reporting success.

## Kotlin-conversion roadmap docs (ongoing Java→Kotlin migration)

The plugin is being incrementally converted from Java to Kotlin. These are the
per-item plans/journeys (the *why*, companion to the throwaway `~/.claude/plans/*`
*what/when*):

- **`kotlin-conversion-external-psi-plan.md`** — item 1: external/synthetic PSI +
  typed JSON wire model for `raku-*-symbols.raku` output.
- **`kotlin-conversion-symbol-walks-plan.md`** — item 2: the stub-vs-AST
  symbol-contribution walks (completion/resolution/find-usages load-bearing wall).
- **`kotlin-conversion-arity-matcher-plan.md`** — item 3: `RakuSignature` arity
  matcher / lexical resolution.
- **`kotlin-conversion-stub-package-journey.md`** — the `psi/stub` package conversion
  (STUB_VERSION serialization safety; one commit per PSI kind). See also
  `stub-building-index-queries.md`.

## Operational howtos

- **`bumping-supported-idea-version.md`** — releasing RIP for a new IDEA version
  (`.versions/*`, `./gradlew buildPlugin`).

## Sibling directories

- **`org/llm/research/`** — experiment harnesses, raw data and working notes.
  Method and mess; read when you want to check or extend a result.
- **`org/llm/report/`** — the tidied findings those experiments produced. Read when
  you just want the answer. `report/raku-tokens/` measures what Raku's minority
  status actually costs in tokens. Short version: the *finished program* is
  token-neutral, but getting to a working one costs 5–12% more, and nearly all of
  that is one failure mode — see `raku-named-args-corpus.md`.

## Conventions

- One investigation → one Markdown file, kebab-case name by topic (`area-symptom.md`).
- Lead with the symptom and the fix's landing point (commit/PR + test), then the root
  cause, then the transferable principle. Optimize for an agent landing cold.
- **Branch names in these docs are historical.** Work this session was done on
  `2026.2-beta.3` and squash-merged to `main` via PR #47 (so original commit hashes
  like `4aade331` are not `main` ancestors, but the changes are present). Earlier docs
  reference branches like `2026.1-beta.2`. Trust file paths, test names, and PR
  numbers over branch/hash references.
- Prefer verifiable anchors: exact file paths, rule/state numbers, test class names,
  and reproducing one-liners (`raku -e '...'`) over prose.
- **Ad-hoc text processing goes in Raku, not Python.** Any one-liner you leave behind
  here — tallying test XML, scraping a log, munging generated output — should be
  runnable with `raku -e`. See `CLAUDE.md`.
