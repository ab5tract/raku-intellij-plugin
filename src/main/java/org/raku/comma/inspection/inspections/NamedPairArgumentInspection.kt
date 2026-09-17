package org.raku.comma.inspection.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import org.raku.comma.inspection.InspectionConstants.NamedPairArgument.*
import org.raku.comma.inspection.RakuInspection
import org.raku.comma.inspection.fixes.FatarrowSimplificationFix
import org.raku.comma.psi.*

class NamedPairArgumentInspection : RakuInspection() {
    override fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement) {
        when (element) {
            is RakuColonPair -> checkColonPair(element, holder)
            is RakuFatArrow  -> checkFatArrow(element, holder)
        }
    }

    private fun checkFatArrow(arrow: RakuFatArrow, holder: ProblemsHolder) {
        // Only a genuinely simplifiable pair (True/False value, or a variable
        // matching the key) warrants a visible warning. The general
        // fatarrow-to-colonpair conversion is a style preference, not a
        // problem: register it invisibly so the quick-fix stays reachable
        // via the intentions menu without a gutter mark on every `a => $b`.
        val key = arrow.key ?: return
        if (getSimplifiedPair(arrow, key, arrow.value) != null) {
            processPair(arrow, holder, "FatArrow")
        } else {
            holder.registerProblem(arrow, DESCRIPTION,
                                   com.intellij.codeInspection.ProblemHighlightType.INFORMATION,
                                   FatarrowSimplificationFix("FatArrow"))
        }
    }

    private fun checkColonPair(pair: RakuColonPair, holder: ProblemsHolder) {
        val key = pair.key ?: return
        val value = pair.statement ?: return
        val child = value.firstChild
        if (getSimplifiedPair(pair, key, child) == null) return

        processPair(pair, holder, "ColonPair")
    }

    private fun processPair(pair: PsiElement, holder: ProblemsHolder, pairKind: String) {
        holder.registerProblem(pair, DESCRIPTION, FatarrowSimplificationFix(pairKind))
    }
}