package org.raku.comma.cro.rename

import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.cro.template.CroTemplateFileType

class RenameTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/ctl-rename"
    }

    private fun doTest(offset: Int, newName: String, result: String) {
        myFixture.configureByText(CroTemplateFileType.INSTANCE, "<:sub fofo(\$title)><\$title.attr></:sub><&fofo><:macro page></:macro><|page>")
        myFixture.getEditor().getCaretModel().moveToOffset(offset)
        myFixture.renameElementAtCaret(newName)
        myFixture.checkResult(result)
    }

    fun testRenameOfVariable() {
        doTest(15, "\$name", "<:sub fofo(\$name)><\$name.attr></:sub><&fofo><:macro page></:macro><|page>")
        doTest(22, "\$name", "<:sub fofo(\$name)><\$name.attr></:sub><&fofo><:macro page></:macro><|page>")
    }

    fun testRenameOfSubroutine() {
        doTest(8, "toto", "<:sub toto(\$title)><\$title.attr></:sub><&toto><:macro page></:macro><|page>")
        doTest(42, "toto", "<:sub toto(\$title)><\$title.attr></:sub><&toto><:macro page></:macro><|page>")
    }

    fun testRenameOfMacro() {
        doTest(57, "page2", "<:sub fofo(\$title)><\$title.attr></:sub><&fofo><:macro page2></:macro><|page2>")
        doTest(72, "page2", "<:sub fofo(\$title)><\$title.attr></:sub><&fofo><:macro page2></:macro><|page2>")
    }

    fun testCrossFileRename() {
        myFixture.configureByFiles("IdeaFoo/base.crotmp", "IdeaFoo/user.crotmp")
        myFixture.renameElementAtCaret("re-named")
        myFixture.checkResultByFile("IdeaFoo/base.crotmp", "IdeaFoo/base.after.crotmp", true)
        myFixture.checkResultByFile("IdeaFoo/user.crotmp", "IdeaFoo/user.after.crotmp", true)
    }
}
