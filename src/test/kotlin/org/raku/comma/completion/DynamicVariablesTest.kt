package org.raku.comma.completion

import com.intellij.codeInsight.completion.CompletionType
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType


class DynamicVariablesTest : CommaFixtureTestCase() {
    fun testIntegration() {
        doTest("sub { my \$*DYNAMIC-VAR1 }; { my \$*DYNAMIC-VAR2; }; { say \$*DY<caret> }",
               "\$*DYNAMIC-VAR1", "\$*DYNAMIC-VAR2")
    }

    private fun doTest(text: String, vararg elems: String) {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, text)
        myFixture.complete(CompletionType.BASIC, 1)
        val methods = myFixture.getLookupElementStrings()!!
        assertNotNull(methods)
        assertContainsElements(methods, *elems)
    }

}
