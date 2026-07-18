package org.raku.comma.actions

import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

class EnterHandlerTest : CommaFixtureTestCase() {
    fun testPodContinuation() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "#| Foo!<caret>")
        myFixture.performEditorAction("EditorEnter")
        assertEquals("#| Foo!\n#| ", myFixture.getDocument(myFixture.getFile()).getText())
        assertEquals(11, myFixture.getCaretOffset())
    }

    fun testPodContinuationInMiddle() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "#| Foo<caret> bar!")
        myFixture.performEditorAction("EditorEnter")
        assertEquals("#| Foo\n#| bar!", myFixture.getDocument(myFixture.getFile()).getText())
        assertEquals(10, myFixture.getCaretOffset())
    }
}
