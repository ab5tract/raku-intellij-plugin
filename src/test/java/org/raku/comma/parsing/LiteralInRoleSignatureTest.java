package org.raku.comma.parsing;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.raku.comma.filetypes.RakuScriptFileType;

public class LiteralInRoleSignatureTest extends BasePlatformTestCase {
    public void testLexerBug1() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class A does B[<caret>]");
        myFixture.type("1");
        myFixture.checkResult("class A does B[1]");
    }
}
