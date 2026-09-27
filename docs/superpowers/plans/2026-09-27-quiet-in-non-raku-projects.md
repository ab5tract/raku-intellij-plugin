# Staying Quiet in Non-Raku Projects Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Four plugin features stop firing in projects that contain no Raku, the verdict is asked of the index rather than cached, and a project can request an ecosystem refresh from the butterfly menu.

**Architecture:** A new `RakuProjectKind` object answers two questions — *does this project contain Raku files* (via `FileTypeIndex`) and *is it a Raku distribution* (META6.json). Four call sites gate on one of those. `RakuEcosystem` gains a real `refresh()` and stops fetching from its field initializer, which is what makes gating it possible at all.

**Tech Stack:** Kotlin 2.3.20, IntelliJ Platform 262 (IU-262.8665.258), JUnit 3-style `BasePlatformTestCase`, Gradle.

## Global Constraints

- **Rakudo:** every gradle invocation runs as `export PATH="$RAKU_PREFIX/bin:$PATH"` then `./gradlew … --rerun`, in ONE command. `$RAKU_PREFIX` is `/home/longwalker/code/raku/x.core/raku-prefix`. Never rakubrew, never pin a release. `--rerun` is mandatory: `PATH` is not a declared task input, so without it gradle reports success having run zero tests.
- **Green baseline is 1322 tests, 0 failures** (1321 tracked + 1 from the untracked `DocDumpProbe.kt`). Any task ending on a different number must say why.
- **Verify from `build/test-results/test/TEST-*.xml`**, not console output, and check the XML is newer than your edits. The rich console does not reliably print a summary line here.
- **Never `git add -A`.** Name files explicitly. `src/test/kotlin/org/raku/comma/docs/DocDumpProbe.kt` is an untracked local probe and must stay untracked.
- **Platform floor:** `sinceBuild = "261"`. Every API used below exists in 262.
- **Text processing:** prefer `raku -e`. Python is permitted (`CLAUDE.md`).

---

## File Structure

| File | Responsibility |
|---|---|
| `src/main/java/org/raku/comma/project/RakuProjectKind.kt` | **Create.** The two predicates. No UI, no side effects. |
| `src/main/java/org/raku/comma/services/application/RakuEcosystem.kt` | **Modify.** Add `refresh()`; remove the eager field initializer. |
| `src/main/java/org/raku/comma/utils/CommaProjectUtil.kt` | **Modify.** Drop the substring clause from `pathContainsRakuCode`; gate the ecosystem block in `refreshProjectState`. |
| `src/main/java/org/raku/comma/project/activity/RakuServiceStarter.kt` | **Modify.** Gate on `hasRakuFiles`. |
| `src/main/java/org/raku/comma/ui/editorMenu/RakuStatusBarWidgetFactory.kt` | **Modify.** Gate on `hasRakuFiles`. |
| `src/main/java/org/raku/comma/ui/editorMenu/RakuWidgetSmartModeRefresher.kt` | **Create.** Re-asks the widget once indexing finishes. |
| `src/main/resources/META-INF/plugin.xml` | **Modify.** Register that activity. |
| `src/main/java/org/raku/comma/ui/editorMenu/RakuStatusBarListPopupStep.kt` | **Modify.** Add the `Refresh Ecosystem` item. |
| `src/main/java/org/raku/comma/sdk/RakuSdkUtil.kt` | **Modify.** Early return in `reactToSdkIssue`. |
| `src/main/java/org/raku/comma/utils/RakuLegacyExtensionsDetector.kt` | **Modify.** Gate on `hasRakuFiles`. |
| `src/main/java/org/raku/comma/actions/UpdateExtensionsAction.kt:173-192` | **Modify.** Replace the `java.io` walk with `FilenameIndex`. |
| `src/main/java/org/raku/comma/services/project/RakuProjectDetailsService.kt` | **Modify.** Remove `doesProjectContainRakuCode` and `hasScannedForRakuFiles`. |
| `src/test/kotlin/org/raku/comma/project/RakuProjectKindTest.kt` | **Create.** Both predicates against real fixtures. |
| `src/test/kotlin/org/raku/comma/actions/LegacyExtensionScanTest.kt` | **Create.** Exclusion-awareness of the legacy scan. |

`RakuProjectKind` lives in `project/` beside `RakuProjectBuilder`, not in `utils/`, because it answers a question about a project rather than being a grab-bag helper — and because `CommaProjectUtil` is already 160 lines of unrelated statics.

---

## Task 1: RakuProjectKind

**Files:**
- Create: `src/main/java/org/raku/comma/project/RakuProjectKind.kt`
- Test: `src/test/kotlin/org/raku/comma/project/RakuProjectKindTest.kt`

**Interfaces:**
- Consumes: nothing.
- Produces:
  - `RakuProjectKind.hasRakuFiles(project: Project): Boolean`
  - `RakuProjectKind.isRakuDistribution(project: Project): Boolean`

- [ ] **Step 1: Write the failing tests**

Create `src/test/kotlin/org/raku/comma/project/RakuProjectKindTest.kt`:

```kotlin
package org.raku.comma.project

import org.raku.comma.CommaFixtureTestCase

class RakuProjectKindTest : CommaFixtureTestCase() {

    fun testAModuleFileMakesItRaku() {
        myFixture.addFileToProject("lib/Thing.rakumod", "unit module Thing;")
        assertTrue(RakuProjectKind.hasRakuFiles(project))
    }

    fun testAScriptFileMakesItRaku() {
        myFixture.addFileToProject("bin/go.raku", "say 42;")
        assertTrue(RakuProjectKind.hasRakuFiles(project))
    }

    // The Perl 5 collision. Raku Test claims bare `.t`, so counting that file
    // type would make any Perl project a Raku project.
    fun testTestFilesAloneDoNotMakeItRaku() {
        myFixture.addFileToProject("t/01-basic.t", "use Test;")
        assertFalse("a .t file alone must not wake the plugin",
                    RakuProjectKind.hasRakuFiles(project))
    }

    // isRakuDistribution is a raw filesystem check against
    // project.basePath, so addFileToProject cannot drive it -- that writes into
    // the source root, not the base dir. Worse, the shared light fixture may
    // scaffold a META6.json of its own during setup, which would make a naive
    // positive test pass for the wrong reason and the negative test impossible.
    // So each of these controls the file itself and restores what it found.
    private fun meta6File() = java.io.File(project.basePath!!, "META6.json")

    fun testMeta6MakesItADistribution() {
        val meta = meta6File()
        val existing = if (meta.exists()) meta.readText() else null
        if (existing == null) meta.writeText("""{"name":"Thing"}""")
        try {
            assertTrue(RakuProjectKind.isRakuDistribution(project))
        } finally {
            if (existing == null) meta.delete()
        }
    }

    // The tiering: Raku files are not enough for the ecosystem fetch.
    fun testRakuFilesWithoutMeta6AreNotADistribution() {
        myFixture.addFileToProject("lib/Thing.rakumod", "unit module Thing;")
        assertTrue(RakuProjectKind.hasRakuFiles(project))

        val meta = meta6File()
        val existing = if (meta.exists()) meta.readText() else null
        if (existing != null) meta.delete()
        try {
            assertFalse("Raku files without a META6.json are not a distribution",
                        RakuProjectKind.isRakuDistribution(project))
        } finally {
            if (existing != null) meta.writeText(existing)
        }
    }
}
```

`myFixture.addFileToProject` puts the file in the project's source root, which
is in `GlobalSearchScope.projectScope`, so the index sees it.

- [ ] **Step 2: Run the tests to verify they fail**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH" && ./gradlew test --rerun --tests "org.raku.comma.project.RakuProjectKindTest"
```

Expected: FAIL to compile — `Unresolved reference 'RakuProjectKind'`.

- [ ] **Step 3: Write the implementation**

Create `src/main/java/org/raku/comma/project/RakuProjectKind.kt`:

```kotlin
package org.raku.comma.project

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import org.raku.comma.filetypes.RakuModuleFileType
import org.raku.comma.filetypes.RakuPodFileType
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.utils.CommaProjectUtil

/**
 * Whether a project has earned the plugin's attention.
 *
 * Two levels, because the features differ in cost. Fetching the ecosystem is
 * network plus zef plus a background task and only means anything where there
 * are dependencies to resolve; the status bar widget and the SDK prompt are
 * useful the moment someone edits Raku.
 */
object RakuProjectKind {

    /**
     * Raku Test is deliberately absent. It claims bare `.t`, which Perl 5 uses
     * too, and a lone test file should not wake the plugin. Nothing reachable
     * is lost: a real Raku project carries a script, module or pod file as
     * well. Shebang-typed files (`#!/usr/bin/env raku`) are counted, because
     * the registered detectors give them a Raku file type.
     */
    private val WAKING_FILE_TYPES = listOf(
        RakuScriptFileType.INSTANCE,
        RakuModuleFileType.INSTANCE,
        RakuPodFileType.INSTANCE,
    )

    /**
     * False while indexing. Erring toward silence is the right direction here:
     * the problem being solved is noise, so a widget that appears a moment
     * late is better than one that appears in a project with no Raku at all.
     */
    fun hasRakuFiles(project: Project): Boolean {
        if (project.isDisposed || DumbService.isDumb(project)) return false
        val scope = GlobalSearchScope.projectScope(project)
        return WAKING_FILE_TYPES.any { FileTypeIndex.containsFileOfType(it, scope) }
    }

    /** A META6.json at the project root -- no dependencies exist without one. */
    fun isRakuDistribution(project: Project): Boolean =
        CommaProjectUtil.projectHasMetaFile(project)
}
```

- [ ] **Step 4: Run the tests to verify they pass**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH" && ./gradlew test --rerun --tests "org.raku.comma.project.RakuProjectKindTest"
```

Expected: 5 tests, 0 failures. If `testTestFilesAloneDoNotMakeItRaku` fails, a
Test file type has crept into `WAKING_FILE_TYPES` — that assertion is the
point of the task, so fix the list, never the test.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/raku/comma/project/RakuProjectKind.kt \
        src/test/kotlin/org/raku/comma/project/RakuProjectKindTest.kt
git commit -m "Ask the index whether a project contains Raku

Two predicates, tiered by what each feature costs. Raku Test is excluded
because it claims bare .t, which Perl 5 uses -- counting it would make any
Perl project a Raku project."
```

---

## Task 2: A refreshable ecosystem that does not fetch on construction

**Files:**
- Modify: `src/main/java/org/raku/comma/services/application/RakuEcosystem.kt`
- Test: `src/test/kotlin/org/raku/comma/services/RakuEcosystemRefreshTest.kt` (create)

**Interfaces:**
- Consumes: nothing.
- Produces: `RakuEcosystem.refresh(): CompletableFuture<EcosystemDetailsState>`

Two changes in one task because they are the same defect: the service fetches
when constructed and can never fetch again.

- [ ] **Step 1: Write the failing test**

Create `src/test/kotlin/org/raku/comma/services/RakuEcosystemRefreshTest.kt`:

```kotlin
package org.raku.comma.services

import com.intellij.openapi.components.service
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.services.application.RakuEcosystem

class RakuEcosystemRefreshTest : CommaFixtureTestCase() {

    // initialize() is guarded by isNotInitializing && isNotInitialized, so once
    // the future completes it hands back cached state forever. A menu item
    // wired to it would silently do nothing, which is worse than no menu item.
    //
    // Asserted by identity, not by isInitialized: fillState returns a fresh
    // state object per fetch, so a re-fetch is observable as a different
    // instance. That holds no matter who initialised the service first, which
    // matters because it is an APP-level singleton the test JVM never resets --
    // CommaProjectUtil.refreshProjectState calls initialize() too, so any test
    // touching that path initialises it for the rest of the run.
    fun testRefreshReFetchesRatherThanReturningCachedState() {
        val eco = service<RakuEcosystem>()
        eco.initialize().get()
        val before = eco.ecosystem
        eco.refresh().get()
        assertNotSame("refresh must re-fetch, not hand back the cached state",
                      before, eco.ecosystem)
    }

    // The no-fetch-on-construction property cannot be observed at runtime in a
    // shared test JVM: by the time this class runs, something else may already
    // have initialised the singleton, and asserting isInitialized would then
    // fail for a reason unrelated to the defect. So it is pinned at the source,
    // the way ParserChangeVersionGuardTest pins its version constants -- and for
    // the same reason, that the runtime value is not a trustworthy witness.
    fun testServiceDoesNotFetchFromItsFieldInitializer() {
        val source = java.io.File(
            "src/main/java/org/raku/comma/services/application/RakuEcosystem.kt").readText()
        assertFalse(
            "the field initializer must not fetch: it made gating the call sites " +
            "impossible and blocked whichever thread resolved the service",
            source.contains("initialize().join()"))
    }
}
```

- [ ] **Step 2: Run it to verify it fails**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH" && ./gradlew test --rerun --tests "org.raku.comma.services.RakuEcosystemRefreshTest"
```

Expected: FAIL to compile — `Unresolved reference 'refresh'`. After adding
`refresh`, `testResolvingTheServiceDoesNotFetch` must still fail against the
eager field initializer; confirm you see that failure before removing it.

- [ ] **Step 3: Implement**

In `RakuEcosystem.kt`, replace the field initializer and add `refresh`. The
current line 30 reads `private var ecosystemState = initialize().join()` —
delete the initializer call and start from empty state:

```kotlin
    // Deliberately NOT `= initialize().join()`. That fetched the ecosystem the
    // moment anything resolved the service -- so gating the call sites could
    // not work, since RakuDependencyService touches it from three properties --
    // and it blocked the resolving thread while doing so.
    private var ecosystemState = EcosystemDetailsState()

    private var initializationFuture = CompletableFuture<EcosystemDetailsState>()
```

Note `initializationFuture` becomes a `var`, since `refresh` replaces it.
Then add:

```kotlin
    /**
     * Re-fetch, discarding what is cached.
     *
     * [initialize] cannot do this: it short-circuits once the future is
     * complete, so calling it again returns the old state.
     */
    fun refresh(): CompletableFuture<EcosystemDetailsState> {
        initializationFuture = CompletableFuture()
        isInitializing = false
        return initialize()
    }
```

- [ ] **Step 4: Run the tests to verify they pass**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH" && ./gradlew test --rerun --tests "org.raku.comma.services.RakuEcosystemRefreshTest"
```

Expected: 2 tests, 0 failures.

- [ ] **Step 5: Run the full suite**

Removing the eager initializer is the riskiest change in this plan: anything
reading `ecosystem` before initialization now sees empty maps instead of
blocking until a fetch finishes. Completion is the likely place for that to
surface, via `RakuDependencyService.ecoProvideToModule`.

```bash
export PATH="$RAKU_PREFIX/bin:$PATH" && ./gradlew test --rerun
```

Expected: 1324 tests (1322 + your 2), 0 failures. **If a completion test fails,
stop and report** — do not paper over it by restoring the eager fetch, and do
not adjust the failing expectation. It is the signal this task exists to find.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/raku/comma/services/application/RakuEcosystem.kt \
        src/test/kotlin/org/raku/comma/services/RakuEcosystemRefreshTest.kt
git commit -m "Let the ecosystem be refreshed, and stop fetching on construction

initialize() short-circuits once its future completes, so nothing could ever
re-fetch. And the field initializer fetched -- blocking -- the moment anything
resolved the service, which made gating the call sites impossible.

Isolated so it can be reverted without unpicking the gates."
```

---

## Task 3: Gate the ecosystem fetch and the startup activity

**Files:**
- Modify: `src/main/java/org/raku/comma/utils/CommaProjectUtil.kt`
- Modify: `src/main/java/org/raku/comma/project/activity/RakuServiceStarter.kt`

**Interfaces:**
- Consumes: `RakuProjectKind.hasRakuFiles`, `RakuProjectKind.isRakuDistribution` (Task 1).
- Produces: nothing new.

- [ ] **Step 1: Gate the startup activity**

`RakuServiceStarter.kt` currently reads:

```kotlin
class RakuServiceStarter : ProjectActivity {
    override suspend fun execute(project: Project) {
        if (! CommaProjectUtil.projectContainsRakuCode(project)) return
        CommaProjectUtil.refreshProjectState(project)
    }
}
```

Replace the condition:

```kotlin
        if (! RakuProjectKind.hasRakuFiles(project)) return
```

Add `import org.raku.comma.project.RakuProjectKind` and drop the
`CommaProjectUtil` import if nothing else in the file uses it.

- [ ] **Step 2: Gate the ecosystem block**

In `CommaProjectUtil.refreshProjectState` (around lines 124-140), the zef
prompt and the ecosystem load run unconditionally. Wrap both in the
distribution check:

```kotlin
    suspend fun refreshProjectState(project: Project) {
        val sdkService = project.service<RakuProjectSdkService>()

        // The ecosystem is a network fetch plus a possible zef install, and it
        // only means anything where there are declared dependencies to
        // resolve. A folder of loose scripts gets neither; Tools > the Raku
        // widget > Refresh Ecosystem is how such a project opts in.
        if (! RakuProjectKind.isRakuDistribution(project)) return

        val maybeInstallZef =   if (sdkService.zef == null)
                                    project.service<RakuModuleInstallPrompt>().installZefItself()
                                else CompletableFuture.completedFuture(0)
        // ...rest of the existing body unchanged
```

Read the whole method before editing and keep everything after the guard
exactly as it is.

- [ ] **Step 3: Run the full suite**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH" && ./gradlew test --rerun
```

Expected: 1324 tests, 0 failures.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/raku/comma/utils/CommaProjectUtil.kt \
        src/main/java/org/raku/comma/project/activity/RakuServiceStarter.kt
git commit -m "Fetch the ecosystem only for actual distributions

A folder of loose scripts has no dependencies to resolve, so it gets neither
the fetch nor the zef prompt. The startup activity as a whole now needs Raku
files rather than the old substring-matching walk."
```

---

## Task 4: Gate the widget and the SDK prompt

**Files:**
- Modify: `src/main/java/org/raku/comma/ui/editorMenu/RakuStatusBarWidgetFactory.kt`
- Modify: `src/main/java/org/raku/comma/sdk/RakuSdkUtil.kt`

**Interfaces:**
- Consumes: `RakuProjectKind.hasRakuFiles` (Task 1).
- Produces: nothing new.

- [ ] **Step 1: Gate the widget**

In `RakuStatusBarWidgetFactory.kt`, replace the body of `isAvailable`:

```kotlin
    override fun isAvailable(project: Project): Boolean {
        return RakuProjectKind.hasRakuFiles(project)
    }
```

Add `import org.raku.comma.project.RakuProjectKind`; remove the now-unused
`com.intellij.openapi.components.service` and
`org.raku.comma.services.project.RakuProjectDetailsService` imports.

- [ ] **Step 2: Gate the SDK prompt**

`RakuSdkUtil.reactToSdkIssue` is declared at line 94 as
`fun reactToSdkIssue(project: Project?, title: String, message: String? = null, ex: Exception? = null)`.
Insert the guard immediately after `val finalMessage = …`, before the
`if (ex != null)` block:

```kotlin
            // Only suppressed where we can see it is not a Raku project. A null
            // project cannot be attributed to one, and silencing that would
            // hide a genuine global SDK problem rather than reduce noise.
            if (project != null && ! RakuProjectKind.hasRakuFiles(project)) return
```

Add `import org.raku.comma.project.RakuProjectKind`.

- [ ] **Step 3: Re-evaluate the widget when indexing finishes**

`hasRakuFiles` answers false while indexing, and `isAvailable` is asked once at
startup -- so without this the butterfly would stay hidden in a genuine Raku
project until the next restart. Create
`src/main/java/org/raku/comma/ui/editorMenu/RakuWidgetSmartModeRefresher.kt`:

```kotlin
package org.raku.comma.ui.editorMenu

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.wm.impl.status.widget.StatusBarWidgetsManager

/**
 * RakuStatusBarWidgetFactory.isAvailable asks the file-type index, which has no
 * answer during indexing and so reports false. That is the right default -- the
 * problem being fixed is noise -- but it means the widget needs a second look
 * once the index is ready, or a real Raku project shows no butterfly until it
 * is reopened.
 */
class RakuWidgetSmartModeRefresher : ProjectActivity {
    override suspend fun execute(project: Project) {
        DumbService.getInstance(project).runWhenSmart {
            if (! project.isDisposed) {
                StatusBarWidgetsManager.getInstance(project)
                    .updateWidget(RakuStatusBarWidgetFactory::class.java)
            }
        }
    }
}
```

Register it in `src/main/resources/META-INF/plugin.xml` beside the other
`backgroundPostStartupActivity` entries (around line 325):

```xml
    <backgroundPostStartupActivity implementation="org.raku.comma.ui.editorMenu.RakuWidgetSmartModeRefresher"/>
```

This is a fourth startup activity, which cuts against the spirit of the change
-- but it registers a callback and returns, doing no work in projects that
never become Raku.

- [ ] **Step 4: Run the full suite**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH" && ./gradlew test --rerun
```

Expected: 1324 tests, 0 failures.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/raku/comma/ui/editorMenu/RakuStatusBarWidgetFactory.kt \
        src/main/java/org/raku/comma/ui/editorMenu/RakuWidgetSmartModeRefresher.kt \
        src/main/resources/META-INF/plugin.xml \
        src/main/java/org/raku/comma/sdk/RakuSdkUtil.kt
git commit -m "Hide the widget and the SDK warning without Raku files

Both asked a persisted, substring-matching predicate; both now ask the index.
A null project still gets the SDK warning -- it cannot be attributed to a
project, so suppressing it would hide real failures rather than reduce noise."
```

---

## Task 5: Refresh Ecosystem in the butterfly menu

**Files:**
- Modify: `src/main/java/org/raku/comma/ui/editorMenu/RakuStatusBarListPopupStep.kt`

**Interfaces:**
- Consumes: `RakuEcosystem.refresh()` (Task 2).
- Produces: nothing new.

This is what makes Task 3 humane: a scripts-only project can ask for what it
no longer gets automatically.

- [ ] **Step 1: Add the menu item**

`RakuStatusBarListPopupStep` is a `ListPopupStep<String>` keyed on the item
string. Three edits, all in that file.

`getValues`:

```kotlin
    override fun getValues(): MutableList<String> {
        return mutableListOf("Select SDK...", "Add SDK", "Launch REPL", "Refresh Ecosystem")
    }
```

`onChosen` — add one branch to the existing `when`:

```kotlin
            "Refresh Ecosystem" -> refreshEcosystem()
```

and the handler:

```kotlin
    /**
     * Gated projects never fetch on their own, so this is the only way in for
     * a project with no META6.json -- and the only way to re-fetch a stale
     * ecosystem for one that has it.
     */
    private fun refreshEcosystem() {
        ApplicationManager.getApplication().service<RakuEcosystem>().refresh()
    }
```

Imports to add: `com.intellij.openapi.application.ApplicationManager`,
`org.raku.comma.services.application.RakuEcosystem`. (`service` is already
imported.)

`getSeparatorAbove` currently puts a separator above `Launch REPL`. Leave it
as-is; `Refresh Ecosystem` sits below `Launch REPL` in the same group.

- [ ] **Step 2: Verify it compiles**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH" && ./gradlew compileKotlin
```

Expected: `BUILD SUCCESSFUL`. No test: the popup step is UI plumbing over
`refresh()`, which Task 2 already tests, and a test asserting that a list
contains a string it was just handed would assert nothing.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/raku/comma/ui/editorMenu/RakuStatusBarListPopupStep.kt
git commit -m "Offer Refresh Ecosystem from the Raku widget

Gating the automatic fetch leaves scripts-only projects with no way to get an
ecosystem at all. This is that way in, and doubles as a manual re-fetch when
the cached one is stale."
```

---

## Task 6: Legacy extension scan stops ignoring exclusions

**Files:**
- Modify: `src/main/java/org/raku/comma/actions/UpdateExtensionsAction.kt:173-192`
- Modify: `src/main/java/org/raku/comma/utils/RakuLegacyExtensionsDetector.kt`
- Test: `src/test/kotlin/org/raku/comma/actions/LegacyExtensionScanTest.kt` (create)

**Interfaces:**
- Consumes: `RakuProjectKind.hasRakuFiles` (Task 1).
- Produces: `UpdateExtensionsAction.collectFilesWithLegacyNames(project: Project)` — **note the changed signature**, `Project` instead of `Array<Module>`.

- [ ] **Step 1: Write the failing test**

Create `src/test/kotlin/org/raku/comma/actions/LegacyExtensionScanTest.kt`:

```kotlin
package org.raku.comma.actions

import org.raku.comma.CommaFixtureTestCase

class LegacyExtensionScanTest : CommaFixtureTestCase() {

    fun testFindsLegacyFilesInTheProject() {
        myFixture.addFileToProject("lib/Old.pm6", "unit module Old;")
        val found = UpdateExtensionsAction.collectFilesWithLegacyNames(project)
        assertTrue("a .pm6 file should be offered for renaming",
                   found.values.flatten().any { it.name == "Old.pm6" })
    }

    // The reason this task exists. FileUtil.findFilesByMask took a java.io.File
    // and so could not see IntelliJ's exclude folders -- on rakudo that meant
    // descending into t/spec, 2158 files the module model excludes. An
    // index-backed scan is scoped, so excluded content is simply absent.
    fun testIgnoresExcludedDirectories() {
        myFixture.addFileToProject("lib/Old.pm6", "unit module Old;")
        val excluded = myFixture.addFileToProject("skipme/Buried.pm6", "unit module Buried;")
        com.intellij.testFramework.PsiTestUtil.addExcludedRoot(
            myFixture.module, excluded.virtualFile.parent)
        try {
            val found = UpdateExtensionsAction.collectFilesWithLegacyNames(project)
            val names = found.values.flatten().map { it.name }
            assertTrue("the visible file should still be found", names.contains("Old.pm6"))
            assertFalse("a file in an excluded directory must not be scanned",
                        names.contains("Buried.pm6"))
        } finally {
            com.intellij.testFramework.PsiTestUtil.removeExcludedRoot(
                myFixture.module, excluded.virtualFile.parent)
        }
    }
}
```

- [ ] **Step 2: Run it to verify it fails**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH" && ./gradlew test --rerun --tests "org.raku.comma.actions.LegacyExtensionScanTest"
```

Expected: FAIL to compile — `collectFilesWithLegacyNames` takes
`Array<Module>`, not `Project`. That signature change is the task.

- [ ] **Step 3: Replace the walk**

`UpdateExtensionsAction.collectFilesWithLegacyNames` currently iterates
modules, then each module's `sourceRoots`, calling
`FileUtil.findFilesByMask(FULL_LEGACY_EXTENSION_PATTERN, root.toNioPath().toFile())`.
Replace the whole method:

```kotlin
        /**
         * Index-backed, so it honours excluded folders. The previous
         * implementation handed a java.io.File to FileUtil.findFilesByMask,
         * which cannot know what IntelliJ excludes -- on a rakudo checkout `t`
         * is a source root, so it walked all 2158 files of the excluded
         * t/spec.
         */
        @JvmStatic
        fun collectFilesWithLegacyNames(project: Project): MutableMap<String, MutableList<File>> {
            val filesToUpdate: MutableMap<String, MutableList<File>> = HashMap()
            val scope = GlobalSearchScope.projectScope(project)
            for (ext in LEGACY_EXTENSIONS) {
                for (vf in FilenameIndex.getAllFilesByExt(project, ext, scope)) {
                    val matcher: Matcher = FULL_LEGACY_EXTENSION_PATTERN.matcher(vf.name)
                    if (matcher.matches()) {
                        val key = matcher.group(1)
                        filesToUpdate.computeIfAbsent(key) { mutableListOf() }.add(File(vf.path))
                    }
                }
            }
            return filesToUpdate
        }
```

Add beside `FULL_LEGACY_EXTENSION_PATTERN` (line 160), derived from the same
alternation so the two cannot drift:

```kotlin
        private val LEGACY_EXTENSIONS = listOf("p6", "pl6", "pm6", "pm", "pod6", "pod", "t")
```

Imports to add: `com.intellij.openapi.project.Project`,
`com.intellij.psi.search.FilenameIndex`,
`com.intellij.psi.search.GlobalSearchScope`. Remove
`com.intellij.openapi.util.io.FileUtil` and the `ModuleRootManager` /
`Module` imports if nothing else in the file uses them.

- [ ] **Step 4: Update the two callers**

`RakuLegacyExtensionsDetector.execute` currently builds `modules` and passes
them. Replace its body:

```kotlin
class RakuLegacyExtensionsDetector : ProjectActivity {
    override suspend fun execute(project: Project) {
        // Advice about Raku source needs Raku source. The pattern matches .pm,
        // .pod and .t, which are current Perl 5 extensions -- without this a
        // Perl project is told its files are "obsolete Raku extensions".
        if (! RakuProjectKind.hasRakuFiles(project)) return

        val filesToUpdate = UpdateExtensionsAction.collectFilesWithLegacyNames(project)
        // ...rest of the existing body unchanged
```

Add `import org.raku.comma.project.RakuProjectKind`; drop the
`ModuleManager` import.

Then in `UpdateExtensionsAction.actionPerformed`, the call currently reads
`collectFilesWithLegacyNames(modules)` where `modules` came from
`ModuleManager.getInstance(project).modules`. Change it to
`collectFilesWithLegacyNames(project)` and delete the now-unused `modules`
local.

- [ ] **Step 5: Run the tests to verify they pass**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH" && ./gradlew test --rerun --tests "org.raku.comma.actions.LegacyExtensionScanTest"
```

Expected: 2 tests, 0 failures. If `testIgnoresExcludedDirectories` fails, the
scan is still not scope-aware — that assertion is the whole point, so fix the
scan, never the test.

- [ ] **Step 6: Run the full suite**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH" && ./gradlew test --rerun
```

Expected: 1326 tests (1324 + your 2), 0 failures.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/org/raku/comma/actions/UpdateExtensionsAction.kt \
        src/main/java/org/raku/comma/utils/RakuLegacyExtensionsDetector.kt \
        src/test/kotlin/org/raku/comma/actions/LegacyExtensionScanTest.kt
git commit -m "Scan for legacy extensions through the index, and only for Raku

FileUtil.findFilesByMask took a java.io.File, so it could not see IntelliJ's
exclude folders: on rakudo, t is a source root and the scan walked all 2158
files of the excluded t/spec. FilenameIndex over the project scope honours
exclusions for free.

Gated too. The pattern matches .pm, .pod and .t -- current Perl 5 extensions --
so an ungated run tells a Perl project its files are obsolete Raku."
```

---

## Task 7: Delete the persisted verdict and the substring clause

Last, so the old predicate stays available while the gates move over.

**Interfaces:**
- Consumes: nothing -- every gate has already moved to `RakuProjectKind` in
  Tasks 3, 4 and 6.
- Produces: nothing. This task only deletes.

**Files:**
- Modify: `src/main/java/org/raku/comma/services/project/RakuProjectDetailsService.kt`
- Modify: `src/main/java/org/raku/comma/utils/CommaProjectUtil.kt`

- [ ] **Step 1: Remove the persisted verdict**

In `RakuProjectDetailsService.kt`, delete:

- the `doesProjectContainRakuCode` property (the `val` and its doc comment),
- the `projectFilesScannedStatus` / `hasScannedForRakuFiles` pair,
- `doesProjectContainRakuCode` from `class RakudoProjectState`,
- the `state.doesProjectContainRakuCode = …` assignment inside `refreshState`,
  and the `if (!hasScannedForRakuFiles)` guard around it.

Keep `isProjectRakudoCore` and its `determineIfProjectIsRakudoCore()` — that
is a different question (is this the rakudo compiler checkout) with other
callers, and it does not walk the tree.

Verify nothing still refers to the removed names:

```bash
grep -rn "doesProjectContainRakuCode\|hasScannedForRakuFiles\|projectContainsNoRakuCode" src/main src/test
```

Expected: no output. `projectContainsNoRakuCode` in `CommaProjectUtil`
delegates to the removed property, so delete that function too.

- [ ] **Step 2: Remove the substring clause**

In `CommaProjectUtil.pathContainsRakuCode`, the filter's third clause reads:

```kotlin
                    || (file.isFile && (file.extension.isNullOrEmpty() && file.readText()
                .lines()
                .first()
                .contains("raku")))
```

Delete it, leaving:

```kotlin
        val filter = VirtualFileFilter { file ->
            // No content sniffing: an extensionless file whose first line merely
            // contained "raku" used to make a whole project Raku, and reading
            // every such file made the walk expensive as well as wrong. The
            // registered shebang file-type detectors handle real
            // `#!/usr/bin/env raku` scripts properly, for the callers that have
            // a project to index.
            (file.isDirectory && !file.path.endsWith(".idea"))
                    || rakuExtensions.contains(file.extension)
        }
```

**`pathContainsRakuCode` itself stays.** `canOpenFileAsProject` calls it to
decide whether a directory opens as a Raku project, which happens before a
project exists — so `FileTypeIndex`, needing a project scope, is unavailable
there. Confirm that is now its only caller:

```bash
grep -rn "pathContainsRakuCode\|projectContainsRakuCode" src/main src/test
```

Expected: the declarations, plus the call in `canOpenFileAsProject`. If
`projectContainsRakuCode` has no callers left, delete it as well.

- [ ] **Step 3: Run the full suite**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH" && ./gradlew test --rerun
```

Expected: 1326 tests, 0 failures.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/raku/comma/services/project/RakuProjectDetailsService.kt \
        src/main/java/org/raku/comma/utils/CommaProjectUtil.kt
git commit -m "Stop persisting whether a project contains Raku

The verdict was cached in project state, so a wrong answer survived restarts --
which is why the widget kept appearing after the predicate was suspect. Nothing
caches it now; the index is asked each time.

The substring clause goes with it: an extensionless file whose first line merely
contained 'raku' made a whole project Raku, and reading every such file made the
walk expensive as well as wrong.

pathContainsRakuCode stays for canOpenFileAsProject, which asks the same
question before a project exists and so cannot use an index."
```

---

## Done when

- `./gradlew test --rerun` reports **1326 tests, 0 failures** with `$RAKU_PREFIX` on `PATH` — or 1325 in a fresh clone, where the untracked `DocDumpProbe.kt` is absent.
- `grep -rn "doesProjectContainRakuCode\|hasScannedForRakuFiles" src/` returns nothing.
- `grep -rn 'contains("raku")' src/main/java/org/raku/comma/utils/CommaProjectUtil.kt` returns nothing.
- `grep -rn "findFilesByMask" src/main` returns nothing.
- Manual check, since no test covers the IDE end to end: open a project with no Raku files. No butterfly, no ecosystem progress bar, no "Obsolete Raku extensions" notification. Then add a `.rakumod`, wait for indexing, and confirm the butterfly appears.
