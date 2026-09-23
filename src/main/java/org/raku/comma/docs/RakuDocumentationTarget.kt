package org.raku.comma.docs

import com.intellij.model.Pointer
import com.intellij.openapi.application.readAction
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.RakuIcons
import org.raku.comma.psi.RakuMethodCall
import org.raku.comma.psi.RakuRoutineDecl
import org.raku.comma.psi.RakuSubCall
import org.raku.comma.psi.RakuSubCallName
import org.raku.comma.psi.external.RakuExternalPsiElement

class RakuDocumentationTarget(
    private val element: PsiElement,
) : DocumentationTarget {

    /**
     * A target must survive being carried across read actions.
     *
     * A RakuExternalPsiElement is synthesised in memory from the SDK symbol
     * cache: it has no file and no offsets for a smart pointer to track, so a
     * hard pointer is used instead of a smart one. That pins the pre-swap
     * element: swapping the SDK rebuilds the whole symbol cache, so a hard
     * pointer created before the swap goes on referring to the now-stale
     * element rather than noticing the swap -- RakuExternalPsiElement.isValid()
     * always returns true, so nothing here would catch it either way. That is
     * acceptable only because this target does not outlive the popup that
     * created it; there is no long-lived cache of targets for a stale one to
     * corrupt.
     */
    override fun createPointer(): Pointer<out DocumentationTarget> {
        if (element is RakuExternalPsiElement) {
            return Pointer.hardPointer(RakuDocumentationTarget(element))
        }
        val elementPointer = SmartPointerManager.createPointer(element)
        return Pointer {
            val live = elementPointer.element ?: return@Pointer null
            RakuDocumentationTarget(live)
        }
    }

    override fun computeDocumentationHint(): String? = RakuDocRendering.hintLine(element)

    // The interface default returns null, which hides "Edit Source"/F4 in the
    // Documentation tool window (DocumentationEditSourceAction.update() checks
    // getNavigatable()?.canNavigate()). The platform's own
    // PsiElementDocumentationTarget implements this; we replace that target
    // for every Raku element, so we must implement it too.
    override val navigatable: Navigatable? get() = element as? Navigatable

    override fun computePresentation(): TargetPresentation {
        var builder = TargetPresentation
            .builder(presentableText())
            .icon(RakuIcons.CAMELIA)
        RakuDocRendering.containerText(element)?.let { builder = builder.containerText(it) }
        RakuDocRendering.locationText(element)?.let { builder = builder.locationText(it) }
        return builder.presentation()
    }

    /**
     * RakuDocRendering.hintLine has no branch for RakuMethodCall/RakuSubCall,
     * so RakuDocRendering.presentableText(element) is null for every call
     * site -- exactly the two types computeDocumentation() routes to the
     * async arm below. Rather than falling straight to the call's own raw
     * source text (a whole RakuFile's text, in the worst case), resolve the
     * call the same way RakuDocRendering.docHtml already does and borrow the
     * resolved declaration's presentable text. PsiNamedElement.name is the
     * next rung -- the same one the platform's own
     * targetPresentation(PsiElement) falls to before it would otherwise log
     * an error and reach getText() -- and raw source text is the last resort,
     * never the first.
     */
    private fun presentableText(): String {
        RakuDocRendering.presentableText(element)?.let { return it }
        callTarget(element)?.let { target -> RakuDocRendering.presentableText(target)?.let { return it } }
        return (element as? PsiNamedElement)?.name ?: element.text ?: ""
    }

    /** The routine a call site's reference resolves to, or null. */
    private fun callTarget(element: PsiElement): RakuRoutineDecl? {
        val reference = when (element) {
            is RakuMethodCall -> element.reference
            is RakuSubCall -> PsiTreeUtil.findChildOfType(element, RakuSubCallName::class.java)?.reference
            else -> null
        }
        if (reference !is PsiReferenceBase.Poly<*>) return null
        return reference.multiResolve(false).firstNotNullOfOrNull { it.element as? RakuRoutineDecl }
    }

    override fun computeDocumentation(): DocumentationResult? = when (element) {
        is RakuMethodCall, is RakuSubCall -> DocumentationResult.asyncDocumentation {
            readAction { documentation() }
        }
        else -> documentation()
    }

    private fun documentation(): DocumentationResult.Documentation? {
        val html = RakuDocRendering.docHtml(element) ?: return null
        var result = DocumentationResult.documentation(html)
        // Immutable builder: assign the return value or the URL is dropped.
        RakuDocRendering.externalUrl(element)?.let { result = result.externalUrl(it) }
        return result
    }
}
