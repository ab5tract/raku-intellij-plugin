# CLAUDE.md

Loaded automatically at the start of every session in this repo. Kept short on
purpose — it holds the few things that are wrong to get wrong, and points at
`org/llm/raku/traces/` for everything else.

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
argued for in `org/llm/raku/traces/test-harness-and-environment.md` rather than
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

**One carve-out, and it is not precedent.** `org/llm/raku/research/raku-tokens/`
contains Python under `paired/*/impl.py` as *measured artifact* — the experiment
is about the token cost of Raku versus Python, so it has to contain both. Every
harness, tokenizer and analysis script in that directory is Raku. Python may not
be introduced anywhere else, and nothing in there licenses it for delivered work.

If you are wondering whether the rule costs anything: barely.
`org/llm/raku/report/raku-tokens/` — Raku costs ~7% more tokens per byte (±2) and needs
~15% fewer bytes, so the finished program is token-neutral. Reaching a *working*
program costs 5–12% more, and the rule itself rests on project coherence, not on
either number.

## Check named arguments before you trust the output

Nearly all of that 5–12% is one failure mode. **Raku silently swallows named
arguments it does not understand** — methods carry an implicit `*%_`, so `.dir(:r)`,
`.dir(:recursive)`, `.dir(:R)` and `.pick(:seed)` are all accepted, all ignored, and
all return confident zeros. Measured, this caused 4 of 4 Raku first-attempt failures
against 0 for Python, and three of the four exited 0 while printing plausible output.
`IO::Path.dir` is **not** recursive; write the walk yourself.

Ask the running Rakudo rather than the docs, which drift from the interpreter:

```bash
raku scripts/named-args.raku IO::Path dir recursive   # exit status = names not declared
```

It prints what the method's candidates actually declare, warns when a catch-all will
eat the rest, resolves forwarding (`Str.subst` declares nothing, yet `:g` works because
it hands `%options` to `Str.match`), and flags adverbs that were probed and found
**inert**. Answers are cached per Rakudo version under `scripts/cache/`.

The rendered cheat sheet is `docs/raku-named-args.md`; how it is built and rebuilt is
`org/llm/raku/traces/raku-named-args-corpus.md`.

**Two things it cannot do.** "Not declared" is not "invalid" — the implicit `*%_` means
a declared list is a whitelist of *understood* adverbs, never an accept/reject boundary.
And a valid-looking adverb can still be dead: `:i :m :r :s :P5` are **compilation**
adverbs, so `S:i/a/b/` works but `"AAA".subst(/a/, "b", :i)` silently does nothing.

## Read the traces before starting

`org/llm/raku/traces/` is durable, agent-authored context, and it travels across
machines in a way per-machine agent memory does not.

**`org/` is a submodule** — [`org-llm-raku`](https://github.com/ab5tract/org-llm-raku),
so the plugin can be cloned without agent artifacts by anyone who would rather not
have them, and so the general Raku knowledge is usable by other projects. If `org/` is
empty, that is a clone without `--recurse-submodules` and everything referenced below
is missing:

```bash
git submodule update --init
```

Nothing in the build reads it, so a checkout without it still builds and tests. Note
the two-repo consequence: **edits under `org/` commit to `org-llm-raku`, not here**,
and this repo then needs a follow-up commit to move the gitlink. Commit and push the
submodule first, or the gitlink points at something nobody else can fetch.

Start at `org/llm/raku/traces/README.md`, which gives a reading order. In particular:

- `test-harness-and-environment.md` — the full version of the section above,
  including which assertions depend on which Rakudo release.
- `parser-generated-lexer-architecture.md` — read before touching `parsing/`;
  `MAINBraid.java` is generated from an external grammar.
- `highlighter-kotlin-and-fallbacks.md` and `docs/color-principles.md` — read
  before touching `highlighter/` or `colorSchemes/`.

When you finish a non-trivial investigation, add a trace there.
