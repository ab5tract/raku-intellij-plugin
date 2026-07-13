package org.raku.comma.commenter;

import com.intellij.codeInsight.generation.actions.CommentByBlockCommentAction;
import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.raku.comma.filetypes.RakuScriptFileType;

public class CommenterTest extends BasePlatformTestCase {
    public void testCommenter() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<caret>say 'foo';");
        CommentByLineCommentAction commentAction = new CommentByLineCommentAction();
        commentAction.actionPerformedImpl(getProject(), myFixture.getEditor());
        myFixture.checkResult("#say 'foo';");
        commentAction.actionPerformedImpl(getProject(), myFixture.getEditor());
        myFixture.checkResult("say 'foo';");
    }

    public void testMultilineCommenter() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>say 'foo';\nsay 'bar';\n\n</selection>");
        CommentByLineCommentAction commentAction = new CommentByLineCommentAction();
        commentAction.actionPerformedImpl(getProject(), myFixture.getEditor());
        myFixture.checkResult("#say 'foo';\n#say 'bar';\n#\n");
        commentAction.actionPerformedImpl(getProject(), myFixture.getEditor());
        myFixture.checkResult("say 'foo';\nsay 'bar';\n\n");
    }

    public void testBlockCommenter() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<selection>say 'foo';\nsay 'bar';\n\n</selection>");
        CommentByBlockCommentAction commentAction = new CommentByBlockCommentAction();
        commentAction.actionPerformedImpl(getProject(), myFixture.getEditor());
        myFixture.checkResult("#`[\nsay 'foo';\nsay 'bar';\n\n]\n");
        commentAction.actionPerformedImpl(getProject(), myFixture.getEditor());
        myFixture.checkResult("say 'foo';\nsay 'bar';\n\n");
    }
}
