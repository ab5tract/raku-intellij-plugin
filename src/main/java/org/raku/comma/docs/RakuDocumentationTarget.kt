package org.raku.comma.docs

import com.intellij.model.Pointer
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import org.raku.comma.RakuIcons
import org.raku.comma.psi.external.RakuExternalPsiElement

class RakuDocumentationTarget(
    private val element: PsiElement,
    private val originalElement: PsiElement?,
) : DocumentationTarget {

    /**
     * A target must survive being carried across read actions.
     *
     * A RakuExternalPsiElement is synthesised in memory from the SDK symbol
     * cache: it has no file and no offsets for a smart pointer to track, so a
     * hard pointer is used for `element` instead. `originalElement`, though,
     * is typically a live, file-backed PSI element with no such guarantee --
     * it is not immutable and never goes through SmartPointerManager here, so
     * pinning it directly inside a hard pointer would be wrong. It is dropped
     * (rebuilt as null) because no RakuDocRendering function ever reads it;
     * only `element` needs to survive. Swapping the SDK rebuilds the whole
     * cache and discards this target with it either way.
     */
    override fun createPointer(): Pointer<out DocumentationTarget> {
        if (element is RakuExternalPsiElement) {
            return Pointer.hardPointer(RakuDocumentationTarget(element, null))
        }
        val elementPointer = SmartPointerManager.createPointer(element)
        val originalPointer = originalElement?.let { SmartPointerManager.createPointer(it) }
        return Pointer {
            val live = elementPointer.element ?: return@Pointer null
            RakuDocumentationTarget(live, originalPointer?.element)
        }
    }

    override fun computeDocumentationHint(): String? = RakuDocRendering.hintLine(element)

    override fun computePresentation(): TargetPresentation {
        var builder = TargetPresentation
            .builder(RakuDocRendering.presentableText(element) ?: element.text ?: "")
            .icon(RakuIcons.CAMELIA)
        RakuDocRendering.containerText(element)?.let { builder = builder.containerText(it) }
        RakuDocRendering.locationText(element)?.let { builder = builder.locationText(it) }
        return builder.presentation()
    }

    override fun computeDocumentation(): DocumentationResult? {
        val html = RakuDocRendering.docHtml(element) ?: return null
        var result = DocumentationResult.documentation(html)
        RakuDocRendering.externalUrl(element)?.let { result = result.externalUrl(it) }
        return result
    }
}
