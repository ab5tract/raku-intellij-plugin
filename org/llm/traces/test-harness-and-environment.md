# Running and testing this plugin: environment gotchas

Practical, load-bearing knowledge for getting tests to run and trusting their
results. Several of these will waste an hour if you don't know them up front.

## rakubrew must be initialized in the same shell as gradle

Tests spawn a real `raku` subprocess to load symbols. If `raku` isn't on `PATH`, they
fail in `setUp()` (`CommaFixtureTestCase.suggestSdkHome()` → "Found a raku in path"
assertion, or a symbol-load timeout). Every gradle invocation must be preceded, **in
the same shell**, by:

```bash
eval "$(~/.rakubrew/bin/rakubrew init Zsh)"
./gradlew test --tests "..."
```

Shell state does not persist between tool calls, so put both in one command.

## The `checkHighlighting()` / `doHighlighting()` pipeline is broken in this environment

Since an SDK bump, the highlighting pipeline fails even for otherwise-untouched tests
here (annotation/highlighting suites time out or error). This is environmental, not
your change. Consequences:

- **Do not** trust a red result from `org.raku.comma.annotation.*` /
  `org.raku.comma.highlighting.*` as evidence your change broke something — it may be
  the pre-existing pipeline breakage, indistinguishable from a real failure.
- **Write inspection/parser regression tests that bypass it.** For inspections, invoke
  `provideVisitFunction` directly over the PSI and assert on `ProblemsHolder.results`
  (see `MissingRoleMethodInspectionTest`). For parsing, use the golden-PSI-tree
  `RakuParsingTestCase` framework (see `parser-generated-lexer-architecture.md`).
- Reliable parser/structure suites that don't route through it:
  `org.raku.comma.parsing.*`, `org.raku.comma.cro.parsing.*`, `org.raku.comma.folding.*`,
  `org.raku.comma.formatter.*`, `org.raku.comma.stub.*`.

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

## Full-suite instability; use a checkpoint subset

The full `./gradlew test` has hung/timed out repeatedly in this environment. A curated
subset (stub + parsing + a few completion/cro suites) is the reliable signal after a
change. Run the suites relevant to what you touched, plus the parser/structure suites
above, rather than betting on a clean full run.

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
user-facing symptoms (e.g. highlighting) that the broken test-highlighting pipeline
can't. `.rakumod` module-file support depends on `<depends>com.intellij.modules.platform</depends>`
/ JCEF wiring in `plugin.xml` (a tab that opens and immediately closes is that
dependency missing — cf. commit `c2c45083` "Fix .rakumod loading by explicitly
depending on JCEF").
