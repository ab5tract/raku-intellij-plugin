package org.raku.comma.rakuast

import org.raku.comma.CommaFixtureTestCase

/**
 * The metadata that makes drag-and-drop possible: which slot each tree
 * position occupies, what that slot is declared to hold, and what each node
 * conforms to.
 */
class RakuAstMetadataTest : CommaFixtureTestCase() {

    private fun analyze(source: String) = RakuAstService.getInstance(project).analyze(source)

    private fun find(node: AstNode, cls: String): AstNode? {
        if (node.nodeClass == cls) return node
        for (child in node.children) find(child, cls)?.let { return it }
        return null
    }

    // A tree position is only writable if it can be named as a slot. The
    // children come from visit-children, which knows nothing about attributes,
    // so this mapping is recovered by identity against the parent's slots.
    fun testChildrenReportTheSlotThatHoldsThem() {
        val tree = analyze("my \$x = 1; say \$x;").tree!!

        val statement = tree.children[1]
        assertEquals("statements", statement.viaAttr)
        assertEquals("the second statement should know its list position", 1, statement.viaIndex)

        val call = statement.children.single()
        assertEquals("expression", call.viaAttr)
        assertNull("a non-list slot has no index", call.viaIndex)
    }

    // The root hangs off nothing.
    fun testRootHasNoSlot() {
        val tree = analyze("my \$x = 1;").tree!!
        assertNull(tree.viaAttr)
        assertNull(tree.viaIndex)
    }

    // The declared type is what a drop is checked against, and it is quite
    // distinct from `kind`, which only describes the current contents.
    fun testNodeSlotsReportTheirDeclaredType() {
        val tree = analyze("my Int \$x = 1;").tree!!
        val declaration = find(tree, "RakuAST::VarDeclaration::Simple")!!

        assertEquals("RakuAST::Name",
                     declaration.attrs.single { it.name == "desigilname" }.type)
        assertEquals("RakuAST::Initializer",
                     declaration.attrs.single { it.name == "initializer" }.type)
    }

    fun testConformanceGatesDropsByDeclaredType() {
        val result = analyze("my \$x = 1;")

        // An IntLiteral is an Expression, so it may go in an expression slot.
        assertTrue(result.accepts("RakuAST::IntLiteral", "RakuAST::Expression"))
        // It is not a Name, and must not be droppable into one.
        assertFalse(result.accepts("RakuAST::IntLiteral", "RakuAST::Name"))
    }

    // Mu and List do not constrain: Mu is what bookkeeping slots declare, and
    // a list slot is declared List, which says nothing about its elements.
    // Both defer to the compile check rather than refusing up front.
    fun testUnconstrainedSlotsAcceptAnything() {
        val result = analyze("my \$x = 1;")

        assertTrue(result.accepts("RakuAST::IntLiteral", "Mu"))
        assertTrue(result.accepts("RakuAST::IntLiteral", "List"))
    }

    // Refusing on metadata we simply do not have would silently disable
    // dropping rather than explain it.
    fun testUnknownClassIsNotRefused() {
        assertTrue(analyze("my \$x = 1;").accepts("RakuAST::NotAThing", "RakuAST::Expression"))
    }

    // A StrLiteral's span covers the bare content between the quotes, and that
    // text alone parses as a call rather than a string. Dragging one has to
    // lift the enclosing quoted construct instead.
    fun testStringLiteralDragsTheQuotedConstruct() {
        val source = "my \$x = \"cool\";"
        val literal = find(analyze(source).tree!!, "RakuAST::StrLiteral")!!

        assertEquals("cool", source.substring(literal.span!!.from, literal.span!!.to))
        val drag = literal.sourceSpan()!!
        assertEquals("\"cool\"", source.substring(drag.from, drag.to))
    }

    // The common case: a node whose span already covers its own source says so
    // by omitting the drag span, rather than repeating it.
    fun testOrdinaryNodeDragsItsOwnSpan() {
        val source = "my \$x = 41;"
        val literal = find(analyze(source).tree!!, "RakuAST::IntLiteral")!!

        assertNull("no drag span is needed here", literal.dragSpan)
        assertEquals("41", source.substring(literal.sourceSpan()!!.from, literal.sourceSpan()!!.to))
    }
}
