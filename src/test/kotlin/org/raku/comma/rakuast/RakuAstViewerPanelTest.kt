package org.raku.comma.rakuast

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
}
