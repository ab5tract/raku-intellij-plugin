package org.raku.comma.inspection.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import org.raku.comma.inspection.InspectionConstants.UnitKeyword.*
import org.raku.comma.inspection.RakuInspection
import org.raku.comma.inspection.fixes.AddUnitKeywordFix
import org.raku.comma.inspection.fixes.RemoveUnitKeywordFix
import org.raku.comma.parsing.RakuElementType
import org.raku.comma.parsing.RakuElementTypes
import org.raku.comma.parsing.RakuTokenTypes
import org.raku.comma.psi.RakuBlockoid
import org.raku.comma.psi.RakuPackageDecl
import org.raku.comma.psi.RakuScopedDecl
import org.raku.comma.psi.RakuStatementList

class UnitKeywordInspection : RakuInspection() {

    override fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement) {
        if (element is RakuScopedDecl) {
            val nodes = element.children
            if (nodes.isEmpty() || nodes[0] !is RakuPackageDecl) return
            val packageDeclaration = nodes[0] as RakuPackageDecl

            val scopedDeclarator: RakuScopedDecl = element

            /* Let's check for a unit declared thing with blockoid */
            val firstChild = scopedDeclarator.parent.firstChild
            if (firstChild.node.elementType !== RakuElementTypes.SCOPED_DECLARATION) return

            if (firstChild.text.startsWith("unit")) {
                var maybeBlockoid: PsiElement? = packageDeclaration.lastChild

                while (maybeBlockoid != null && (IS_WHITESPACE.test(maybeBlockoid) || IS_COMMENT.test(maybeBlockoid))) {
                    maybeBlockoid = maybeBlockoid.prevSibling
                }

                if (maybeBlockoid is RakuBlockoid) {
                    val length: Int = packageDeclaration.textLength

                    holder.registerProblem(scopedDeclarator,
                                           TextRange(0, length), // -6 = "unit ".length
                                           DESCRIPTION_BLOCK_FORM,
                                           RemoveUnitKeywordFix(firstChild.textOffset))
                }
            }
        } else if (element is RakuPackageDecl && element.lastChild is RakuStatementList) {
            /* This looks like a missing unit at the front with a semicolon
             * at the end of the line. Let's complain about the statement
             * separator if we can find it.
             */
            var maybeStatementTerminator = element.lastChild.prevSibling
            while (IS_WHITESPACE.test(maybeStatementTerminator) || IS_COMMENT.test(maybeStatementTerminator)) {
                maybeStatementTerminator = maybeStatementTerminator.prevSibling
            }

            if (maybeStatementTerminator == null) return
            if (element.parent is RakuScopedDecl && (element.parent as RakuScopedDecl).scope == "unit") return
            val node = maybeStatementTerminator.node
            if (node.elementType === RakuTokenTypes.STATEMENT_TERMINATOR) {
                val packageDeclarator = element.getFirstChild()
                var declaratorType: String? = null
                if (packageDeclarator.node.elementType === RakuTokenTypes.PACKAGE_DECLARATOR) {
                    declaratorType = packageDeclarator.text
                }
                val errorMessage = if (declaratorType == null) DESCRIPTION_DEFAULT else DESCRIPTION_FORMAT.format(declaratorType)
                holder.registerProblem(element, TextRange(0, node.startOffset - element.textRange.startOffset), errorMessage, AddUnitKeywordFix())
            }
        }
    }
}