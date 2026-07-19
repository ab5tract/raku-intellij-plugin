package org.raku.comma.inspection.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import org.raku.comma.inspection.InspectionConstants.UselessUse.DESCRIPTION
import org.raku.comma.inspection.RakuInspection
import org.raku.comma.psi.*
import org.raku.comma.psi.effects.Effect
import java.util.*

class UselessUseInspection : RakuInspection() {
    override fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement) {
        if (element is RakuStatementList
            && (element.getParent() is RakuBlockoid || element.getParent() is RakuFile)
        ) {
            val statements = getStatements(element)
            val lastSunk = isLastStatementSunk(element as RakuStatementList)
            var i = 0
            val lastElement = statements.size - (if (lastSunk) 0 else 1)
            while (i < lastElement) {
                val statement = statements[i]
                i++
                // Zero-length statements come from parser error recovery;
                // flagging them just decorates a stray brace.
                if (statement.textLength == 0) continue
                val effect = statement.inferEffects()
                if (!effect.`is`(Effect.IMPURE) && !effect.`is`(Effect.DECLARATION)) {
                    holder.registerProblem(statement, DESCRIPTION, ProblemHighlightType.WARNING)
                }
            }
        }
    }

    private fun getStatements(element: PsiElement): List<RakuStatement> {
        return Arrays.stream<PsiElement>(element.children)
                     .filter { it != null }
                     .filter { c: PsiElement -> c is RakuStatement }
                     .map    { it as RakuStatement }
                     .toList()
    }

    private fun isLastStatementSunk(statementList: RakuStatementList): Boolean {
        // Last in file will be sunk always.
        val parent = statementList.parent
        if (parent is RakuFile) return true

        // Otherwise, ensure we have a blockoid; all other cases we consider
        // as not sunk (which is conservative: it won't lead to bogus useless
        // use warnings.)
        if (parent !is RakuBlockoid) return false

        // If we're in a routine declaration, check the return type; if it's
        // Nil then we're sunk.
        val blockoidParent = parent.getParent()
        if (blockoidParent is RakuRoutineDecl) {
            val type = blockoidParent.returnType
            return type.nominalType().name == "Nil"
        }

        // Pointy blocks and normal blocks need us to look further out.
        if (blockoidParent is RakuPointyBlock || blockoidParent is RakuBlock) {
            // If it's a statement-level loop, then always sunk.
            val holder = blockoidParent.parent
            if (isLoop(holder)) {
                return holder.parent is RakuStatement
            }

            // If it's a conditional, then sunk if the conditional is both
            // statement level and itself sunk.
            if (isConditional(holder)) {
                val maybeStatement = holder.parent
                if (maybeStatement is RakuStatement) {
                    val maybeStatementList = maybeStatement.getParent()
                    if (maybeStatementList is RakuStatementList) {
                        val statements = getStatements(maybeStatementList)
                        return statements[statements.size - 1] !== maybeStatement
                                || isLastStatementSunk(maybeStatementList)
                    }
                }
                return false
            }
        }

        return false
    }

    private fun isConditional(holder: PsiElement): Boolean {
        return holder is RakuIfStatement
            || holder is RakuUnlessStatement
            || holder is RakuWithoutStatement
    }

    private fun isLoop(holder: PsiElement): Boolean {
        return holder is RakuForStatement
            || holder is RakuWhileStatement
            || holder is RakuUntilStatement
            || holder is RakuRepeatStatement
            || holder is RakuLoopStatement
    }
}