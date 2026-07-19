package org.raku.comma.inspection.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.inspection.RakuInspection
import org.raku.comma.inspection.fixes.StubMissingSubroutineFix
import org.raku.comma.psi.*
import org.raku.comma.services.project.RakuProjectDetailsService
import org.raku.comma.services.project.RakuProjectSdkService

class UndeclaredOrDeprecatedRoutineInspection : RakuInspection() {
    override fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement) {
        if (element !is RakuSubCallName) return

        // Only do the analysis if the core setting symbols are available.
        // TODO!!! Get the CoreSettings stuff extracted working again.
        val setting = element.getProject().service<RakuProjectSdkService>().symbolCache.getCoreSettingFile()
                            ?: return
        if (setting.virtualFile.name == "DUMMY") return

        // Resolve the reference.
        val subName = element.callName
        if (subName.startsWith("::")) return
        val reference = element.reference as PsiReferenceBase.Poly<*> ?: return
        val results = reference.multiResolve(true)

        // If no resolve results, then we've got an error.
        if (results.isEmpty()) {
            // Check whether there is an operator of the same name in scope
            if (element.resolvesAsLexicalOperator() ||  element.isValidNqp()) return

            val description = "Subroutine %s is not declared".format(subName)
            // `const $x = ...` is a common Raku-ism typo for `constant`; offer
            // the dedicated fix alongside stubbing (lost in the annotator
            // migration).
            val next = com.intellij.psi.util.PsiTreeUtil.skipWhitespacesForward(element)
            if (subName == "const" && (next is org.raku.comma.psi.RakuVariable || next is org.raku.comma.psi.RakuInfixApplication)) {
                holder.registerProblem(element, description, StubMissingSubroutineFix(),
                                       org.raku.comma.inspection.fixes.ConstKeywordFix())
            } else {
                holder.registerProblem(element, description, StubMissingSubroutineFix())
            }
        } else if (results.size == 1) {
            // If it resolves to a type, highlight it as one.
            val resolvedElement = results[0].element
            if (resolvedElement is RakuPackageDecl) {
                resolvedElement.reference?.let { holder.registerProblem(it, ProblemHighlightType.INFORMATION) }
            } else if (resolvedElement is RakuDeprecatable && resolvedElement.isDeprecated) {
                // If it resolves to a deprecated routine, highlight it as one.
                var deprecationMessage = (resolvedElement as RakuDeprecatable).deprecationMessage
                deprecationMessage = if (deprecationMessage == null) "" else " ... use $deprecationMessage"
                val message = "%s is deprecated%s".format(subName, deprecationMessage)
                holder.registerProblem(resolvedElement, message, ProblemHighlightType.WARNING)
            }
        }
    }

    private fun RakuSubCallName.isValidNqp(): Boolean {
        if (! this.callName.startsWith("nqp::")) return false
        return PsiTreeUtil.findChildrenOfType(containingFile, RakuUseStatement::class.java)
                    .any { it.moduleName == "nqp" || it.moduleName == "MONKEY-GUTS" || it.moduleName == "MONKEY" }
                || this.project.service<RakuProjectDetailsService>().isProjectRakudoCore
    }
}