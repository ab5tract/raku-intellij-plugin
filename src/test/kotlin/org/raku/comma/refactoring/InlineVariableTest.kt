package org.raku.comma.refactoring

import com.intellij.refactoring.util.CommonRefactoringUtil
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.refactoring.inline.variable.RakuInlineVariableActionHandler

class InlineVariableTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/inline-variable"
    }

    fun testInitSingleUsageSave() {
        doTest()
    }

    fun testInitSingleUsageNoSave() {
        doTest()
    }

    fun testInitManyUsages() {
        doTest()
    }

    fun testLateSingleInitCaseException() {
        assertThrows(CommonRefactoringUtil.RefactoringErrorHintException::class.java,
            "Cannot perform inline refactoring: refactoring is supported only when the initializer is present", this::doTest)
    }

    fun testIntermediateAssignmentConflict() {
        assertThrows(CommonRefactoringUtil.RefactoringErrorHintException::class.java,
            "Cannot perform inline refactoring: variable to be inlined has occurrences as lvalue", this::doTest)
    }

    fun testMultivarInitSingleCaseLeftSave() {
        doTest()
    }

    fun testMultivarInitSingleCaseLeftNoSave() {
        doTest()
    }

    fun testMultivarInitSingleCaseRightSave() {
        doTest()
    }

    fun testMultivarInitSingleCaseRightNoSave() {
        doTest()
    }

    fun testMultivarInitDoubleCaseLeftSave() {
        doTest()
    }

    fun testMultivarInitDoubleCaseLeftNoSave() {
        doTest()
    }

    fun testMultivarInitDoubleCaseCenterSave() {
        doTest()
    }

    fun testMultivarInitDoubleCaseCenterNoSave() {
        doTest()
    }

    fun testMultivarInitDoubleCaseRightSave() {
        doTest()
    }

    fun testMultivarInitDoubleCaseRightNoSave() {
        doTest()
    }

    fun testNamedParameterSingleUsageSave() {
        doTest()
    }

    fun testNamedParameterSingleUsageNoSave() {
        doTest()
    }

    fun testPositionalParameterSingleUsageSave() {
        doTest()
    }

    fun testPositionalParameterSingleUsageNoSave() {
        doTest()
    }

    fun testStrLiteralInline() {
        doTest()
    }

    fun testStartInline() {
        doTest()
    }

    fun testInlineIntoNamedArg() {
        doTest()
    }

    fun testSingleAssignment() {
        doTest()
    }

    fun testAttributeVariableException() {
        assertThrows(CommonRefactoringUtil.RefactoringErrorHintException::class.java,
                     "Cannot perform inline refactoring: attributes of class are used that are not available at inlining location",
                     this::doTest)
    }

    fun testSelfOfOtherClassInExpressionException() {
        assertThrows(CommonRefactoringUtil.RefactoringErrorHintException::class.java,
                     "Cannot perform inline refactoring: a reference to `self` is found, but caller and callee are in different classes",
                     this::doTest)
    }

    fun testNoAssignmentException() {
        assertThrows(CommonRefactoringUtil.RefactoringErrorHintException::class.java,
                     "Cannot perform inline refactoring: refactoring is supported only when the initializer is present",
                     this::doTest)
    }

    fun testScopeConflictException() {
        assertThrows(CommonRefactoringUtil.RefactoringErrorHintException::class.java,
                     "Cannot perform inline refactoring: element from original code is shadowed by another one at inlining location",
                     this::doTest)
    }

    fun testScopeConflictWithRoutineException() {
        assertThrows(CommonRefactoringUtil.RefactoringErrorHintException::class.java,
                     "Cannot perform inline refactoring: element from original code is shadowed by another one at inlining location",
                     this::doTest)
    }

    private fun doTest() {
        myFixture.configureByFile(getTestName(true) + "Before.p6")
        val action = RakuInlineVariableActionHandler()
        assertTrue(action.isEnabledOnElement(myFixture.getElementAtCaret()))
        action.inlineElement(getProject(), myFixture.getEditor(), myFixture.getElementAtCaret())
        myFixture.checkResultByFile(getTestName(true) + ".p6")
    }
}
