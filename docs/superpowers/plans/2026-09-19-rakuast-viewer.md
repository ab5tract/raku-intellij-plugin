# RakuAST Viewer Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A tool window that shows Rakudo's real `RakuAST` tree for the selected Raku code, lets the user edit node attributes, and writes each edit back by replacing exactly that node's source span.

**Architecture:** A bundled Raku script does all RakuAST work and prints one line of JSON; a project service runs it via the configured SDK and decodes the JSON; a Swing panel renders tree + attributes; a separate applier writes edits through PSI + `RangeMarker` inside a `WriteCommandAction`. The script knows nothing about IntelliJ, the service nothing about Swing, and only the applier touches documents.

**Tech Stack:** Kotlin, IntelliJ Platform SDK, kotlinx.serialization, Raku (`use experimental :rakuast`), JUnit via `CommaFixtureTestCase`, Gradle.

**Design doc:** `docs/superpowers/specs/2026-09-19-rakuast-viewer-design.md`

## Global Constraints

- **Every gradle invocation needs the rakubrew preamble.** Shell state does not persist between tool calls, so it all goes in one command:
  ```bash
  eval "$(~/.rakubrew/bin/rakubrew init Zsh)"
  rakubrew switch "${RAKUBREW_RAKU_VERSION:-moar-2026.03}"
  ./gradlew test --rerun --tests "..."
  ```
  Without it the system Rakudo in `/usr/bin` wins and symbol-dependent assertions fail in ways that impersonate plugin bugs. Note the `moar-` prefix; a bare `2026.03` prints "Sorry, not found" and still exits 0.
- **A green build is not evidence.** `PATH` and the SDK are not declared inputs of the `test` task. Always pass `--rerun`, and confirm tests actually executed:
  ```bash
  raku -e 'my $n = 0; for "build/test-results/test".IO.dir(test => *.ends-with(".xml")) -> $p {
      $n += +$0 if $p.slurp.substr(0, 600) ~~ / "tests=\"" (\d+) "\"" / }; say "$n tests"'
  ```
- **Never assert deparse formatting in tests.** Assert tree *structure* — node classes and paths — only. `sub f ($a!)` on 2026.03 became `sub f ($a)` on 2026.08-445; an assertion on that text is red on one Rakudo and green on another.
- **Text processing in Raku, not Python.** This is a Raku project; ad-hoc parsing and tallying goes in `raku -e '...'`.
- **The script never signals failure by exit code.** `RakuCommandLine.executeAndRead` reads stdout only and returns an empty list on any non-zero exit, discarding the reason. All errors are printed as `{"error": "..."}` on stdout with `exit 0`.
- **Replacement is always narrow.** Only the edited node's span is ever replaced. Anything wider silently deletes the user's `#` comments, which have no AST node and cannot be recovered.
- **Bleeding-edge Rakudo for manual RakuAST probing:** `/home/longwalker/code/raku/x.core/raku-prefix/bin/raku` (v2026.08-445 at time of writing). The test suite still pins rakubrew.

## Protocol note (deviation from the design doc)

The design doc says the script "reads a JSON request on stdin". **Implement argv + temp files instead.** Raku core has no JSON *parser*, and `RakuCommandLine.executeAndRead` never writes to stdin — it only reads stdout. Vendoring a parser to avoid that is wasted work. The script therefore takes positional arguments and reads large values from temp files:

```
raku rakuast-tool.raku analyze <source-file>
raku rakuast-tool.raku edit    <source-file> <path-csv> <attr> <value-file> <value-kind>
```

Output is unchanged: one line of JSON on stdout. Only emitting JSON is needed, which the vendored `to-json` already covers.

## File Structure

| File | Responsibility |
|---|---|
| `src/main/resources/rakuast/rakuast-tool.raku` | All RakuAST work. Parse, walk, introspect, mutate, deparse. Emits JSON. Zero IntelliJ knowledge. |
| `src/main/java/org/raku/comma/rakuast/AstJson.kt` | `@Serializable` wire model + `decode` helpers. Pure; no subprocess. |
| `src/main/java/org/raku/comma/rakuast/RakuAstService.kt` | Project service. Extracts script, runs via `RakuCommandLine`, decodes. `analyze` / `edit`. |
| `src/main/java/org/raku/comma/rakuast/RakuAstEditApplier.kt` | The only writer. PSI + `RangeMarker` + `WriteCommandAction`. |
| `src/main/java/org/raku/comma/rakuast/RakuAstViewerPanel.kt` | Swing panel: tree + attribute table. |
| `src/main/java/org/raku/comma/rakuast/RakuAstViewerFactory.kt` | `ToolWindowFactory`. |
| `src/main/java/org/raku/comma/rakuast/AnalyzeSelectionAction.kt` | Action populating the panel from the selection. |
| `src/main/resources/META-INF/plugin.xml` | Register tool window + action. |
| `src/test/kotlin/org/raku/comma/rakuast/*` | Tests, one file per unit above. |

---

### Task 1: Wire model and JSON decoding

Pure Kotlin. No subprocess, so these tests are fast and deterministic.

**Files:**
- Create: `src/main/java/org/raku/comma/rakuast/AstJson.kt`
- Test: `src/test/kotlin/org/raku/comma/rakuast/AstJsonTest.kt`

**Interfaces:**
- Consumes: nothing.
- Produces: `AstSpan(from: Int, to: Int)`, `AstAttr(name: String, kind: String, display: String, editable: Boolean)`, `AstNode(nodeClass: String, path: List<Int>, span: AstSpan, attrs: List<AstAttr>, children: List<AstNode>)`, `AnalyzeResult(tree: AstNode?, error: String?)`, `EditResult(text: String?, span: AstSpan?, tree: AstNode?, error: String?)`, and `AstJson.decodeAnalyze(String): AnalyzeResult`, `AstJson.decodeEdit(String): EditResult`. Both return a result carrying `error` rather than throwing.

- [ ] **Step 1: Write the failing test**

```kotlin
package org.raku.comma.rakuast

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AstJsonTest {

    @Test
    fun decodesAnalyzeTree() {
        val json = """
            {"tree":{"class":"RakuAST::StatementList","path":[],"span":{"from":0,"to":19},
             "attrs":[],
             "children":[{"class":"RakuAST::IntLiteral","path":[0],"span":{"from":8,"to":10},
              "attrs":[{"name":"value","kind":"scalar","display":"41","editable":true}],
              "children":[]}]}}
        """.trimIndent()

        val result = AstJson.decodeAnalyze(json)

        assertNull(result.error)
        val root = result.tree!!
        assertEquals("RakuAST::StatementList", root.nodeClass)
        assertEquals(emptyList<Int>(), root.path)
        val child = root.children.single()
        assertEquals("RakuAST::IntLiteral", child.nodeClass)
        assertEquals(listOf(0), child.path)
        assertEquals(8, child.span.from)
        assertEquals(10, child.span.to)
        val attr = child.attrs.single()
        assertEquals("value", attr.name)
        assertEquals("scalar", attr.kind)
        assertTrue(attr.editable)
    }

    @Test
    fun decodesErrorPayload() {
        val result = AstJson.decodeAnalyze("""{"error":"Invalid typename 'Foo'"}""")
        assertNull(result.tree)
        assertEquals("Invalid typename 'Foo'", result.error)
    }

    @Test
    fun decodesEditResult() {
        val json = """{"text":"99","span":{"from":8,"to":10},
                       "tree":{"class":"RakuAST::StatementList","path":[],
                               "span":{"from":0,"to":19},"attrs":[],"children":[]}}"""
        val result = AstJson.decodeEdit(json)
        assertNull(result.error)
        assertEquals("99", result.text)
        assertEquals(8, result.span!!.from)
        assertEquals("RakuAST::StatementList", result.tree!!.nodeClass)
    }

    // Garbage must not throw: the subprocess can emit anything, including
    // an empty string when RakuCommandLine swallows a non-zero exit.
    @Test
    fun malformedInputBecomesError() {
        for (bad in listOf("", "not json", "[]", "{")) {
            val result = AstJson.decodeAnalyze(bad)
            assertNull("tree should be null for: $bad", result.tree)
            assertTrue("error should be set for: $bad", result.error != null)
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
eval "$(~/.rakubrew/bin/rakubrew init Zsh)"
rakubrew switch "${RAKUBREW_RAKU_VERSION:-moar-2026.03}"
./gradlew test --rerun --tests "org.raku.comma.rakuast.AstJsonTest"
```
Expected: FAIL — `Unresolved reference: AstJson`.

- [ ] **Step 3: Write minimal implementation**

```kotlin
package org.raku.comma.rakuast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AstSpan(val from: Int, val to: Int)

@Serializable
data class AstAttr(
    val name: String,
    val kind: String,          // "scalar" | "node" | "list" | "null"
    val display: String,
    val editable: Boolean = false,
)

@Serializable
data class AstNode(
    // `class` is a Kotlin keyword, so the wire name is mapped explicitly.
    @SerialName("class") val nodeClass: String,
    val path: List<Int> = emptyList(),
    val span: AstSpan,
    val attrs: List<AstAttr> = emptyList(),
    val children: List<AstNode> = emptyList(),
)

@Serializable
data class AnalyzeResult(val tree: AstNode? = null, val error: String? = null)

@Serializable
data class EditResult(
    val text: String? = null,
    val span: AstSpan? = null,
    val tree: AstNode? = null,
    val error: String? = null,
)

object AstJson {
    // Matches RakuExternalNamesParser's configuration.
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun decodeAnalyze(text: String): AnalyzeResult =
        decode(text) { AnalyzeResult(error = it) }

    fun decodeEdit(text: String): EditResult =
        decode(text) { EditResult(error = it) }

    private inline fun <reified T> decode(text: String, onError: (String) -> T): T =
        try {
            json.decodeFromString<T>(text)
        } catch (e: Exception) {
            onError(
                if (text.isBlank()) "The Raku backend produced no output."
                else "Could not read the Raku backend's response: ${e.message}"
            )
        }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run the same command as Step 2. Expected: PASS, 4 tests.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/raku/comma/rakuast/AstJson.kt \
        src/test/kotlin/org/raku/comma/rakuast/AstJsonTest.kt
git commit -m "Add the RakuAST viewer wire model and JSON decoding"
```

---

### Task 2: Backend `analyze` plus the service that runs it

The script and the service are mutually dependent — neither is testable alone — so they land together, verified end-to-end against a real Rakudo.

**Files:**
- Create: `src/main/resources/rakuast/rakuast-tool.raku`
- Create: `src/main/java/org/raku/comma/rakuast/RakuAstService.kt`
- Test: `src/test/kotlin/org/raku/comma/rakuast/RakuAstServiceTest.kt`

**Interfaces:**
- Consumes: everything from Task 1.
- Produces: `RakuAstService.getInstance(project): RakuAstService` and `fun analyze(source: String): AnalyzeResult`. Blocking; callers must keep it off the EDT.

**Attribute rules the script must implement** (measured against v2026.08-445; `VarDeclaration::Simple` has 24+ attributes, most of them internal):

- Read each attribute by **binding**, never assigning: `my $raw := try { $a.get_value($node) }`. Assigning into a `Scalar` container defeats the null guard and `.defined`/`.elems`/`.gist` then throw *"elems requires a concrete object (got a VMNull type object instead)"*.
- Skip when `nqp::isnull(nqp::decont($raw))`.
- Skip undefined (type-object) values — this alone takes 24 attributes down to 14.
- Skip this bookkeeping denylist:
  `sunk thunks okifnil sorries worries origin lowered-array-init lowered-to-local initializer-in-method is-parameter attribute-package generics-package conflicting-type unit-package qualified-root original-type`
- `editable` is true when a `set-<name>` method exists on the node.
- Every `display` rendering is individually wrapped in `try` with a `"(unrenderable)"` fallback — `.gist` throws on some values even after the null guard.

- [ ] **Step 1: Write the failing test**

```kotlin
package org.raku.comma.rakuast

import org.raku.comma.CommaFixtureTestCase

class RakuAstServiceTest : CommaFixtureTestCase() {

    private fun service() = RakuAstService.getInstance(project)

    // Structure only. Never assert deparse text: it changes between Rakudo
    // releases (`sub f ($a!)` on 2026.03 became `sub f ($a)` on 2026.08-445).
    fun testAnalyzeReturnsTreeShape() {
        val result = service().analyze("my Int \$x = 41 + 1;")

        assertNull(result.error)
        val root = result.tree!!
        assertEquals("RakuAST::StatementList", root.nodeClass)

        val classes = mutableListOf<String>()
        fun walk(n: AstNode) { classes.add(n.nodeClass); n.children.forEach(::walk) }
        walk(root)

        assertTrue("expected a VarDeclaration::Simple, got $classes",
                   classes.contains("RakuAST::VarDeclaration::Simple"))
        assertTrue("expected an IntLiteral, got $classes",
                   classes.contains("RakuAST::IntLiteral"))
    }

    fun testIntLiteralExposesEditableValue() {
        val root = service().analyze("my \$x = 41;").tree!!
        val lit = findFirst(root, "RakuAST::IntLiteral")!!
        val value = lit.attrs.single { it.name == "value" }
        assertEquals("scalar", value.kind)
        assertTrue("value should be editable", value.editable)
        assertEquals("41", value.display)
    }

    // Spans are snippet-relative and must select the node's own text.
    fun testSpansAreSnippetRelative() {
        val source = "my \$x = 41;"
        val root = service().analyze(source).tree!!
        val lit = findFirst(root, "RakuAST::IntLiteral")!!
        assertEquals("41", source.substring(lit.span.from, lit.span.to))
    }

    fun testBookkeepingAttributesAreHidden() {
        val root = service().analyze("my \$x = 41;").tree!!
        val lit = findFirst(root, "RakuAST::IntLiteral")!!
        val names = lit.attrs.map { it.name }
        for (hidden in listOf("sunk", "thunks", "okifnil", "sorries", "worries", "origin")) {
            assertFalse("$hidden should be hidden, got $names", names.contains(hidden))
        }
    }

    // .AST does compile-time resolution, so this is a normal, expected outcome
    // rather than a crash. The message must survive to the caller.
    fun testUnresolvableCodeReportsErrorNotCrash() {
        val result = service().analyze("class Foo is NoSuchParentType { }")
        assertNull(result.tree)
        assertNotNull("expected a readable error", result.error)
        assertFalse("error should not be empty", result.error!!.isBlank())
    }

    private fun findFirst(node: AstNode, cls: String): AstNode? {
        if (node.nodeClass == cls) return node
        for (c in node.children) findFirst(c, cls)?.let { return it }
        return null
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
eval "$(~/.rakubrew/bin/rakubrew init Zsh)"
rakubrew switch "${RAKUBREW_RAKU_VERSION:-moar-2026.03}"
./gradlew test --rerun --tests "org.raku.comma.rakuast.RakuAstServiceTest"
```
Expected: FAIL — `Unresolved reference: RakuAstService`.

- [ ] **Step 3: Write the backend script**

Create `src/main/resources/rakuast/rakuast-tool.raku`. Copy the vendored JSON block verbatim from `src/main/resources/symbols/raku-module-symbols.raku` — everything between `# ========== JSON CODE FROM JSON::Fast ==========` and `# ========== END OF JSON CODE ==========` — then add:

```raku
use experimental :rakuast;
use nqp;

# ========== JSON CODE FROM JSON::Fast ==========
# (copy verbatim from symbols/raku-module-symbols.raku)
# ========== END OF JSON CODE ==========

my constant @HIDDEN = <
    sunk thunks okifnil sorries worries origin
    lowered-array-init lowered-to-local initializer-in-method is-parameter
    attribute-package generics-package conflicting-type unit-package
    qualified-root original-type
>;

sub attrs-of($node) {
    my %setters = $node.^methods.map(*.name).grep(*.starts-with('set-'))
                       .map({ .substr(4) => True }).Hash;
    my @out;
    for $node.^attributes -> $a {
        my $name = $a.name.substr(2);
        next if @HIDDEN.first($name);

        # Bind, never assign: a Scalar container defeats the null guard and
        # .defined/.elems/.gist then die on VMNull.
        my $raw := try { $a.get_value($node) };
        next if nqp::isnull(nqp::decont($raw));
        next unless (try { $raw.defined }) // False;

        my ($kind, $display);
        if $raw ~~ RakuAST::Node {
            $kind    = 'node';
            $display = (try { $raw.^name ~ " -> '" ~ $raw.DEPARSE.trim ~ "'" }) // '(unrenderable)';
        }
        elsif $raw ~~ Positional {
            $kind    = 'list';
            $display = (try { "[" ~ $raw.elems ~ " items]" }) // '(unrenderable)';
        }
        else {
            $kind    = 'scalar';
            $display = (try { $raw.gist }) // '(unrenderable)';
        }

        @out.push: {
            name     => $name,
            kind     => $kind,
            display  => $display,
            editable => (%setters{$name} ?? True !! False),
        };
    }
    @out
}

sub node-json($node, @path) {
    my @children;
    my $i = 0;
    $node.visit-children(-> $child {
        if $child ~~ RakuAST::Node {
            @children.push: node-json($child, [|@path, $i]);
            $i++;
        }
    });
    my $origin := $node.origin;
    my %span = $origin.defined ?? { from => $origin.from, to => $origin.to }
                               !! { from => 0, to => 0 };
    %(
        class    => $node.^name,
        path     => @path,
        span     => %span,
        attrs    => attrs-of($node),
        children => @children,
    )
}

sub fail-with($message) {
    say to-json({ error => $message });
    exit 0;
}

# Errors must reach the caller as JSON on stdout: RakuCommandLine reads stdout
# only and discards everything on a non-zero exit.
CATCH { default { fail-with(.message // .gist); } }

my $verb = @*ARGS[0] // fail-with('No verb given.');

if $verb eq 'analyze' {
    my $source = @*ARGS[1].IO.slurp;
    say to-json({ tree => node-json($source.AST, []) });
}
else {
    fail-with("Unknown verb '$verb'.");
}
```

- [ ] **Step 4: Write the service**

```kotlin
package org.raku.comma.rakuast

import com.intellij.execution.ExecutionException
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.raku.comma.utils.RakuCommandLine
import org.raku.comma.utils.RakuUtils
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

@Service(Service.Level.PROJECT)
class RakuAstService(private val project: Project) {

    fun analyze(source: String): AnalyzeResult {
        val sourceFile = writeTemp("rakuast-source", source)
            ?: return AnalyzeResult(error = "Could not write a temporary file for the snippet.")
        return try {
            AstJson.decodeAnalyze(run(listOf("analyze", sourceFile.absolutePath)))
        } finally {
            sourceFile.delete()
        }
    }

    /** Blocking. Callers must keep this off the EDT. */
    private fun run(args: List<String>): String {
        val script = RakuUtils.getResourceAsFile(SCRIPT)
            ?: return errorJson("Could not extract $SCRIPT from the plugin.")
        return try {
            val cmd = RakuCommandLine(project)
            cmd.setWorkDirectory(project.basePath)
            cmd.addParameter(script.path)
            cmd.addParameters(args)
            // executeAndRead deletes the script file and returns an empty list
            // on a non-zero exit, so an empty result means "no usable output".
            cmd.executeAndRead(script).joinToString("\n")
        } catch (e: ExecutionException) {
            // Thrown as "No SDK for project" when the project SDK is unset.
            LOG.info("RakuAST backend could not start", e)
            errorJson(e.message ?: "Could not start Raku for this project.")
        }
    }

    private fun writeTemp(prefix: String, content: String): File? = try {
        val f = Files.createTempFile(prefix, ".raku").toFile()
        f.writeText(content, StandardCharsets.UTF_8)
        f
    } catch (e: Exception) {
        LOG.warn("Could not write RakuAST temp file", e)
        null
    }

    // Built through the serializer so quotes and backslashes in the message
    // cannot produce malformed JSON. Both result types carry `error`, and the
    // decoder ignores unknown keys, so one shape serves both.
    private fun errorJson(message: String) =
        kotlinx.serialization.json.Json.encodeToString(AnalyzeResult(error = message))

    companion object {
        private val LOG = Logger.getInstance(RakuAstService::class.java)
        private const val SCRIPT = "rakuast/rakuast-tool.raku"

        @JvmStatic
        fun getInstance(project: Project): RakuAstService = project.service()
    }
}
```

- [ ] **Step 5: Run tests to verify they pass**

Run the Step 2 command. Expected: PASS, 5 tests.

If `testUnresolvableCodeReportsErrorNotCrash` fails with an empty error, the `CATCH` is not catching a *compile*-time failure of `.AST`. Wrap the `.AST` call itself:
```raku
my $ast = (try { $source.AST }) // fail-with("Could not parse the selection: " ~ ($! // 'unknown error'));
```

- [ ] **Step 6: Confirm the tests really ran**

```bash
raku -e 'my $n = 0; for "build/test-results/test".IO.dir(test => *.ends-with(".xml")) -> $p {
    $n += +$0 if $p.slurp.substr(0, 600) ~~ / "tests=\"" (\d+) "\"" / }; say "$n tests"'
```

- [ ] **Step 7: Commit**

```bash
git add src/main/resources/rakuast/rakuast-tool.raku \
        src/main/java/org/raku/comma/rakuast/RakuAstService.kt \
        src/test/kotlin/org/raku/comma/rakuast/RakuAstServiceTest.kt
git commit -m "Analyze a Raku snippet into a RakuAST tree"
```

---

### Task 3: Backend `edit` plus `RakuAstService.edit`

**Files:**
- Modify: `src/main/resources/rakuast/rakuast-tool.raku` (add the `edit` branch)
- Modify: `src/main/java/org/raku/comma/rakuast/RakuAstService.kt` (add `edit`)
- Test: `src/test/kotlin/org/raku/comma/rakuast/RakuAstEditTest.kt`

**Interfaces:**
- Consumes: Task 2's script and service.
- Produces: `fun RakuAstService.edit(source: String, path: List<Int>, attr: String, value: String, valueKind: String): EditResult`, where `valueKind` is `"scalar"` or `"node"`.

- [ ] **Step 1: Write the failing test**

```kotlin
package org.raku.comma.rakuast

import org.raku.comma.CommaFixtureTestCase

class RakuAstEditTest : CommaFixtureTestCase() {

    private fun service() = RakuAstService.getInstance(project)

    private fun pathOf(root: AstNode, cls: String): List<Int> {
        if (root.nodeClass == cls) return root.path
        for (c in root.children) {
            val p = runCatching { pathOf(c, cls) }.getOrNull()
            if (p != null) return p
        }
        error("no $cls in tree")
    }

    fun testEditScalarReturnsNarrowReplacement() {
        val source = "my \$x = 41;"
        val path = pathOf(service().analyze(source).tree!!, "RakuAST::IntLiteral")

        val result = service().edit(source, path, "value", "99", "scalar")

        assertNull(result.error)
        assertEquals("99", result.text)
        // The span must cover only the literal, never the whole statement.
        assertEquals("41", source.substring(result.span!!.from, result.span!!.to))
        assertNotNull("edit should return a refreshed tree", result.tree)
    }

    fun testEditNodeValuedAttributeParsesSnippet() {
        val source = "my Int \$x = 41;"
        val path = pathOf(service().analyze(source).tree!!, "RakuAST::VarDeclaration::Simple")

        val result = service().edit(source, path, "type", "Str", "node")

        assertNull(result.error)
        assertNotNull(result.text)
    }

    // Must fail before mutating anything.
    fun testInvalidSnippetIsRejected() {
        val source = "my Int \$x = 41;"
        val path = pathOf(service().analyze(source).tree!!, "RakuAST::VarDeclaration::Simple")

        val result = service().edit(source, path, "type", "this is not raku ===", "node")

        assertNotNull("expected a readable error", result.error)
        assertNull("nothing should be returned to write", result.text)
    }

    fun testUnknownPathIsRejected() {
        val result = service().edit("my \$x = 41;", listOf(99, 99), "value", "1", "scalar")
        assertNotNull(result.error)
        assertNull(result.text)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
eval "$(~/.rakubrew/bin/rakubrew init Zsh)"
rakubrew switch "${RAKUBREW_RAKU_VERSION:-moar-2026.03}"
./gradlew test --rerun --tests "org.raku.comma.rakuast.RakuAstEditTest"
```
Expected: FAIL — `Unresolved reference: edit`.

- [ ] **Step 3: Add the `edit` branch to the script**

Insert before the final `else { fail-with(...) }`:

```raku
sub node-at($root, @path) {
    my $current = $root;
    for @path -> $index {
        my @kids;
        $current.visit-children(-> $c { @kids.push($c) if $c ~~ RakuAST::Node });
        fail-with("The selected node is no longer present.") unless @kids[$index].defined;
        $current = @kids[$index];
    }
    $current
}

if $verb eq 'edit' {
    my $source    = @*ARGS[1].IO.slurp;
    my @path      = @*ARGS[2] ?? @*ARGS[2].split(',').map(*.Int) !! ();
    my $attr      = @*ARGS[3];
    my $value     = @*ARGS[4].IO.slurp;
    my $kind      = @*ARGS[5];

    my $ast    = (try { $source.AST }) // fail-with('Could not parse the selection.');
    my $target = node-at($ast, @path);

    # Validate before mutating, so a bad snippet never reaches the file.
    my $new-value = do if $kind eq 'node' {
        my $parsed = (try { $value.AST }) // fail-with("Could not parse '$value' as Raku.");
        # A bare expression arrives wrapped in StatementList/Statement::Expression.
        my $inner = $parsed;
        while $inner ~~ RakuAST::StatementList | RakuAST::Statement::Expression {
            my @kids;
            $inner.visit-children(-> $c { @kids.push($c) if $c ~~ RakuAST::Node });
            last unless @kids;
            $inner = @kids[0];
        }
        $inner
    }
    else {
        # Scalars: prefer the node's current type. Int stays Int, Str stays Str.
        $value ~~ /^ '-'? \d+ $/ ?? $value.Int !! $value
    };

    my $setter = 'set-' ~ $attr;
    if $target.^can($setter) {
        try { $target."$setter"($new-value) } // fail-with("Could not set '$attr'.");
    }
    else {
        try { nqp::bindattr(nqp::decont($target), $target.WHAT, '$!' ~ $attr, nqp::decont($new-value)) }
            // fail-with("'$attr' cannot be set on {$target.^name}.");
    }

    my $text = (try { $target.DEPARSE }) // fail-with('The edit produced source that could not be rendered.');

    # Sanity check: never hand back source that cannot be re-parsed.
    fail-with('The edit produced invalid Raku and was not applied.')
        unless (try { $text.AST; True }) // False;

    my $origin := $target.origin;
    say to-json({
        text => $text,
        span => ($origin.defined ?? { from => $origin.from, to => $origin.to }
                                 !! { from => 0, to => 0 }),
        tree => node-json($ast, []),
    });
}
```

- [ ] **Step 4: Add `edit` to the service**

```kotlin
    fun edit(
        source: String,
        path: List<Int>,
        attr: String,
        value: String,
        valueKind: String,
    ): EditResult {
        val sourceFile = writeTemp("rakuast-source", source)
            ?: return EditResult(error = "Could not write a temporary file for the snippet.")
        val valueFile = writeTemp("rakuast-value", value)
            ?: run {
                sourceFile.delete()
                return EditResult(error = "Could not write a temporary file for the new value.")
            }
        return try {
            AstJson.decodeEdit(
                run(listOf(
                    "edit",
                    sourceFile.absolutePath,
                    path.joinToString(","),
                    attr,
                    valueFile.absolutePath,
                    valueKind,
                ))
            )
        } finally {
            sourceFile.delete()
            valueFile.delete()
        }
    }
```

- [ ] **Step 5: Run tests to verify they pass**

Run the Step 2 command. Expected: PASS, 4 tests. Then re-run Task 2's tests to confirm no regression:
```bash
./gradlew test --rerun --tests "org.raku.comma.rakuast.*"
```

- [ ] **Step 6: Commit**

```bash
git add src/main/resources/rakuast/rakuast-tool.raku \
        src/main/java/org/raku/comma/rakuast/RakuAstService.kt \
        src/test/kotlin/org/raku/comma/rakuast/RakuAstEditTest.kt
git commit -m "Apply attribute edits to a RakuAST node and deparse just that node"
```

---

### Task 4: Apply an edit to the document

The only unit that writes. Mirrors `FatarrowSimplificationFix`, which is the established replacement pattern in this codebase.

**Files:**
- Create: `src/main/java/org/raku/comma/rakuast/RakuAstEditApplier.kt`
- Test: `src/test/kotlin/org/raku/comma/rakuast/RakuAstEditApplierTest.kt`

**Interfaces:**
- Consumes: `EditResult` and `AstSpan` from Task 1.
- Produces: `RakuAstEditApplier.apply(project: Project, editor: Editor, baseOffset: Int, result: EditResult): Boolean` — true when the document was changed. Must be called on the EDT; it opens its own write action.

**Deviation from the design doc — please confirm.** The doc says the applier "resolves the enclosing PSI element" and replaces through it. This task writes via document offsets plus a `RangeMarker`, without consulting PSI. The reasoning: undo comes from `WriteCommandAction`, not from PSI; the edit range is already authoritative because it comes from the node's own `origin`; and `FatarrowSimplificationFix` uses PSI only to *obtain* a `TextRange` it then hands straight to `document.createRangeMarker`, which is the step that actually matters here.

What PSI correlation would add is boundary snapping — useful because origin spans can be surprising (`my Int $x = 41 + 1` reports its declaration span as just `$x = 41 + 1`). That matters for editing a declaration as a whole; it does not matter for the leaf edits this plan delivers. If you want PSI correlation in v1, say so and it becomes a step here: resolve via `PsiDocumentManager.getInstance(project).getPsiFile(document)` then `findElementAt(start)`, and refuse the edit when no element covers the range.

- [ ] **Step 1: Write the failing test**

```kotlin
package org.raku.comma.rakuast

import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

class RakuAstEditApplierTest : CommaFixtureTestCase() {

    fun testReplacesOnlyTheNodeSpan() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say 1;\nmy \$x = 41;\n")
        // "41" sits at offset 16 in the file; the snippet began at offset 7.
        val result = EditResult(text = "99", span = AstSpan(from = 8, to = 10))

        val changed = RakuAstEditApplier.apply(project, myFixture.editor, 7, result)

        assertTrue(changed)
        assertEquals("say 1;\nmy \$x = 99;\n", myFixture.editor.document.text)
    }

    // Everything outside the edited node must survive byte-for-byte, including
    // comments, which DEPARSE cannot reproduce.
    fun testCommentsAndFormattingOutsideTheNodeSurvive() {
        val original = "my  \$x   =   41;   # keep me\n# and me\n"
        myFixture.configureByText(RakuScriptFileType.INSTANCE, original)
        val result = EditResult(text = "99", span = AstSpan(from = 13, to = 15))

        RakuAstEditApplier.apply(project, myFixture.editor, 0, result)

        val text = myFixture.editor.document.text
        assertTrue("trailing comment lost: $text", text.contains("# keep me"))
        assertTrue("standalone comment lost: $text", text.contains("# and me"))
        assertTrue("spacing lost: $text", text.contains("my  \$x   =   "))
        assertTrue("edit not applied: $text", text.contains("99"))
    }

    fun testRefusesResultCarryingAnError() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 41;")
        val before = myFixture.editor.document.text

        val changed = RakuAstEditApplier.apply(
            project, myFixture.editor, 0, EditResult(error = "nope"))

        assertFalse(changed)
        assertEquals(before, myFixture.editor.document.text)
    }

    fun testRefusesSpanOutsideDocument() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 41;")
        val before = myFixture.editor.document.text

        val changed = RakuAstEditApplier.apply(
            project, myFixture.editor, 9000, EditResult(text = "99", span = AstSpan(0, 2)))

        assertFalse(changed)
        assertEquals(before, myFixture.editor.document.text)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
eval "$(~/.rakubrew/bin/rakubrew init Zsh)"
rakubrew switch "${RAKUBREW_RAKU_VERSION:-moar-2026.03}"
./gradlew test --rerun --tests "org.raku.comma.rakuast.RakuAstEditApplierTest"
```
Expected: FAIL — `Unresolved reference: RakuAstEditApplier`.

- [ ] **Step 3: Write minimal implementation**

```kotlin
package org.raku.comma.rakuast

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

object RakuAstEditApplier {

    /**
     * Replaces exactly the edited node's span. Anything wider would delete
     * ordinary `#` comments, which have no RakuAST node and cannot be restored
     * by deparsing.
     */
    fun apply(project: Project, editor: Editor, baseOffset: Int, result: EditResult): Boolean {
        val text = result.text ?: return false
        val span = result.span ?: return false
        if (result.error != null) return false

        val document = editor.document
        val start = baseOffset + span.from
        val end = baseOffset + span.to
        if (start < 0 || end > document.textLength || start > end) return false

        // A RangeMarker keeps the target valid if the document shifts under us.
        val marker = document.createRangeMarker(start, end)
        try {
            WriteCommandAction.runWriteCommandAction(project, "Apply RakuAST Edit", null, {
                if (marker.isValid) {
                    document.replaceString(marker.startOffset, marker.endOffset, text)
                }
            })
        } finally {
            marker.dispose()
        }
        return true
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run the Step 2 command. Expected: PASS, 4 tests.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/raku/comma/rakuast/RakuAstEditApplier.kt \
        src/test/kotlin/org/raku/comma/rakuast/RakuAstEditApplierTest.kt
git commit -m "Write RakuAST edits back through a RangeMarker write action"
```

---

### Task 5: Tool window, panel, and action

**Files:**
- Create: `src/main/java/org/raku/comma/rakuast/RakuAstViewerPanel.kt`
- Create: `src/main/java/org/raku/comma/rakuast/RakuAstViewerFactory.kt`
- Create: `src/main/java/org/raku/comma/rakuast/AnalyzeSelectionAction.kt`
- Modify: `src/main/resources/META-INF/plugin.xml:319` (beside the existing `Raku Grammar Preview` registration)
- Modify: `src/main/resources/META-INF/actions.xml`
- Test: `src/test/kotlin/org/raku/comma/rakuast/RakuAstViewerPanelTest.kt`

**Interfaces:**
- Consumes: `RakuAstService`, `RakuAstEditApplier`, and the Task 1 model.
- Produces: `RakuAstViewerPanel(project: Project)` with `fun showAnalysis(editor: Editor, baseOffset: Int, snippet: String, result: AnalyzeResult)`, `fun statusText(): String`, and `fun nodeCount(): Int` (the last two exist so the panel's state is assertable without inspecting Swing internals). Also `RakuAstViewerFactory.TOOL_WINDOW_ID: String` and `RakuAstViewerFactory.findPanel(project): RakuAstViewerPanel?`, used by the action.

- [ ] **Step 1: Write the failing test**

Panels are awkward to assert on, so test the state machine rather than the pixels.

```kotlin
package org.raku.comma.rakuast

import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

class RakuAstViewerPanelTest : CommaFixtureTestCase() {

    fun testShowsErrorTextWhenAnalysisFailed() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 1;")
        val panel = RakuAstViewerPanel(project)

        panel.showAnalysis(myFixture.editor, 0, "my \$x = 1;",
                           AnalyzeResult(error = "Invalid typename 'Foo'"))

        assertTrue(panel.statusText().contains("Invalid typename 'Foo'"))
        assertEquals(0, panel.nodeCount())
    }

    fun testPopulatesTreeOnSuccess() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 1;")
        val panel = RakuAstViewerPanel(project)
        val tree = AstNode(
            nodeClass = "RakuAST::StatementList",
            path = emptyList(),
            span = AstSpan(0, 10),
            children = listOf(AstNode("RakuAST::IntLiteral", listOf(0), AstSpan(8, 9))),
        )

        panel.showAnalysis(myFixture.editor, 0, "my \$x = 1;", AnalyzeResult(tree = tree))

        assertEquals(2, panel.nodeCount())
        assertTrue(panel.statusText().isBlank())
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
eval "$(~/.rakubrew/bin/rakubrew init Zsh)"
rakubrew switch "${RAKUBREW_RAKU_VERSION:-moar-2026.03}"
./gradlew test --rerun --tests "org.raku.comma.rakuast.RakuAstViewerPanelTest"
```
Expected: FAIL — `Unresolved reference: RakuAstViewerPanel`.

- [ ] **Step 3: Write the panel**

Hand-rolled Swing, matching `RakuGrammarPreviewer` (this codebase uses Kotlin UI DSL for dialogs only).

```kotlin
package org.raku.comma.rakuast

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel
import javax.swing.JTable
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class RakuAstViewerPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val status = JBLabel("")
    private val treeRoot = DefaultMutableTreeNode("(nothing analyzed)")
    private val treeModel = DefaultTreeModel(treeRoot)
    private val tree = Tree(treeModel)
    private val attrModel = DefaultTableModel(arrayOf("Attribute", "Value"), 0)
    private val attrTable = JTable(attrModel)

    private var currentEditor: Editor? = null
    private var baseOffset: Int = 0
    private var snippet: String = ""
    private var nodes = 0

    init {
        val splitter = JBSplitter(true, 0.6f)
        splitter.firstComponent = JBScrollPane(tree)
        splitter.secondComponent = JBScrollPane(attrTable)
        add(status, BorderLayout.NORTH)
        add(splitter, BorderLayout.CENTER)

        tree.addTreeSelectionListener {
            val selected = tree.lastSelectedPathComponent as? DefaultMutableTreeNode
            val node = selected?.userObject as? AstNode ?: return@addTreeSelectionListener
            showAttributes(node)
            highlight(node)
        }
    }

    fun showAnalysis(editor: Editor, baseOffset: Int, snippet: String, result: AnalyzeResult) {
        this.currentEditor = editor
        this.baseOffset = baseOffset
        this.snippet = snippet
        this.nodes = 0

        treeRoot.removeAllChildren()
        attrModel.rowCount = 0

        val error = result.error
        val tree = result.tree
        if (error != null || tree == null) {
            status.text = error ?: "The Raku backend returned no tree."
            treeRoot.userObject = "(no tree)"
        } else {
            status.text = ""
            treeRoot.userObject = tree
            addChildren(treeRoot, tree)
            nodes = countNodes(tree)
        }
        treeModel.reload()
    }

    fun statusText(): String = status.text

    fun nodeCount(): Int = nodes

    private fun countNodes(node: AstNode): Int =
        1 + node.children.sumOf { countNodes(it) }

    private fun addChildren(parent: DefaultMutableTreeNode, node: AstNode) {
        for (child in node.children) {
            val swingChild = DefaultMutableTreeNode(child)
            parent.add(swingChild)
            addChildren(swingChild, child)
        }
    }

    private fun showAttributes(node: AstNode) {
        attrModel.rowCount = 0
        for (attr in node.attrs) attrModel.addRow(arrayOf(attr.name, attr.display))
    }

    private fun highlight(node: AstNode) {
        val editor = currentEditor ?: return
        val start = baseOffset + node.span.from
        val end = baseOffset + node.span.to
        if (start < 0 || end > editor.document.textLength || start > end) return
        editor.selectionModel.setSelection(start, end)
        editor.caretModel.moveToOffset(start)
    }
}
```

`AstNode` renders in the tree via `toString()`. Add to `AstJson.kt`:

```kotlin
// (inside AstNode)
override fun toString(): String = nodeClass.removePrefix("RakuAST::")
```

- [ ] **Step 4: Run tests to verify they pass**

Run the Step 2 command. Expected: PASS, 2 tests.

- [ ] **Step 5: Write the factory and action**

```kotlin
package org.raku.comma.rakuast

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class RakuAstViewerFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = RakuAstViewerPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        content.putUserData(PANEL_KEY, panel)
        toolWindow.contentManager.addContent(content)
    }

    companion object {
        val PANEL_KEY = com.intellij.openapi.util.Key.create<RakuAstViewerPanel>("RakuAstViewerPanel")
        const val TOOL_WINDOW_ID = "RakuAST Viewer"

        fun findPanel(project: Project): RakuAstViewerPanel? =
            com.intellij.openapi.wm.ToolWindowManager.getInstance(project)
                .getToolWindow(TOOL_WINDOW_ID)
                ?.contentManager?.contents
                ?.firstNotNullOfOrNull { it.getUserData(PANEL_KEY) }
    }
}
```

```kotlin
package org.raku.comma.rakuast

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.wm.ToolWindowManager

class AnalyzeSelectionAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val selection = editor.selectionModel
        val snippet = selection.selectedText ?: return
        val baseOffset = selection.selectionStart

        ToolWindowManager.getInstance(project)
            .getToolWindow(RakuAstViewerFactory.TOOL_WINDOW_ID)?.activate(null, true)

        // The subprocess costs ~310ms; never run it on the EDT.
        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Analyzing RakuAST", true) {
                override fun run(indicator: ProgressIndicator) {
                    val result = RakuAstService.getInstance(project).analyze(snippet)
                    ApplicationManager.getApplication().invokeLater {
                        RakuAstViewerFactory.findPanel(project)
                            ?.showAnalysis(editor, baseOffset, snippet, result)
                    }
                }
            })
    }

    override fun update(event: AnActionEvent) {
        val editor = event.getData(CommonDataKeys.EDITOR)
        event.presentation.isEnabled =
            event.project != null && editor?.selectionModel?.hasSelection() == true
    }

    override fun getActionUpdateThread() =
        com.intellij.openapi.actionSystem.ActionUpdateThread.EDT
}
```

- [ ] **Step 6: Register in plugin.xml and actions.xml**

In `src/main/resources/META-INF/plugin.xml`, directly after the existing `Raku Grammar Preview` entry (line ~319):

```xml
    <toolWindow id="RakuAST Viewer" anchor="right"
                factoryClass="org.raku.comma.rakuast.RakuAstViewerFactory"
                secondary="true" canCloseContents="false"/>
```

In `src/main/resources/META-INF/actions.xml`, inside `<actions>`:

```xml
    <action id="org.raku.comma.rakuast.AnalyzeSelectionAction"
            class="org.raku.comma.rakuast.AnalyzeSelectionAction"
            text="Analyze Selection as RakuAST"
            description="Show the RakuAST tree for the selected Raku code">
        <add-to-group group-id="EditorPopupMenu" anchor="last"/>
    </action>
```

- [ ] **Step 7: Run the full suite**

```bash
eval "$(~/.rakubrew/bin/rakubrew init Zsh)"
rakubrew switch "${RAKUBREW_RAKU_VERSION:-moar-2026.03}"
./gradlew test --rerun
```
Expected: BUILD SUCCESSFUL, no regressions. Then confirm the count:
```bash
raku -e 'my $n = 0; for "build/test-results/test".IO.dir(test => *.ends-with(".xml")) -> $p {
    $n += +$0 if $p.slurp.substr(0, 600) ~~ / "tests=\"" (\d+) "\"" / }; say "$n tests"'
```

- [ ] **Step 8: Verify by hand in a sandbox IDE**

```bash
eval "$(~/.rakubrew/bin/rakubrew init Zsh)"
rakubrew switch "${RAKUBREW_RAKU_VERSION:-moar-2026.03}"
./gradlew runIde
```
In the sandbox IDE: open a Raku file, select `my $x = 41 + 1;`, right-click → *Analyze Selection as RakuAST*. Confirm the tree appears, selecting `IntLiteral` highlights `41` in the editor, and the attribute table shows `value` / `41`.

- [ ] **Step 9: Commit**

```bash
git add src/main/java/org/raku/comma/rakuast/RakuAstViewerPanel.kt \
        src/main/java/org/raku/comma/rakuast/RakuAstViewerFactory.kt \
        src/main/java/org/raku/comma/rakuast/AnalyzeSelectionAction.kt \
        src/main/java/org/raku/comma/rakuast/AstJson.kt \
        src/main/resources/META-INF/plugin.xml \
        src/main/resources/META-INF/actions.xml \
        src/test/kotlin/org/raku/comma/rakuast/RakuAstViewerPanelTest.kt
git commit -m "Add the RakuAST viewer tool window and analyze action"
```

---

## Deferred to a follow-up plan

These are in the design doc but deliberately not in this plan, which delivers a working read-only-plus-scalar-edit viewer first:

- **Inline attribute editing in the table.** Task 3 and Task 4 provide the whole backend and writer; what is missing is the cell editor wiring `attrTable` → `RakuAstService.edit` → `RakuAstEditApplier`, plus the modification-stamp staleness guard (re-analyze before applying; refuse only if the path no longer resolves).
- **The bookkeeping toggle** that reveals hidden and null-valued attributes.
- **RakuDoc editing** via `WHY` / `leading` / `trailing`.
- **List-valued attribute editing** (insert/remove/reorder), read-only in v1.
