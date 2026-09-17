package org.raku.comma.highlighting

import com.intellij.psi.tree.IElementType
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.parsing.RakuElementTypes
import org.raku.comma.parsing.RakuHighlighterLexer

class ConditionalBranchHighlightingTest : CommaFixtureTestCase() {

    private val text = "my \$shared = 1;\n#?if jvm\nmy \$j = \"code\";\n#?endif\nsay \$shared;\n"

    private fun lexed(): List<Pair<IElementType, String>> {
        val lexer = RakuHighlighterLexer.branchAware()
        lexer.start(text)
        val tokens = ArrayList<Pair<IElementType, String>>()
        while (lexer.tokenType != null) {
            tokens.add(lexer.tokenType!! to text.substring(lexer.tokenStart, lexer.tokenEnd))
            lexer.advance()
        }
        return tokens
    }

    fun testBranchBodyLexesAsCodeNotWhitespace() {
        val tokens = lexed()
        // The jvm branch's string literal must be lexed as a real token.
        assertTrue("expected a token for \"code\" inside the branch, got: $tokens",
                   tokens.any { it.second == "code" })
        // And the merged branch token itself must not survive to the token stream.
        assertFalse(tokens.any { it.first == RakuElementTypes.CONDITIONAL_BRANCH })
    }

    fun testTokensTileTheWholeFile() {
        val lexer = RakuHighlighterLexer.branchAware()
        lexer.start(text)
        var expectedStart = 0
        while (lexer.tokenType != null) {
            assertEquals("gap or overlap at ${lexer.tokenStart}", expectedStart, lexer.tokenStart)
            expectedStart = lexer.tokenEnd
            lexer.advance()
        }
        assertEquals(text.length, expectedStart)
    }

    // The reported breakage: editing near a directive must not blow up
    // incremental highlighting. The harness escalates logged errors, so
    // survival of type-then-rehighlight IS the assertion.
    fun testEditingNearDirectiveDoesNotBreakHighlighting() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, text + "say 42;\n")
        myFixture.doHighlighting()
        myFixture.editor.caretModel.moveToOffset(myFixture.file.textLength)
        myFixture.type("my \$x = 3;")
        myFixture.doHighlighting()
        myFixture.editor.caretModel.moveToOffset(text.indexOf("say"))
        myFixture.type("# ")
        myFixture.doHighlighting()
    }
}
