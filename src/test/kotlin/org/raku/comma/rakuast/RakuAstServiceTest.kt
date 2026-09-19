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
