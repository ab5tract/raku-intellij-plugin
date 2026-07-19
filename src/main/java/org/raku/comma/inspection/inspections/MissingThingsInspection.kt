package org.raku.comma.inspection.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.extapi.psi.ASTDelegatePsiElement
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.raku.comma.inspection.InspectionConstants.MissingThings.*
import org.raku.comma.inspection.RakuInspection
import org.raku.comma.psi.*

// TODO: See if we can perfect the TextRange such that it only highlights the affected section

class MissingThingsInspection : RakuInspection() {
    override fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement) {
        if (element !is ASTDelegatePsiElement) return

        val errorReport: Report? =
        when (element) {
            is RakuSubCall,
            is RakuMethodCall,
            is RakuParenthesizedExpr,
            is RakuLoopStatement,
            is RakuVariableDecl,
            is RakuSignature,
            is RakuCall -> {
                val opener = element.node.getChildren(T_PAREN_OPEN)
                val closer = element.node.getChildren(T_PAREN_CLOSE)
                if (opener.isNotEmpty() && closer.isEmpty()) Report(opener.first().startOffsetInParent,")") else null
            }

            is RakuArrayComposer -> {
                val opener = element.getNode().getChildren(T_ARRAY_COMP_OPEN)
                val closer = element.getNode().getChildren(T_ARRAY_COMP_CLOSE)
                if (opener.isNotEmpty() && closer.isEmpty())  Report(opener.first().startOffsetInParent, "]") else null
            }

            is RakuArrayIndex -> {
                val opener = element.getNode().getChildren(T_ARRAY_INDEX_OPEN)
                val closer = element.getNode().getChildren(T_ARRAY_INDEX_CLOSE)
                if (opener.isNotEmpty() && closer.isEmpty()) Report(opener.first().startOffsetInParent,"]") else null
            }

            is RakuBlockoid -> {
                val opener = element.getNode().getChildren(T_BLOCK_OPEN)
                val closer = element.getNode().getChildren(T_BLOCK_CLOSE)
                if (opener.isNotEmpty() && closer.isEmpty()) Report(opener.first().startOffsetInParent,"}") else null
            }

            is RakuRegexGroup -> {
                val opener = element.getNode().getChildren(T_RX_GROUP_OPEN)
                val closer = element.getNode().getChildren(T_RX_GROUP_CLOSE)
                if (opener.isNotEmpty() && closer.isEmpty()) Report(opener.first().startOffsetInParent, "]") else null
            }

            is RakuRegexAssertion -> {
                val opener = element.getNode().getChildren(T_RX_ASSERT_OPEN)
                val closer = element.getNode().getChildren(T_RX_ASSERT_CLOSE)
                if (opener.isNotEmpty() && closer.isEmpty()) Report(opener.first().startOffsetInParent, ">") else null
            }

            is RakuRegexCapturePositional -> {
                val opener = element.getNode().getChildren(T_RX_CAP_OPEN)
                val closer = element.getNode().getChildren(T_RX_CAP_CLOSE)
                if (opener.isNotEmpty() && closer.isEmpty()) Report(opener.first().startOffsetInParent,")") else null
            }

            else -> null
        }

        if (errorReport != null) {
            val description = DESCRIPTION_FORMAT.format(errorReport.char)
            // Highlight just the unclosed opener, as the annotator always did.
            holder.registerProblem(element, TextRange(errorReport.pos, errorReport.pos + 1), description)
        }
    }

    private data class Report(val pos: Int, val char: String)
}