package org.raku.comma.editor

import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.formatter.RakuCodeStyleSettings

class UnicodeReplacementHandlerTest : CommaFixtureTestCase() {
    fun testElem() {
        doTest("1 (elem<caret>", ')', "1 ∈")
    }

    fun testElemInComment() {
        doTest("# 1 (elem", ')', "# 1 (elem)")
    }

    fun testCont() {
        doTest("1 (cont", ')', "1 ∋")
    }

    fun testExclusion() {
        doTest("1 (-", ')', "1 ∖")
    }

    fun testComposition() {
        doTest("1 ", 'o', "1 ∘")
    }

    fun testAnd() {
        doTest("1 (&", ')', "1 ∩")
    }

    fun testOr() {
        doTest("1 (|", ')', "1 ∪")
    }

    fun testApproximatelyEqual() {
        doTest("1 =~", '=', "1 ≅")
    }

    fun testIncludes1() {
        doTest("1 (<", ')', "1 ⊂")
    }

    fun testIncludes2() {
        doTest("1 (>", ')', "1 ⊃")
    }

    fun testIncludesOrEqual1() {
        doTest("1 (<=", ')', "1 ⊆")
    }

    fun testIncludesOrEqual2() {
        doTest("1 (>=", ')', "1 ⊇")
    }

    fun testMultisetMultiplication() {
        doTest("1 (.", ')', "1 ⊍")
    }

    fun testMultisetAddition() {
        doTest("1 (+", ')', "1 ⊎")
    }

    fun testMultisetDifference() {
        doTest("1 (^", ')', "1 ⊖")
    }

    fun testNotElem() {
        doTest("1 !(elem", ')', "1 ∉")
    }

    fun testNotCont() {
        doTest("1 !(cont", ')', "1 ∌")
    }

    fun testNotIncluded() {
        doTest("1 !(<", ')', "1 ⊄")
    }

    fun testNotIncludes() {
        doTest("1 !(>", ')', "1 ⊅")
    }

    fun testNotIncludedOrEqual() {
        doTest("1 !(<=", ')', "1 ⊈")
    }

    fun testNotIncludesOrEqual() {
        doTest("1 !(>=", ')', "1 ⊉")
    }

    private fun doTest(source: String, type: Char, result: String) {
        CodeStyleSettingsManager.getInstance(getProject()).getMainProjectCodeStyle()!!.getCustomSettings(RakuCodeStyleSettings::class.java).CONVERT_TO_UNICODE = true
        myFixture.configureByText(RakuScriptFileType.INSTANCE, source)
        myFixture.getEditor().getCaretModel().moveToOffset(myFixture.getEditor().getDocument().getTextLength())
        myFixture.type(type)
        myFixture.checkResult(result)
        CodeStyleSettingsManager.getInstance(getProject()).getMainProjectCodeStyle()!!.getCustomSettings(RakuCodeStyleSettings::class.java).CONVERT_TO_UNICODE = false
    }
}
