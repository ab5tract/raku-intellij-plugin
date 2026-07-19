package org.raku.comma.inspection.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import org.raku.comma.inspection.RakuInspection
import org.raku.comma.inspection.fixes.UseBindingToDestructureFix
import org.raku.comma.psi.RakuInfix
import org.raku.comma.psi.RakuVariableDecl

class ListAssignmentInspection : RakuInspection() {

    private val descriptionFormat = "%s slurps everything from assignment"

    override fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement) {
        if (element !is RakuVariableDecl) return

        if (!element.hasInitializer()) return

        val infix = PsiTreeUtil.findChildOfType(element, RakuInfix::class.java)
        if (infix == null || infix.operator.text != "=") return

        val variables = element.variables
        if (variables.size < 2) return

        for ((i, variable) in variables.withIndex()) {
            val name = variable.variableName ?: continue
            val startsWithAt = name.startsWith("@")
            if (i != variables.size - 1 && (startsWithAt || name.startsWith("%"))) {
                val description = descriptionFormat.format(if (startsWithAt) "Array" else "Hash")
                // Anchor on the declaration (it contains both the slurpy
                // variable to highlight and the infix the fix rewrites).
                val range = TextRange(
                    variable.textRange.startOffset - element.textRange.startOffset,
                    variable.textRange.endOffset - element.textRange.startOffset
                )
                holder.registerProblem(element, range, description, UseBindingToDestructureFix())
            }
        }
    }
}