package org.raku.comma.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.parsing.RakuConditionalCompilation

/**
 * Which #?if condition an element lives under, and whether two elements can
 * never coexist in one build. gen-cat has no else-chains, so exclusivity is
 * decided by the conditions themselves, not adjacency: `X` vs `!X`, or two
 * different positive backends. A negated pair like `!jvm` vs `!js` is NOT
 * exclusive (both true on moar), and unconditional code is exclusive with
 * nothing that can be true alongside the active backend.
 */
object RakuConditionalBranches {

    fun conditionOf(element: PsiElement): String? {
        // Inside an island the branch element knows its condition; active
        // regions have no island, so fall back to the positional lookup.
        val branch = PsiTreeUtil.getParentOfType(element, RakuCondBranch::class.java, false)
        if (branch != null) return branch.condition
        val file = element.containingFile ?: return null
        return RakuConditionalCompilation.conditionAt(file.text, element.textOffset)
    }

    fun inMutuallyExclusiveBranches(a: PsiElement, b: PsiElement): Boolean {
        return exclusive(conditionOf(a), conditionOf(b))
    }

    private fun exclusive(condA: String?, condB: String?): Boolean {
        if (condA == null || condB == null) return false
        val negA = condA.startsWith("!")
        val negB = condB.startsWith("!")
        val nameA = if (negA) condA.substring(1) else condA
        val nameB = if (negB) condB.substring(1) else condB
        if (nameA == nameB) return negA != negB      // jvm vs !jvm
        return !negA && !negB                        // jvm vs js
    }
}
