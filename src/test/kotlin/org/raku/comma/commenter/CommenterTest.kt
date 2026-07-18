package org.raku.comma.commenter

import com.intellij.codeInsight.generation.actions.CommentByBlockCommentAction
import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.raku.comma.filetypes.RakuScriptFileType

class CommenterTest : BasePlatformTestCase() {
    fun testCommenter() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<caret>say 'foo';")
        val commentAction = CommentByLineCommentAction()
        commentAction.actionPerformedImpl(project, myFixture.editor)
        myFixture.checkResult("#say 'foo';")
        commentAction.actionPerformedImpl(project, myFixture.editor)
        myFixture.checkResult("say 'foo';")
    }

    fun testMultilineCommenter() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>say 'foo';\nsay 'bar';\n\n</selection>")
        val commentAction = CommentByLineCommentAction()
        commentAction.actionPerformedImpl(project, myFixture.editor)
        myFixture.checkResult("#say 'foo';\n#say 'bar';\n#\n")
        commentAction.actionPerformedImpl(project, myFixture.editor)
        myFixture.checkResult("say 'foo';\nsay 'bar';\n\n")
    }

    fun testBlockCommenter() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>say 'foo';\nsay 'bar';\n\n</selection>")
        val commentAction = CommentByBlockCommentAction()
        commentAction.actionPerformedImpl(project, myFixture.editor)
        myFixture.checkResult("#`[\nsay 'foo';\nsay 'bar';\n\n]\n")
        commentAction.actionPerformedImpl(project, myFixture.editor)
        myFixture.checkResult("say 'foo';\nsay 'bar';\n\n")
    }
}
