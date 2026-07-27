package org.raku.comma.highlighting

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.psi.RakuInfixApplication
import org.raku.comma.psi.RakuMethodCall
import org.raku.comma.psi.RakuPostfixApplication
import org.raku.comma.psi.RakuPrefixApplication
import org.raku.comma.psi.RakuSubCallName
import org.raku.comma.psi.RakuVariable
import org.raku.comma.psi.RakuVariableDecl
import org.raku.comma.psi.RakuParameterVariable
import org.raku.comma.psi.external.RakuExternalPsiElement

// RakuSemanticAnnotator itself is exercised through the full highlighting
// pipeline in the running IDE (see manual verification in the implementation
// plan) -- these tests instead pin the resolution logic it depends on
// directly, since routing through myFixture.doHighlighting()/checkHighlighting()
// drags in every registered highlighting pass across every bundled plugin
// (including an unrelated, currently-broken Kotlin CodeVision extension) and
// fails the whole test on unrelated noise.
//
// No positive "builtin variable resolves externally" test: CORE.setting's
// dynamic (twigil-*) variables like $*OUT/%*ENV resolve through a separate,
// project-only stub index (RakuDynamicVariablesStubIndex) that CORE.setting's
// synthetic symbols never populate, so they don't resolve to anything at all
// today -- a pre-existing gap unrelated to this annotator. The topic variable
// $_ also doesn't resolve without additional binding context. BUILTIN_VARIABLE
// shares its detection logic with BUILTIN_CALL (covered below) and will fire
// correctly for whatever does resolve to a RakuExternalPsiElement; real-world
// coverage is worth eyeballing in runIde rather than forcing a synthetic case.
class RakuSemanticHighlightTest : CommaFixtureTestCase() {
    private fun configure(text: String) = myFixture.configureByText(RakuScriptFileType.INSTANCE, text)

    // Mirrors RakuSemanticAnnotator.annotateResolvable's "all candidates external" rule.
    private fun resolvesExternally(element: com.intellij.psi.PsiElement): Boolean {
        val reference = element.reference as? PsiPolyVariantReference ?: return false
        val results = reference.multiResolve(false)
        return results.isNotEmpty() && results.all { it.element is RakuExternalPsiElement }
    }

    fun testBuiltinCallResolvesExternally() {
        configure("say 42;")
        val callName = PsiTreeUtil.findChildOfType(myFixture.file, RakuSubCallName::class.java)!!
        assertTrue(resolvesExternally(callName))
    }

    fun testUserDeclaredCallDoesNotResolveExternally() {
        configure("sub mine { 42 }; mine;")
        val callNames = PsiTreeUtil.findChildrenOfType(myFixture.file, RakuSubCallName::class.java)
        val usage = callNames.first { it.callName == "mine" }
        assertFalse(resolvesExternally(usage))
    }

    fun testBuiltinMethodCallResolvesExternally() {
        configure("42.Str;")
        val call = PsiTreeUtil.findChildOfType(myFixture.file, RakuMethodCall::class.java)!!
        assertTrue(resolvesExternally(call))
    }

    fun testUserDeclaredVariableDoesNotResolveExternally() {
        configure("my \$x = 1; say \$x;")
        val variables = PsiTreeUtil.findChildrenOfType(myFixture.file, RakuVariable::class.java)
        val usage = variables.first { it.parent !is RakuVariableDecl && it.parent !is RakuParameterVariable }
        assertFalse(resolvesExternally(usage))
    }

    // Mirrors RakuSemanticAnnotator.isAssignmentTarget/isReassignedInScope (minus caching,
    // irrelevant for a one-shot test).
    private fun isAssignmentTarget(usage: PsiElement): Boolean = when (val parent = usage.parent) {
        is RakuInfixApplication -> parent.isAssignish && parent.operands.firstOrNull() == usage
        is RakuPrefixApplication -> parent.isAssignish && parent.operand == usage
        is RakuPostfixApplication -> parent.isAssignish && parent.operand == usage
        else -> false
    }

    private fun isReassignedInScope(declaration: PsiElement): Boolean =
        ReferencesSearch.search(declaration, declaration.useScope).findAll().any { isAssignmentTarget(it.element) }

    private fun declarationOfFirstUsage(): PsiElement {
        val variables = PsiTreeUtil.findChildrenOfType(myFixture.file, RakuVariable::class.java)
        val usage = variables.first { it.parent !is RakuVariableDecl && it.parent !is RakuParameterVariable }
        return (usage.reference as PsiPolyVariantReference).multiResolve(false).single().element!!
    }

    fun testReassignedVariableIsDetected() {
        configure("my \$x = 1; \$x = 2; say \$x;")
        assertTrue(isReassignedInScope(declarationOfFirstUsage()))
    }

    fun testNeverReassignedVariableIsNotDetected() {
        configure("my \$x = 1; say \$x;")
        assertFalse(isReassignedInScope(declarationOfFirstUsage()))
    }

    fun testCompoundAssignmentCountsAsReassignment() {
        configure("my \$x = 1; \$x += 1; say \$x;")
        assertTrue(isReassignedInScope(declarationOfFirstUsage()))
    }

    fun testIncrementCountsAsReassignment() {
        configure("my \$x = 1; \$x++; say \$x;")
        assertTrue(isReassignedInScope(declarationOfFirstUsage()))
    }

    fun testNestedScopeReassignmentIsDetected() {
        configure("my \$x = 1; { \$x = 2; }; say \$x;")
        assertTrue(isReassignedInScope(declarationOfFirstUsage()))
    }

    fun testParameterReassignmentIsDetected() {
        configure("sub f(\$x is rw) { \$x = 2; say \$x; }")
        val parameters = PsiTreeUtil.findChildrenOfType(myFixture.file, RakuVariable::class.java)
            .filter { it.parent is RakuParameterVariable }
        val paramDecl = (parameters.first().parent) as RakuParameterVariable
        assertTrue(isReassignedInScope(paramDecl))
    }
}
