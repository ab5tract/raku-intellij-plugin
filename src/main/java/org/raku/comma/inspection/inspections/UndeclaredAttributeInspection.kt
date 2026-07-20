package org.raku.comma.inspection.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import org.raku.comma.inspection.RakuInspection
import org.raku.comma.psi.RakuParameterVariable
import org.raku.comma.psi.RakuVariable
import org.raku.comma.psi.RakuVariableDecl
import org.raku.comma.psi.symbols.MOPSymbolsAllowed
import org.raku.comma.psi.symbols.RakuSingleResolutionSymbolCollector
import org.raku.comma.psi.symbols.RakuSymbolKind

class UndeclaredAttributeInspection : RakuInspection() {

    override fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement) {
        // Filter out anything except attribute usages.
        if (element !is RakuVariable) return
        if (element.getParent() is RakuVariableDecl || element.getParent() is RakuParameterVariable) {
            return
        }
        val variableName = element.variableName
        if (variableName == null || variableName.length <= 2 || RakuVariable.getTwigil(element.getText()) != '!') {
            return
        }

        element.getReference() ?: return

        val enclosingPackage = element.getSelfType()
        if (enclosingPackage == null) {
            val problemText = String.format("Attribute %s is used where no self is in scope", variableName)
            holder.registerProblem(element, problemText, ProblemHighlightType.ERROR)
            return
        }
        val collector = RakuSingleResolutionSymbolCollector(variableName, RakuSymbolKind.Variable)
        enclosingPackage.contributeMOPSymbols(
            collector,
            MOPSymbolsAllowed(
                true,
                true,
                true,
                enclosingPackage.packageKind == "role"
            )
        )

        if (collector.result == null) {
            val problemText = String.format("Attribute %s is used, but not declared", variableName)
            holder.registerProblem(element, problemText, ProblemHighlightType.ERROR)
        }
    }
}