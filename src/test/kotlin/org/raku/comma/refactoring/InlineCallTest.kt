package org.raku.comma.refactoring

import com.intellij.refactoring.util.CommonRefactoringUtil
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.refactoring.inline.call.RakuInlineCallActionHandler

class InlineCallTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/inline-call"
    }

    fun testSingleStatementBlockInlineSave() {
        doTest()
    }

    fun testSingleStatementBlockInlineNoSave() {
        doTest()
    }

    fun testMultiStatementBlockInlineSave() {
        doTest()
    }

    fun testMultiStatementBlockInlineNoSave() {
        doTest()
    }

    fun testSingleReturnMulticallSave() {
        doTest()
    }

    fun testSingleReturnMulticallNoSave() {
        doTest()
    }

    fun testPositionalArgumentSave() {
        doTest()
    }

    fun testPositionalArgumentNoSave() {
        doTest()
    }

    fun testPositionalArgumentsSave() {
        doTest()
    }

    fun testPositionalArgumentsNoSave() {
        doTest()
    }

    fun testSingleNamedArgumentSave() {
        doTest()
    }

    fun testSingleNamedArgumentNoSave() {
        doTest()
    }

    fun testNamedArgumentsSave() {
        doTest()
    }

    fun testNamedArgumentsNoSave() {
        doTest()
    }

    fun testNamedArgumentsReverseSave() {
        doTest()
    }

    fun testNamedArgumentsReverseNoSave() {
        doTest()
    }

    fun testAcceptIncompleteCallWithDefault() {
        doTest()
    }

    fun testExpressionInCallNoSave() {
        doTest()
    }

    fun testSimpleMethodInlineSave() {
        doTest()
    }

    fun testSimpleMethodInlineNoSave() {
        doTest()
    }

    fun testExpressionsAreParenthesised() {
        doTest()
    }

    fun testLiteralIsNotParenthesised() {
        doTest()
    }

    fun testEmptyRoutineIsReplacedWithNil() {
        doTest()
    }

    fun testCannotInlineProto() {
        assertThrows(
            CommonRefactoringUtil.RefactoringErrorHintException::class.java,
            "Cannot perform inline refactoring: inlining of routine with proto is not supported", this::doTest)
    }

    fun testCannotInlineMulti() {
        assertThrows(
            CommonRefactoringUtil.RefactoringErrorHintException::class.java,
            "Cannot perform inline refactoring: inlining of routine with multi is not supported", this::doTest)
    }

    fun testCannotInlineMultipleReturns() {
        assertThrows(
            CommonRefactoringUtil.RefactoringErrorHintException::class.java,
            "Cannot perform inline refactoring: return statement interrupts the execution flow", this::doTest)
    }

    fun testCannotInlineNonTrailingReturn() {
        assertThrows(
            CommonRefactoringUtil.RefactoringErrorHintException::class.java,
            "Cannot perform inline refactoring: return statement interrupts the execution flow", this::doTest)
    }

    fun testStateVariableFails() {
        assertThrows(
            CommonRefactoringUtil.RefactoringErrorHintException::class.java,
            "Cannot perform inline refactoring: state variables are present", this::doTest)
    }

    fun testCannotInlineWhenLexicalsAreUnavailable() {
        assertThrows(
            CommonRefactoringUtil.RefactoringErrorHintException::class.java,
            "Cannot perform inline refactoring: lexical is used in original code that are not available at inlining location", this::doTest)
    }

    fun testCannotInlineMethodFromOtherClassWithSelf() {
        assertThrows(
            CommonRefactoringUtil.RefactoringErrorHintException::class.java,
            "Cannot perform inline refactoring: a reference to `self` is found, but caller and callee are in different classes", this::doTest)
    }

    fun testCannotInlineMethodFromOtherClassWithPrivateMethod() {
        assertThrows(
            CommonRefactoringUtil.RefactoringErrorHintException::class.java,
            "Cannot perform inline refactoring: a reference to `self` is found, but caller and callee are in different classes", this::doTest)
    }

    fun testCannotInlineMethodFromOtherClassWithAttribute() {
        assertThrows(
            CommonRefactoringUtil.RefactoringErrorHintException::class.java,
            "Cannot perform inline refactoring: attributes of class are used that are not available at inlining location", this::doTest)
    }

    fun testCannotInlineWithNameShadowed() {
        assertThrows(
            CommonRefactoringUtil.RefactoringErrorHintException::class.java,
            "Cannot perform inline refactoring: element from original code is shadowed by another one at inlining location", this::doTest)
    }

    private fun doTest() {
        myFixture.configureByFile(getTestName(true) + "Before.p6")
        val action = RakuInlineCallActionHandler()
        action.inlineElement(getProject(), myFixture.getEditor(), myFixture.getElementAtCaret())
        myFixture.checkResultByFile(getTestName(true) + ".p6")
    }
}
