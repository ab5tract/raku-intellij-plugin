package org.raku.comma.inspection.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiEditorUtil
import org.raku.comma.highlighter.RakuHighlighter
import org.raku.comma.inspection.RakuInspection
import org.raku.comma.psi.PodFormatted

/**
 * Pod inline formatting codes -- and the worked example for
 * [RakuHighlighter.styledAttributes] and [RakuHighlighter.StyleEffect].
 *
 * All three follow one shape: **the key supplies the colour, the helper
 * supplies the decoration.** Each key falls back alongside
 * [RakuHighlighter.POD_TEXT], so left alone a formatted run is the colour of
 * the text around it and differs only in how it is drawn -- while anyone who
 * wants, say, bold Pod text in its own colour has a live control to do it
 * with.
 *
 * The helpers exist because neither decoration can be spelled on the key
 * itself: a fallback resolves to another key's attributes *whole*, so there
 * is no way to say "this colour, plus bold". See [RakuHighlighter.Style] for
 * why a font style has to be a second key or a computed attribute, and
 * [RakuHighlighter.StyleEffect] for why an effect can only ever be computed.
 *
 * These used to get their font style from a `colorSchemes/` `FONT_TYPE`
 * entry, which only ever worked under Default and Darcula -- a third-party
 * theme never sees our `additionalTextAttributes`, so `B<>` rendered in the
 * doc-comment colour and *not* bold, wrong twice over.
 */
class PodFormatterInspection : RakuInspection() {
    override fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement) {
        if (element !is PodFormatted) return
        val editor = PsiEditorUtil.findEditor(element) ?: return
        val range = element.formattedTextRange
        val scheme = editor.colorsScheme

        // Which key, and nothing else. What each is *drawn* as -- bold,
        // italic, an underline -- is declared beside the key in
        // RakuHighlighter, so this cannot drift out of step with it.
        val key = when (element.getFormatCode()) {
            "B" -> RakuHighlighter.POD_TEXT_BOLD
            "I" -> RakuHighlighter.POD_TEXT_ITALIC
            "U" -> RakuHighlighter.POD_TEXT_UNDERLINE
            else -> return
        }

        customHighlight(
            editor,
            range,
            RakuHighlighter.decoratedAttributes(scheme, key),
            HighlighterLayer.SYNTAX,
        )
    }
}
