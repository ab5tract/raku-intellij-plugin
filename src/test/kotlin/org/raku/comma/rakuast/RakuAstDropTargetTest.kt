package org.raku.comma.rakuast

import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

/**
 * Resolving "drop here" into "write this slot".
 *
 * This is the half of drag-and-drop that a Swing gesture cannot usefully
 * simulate, and the half where being wrong means silently editing the wrong
 * node — so it is driven directly.
 */
class RakuAstDropTargetTest : CommaFixtureTestCase() {

    private fun analyzedPane(source: String): RakuAstPane {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, source)
        val pane = RakuAstPane(project, RakuAstViewerOptions(), PaneId.LEFT)
        pane.showAnalysis(myFixture.editor, 0,
                          source, RakuAstService.getInstance(project).analyze(source))
        return pane
    }

    private fun slotFor(pane: RakuAstPane, node: AstNode): DropSlot? {
        val method = RakuAstPane::class.java
            .getDeclaredMethod("replacementSlotFor", AstNode::class.java)
        method.isAccessible = true
        return method.invoke(pane, node) as DropSlot?
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> fieldValue(pane: RakuAstPane, name: String): T {
        val field = RakuAstPane::class.java.getDeclaredField(name)
        field.isAccessible = true
        return field.get(pane) as T
    }

    private fun find(node: AstNode, cls: String): AstNode? {
        if (node.nodeClass == cls) return node
        for (child in node.children) find(child, cls)?.let { return it }
        return null
    }

    // Dropping onto a node replaces it, and a node's place is a slot on its
    // parent — so the write targets the parent, not the node dropped on.
    fun testDroppingOnANodeTargetsItsParentsSlot() {
        val pane = analyzedPane("my \$x = 41;")
        val root = fieldValue<AstNode?>(pane, "rootNode")!!
        val declaration = find(root, "RakuAST::VarDeclaration::Simple")!!

        val slot = slotFor(pane, declaration)!!

        assertEquals("expression", slot.attrName)
        assertEquals("RakuAST::Statement::Expression", slot.node.nodeClass)
        assertEquals("RakuAST::Expression", slot.declaredType)
    }

    // The declared type travels with the slot, and it is what gates the drop:
    // an Expression may go here, a Name may not.
    // The call gives the tree a RakuAST::Name node, so its conformance is
    // actually reported. A class the analysis never saw is deliberately
    // permitted rather than refused, so a negative case has to use one that
    // is genuinely present.
    fun testResolvedSlotCarriesTheTypeThatGatesTheDrop() {
        val pane = analyzedPane("my \$x = 41; say \$x;")
        val root = fieldValue<AstNode?>(pane, "rootNode")!!
        val conformance = fieldValue<Map<String, List<String>>>(pane, "conformance")
        val slot = slotFor(pane, find(root, "RakuAST::VarDeclaration::Simple")!!)!!
        assertEquals("RakuAST::Expression", slot.declaredType)

        val intLiteral = RakuAstDragPayload(
            PaneId.LEFT, "RakuAST::IntLiteral", conformance["RakuAST::IntLiteral"], "99")
        val name = RakuAstDragPayload(
            PaneId.LEFT, "RakuAST::Name", conformance["RakuAST::Name"], "foo")
        assertNotNull("the tree should report this class", name.conforms)

        assertTrue("an expression belongs in an expression slot",
                   intLiteral.fitsSlot(slot.declaredType))
        assertFalse("a name is not an expression and must be refused",
                    name.fitsSlot(slot.declaredType))
    }

    // The root hangs off nothing, so there is no slot to write.
    fun testRootIsNotADropTarget() {
        val pane = analyzedPane("my \$x = 41;")
        val root = fieldValue<AstNode?>(pane, "rootNode")!!

        assertNull(slotFor(pane, root))
    }

    // Replacing one element of a list needs an index, which the edit verb
    // cannot express. Refused rather than silently retargeting the whole list.
    fun testListElementIsNotYetADropTarget() {
        val pane = analyzedPane("my \$x = 1; say \$x;")
        val root = fieldValue<AstNode?>(pane, "rootNode")!!
        val statement = root.children[1]

        assertEquals("statements", statement.viaAttr)
        assertEquals(1, statement.viaIndex)
        assertNull("a list element has no single-slot address", slotFor(pane, statement))
    }

    // A dragged node is lifted from the source text, not deparsed, so it keeps
    // whatever the user actually wrote.
    fun testDraggedTextComesFromTheSource() {
        val source = "my \$x = 41;"
        val pane = analyzedPane(source)
        val root = fieldValue<AstNode?>(pane, "rootNode")!!
        val literal = find(root, "RakuAST::IntLiteral")!!

        val method = RakuAstPane::class.java
            .getDeclaredMethod("sourceTextOf", AstNode::class.java)
        method.isAccessible = true

        assertEquals("41", method.invoke(pane, literal))
    }

    // And a node whose span does not yield standalone source is lifted from
    // its drag span instead, so the quotes come with it.
    fun testDraggedStringKeepsItsQuotes() {
        val pane = analyzedPane("my \$x = \"cool\";")
        val root = fieldValue<AstNode?>(pane, "rootNode")!!
        val literal = find(root, "RakuAST::StrLiteral")!!

        val method = RakuAstPane::class.java
            .getDeclaredMethod("sourceTextOf", AstNode::class.java)
        method.isAccessible = true

        assertEquals("\"cool\"", method.invoke(pane, literal))
    }
}
