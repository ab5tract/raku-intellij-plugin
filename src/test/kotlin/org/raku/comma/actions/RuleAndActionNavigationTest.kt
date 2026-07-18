package org.raku.comma.actions

import com.intellij.ide.actions.GotoRelatedSymbolAction
import com.intellij.openapi.actionSystem.DataContext
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

class RuleAndActionNavigationTest : CommaFixtureTestCase() {
    private fun doTest(text: String, offset: Int) {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, text)
        val items = GotoRelatedSymbolAction.getItems(myFixture.file, myFixture.editor, DataContext.EMPTY_CONTEXT)
        assertEquals(1, items.size)
        assertEquals(offset, items[0].element!!.textOffset)
    }

    fun testGoingToActionFromRule() {
        doTest("grammar G { rule TOP { <caret> } }; class G { method TOP(\$/) {} }", 46)
    }

    fun testGoingToRuleFromAction() {
        doTest("grammar G { rule TOP { x } }; class G { method TOP(\$/) {<caret>} }", 17)
    }

    fun testGoingToActionFromRuleWithLongname() {
        doTest("grammar G { token foo:sym<bar> { <caret> } }; class G { method foo:sym<bar>(\$/) {} }", 56)
    }

    fun testGoingToRuleFromActionWithLongname() {
        doTest("grammar G { token foo:sym<bar> { x } }; class G { method foo:sym<bar>(\$/) {<caret>} }", 18)
    }
}
