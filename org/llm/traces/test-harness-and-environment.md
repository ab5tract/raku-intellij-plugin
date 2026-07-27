# Running and testing this plugin: environment gotchas

Practical, load-bearing knowledge for getting tests to run and trusting their
results. Several of these will waste an hour if you don't know them up front.

## rakubrew must be initialized *and switched to 2026.03* in the same shell as gradle

Tests spawn a real `raku` subprocess to load symbols. If `raku` isn't on `PATH`, they
fail in `setUp()` (`CommaFixtureTestCase.suggestSdkHome()` → "Found a raku in path"
assertion, or a symbol-load timeout). Every gradle invocation must be preceded, **in
the same shell**, by:

```bash
eval "$(~/.rakubrew/bin/rakubrew init Zsh)"
rakubrew switch moar-2026.03
./gradlew test --rerun --tests "..."
```

Shell state does not persist between tool calls, so put all of them in one command.

Three ways this line goes wrong, all of which end in a green build that proves nothing:

- **The version is `moar-2026.03`, not `2026.03`.** A bare `2026.03` prints "Sorry,
  '2026.03' not found. Did you mean: moar-2026.03" — but through the `rakubrew` shell
  function that `init` installs it still returns success, so `&&` chains march on with
  the switch silently not applied. (Called directly as `~/.rakubrew/bin/rakubrew`,
  outside the hook, the same mistake exits 1.)
- **`--rerun` is not optional when you have changed only the environment.** `PATH` and
  the SDK are not declared inputs of the `test` task, so switching Rakudo leaves it
  `UP-TO-DATE`. `./gradlew test` then reports `BUILD SUCCESSFUL in 629ms` having
  executed zero tests. Check for `> Task :test UP-TO-DATE` before believing a result,
  and confirm the "N tests completed" line is present.
- **`rakubrew init` alone may already be enough**, which masks the first bullet:
  `init` exports `~/.rakubrew/versions/<CURRENT>/bin` at the front of `PATH`, and
  `~/.rakubrew/CURRENT` is already `moar-2026.03` here. The `switch` line is insurance
  against `CURRENT` having drifted, not the thing doing the work.

**The `switch` line is load-bearing, not decoration.** `suggestSdkHome()` takes the
first `PATH` entry that looks like a Raku SDK home, so without it the system Rakudo in
`/usr/bin` wins. Symbol-dependent assertions then fail in ways that impersonate plugin
bugs, because CORE.setting genuinely differs between releases:

| | 2026.03 (canonical) | 2025.08 (system) |
|---|---|---|
| `Mu.^find_method("perl").candidates[0].DEPRECATED` | `raku` | *absent* |
| `&open.candidates` | 2 — `("-", \|c)`, `($path, \|c)` | 1 — `(IO(Any) $path, \|c)` |

On 2025.08 `raku-core-symbols.raku` emits `perl` with no `x` (deprecation) key, so no
deprecation warning is possible; and `open` is no longer a multi with several
candidates, so an arity error reads "Not enough positional arguments" rather than
enumerating "No multi candidates match (...)".

**When a symbol-dependent assertion fails, check `raku -v` before you touch the
expectation.** An expectation that merely encodes a different Rakudo is not evidence of
a regression. The two cases above were the ones that bit, and `1b187385` unpinned them
deliberately (see below), so they no longer serve as examples — but the failure mode
recurs for any assertion whose text is built from SDK signatures.

Note that the deprecation lives on the *candidate*, not on the proto (`is DEPRECATED`
is a `Method+{is-DEPRECATED}` mixin), which is why probing `$m.DEPRECATED` on the proto
reports nothing even on 2026.03.

## Assertions built from CORE.setting text should not be pinned exactly

`CommaFixtureTestCase.checkHighlightingContains(vararg fragments)` asserts that the
rendered actual highlighting *contains* each fragment, for annotations whose full text
is assembled from the SDK's own signatures. `testCallArityMismatchAnnotating` uses it
for `open;`: the multi-candidate list is Rakudo's, not the plugin's, so pinning it
exactly pins a Rakudo release for no gain in coverage.

Keep a fragment that closes a span (e.g. `open</error>`) in the list, otherwise the
assertion passes even when the annotation vanishes entirely. Configure the source
*without* expectation markup when using it.

This is the right tool only when the varying part belongs to Rakudo. An annotation the
plugin composes itself should still be pinned exactly.

`~/.rakubrew/MODE` is `env` here, so rakubrew works by rewriting `PATH` from the shell
hook; there are no shims. A `PATH` that lacks the hook is the normal failure mode for
a non-interactive shell that never sourced the user's profile.

## ~~The `checkHighlighting()` pipeline is broken~~ / ~~use a checkpoint subset~~ — RESOLVED

**Both of these claims are obsolete. Do not act on them.** They were symptoms of one
harness bug, fixed in `test-harness-project-reuse.md`: the full suite now runs ~1100
tests in ~2 minutes, and `org.raku.comma.annotation.*` / `org.raku.comma.highlighting.*`
are as trustworthy as any other suite.

What was really happening: the light project was rebuilt for *every* test, so each test
respawned a ~4s `raku` CORE-symbols subprocess. Under that load a spawn would sometimes
fail and `getCoreSettingFile()` silently fell back to the stale bundled
`symbols/CORE.fallback` — so symbol-dependent highlighting assertions failed
non-deterministically, which read as "the pipeline is broken". The ~69-minute runtime
that motivated the "checkpoint subset" advice had the same single cause.

Still true and still worth doing: **direct-invocation inspection tests are a good
idea on their own merits** — faster and far more precise than a golden-highlighting
comparison. Invoke `provideVisitFunction` over the PSI and assert on
`ProblemsHolder.results` (see `MissingRoleMethodInspectionTest`,
`RedeclaredImportedSymbolInspectionTest`). For parsing, use the golden-PSI-tree
`RakuParsingTestCase` framework (see `parser-generated-lexer-architecture.md`).

## Logged errors are escalated to hard failures under test

All test JVMs run with `-Dintellij.testFramework.rethrow.logged.errors=true`. Any
`Logger.error(...)` becomes a `TestLoggerAssertionError`. So things a real IDE only
logs-and-continues (a background coroutine failing, a stub-index-during-stub-building
warning) become test failures. Two real examples handled this way:

- **CodeVision** (`c7beea6b`): the bundled Kotlin plugin's
  `KotlinScriptDefinitionCodeVisionProvider` fails a resource-bundle lookup in this
  sandbox and logs an error from a background coroutine during project startup. In
  real use it's harmless. `CommaFixtureTestCase.runBare()` scope-suppresses **only
  that one message** via `LoggedErrorProcessor` so genuine logged-error regressions
  still fail loudly. `runBare()` (not `runTestRunnable()`) is required because the
  coroutine can fire during `setUp()`, before the test body.
- **Stub-index-during-stub-building** — see `stub-building-index-queries.md`.

Pattern for suppressing exactly one known-benign message in a scratch test:
```kotlin
LoggedErrorProcessor.executeWith<Throwable>(object : LoggedErrorProcessor() {
    override fun processError(category, message, details, t) =
        if (message.contains("<known-benign-substring>")) Action.NONE
        else super.processError(category, message, details, t)
}) { /* body */ }
```

## Just run the full suite

`./gradlew test` is ~2 minutes for 1087 tests. There is no longer any reason to guess
at a "checkpoint subset" — run everything. See `test-harness-project-reuse.md` for why
it used to take over an hour.

## Tests that need a Raku module you don't have installed

`CommaFixtureTestCase.ensureModuleIsLoaded` probes the SDK (`raku -e 'use X'`, cached
per JVM) and aborts the test as `[SKIPPED]` when the module genuinely isn't installed,
instead of letting it fail later on a content assertion. `Cro::WebApp::Template`,
`Cro::WebApp::Form` and `OO::Monitors` are not installed on every dev machine, so
`GoToDeclarationTest`'s template-jump tests, `TraitCompletionTest.testAttributeTrait`
and `MethodCompletionTest.testMetaMethodCompletion` skip unless you `zef install` them.

A skip is deliberately *not* the same as a pass-with-no-symbols: if the module **is**
installed and symbol loading then fails, the test still fails. Grep the run log for
`[SKIPPED]` to see what didn't actually run.

Pass `required = false` when the test only needs the module *named* in the source it
operates on rather than resolved — `IntentionTest`'s two monitor cases rewrite code that
mentions `OO::Monitors` without ever looking a symbol up, and skipping those would have
been a silent loss of coverage. Those log `[NOTE]` instead and keep running.

## Scratch tests: write outputs to a randomized temp dir

Investigation/scratch tests should write to a fresh randomized directory under the
user temp dir (JVM `java.io.tmpdir`, the equivalent of Raku's `$*TMPDIR`), not a fixed
path, so repeated/concurrent runs don't clobber each other and echo the path so it's
discoverable:
```kotlin
val dir = Files.createTempDirectory("raku-scratch-<label>-").toFile()
File(dir, "out.txt").writeText(...); println("[scratch] wrote ${'$'}dir/out.txt")
```

## Running the sandbox IDE

`./gradlew runIde` launches a sandbox IDE with the plugin. Used to reproduce
user-facing symptoms end to end (the test suite is trustworthy again, but seeing it is
still seeing it). `.rakumod` module-file support depends on `<depends>com.intellij.modules.platform</depends>`
/ JCEF wiring in `plugin.xml` (a tab that opens and immediately closes is that
dependency missing — cf. commit `c2c45083` "Fix .rakumod loading by explicitly
depending on JCEF").
