# CLAUDE.md

Loaded automatically at the start of every session in this repo. Kept short on
purpose — it holds the few things that are wrong to get wrong, and points at
`org/llm/traces/` for everything else.

## Every gradle invocation needs the rakubrew preamble

Tests spawn a real `raku` to load CORE symbols, and `suggestSdkHome()` takes the
first `PATH` entry that looks like a Raku SDK home. Without the preamble the
system Rakudo in `/usr/bin` wins, and symbol-dependent assertions fail in ways
that impersonate plugin bugs. Shell state does not persist between tool calls,
so all of it goes in one command:

```bash
eval "$(~/.rakubrew/bin/rakubrew init Zsh)"
rakubrew switch "${RAKUBREW_RAKU_VERSION:-moar-2026.03}"
./gradlew test --rerun --tests "..."
```

`RAKUBREW_RAKU_VERSION` is yours to set — export it to work against whatever
Rakudo you are targeting, and the line above follows. `moar-2026.03` is only the
default because it is what the currently pinned expectations were written
against; it is not a blessed version, and a version worth keeping should be
argued for in `org/llm/traces/test-harness-and-environment.md` rather than
hardcoded here. Note the `moar-` prefix: a bare `2026.03` prints "Sorry, not
found" and still returns success through the shell function `rakubrew init`
installs, so `&&` chains march on with the switch unapplied.

**A green build is not evidence on its own.** `PATH` and the SDK are not
declared inputs of the `test` task, so an environment change leaves it
`UP-TO-DATE` and `./gradlew test` reports success having run zero tests. Use
`--rerun`, and confirm tests actually executed before believing a result:

```bash
raku -e 'my $n = 0; for "build/test-results/test".IO.dir(test => *.ends-with(".xml")) -> $p {
    $n += +$0 if $p.slurp.substr(0, 600) ~~ / "tests=\"" (\d+) "\"" / }; say "$n tests"'
```

When a symbol-dependent assertion fails, check `raku -v` before you touch the
expectation. An expectation that merely encodes a different Rakudo is not a
regression, and "fixing" it can make things worse — that has already happened
once with the `.perl` deprecation test.

## Text processing in Raku, not Python

This is a Raku project. Ad-hoc parsing, tallying and munging — of test output,
XML, logs, anything — goes in `raku -e '...'` (or a script under `scripts/`).
Do not reach for `python3`, and do not treat "it was just a quick one-liner" as
an exception.

**One carve-out, and it is not precedent.** `org/llm/research/raku-tokens/`
contains Python under `paired/*/impl.py` as *measured artifact* — the experiment
is about the token cost of Raku versus Python, so it has to contain both. Every
harness, tokenizer and analysis script in that directory is Raku. Python may not
be introduced anywhere else, and nothing in there licenses it for delivered work.

If you are wondering whether the rule costs anything: measured, it does not.
`org/llm/report/raku-tokens/` — Raku costs ~7% more tokens per byte (±2) and
needs ~15% fewer bytes, so the two cancel and the choice is roughly
token-neutral. The rule rests on project coherence, not on that number.

## Read the traces before starting

`org/llm/traces/` is durable, in-repo, agent-authored context, and it travels
across machines in a way per-machine agent memory does not. Start at
`org/llm/traces/README.md`, which gives a reading order. In particular:

- `test-harness-and-environment.md` — the full version of the section above,
  including which assertions depend on which Rakudo release.
- `parser-generated-lexer-architecture.md` — read before touching `parsing/`;
  `MAINBraid.java` is generated from an external grammar.
- `highlighter-kotlin-and-fallbacks.md` and `docs/color-principles.md` — read
  before touching `highlighter/` or `colorSchemes/`.

When you finish a non-trivial investigation, add a trace there.
