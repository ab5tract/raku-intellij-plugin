package org.raku.comma.inspection.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.inspection.RakuInspection
import org.raku.comma.psi.*

class AssignmentToImmutableInspection : RakuInspection() {
    override fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement) {
        when {
            element is RakuInfixApplication && element.isAssignish -> {
                if (element.operands.isNotEmpty()) {
                    checkAssignable(element.operands[0], holder)
                }
            }
            element is RakuPrefixApplication && element.isAssignish -> {
                element.operand?.let { checkAssignable(it, holder) }
            }
            element is RakuPostfixApplication && element.isAssignish -> {
                element.operand?.let { checkAssignable(it, holder) }
            }
        }
    }

    private fun checkAssignable(
        operand: PsiElement,
        holder: ProblemsHolder
    ) {
        val variant: String = when (operand) {
            is RakuVariable -> {
                // Only scalars.
                val name = operand.variableName
                val yieldValue: StringBuilder = StringBuilder()
                if ( !(name == null || (!name.startsWith("$") && !name.startsWith("&")))) {

                    // See if it resolves to a parameter.
                    val reference = operand.reference
                    when (val declaration = reference!!.resolve()) {
                        is RakuParameterVariable -> {
                            // Parameter, but is it a signature on a block, not just `my ($x, $y)`?
                            val parameter = declaration.getParent() as? RakuParameter ?: return
                            val signature = PsiTreeUtil.getParentOfType(
                                parameter,
                                RakuSignature::class.java
                            )
                            if (signature == null || signature.parent is RakuVariableDecl) return

                            // Ensure it's readonly.
                            if (parameter.isCopy || parameter.isRW || parameter.isRaw) return
                            yieldValue.append("a readonly parameter")
                        }

                        is RakuConstant -> {
                            yieldValue.append("a constant")
                        }

                        is RakuRoutineDecl -> {
                            if (!(name == "\$_" || name == "$!" || name == "$/")) {
                                yieldValue.append("a routine")
                            }
                        }

                        else -> yieldValue.append("")
                    }
                }
                yieldValue.toString()
            }
            is RakuIntLiteral        -> "an Int Literal"
            is RakuStrLiteral        -> "a Str literal"
            is RakuComplexLiteral    -> "a Complex literal"
            is RakuNumLiteral        -> "a Num literal"
            is RakuRatLiteral        -> "a Rat literal"
            is RakuRegexLiteral      -> "a Regex literal"
            is RakuRoutineDecl       -> "a routine declaration"
            is RakuParenthesizedExpr -> if (operand.inferType().name == "Pair") "a Pair literal" else ""
            is RakuColonPair -> {
                if (PsiTreeUtil.getChildOfType(
                        operand,
                        RakuSignature::class.java
                    ) != null
                ) "a signature literal" else "a Pair literal"
            }
            is RakuCapture -> "a capture literal"
            is RakuTypeName -> {
                val reference = operand.getReference() ?: return
                val declaration = reference.resolve()
                if (declaration is RakuConstant) "a constant" else ""
            }
            else -> ""
        }

        if (variant.isNotEmpty()) {
            holder.registerProblem(operand, makeMessage(variant), ProblemHighlightType.ERROR)
        }
    }

    private fun makeMessage(variant: String): String {
        return "Cannot assign to %s".format(variant)
    }
}