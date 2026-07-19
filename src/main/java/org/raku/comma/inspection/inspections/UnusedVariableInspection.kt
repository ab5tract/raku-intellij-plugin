package org.raku.comma.inspection.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.highlighter.RakuHighlighter
import org.raku.comma.inspection.RakuInspection
import org.raku.comma.inspection.fixes.DeleteUnusedVariableFix
import org.raku.comma.psi.*
import java.util.concurrent.atomic.AtomicInteger

class UnusedVariableInspection : RakuInspection() {

    override fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement) {
        // See if it's something we know how to handle, and extract the items to
        // search for and the search scope.
        var toCheck: List<PsiElement?>? = null
        var searchScope: LocalSearchScope? = null
        var error: String? = null
        if (element is RakuVariableDecl) {
            // If it's lexical or state, then we want to check for usages in the
            // declaring lexical scope.
            val scope = element.scope
            if (scope == "my" || scope == "state") {
                val usageScope = PsiTreeUtil.getParentOfType(element, RakuPsiScope::class.java) ?: return
                searchScope = LocalSearchScope(usageScope)
                toCheck = ArrayList()
                for (variable in element.variables) {
                    if (namedWithoutTwigil(variable.name)) continue
                    toCheck.add(if (variable.parent is RakuParameterVariable) variable.parent else element)
                }
                error = "Unused variable"
            } else if (scope == "has") {
                val containingPackage = PsiTreeUtil.getParentOfType(element, RakuPackageDecl::class.java)
                if (containingPackage != null && containingPackage.packageKind != "role") {
                    searchScope = LocalSearchScope(containingPackage)
                    toCheck = ArrayList()
                    for (variable in element.variables) {
                        val name = variable.variableName
                        if (name == null || name.length < 3 || RakuVariable.getTwigil(name) != '!') continue
                        toCheck.add(variable)
                    }
                    error = "Unused attribute"
                }
            }
        } else if (element is RakuParameterVariable) {
            // Make sure the parameter is not really part of a signature in a
            // variable declaration; those are checked separately. Also ensure
            // it's not anonymous nor carrying a twigil, and not on a stubbed
            // method (where they can be just for documentation).
            val name = (element as RakuParameterVariable).name!!
            if (namedWithoutTwigil(name)) return
            val signature = PsiTreeUtil.getParentOfType(element, RakuSignature::class.java)
            if (signature != null) {
                val signatureOwner = signature.parent
                if (signatureOwner !is RakuVariableDecl
                    && ! (signatureOwner is RakuRoutineDecl && signatureOwner.isStubbed)
                ) {
                    val usageScope = PsiTreeUtil.getParentOfType(element, RakuPsiScope::class.java)
                    if (usageScope != null) {
                        searchScope = LocalSearchScope(usageScope)
                        toCheck = listOf<PsiElement>(element)
                        error = "Unused parameter"
                    }
                }
            }
        }


        // Assuming we have search targets, go over them.
        if (toCheck == null) return
        for (expectedUsed in toCheck) {
            // We need two references, since the declaration resolves to itself.
            val results = ReferencesSearch.search(expectedUsed!!, searchScope!!)
            val count = AtomicInteger()
            results.forEach { _ -> count.incrementAndGet() }

            // Annotate if not found.
            if (count.get() < 2 && ! implicitlyUsed(expectedUsed)) {
                val toAnnotate = if (expectedUsed is RakuVariableDecl) expectedUsed.variables[0] else expectedUsed

                customHighlight(toAnnotate, RakuHighlighter.UNUSED)
                // Deleting is only safe for standalone variables: removing a
                // parameter changes the signature, and removing one variable
                // of a multi-declaration mangles the declaration.
                val enclosingDecl = expectedUsed as? RakuVariableDecl
                    ?: PsiTreeUtil.getParentOfType(expectedUsed, RakuVariableDecl::class.java)
                val deletable = (error == "Unused variable" || error == "Unused attribute")
                        && (enclosingDecl == null || enclosingDecl.variables.size == 1)
                val fixes = if (deletable) arrayOf<com.intellij.codeInspection.LocalQuickFix>(DeleteUnusedVariableFix(toAnnotate.text)) else emptyArray()
                holder.registerProblem(toAnnotate,
                                       error!!,
                                       ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                       *fixes)
            }
        }
    }


    private fun namedWithoutTwigil(name: String?): Boolean {
        if (name == null || name.length < 2) return true
        val twigil = RakuVariable.getTwigil(name)
        if (twigil != ' ') return true
        return false
    }

    private fun implicitlyUsed(used: PsiElement): Boolean {
        var name: String? = null
        if (used is RakuVariable) {
            name = used.variableName
        } else if (used is RakuParameterVariable) {
            name = used.name
        }
        if (name == null) return false

        if (name == "\$_") {
            return implicitlyUsedTopic(PsiTreeUtil.getParentOfType(used, RakuPsiScope::class.java))
        }
        if (name == "$/") {
            return implicitlyUsedMatch(PsiTreeUtil.getParentOfType(used, RakuPsiScope::class.java))
        }

        return false
    }

    private fun implicitlyUsedTopic(current: RakuPsiElement?): Boolean {
        for (child in current!!.children) {
            // If this construct topicalizes, then it won't have implicit uses of the
            // current topic.
            if (child is RakuTopicalizer && child.isTopicalizing) continue
            // If it's a `when` statement, it tests against the topic.
            if (child is RakuWhenStatement) return true
            // If it's a call on the topic (`.foo`), it uses the topic.
            if (child is RakuMethodCall && child.isTopicCall) return true
            // Otherwise, recurse.
            if (child is RakuPsiElement && implicitlyUsedTopic(child)) return true
        }
        return false
    }

    private fun implicitlyUsedMatch(current: RakuPsiElement?): Boolean {
        for (child in current!!.children) {
            // Use of $0 and $<foo> is the implicit use we're looking for.
            if (child is RakuVariable && child.isCaptureVariable) return true
            // If it's a routine, then it will declare a fresh $/.
            if (child is RakuRoutineDecl) continue
            // Otherwise, recurse.
            if (child is RakuPsiElement && implicitlyUsedMatch(child)) return true
        }
        return false
    }
}