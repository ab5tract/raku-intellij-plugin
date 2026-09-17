package org.raku.comma.stub

import com.intellij.psi.impl.source.PsiFileImpl
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuModuleFileType
import org.raku.comma.psi.RakuFile
import org.raku.comma.psi.stub.RakuRoutineDeclStub

class ConditionalBranchStubTest : CommaFixtureTestCase() {

    fun testJvmOnlyRoutineIsStubbed() {
        myFixture.configureByText(
            RakuModuleFileType.INSTANCE,
            "sub shared-sub() { }\n#?if jvm\nsub jvm-only() { }\n#?endif\n"
        )
        val stubTree = (myFixture.file as PsiFileImpl).calcStubTree()
        val routineNames = stubTree.plainList
            .mapNotNull { it as? RakuRoutineDeclStub }
            .map { it.getRoutineName() }
        assertTrue("expected jvm-only in $routineNames", routineNames.contains("jvm-only"))
        assertTrue(routineNames.contains("shared-sub"))
    }

    fun testJvmOnlyDeclarationIsInFileDeclarations() {
        myFixture.configureByText(
            RakuModuleFileType.INSTANCE,
            "#?if jvm\nsub jvm-only() { }\n#?endif\n"
        )
        val declarations = (myFixture.file as RakuFile).getDeclarations()
        assertTrue(declarations.any { it.name == "jvm-only" })
    }
}
