package org.raku.comma.editor

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessors
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.RakuLanguage

class RakuSmartEnterTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/smartEnter"
    }

    private fun doTest() {
        myFixture.configureByFile(getTestName(false) + ".p6")
        val processors = SmartEnterProcessors.INSTANCE.forKey(RakuLanguage.INSTANCE)
        WriteCommandAction.writeCommandAction(myFixture.project).run<RuntimeException> {
            val editor = myFixture.editor
            for (processor in processors) {
                processor.process(myFixture.project, editor, myFixture.file)
            }
        }
        checkResultByFileDumpable(getTestName(false) + "_after.p6")
    }

    fun testStatementAfterCall() = doTest()
    fun testStatementAfterCallWithSemicolon() = doTest()
    fun testMultilineStatement() = doTest()
    fun testMultilineStatementWithSemicolon() = doTest()
    fun testClassAfterName() = doTest()
    fun testPackageAfterName() = doTest()
    fun testClassAfterTrait() = doTest()
    fun testClassAfterWhiteSpace() = doTest()
    fun testRoleAfterSignature() = doTest()
    fun testClassAfterBadCharacter() = doTest()
    fun testClassAfterSemiBlockoid() = doTest()
    fun testClassAfterBlockoid() = doTest()
    fun testScopedClass() = doTest()
    fun testUnit() = doTest()
    fun testRoutineSignature() = doTest()
    fun testRoutineOutsideSignature() = doTest()
    fun testWhitespaceIsNotCaptured() = doTest()
    fun testIfHandling() = doTest()
    fun testElseHandling() = doTest()
    fun testWith() = doTest()
    fun testOrWith() = doTest()
    fun testUnless() = doTest()
    fun testFor() = doTest()
    fun testFor2() = doTest()
    fun testGiven() = doTest()
    fun testGivenWhen() = doTest()
    fun testLoopCondition() = doTest()
    fun testLoopOutside() = doTest()
    fun testInsideOfBlock() = doTest()
    fun testLoopInsideOfBlock() = doTest()
}
