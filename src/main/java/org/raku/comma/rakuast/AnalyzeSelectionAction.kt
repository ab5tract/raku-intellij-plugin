package org.raku.comma.rakuast

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.psi.RakuFile
import org.raku.comma.psi.RakuNeedStatement
import org.raku.comma.psi.RakuPackageDecl
import org.raku.comma.psi.RakuRoutineDecl
import org.raku.comma.psi.RakuUseStatement

class AnalyzeSelectionAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val selection = editor.selectionModel
        val snippet = selection.selectedText ?: return
        val baseOffset = selection.selectionStart

        // .AST compiles its string as a whole compilation unit, so a selection
        // cannot see the file's imports unless they travel with it.
        val service = RakuAstService.getInstance(project)
        val psiFile = event.getData(CommonDataKeys.PSI_FILE)
        val context =
            if (service.supportsFileContext()) fileContextOf(psiFile) else emptyList()

        ToolWindowManager.getInstance(project)
            .getToolWindow(RakuAstViewerFactory.TOOL_WINDOW_ID)?.activate(null, true)

        // The subprocess costs ~310ms; never run it on the EDT.
        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Analyzing RakuAST", true) {
                override fun run(indicator: ProgressIndicator) {
                    val result = service.analyze(snippet, context)
                    ApplicationManager.getApplication().invokeLater {
                        RakuAstViewerFactory.findPanel(project)
                            ?.showAnalysis(editor, baseOffset, snippet, result,
                                           contextAvailable = service.supportsFileContext())
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

    /**
     * The file's compilation-unit-level `use` and `need` statements, in source
     * order, reconstructed from their module names rather than copied verbatim
     * so a malformed or partially-typed line cannot break the prefix we
     * compile. Only top-level statements count: an import nested inside a
     * block is not in scope for the whole file either.
     */
    private fun fileContextOf(psiFile: PsiFile?): List<String> {
        val file = psiFile as? RakuFile ?: return emptyList()
        val statements = mutableListOf<String>()
        for (use in PsiTreeUtil.findChildrenOfType(file, RakuUseStatement::class.java)) {
            if (!isTopLevel(file, use)) continue
            use.moduleName?.takeIf { it.isNotBlank() }?.let { statements.add("use $it;") }
        }
        for (need in PsiTreeUtil.findChildrenOfType(file, RakuNeedStatement::class.java)) {
            if (!isTopLevel(file, need)) continue
            for (name in need.moduleNames) {
                if (name.isNotBlank()) statements.add("need $name;")
            }
        }
        return statements
    }

    // Top-level means no enclosing package or routine: anything inside one is
    // scoped to it, so replaying it in front of the selection would be wrong.
    private fun isTopLevel(file: RakuFile, element: PsiElement): Boolean {
        var parent = element.parent
        while (parent != null && parent != file) {
            if (parent is RakuPackageDecl || parent is RakuRoutineDecl) return false
            parent = parent.parent
        }
        return true
    }

    // BGT, not EDT: update() asks for PSI_FILE, which the action system
    // provides by rules and refuses to compute on the EDT ("'psi.File' is
    // requested on EDT ... See ActionUpdateThread javadoc"). Reading the
    // editor's selection is fine here too -- the data context is pre-cached
    // before update() runs, and BGT updates hold a read action.
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
