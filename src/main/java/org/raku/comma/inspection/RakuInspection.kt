package org.raku.comma.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiEditorUtil
import org.raku.comma.psi.RakuLongName
import org.raku.comma.psi.RakuRoutineDecl
import org.raku.comma.psi.external.RakuExternalPsiElement

abstract class RakuInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return RakuPsiElementVisitor(holder, wrapVisitFunction())
    }

    private fun wrapVisitFunction(): (ProblemsHolder, PsiElement) -> Unit  {
        return { innerHolder, element ->
            if (element !is RakuExternalPsiElement) {
                provideVisitFunction(innerHolder, element)
            }
        }
    }

    abstract fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement)

    protected fun customHighlight(element: PsiElement, attributesKey: TextAttributesKey) {
        val editor = PsiEditorUtil.findEditor(element) ?: return
        customHighlight(editor, element.textRange, attributesKey)
    }

    protected fun customHighlight(element: PsiElement, range: TextRange, attributesKey: TextAttributesKey) {
        val editor = PsiEditorUtil.findEditor(element) ?: return
        customHighlight(editor, range, attributesKey)
    }

    protected fun customHighlight(editor: Editor, range: TextRange, attributesKey: TextAttributesKey) {
        customHighlight(editor, range, attributesKey, HighlighterLayer.WEAK_WARNING)
    }

    protected fun customHighlight(editor: Editor, range: TextRange, attributesKey: TextAttributesKey, layer: Int) {
        editor.markupModel.addRangeHighlighter(attributesKey,
                                               range.startOffset,
                                               range.endOffset,
                                               layer,
                                               HighlighterTargetArea.EXACT_RANGE)
    }

    /**
     * For attributes resolved at apply time rather than named by a key -- see
     * [org.raku.comma.highlighter.RakuHighlighter.effectAttributes]. Unlike the
     * key overload these do not re-resolve when the user switches scheme, which
     * costs nothing here because the inspection reruns and rebuilds them.
     */
    protected fun customHighlight(editor: Editor, range: TextRange, attributes: TextAttributes, layer: Int) {
        editor.markupModel.addRangeHighlighter(range.startOffset,
                                               range.endOffset,
                                               layer,
                                               attributes,
                                               HighlighterTargetArea.EXACT_RANGE)
    }

    protected fun highlightTextRange(element: RakuRoutineDecl): TextRange {
        val start = element.declaratorNode.textOffset
        // The signature string is not always literally present in the source
        // (implicit signatures), so clamp to the declaration's own range.
        val end = (element.textOffset + element.declaratorNode.textLength + element.signature.length - 2)
            .coerceIn(start + 1, element.textRange.endOffset)
        return TextRange(start, end)
    }

    protected fun highlightTextRange(element: RakuLongName): TextRange {
        return TextRange(element.textOffset, element.textOffset + element.textRange.length)
    }

    protected fun removeHighlighters(element: PsiElement) {
        val editor = PsiEditorUtil.findEditor(element) ?: return
        editor.markupModel.allHighlighters.filter { highlighter -> highlighter.textRange == element.textRange }
                                          .forEach { highlighter -> editor.markupModel.removeHighlighter(highlighter) }
    }
}
