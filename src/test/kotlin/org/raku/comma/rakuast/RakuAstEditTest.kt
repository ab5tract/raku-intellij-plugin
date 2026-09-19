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

    // Replacing a whole node-valued attribute: a statement's expression is a
    // Stub (`!!!`), and gets re-parsed and re-assigned as `++$`.
    //
    // This failed before, and not because the setter could not do it. The
    // script checked `try { ... } // fail-with(...)`, and `//` is defined-or,
    // so set-expression returning Nil read as failure even though the change
    // had already been applied.
    fun testReplacingAStatementsExpressionNode() {
        val source = "!!!;"
        val path = pathOf(service().analyze(source).tree!!, "RakuAST::Statement::Expression")

        val result = service().edit(source, path, "expression", "++\$", "node")

        assertNull(result.error)
        assertEquals("++\$", result.text)
        // The span covers the statement being replaced, not the whole file.
        assertEquals("!!!", source.substring(result.span!!.from, result.span!!.to))

        val expression = findFirst(result.tree!!, "RakuAST::Statement::Expression")!!
            .attrs.single { it.name == "expression" }
        assertTrue("the expression should now be the parsed ++\$, got: ${expression.display}",
                   expression.display.contains("ApplyPrefix"))
    }

    private fun findFirst(node: AstNode, cls: String): AstNode? {
        if (node.nodeClass == cls) return node
        for (child in node.children) findFirst(child, cls)?.let { return it }
        return null
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

    // Regression guard: scalar coercion must key off the attribute's own
    // current type on the node, not off the shape of the incoming string.
    // A numeric-looking string edited onto a Str-typed attribute must stay
    // a Str, not silently become an Int. The only place this surfaces is
    // in the deparsed/re-parsed structure (JSON `display` uses `.gist`,
    // which reads identically for Str "42" and Int 42), so this asserts on
    // the class of the re-parsed result rather than on deparse formatting.
    fun testEditScalarPreservesAttributeType() {
        val source = "my \$x = \"hello\";"
        val path = pathOf(service().analyze(source).tree!!, "RakuAST::StrLiteral")

        val result = service().edit(source, path, "value", "42", "scalar")

        assertNull(result.error)
        assertNotNull(result.text)

        val reparsed = service().analyze(result.text!!).tree!!
        val classes = collectClasses(reparsed)
        assertTrue(
            "expected a RakuAST::StrLiteral when re-parsing '${result.text}', got $classes",
            classes.contains("RakuAST::StrLiteral")
        )
        assertFalse(
            "the edited value must not have become a bare Int literal, got $classes",
            classes.contains("RakuAST::IntLiteral")
        )
    }

    private fun collectClasses(node: AstNode): List<String> {
        val out = mutableListOf(node.nodeClass)
        node.children.forEach { out += collectClasses(it) }
        return out
    }
}
