# Conditional-Compilation Islands Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Every branch of a gen-cat conditional (`#?if jvm` … `#?endif`) gets syntax highlighting, real PSI, and indexing, parsed "as if its directive were true", with torn branches contained in lazy islands.

**Architecture:** The active (moar) selection keeps driving the file's outer parse. A wrapper lexer merges each blanked inactive region into one `CONDITIONAL_BRANCH` token registered as a whitespace token (so the 22.8k-line generated `RakuParser` never sees it). That token is an `ILazyParseableElementType` that lazily parses the branch's original text as a statement list under a `RakuCondBranch` PSI element. The highlighter re-lexes branch tokens with a `LayeredLexer` layer.

**Tech Stack:** IntelliJ Platform (LexerBase, ILazyParseableElementType, LayeredLexer), Java for `parsing/` (that package stays Java per Kotlin-conversion policy), Kotlin for new PSI, JUnit3-style platform tests via Gradle.

**Spec:** `docs/superpowers/specs/2026-09-17-conditional-compilation-islands-design.md`

## Global Constraints

- The generated parser (`RakuParser.java`, `MAINBraid.java`) and the cursor-machine lexer runtime must not be modified.
- Island expansion (lazy `parseContents`) must be pure lex/parse — no symbol resolution, no index queries (stub-building safety; see commit 4a05b048).
- Masking stays one-for-one: buffer length and every offset must remain valid.
- Marker lines (`#?if …`, `#?endif`) remain ordinary comment tokens at column 0 only.
- Run tests with `./gradlew test --tests "<pattern>"`. A test class must be verified to have actually run (check `build/test-results/test/TEST-<fqcn>.xml` is fresh) — `BUILD SUCCESSFUL` alone is not proof.
- Test-harness note: the platform escalates any `Logger.error` during a test to a failure; use that for "does not break" assertions.
- All commits end with: `Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>`

---

### Task 1: Region model in RakuConditionalCompilation

**Files:**
- Modify: `src/main/java/org/raku/comma/parsing/RakuConditionalCompilation.java`
- Test: `src/test/kotlin/org/raku/comma/parsing/RakuConditionalRegionsTest.kt`

**Interfaces:**
- Produces: `RakuConditionalCompilation.Region` (public static final class with `int start; int end; String condition; boolean active`), `public static List<Region> regions(CharSequence text)` (all directive regions, document order; `start` = offset of first char of the first body line, `end` = offset of the first char of the `#?endif` line; empty bodies produce `start == end`), `public static List<Region> inactiveRegions(CharSequence text)` (only `!active`, only non-empty), and `public static String conditionAt(CharSequence text, int offset)` (condition of the region whose `[start, end)` contains `offset`, else null). `preprocess` behavior is unchanged.

- [ ] **Step 1: Write the failing test**

```kotlin
package org.raku.comma.parsing

import junit.framework.TestCase

class RakuConditionalRegionsTest : TestCase() {

    private val text = """
        my ${'$'}shared = 1;
        #?if moar
        my ${'$'}m = 1;
        #?endif
        #?if jvm
        my ${'$'}j = 1;
        my ${'$'}j2 = 2;
        #?endif
        say ${'$'}shared;
    """.trimIndent()

    fun testRegionsReportsBothConditionals() {
        val regions = RakuConditionalCompilation.regions(text)
        assertEquals(2, regions.size)

        val moar = regions[0]
        assertTrue(moar.active)
        assertEquals("moar", moar.condition)
        assertEquals("my \$m = 1;\n", text.substring(moar.start, moar.end))

        val jvm = regions[1]
        assertFalse(jvm.active)
        assertEquals("jvm", jvm.condition)
        assertEquals("my \$j = 1;\nmy \$j2 = 2;\n", text.substring(jvm.start, jvm.end))
    }

    fun testInactiveRegionsOnlyReportsDeadBranches() {
        val inactive = RakuConditionalCompilation.inactiveRegions(text)
        assertEquals(1, inactive.size)
        assertEquals("jvm", inactive[0].condition)
    }

    fun testConditionAt() {
        val regions = RakuConditionalCompilation.regions(text)
        assertEquals("moar", RakuConditionalCompilation.conditionAt(text, regions[0].start))
        assertEquals("jvm", RakuConditionalCompilation.conditionAt(text, regions[1].end - 1))
        assertNull(RakuConditionalCompilation.conditionAt(text, 0))
    }

    fun testNegatedConditionKeepsBang() {
        val t = "#?if !js\nmy \$x = 1;\n#?endif\n"
        val regions = RakuConditionalCompilation.regions(t)
        assertEquals(1, regions.size)
        assertEquals("!js", regions[0].condition)
        assertTrue(regions[0].active) // !js is live under moar
    }

    fun testEmptyBodyProducesNoInactiveRegion() {
        val t = "#?if jvm\n#?endif\n"
        assertTrue(RakuConditionalCompilation.inactiveRegions(t).isEmpty())
    }

    fun testUnrecognisedMarkerIsNoRegion() {
        // '#?ifsomething' and trailing junk are not markers (mirrors isOmitted rules)
        assertTrue(RakuConditionalCompilation.regions("#?ifsomething\nfoo\n#?endif\n").isEmpty())
    }

    fun testSecondIfClosesTheFirstRegion() {
        val t = "#?if jvm\nmy \$a = 1;\n#?if js\nmy \$b = 2;\n#?endif\n"
        val regions = RakuConditionalCompilation.regions(t)
        assertEquals(2, regions.size)
        assertEquals("jvm", regions[0].condition)
        assertEquals("my \$a = 1;\n", t.substring(regions[0].start, regions[0].end))
        assertEquals("js", regions[1].condition)
    }

    fun testUnterminatedIfRunsToEndOfFile() {
        val t = "say 1;\n#?if jvm\nmy \$a = 1;\n"
        val regions = RakuConditionalCompilation.regions(t)
        assertEquals(1, regions.size)
        assertEquals(t.length, regions[0].end)
    }

    fun testPreprocessStillBlanksInactiveBody() {
        val processed = RakuConditionalCompilation.preprocess(text).toString()
        assertEquals(text.length, processed.length)
        assertFalse(processed.contains("\$j = 1"))
        assertTrue(processed.contains("\$m = 1"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "org.raku.comma.parsing.RakuConditionalRegionsTest"`
Expected: compilation FAILURE — `Region`, `regions`, `inactiveRegions`, `conditionAt` do not exist.

- [ ] **Step 3: Implement**

In `RakuConditionalCompilation.java`, add the `Region` class and refactor the scanning loop so `preprocess` and `regions` share it. Keep `isOmitted`'s exact validation rules, but split it: a new `parseCondition(text, from, lineEnd)` returns the raw condition string (`"jvm"`, `"!js"`, …) or null when the line is not a well-formed marker; activity = existing negation-vs-`BACKEND` logic.

```java
    public static final class Region {
        public final int start;
        public final int end;
        public final String condition;
        public final boolean active;

        Region(int start, int end, String condition, boolean active) {
            this.start = start;
            this.end = end;
            this.condition = condition;
            this.active = active;
        }
    }

    public static java.util.List<Region> regions(CharSequence text) {
        java.util.List<Region> result = new java.util.ArrayList<>();
        if (indexOf(text, IF_MARKER) < 0) return result;

        int length = text.length();
        int pos = 0;
        String openCondition = null;
        int bodyStart = -1;

        while (pos < length) {
            int lineEnd = lineEnd(text, pos);
            int nextLine = nextLineStart(text, lineEnd, length);

            if (startsWith(text, pos, ENDIF_MARKER)) {
                if (openCondition != null) {
                    result.add(new Region(bodyStart, pos, openCondition, isActive(openCondition)));
                    openCondition = null;
                }
            }
            else if (startsWith(text, pos, IF_MARKER)) {
                // A second #?if re-decides (preprocess semantics). The lines
                // under the first marker still belong to it, so close its
                // region at this marker line before re-opening.
                if (openCondition != null) {
                    result.add(new Region(bodyStart, pos, openCondition, isActive(openCondition)));
                }
                String condition = parseCondition(text, pos + IF_MARKER.length(), lineEnd);
                openCondition = condition;
                bodyStart = condition == null ? -1 : nextLine;
            }

            pos = nextLine;
        }
        // An unterminated #?if runs to end-of-file, exactly as preprocess blanks it.
        if (openCondition != null) {
            result.add(new Region(bodyStart, length, openCondition, isActive(openCondition)));
        }
        return result;
    }

    public static java.util.List<Region> inactiveRegions(CharSequence text) {
        java.util.List<Region> result = new java.util.ArrayList<>();
        for (Region region : regions(text))
            if (!region.active && region.end > region.start) result.add(region);
        return result;
    }

    public static String conditionAt(CharSequence text, int offset) {
        for (Region region : regions(text))
            if (region.start <= offset && offset < region.end) return region.condition;
        return null;
    }

    private static boolean isActive(String condition) {
        boolean negated = condition.startsWith("!");
        String name = negated ? condition.substring(1) : condition;
        return negated != name.equals(BACKEND);
    }

    /** Raw condition ("jvm", "!js") or null when the line is not a marker. */
    private static String parseCondition(CharSequence text, int from, int lineEnd) {
        int pos = skipSpaces(text, from, lineEnd);
        if (pos == from) return null; // '#?ifsomething' is not a marker

        boolean negated = pos < lineEnd && text.charAt(pos) == '!';
        if (negated) pos = skipSpaces(text, pos + 1, lineEnd);

        int nameStart = pos;
        while (pos < lineEnd && isWordChar(text.charAt(pos))) pos++;
        if (pos == nameStart) return null;

        String name = text.subSequence(nameStart, pos).toString();
        if (skipSpaces(text, pos, lineEnd) != lineEnd) return null; // trailing junk
        return negated ? "!" + name : name;
    }
```

Then rewrite `isOmitted` to delegate: `String condition = parseCondition(text, from, lineEnd); return condition != null && !isActive(condition);`

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "org.raku.comma.parsing.RakuConditionalRegionsTest" --tests "org.raku.comma.parsing.ConditionalCompilationTest"`
Expected: all PASS (the existing golden test proves `preprocess` is unchanged). Verify freshness via `build/test-results/test/`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/raku/comma/parsing/RakuConditionalCompilation.java src/test/kotlin/org/raku/comma/parsing/RakuConditionalRegionsTest.kt
git commit -m "Expose conditional-compilation regions from the preprocessor"
```

---

### Task 2: CONDITIONAL_BRANCH token, merging lexer, and island PSI

**Files:**
- Create: `src/main/java/org/raku/comma/parsing/RakuCondBranchElementType.java`
- Create: `src/main/java/org/raku/comma/parsing/RakuCondBranchMergingLexer.java`
- Create: `src/main/java/org/raku/comma/psi/RakuCondBranch.kt`
- Create: `src/main/java/org/raku/comma/psi/impl/RakuCondBranchImpl.kt`
- Modify: `src/main/java/org/raku/comma/parsing/RakuElementTypes.java` (add constant)
- Modify: `src/main/java/org/raku/comma/parsing/RakuParserDefinition.java` (createLexer, getWhitespaceTokens, createElement)
- Test: `src/test/kotlin/org/raku/comma/parsing/ConditionalIslandsTest.kt` + `testData/parsing/conditional-islands/ParsingTestData.p6`

**Interfaces:**
- Consumes: `RakuConditionalCompilation.inactiveRegions(CharSequence)` and `Region.start/end` from Task 1.
- Produces: `RakuElementTypes.CONDITIONAL_BRANCH` (an `ILazyParseableElementType`, debug name `Raku:CONDITIONAL_BRANCH`); `RakuCondBranchMergingLexer(Lexer delegate)`; PSI interface `RakuCondBranch : RakuPsiElement` with `val condition: String?`; impl `RakuCondBranchImpl(node: ASTNode)`.

- [ ] **Step 1: Write the failing golden test**

Create `testData/parsing/conditional-islands/ParsingTestData.p6`:

```
my $shared = 1;
#?if moar
my $backend = "moar";
#?endif
#?if jvm
my $backend = "jvm";
#?endif
say $shared;
```

Create the test class:

```kotlin
package org.raku.comma.parsing

class ConditionalIslandsTest : RakuParsingTestCase("conditional-islands")
```

- [ ] **Step 2: Run test to verify current (island-less) behavior**

Run: `./gradlew test --tests "org.raku.comma.parsing.ConditionalIslandsTest"`
Expected: FAIL on first run (missing `ParsingTestData.txt`; the harness writes it). Inspect the generated `testData/parsing/conditional-islands/ParsingTestData.txt`: the jvm branch must currently appear as plain whitespace (no `VARIABLE_DECLARATION` for the second `$backend`). Delete the generated `.txt` — the golden file is regenerated after the implementation.

- [ ] **Step 3: Implement the element type**

`RakuCondBranchElementType.java`:

```java
package org.raku.comma.parsing;

import com.intellij.psi.tree.ILazyParseableElementType;
import org.raku.comma.RakuLanguage;

/**
 * One inactive #?if branch, carried through the outer parse as a single
 * "whitespace" token so the generated parser never sees it, and lazily
 * parsed as a Raku statement list ("as if its directive were true") when the
 * PSI below it is first needed. The default ILazyParseableElementType
 * parseContents implementation reuses the language's ParserDefinition, which
 * is exactly what we want: pure lex/parse, no resolution, safe during stub
 * building.
 */
public class RakuCondBranchElementType extends ILazyParseableElementType {
    public RakuCondBranchElementType() {
        super("CONDITIONAL_BRANCH", RakuLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "Raku:" + super.toString();
    }
}
```

In `RakuElementTypes.java`, next to the other element type constants, add:

```java
    IElementType CONDITIONAL_BRANCH = new RakuCondBranchElementType();
```

- [ ] **Step 4: Implement the merging lexer**

`RakuCondBranchMergingLexer.java`:

```java
package org.raku.comma.parsing;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Wraps the cursor-machine lexer and merges the tokens covering each
 * inactive #?if region body (blanked to whitespace by preprocess) into a
 * single CONDITIONAL_BRANCH token. Delegate tokens straddling a region edge
 * are split, so the merged token covers exactly the region body lines.
 * The delegate is untouched otherwise; on files without directives this is
 * pass-through.
 */
public class RakuCondBranchMergingLexer extends LexerBase {
    private final Lexer delegate;

    private List<RakuConditionalCompilation.Region> regions;
    private int regionIndex;
    private int pos;              // wrapper's current token start
    private int tokenEnd;
    private IElementType tokenType;
    private boolean splitting;    // mid-split of a delegate token: state is not restartable

    /* Never re-use a state, so lexer resumption never mistakes a split
     * position for a clean restart point (same cheat as RakuLexer). */
    private int freshState = 1;

    public RakuCondBranchMergingLexer(Lexer delegate) {
        this.delegate = delegate;
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        regions = RakuConditionalCompilation.inactiveRegions(buffer);
        delegate.start(buffer, startOffset, endOffset, initialState);
        regionIndex = 0;
        pos = delegate.getTokenStart();
        computeToken();
    }

    @Override
    public void advance() {
        pos = tokenEnd;
        computeToken();
    }

    private void computeToken() {
        // Skip regions that end at or before the current position.
        while (regionIndex < regions.size() && regions.get(regionIndex).end <= pos) regionIndex++;

        // Move the delegate forward until its token covers (or starts at) pos.
        while (delegate.getTokenType() != null && delegate.getTokenEnd() <= pos) delegate.advance();

        if (delegate.getTokenType() == null && (regionIndex >= regions.size() || pos >= getBufferEnd())) {
            tokenType = null;
            tokenEnd = pos;
            splitting = false;
            return;
        }

        RakuConditionalCompilation.Region region =
            regionIndex < regions.size() ? regions.get(regionIndex) : null;

        if (region != null && region.start <= pos) {
            // Inside a region: one merged token to the region end.
            tokenType = RakuElementTypes.CONDITIONAL_BRANCH;
            tokenEnd = region.end;
            // The delegate token containing region.end may extend past it.
            splitting = delegate.getTokenType() != null && delegate.getTokenStart() < region.end
                        && delegate.getTokenEnd() > region.end;
            return;
        }

        // Outside any region: pass the delegate token through, clipped at the
        // next region start if it straddles one.
        tokenType = delegate.getTokenType();
        int dEnd = delegate.getTokenEnd();
        if (region != null && region.start < dEnd) {
            tokenEnd = region.start;
            splitting = true;
        } else {
            tokenEnd = dEnd;
            splitting = false;
        }
    }

    @Override
    public int getState() {
        return splitting || insideRegion() ? freshState++ : delegate.getState();
    }

    private boolean insideRegion() {
        return tokenType == RakuElementTypes.CONDITIONAL_BRANCH;
    }

    @Override
    public IElementType getTokenType() {
        return tokenType;
    }

    @Override
    public int getTokenStart() {
        return pos;
    }

    @Override
    public int getTokenEnd() {
        return tokenEnd;
    }

    @NotNull
    @Override
    public CharSequence getBufferSequence() {
        return delegate.getBufferSequence();
    }

    @Override
    public int getBufferEnd() {
        return delegate.getBufferEnd();
    }
}
```

- [ ] **Step 5: Implement the PSI element**

`src/main/java/org/raku/comma/psi/RakuCondBranch.kt`:

```kotlin
package org.raku.comma.psi

/**
 * The PSI island for one inactive `#?if` branch: a transparent container
 * whose children are the branch's statements parsed as if the directive
 * were true. Deliberately NOT a RakuPsiScope, so scope walkers
 * (getDeclarations/getSymbolContributors) descend into it and the branch's
 * declarations join the surrounding scope — that is what makes them
 * indexed, completable, and navigable.
 */
interface RakuCondBranch : RakuPsiElement {
    /** The raw directive condition, e.g. "jvm" or "!moar"; null if unrecoverable. */
    val condition: String?
}
```

`src/main/java/org/raku/comma/psi/impl/RakuCondBranchImpl.kt`:

```kotlin
package org.raku.comma.psi.impl

import com.intellij.lang.ASTNode
import org.raku.comma.parsing.RakuConditionalCompilation
import org.raku.comma.psi.RakuASTWrapperPsiElement
import org.raku.comma.psi.RakuCondBranch

class RakuCondBranchImpl(node: ASTNode) : RakuASTWrapperPsiElement(node), RakuCondBranch {
    override val condition: String?
        get() = RakuConditionalCompilation.conditionAt(containingFile.text, textOffset)

    override fun toString(): String = "RakuCondBranch(${condition ?: "?"})"
}
```

- [ ] **Step 6: Wire the parser definition**

In `RakuParserDefinition.java`:

```java
    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new RakuCondBranchMergingLexer(new RakuLexer());
    }
```

Replace `getCommentTokens` (keep the existing comment, extend it). NOT the
whitespace set: `PsiBuilderImpl.createLeaf` returns `PsiWhiteSpaceImpl`
unconditionally for whitespace-set tokens, so a lazy type there never
becomes a chameleon. Comment-set tokens take the normal leaf path where the
`ILazyParseableElementType` check engages — JavaDoc's `DOC_COMMENT` is the
platform precedent.

```java
    // Both whitespace and comment tokens are empty, as we want to
    // match it in our parser. The one exception: an inactive #?if branch is
    // carried as a single CONDITIONAL_BRANCH token that the generated parser
    // must never see; registering it as a comment token makes PsiBuilder
    // place it into the tree (as a lazy-parseable island, the same way
    // JavaDoc's DOC_COMMENT works) behind the parser's back.
    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return TokenSet.create(RakuElementTypes.CONDITIONAL_BRANCH);
    }
```

In `createElement`, add (alongside the STATEMENT_LIST case):

```java
        if (type == RakuElementTypes.CONDITIONAL_BRANCH)
            return new RakuCondBranchImpl(astNode);
```

- [ ] **Step 7: Regenerate the golden file and inspect**

Run: `./gradlew test --tests "org.raku.comma.parsing.ConditionalIslandsTest"`
Expected: FAIL once (writes `ParsingTestData.txt`). **Inspect the generated file — this is the critical verification of the whitespace-set mechanism:** it must contain a `Raku:CONDITIONAL_BRANCH` node whose children include a `VARIABLE_DECLARATION` for the jvm `$backend`, and the trailing `say $shared;` must parse as a normal statement. If the branch appears as a plain leaf with no children, the lazy-leaf-in-whitespace-set mechanism did not engage — STOP, do not improvise: report the finding and reassess with the human partner (the spec names a fallback of accepting the token in the statement-list loop, which needs its own design pass on the generated parser).

- [ ] **Step 8: Run to verify green, plus the full parsing suite**

Run: `./gradlew test --tests "org.raku.comma.parsing.*"`
Expected: `ConditionalIslandsTest` PASSES on second run. `ConditionalCompilationTest`'s golden file may now differ (its inactive regions become islands): inspect the diff of `testData/parsing/conditional-compilation/ParsingTestData.txt`, confirm the change is exactly "blank region → island subtree", delete the stale golden, re-run to regenerate, re-run to green. All other parsing suites must be untouched.

- [ ] **Step 9: Commit**

```bash
git add src/main/java/org/raku/comma/parsing src/main/java/org/raku/comma/psi src/test/kotlin/org/raku/comma/parsing/ConditionalIslandsTest.kt testData/parsing/conditional-islands testData/parsing/conditional-compilation
git commit -m "Parse inactive #?if branches as lazy conditional islands"
```

---

### Task 3: Torn-branch containment goldens

**Files:**
- Test: `src/test/kotlin/org/raku/comma/parsing/ConditionalIslandsTornTest.kt` + `testData/parsing/conditional-islands-torn/ParsingTestData.p6`

**Interfaces:**
- Consumes: island machinery from Task 2. No new production code expected — this task pins the containment property.

- [ ] **Step 1: Create the torn fixture** (the documented Date.rakumod shape)

`testData/parsing/conditional-islands-torn/ParsingTestData.p6`:

```
#?if !js
my $valid-units = foo(
#?endif
#?if js
my $valid-units = bar(
#?endif
  'day', 1,
);
say "after";
```

```kotlin
package org.raku.comma.parsing

class ConditionalIslandsTornTest : RakuParsingTestCase("conditional-islands-torn")
```

- [ ] **Step 2: Generate, inspect, and lock the golden**

Run: `./gradlew test --tests "org.raku.comma.parsing.ConditionalIslandsTornTest"` (twice: generate, then green).
Inspect the generated `.txt` between runs. Required properties:
1. The `!js` (active) branch's `foo(` call consumes `'day', 1,` and `)` exactly as before islands existed — the outer structure follows the active selection.
2. The `js` branch is a `Raku:CONDITIONAL_BRANCH` island containing the incomplete `bar(` call, with any `PsiErrorElement` INSIDE the island node, not after it.
3. `say "after";` is a clean top-level statement.

If property 2 or 3 fails (errors escape the island), STOP and report — containment is the spec's core guarantee.

- [ ] **Step 3: Commit**

```bash
git add src/test/kotlin/org/raku/comma/parsing/ConditionalIslandsTornTest.kt testData/parsing/conditional-islands-torn
git commit -m "Pin torn conditional branches as contained islands"
```

---

### Task 4: Branch-aware highlighting and word indexing

**Files:**
- Modify: `src/main/java/org/raku/comma/parsing/RakuHighlighterLexer.java` (add static factory)
- Modify: `src/main/java/org/raku/comma/highlighter/RakuSyntaxHighlighter.kt`
- Modify: `src/main/java/org/raku/comma/parsing/RakuWordsScanner.java`
- Test: `src/test/kotlin/org/raku/comma/highlighting/ConditionalBranchHighlightingTest.kt`

**Interfaces:**
- Consumes: `RakuCondBranchMergingLexer`, `RakuElementTypes.CONDITIONAL_BRANCH` from Task 2.
- Produces: `public static Lexer RakuHighlighterLexer.branchAware()` — a `LayeredLexer` whose base merges branches and whose layer re-lexes each `CONDITIONAL_BRANCH` token as live Raku.

- [ ] **Step 1: Write the failing tests**

```kotlin
package org.raku.comma.highlighting

import com.intellij.psi.tree.IElementType
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.parsing.RakuElementTypes
import org.raku.comma.parsing.RakuHighlighterLexer
import org.raku.comma.parsing.RakuTokenTypes

class ConditionalBranchHighlightingTest : CommaFixtureTestCase() {

    private val text = "my \$shared = 1;\n#?if jvm\nmy \$j = \"code\";\n#?endif\nsay \$shared;\n"

    private fun lexed(): List<Pair<IElementType, String>> {
        val lexer = RakuHighlighterLexer.branchAware()
        lexer.start(text)
        val tokens = ArrayList<Pair<IElementType, String>>()
        while (lexer.tokenType != null) {
            tokens.add(lexer.tokenType!! to text.substring(lexer.tokenStart, lexer.tokenEnd))
            lexer.advance()
        }
        return tokens
    }

    fun testBranchBodyLexesAsCodeNotWhitespace() {
        val tokens = lexed()
        // The jvm branch's string literal must be lexed as a real token.
        assertTrue("expected a token for \"code\" inside the branch, got: $tokens",
                   tokens.any { it.second == "code" })
        // And the merged branch token itself must not survive to the token stream.
        assertFalse(tokens.any { it.first == RakuElementTypes.CONDITIONAL_BRANCH })
    }

    fun testTokensTileTheWholeFile() {
        val lexer = RakuHighlighterLexer.branchAware()
        lexer.start(text)
        var expectedStart = 0
        while (lexer.tokenType != null) {
            assertEquals("gap or overlap at ${lexer.tokenStart}", expectedStart, lexer.tokenStart)
            expectedStart = lexer.tokenEnd
            lexer.advance()
        }
        assertEquals(text.length, expectedStart)
    }

    // The reported breakage: editing near a directive must not blow up
    // incremental highlighting. The harness escalates logged errors, so
    // survival of type-then-rehighlight IS the assertion.
    fun testEditingNearDirectiveDoesNotBreakHighlighting() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, text + "say 42;\n")
        myFixture.doHighlighting()
        myFixture.editor.caretModel.moveToOffset(myFixture.file.textLength)
        myFixture.type("my \$x = 3;")
        myFixture.doHighlighting()
        myFixture.editor.caretModel.moveToOffset(text.indexOf("say"))
        myFixture.type("# ")
        myFixture.doHighlighting()
    }
}
```

- [ ] **Step 2: Run tests to verify failure**

Run: `./gradlew test --tests "org.raku.comma.highlighting.ConditionalBranchHighlightingTest"`
Expected: compilation FAILURE (`branchAware` does not exist).

- [ ] **Step 3: Implement**

`RakuHighlighterLexer.java` becomes:

```java
package org.raku.comma.parsing;

import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;

public class RakuHighlighterLexer extends RakuLexer {
    @Override
    public void advance() {
        // Skip over zero-width tokens, which break various lexer-based features,
        // such as brace matching in some cases.
        super.advance();
        while (getTokenType() != null && getTokenStart() == getTokenEnd()) super.advance();
    }

    /**
     * The lexer for user-facing token streams (syntax highlighting, word
     * indexing): inactive #?if branches are merged into CONDITIONAL_BRANCH
     * tokens by the wrapper, then re-lexed as live Raku by the layer -- so
     * every branch shows real code, "as if its directive were true".
     */
    public static Lexer branchAware() {
        LayeredLexer lexer = new LayeredLexer(new RakuCondBranchMergingLexer(new RakuHighlighterLexer()));
        lexer.registerSelfStoppingLayer(new RakuHighlighterLexer(),
                                        new IElementType[]{RakuElementTypes.CONDITIONAL_BRANCH},
                                        IElementType.EMPTY_ARRAY);
        return lexer;
    }
}
```

`RakuSyntaxHighlighter.kt`: change `getHighlightingLexer()` to `RakuHighlighterLexer.branchAware()`.

`RakuWordsScanner.java`: change the constructor's `myLexer = new RakuLexer();` to `myLexer = RakuHighlighterLexer.branchAware();` with the comment `// Branch-aware so words inside inactive #?if branches are find-usages-indexed.`

- [ ] **Step 4: Run tests to verify green**

Run: `./gradlew test --tests "org.raku.comma.highlighting.*" --tests "org.raku.comma.findUsages.*"`
Expected: all PASS (find-usages suite guards the words-scanner change).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/raku/comma/parsing/RakuHighlighterLexer.java src/main/java/org/raku/comma/highlighter/RakuSyntaxHighlighter.kt src/main/java/org/raku/comma/parsing/RakuWordsScanner.java src/test/kotlin/org/raku/comma/highlighting/ConditionalBranchHighlightingTest.kt
git commit -m "Highlight and word-index inactive #?if branches as live code"
```

---

### Task 5: Islands contribute stubs and declarations

**Files:**
- Test: `src/test/kotlin/org/raku/comma/stub/ConditionalBranchStubTest.kt`

**Interfaces:**
- Consumes: island machinery (Task 2). Expected to need no production change — `DefaultStubBuilder` walks the AST and lazy nodes expand on walking, and `RakuPsiScope` walkers descend into non-scope `RakuPsiElement`s. If the test fails, the fix belongs in whichever walker refuses to descend (report before changing anything outside that walker).

- [ ] **Step 1: Write the test**

```kotlin
package org.raku.comma.stub

import com.intellij.psi.impl.source.PsiFileImpl
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuModuleFileType
import org.raku.comma.psi.RakuFile
import org.raku.comma.psi.stub.RakuRoutineDeclStub

class ConditionalBranchStubTest : CommaFixtureTestCase() {

    fun testJvmOnlyRoutineIsStubbed() {
        myFixture.configureByText(
            RakuModuleFileType.INSTANCE,
            "sub shared-sub() { }\n#?if jvm\nsub jvm-only() { }\n#?endif\n"
        )
        val stubTree = (myFixture.file as PsiFileImpl).calcStubTree()
        val routineNames = stubTree.plainList
            .mapNotNull { it as? RakuRoutineDeclStub }
            .map { it.routineName }
        assertTrue("expected jvm-only in $routineNames", routineNames.contains("jvm-only"))
        assertTrue(routineNames.contains("shared-sub"))
    }

    fun testJvmOnlyDeclarationIsInFileDeclarations() {
        myFixture.configureByText(
            RakuModuleFileType.INSTANCE,
            "#?if jvm\nsub jvm-only() { }\n#?endif\n"
        )
        val declarations = (myFixture.file as RakuFile).getDeclarations()
        assertTrue(declarations.any { it.name == "jvm-only" })
    }
}
```

Note: check `RakuRoutineDeclStub`'s accessor name before running — if the stub interface calls it `getName()` rather than `getRoutineName()`, use that (the point of the assertion is the name string, not the accessor).

- [ ] **Step 2: Run, diagnose, fix if needed**

Run: `./gradlew test --tests "org.raku.comma.stub.ConditionalBranchStubTest"`
Expected: PASS with no production change. If it fails: read the failure — the likely candidates are (a) `RakuFileStubBuilder`'s skip logic and (b) `shouldCreateStub` guards on parents. Fix only the walker that refused to descend, nothing else. If the failure is the stub-vs-AST mismatch error (`stub tree does not match AST`), STOP and report — that means lazy islands and stub building disagree structurally, which needs a design conversation, not a patch.

- [ ] **Step 3: Run neighbor suites**

Run: `./gradlew test --tests "org.raku.comma.stub.*" --tests "org.raku.comma.completion.*"`
Expected: all PASS.

- [ ] **Step 4: Commit**

```bash
git add src/test/kotlin/org/raku/comma/stub/ConditionalBranchStubTest.kt
git commit -m "Pin stub and declaration contribution from conditional islands"
```

---

### Task 6: Mutual-exclusivity guard for duplicate declarations

**Files:**
- Create: `src/main/java/org/raku/comma/psi/RakuConditionalBranches.kt`
- Modify: `src/main/java/org/raku/comma/highlighter/RakuHighlightVisitor.java` (the `getDuplicateHighlightInfo` call sites / `markDuplicateValue`)
- Test: `src/test/kotlin/org/raku/comma/highlighting/ConditionalBranchDuplicatesTest.kt`

**Interfaces:**
- Consumes: `RakuCondBranch.condition` (Task 2), `RakuConditionalCompilation.conditionAt` (Task 1).
- Produces: `object RakuConditionalBranches` with `fun conditionOf(element: PsiElement): String?` and `fun inMutuallyExclusiveBranches(a: PsiElement, b: PsiElement): Boolean`.

- [ ] **Step 1: Write the failing tests**

```kotlin
package org.raku.comma.highlighting

import com.intellij.lang.annotation.HighlightSeverity
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

class ConditionalBranchDuplicatesTest : CommaFixtureTestCase() {

    private fun errorTexts(code: String): List<String> {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, code)
        return myFixture.doHighlighting()
            .filter { it.severity == HighlightSeverity.ERROR }
            .map { it.description ?: "" }
    }

    fun testCrossBranchTwinsDoNotFlag() {
        val errors = errorTexts(
            "#?if jvm\nsub backend-name() { \"jvm\" }\n#?endif\n" +
            "#?if !jvm\nsub backend-name() { \"not jvm\" }\n#?endif\n"
        )
        assertTrue("cross-branch twins flagged: $errors", errors.isEmpty())
    }

    fun testDifferentBackendsDoNotFlag() {
        val errors = errorTexts(
            "#?if jvm\nsub backend-name() { \"jvm\" }\n#?endif\n" +
            "#?if js\nsub backend-name() { \"js\" }\n#?endif\n"
        )
        assertTrue("jvm-vs-js twins flagged: $errors", errors.isEmpty())
    }

    fun testSameBranchDuplicatesStillFlag() {
        val errors = errorTexts(
            "#?if jvm\nsub backend-name() { 1 }\nsub backend-name() { 2 }\n#?endif\n"
        )
        assertTrue("same-branch duplicate NOT flagged", errors.isNotEmpty())
    }

    fun testPlainDuplicatesStillFlag() {
        val errors = errorTexts("sub twice() { 1 }\nsub twice() { 2 }\n")
        assertTrue("plain duplicate NOT flagged", errors.isNotEmpty())
    }

    fun testActiveCodeVsCompatibleBranchStillFlags() {
        // !js is live under moar, so a shared decl genuinely collides with it.
        val errors = errorTexts(
            "sub twice() { 1 }\n#?if !js\nsub twice() { 2 }\n#?endif\n"
        )
        assertTrue("shared-vs-active-branch duplicate NOT flagged", errors.isNotEmpty())
    }
}
```

Before Step 2, sanity-check `testPlainDuplicatesStillFlag` in isolation: it documents today's behavior and must pass BEFORE any changes. If it does not (the duplicate check may key off something else, e.g. only multi/only mixes), adjust the fixture code until the plain-duplicate case genuinely flags today, and mirror that shape in the other tests. The guard tests are only meaningful against a working duplicate detector.

- [ ] **Step 2: Run to verify the two cross-branch tests fail**

Run: `./gradlew test --tests "org.raku.comma.highlighting.ConditionalBranchDuplicatesTest"`
Expected: `testCrossBranchTwinsDoNotFlag` and `testDifferentBackendsDoNotFlag` FAIL (twins are currently flagged); the three "still flags" tests PASS.

- [ ] **Step 3: Implement the utility**

`RakuConditionalBranches.kt`:

```kotlin
package org.raku.comma.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.parsing.RakuConditionalCompilation

/**
 * Which #?if condition an element lives under, and whether two elements can
 * never coexist in one build. gen-cat has no else-chains, so exclusivity is
 * decided by the conditions themselves, not adjacency: `X` vs `!X`, or two
 * different positive backends. A negated pair like `!jvm` vs `!js` is NOT
 * exclusive (both true on moar), and unconditional code is exclusive with
 * nothing that can be true alongside the active backend.
 */
object RakuConditionalBranches {

    fun conditionOf(element: PsiElement): String? {
        // Inside an island the branch element knows its condition; active
        // regions have no island, so fall back to the positional lookup.
        val branch = PsiTreeUtil.getParentOfType(element, RakuCondBranch::class.java, false)
        if (branch != null) return branch.condition
        val file = element.containingFile ?: return null
        return RakuConditionalCompilation.conditionAt(file.text, element.textOffset)
    }

    fun inMutuallyExclusiveBranches(a: PsiElement, b: PsiElement): Boolean {
        return exclusive(conditionOf(a), conditionOf(b))
    }

    private fun exclusive(condA: String?, condB: String?): Boolean {
        if (condA == null || condB == null) return false
        val negA = condA.startsWith("!")
        val negB = condB.startsWith("!")
        val nameA = if (negA) condA.substring(1) else condA
        val nameB = if (negB) condB.substring(1) else condB
        if (nameA == nameB) return negA != negB      // jvm vs !jvm
        return !negA && !negB                        // jvm vs js
    }
}
```

- [ ] **Step 4: Guard the duplicate reports**

In `RakuHighlightVisitor.java` there are four `getDuplicateHighlightInfo(...)` call sites (near lines 222, 241, 284, 409) plus the pooled selection in `markDuplicateValue`. Apply one rule everywhere: a previously-pooled element that is mutually exclusive with the current declaration is not a duplicate of it.

In `markDuplicateValue`, filter the pool before selecting what to mark:

```java
    private boolean markDuplicateValue(RakuPsiElement decl, List<RakuPsiElement> v) {
        List<RakuPsiElement> coexisting = v.stream()
            .filter(other -> other == decl
                    || !RakuConditionalBranches.INSTANCE.inMutuallyExclusiveBranches(other, decl))
            .collect(Collectors.toList());
        if (coexisting.size() < 2) return false;
        // ... existing body, operating on `coexisting` instead of `v` ...
    }
```

At the `visitSignatureHolder` report site (~line 284), where a pooled holder pair is about to be reported against the current one, add the same skip:

```java
    if (RakuConditionalBranches.INSTANCE.inMutuallyExclusiveBranches(
            (PsiElement) oldAndNewHolders.first, (PsiElement) oldAndNewHolders.second)) {
        continue; // different #?if worlds; both are real in their own build
    }
```

(Exact receiver expressions must be adapted to the surrounding code when editing — the rule, not the spelling, is normative: both elements of every reported pair go through `inMutuallyExclusiveBranches` first.) Do the same at the ~222, ~241 and ~409 sites, comparing the element about to be reported with the element it is reported against.

- [ ] **Step 5: Run to verify all five tests pass**

Run: `./gradlew test --tests "org.raku.comma.highlighting.ConditionalBranchDuplicatesTest"`
Expected: 5/5 PASS.

- [ ] **Step 6: Run the annotation suite for regressions**

Run: `./gradlew test --tests "org.raku.comma.annotation.*" --tests "org.raku.comma.highlighting.*"`
Expected: only the known pre-existing flake `AnnotationTest.testCallArityMismatchAnnotating` (`.perl` deprecation, environment-dependent) may fail; anything else is a regression to fix before committing.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/org/raku/comma/psi/RakuConditionalBranches.kt src/main/java/org/raku/comma/highlighter/RakuHighlightVisitor.java src/test/kotlin/org/raku/comma/highlighting/ConditionalBranchDuplicatesTest.kt
git commit -m "Exempt mutually exclusive #?if branches from duplicate checks"
```

---

### Task 7: Full-suite verification against real Rakudo sources

**Files:**
- No new files; possible golden regenerations only.

- [ ] **Step 1: Smoke-test against a real Rakudo file**

Rakudo sources live at `~/code/raku/x.core/rakudo/src`. Pick the two known-hard shapes and run them through a throwaway fixture check (write the file content into `myFixture.configureByText` in a scratch test, `doHighlighting()`, then DELETE the scratch test before committing):
- `src/core.c/Date.rakumod`-style torn `nqp::hash(` conditional,
- a file with adjacent `#?if jvm` / `#?if moar` / `#?if js` alternatives (e.g. the shape in `main.nqp` lines 64-72, as `.rakumod` content).
Expected: no logged errors (harness escalates), and highlighting completes.

- [ ] **Step 2: Full test run**

Run: `./gradlew test`
Expected: green except the documented pre-existing flake (`AnnotationTest.testCallArityMismatchAnnotating`). Any parsing golden diffs must be inspected: legitimate island-shaped changes get regenerated and committed; anything else is a regression.

- [ ] **Step 3: Commit any regenerated goldens**

```bash
git add testData/parsing
git commit -m "Regenerate parsing goldens for conditional islands"
```

(Skip the commit if nothing was regenerated.)
