package org.raku.comma.docs

import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.psi.PsiElement
import org.raku.comma.RakuLanguage

/**
 * Unlike lang.documentationProvider, psiTargetProvider is not language-filtered
 * -- the platform calls it for elements of every language -- so the language
 * check is ours to make.
 */
class RakuPsiDocumentationTargetProvider : PsiDocumentationTargetProvider {
    override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
        if (element.language != RakuLanguage.INSTANCE) return null
        return RakuDocumentationTarget(element, originalElement)
    }
}
