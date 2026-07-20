package org.raku.comma.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.LightProjectDescriptor
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.RakuMultiModuleProjectDescriptor

/**
 * Characterization tests for the symbol-contribution walks in RakuFileImpl /
 * RakuPackageDeclImpl, written ahead of their unification + Kotlin conversion.
 * These exercise the stub branch of contributeGlobals: the dependency module
 * is indexed, not open in an editor.
 */
class WalkContributionTest : CommaFixtureTestCase() {
    override fun getProjectDescriptor(): LightProjectDescriptor {
        return RakuMultiModuleProjectDescriptor()
    }

    override fun getTestDataPath(): String {
        return "testData/multi-module"
    }

    private fun lookupsFor(text: String): List<String>? {
        myFixture.copyFileToProject("Module/Walks.pm6", "../lib/Module/Walks.pm6")
        myFixture.configureByText("10-walk.t", text)
        myFixture.complete(CompletionType.BASIC, 1)
        return myFixture.getLookupElementStrings()
    }

    fun testUseContributesOurAndExportedSubs() {
        val vars = lookupsFor("use Module::Walks; walk-<caret>")!!
        assertContainsElements(vars, "walk-our-sub", "walk-our-other", "walk-exported")
        assertDoesntContain(vars, "walk-hidden")
    }

    fun testNeedContributesGlobals() {
        val vars = lookupsFor("need Module::Walks; walk-<caret>")!!
        assertContainsElements(vars, "walk-our-sub", "walk-our-other")
    }

    fun testUseContributesNestedPackagesWithPrefix() {
        val vars = lookupsFor("use Module::Walks; my Module::Walks::Walk<caret>")!!
        assertContainsElements(vars, "Module::Walks::Walker", "Module::Walks::Walkest")
    }

    fun testExportedNonOurEnumContributedFromStubbedDependency() {
        // Pins the stub-branch gate (isExported() || our) that the unified walk
        // adopts for both lenses; the AST branch historically required "our".
        val vars = lookupsFor("use Module::Walks; my WalkCol<caret>")
        // Single exact match auto-inserts and returns null lookups.
        if (vars != null) {
            assertContainsElements(vars, "WalkColor")
        } else {
            assertTrue(myFixture.editor.document.text.contains("WalkColor"))
        }
    }

    fun testSubExportModuleColdContributesNothing() {
        // sub EXPORT symbols come from an external raku run; with a cold cache
        // the current walk contributes nothing synchronously (and the stub
        // branch does not trigger EXPORT at all). Pin the absence; Commit C of
        // the walk redesign strengthens this into an async-completion test.
        myFixture.copyFileToProject("Module/WalkExport.pm6", "../lib/Module/WalkExport.pm6")
        myFixture.configureByText("11-walk.t", "use Module::WalkExport; walk-ex<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()
        assertTrue(vars == null || "walk-export-sub" !in vars)
    }
}
