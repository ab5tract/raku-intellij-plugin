package org.raku.comma.parsing

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType
import junit.framework.TestCase

/**
 * A minimal delegate that emits exactly one real token and then reports
 * itself exhausted (tokenType == null), mirroring RakuLexer bailing out
 * early on malformed input (its "failed to lex the whole file" paths).
 * getTokenEnd() throws once exhausted, exactly like RakuLexer.getTokenEnd()
 * (stack.peek().pos on an empty cursor stack) would.
 */
private class EarlyBailoutLexer(private val firstTokenEnd: Int) : LexerBase() {
    private var buffer: CharSequence = ""
    private var bufferEnd = 0
    private var exhausted = false

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.bufferEnd = endOffset
        exhausted = false
    }

    override fun getState(): Int = 0

    override fun getTokenType(): IElementType? =
        if (exhausted) null else RakuTokenTypes.UNV_WHITE_SPACE

    override fun getTokenStart(): Int = 0

    override fun getTokenEnd(): Int {
        check(!exhausted) { "getTokenEnd() called on an exhausted delegate" }
        return firstTokenEnd
    }

    override fun advance() {
        exhausted = true
    }

    override fun getBufferSequence(): CharSequence = buffer

    override fun getBufferEnd(): Int = bufferEnd
}

/**
 * Regression coverage for the crash a code review caught: when the
 * (real) cursor-machine RakuLexer bails out early -- before reaching a
 * later inactive #?if region -- RakuCondBranchMergingLexer.computeToken()
 * used to fall into the "outside any region" branch and call
 * delegate.getTokenEnd() unconditionally, throwing on the exhausted
 * delegate. It must instead terminate cleanly (null token), matching the
 * delegate's own graceful stop. A real RakuLexer bailout is awkward to
 * construct reliably from Raku source, so this simulates it with a stub.
 */
class RakuCondBranchMergingLexerTest : TestCase() {

    fun testExhaustedDelegateBeforePendingRegionTerminatesCleanly() {
        // "hello\n" (0,6), then an inactive #?if region starting well after
        // the point where the stub delegate reports itself exhausted.
        val text = "hello\n#?if jvm\nbody\n#?endif\n"
        val regions = RakuConditionalCompilation.inactiveRegions(text)
        assertEquals(1, regions.size)
        assertTrue(
            "the region must start after the delegate's early bailout point",
            regions[0].start > 6
        )

        val lexer = RakuCondBranchMergingLexer(EarlyBailoutLexer(6))
        lexer.start(text, 0, text.length, 0)

        // First token: the delegate's one real token, passed through as-is.
        assertEquals(RakuTokenTypes.UNV_WHITE_SPACE, lexer.tokenType)
        assertEquals(0, lexer.tokenStart)
        assertEquals(6, lexer.tokenEnd)

        // Advancing past it drives the delegate to exhaustion while a
        // region still lies ahead of the current position. This must not
        // throw (pre-fix: IndexOutOfBoundsException-equivalent from the
        // stub's guarded getTokenEnd(), mirroring RakuLexer's real crash).
        lexer.advance()

        assertNull(
            "the wrapper must terminate cleanly instead of dereferencing " +
                "the exhausted delegate",
            lexer.tokenType
        )
    }
}
