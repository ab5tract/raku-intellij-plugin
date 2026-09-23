package org.raku.comma.inspection.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiEditorUtil
import org.raku.comma.highlighter.RakuHighlighter
import org.raku.comma.inspection.RakuInspection
import org.raku.comma.psi.PodFormatted

class PodFormatterInspection : RakuInspection() {
    override fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement) {
        if (element !is PodFormatted) return
        val editor = PsiEditorUtil.findEditor(element) ?: return
        val range = element.formattedTextRange

        when (element.getFormatCode()) {
            // B<> and I<> carry font style only, so they compose over the Pod
            // text color the lexer already applied.
            "B" -> customHighlight(editor, range, RakuHighlighter.POD_TEXT_BOLD, HighlighterLayer.SYNTAX)
            "I" -> customHighlight(editor, range, RakuHighlighter.POD_TEXT_ITALIC, HighlighterLayer.SYNTAX)
            // U<> cannot: an underline needs a color, and a color scheme entry
            // that names one stops inheriting everything else. Resolve it from
            // the theme instead -- see RakuHighlighter.effectAttributes.
            "U" -> customHighlight(
                editor,
                range,
                RakuHighlighter.effectAttributes(
                    editor.colorsScheme,
                    RakuHighlighter.POD_TEXT_UNDERLINE,
                    EffectType.LINE_UNDERSCORE,
                ),
                HighlighterLayer.SYNTAX,
            )
        }
    }
}
