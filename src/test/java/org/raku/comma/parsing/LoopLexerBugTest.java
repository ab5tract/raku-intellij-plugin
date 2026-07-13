package org.raku.comma.parsing;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.raku.comma.filetypes.RakuScriptFileType;

public class LoopLexerBugTest extends BasePlatformTestCase {
    public void testLexerBug1() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "loop (my $a = 5; $a < <caret>)");
        myFixture.type("5Foo");
        myFixture.checkResult("loop (my $a = 5; $a < 5Foo)");
    }
}
