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

    private fun insertSlotFor(pane: RakuAstPane, parent: AstNode, childIndex: Int): DropSlot? {
        val method = RakuAstPane::class.java.getDeclaredMethod(
            "insertionSlotFor", AstNode::class.java, Int::class.javaPrimitiveType)
        method.isAccessible = true
        return method.invoke(pane, parent, childIndex) as DropSlot?
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

    // Dropping onto a list item replaces that item by position, not the whole
    // list -- setting the attribute outright would discard its siblings.
    fun testDroppingOnAListItemReplacesThatItem() {
        val pane = analyzedPane("my \$x = 1; say \$x;")
        val root = fieldValue<AstNode?>(pane, "rootNode")!!
        val statement = root.children[1]

        assertEquals("statements", statement.viaAttr)
        assertEquals(1, statement.viaIndex)

        val slot = slotFor(pane, statement)!!
        assertEquals("statements", slot.attrName)
        assertEquals(1, slot.listIndex)
        assertEquals("one item replaced, not the list", 1, slot.replaceCount)
    }

    // Dropping between two items inserts rather than replaces: the item that
    // would be pushed down names both the list and the position.
    fun testDroppingBetweenItemsInserts() {
        val pane = analyzedPane("my \$x = 1; say \$x;")
        val root = fieldValue<AstNode?>(pane, "rootNode")!!

        val slot = insertSlotFor(pane, root, 1)!!
        assertEquals("statements", slot.attrName)
        assertEquals(1, slot.listIndex)
        assertEquals("nothing is replaced by an insert", 0, slot.replaceCount)
    }

    // Past the last child, the position is one after the final item.
    fun testDroppingPastTheEndAppends() {
        val pane = analyzedPane("my \$x = 1; say \$x;")
        val root = fieldValue<AstNode?>(pane, "rootNode")!!

        val slot = insertSlotFor(pane, root, root.children.size)!!
        assertEquals(2, slot.listIndex)
        assertEquals(0, slot.replaceCount)
    }

    // A tree child index is not a list index. A call's children are its Name
    // and its ArgList, which belong to no list -- the gap between them is not
    // an insertion point, and must not be mistaken for one.
    fun testGapBetweenNonListChildrenIsNotAnInsertionPoint() {
        // The declaration is not incidental: .AST resolves names, so a bare
        // `say $x;` does not compile on its own and yields no tree at all.
        val pane = analyzedPane("my \$x = 1; say \$x;")
        val root = fieldValue<AstNode?>(pane, "rootNode")!!
        val call = find(root, "RakuAST::Call::Name::WithoutParentheses")!!

        assertNull("Name and ArgList are separate attributes, not list items",
                   call.children.firstOrNull()?.viaIndex)
        assertNull(insertSlotFor(pane, call, 1))
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
