package org.raku.comma.output

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.run.RakuOutputLinkFilter

class RakuStackTraceOutputLinkTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/outputFilter"
    }

    fun testStackTraceFilterRegex() {
        doTest("    in method with-connection at %s (Easii::DB) line 15\n", 14)
        doTest("    in block at %s (Easii::Input::Store) line 32\n", 31)
        doTest("in block <unit> at %s line 2", 1)
    }

    private fun doTest(stackTraceLine: String, number: Int) {
        val file = myFixture.configureByFiles("IdeaFoo/Prefix.p6", "IdeaFoo/Test.pm6")[1]
        val filter = RakuOutputLinkFilter(project)
        val line = String.format(stackTraceLine, file.virtualFile.path)
        val result = filter.applyFilter(line, line.length)
        val items = result!!.resultItems
        assertNotEmpty(items)

        items[0].hyperlinkInfo!!.navigate(project)

        val editors = FileEditorManager.getInstance(project).selectedEditors
        assertEquals(1, editors.size)
        assertEquals(editors[0].file, file.virtualFile)
        val editor = DataManager.getInstance().dataContextFromFocusAsync.blockingGet(1)!!.getData(CommonDataKeys.EDITOR)
        assertEquals(number, editor!!.caretModel.offset)
    }
}
