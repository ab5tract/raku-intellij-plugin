package org.raku.comma.highlighter

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import org.raku.comma.psi.RakuMethodCall
import org.raku.comma.psi.RakuParameterVariable
import org.raku.comma.psi.RakuSubCallName
import org.raku.comma.psi.RakuVariable
import org.raku.comma.psi.RakuVariableDecl
import org.raku.comma.psi.external.RakuExternalPsiElement

// Resolution-based (semantic) highlighting layered on top of the plain lexer
// highlighting from RakuSyntaxHighlighter -- distinguishing references that
// resolve to a built-in/external symbol (CORE.setting or an imported module;
// v1 doesn't split those two apart, see RakuExternalPsiElement) from ordinary
// user-declared ones.
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
        // to a built-in" -- only usages can resolve to one.
        val parent = variable.parent
        if (parent is RakuVariableDecl || parent is RakuParameterVariable) return

        annotateResolvable(variable, variable, RakuHighlighter.BUILTIN_VARIABLE, holder)
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
            holder.newAnnotation(HighlightSeverity.INFORMATION, "Resolves to a built-in or imported symbol")
                .range(range).textAttributes(key).create()
        }
    }
}
