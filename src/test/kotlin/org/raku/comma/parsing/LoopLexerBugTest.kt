package org.raku.comma.parsing

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.raku.comma.filetypes.RakuScriptFileType

class LoopLexerBugTest : BasePlatformTestCase() {
    fun testLexerBug1() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "loop (my \$a = 5; \$a < <caret>)")
        myFixture.type("5Foo")
        myFixture.checkResult("loop (my \$a = 5; \$a < 5Foo)")
    }
}
