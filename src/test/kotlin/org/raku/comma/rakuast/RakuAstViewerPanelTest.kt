package org.raku.comma.rakuast

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.ide.CopyPasteManager
import java.awt.datatransfer.DataFlavor
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

    fun testHighlightIgnoresEditorDisposedAfterStorage() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 1;")
        val panel = RakuAstViewerPanel(project)

        // A throwaway editor, independent of the fixture's own editor, so we can
        // dispose it without disturbing fixture teardown.
        val factory = EditorFactory.getInstance()
        val editor = factory.createEditor(factory.createDocument("my \$x = 1;"), project)

        val leaf = AstNode("RakuAST::IntLiteral", listOf(0), AstSpan(8, 9))
        val tree = AstNode(
            nodeClass = "RakuAST::StatementList",
            path = emptyList(),
            span = AstSpan(0, 10),
            children = listOf(leaf),
        )
        try {
            panel.showAnalysis(editor, 0, "my \$x = 1;", AnalyzeResult(tree = tree))

            // Simulates the file tab closing after analysis stored the editor, but
            // before the user clicks a node in the tree.
            factory.releaseEditor(editor)

            // Reaches the private highlight() the same way a real tree-selection
            // click would (RakuAstViewerPanel deliberately exposes no public hook
            // for this -- see the class's Swing tree, which is private by design).
            val highlight = RakuAstViewerPanel::class.java.getDeclaredMethod("highlight", AstNode::class.java)
            highlight.isAccessible = true
            highlight.invoke(panel, leaf) // must not throw on the now-disposed editor
        } finally {
            if (!editor.isDisposed) factory.releaseEditor(editor)
        }
    }

    // Clicking the (gist) label copies it. The balloon needs a real mouse
    // event, so this drives the copy itself -- which is the part that can be
    // wrong.
    fun testCopyGistPutsTheGistOnTheClipboard() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 1;")
        val panel = RakuAstViewerPanel(project)

        val gist = "RakuAST::IntLiteral.new(41)"
        seedGistRow(panel, cached = gist, shown = gist)

        assertTrue("a rendered gist should copy", invokeCopyGist(panel, 0))
        assertEquals(gist, CopyPasteManager.getInstance().contents!!
            .getTransferData(DataFlavor.stringFlavor))
    }

    // Copying the placeholder would be discovered only on paste, which is
    // worse than the click appearing to do nothing.
    fun testCopyGistRefusesThePlaceholder() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 1;")
        val panel = RakuAstViewerPanel(project)

        seedGistRow(panel, cached = null, shown = "Rendering…")

        assertFalse("the placeholder must not be copied", invokeCopyGist(panel, 0))
    }

    private fun seedGistRow(panel: RakuAstViewerPanel, cached: String?, shown: String) {
        val model = fieldValue<javax.swing.table.DefaultTableModel>(panel, "attrModel")
        model.rowCount = 0
        model.addRow(arrayOf("(gist)", shown))
        if (cached != null) {
            @Suppress("UNCHECKED_CAST")
            val cache = fieldValue<MutableMap<List<Int>, String>>(panel, "gistCache")
            cache[listOf(0)] = cached
        }
    }

    private fun invokeCopyGist(panel: RakuAstViewerPanel, row: Int): Boolean {
        val method = RakuAstViewerPanel::class.java
            .getDeclaredMethod("copyGist", Int::class.javaPrimitiveType)
        method.isAccessible = true
        return method.invoke(panel, row) as Boolean
    }

    // A root node on its own says nothing about the code you just selected,
    // so the tree opens fully by default.
    fun testTreeIsFullyExpandedAfterAnalysis() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 1;")
        val panel = RakuAstViewerPanel(project)

        panel.showAnalysis(myFixture.editor, 0, "my \$x = 1;", AnalyzeResult(tree = threeLevelTree()))

        val tree = fieldValue<javax.swing.JTree>(panel, "tree")
        assertEquals("all three nodes should be visible without expanding anything",
                     3, tree.rowCount)
    }

    // Unchecking it has to actually be honoured, or the preference is a lie.
    fun testTreeStaysCollapsedWhenExpandAllIsOff() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 1;")
        val panel = RakuAstViewerPanel(project)
        fieldValue<javax.swing.JCheckBox>(panel, "expandAll").isSelected = false

        panel.showAnalysis(myFixture.editor, 0, "my \$x = 1;", AnalyzeResult(tree = threeLevelTree()))

        // 2, not 1: a JTree shows its root expanded by default, so the root
        // and its immediate child are visible either way. What distinguishes
        // "not expanded" is that the third level stays hidden.
        val tree = fieldValue<javax.swing.JTree>(panel, "tree")
        assertEquals("the deepest node should not be visible", 2, tree.rowCount)
    }

    private fun threeLevelTree() = AstNode(
        nodeClass = "RakuAST::StatementList",
        span = AstSpan(0, 10),
        children = listOf(
            AstNode(
                nodeClass = "RakuAST::Statement::Expression",
                path = listOf(0),
                span = AstSpan(0, 10),
                children = listOf(
                    AstNode("RakuAST::IntLiteral", listOf(0, 0), AstSpan(8, 9)),
                ),
            ),
        ),
    )

    // Shift+toggle expands or collapses a whole subtree rather than one level.
    // The mouse gesture can't be simulated meaningfully here, so this drives
    // the recursion directly -- which is where the behaviour actually lives.
    fun testExpandAndCollapseSubtreeAreRecursive() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 1;")
        val panel = RakuAstViewerPanel(project)

        // Three levels deep, so a one-level toggle could not produce the
        // all-expanded state this asserts.
        val deep = AstNode(
            nodeClass = "RakuAST::StatementList",
            span = AstSpan(0, 10),
            children = listOf(
                AstNode(
                    nodeClass = "RakuAST::Statement::Expression",
                    path = listOf(0),
                    span = AstSpan(0, 10),
                    children = listOf(
                        AstNode("RakuAST::IntLiteral", listOf(0, 0), AstSpan(8, 9)),
                    ),
                ),
            ),
        )
        panel.showAnalysis(myFixture.editor, 0, "my \$x = 1;", AnalyzeResult(tree = deep))

        val tree = fieldValue<javax.swing.JTree>(panel, "tree")
        val rootPath = javax.swing.tree.TreePath(tree.model.root)

        invokeWithPath(panel, "expandSubtree", rootPath)
        assertEquals("every row should be visible after a recursive expand",
                     3, tree.rowCount)

        invokeWithPath(panel, "collapseSubtree", rootPath)
        assertEquals("only the root should remain after a recursive collapse",
                     1, tree.rowCount)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> fieldValue(panel: RakuAstViewerPanel, name: String): T {
        val field = RakuAstViewerPanel::class.java.getDeclaredField(name)
        field.isAccessible = true
        return field.get(panel) as T
    }

    private fun invokeWithPath(panel: RakuAstViewerPanel, name: String, path: javax.swing.tree.TreePath) {
        val method = RakuAstViewerPanel::class.java
            .getDeclaredMethod(name, javax.swing.tree.TreePath::class.java)
        method.isAccessible = true
        method.invoke(panel, path)
    }

    private fun invokeHighlight(panel: RakuAstViewerPanel, node: AstNode) {
        val highlight = RakuAstViewerPanel::class.java.getDeclaredMethod("highlight", AstNode::class.java)
        highlight.isAccessible = true
        highlight.invoke(panel, node)
    }

    // A null span means the backend's .origin was undefined (e.g. a
    // synthetic RakuAST::Type::Setting node) -- distinct from a real
    // zero-length span at offset 0. Before the fix, this fell through to
    // baseOffset+0..baseOffset+0, silently clearing the selection and
    // jumping the caret. It must now be a no-op.
    fun testHighlightNoOpOnNullSpan() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 1;")
        val panel = RakuAstViewerPanel(project)
        val leaf = AstNode("RakuAST::Type::Setting", listOf(0), span = null)
        val tree = AstNode(
            nodeClass = "RakuAST::StatementList",
            path = emptyList(),
            span = AstSpan(0, 10),
            children = listOf(leaf),
        )
        panel.showAnalysis(myFixture.editor, 0, "my \$x = 1;", AnalyzeResult(tree = tree))
        myFixture.editor.selectionModel.setSelection(0, 3)

        invokeHighlight(panel, leaf) // must not throw, must not touch the selection

        assertEquals(0, myFixture.editor.selectionModel.selectionStart)
        assertEquals(3, myFixture.editor.selectionModel.selectionEnd)
    }

    // Happy path, pure ASCII: the grapheme-to-UTF-16 map is the identity here,
    // so this must select exactly the same range highlight() always has.
    fun testHighlightSelectsAsciiSpanUnchanged() {
        val snippet = "my \$x = 1;"
        myFixture.configureByText(RakuScriptFileType.INSTANCE, snippet)
        val panel = RakuAstViewerPanel(project)
        // "1" sits at grapheme (== UTF-16, all ASCII) 8..9.
        val leaf = AstNode("RakuAST::IntLiteral", listOf(0), AstSpan(8, 9))
        val tree = AstNode(
            nodeClass = "RakuAST::StatementList",
            path = emptyList(),
            span = AstSpan(0, snippet.length),
            children = listOf(leaf),
        )
        panel.showAnalysis(myFixture.editor, 0, snippet, AnalyzeResult(tree = tree))

        invokeHighlight(panel, leaf)

        assertEquals("", panel.statusText())
        assertEquals(8, myFixture.editor.selectionModel.selectionStart)
        assertEquals(9, myFixture.editor.selectionModel.selectionEnd)
        assertEquals("1", myFixture.editor.selectionModel.selectedText)
    }

    // The finding's own repro: an astral character (🐪, one NFG grapheme but
    // a UTF-16 surrogate pair -- two code units) before the target node
    // shifts every UTF-16 offset after it by one relative to the grapheme
    // count. The backend reports "my $y = 41" at grapheme 13..23; the
    // document (UTF-16) holds that same text at 14..24. Without grapheme
    // conversion, highlight() would select one character short/offset.
    fun testHighlightConvertsGraphemeIndicesAcrossAstralCharacter() {
        val snippet = "my \$e = \"🐪\"; my \$y = 41;"
        // Sanity-check the repro's own claimed offsets before relying on them.
        assertEquals("my \$y = 41", snippet.substring(14, 24))

        myFixture.configureByText(RakuScriptFileType.INSTANCE, snippet)
        val panel = RakuAstViewerPanel(project)
        // Grapheme indices, as the backend would report them (one grapheme
        // for the astral camel, not two).
        val leaf = AstNode("RakuAST::StatementList", listOf(0), AstSpan(13, 23))
        panel.showAnalysis(myFixture.editor, 0, snippet, AnalyzeResult(tree = leaf))

        invokeHighlight(panel, leaf)

        assertEquals("", panel.statusText())
        assertEquals(14, myFixture.editor.selectionModel.selectionStart)
        assertEquals(24, myFixture.editor.selectionModel.selectionEnd)
        assertEquals("my \$y = 41", myFixture.editor.selectionModel.selectedText)
    }

    // Separate from the grapheme-vs-UTF-16 conversion above: even with a
    // correctly-converted span, the document can simply have changed (e.g. a
    // line typed above the selection) since analyze() ran, shifting
    // baseOffset out from under the computed range. The substring-mismatch
    // guard in highlight() must catch that too and refuse rather than select
    // unrelated text.
    fun testHighlightRefusesWhenDocumentChangedSinceAnalyze() {
        val snippet = "my \$x = 1;"
        myFixture.configureByText(RakuScriptFileType.INSTANCE, snippet)
        val panel = RakuAstViewerPanel(project)
        // "1" sits at 8..9 in the original snippet.
        val leaf = AstNode("RakuAST::IntLiteral", listOf(0), AstSpan(8, 9))
        val tree = AstNode(
            nodeClass = "RakuAST::StatementList",
            path = emptyList(),
            span = AstSpan(0, snippet.length),
            children = listOf(leaf),
        )
        panel.showAnalysis(myFixture.editor, 0, snippet, AnalyzeResult(tree = tree))

        // Simulate typing above the selection after analyze: the document
        // shifts under the stored baseOffset, so offset 8 no longer holds "1".
        WriteCommandAction.runWriteCommandAction(project) {
            myFixture.editor.document.insertString(0, "XX")
        }
        val selectionBefore = myFixture.editor.selectionModel.selectedText

        invokeHighlight(panel, leaf)

        assertTrue(
            "status should explain the refusal, got: ${panel.statusText()}",
            panel.statusText().contains("changed since analysis")
        )
        assertEquals(selectionBefore, myFixture.editor.selectionModel.selectedText)
    }
}
