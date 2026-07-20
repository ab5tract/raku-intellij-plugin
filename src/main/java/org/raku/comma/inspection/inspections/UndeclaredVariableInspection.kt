package org.raku.comma.inspection.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.inspection.InspectionConstants.UndeclaredVariable.*
import org.raku.comma.inspection.RakuInspection
import org.raku.comma.psi.*
import org.raku.comma.psi.symbols.RakuSymbolKind
import java.util.regex.Pattern

class UndeclaredVariableInspection : RakuInspection() {

    override fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement) {

        // Make sure we've got a sensible looking variable to check.
        if (element !is RakuVariable) return
        if (element.getParent() is RakuRegexVariable) return
        if (element.getParent() is RakuVariableDecl) return

        val variableName = element.variableName ?: return

        val regexVarPatterns = Pattern.compile("\\$\\d+|\\$<[\\w\\d_-]+>")
        if (regexVarPatterns.matcher(variableName).matches()) {
            val symbol = element.resolveLexicalSymbol(RakuSymbolKind.Variable, "$/")
            if (! symbol!!.isImplicitlyDeclared) return
        }

        // Check for $=finish section
        if (RakuVariable.getTwigil(variableName) == '=' && variableName == "$=finish") {
            val list: PsiElement? = PsiTreeUtil.getChildOfType(PsiTreeUtil.getParentOfType(element, PsiFile::class.java),
                                                               RakuStatementList::class.java)
            if (list == null) return
            val maybeFinish: PsiElement? = PsiTreeUtil.findChildOfType(list, PodBlockFinish::class.java)
            if (maybeFinish != null) {
                holder.registerProblem(element, DESCRIPTION_DOC_FINISH, ProblemHighlightType.ERROR)
            }
        }

        // We only check usual variables in this annotator
        // attributes are handled by another one
        if (RakuVariable.getTwigil(variableName) != ' ') return

        // Ignore anonymous variables
        // It also skips cases of contextualizer declarations
        if (ANONYMOUS_VARIABLES.contains(variableName)) return

        // If it's an implicit variable, ensure that there is either no signature on the containing block
        // or a signature that declares the implicit variable
        if (IMPLICIT_VARIABLES.contains(variableName) && element.parent !is RakuParameterVariable) {
            // At file top level there is no enclosing block, hence no
            // signature that could shadow the implicit variable.
            val nearestBlockoid = PsiTreeUtil.getParentOfType(element, RakuBlockoid::class.java) ?: return

            // Even Kotlin hasn't fixed this stupidity in Java...
            // I present to you a hand-unrolled loop of class checking
            var aSignatureBlock: PsiElement? = PsiTreeUtil.getParentOfType(element, RakuPointyBlock::class.java)
            if (aSignatureBlock != null && nearestBlockoid.parent != aSignatureBlock) return

            // Only compute for routine declaration if we haven't already found a pointy block
            if (aSignatureBlock == null) {
                aSignatureBlock = PsiTreeUtil.getParentOfType(element, RakuRoutineDecl::class.java)
                if (aSignatureBlock != null && nearestBlockoid.parent != aSignatureBlock) return
            }

            val signature = PsiTreeUtil.getChildOfType(aSignatureBlock, RakuSignature::class.java) ?: return
            if (signature.parameters.find { starStripper(it.text) == variableName } != null) return
            holder.registerProblem(element, DESCRIPTION_IMPLICIT_WITH_SIGNATURE_FORMAT.format(variableName), ProblemHighlightType.ERROR)
        }

        // Make sure it's not a long or late-bound name.
        if (variableName.contains("::") || variableName.contains(":[")) return

        // Otherwise, try to resolve it.
        val symbol = element.resolveLexicalSymbol(RakuSymbolKind.Variable, variableName)
        if (symbol == null) {
            val reference = checkNotNull(element.getReference())
            val resolved = reference.resolve()
            if (resolved == null) {
                // Straight resolution failure
                holder.registerProblem(element,
                                       DESCRIPTION_NOT_DECLARED_FORMAT.format(variableName),
                                       ProblemHighlightType.ERROR)
            }
        } else {
            if (RakuVariable.getSigil(variableName) != '&') {
                val psi = symbol.psi
                if (psi != null && psi.containingFile === element.getContainingFile() && psi.textOffset > element.getTextOffset()) {
                    holder.registerProblem(element,
                                           DESCRIPTION_NOT_DECLARED_SCOPE_FORMAT.format(variableName),
                                           ProblemHighlightType.ERROR)
                }
            }
        }
    }

    private fun starStripper(text: String): String {
        return text.replace("*", "").trim()
    }
}