package org.raku.comma.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.inspection.inspections.RedeclaredImportedSymbolInspection

// Direct-invocation tests for RedeclaredImportedSymbolInspection, in the style
// of MissingRoleMethodInspectionTest: the inspection's visit function is run
// over the PSI and the reported problems asserted, without routing through
// checkHighlighting(). The end-to-end highlighting case is covered by
// RakuHighlightTest.testDuplicatesInExternal, which uses the same Base.rakumod.
//
// testData/highlight/Base.rakumod declares `sub foo is export` and `class C`.
class RedeclaredImportedSymbolInspectionTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String = "testData/highlight"

    private fun problems(code: String): List<String> {
        myFixture.copyFileToProject("Base.rakumod")
        myFixture.configureByText(RakuScriptFileType.INSTANCE, code)
        val inspection = RedeclaredImportedSymbolInspection()
        val holder = ProblemsHolder(InspectionManager.getInstance(project), myFixture.file, false)
        myFixture.file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                inspection.provideVisitFunction(holder, element)
                super.visitElement(element)
            }
        })
        return holder.results.map { it.descriptionTemplate }
    }

    fun testRedeclaredImportedRoutineFires() {
        val p = problems("use Base;\nsub foo { }")
        assertSize(1, p)
        assertEquals("Re-declaration of foo from Base.rakumod:1", p[0])
    }

    fun testRedeclaredImportedPackageFires() {
        val p = problems("use Base;\nclass C { }")
        assertSize(1, p)
        assertEquals("Re-declaration of C from Base.rakumod:3", p[0])
    }

    // Adding a candidate to an imported routine is the supported thing to do --
    // it is literally what Rakudo's own error message suggests.
    fun testMultiCandidateDoesNotFire() {
        assertEmpty(problems("use Base;\nmulti sub foo(Int \$n) { \$n }"))
    }

    fun testUnrelatedNameDoesNotFire() {
        assertEmpty(problems("use Base;\nsub bar { }"))
    }

    // In-file duplicates are RakuHighlightVisitor's job; this inspection must
    // not double-report them.
    fun testSameFileDuplicateDoesNotFire() {
        assertEmpty(problems("sub baz { }\nsub baz { }"))
    }

    fun testNoUseStatementDoesNotFire() {
        assertEmpty(problems("sub foo { }\nclass C { }"))
    }
}
