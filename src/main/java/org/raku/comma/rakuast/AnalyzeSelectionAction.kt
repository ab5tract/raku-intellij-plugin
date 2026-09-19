package org.raku.comma.rakuast

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.wm.ToolWindowManager
import org.raku.comma.psi.RakuFile

class AnalyzeSelectionAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val selection = editor.selectionModel
        val snippet = selection.selectedText ?: return
        val baseOffset = selection.selectionStart

        ToolWindowManager.getInstance(project)
            .getToolWindow(RakuAstViewerFactory.TOOL_WINDOW_ID)?.activate(null, true)

        // The subprocess costs ~310ms; never run it on the EDT.
        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Analyzing RakuAST", true) {
                override fun run(indicator: ProgressIndicator) {
                    val result = RakuAstService.getInstance(project).analyze(snippet)
                    ApplicationManager.getApplication().invokeLater {
                        RakuAstViewerFactory.findPanel(project)
                            ?.showAnalysis(editor, baseOffset, snippet, result)
                    }
                }
            })
    }

    override fun update(event: AnActionEvent) {
        // Mirrors RakuReplUsingThisModuleAction: gate on RakuFile so this
        // doesn't show up (enabled or not) in the right-click menu of every
        // editor in the IDE -- Java, JSON, Markdown, plain text -- shelling
        // out to Raku on non-Raku source whenever text happens to be selected.
        val editor = event.getData(CommonDataKeys.EDITOR)
        val file = event.getData(CommonDataKeys.PSI_FILE)
        val available = event.project != null &&
            file is RakuFile &&
            editor?.selectionModel?.hasSelection() == true
        event.presentation.isEnabledAndVisible = available
    }

    override fun getActionUpdateThread() =
        com.intellij.openapi.actionSystem.ActionUpdateThread.EDT
}
