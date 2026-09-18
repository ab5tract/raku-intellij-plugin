package org.raku.comma.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.inspection.inspections.UnimportedTypeInspection

// Direct-invocation tests in the style of RedeclaredImportedSymbolInspectionTest:
// the inspection's visit function is run over the PSI and the reported problems
// asserted, without routing through checkHighlighting().
//
// testData/highlight/Base.rakumod declares `sub foo is export` and `class C`.
class UnimportedTypeInspectionTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String = "testData/highlight"

    private fun problems(code: String, vararg extraFiles: Pair<String, String>): List<ProblemDescriptor> {
        myFixture.copyFileToProject("Base.rakumod")
        for ((path, text) in extraFiles) myFixture.addFileToProject(path, text)
        myFixture.configureByText(RakuScriptFileType.INSTANCE, code)
        val inspection = UnimportedTypeInspection()
        val holder = ProblemsHolder(InspectionManager.getInstance(project), myFixture.file, false)
        myFixture.file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                inspection.provideVisitFunction(holder, element)
                super.visitElement(element)
            }
        })
        return holder.results
    }

    private fun fixNames(descriptor: ProblemDescriptor): List<String> =
        descriptor.fixes.orEmpty().map { (it as LocalQuickFix).name }

    fun testUnimportedProjectTypeFires() {
        val p = problems("my C \$c;")
        assertSize(1, p)
        assertEquals("Type C is provided by a module that is not imported", p[0].descriptionTemplate)
        assertEquals(listOf("Use module 'Base'"), fixNames(p[0]))
    }

    fun testUnimportedTypeInSignatureFires() {
        val p = problems("sub render(C \$c) { }")
        assertSize(1, p)
        assertEquals(listOf("Use module 'Base'"), fixNames(p[0]))
    }

    fun testNestedModuleNameIsDerivedFromPath() {
        val p = problems(
            "my Amazing::Mazes::Renderer \$r;",
            "Amazing/Mazes/Renderer.rakumod" to "unit class Amazing::Mazes::Renderer;\n"
        )
        assertSize(1, p)
        assertEquals(listOf("Use module 'Amazing::Mazes::Renderer'"), fixNames(p[0]))
    }

    fun testOneFixPerProvidingModule() {
        val p = problems("my C \$c;", "Other.rakumod" to "class C { }\n")
        assertSize(1, p)
        assertEquals(listOf("Use module 'Base'", "Use module 'Other'"), fixNames(p[0]))
    }

    fun testImportedTypeDoesNotFire() {
        assertEmpty(problems("use Base;\nmy C \$c;"))
    }

    fun testSameFileTypeDoesNotFire() {
        assertEmpty(problems("class D { }\nmy D \$d;"))
    }

    // Unknown names may arrive via `require` or indirect lookup; without a
    // concrete import to offer, the inspection must stay silent.
    fun testUnknownTypeDoesNotFire() {
        assertEmpty(problems("my Zzz \$z;"))
    }

    fun testCoreTypeDoesNotFire() {
        assertEmpty(problems("my Int \$n;"))
    }

    fun testQuickFixAddsUseStatement() {
        val p = problems("my C \$c;")
        assertSize(1, p)
        val fix = p[0].fixes!![0] as LocalQuickFix
        WriteCommandAction.runWriteCommandAction(project) { fix.applyFix(project, p[0]) }
        assertEquals("use Base;\nmy C \$c;", myFixture.editor.document.text)
    }
}
