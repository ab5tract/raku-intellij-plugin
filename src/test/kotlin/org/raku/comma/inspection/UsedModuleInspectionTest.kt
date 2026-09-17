package org.raku.comma.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.inspection.inspections.UsedModuleInspection

// Direct-invocation tests for UsedModuleInspection, in the style of
// MissingRoleMethodInspectionTest. The dependency service is never initialized
// in these tests, which is exactly the state in which pragmas and preinstalled
// modules used to be flagged with the META6.json warning.
class UsedModuleInspectionTest : CommaFixtureTestCase() {

    private fun problems(code: String): List<String> {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, code)
        val inspection = UsedModuleInspection()
        val holder = ProblemsHolder(InspectionManager.getInstance(project), myFixture.file, false)
        myFixture.file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                inspection.provideVisitFunction(holder, element)
                super.visitElement(element)
            }
        })
        return holder.results.map { it.descriptionTemplate }
    }

    fun testUseNqpDoesNotFire() {
        assertEmpty(problems("use nqp;"))
    }

    fun testUseExperimentalDoesNotFire() {
        assertEmpty(problems("use experimental :rakuast;"))
    }

    fun testUsePreinstalledModuleDoesNotFire() {
        assertEmpty(problems("use NativeCall;"))
    }

    fun testUseLateBoundModuleDoesNotFire() {
        assertEmpty(problems("use ::('Late::Bound');"))
    }

    fun testUnknownModuleStillFires() {
        val p = problems("use Totally::Unknown::Module;")
        assertSize(1, p)
        assertTrue(p[0].contains("Totally::Unknown::Module"))
    }
}
