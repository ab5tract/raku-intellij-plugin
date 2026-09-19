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
