package org.raku.comma.completion

import com.intellij.codeInsight.completion.CompletionType
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType


class ModuleNameCompletionTest : CommaFixtureTestCase() {
    fun testPragmaCompletion() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "use exp<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        if (java.lang.Boolean.getBoolean("raku.test.dump.actual"))
            println("LOOKUP-ACTUAL ${getTestName(false)} <<<${myFixture.getLookupElementStrings()?.sorted()}>>>")
        assertNull(myFixture.getLookupElementStrings())
    }

    fun testVersionCompletion() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "use v6<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val names = myFixture.getLookupElementStrings()!!
        assertEmpty(names)
    }

    fun testLibraryCompletion1() {
        doTest("Te", "Test")
    }

    fun testLibraryCompletion2() {
        doTest("Nati", "NativeCall")
    }

    private fun doTest(prefix: String, full: String) {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, String.format("use %s<caret>", prefix))
        myFixture.complete(CompletionType.BASIC, 1)
        val names = myFixture.getLookupElementStrings()!!
        assertNotNull(names)
        assertContainsElements(names, full)
    }
}
