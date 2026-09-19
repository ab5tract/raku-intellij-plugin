package org.raku.comma.rakuast

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.EditorFactory
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

    // Findings 2+3, one shared guard: Raku's origin offsets are NFG grapheme
    // indices into the analyzed snippet, not IntelliJ's UTF-16 document
    // offsets, and the document can also simply have changed (e.g. a line
    // typed above the selection) since analyze() ran. Either way, the text
    // at baseOffset+span no longer matches what the backend analyzed, and
    // highlight() must refuse rather than select unrelated text.
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
