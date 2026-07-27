package org.raku.comma.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.inspection.inspections.MissingRoleMethodInspection

// Direct-invocation tests for MissingRoleMethodInspection. These do not route
// through checkHighlighting()/doHighlighting(); they run the inspection's
// visit function over the PSI and assert on the reported problems.
class MissingRoleMethodInspectionTest : CommaFixtureTestCase() {

    private fun problems(code: String): List<String> {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, code)
        val inspection = MissingRoleMethodInspection()
        val holder = ProblemsHolder(InspectionManager.getInstance(project), myFixture.file, false)
        myFixture.file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                inspection.provideVisitFunction(holder, element)
                super.visitElement(element)
            }
        })
        return holder.results.map { it.descriptionTemplate }
    }

    fun testPublicStubImplementedDoesNotFire() {
        assertEmpty(problems("role R { method foo { ... } }\nclass C does R { method foo { 42 } }"))
    }

    fun testPublicStubImplementedWithEmptyBodyDoesNotFire() {
        assertEmpty(problems("role R { method foo { ... } }\nclass C does R { method foo {} }"))
    }

    fun testPublicStubNotImplementedFires() {
        val p = problems("role R { method foo { ... } }\nclass C does R { }")
        assertSize(1, p)
        assertTrue(p[0].contains("foo"))
    }

    // Raku does NOT enforce private (!-twigil) stubbed role methods as
    // composition requirements: a class may `does` a role with a stubbed
    // private method and compile fine without implementing it. The inspection
    // must therefore never flag a private role method as missing.
    fun testPrivateStubNotImplementedDoesNotFire() {
        assertEmpty(problems("role R { method !foo { ... } }\nclass C does R { }"))
    }

    fun testPrivateStubImplementedDoesNotFire() {
        assertEmpty(problems("role R { method !foo { ... } }\nclass C does R { method !foo { 42 } }"))
    }
}
