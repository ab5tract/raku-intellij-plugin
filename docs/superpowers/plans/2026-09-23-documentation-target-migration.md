# DocumentationTarget Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Serve Raku documentation through `DocumentationTarget` instead of the legacy `DocumentationProvider`, with no user-visible regression, plus a structured `TargetPresentation` and an async call-site path.

**Architecture:** A single `PsiDocumentationTargetProvider` returns a `RakuDocumentationTarget` for Raku elements. The target delegates all string building to `RakuDocRendering`, a plain object extracted verbatim from today's provider, so the hover string is unchanged by construction. The legacy registration is deleted, not left alongside — the platform bridges old providers into the same pipeline and both would double every popup.

**Tech Stack:** Kotlin 2.3.20, IntelliJ Platform 262 (IU-262.8665.258), JUnit 3-style `BasePlatformTestCase`, Gradle.

## Global Constraints

- **Rakudo:** run every gradle invocation with `export PATH="$RAKU_PREFIX/bin:$PATH"` and `--rerun`. Never pin a rakubrew release. See `CLAUDE.md`.
- **Green baseline is 1297 tests, 0 failures.** Any task that ends with a different number must explain why.
- **The 25 `DocumentationTest` expectations are frozen.** No task may edit a string inside a `testQuickDoc(...)`, `testGeneratedDoc(...)`, `testURL(...)` or `assertEquals(...)` call in that file. Only the three helper bodies change. If an expectation needs to change, stop — that is a regression, not a task.
- **Platform version floor:** `sinceBuild = "261"` (`build.gradle.kts`). Every API used below exists in 262; none is guarded.
- **Text processing:** prefer `raku -e`. Python is permitted for now (`CLAUDE.md`).

---

## File Structure

| File | Responsibility |
|---|---|
| `src/main/java/org/raku/comma/docs/RakuDocRendering.kt` | **Create.** Pure string building: hint line, presentable text, container, location, doc HTML, external URL. No platform documentation types. |
| `src/main/java/org/raku/comma/docs/RakuDocumentationTarget.kt` | **Create.** The `DocumentationTarget`. Pointer strategy, presentation assembly, sync/async branch. |
| `src/main/java/org/raku/comma/docs/RakuPsiDocumentationTargetProvider.kt` | **Create.** EP entry point. Filters non-Raku elements. |
| `src/main/java/org/raku/comma/docs/RakuDocumentationProvider.kt` | **Delete** in Task 6. |
| `src/main/resources/META-INF/plugin.xml:301-302` | **Modify** in Task 6. Swap the EP. |
| `src/test/kotlin/org/raku/comma/docs/DocumentationTest.kt:14-34` | **Modify** in Task 6. Helper bodies only. |
| `src/test/kotlin/org/raku/comma/docs/DocumentationTargetTest.kt` | **Create.** Presentation slots and pointer survival. |
| `src/test/kotlin/org/raku/comma/docs/DocDumpProbe.kt` | **Modify** in Task 6. Untracked probe; keep it compiling. |

`RakuDocRendering` is separate from the target because the signature line has two consumers (`computeDocumentationHint()` and `presentableText`) and because it can be unit-tested without constructing a platform target.

---

## Task 1: Extract rendering, unchanged

Move today's three `when` blocks into a plain object. **Behaviour must not change** — this task is pure extraction, and the existing 25 tests still run against the old provider, which now delegates.

**Files:**
- Create: `src/main/java/org/raku/comma/docs/RakuDocRendering.kt`
- Modify: `src/main/java/org/raku/comma/docs/RakuDocumentationProvider.kt`
- Test: existing `src/test/kotlin/org/raku/comma/docs/DocumentationTest.kt` (unchanged)

**Interfaces:**
- Consumes: nothing.
- Produces:
  - `RakuDocRendering.hintLine(element: PsiElement): String?`
  - `RakuDocRendering.externalUrl(element: PsiElement): String?`
  - `RakuDocRendering.docHtml(element: PsiElement): String?`

- [ ] **Step 1: Create the rendering object**

Create `src/main/java/org/raku/comma/docs/RakuDocRendering.kt`. Copy the bodies of `getQuickNavigateInfo`, `getUrlFor` and `generateDoc` **verbatim** from `RakuDocumentationProvider.kt`, renaming only the functions. Carry over every import the bodies need (the full list is at `RakuDocumentationProvider.kt:1-26`).

```kotlin
package org.raku.comma.docs

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil
// ...plus the rest of RakuDocumentationProvider.kt:1-26 as needed

/**
 * Every user-visible string the documentation feature produces.
 *
 * Separate from the target because the signature line has two consumers --
 * the hover hint and TargetPresentation.presentableText -- and because it can
 * be exercised without constructing a platform DocumentationTarget.
 */
object RakuDocRendering {

    /** Today's getQuickNavigateInfo, verbatim. The hover string. */
    fun hintLine(element: PsiElement): String? = when (element) {
        // Copy RakuDocumentationProvider.kt lines 32-82 verbatim: the whole
        // `when` body, from `is RakuConstant ->` through `else -> null`.
        // Do not retype it. A transcription slip here is a silent regression,
        // and nothing catches it until the EP swap in Task 6.
        else -> null
    }

    /** Today's getUrlFor, verbatim. */
    fun externalUrl(element: PsiElement): String? {
        // Copy RakuDocumentationProvider.kt lines 86-100 verbatim, changing
        // only the two `return listOf("https://docs.raku.org/...")` statements
        // to return the bare String.
        return null
    }

    /**
     * Today's generateDoc, verbatim. `@Synchronized` stays on the provider for
     * now; Task 5 removes it separately.
     */
    fun docHtml(element: PsiElement): String? {
        // Copy RakuDocumentationProvider.kt lines 105-128 verbatim: the
        // RakuMethodCall/RakuSubCall multiResolve branch, then the
        // RakuDocumented branch including its `docsString == "<br>"` guard.
        return null
    }
}
```

Note `externalUrl` returns `String?` where `getUrlFor` returned `List<String>?`. The old body only ever built single-element lists; the caller re-wraps.

- [ ] **Step 2: Delegate from the old provider**

Replace the three method bodies in `RakuDocumentationProvider.kt` with delegation. Keep `@Synchronized` for now — removing it is Task 5.

Note there are **two** `@Synchronized` annotations in the original, on
`getQuickNavigateInfo` (line 30) and on `generateDoc` (line 103). Both stay put
in this task — dropping either here would make this extraction a behaviour
change. Task 5 removes both, deliberately and separably.

```kotlin
class RakuDocumentationProvider : DocumentationProvider {
    @Synchronized
    override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement?): String? =
        RakuDocRendering.hintLine(element)

    override fun getUrlFor(element: PsiElement, originalElement: PsiElement?): List<String>? =
        RakuDocRendering.externalUrl(element)?.let { listOf(it) }

    @Synchronized
    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? =
        RakuDocRendering.docHtml(element)

    override fun getDocumentationElementForLookupItem(
        psiManager: PsiManager, `object`: Any?, element: PsiElement?): PsiElement? = null

    override fun getDocumentationElementForLink(
        psiManager: PsiManager, link: String, context: PsiElement?): PsiElement? = null
}
```

- [ ] **Step 3: Run the full documentation suite**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH"
./gradlew test --rerun --tests "org.raku.comma.docs.DocumentationTest"
```

Expected: `25 tests completed, 0 failed`. A pure extraction that changes a string will fail here, which is the point of doing it before anything else.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/raku/comma/docs/
git commit -m "Extract documentation rendering from the provider

Pure move, no behaviour change: the provider now delegates. The signature
line is about to gain a second consumer in TargetPresentation, and a plain
object can be exercised without constructing a platform target."
```

---

## Task 2: Presentation helpers

Add the three presentation slots to `RakuDocRendering`. These are new strings with no existing coverage, so they get tests first.

**Files:**
- Modify: `src/main/java/org/raku/comma/docs/RakuDocRendering.kt`
- Test: `src/test/kotlin/org/raku/comma/docs/DocumentationTargetTest.kt` (create)

**Interfaces:**
- Consumes: `RakuDocRendering.hintLine` (Task 1).
- Produces:
  - `RakuDocRendering.presentableText(element: PsiElement): String?`
  - `RakuDocRendering.containerText(element: PsiElement): String?`
  - `RakuDocRendering.locationText(element: PsiElement): String?`

Rules from the spec: the routine keyword is dropped from `presentableText` **for routines only**; every other element kind keeps today's string whole. `containerText` is the owning package, null at top level. `locationText` is `CORE.setting` for external elements, otherwise the containing file name.

- [ ] **Step 1: Write the failing test**

Create `src/test/kotlin/org/raku/comma/docs/DocumentationTargetTest.kt`:

```kotlin
package org.raku.comma.docs

import org.raku.comma.CommaFixtureTestCase

class DocumentationTargetTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String = "testData/docs"

    private fun elementAt(fixture: String): com.intellij.psi.PsiElement {
        myFixture.configureByFile("$fixture.p6")
        return myFixture.elementAtCaret
    }

    // The icon and container carry the kind, so the keyword is redundant in a
    // narrow slot -- but only for routines. Stripping the first word of
    // "class Magician is Cool does Int" would leave a fragment.
    fun testRoutinePresentableTextDropsTheKeyword() {
        val element = elementAt("methodExternalFromCORE")
        assertEquals("method Capture(--&gt; Mu)", RakuDocRendering.hintLine(element))
        assertEquals("Capture(--&gt; Mu)", RakuDocRendering.presentableText(element))
    }

    fun testPackagePresentableTextIsUnchanged() {
        val element = elementAt("quickDocsClass")
        assertEquals("class Magician is Cool does Int", RakuDocRendering.presentableText(element))
    }

    fun testCoreSymbolIsLocatedInTheSetting() {
        assertEquals("CORE.setting", RakuDocRendering.locationText(elementAt("methodExternalFromCORE")))
    }

    fun testProjectSymbolIsLocatedInItsFile() {
        assertEquals("quickDocsClass.p6", RakuDocRendering.locationText(elementAt("quickDocsClass")))
    }

    // A top-level declaration has no owner; the slot stays empty rather than
    // inventing one.
    fun testTopLevelDeclarationHasNoContainer() {
        assertNull(RakuDocRendering.containerText(elementAt("quickDocsClass")))
    }
}
```

- [ ] **Step 2: Run it to make sure it fails**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH"
./gradlew test --rerun --tests "org.raku.comma.docs.DocumentationTargetTest"
```

Expected: FAIL to compile — `Unresolved reference 'presentableText'`. That is the correct RED for a function that does not exist yet.

- [ ] **Step 3: Implement the three helpers**

Append to `RakuDocRendering`:

```kotlin
    /**
     * The narrow slot in a popup or list row. For routines the kind keyword is
     * dropped -- the icon and container already say "method" -- but only for
     * routines: stripping the first word of "class Magician is Cool does Int"
     * would leave a fragment rather than a shorter label.
     */
    fun presentableText(element: PsiElement): String? {
        val line = hintLine(element) ?: return null
        if (element !is RakuRoutineDecl) return line
        return line.substringAfter(' ', line)
    }

    /** The owning package, or null for a top-level declaration. */
    fun containerText(element: PsiElement): String? {
        val owner = PsiTreeUtil.getParentOfType(element, RakuPackageDecl::class.java)
        return owner?.packageName
    }

    /**
     * Where the symbol came from: the setting for CORE, else the file.
     *
     * Deliberately the literal "CORE.setting" rather than
     * ProjectSdkSymbolCache.SETTING_FILE_NAME, whose value is
     * "SETTINGS.rakumod" -- the name of the LightVirtualFile the plugin
     * synthesises to back CORE symbols. That is an implementation detail; the
     * name a Raku developer knows, and the one docs.raku.org uses, is
     * CORE.setting.
     *
     * The cost, accepted: Ctrl-clicking a CORE symbol opens a tab titled
     * SETTINGS.rakumod, so the popup and the tab disagree. Renaming the
     * constant would fix that but it feeds symbol-cache lookups and file
     * construction, well outside this migration.
     */
    fun locationText(element: PsiElement): String? = when (element) {
        is RakuExternalPsiElement -> CORE_SETTING_LABEL
        else -> element.containingFile?.name
    }

    private const val CORE_SETTING_LABEL = "CORE.setting"
```

If `containerText` returns the wrong owner for an external element — CORE methods hang off `ExternalRakuPackageDecl`, which `getParentOfType(…, RakuPackageDecl::class.java)` should still match, since `ExternalRakuPackageDecl` implements `RakuPackageDecl` (see `RakuDocumentationProvider.kt:88-90`, which relies on exactly that) — fix it here rather than in the target.

- [ ] **Step 4: Run the tests to verify they pass**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH"
./gradlew test --rerun --tests "org.raku.comma.docs.DocumentationTargetTest"
```

Expected: `5 tests completed, 0 failed`.

`testCoreSymbolIsLocatedInTheSetting` asserts the literal `"CORE.setting"`. Do
**not** replace it with `ProjectSdkSymbolCache.SETTING_FILE_NAME` — that constant
is `"SETTINGS.rakumod"`, the synthesised virtual file's name, and showing it in
the popup was considered and rejected. The display label and the constant are
intentionally decoupled; see the KDoc on `locationText`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/raku/comma/docs/RakuDocRendering.kt \
        src/test/kotlin/org/raku/comma/docs/DocumentationTargetTest.kt
git commit -m "Render the presentation slots a structured target needs

Icon, name, owner and origin instead of one flat string. The routine keyword
is dropped from the narrow slot only for routines; a package keeps its
declaration whole, since a fragment is not a shorter label."
```

---

## Task 3: The target

**Files:**
- Create: `src/main/java/org/raku/comma/docs/RakuDocumentationTarget.kt`
- Test: `src/test/kotlin/org/raku/comma/docs/DocumentationTargetTest.kt` (modify)

**Interfaces:**
- Consumes: all six `RakuDocRendering` functions (Tasks 1-2).
- Produces: `RakuDocumentationTarget(element: PsiElement, originalElement: PsiElement?)` implementing `DocumentationTarget`.

- [ ] **Step 1: Write the failing test**

Append to `DocumentationTargetTest`:

```kotlin
    // computeDocumentationBlocking dereferences the pointer before computing,
    // so this covers createPointer() as well as the content. External elements
    // are synthesised from the symbol cache and have no file for a smart
    // pointer to track, which is why they use a hard pointer.
    fun testTargetSurvivesItsOwnPointer() {
        val target = RakuDocumentationTarget(elementAt("methodExternalFromCORE"), null)
        val data = com.intellij.platform.backend.documentation.impl.computeDocumentationBlocking(
            target.createPointer())
        assertNotNull("the pointer must still dereference to a live target", data)
        assertTrue(data!!.html!!.contains("Throws X::Cannot::Capture"))
    }

    fun testTargetHintMatchesTheRenderedLine() {
        val element = elementAt("methodExternalFromCORE")
        assertEquals(RakuDocRendering.hintLine(element),
                     RakuDocumentationTarget(element, null).computeDocumentationHint())
    }

    fun testTargetPresentationCarriesAllFourSlots() {
        val presentation = RakuDocumentationTarget(elementAt("methodExternalFromCORE"), null)
            .computePresentation()
        assertEquals("Capture(--&gt; Mu)", presentation.presentableText)
        assertEquals("CORE.setting", presentation.locationText)
        assertNotNull("a Raku symbol should carry the Camelia icon", presentation.icon)
    }

    // containerText's owner-resolution branch is otherwise untested: Task 2
    // only pinned the null case. This is the slot where a wrong answer is
    // plausible, because a CORE method's owner is reached by hopping to an
    // ExternalRakuPackageDecl rather than a real PSI package.
    fun testCoreMethodIsOwnedByItsType() {
        val owner = RakuDocRendering.containerText(elementAt("methodExternalFromCORE"))
        assertNotNull("a CORE method must report the type that owns it", owner)
        assertEquals("Mu", owner)
    }
```

- [ ] **Step 2: Run it to make sure it fails**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH"
./gradlew test --rerun --tests "org.raku.comma.docs.DocumentationTargetTest"
```

Expected: FAIL to compile — `Unresolved reference 'RakuDocumentationTarget'`.

- [ ] **Step 3: Implement the target**

Create `src/main/java/org/raku/comma/docs/RakuDocumentationTarget.kt`:

```kotlin
package org.raku.comma.docs

import com.intellij.model.Pointer
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import org.raku.comma.RakuIcons
import org.raku.comma.psi.external.RakuExternalPsiElement

class RakuDocumentationTarget(
    private val element: PsiElement,
    private val originalElement: PsiElement?,
) : DocumentationTarget {

    /**
     * A target must survive being carried across read actions.
     *
     * A RakuExternalPsiElement is synthesised in memory from the SDK symbol
     * cache: it has no file and no offsets for a smart pointer to track, and
     * it is immutable, so there is no later state for a pointer to go stale
     * against. Swapping the SDK rebuilds the whole cache and discards this
     * target with it. A hard pointer is therefore correct, not a shortcut.
     */
    override fun createPointer(): Pointer<out DocumentationTarget> {
        if (element is RakuExternalPsiElement) return Pointer.hardPointer(this)
        val elementPointer = SmartPointerManager.createPointer(element)
        val originalPointer = originalElement?.let { SmartPointerManager.createPointer(it) }
        return Pointer {
            val live = elementPointer.element ?: return@Pointer null
            RakuDocumentationTarget(live, originalPointer?.element)
        }
    }

    override fun computeDocumentationHint(): String? = RakuDocRendering.hintLine(element)

    override fun computePresentation(): TargetPresentation {
        val builder = TargetPresentation
            .builder(RakuDocRendering.presentableText(element) ?: element.text ?: "")
            .icon(RakuIcons.CAMELIA)
        RakuDocRendering.containerText(element)?.let { builder.containerText(it) }
        RakuDocRendering.locationText(element)?.let { builder.locationText(it) }
        return builder.presentation()
    }

    override fun computeDocumentation(): DocumentationResult? {
        val html = RakuDocRendering.docHtml(element) ?: return null
        val result = DocumentationResult.documentation(html)
        RakuDocRendering.externalUrl(element)?.let { result.externalUrl(it) }
        return result
    }
}
```

`TargetPresentationBuilder` methods return the builder, so the `?.let` calls must use the return value if the builder is immutable. If Step 4 shows the container or location missing, reassign instead:

```kotlin
        var builder = TargetPresentation.builder(...).icon(RakuIcons.CAMELIA)
        RakuDocRendering.containerText(element)?.let { builder = builder.containerText(it) }
        RakuDocRendering.locationText(element)?.let { builder = builder.locationText(it) }
```

- [ ] **Step 4: Run the tests to verify they pass**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH"
./gradlew test --rerun --tests "org.raku.comma.docs.DocumentationTargetTest"
```

Expected: `8 tests completed, 0 failed`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/raku/comma/docs/RakuDocumentationTarget.kt \
        src/test/kotlin/org/raku/comma/docs/DocumentationTargetTest.kt
git commit -m "Add the Raku documentation target

Still unregistered -- the EP swap is a separate step, so this lands without
changing what the IDE does."
```

---

## Task 4: The provider, and verify the hover source

The spec names one unverified assumption: that Ctrl-hover reads `computeDocumentationHint()`. **Verify it here, before the swap**, because the parity claim rests on it.

**Files:**
- Create: `src/main/java/org/raku/comma/docs/RakuPsiDocumentationTargetProvider.kt`

**Interfaces:**
- Consumes: `RakuDocumentationTarget` (Task 3).
- Produces: `RakuPsiDocumentationTargetProvider`, an EP implementation.

- [ ] **Step 1: Verify the hover source**

The EP is **not language-filtered**, so read the platform's own hover code to confirm which method it calls:

```bash
IDEA=$(raku -e 'say "src/main/resources/docs/core.json".IO.absolute' >/dev/null; \
  echo ~/.gradle/caches/9.4.1/transforms/3367e41879fe7b0ad5af77985e3b2f1b/transformed/idea-2026.2)
find "$IDEA" -name "*.jar" -print0 | xargs -0 -P8 -I{} sh -c \
  'unzip -l "$1" </dev/null 2>/dev/null | grep -qi "CtrlMouse\|DocumentationTargetHover" && echo "$1"' _ {}
```

Then `javap -c` the matching class and look for `computeDocumentationHint`.

**If hover calls `computeDocumentationHint`:** proceed.
**If hover calls `computePresentation`:** STOP and report. The keyword would vanish from hover, which was not agreed; the spec's presentation decision needs revisiting first.

- [ ] **Step 2: Write the provider**

Create `src/main/java/org/raku/comma/docs/RakuPsiDocumentationTargetProvider.kt`:

```kotlin
package org.raku.comma.docs

import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.psi.PsiElement
import org.raku.comma.RakuLanguage

/**
 * Unlike lang.documentationProvider, psiTargetProvider is not language-filtered
 * -- the platform calls it for elements of every language -- so the language
 * check is ours to make.
 */
class RakuPsiDocumentationTargetProvider : PsiDocumentationTargetProvider {
    override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
        if (element.language != RakuLanguage.INSTANCE) return null
        return RakuDocumentationTarget(element, originalElement)
    }
}
```

Check `RakuLanguage`'s singleton name before writing this — `grep -n "object\|INSTANCE" src/main/java/org/raku/comma/RakuLanguage.*`. External elements report their language via `RakuExternalPsiElement.getLanguage()` (`RakuExternalPsiElement.kt` imports `RakuLanguage`), so CORE symbols pass this check.

- [ ] **Step 3: Verify it compiles**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH"
./gradlew compileKotlin
```

Expected: `BUILD SUCCESSFUL`. Nothing is registered yet, so no test change.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/raku/comma/docs/RakuPsiDocumentationTargetProvider.kt
git commit -m "Add the documentation target provider

psiTargetProvider is not language-filtered, so the Raku check is ours."
```

---

## Task 5: Remove @Synchronized

Isolated deliberately. The spec records that `1bef5d59` added this annotation with no stated rationale, so this is the change most likely to surface something, and it must be revertible without unpicking the migration.

**Files:**
- Modify: `src/main/java/org/raku/comma/docs/RakuDocumentationProvider.kt`

- [ ] **Step 1: Remove both annotations**

Delete **both** `@Synchronized` annotations from `RakuDocumentationProvider.kt`
— one on `getQuickNavigateInfo`, one on `generateDoc`. Confirm with:

```bash
raku -e 'say "src/main/java/org/raku/comma/docs/RakuDocumentationProvider.kt".IO.lines.grep(*.contains("Synchronized")).elems, " left"'
```

Expected after the edit: `0 left`.

Both guard the same thing, and the spec's reasoning covers both: the data behind
them is either an immutable field populated when the symbol cache was built, or
PSI that the read action already guards. Removing one and keeping the other
would leave a lock whose only remaining justification is that nobody removed it.

- [ ] **Step 2: Run the full suite**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH"
./gradlew test --rerun
```

Expected: `1297 tests, 0 failures`. A flaky or hanging documentation test here is the signal that the lock was load-bearing — revert this task and report, rather than continuing.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/raku/comma/docs/RakuDocumentationProvider.kt
git commit -m "Drop @Synchronized from documentation generation

The data behind it is either an immutable field populated when the symbol
cache was built, or PSI the read action already guards. It arrived in
1bef5d59, a bulk release commit that records no reason for it.

Isolated so it can be reverted without unpicking the target migration."
```

---

## Task 6: Swap the extension point

The cutover. Everything before this was additive and invisible.

**Files:**
- Modify: `src/main/resources/META-INF/plugin.xml:301-302`
- Modify: `src/test/kotlin/org/raku/comma/docs/DocumentationTest.kt:14-34`
- Modify: `src/test/kotlin/org/raku/comma/docs/DocDumpProbe.kt`
- Delete: `src/main/java/org/raku/comma/docs/RakuDocumentationProvider.kt`

- [ ] **Step 1: Rewrite the three test helpers**

In `DocumentationTest.kt`, replace **only** the bodies of `testGeneratedDoc`, `testQuickDoc` and `testURL`. Do not touch any of the 25 test methods below them.

```kotlin
import com.intellij.platform.backend.documentation.impl.computeDocumentationBlocking

    private fun targetFor(fixture: String): RakuDocumentationTarget {
        myFixture.configureByFile("$fixture.p6")
        return RakuDocumentationTarget(myFixture.elementAtCaret, null)
    }

    private fun testGeneratedDoc(result: String) {
        val target = targetFor(getTestName(true))
        assertEquals(result, computeDocumentationBlocking(target.createPointer())?.html)
    }

    private fun testQuickDoc(result: String) {
        assertEquals(result, targetFor(getTestName(true)).computeDocumentationHint())
    }

    // externalUrl is asserted through the rendering object rather than read
    // back out of DocumentationData: LinkData is reachable only through an
    // internal accessor, and the wiring into .externalUrl() is one line covered
    // by DocumentationTargetTest.
    private fun testURL(result: String) {
        myFixture.configureByFile(getTestName(true) + ".p6")
        assertEquals(result, RakuDocRendering.externalUrl(myFixture.elementAtCaret))
    }
```

`testMethodExternalFromCOREClass` (line ~145) builds its own provider inline rather than using a helper. Rewrite that one method's plumbing too — `provider.getQuickNavigateInfo(decls[0].element, null)` becomes `RakuDocumentationTarget(decls[0].element!!, element).computeDocumentationHint()`, and `provider.generateDoc(...)` becomes the blocking call — **but leave its three expected strings exactly as they are.**

- [ ] **Step 2: Swap the registration**

In `src/main/resources/META-INF/plugin.xml`, replace lines 301-302:

```xml
<!-- remove -->
<lang.documentationProvider language="Raku"
                            implementationClass="org.raku.comma.docs.RakuDocumentationProvider"/>
<!-- add -->
<platform.backend.documentation.psiTargetProvider
    implementation="org.raku.comma.docs.RakuPsiDocumentationTargetProvider"/>
```

Note the attribute is `implementation`, not `implementationClass`, and there is no `language` attribute. Both registered at once would double every popup.

- [ ] **Step 3: Delete the old provider and fix the probe**

```bash
git rm src/main/java/org/raku/comma/docs/RakuDocumentationProvider.kt
```

In `DocDumpProbe.kt`, replace each `DocumentationManager.getProviderFromElement(x)` pair with a `RakuDocumentationTarget(x, null)`, using `computeDocumentationHint()` for the QUICK lines and `computeDocumentationBlocking(target.createPointer())?.html` for the DOC lines. Drop the `DocumentationManager` import.

- [ ] **Step 4: Run the full suite**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH"
./gradlew test --rerun
```

Expected: `1297 tests, 0 failures`. Every one of the 25 frozen expectations passing through the new API is the proof of parity.

- [ ] **Step 5: Eyeball the real output**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH"
./gradlew test --rerun --tests "org.raku.comma.docs.DocDumpProbe"
raku -e 'say "/tmp/docdump.txt".IO.slurp'
```

Expected: identical to the pre-migration dump — `class IntStr`, `method Capture(--&gt; Mu)`, the Baggy-addition operator body, `method end(--&gt; Int)`.

- [ ] **Step 6: Commit**

```bash
git add -A src/main/resources/META-INF/plugin.xml src/main/java/org/raku/comma/docs/ \
           src/test/kotlin/org/raku/comma/docs/
git commit -m "Serve documentation through DocumentationTarget

Swaps lang.documentationProvider for psiTargetProvider and deletes the legacy
provider. The 25 expectations in DocumentationTest are untouched -- only the
three helpers behind them changed -- so their passing is the parity proof.

psiTargetProvider takes a bare implementation attribute and is not
language-filtered, so the provider makes the Raku check itself."
```

---

## Task 7: Async the call-site branch

Last, so that if it misbehaves the migration is already landed and green.

**Files:**
- Modify: `src/main/java/org/raku/comma/docs/RakuDocumentationTarget.kt`

- [ ] **Step 1: Make the call-site branch async**

Only `RakuMethodCall` and `RakuSubCall` go async — those are the elements whose rendering runs `multiResolve(false)`, which can consult indexes. Declarator pod and CORE lookups stay synchronous: both are in-memory reads, and wrapping them would add coroutine machinery for no latency win.

```kotlin
    override fun computeDocumentation(): DocumentationResult? = when (element) {
        is RakuMethodCall, is RakuSubCall -> DocumentationResult.asyncDocumentation {
            readAction { documentation() }
        }
        else -> documentation()
    }

    private fun documentation(): DocumentationResult.Documentation? {
        val html = RakuDocRendering.docHtml(element) ?: return null
        val result = DocumentationResult.documentation(html)
        RakuDocRendering.externalUrl(element)?.let { result.externalUrl(it) }
        return result
    }
```

Imports to add: `com.intellij.openapi.application.readAction`, `org.raku.comma.psi.RakuMethodCall`, `org.raku.comma.psi.RakuSubCall`.

- [ ] **Step 2: Run the full suite**

```bash
export PATH="$RAKU_PREFIX/bin:$PATH"
./gradlew test --rerun
```

Expected: `1297 tests, 0 failures`. `testMethodExternalFromCOREClass` resolves `.end` through `multiResolve`, so it exercises exactly the branch being made async — and `computeDocumentationBlocking` resolves async results, so the helper needs no change.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/raku/comma/docs/RakuDocumentationTarget.kt
git commit -m "Compute call-site documentation off the UI thread

multiResolve can consult indexes and is the only documentation path that is
not an in-memory read. Declarator pod and CORE lookups stay synchronous:
both return an already-populated field or walk PSI, so async would add
machinery for no win."
```

---

## Done when

- `./gradlew test --rerun` reports **1297 tests, 0 failures** with `$RAKU_PREFIX` on `PATH`.
- No string inside a `testQuickDoc`/`testGeneratedDoc`/`testURL`/`assertEquals` call in `DocumentationTest.kt` differs from `HEAD` at the start of this plan. Check with:
  `git diff <base> -- src/test/kotlin/org/raku/comma/docs/DocumentationTest.kt` — only helper bodies and imports should appear.
- `/tmp/docdump.txt` matches its pre-migration content.
- `grep -rn "lang.documentationProvider" src/main/resources/META-INF/plugin.xml` returns nothing.
