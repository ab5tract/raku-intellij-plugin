package org.raku.comma.rakuast

import org.raku.comma.CommaFixtureTestCase
import java.nio.file.Files

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

    // Rakudo's one-line node summary. Asserts the parts that carry meaning --
    // the class name and the identity marker -- rather than exact spacing or
    // the source excerpt, both of which are Rakudo's to change.
    fun testNodesCarryRakudoSummary() {
        val root = service().analyze("my \$x = 41 + 1;").tree!!

        val infix = findFirst(root, "RakuAST::Infix")!!
        val summary = infix.summary
        assertNotNull("Infix should carry a summary", summary)
        assertTrue("summary should name the class, got: $summary",
                   summary!!.startsWith("Infix"))
        assertTrue("summary should carry the operator identity, got: $summary",
                   summary.contains("+"))

        // The root is a different shape -- no identity marker -- so this also
        // proves the summary is not just the class name echoed back.
        val rootSummary = root.summary
        assertNotNull("root should carry a summary", rootSummary)
        assertTrue("root summary should name StatementList, got: $rootSummary",
                   rootSummary!!.startsWith("StatementList"))
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
        val litSpan = lit.span!!
        assertEquals("41", source.substring(litSpan.from, litSpan.to))
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

    // Regression guard: the original five tests here all used variable
    // declarations only, which let a MoarVM segfault on other node shapes
    // (sub/class/for/regex declarations, each via a different corrupted
    // attribute) ship green. `sub`/`class` walk many more RakuAST node
    // types and their internal attributes than a bare `my $x = ...;` does.
    fun testAnalyzeSubDeclarationSucceeds() {
        val result = service().analyze("sub f(\$a, \$b) { \$a + \$b }")
        assertNull(result.error)
        val root = result.tree!!
        val classes = mutableListOf<String>()
        fun walk(n: AstNode) { classes.add(n.nodeClass); n.children.forEach(::walk) }
        walk(root)
        assertTrue("expected a RakuAST::Sub, got $classes", classes.contains("RakuAST::Sub"))
    }

    fun testAnalyzeClassDeclarationSucceeds() {
        val result = service().analyze("class Foo { has \$.bar; method baz() { 42 } }")
        assertNull(result.error)
        val root = result.tree!!
        val classes = mutableListOf<String>()
        fun walk(n: AstNode) { classes.add(n.nodeClass); n.children.forEach(::walk) }
        walk(root)
        assertTrue("expected a RakuAST::Class, got $classes", classes.contains("RakuAST::Class"))
        assertTrue("expected a RakuAST::Method, got $classes", classes.contains("RakuAST::Method"))
    }

    // Regression guard: a node with an undefined .origin (here, the implicit
    // RakuAST::Type::Setting on an untyped parameter) must decode to a null
    // span, never to a real-looking (0, 0). Before the fix, clicking this
    // node in the tree would silently clear the selection and jump the
    // caret to baseOffset; once the (currently unwired) edit UI ships, an
    // indistinguishable (0,0) would let apply() insert text at baseOffset
    // instead of refusing.
    fun testUndefinedOriginProducesNullSpan() {
        val root = service().analyze("sub f(\$a) { \$a * 2 }").tree!!
        val setting = findFirst(root, "RakuAST::Type::Setting")
        assertNotNull("expected a RakuAST::Type::Setting node in the tree", setting)
        assertNull("a node with no .origin must report a null span, not (0,0)", setting!!.span)
    }

    // Regression guard for run()'s stdout handling: code that runs at compile
    // time can print stray stdout before the wire-format JSON line, which
    // .joinToString("\n")-ing every stdout line would splice into the JSON
    // and corrupt it. run() now takes the last non-blank stdout line instead.
    //
    // A `BEGIN { say ... }` block is the obvious way to reproduce this, but
    // on the pinned Rakudo (moar-2026.03) it hits an unrelated backend
    // limitation: `.AST` on ANY snippet containing a BEGIN or CHECK phaser
    // fails outright with "Unknown compilation input 'qast'", regardless of
    // whether the phaser prints anything at all. Confirmed directly against
    // the interpreter: `BEGIN { 1 }` (no output) fails identically to
    // `BEGIN { say "noise" }`. So a BEGIN-based test cannot exercise this fix
    // on this Rakudo version -- it never gets past `.AST` to reach the stdout
    // handling at all.
    //
    // A module that prints when it is loaded reproduces the same "stray
    // stdout before the JSON line" scenario -- `use` also runs at compile
    // time -- without going through BEGIN/CHECK, and does not trip the qast
    // bug (also confirmed directly against the interpreter).
    fun testModuleLoadStdoutNoiseDoesNotCorruptOutput() {
        val libDir = Files.createTempDirectory("rakuast-noisy-lib")
        val moduleFile = libDir.resolve("NoisyModule.rakumod").toFile()
        moduleFile.writeText("say \"noise-from-module-load\";")
        try {
            val snippet = "use lib \"${libDir}\"; use NoisyModule; my \$x = 1;"
            val result = service().analyze(snippet)
            assertNull("expected no error, got: ${result.error}", result.error)
            assertNotNull(result.tree)
        } finally {
            // Rakudo may leave a .precomp cache directory under libDir after
            // loading NoisyModule, so clean up recursively rather than
            // assuming libDir only ever contains moduleFile.
            libDir.toFile().deleteRecursively()
        }
    }

    private fun findFirst(node: AstNode, cls: String): AstNode? {
        if (node.nodeClass == cls) return node
        for (c in node.children) findFirst(c, cls)?.let { return it }
        return null
    }
}
