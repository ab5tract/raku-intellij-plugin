package org.raku.comma.editor

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import org.raku.comma.CommaFixtureTestCase

import java.util.List

class RakuLineMarkerTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/editor"
    }

    fun testRoles() {
        doTest(4)
    }

    fun testClasses() {
        doTest(6)
    }

    fun testNameUsages() {
        doTest(2)
    }

    private fun doTest(size: Int) {
        myFixture.configureByFile(getTestName(true) + ".pm6")
        myFixture.doHighlighting()
        val list = DaemonCodeAnalyzerImpl.getLineMarkers(myFixture.getEditor().getDocument(), getProject())
        assertEquals(list.size, size)
    }
}
