# CLAUDE.md

Loaded automatically at the start of every session in this repo. Kept short on
purpose — it holds the few things that are wrong to get wrong, and points at
`org/llm/raku/traces/` for everything else.

## Every gradle invocation needs the newest Rakudo on PATH

Tests spawn a real `raku` — to load CORE symbols, and for the RakuAST viewer to
build the AST whose spans and slots the tests assert against. `suggestSdkHome()`
takes the *first* `PATH` entry that looks like a Raku SDK home, so without this
the system Rakudo in `/usr/bin` wins and symbol-dependent assertions fail in ways
that impersonate plugin bugs. Shell state does not persist between tool calls,
so all of it goes in one command:

```bash
export PATH="$RAKU_PREFIX/bin:$PATH"
./gradlew test --rerun --tests "..."
```

**Run against the newest Rakudo you have, and do not pin one.** `$RAKU_PREFIX` is
the source build this work is developed against: it carries the latest deparse
rules and the accurate RakuAST node origins the viewer is built on. Without a
source build, take the newest release rakubrew offers — `rakubrew list-available`
prints them oldest-first, so the last entry is the one you want — and switch in
the same shell. Note the `moar-` prefix there: a bare `2026.08` prints "Sorry,
not found" and still returns success through the shell function `rakubrew init`
installs, so `&&` chains march on with the switch unapplied.

This file used to default to `moar-2026.03`. Pinning does not stop expectations
from encoding a Rakudo release; it only chooses which one they silently encode,
and then rots as the code moves. Held at 2026.03, all eight RakuAST viewer tests
failed — that release reports no span for `StrLiteral` at all — and nothing was
wrong with the plugin. See
`org/llm/raku/traces/test-harness-and-environment.md`.

**A green build is not evidence on its own.** `PATH` and the SDK are not
declared inputs of the `test` task, so an environment change leaves it
`UP-TO-DATE` and `./gradlew test` reports success having run zero tests. Use
`--rerun`, and confirm tests actually executed before believing a result:

```bash
raku -e 'my $n = 0; for "build/test-results/test".IO.dir(test => *.ends-with(".xml")) -> $p {
    $n += +$0 if $p.slurp.substr(0, 600) ~~ / "tests=\"" (\d+) "\"" / }; say "$n tests"'
```

When a symbol-dependent assertion fails, check `raku -v` before you touch the
expectation, and confirm the difference by asking Rakudo directly rather than
inferring it from the test. An expectation that merely encodes a different
Rakudo is not a regression, and "fixing" it blindly can make things worse — that
has already happened once with the `.perl` deprecation test. When they genuinely
differ, move the *expectation* forward; do not pin the environment back.

## Text processing: Raku preferred, Python allowed for now

This is a Raku project, and ad-hoc parsing, tallying and munging — of test
output, XML, logs — reads better as `raku -e '...'`, or a script under
`scripts/`. Prefer it.

**The prohibition that used to stand here is lifted, deliberately and
temporarily.** It said not to reach for `python3` at all, and not to treat "just
a quick one-liner" as an exception. It is worth having again — but *after*
`raku-master` exists. The point of that work is to make the Raku option the easy
one, and a rule that leans on discipline instead of ergonomics is the wrong way
round. Revisit this then, not before.

`org/llm/raku/research/raku-tokens/` contains Python under `paired/*/impl.py` as
*measured artifact*: the experiment compares the token cost of Raku against
Python, so it has to contain both. Every harness, tokenizer and analysis script
in that directory is Raku and should stay that way — the data is only comparable
if the instrument does not change.

What the choice actually costs, from `org/llm/raku/report/raku-tokens/`: Raku
runs ~7% more tokens per byte (±2) and needs ~15% fewer bytes, so the finished
program is token-neutral. Reaching a *working* one costs 5–12% more, and nearly
all of that is a single failure mode — the one the next section is about.

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
