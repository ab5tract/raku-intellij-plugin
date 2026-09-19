package org.raku.comma.rakuast

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
}
