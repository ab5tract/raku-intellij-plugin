package org.raku.comma.refactoring

import com.intellij.refactoring.util.CommonRefactoringUtil
import com.intellij.testFramework.UsefulTestCase
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.RakuConstantExtractionHandlerMock
import org.raku.comma.RakuVariableExtractionHandlerMock
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.refactoring.introduce.variable.RakuIntroduceVariableHandler

class ExtractDeclarationTest : CommaFixtureTestCase() {
    fun testExpressionVariableExtraction() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say pi; say 1<selection>0 + 5</selection>0; say 10 + 50;")
        val handler = RakuVariableExtractionHandlerMock(null, "\$foo")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("say pi;\nmy \$foo = 10 + 50;\nsay \$foo;\nsay \$foo;")
    }

    fun testExpressionVariableExtractionFromCursor() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "foo(\"st<selection>ring-v</selection>alue\");")
        val handler = RakuVariableExtractionHandlerMock(null, "\$foo")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my \$foo = \"string-value\";\nfoo(\$foo);")
    }

    fun testExpressionConstantExtraction() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say pi; say 1<selection>0 + 5</selection>0; say 10 + 50;")
        val handler = RakuConstantExtractionHandlerMock(null, "\$foo")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("say pi;\nmy constant \$foo = 10 + 50;\nsay \$foo;\nsay \$foo;")
    }

    fun testStatementVariableExtraction() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>(^10).roll</selection>;")
        val handler = RakuVariableExtractionHandlerMock(null, "\$foo")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my \$foo = (^10).roll;")
    }

    fun testStatementVariableExtractionFull() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>(^10).roll;</selection>")
        val handler = RakuVariableExtractionHandlerMock(null, "\$bar")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my \$bar = (^10).roll;")
    }

    fun testStatementConstantExtraction() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>(^10).roll;</selection>")
        val handler = RakuConstantExtractionHandlerMock(null, "\$bar")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my constant \$bar = (^10).roll;")
    }

    fun testWhitespaceIsHandled() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>if True { say 10 } else { say 'no' }   </selection>")
        val handler = RakuVariableExtractionHandlerMock(null, "\$bar")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my \$bar = do if True { say 10 } else { say 'no' };")
    }

    fun testIfStatementExtraction() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>if True { say 10 } else { say 'no' }</selection>")
        val handler = RakuVariableExtractionHandlerMock(null, "\$bar")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my \$bar = do if True { say 10 } else { say 'no' };")
    }

    fun testUnlessStatementExtraction() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>unless False { say 10 }</selection>")
        val handler = RakuVariableExtractionHandlerMock(null, "\$bar")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my \$bar = do unless False { say 10 };")
    }


    fun testWithStatementExtraction() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>with \$foo { say 10 };</selection>")
        val handler = RakuVariableExtractionHandlerMock(null, "\$bar")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my \$bar = do with \$foo { say 10 };")
    }

    fun testWithoutStatementExtraction() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>without \$foo { say 10 };</selection>")
        val handler = RakuVariableExtractionHandlerMock(null, "\$bar")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my \$bar = do without \$foo { say 10 };")
    }

    fun testWhenStatementExtraction() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>when \$foo eq 50 { 10 };</selection>")
        val handler = RakuVariableExtractionHandlerMock(null, "\$bar")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my \$bar = do when \$foo eq 50 { 10 };")
    }

    fun testForStatementExtraction() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>for 1..3 { 10 }</selection>;")
        val handler = RakuVariableExtractionHandlerMock(null, "\$bar")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my \$bar = do for 1 .. 3 { 10 };")
    }

    fun testGivenStatementExtraction() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>given \$foo { when 1 { say 10 } }</selection>;")
        val handler = RakuVariableExtractionHandlerMock(null, "\$bar")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my \$bar = do given \$foo { when 1 { say 10 } };")
    }

    fun testLoopStatementExtraction() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>loop (my \$i = 0; \$i < 10; \$i++) { say \$i; }</selection>")
        val handler = RakuVariableExtractionHandlerMock(null, "\$bar")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my \$bar = do loop (my \$i = 0; \$i < 10; \$i++) { say \$i; };")
    }

    fun testWhileStatementExtraction() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>while \$foo != 0 { say 10 };</selection>")
        val handler = RakuVariableExtractionHandlerMock(null, "\$bar")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my \$bar = do while \$foo != 0 { say 10 };")
    }

    fun testUntilStatementExtraction() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>until \$foo eq 'Foo' { say 10 };</selection>")
        val handler = RakuVariableExtractionHandlerMock(null, "\$bar")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my \$bar = do until \$foo eq 'Foo' { say 10 };")
    }

    fun testRepeatStatementExtraction() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>repeat { say 10 } until True</selection>")
        val handler = RakuVariableExtractionHandlerMock(null, "\$bar")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my \$bar = do repeat { say 10 } until True;")
    }

    fun testCorrectAnchorSelection() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say 3 * (<selection>10 + 10</selection>);")
        val handler = RakuVariableExtractionHandlerMock(null, "\$bar")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my \$bar = 10 + 10;\nsay 3 * (\$bar);")
    }

    fun testPhaserExtractionFailing() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>BEGIN { say 10; }</selection>")
        val handler = RakuVariableExtractionHandlerMock(null, "\$bar")
        UsefulTestCase.assertThrows(CommonRefactoringUtil.RefactoringErrorHintException::class.java) {
            handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        }
    }

    fun testImportsExtractionFailing() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "use <selection>Foo::Bar</selection>;")
        val handler = RakuVariableExtractionHandlerMock(null, "\$bar")
        UsefulTestCase.assertThrows(CommonRefactoringUtil.RefactoringErrorHintException::class.java) {
            handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        }
    }

    fun testNonpostfixCallExtraction() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$a = %foo{<selection>42.foo</selection>};")
        val handler = RakuVariableExtractionHandlerMock(null, "\$foo")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my \$foo = 42.foo;\nmy \$a = %foo{\$foo};")
    }

    fun testNoExtractionForTypeInDeclaration() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my In<caret>t \$a;")
        val handler = RakuVariableExtractionHandlerMock(null, "\$foo")
        assertThrows(CommonRefactoringUtil.RefactoringErrorHintException::class.java, "Cannot refactor with this selection") {
            handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        }
    }

    fun testNoExtractionForTypeInParameter() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "when :(In<caret>t \$a) {};")
        val handler = RakuVariableExtractionHandlerMock(null, "\$foo")
        assertThrows(CommonRefactoringUtil.RefactoringErrorHintException::class.java, "Cannot refactor with this selection") {
            handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        }
    }

    fun testLiteralExtraction() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say <selection>42</selection>; say 42;")
        val handler = RakuVariableExtractionHandlerMock(null, "\$foo")
        handler.replaceAll = false
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my \$foo = 42;\nsay \$foo;\nsay 42;")
    }

    fun testExtractMethodCall() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "1.is-pr<caret>ime;")
        val handler = RakuVariableExtractionHandlerMock(null, "\$result")
        handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null)
        myFixture.checkResult("my \$result = 1.is-prime;")
    }
}