package org.raku.comma.rakuast

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.ide.util.PropertiesComponent
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * The body of the RakuAST Viewer tool window: the analysis pane, plus the
 * display options that apply to it.
 *
 * The split between this and [RakuAstPane] is by lifetime, not by layout.
 * Everything belonging to one analysis -- the tree, the attribute table, the
 * bound editor, the gist cache -- lives in the pane, so that more than one can
 * exist. What is left here is what must not be duplicated: the preference
 * checkboxes, which are backed by a single project-scoped key each.
 */
class RakuAstViewerPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val options = RakuAstViewerOptions(
        expandAll = PropertiesComponent.getInstance(project).getBoolean(EXPAND_ALL_KEY, true),
        showGist = PropertiesComponent.getInstance(project).getBoolean(SHOW_GIST_KEY, false),
    )

    private val leftPane = RakuAstPane(project, options, PaneId.LEFT, ::activate)
    private val rightPane = RakuAstPane(project, options, PaneId.RIGHT, ::activate)

    private var activePane = leftPane

    // On by default: an AST tree is deep and narrow, and a root node alone
    // tells you nothing about the code you just selected. Remembered per
    // project, so someone who prefers to drill down is not re-deciding it
    // every session.
    private val expandAll = JBCheckBox("Expand all after analyzing").apply {
        isSelected = options.expandAll
        border = JBUI.Borders.empty(2, 6)
    }

    // Off by default: each selection costs a round trip to Raku, which is
    // worth paying only for someone who actually reads gists.
    private val showGist = JBCheckBox("Show node gist").apply {
        isSelected = options.showGist
        border = JBUI.Borders.empty(2, 6)
    }

    init {
        // Side by side rather than stacked, and keyed so the divider survives
        // a restart. Note the tool window is anchored right (plugin.xml), where
        // it opens narrow -- JBSplitter.orientation is a var, so a "stack
        // vertically" toggle is a cheap addition if two trees prove cramped.
        add(JBSplitter(false, PANE_SPLIT_KEY, 0.5f).apply {
            firstComponent = leftPane
            secondComponent = rightPane
        }, BorderLayout.CENTER)
        add(JPanel(BorderLayout()).apply {
            add(expandAll, BorderLayout.WEST)
            add(showGist, BorderLayout.CENTER)
        }, BorderLayout.SOUTH)

        // The third argument to setValue is the *default*, and it decides
        // whether the preference is stored at all. It differs between these two
        // because their defaults differ -- carried over deliberately, not
        // copy-paste.
        showGist.addActionListener {
            options.showGist = showGist.isSelected
            PropertiesComponent.getInstance(project).setValue(SHOW_GIST_KEY, options.showGist, false)
            panes().forEach { it.onShowGistChanged() }
        }

        expandAll.addActionListener {
            options.expandAll = expandAll.isSelected
            PropertiesComponent.getInstance(project).setValue(EXPAND_ALL_KEY, options.expandAll, true)
            panes().forEach { it.onExpandAllChanged() }
        }

        // Paint the initial state, so one pane is visibly the target before
        // anything has been focused.
        activate(leftPane)
    }

    fun panes(): List<RakuAstPane> = listOf(leftPane, rightPane)

    fun pane(id: PaneId): RakuAstPane = if (id == PaneId.LEFT) leftPane else rightPane

    fun activePaneId(): PaneId = activePane.paneId

    /**
     * Shows [result] in the pane [target] names, and makes that pane active.
     *
     * Analyzing into a pane activates it so the iterate-in-place loop needs no
     * extra clicks: the next analysis goes where the last one went.
     *
     * [target] is last and defaulted so the original single-pane call site
     * still compiles unchanged.
     */
    fun showAnalysis(
        editor: Editor,
        baseOffset: Int,
        snippet: String,
        result: AnalyzeResult,
        contextAvailable: Boolean = true,
        target: PaneTarget = PaneTarget.ACTIVE,
    ) {
        val pane = resolve(target)
        pane.showAnalysis(editor, baseOffset, snippet, result, contextAvailable)
        activate(pane)
    }

    private fun resolve(target: PaneTarget): RakuAstPane = when (target) {
        PaneTarget.ACTIVE -> activePane
        PaneTarget.OTHER -> if (activePane === leftPane) rightPane else leftPane
        PaneTarget.LEFT -> leftPane
        PaneTarget.RIGHT -> rightPane
    }

    private fun activate(pane: RakuAstPane) {
        activePane = pane
        panes().forEach { it.setActiveAppearance(it === pane) }
    }

    companion object {
        private const val EXPAND_ALL_KEY = "org.raku.comma.rakuast.expandAll"
        private const val SHOW_GIST_KEY = "org.raku.comma.rakuast.showGist"

        private const val PANE_SPLIT_KEY = "org.raku.comma.rakuast.paneSplit"
    }
}
