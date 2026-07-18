package org.raku.comma.refactoring

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.util.CommonRefactoringUtil
import com.intellij.testFramework.UsefulTestCase
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.refactoring.RakuCodeBlockType
import org.raku.comma.psi.RakuFile
import org.raku.comma.psi.RakuPackageDecl
import org.raku.comma.psi.RakuRoutineDecl
import org.raku.comma.psi.RakuStatement
import org.raku.comma.psi.RakuStatementList

class ExtractCodeBlockTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/block-extract"
    }

    fun testMethodSingleScopePresence() {
        doScopeTest("start", RakuCodeBlockType.METHOD) { scopes ->
            assertEquals(1, scopes.size)
            checkPackage(scopes, 0, "A", "class")
        }
    }

    fun testMethodOuterClassScopePresence() {
        doScopeTest("start", RakuCodeBlockType.METHOD) { scopes ->
            assertEquals(4, scopes.size)
            checkPackage(scopes, 0, "M", "monitor")
            checkPackage(scopes, 1, "G", "grammar")
            checkPackage(scopes, 2, "R", "role")
            checkPackage(scopes, 3, "C", "class")
        }
    }

    fun testSubFilePresence() {
        doScopeTest("'start'", RakuCodeBlockType.ROUTINE) { scopes ->
            assertEquals(1, scopes.size)
            val decl = PsiTreeUtil.getParentOfType(
                scopes[0], RakuPackageDecl::class.java, RakuRoutineDecl::class.java, RakuFile::class.java)
            assertTrue(decl is RakuFile)
        }
    }

    fun testSubNestedScopePresence() {
        doScopeTest("'start'", RakuCodeBlockType.ROUTINE) { scopes ->
            assertEquals(4, scopes.size)
            checkPackage(scopes, 2, "ABC", "class")
        }
    }

    private fun checkPackage(scopes: List<RakuStatementList>, index: Int, packageName: String, packageKind: String) {
        val decl = PsiTreeUtil.getParentOfType(
            scopes[index], RakuPackageDecl::class.java, RakuRoutineDecl::class.java, RakuFile::class.java)
        assertTrue(decl is RakuPackageDecl)
        assertNotNull(decl)
        assertEquals(packageName, (decl as RakuPackageDecl).packageName)
        assertEquals(packageKind, decl.packageKind)
    }

    fun testTopFileSubroutineExtraction() {
        doTest({ getClosestStatementListByText("say 1") }, "foo-bar", RakuCodeBlockType.ROUTINE)
    }

    fun testTopFileMethodImpossible() {
        UsefulTestCase.assertThrows(CommonRefactoringUtil.RefactoringErrorHintException::class.java) {
            doTest({ getClosestStatementListByText("say 1") }, "foo-bar", RakuCodeBlockType.METHOD)
        }
    }

    fun testInMethodMethodExtraction() {
        UsefulTestCase.assertThrows(CommonRefactoringUtil.RefactoringErrorHintException::class.java) {
            doTest({ getClosestStatementListByText("foo") }, "foo-bar", RakuCodeBlockType.PRIVATEMETHOD)
        }
    }

    fun testInClassMethodExtraction() {
        doTest({ getNextList(getClosestStatementListByText("say 'foo'")) }, "foo-bar", RakuCodeBlockType.METHOD)
    }

    fun testInClassPrivateMethodExtraction() {
        doTest({ getNextList(getClosestStatementListByText("say 'foo'")) }, "foo-bar", RakuCodeBlockType.PRIVATEMETHOD)
    }

    fun testSubroutineExtractionTwoLevelsUp() {
        doTest({ getNextList(getNextList(getClosestStatementListByText("say 'foo'"))) }, "outer-sub", RakuCodeBlockType.ROUTINE)
    }

    fun testSubroutineWithLocalVariablesExtraction() {
        doTest({ getNextList(getClosestStatementListByText("Magic number")) }, "do-magic", RakuCodeBlockType.ROUTINE)
    }

    fun testSubroutineWithTypedLocalVariablesExtraction() {
        doTest({ getNextList(getClosestStatementListByText("Magic number")) }, "do-magic", RakuCodeBlockType.ROUTINE)
    }

    fun testLocalDeclarationsAreNotPassed() {
        doTest({ getNextList(getClosestStatementListByText("inner")) }, "extracted", RakuCodeBlockType.ROUTINE)
    }

    fun testSelfInSameClassMethodIsUntouched() {
        doTest({ getNextList(getNextList(getClosestStatementListByText("self"))) }, "inner", RakuCodeBlockType.METHOD)
    }

    fun testSelfInSubroutineIsPassed() {
        doTest({ getNextList(getClosestStatementListByText("self")) }, "foo", RakuCodeBlockType.ROUTINE)
    }

    fun testSelfInAnotherClassIsPassed() {
        doTest({ getNextList(getClosestStatementListByText("self")) }, "foo", RakuCodeBlockType.METHOD)
    }

    fun testAttributesToSubArePassed() {
        doTest({ getNextList(getClosestStatementListByText("\$!")) }, "foo", RakuCodeBlockType.ROUTINE)
    }

    fun testAttributesToNewNearMethodAreNotPassed() {
        doTest({ getNextList(getClosestStatementListByText("say \$!")) }, "two", RakuCodeBlockType.PRIVATEMETHOD)
    }

    fun testAttributesToMethodLexicalSubAreNotPassed() {
        doTest({ getClosestStatementListByText("say \$!") }, "inner-lexical", RakuCodeBlockType.ROUTINE)
    }

    fun testAttributesArePassedToOuterClass() {
        doTest({ getNextList(getNextList(getClosestStatementListByText("say \$!"))) }, "outer", RakuCodeBlockType.METHOD)
    }

    fun testLexicalSubBeingPassed() {
        doTest({ getNextList(getClosestStatementListByText("a(5)")) }, "with-a-lexical", RakuCodeBlockType.ROUTINE)
    }

    fun testLexicalSubsAreDifferentiated() {
        doTest({ getNextList(getClosestStatementListByText("will be")) }, "extracted", RakuCodeBlockType.ROUTINE)
    }

    fun testVarUsedInDeclarationIsPassed() {
        doTest({ getNextList(getClosestStatementListByText("\$var.key")) }, "foo", RakuCodeBlockType.PRIVATEMETHOD)
    }

    fun testVarsUsedAreNotDuplicated() {
        doTest({ getNextList(getClosestStatementListByText("\$foo")) }, "foo", RakuCodeBlockType.ROUTINE)
    }

    fun testVarRenaming() {
        doTest({ getNextList(getClosestStatementListByText("say \$aaa")) }, "foo-bar", RakuCodeBlockType.ROUTINE) { data ->
            data.variables[0].parameterName = "\$bbb"
            data
        }
    }

    fun testVarsSwapping() {
        doTest({ getNextList(getClosestStatementListByText("say \$one")) }, "sum", RakuCodeBlockType.ROUTINE) { data ->
            val temp = data.variables[0]
            data.variables[0] = data.variables[1]
            data.variables[1] = temp
            data
        }
    }

    fun testHeredoc() {
        doTest({ getClosestStatementListByText("END") }, "heredoc", RakuCodeBlockType.ROUTINE)
    }

    fun testMathExpression() {
        doTest({ getClosestStatementListByText("say") }, "math", RakuCodeBlockType.ROUTINE)
    }

    fun testFullMathExpression() {
        doTest({ getClosestStatementListByText("say") }, "math", RakuCodeBlockType.ROUTINE, 1)
    }

    fun testTopMathExpression() {
        doTest({ getClosestStatementListByText("say") }, "math", RakuCodeBlockType.ROUTINE, 2)
    }

    fun testMathExpressionFromSelection() {
        doTest({ getClosestStatementListByText("say") }, "math", RakuCodeBlockType.ROUTINE)
    }

    fun testFullMathExpressionFromSelection() {
        doTest({ getClosestStatementListByText("say") }, "math", RakuCodeBlockType.ROUTINE, 1)
    }

    fun testCallchain() {
        doTest({ getClosestStatementListByText("foo") }, "cond", RakuCodeBlockType.ROUTINE, 0)
    }

    fun testCallchainFromSelection1() {
        doTest({ getClosestStatementListByText("foo") }, "cond", RakuCodeBlockType.ROUTINE, 1)
    }

    fun testCallchainFromSelection2() {
        doTest({ getClosestStatementListByText("foo") }, "cond", RakuCodeBlockType.ROUTINE)
    }

    fun testConstructWithBracesExtractionAsLastExpr() {
        doTest({ getClosestStatementListByText("method") }, "foo", RakuCodeBlockType.METHOD, 0)
    }

    // Helper methods

    /** Gets innermost statement list in an opened file around a line of text passed */
    private fun getClosestStatementListByText(text: String): RakuStatementList {
        return myFixture.findElementByText(text, RakuStatementList::class.java)
    }

    private fun getNextList(list: RakuStatementList): RakuStatementList {
        return PsiTreeUtil.getParentOfType(list, RakuStatementList::class.java, true)!!
    }

    private fun doScopeTest(text: String, type: RakuCodeBlockType, check: (List<RakuStatementList>) -> Unit) {
        myFixture.configureByFile(getTestName(true) + ".p6")
        val start = myFixture.findElementByText(text, PsiElement::class.java)
        val scopes = RakuExtractCodeBlockHandlerMock(type).getPossibleScopes(arrayOf(start))
        check(scopes)
    }

    private fun doTest(
        getScope: () -> RakuStatementList,
        name: String,
        type: RakuCodeBlockType,
        userAction: ((NewCodeBlockData) -> NewCodeBlockData)? = null,
    ) {
        myFixture.configureByFile(getTestName(true) + "Before.p6")
        val scope = getScope()
        val handler = RakuExtractCodeBlockHandlerMock(type, scope, name, userAction)
        handler.invoke(myFixture.project, myFixture.editor, myFixture.file, null)
        myFixture.checkResultByFile(getTestName(true) + ".p6", true)
    }

    private fun doTest(getScope: () -> RakuStatementList, name: String, type: RakuCodeBlockType, exprLevel: Int) {
        myFixture.configureByFile(getTestName(true) + "Before.p6")
        val scope = getScope()
        val handler = RakuExtractCodeBlockHandlerMock(type, scope, name, exprLevel)
        handler.invoke(myFixture.project, myFixture.editor, myFixture.file, null)
        myFixture.checkResultByFile(getTestName(true) + ".p6", true)
    }

    private class RakuExtractCodeBlockHandlerMock : RakuExtractCodeBlockHandler {
        private val userAction: ((NewCodeBlockData) -> NewCodeBlockData)?
        private val parent: RakuStatementList?
        private val name: String
        private var myExpressionTargetIndex = 0

        constructor(type: RakuCodeBlockType, parent: RakuStatementList, name: String, expressionTargetIndex: Int)
            : super(type) {
            this.parent = parent
            this.name = name
            this.userAction = null
            this.myExpressionTargetIndex = expressionTargetIndex
        }

        constructor(type: RakuCodeBlockType, parent: RakuStatementList, name: String,
                    userAction: ((NewCodeBlockData) -> NewCodeBlockData)?)
            : super(type) {
            this.parent = parent
            this.name = name
            this.userAction = userAction
        }

        constructor(type: RakuCodeBlockType) : super(type) {
            userAction = null
            parent = null
            name = ""
        }

        public override fun getPossibleScopes(elements: Array<PsiElement>): List<RakuStatementList> {
            return super.getPossibleScopes(elements)
        }

        override fun invokeWithStatements(project: Project, editor: Editor?, file: PsiFile?, elementsToExtract: Array<PsiElement>) {
            invokeWithScope(project, editor, parent, elementsToExtract)
        }

        override fun getNewBlockData(project: Project, parentToCreateAt: RakuStatementList, elements: Array<PsiElement>): NewCodeBlockData {
            val data = NewCodeBlockData(myCodeBlockType, name, getCapturedVariables(parent, elements))
            data.containsExpression = isExpr
            data.wantsSemicolon = isExpr && elements.size == 1 && checkNeedsSemicolon(elements[0])
            return userAction?.invoke(data) ?: data
        }

        override fun getExpressionsFromSelection(file: PsiFile, editor: Editor, commonParent: PsiElement, fullStatementBackup: PsiElement?): Array<PsiElement> {
            val targets = getExpressionTargets(commonParent)
            val psiElement = targets[myExpressionTargetIndex]
            isExpr = psiElement !is RakuStatement
            return arrayOf(psiElement)
        }

        override fun getElementsFromCaret(file: PsiFile, editor: Editor): Array<PsiElement> {
            val offset = editor.caretModel.offset
            val element = file.findElementAt(offset) ?: return PsiElement.EMPTY_ARRAY
            val targets = getExpressionTargets(element.parent)
            val psiElement = targets[myExpressionTargetIndex]
            isExpr = psiElement !is RakuStatement
            return arrayOf(psiElement)
        }
    }
}
