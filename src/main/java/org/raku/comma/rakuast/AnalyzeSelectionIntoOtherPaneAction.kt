package org.raku.comma.rakuast

import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Analyzes the selection into whichever pane is *not* currently active.
 *
 * The way to seed the second pane without first clicking into it: the usual
 * action follows the active pane, which is what you want while iterating but
 * not when setting up a comparison.
 *
 * Deliberately not added to the editor's context menu -- one entry there is
 * enough. It is reachable through Find Action, and a user who does this often
 * can bind a key to it.
 */
class AnalyzeSelectionIntoOtherPaneAction : AnalyzeSelectionAction() {

    override fun actionPerformed(event: AnActionEvent) = analyze(event, PaneTarget.OTHER)
}
