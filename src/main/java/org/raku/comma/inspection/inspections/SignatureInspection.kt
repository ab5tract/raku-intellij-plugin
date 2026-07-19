package org.raku.comma.inspection.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import org.raku.comma.inspection.InspectionConstants.Signature.*
import org.raku.comma.inspection.RakuInspection
import org.raku.comma.psi.RakuParameter
import org.raku.comma.psi.RakuSignature
import org.raku.comma.psi.RakuVariable

class SignatureInspection : RakuInspection() {
    override fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement) {
        if (element !is RakuSignature) return

        var state: SignatureState? = null
        val params = element.getChildren()
        if (params.isEmpty()) return

        var isFirst = true

        for (param in params) {
            if (param !is RakuParameter) continue

            if (isFirst) {
                state =
                    when {
                        param.isPositional -> SignatureState.POSITIONAL
                        param.isNamed -> SignatureState.NAMED
                        param.isOptional -> SignatureState.OPTIONAL
                        else -> SignatureState.VARIADIC
                    }
                isFirst = false
            }

            // Positionals go first
            if (state != SignatureState.POSITIONAL
                && param.isPositional
                && !param.isOptional
                && !param.isExplicitlyOptional
                && !param.isSlurpy
            ) {
                 val message =
                    when (state) {
                        SignatureState.NAMED    ->  "Cannot put positional parameter %s after a named parameter"
                        SignatureState.OPTIONAL ->  "Cannot put positional parameter %s after an optional parameter"
                        SignatureState.VARIADIC ->  "Cannot put positional parameter %s after a variadic parameter"
                        else -> null
                    } ?: return

                holder.registerProblem(param, message.format(param.variableName), ProblemHighlightType.ERROR)
                return
            }

            // Optionals go before variadic, but not named ones
            if (state == SignatureState.VARIADIC && param.isOptional && !param.isNamed) {
                val description = "Cannot put optional parameter %s after a variadic parameter".format(param.variableName)
                holder.registerProblem(param, description, ProblemHighlightType.ERROR)
                return
            }

            // Optionals go before named
            if (param.isOptional && !param.isNamed && state == SignatureState.NAMED) {
                val description = DESCRIPTION_NO_OPTIONAL_AFTER_NAMED.format(param.variableName)
                holder.registerProblem(param, description, ProblemHighlightType.WARNING)
                return
            }

            // A problem-free parameter must not abort the scan of the rest
            // of the signature.
            val problem: Problem? =
                when {
                    param.isExplicitlyOptional && param.isNamed
                        -> Problem(ProblemHighlightType.WARNING, DESCRIPTION_NAMED_ARE_OPTIONAL)
                    param.isRequired && param.initializer != null && RakuVariable.getTwigil(param.variableName) != '!'
                        -> Problem(ProblemHighlightType.ERROR, DESCRIPTION_CANNOT_REQUIRE_DEFAULT)
                    param.isPositional && param.isRequired
                        -> Problem(ProblemHighlightType.WARNING, DESCRIPTION_POS_ALREADY_REQUIRED)
                    param.isExplicitlyOptional && param.initializer != null
                        -> Problem(ProblemHighlightType.WARNING, DESCRIPTION_DEFAULTS_ARE_OPTIONAL)
                    else
                        -> null
                }
            if (problem != null) {
                holder.registerProblem(param, problem.description.format(param.variableName), problem.level)
            }

            state = when {
                param.isNamed    -> SignatureState.NAMED
                param.isOptional -> SignatureState.OPTIONAL
                param.isSlurpy   -> SignatureState.VARIADIC
                else -> null
            } ?: continue
        }
    }

    data class Problem(val level: ProblemHighlightType, val description: String)
    enum class SignatureState {
        POSITIONAL, NAMED, OPTIONAL, VARIADIC
    }
}