package org.raku.comma.actions

import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

class RakuStatementMoveTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/mover"
    }

    fun testSimple() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "say 1;\n<caret>say 2;\nsay 3;\n")
        myFixture.performEditorAction("MoveStatementUp")
        myFixture.checkResult("<caret>say 2;\nsay 1;\nsay 3;\n")

        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "say 1;\n<caret>say 2;\nsay 3;\n")
        myFixture.performEditorAction("MoveStatementDown")
        myFixture.checkResult("say 1;\nsay 3;\n<caret>say 2;\n")
    }

    fun testBracketedDown() {
        myFixture.configureByFile("BlockTestData.p6")
        myFixture.performEditorAction("MoveStatementDown")
        myFixture.checkResultByFile("BlockTestDataDown.p6")
    }

    fun testBracketedCursorFirstUp() {
        myFixture.configureByFile("BlockTestData.p6")
        myFixture.getEditor().getCaretModel().moveToOffset(25)
        myFixture.performEditorAction("MoveStatementUp")
        myFixture.checkResultByFile("BlockTestDataCursorUp.p6")
    }

    fun testBracketedUp() {
        myFixture.configureByFile("BlockTestData.p6")
        myFixture.performEditorAction("MoveStatementUp")
        myFixture.checkResultByFile("BlockTestDataUp.p6")
    }

    fun testBracketedUpFirstBlockCursor() {
        myFixture.configureByFile("BlockTestDataBegin.p6")
        myFixture.getEditor().getCaretModel().moveToOffset(24)
        myFixture.performEditorAction("MoveStatementUp")
        myFixture.checkResultByFile("BlockTestDataBeginUpCursor.p6")
    }

    fun testBracketedUpFirstBlock() {
        myFixture.configureByFile("BlockTestDataBegin.p6")
        myFixture.performEditorAction("MoveStatementUp")
        myFixture.checkResultByFile("BlockTestDataBeginUp.p6")
    }

    fun testBracketedNestedCursor() {
        myFixture.configureByFile("NestedBrackets.p6")
        myFixture.getEditor().getCaretModel().moveToOffset(45)
        myFixture.performEditorAction("MoveStatementUp")
        myFixture.checkResultByFile("NestedBracketsUpCursor.p6")
    }

    fun testBracketedNested() {
        myFixture.configureByFile("NestedBrackets.p6")
        myFixture.performEditorAction("MoveStatementUp")
        myFixture.checkResultByFile("NestedBracketsUp.p6")
    }

    fun testMultilineDown() {
        myFixture.configureByFile("MultilineTestData.p6")
        myFixture.performEditorAction("MoveStatementDown")
        myFixture.checkResultByFile("MultilineTestDataDown.p6")
    }

    fun testMultilineUp() {
        myFixture.configureByFile("MultilineTestData.p6")
        myFixture.performEditorAction("MoveStatementUp")
        myFixture.checkResultByFile("MultilineTestDataUp.p6")
    }

    fun testCase1() {
        myFixture.configureByFile("Case1.p6")
        myFixture.performEditorAction("MoveStatementDown")
        myFixture.checkResultByFile("Case1Down.p6")
    }

    fun testCase2() {
        myFixture.configureByFile("Case2.p6")
        myFixture.performEditorAction("MoveStatementDown")
        myFixture.checkResultByFile("Case2Down.p6")
    }

    fun testCase3() {
        myFixture.configureByFile("Case3.p6")
        myFixture.performEditorAction("MoveStatementUp")
        myFixture.checkResultByFile("Case3Up.p6")
    }

    fun testCase4() {
        myFixture.configureByFile("Case4.p6")
        myFixture.performEditorAction("MoveStatementUp")
        myFixture.checkResultByFile("Case4Up.p6")
    }

    fun testCase5() {
        myFixture.configureByFile("Case5.p6")
        myFixture.performEditorAction("MoveStatementUp")
        myFixture.checkResultByFile("Case5.p6")
    }

    fun testCase6() {
        myFixture.configureByFile("Case6.p6")
        myFixture.performEditorAction("MoveStatementUp")
        myFixture.checkResultByFile("Case6Up.p6")
    }

    fun testCase7() {
        myFixture.configureByFile("Case7.p6")
        myFixture.performEditorAction("MoveStatementDown")
        myFixture.checkResultByFile("Case7Down.p6")
    }

    fun testCase8() {
        myFixture.configureByFile("Case8.p6")
        myFixture.performEditorAction("MoveStatementDown")
        myFixture.checkResultByFile("Case8Down.p6")
    }

    fun testCase9() {
        myFixture.configureByFile("Case9.p6")
        myFixture.performEditorAction("MoveStatementDown")
        myFixture.checkResultByFile("Case9Down.p6")
    }

    fun testCase10() {
        myFixture.configureByFile("Case10.p6")
        myFixture.performEditorAction("MoveStatementDown")
        myFixture.checkResultByFile("Case10Down.p6")
    }

    fun testCase10Up() {
        myFixture.configureByFile("Case10.p6")
        myFixture.performEditorAction("MoveStatementUp")
        myFixture.checkResultByFile("Case10Up.p6")
    }

    fun testHeredocEdgeCases() {
        myFixture.configureByFile("Heredoc1.p6")
        myFixture.performEditorAction("MoveStatementUp")
        myFixture.checkResultByFile("Heredoc1.p6")
        myFixture.performEditorAction("MoveStatementDown")
        myFixture.checkResultByFile("Heredoc1.p6")
        myFixture.getEditor().getCaretModel().moveToOffset(100)
        myFixture.performEditorAction("MoveStatementUp")
        myFixture.checkResultByFile("Heredoc1Cursor.p6")
        myFixture.performEditorAction("MoveStatementDown")
        myFixture.checkResultByFile("Heredoc1Cursor.p6")
    }

    fun testHeredocDown() {
        myFixture.configureByFile("Heredoc2.p6")
        myFixture.performEditorAction("MoveStatementDown")
        myFixture.checkResultByFile("Heredoc2Down.p6")
    }

    fun testHeredocDownCursor() {
        myFixture.configureByFile("Heredoc2.p6")
        myFixture.getEditor().getCaretModel().moveToOffset(100)
        myFixture.performEditorAction("MoveStatementDown")
        myFixture.checkResultByFile("Heredoc2Down.p6")
    }

    fun testHeredocUp() {
        myFixture.configureByFile("Heredoc3.p6")
        myFixture.performEditorAction("MoveStatementUp")
        myFixture.checkResultByFile("Heredoc3Up.p6")
    }

    fun testHeredocUpCursor() {
        myFixture.configureByFile("Heredoc3.p6")
        myFixture.getEditor().getCaretModel().moveToOffset(100)
        myFixture.performEditorAction("MoveStatementUp")
        myFixture.checkResultByFile("Heredoc3Up.p6")
    }
}
