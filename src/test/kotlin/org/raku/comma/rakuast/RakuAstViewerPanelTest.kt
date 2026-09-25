package org.raku.comma.rakuast

import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

/**
 * The container's own concerns: that there really are two independent panes,
 * and that the preferences it owns reach both of them. Everything about what a
 * single analysis does lives in [RakuAstPaneTest].
 */
class RakuAstViewerPanelTest : CommaFixtureTestCase() {

    fun testHoldsTwoPanes() {
        val panel = RakuAstViewerPanel(project)

        assertEquals(2, panel.panes().size)
        assertEquals(PaneId.LEFT, panel.pane(PaneId.LEFT).paneId)
        assertEquals(PaneId.RIGHT, panel.pane(PaneId.RIGHT).paneId)
    }

    // The point of two panes is that one can hold a reference analysis while
    // you work in the other, so an analysis must not leak across.
    fun testAnalyzingDoesNotDisturbTheOtherPane() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 1;")
        val panel = RakuAstViewerPanel(project)
        val tree = AstNode(
            nodeClass = "RakuAST::StatementList",
            span = AstSpan(0, 10),
            children = listOf(AstNode("RakuAST::IntLiteral", listOf(0), AstSpan(8, 9))),
        )

        panel.showAnalysis(myFixture.editor, 0, "my \$x = 1;", AnalyzeResult(tree = tree))

        assertEquals(2, panel.pane(PaneId.LEFT).nodeCount())
        assertEquals("the right pane should still be empty",
                     0, panel.pane(PaneId.RIGHT).nodeCount())
    }

    // One options object behind one persisted key, so a toggle has to reach
    // both panes. If it reached only the focused one the panes would disagree
    // about a setting the user set once.
    fun testGistToggleReachesBothPanes() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 1;")
        val panel = RakuAstViewerPanel(project)
        val options = fieldValue<RakuAstViewerOptions>(panel, "options")

        val before = options.showGist
        toggle(panel, "showGist")

        assertEquals("the toggle should have flipped the shared option",
                     !before, options.showGist)
        // Both panes read that same instance, which is the property that makes
        // a single checkbox correct for two panes.
        panel.panes().forEach {
            assertSame(options, fieldValue<RakuAstViewerOptions>(it, "options"))
        }
    }

    // The left pane is the target until something says otherwise, so the
    // viewer is usable without first learning that panes have focus.
    fun testLeftPaneStartsActive() {
        assertEquals(PaneId.LEFT, RakuAstViewerPanel(project).activePaneId())
    }

    // Analyzing follows the active pane, and analyzing also *sets* it. That
    // pairing is what makes the iterate-in-place loop need no extra clicks:
    // re-analyzing goes back to where the last analysis landed, leaving the
    // other pane untouched as a reference.
    fun testOtherTargetLandsInTheInactivePaneAndActivatesIt() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 1;")
        val panel = RakuAstViewerPanel(project)

        panel.showAnalysis(myFixture.editor, 0, "my \$x = 1;",
                           AnalyzeResult(tree = oneChildTree()), target = PaneTarget.OTHER)

        assertEquals(PaneId.RIGHT, panel.activePaneId())
        assertEquals(2, panel.pane(PaneId.RIGHT).nodeCount())
        assertEquals(0, panel.pane(PaneId.LEFT).nodeCount())

        // Now the default target has moved with it: a second analysis stays
        // in the right pane rather than bouncing back to the left.
        panel.showAnalysis(myFixture.editor, 0, "my \$x = 1;",
                           AnalyzeResult(tree = oneChildTree()))

        assertEquals(PaneId.RIGHT, panel.activePaneId())
        assertEquals(0, panel.pane(PaneId.LEFT).nodeCount())
    }

    private fun oneChildTree() = AstNode(
        nodeClass = "RakuAST::StatementList",
        span = AstSpan(0, 10),
        children = listOf(AstNode("RakuAST::IntLiteral", listOf(0), AstSpan(8, 9))),
    )

    // Two panes on one file is a workflow this feature exists for, and an edit
    // in one moves the text under the other's spans. The other pane's own
    // guard would catch that, but only after a click was refused.
    fun testEditInOnePaneWarnsTheOtherOnTheSameFile() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 1;")
        val panel = RakuAstViewerPanel(project)

        // Both panes analyze the same document.
        panel.showAnalysis(myFixture.editor, 0, "my \$x = 1;",
                           AnalyzeResult(tree = oneChildTree()), target = PaneTarget.LEFT)
        panel.showAnalysis(myFixture.editor, 0, "my \$x = 1;",
                           AnalyzeResult(tree = oneChildTree()), target = PaneTarget.RIGHT)

        invokeEditApplied(panel, panel.pane(PaneId.LEFT))

        assertTrue("the other pane should say its analysis is stale, got: " +
                   panel.pane(PaneId.RIGHT).statusText(),
                   panel.pane(PaneId.RIGHT).statusText().contains("re-run Analyze"))
        assertEquals("the editing pane should not warn itself",
                     "", panel.pane(PaneId.LEFT).statusText())
    }

    // A pane showing a different file is unaffected, and a pane showing
    // nothing has nothing to invalidate.
    fun testEmptyPaneIsNotWarned() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 1;")
        val panel = RakuAstViewerPanel(project)
        panel.showAnalysis(myFixture.editor, 0, "my \$x = 1;",
                           AnalyzeResult(tree = oneChildTree()), target = PaneTarget.LEFT)

        invokeEditApplied(panel, panel.pane(PaneId.LEFT))

        assertEquals("", panel.pane(PaneId.RIGHT).statusText())
    }

    private fun invokeEditApplied(panel: RakuAstViewerPanel, pane: RakuAstPane) {
        val method = RakuAstViewerPanel::class.java
            .getDeclaredMethod("editApplied", RakuAstPane::class.java)
        method.isAccessible = true
        method.invoke(panel, pane)
    }

    private fun toggle(panel: RakuAstViewerPanel, name: String) {
        val box = fieldValue<javax.swing.JCheckBox>(panel, name)
        box.isSelected = !box.isSelected
        box.actionListeners.forEach {
            it.actionPerformed(java.awt.event.ActionEvent(box, 0, ""))
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> fieldValue(target: Any, name: String): T {
        val field = target.javaClass.getDeclaredField(name)
        field.isAccessible = true
        return field.get(target) as T
    }
}
