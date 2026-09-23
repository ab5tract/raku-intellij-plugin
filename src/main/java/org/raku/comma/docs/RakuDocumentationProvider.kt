package org.raku.comma.docs

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

class RakuDocumentationProvider : DocumentationProvider {

    @Synchronized
    override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement?): String? =
        RakuDocRendering.hintLine(element)

    override fun getUrlFor(element: PsiElement, originalElement: PsiElement?): List<String>? =
        RakuDocRendering.externalUrl(element)?.let { listOf(it) }

    @Synchronized
    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? =
        RakuDocRendering.docHtml(element)

    override fun getDocumentationElementForLookupItem(psiManager: PsiManager, `object`: Any?, element: PsiElement?): PsiElement? = null

    override fun getDocumentationElementForLink(psiManager: PsiManager, link: String, context: PsiElement?): PsiElement? = null
}
