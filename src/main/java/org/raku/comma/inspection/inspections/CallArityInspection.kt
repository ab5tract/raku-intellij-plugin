package org.raku.comma.inspection.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.ResolveResult
import org.raku.comma.inspection.RakuInspection
import org.raku.comma.psi.*
import org.raku.comma.psi.RakuSignature.MatchFailureReason
import org.raku.comma.psi.external.ExternalRakuRoutineDecl
import org.raku.comma.psi.external.ExternalRakuSignature
import org.raku.comma.psi.type.RakuUntyped
import java.util.stream.Collectors

class CallArityInspection : RakuInspection() {
    override fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement) {
        if (element !is RakuCodeBlockCall) return

        val refElement = if (element is RakuSubCall) element.getFirstChild() else element

        val args = element.callArguments
        if (element is RakuMethodCall) {
            // Ignore meta-methods for now
            if (element.callName.startsWith(".^")) return

            val wholeNode = element.wholeCallNode
            if (wholeNode is RakuPostfixApplication) {
                val operand = wholeNode.operand
                if (operand is RakuPsiElement) {
                    val type = operand.inferType()
                    if (type is RakuUntyped) return
                }
            }
        }

        // If there is a `|` in a call, we are not smart enough to show anything worthy for this case
        for (arg in args) {
            if (arg is RakuPrefixApplication) {
                val prefix = arg.prefix
                if (prefix != null && prefix.text.startsWith("|")) return
            }
        }

        val ref = refElement.reference as? PsiPolyVariantReference ?: return

        val defs = ref.multiResolve(false)
        // TODO: Fix nqp / external symbol caching so it actually works
        if (defs.isEmpty() || resolvesToNqp(element.callName, defs)) return

        val annotations: MutableList<AnnotationBuilderWrap?> = ArrayList()

        MULTI_LOOP@ for (def in defs) {
            if (def.element !is RakuRoutineDecl) return

            var signature = (def.element as RakuRoutineDecl).signatureNode
            if (signature == null) {
                signature = RakuElementFactory.createRoutineSignature(element.getProject(), ArrayList())
                if (signature is ExternalRakuSignature) return
            }
            val result = signature!!.acceptsArguments(args, true, element is RakuMethodCall)
            if (result.isAccepted) {
                for (i in annotations.indices) {
                    annotations[0] = null
                }
                return
            } else {
                for (i in 0..args.size) {
                    val reason = result.getArgumentFailureReason(i) ?: continue
                    when (reason) {
                        MatchFailureReason.TOO_MANY_ARGS -> {
                            annotations.add(AnnotationBuilderWrap(signature,"Too many positional arguments"))
                            continue@MULTI_LOOP
                        }

                        MatchFailureReason.SURPLUS_NAMED -> {
                            annotations.add(AnnotationBuilderWrap(signature,"No such named parameter in signature"))
                            continue@MULTI_LOOP
                        }

                        MatchFailureReason.NOT_ENOUGH_ARGS -> {
                             annotations.add(AnnotationBuilderWrap(signature, "Not enough positional arguments"))
                            continue@MULTI_LOOP
                        }

                        MatchFailureReason.MISSING_REQUIRED_NAMED -> {
                            annotations.add(AnnotationBuilderWrap(signature, "This call misses a required named argument: " + reason.name))
                            continue@MULTI_LOOP
                        }

                        else -> { return }
                    }
                }
            }
        }

        if (defs.size == 1) {
            for (wrapper in annotations) {
                holder.registerProblem(element, wrapper!!.text, ProblemHighlightType.ERROR)
            }
        } else {
            // Multi...
            val message = String.format("No multi candidates match (%s)",
                                        annotations.stream()
                                            .map {
                                                an: AnnotationBuilderWrap? -> "%s: %s".format(an!!.signature.summary(RakuUntyped.INSTANCE),
                                                                                              an.text)
                                            }
                                            .collect(Collectors.joining(", ")))
            // Anchor on the call being inspected: the offending signature may
            // live in a non-physical external file (e.g. CORE symbols).
            holder.registerProblem(element, message, ProblemHighlightType.ERROR)
        }
    }

    private fun resolvesToNqp(callName: String, defs: Array<ResolveResult>): Boolean {
        if (! callName.startsWith("nqp::")) return false
        if (defs.size != 1) return false
        val element = defs.first().element as? ExternalRakuRoutineDecl ?: return false
        return element.parent.containingFile.name == "nqp.rakumod"
    }

    @JvmRecord
    private data class AnnotationBuilderWrap(val signature: RakuSignature, val text: String)
}