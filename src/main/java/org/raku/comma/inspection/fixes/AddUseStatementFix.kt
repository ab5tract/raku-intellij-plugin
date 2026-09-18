package org.raku.comma.inspection.fixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager

/**
 * Inserts `use <module>;` at the top of the file. Unlike [AddUseModuleFix]
 * it needs no open editor (so it also works in batch inspection runs), and
 * it does not touch META6.json depends -- the module it imports lives in
 * the same project, so it is not an external dependency.
 */
class AddUseStatementFix(private val moduleName: String) : LocalQuickFix {

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val file = descriptor.psiElement?.containingFile ?: return
        val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return
        document.insertString(0, "use $moduleName;\n")
    }

    override fun getName(): String { return "Use module '%s'".format(moduleName) }

    override fun getFamilyName(): String { return "Use module" }
}
