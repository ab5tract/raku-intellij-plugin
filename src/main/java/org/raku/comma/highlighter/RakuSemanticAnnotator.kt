package org.raku.comma.highlighter

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.raku.comma.psi.RakuInfixApplication
import org.raku.comma.psi.RakuMethodCall
import org.raku.comma.psi.RakuParameterVariable
import org.raku.comma.psi.RakuPostfixApplication
import org.raku.comma.psi.RakuPrefixApplication
import org.raku.comma.psi.RakuSubCallName
import org.raku.comma.psi.RakuVariable
import org.raku.comma.psi.RakuVariableDecl
import org.raku.comma.psi.external.RakuExternalPsiElement

// Resolution-based (semantic) highlighting layered on top of the plain lexer
// highlighting from RakuSyntaxHighlighter -- distinguishing references that
// resolve to a built-in/external symbol (CORE.setting or an imported module;
// v1 doesn't split those two apart, see RakuExternalPsiElement) from ordinary
// user-declared ones, and variables that get reassigned somewhere within
// their scope (matching Java/Kotlin's REASSIGNED_LOCAL_VARIABLE convention:
// the declaration token itself isn't marked, only subsequent usages are).
//
// Known v1 gap: dynamic (twigil-*) variables like $*OUT/%*ENV resolve through
// RakuVariableReference's separate DYNAMIC_VARIABLES stub index rather than
// the CORE.setting RakuExternalPsiElement path, so they aren't flagged as
// builtins here. Most builtins (routines, methods, $_/$!/$/ and friends) go
// through the normal lexical/external path and are covered.
class RakuSemanticAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is RakuVariable -> annotateVariable(element, holder)
            is RakuSubCallName -> annotateResolvable(element, element, RakuHighlighter.BUILTIN_CALL, holder)
            is RakuMethodCall -> annotateResolvable(element, element.simpleName, RakuHighlighter.BUILTIN_CALL, holder)
        }
    }

    private fun annotateVariable(variable: RakuVariable, holder: AnnotationHolder) {
        // Declaration sites (`my $x`, a signature parameter) aren't "a reference
        // to a built-in" or "an assignment target of themselves" -- only usages
        // are annotated.
        val parent = variable.parent
        if (parent is RakuVariableDecl || parent is RakuParameterVariable) return

        val reference = variable.reference as? PsiPolyVariantReference ?: return
        val results = reference.multiResolve(false)
        if (results.isEmpty()) return

        if (results.all { it.element is RakuExternalPsiElement }) {
            annotate(variable, RakuHighlighter.BUILTIN_VARIABLE, holder)
            return
        }

        // Local-variable resolution isn't multi-dispatch like routine calls are;
        // an ambiguous result here isn't a case this annotator has an opinion on.
        if (results.size != 1) return
        when (val declaration = results[0].element) {
            is RakuVariableDecl ->
                if (isReassignedInScope(declaration)) annotate(variable, RakuHighlighter.REASSIGNED_LOCAL_VARIABLE, holder)
            is RakuParameterVariable ->
                if (isReassignedInScope(declaration)) annotate(variable, RakuHighlighter.REASSIGNED_PARAMETER, holder)
        }
    }

    private fun annotateResolvable(referenceOwner: PsiElement, range: PsiElement, key: TextAttributesKey, holder: AnnotationHolder) {
        // Many CORE.setting routines (say, print, ...) are multi-dispatch, so a
        // plain PsiReference.resolve() -- which only succeeds for a single
        // unambiguous candidate -- comes back null for exactly the most common
        // builtins. Go through multiResolve() and require every candidate to be
        // external, so a user overload sharing a name with a builtin isn't
        // mislabeled.
        val reference = referenceOwner.reference as? PsiPolyVariantReference ?: return
        val results = reference.multiResolve(false)
        if (results.isEmpty()) return
        if (results.all { it.element is RakuExternalPsiElement }) {
            annotate(range, key, holder)
        }
    }

    private fun annotate(range: PsiElement, key: TextAttributesKey, holder: AnnotationHolder) {
        holder.newAnnotation(HighlightSeverity.INFORMATION, "Resolves to a built-in, imported symbol, or reassigned variable")
            .range(range).textAttributes(key).create()
    }

    // Cached per declaration (not per usage) since it's the same expensive
    // question -- "is $x reassigned anywhere in its scope" -- asked again for
    // every one of $x's usages the annotator visits. Coarse invalidation on any
    // PSI change is fine given how small Raku files typically are.
    private fun isReassignedInScope(declaration: PsiElement): Boolean {
        return CachedValuesManager.getCachedValue(declaration) {
            val reassigned = ReferencesSearch.search(declaration, declaration.useScope)
                .findAll().any { isAssignmentTarget(it.element) }
            CachedValueProvider.Result.create(reassigned, PsiModificationTracker.MODIFICATION_COUNT)
        }
    }

    private fun isAssignmentTarget(usage: PsiElement): Boolean {
        return when (val parent = usage.parent) {
            is RakuInfixApplication -> parent.isAssignish && parent.operands.firstOrNull() == usage
            is RakuPrefixApplication -> parent.isAssignish && parent.operand == usage
            is RakuPostfixApplication -> parent.isAssignish && parent.operand == usage
            else -> false
        }
    }
}
