package org.raku.comma.inspection.fixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiEditorUtil
import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.psi.RakuPostfixApplication

class UseWithSyntaxFix(replaced: String, private val start: Int, private val end: Int) : LocalQuickFix {

    private val replacer = getReplacer(replaced)

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        // The problem anchors on the conditional statement; dig out the
        // `foo.defined` postfix application it reported. The reported range
        // runs from the branch keyword through its condition, so the right
        // condition is the one ending exactly at the range end (a `<=` match
        // would pick an earlier branch's condition in an if/elsif chain).
        val rangeEnd = descriptor.psiElement.textRange.startOffset + descriptor.textRangeInElement.endOffset
        val condition = descriptor.psiElement as? RakuPostfixApplication
            ?: PsiTreeUtil.findChildrenOfType(descriptor.psiElement, RakuPostfixApplication::class.java)
                   .firstOrNull { it.textRange.endOffset == rangeEnd }
            ?: return
        val editor = PsiEditorUtil.findEditor(descriptor.psiElement) ?: return

        // Both edits go through the document inside one command, applying the
        // later range first so the earlier offsets stay valid; the old
        // PSI-delete-then-document-replace order corrupted multi-branch
        // conditionals.
        val definedRange = condition.lastChild.textRange
        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.deleteString(definedRange.startOffset, definedRange.endOffset)
            editor.document.replaceString(start, end, replacer)
        }
    }

    private fun getReplacer(text: String): String {
        return when (text) {
            "if" -> "with"
            "elsif" -> "orwith"
            "unless" -> "without"
            else -> "without"
        }
    }

    override fun getName(): String {
        return "Use '%s' syntax construction".format(replacer)
    }

    override fun getFamilyName(): String {
        return "Use equivalent 'with'-style construction"
    }
}