package org.raku.comma.editor;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.psi.PsiElement;
import org.raku.comma.highlighter.RakuHighlighter;
import org.raku.comma.psi.RakuRegexSigspace;
import org.jetbrains.annotations.NotNull;

public class SigSpaceAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (psiElement instanceof RakuRegexSigspace && psiElement.getTextLength() >= 1) {
            // Sigspace is a blank run, so the dotted underline is all there is
            // to see -- and an effect needs a color, which a color scheme entry
            // cannot supply without giving up the fallback for everything else.
            // Resolve it from the theme; see RakuHighlighter.effectAttributes.
            TextAttributes attributes = RakuHighlighter.effectAttributes(
                EditorColorsManager.getInstance().getGlobalScheme(),
                RakuHighlighter.REGEX_SIG_SPACE,
                EffectType.BOLD_DOTTED_LINE);
            annotationHolder.newAnnotation(HighlightSeverity.INFORMATION, "Implicit <.ws> call")
                .range(psiElement).enforcedTextAttributes(attributes).create();
        }
    }
}
