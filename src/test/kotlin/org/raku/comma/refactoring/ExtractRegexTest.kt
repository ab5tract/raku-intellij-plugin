package org.raku.comma.refactoring

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.psi.RakuPsiScope

class ExtractRegexTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/regex-extract"
    }

    fun testBasic() {
        doTest("rule-case", RakuRegexPartType.RULE, false)
        doTest("basic", RakuRegexPartType.TOKEN, false)
        doTest("basic-rule", RakuRegexPartType.RULE, false)
        doTest("basic-regex-capture", RakuRegexPartType.REGEX, true)
    }

    fun testGrammar() {
        doTest("grammar", RakuRegexPartType.TOKEN, false)
        doTest("grammar-rule", RakuRegexPartType.RULE, true)
    }

    private fun doTest(filename: String, type: RakuRegexPartType, isCapture: Boolean) {
        myFixture.configureByFile("$filename-before.p6")
        val handler = RakuExtractRegexPartHandlerMock(type, isCapture)
        handler.invoke(myFixture.project, myFixture.editor, myFixture.file, null)
        checkResultByFileDumpable("$filename.p6")
    }

    private class RakuExtractRegexPartHandlerMock(
        private val myType: RakuRegexPartType,
        private val myIsCapture: Boolean,
    ) : RakuExtractRegexPartHandler() {

        override fun getNewRegexPartData(
            project: Project,
            parentToCreateAt: RakuPsiScope,
            atoms: Array<PsiElement>,
            isLexical: Boolean,
            parentType: RakuRegexPartType,
        ): NewRegexPartData {
            val params = getCapturedVariables(parentToCreateAt, atoms)
            var base = ""
            if (params.isNotEmpty()) {
                base += NewCodeBlockData.formSignature(params, false)
            }
            return NewRegexPartData(
                myType, "foo",
                if (base.isEmpty()) "" else "($base)",
                myIsCapture, isLexical, myType
            )
        }
    }
}
