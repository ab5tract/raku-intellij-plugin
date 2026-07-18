package org.raku.comma.parsing

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.raku.comma.filetypes.RakuScriptFileType

class LiteralInRoleSignatureTest : BasePlatformTestCase() {
    fun testLexerBug1() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class A does B[<caret>]")
        myFixture.type("1")
        myFixture.checkResult("class A does B[1]")
    }
}
