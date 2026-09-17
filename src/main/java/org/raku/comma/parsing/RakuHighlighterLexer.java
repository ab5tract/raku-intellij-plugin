package org.raku.comma.parsing;

import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RakuHighlighterLexer extends RakuLexer {
    @Override
    public void advance() {
        // Skip over zero-width tokens, which break various lexer-based features,
        // such as brace matching in some cases.
        super.advance();
        while (getTokenType() != null && getTokenStart() == getTokenEnd()) super.advance();
    }

    /**
     * The lexer for user-facing token streams (syntax highlighting, word
     * indexing): inactive #?if branches are merged into CONDITIONAL_BRANCH
     * tokens by the wrapper, then re-lexed as live Raku by the layer -- so
     * every branch shows real code, "as if its directive were true".
     */
    public static Lexer branchAware() {
        LayeredLexer lexer = new LayeredLexer(new RakuCondBranchMergingLexer(new RakuHighlighterLexer()));
        lexer.registerSelfStoppingLayer(new BranchBodyLexer(),
                                        new IElementType[]{RakuElementTypes.CONDITIONAL_BRANCH},
                                        IElementType.EMPTY_ARRAY);
        return lexer;
    }

    /**
     * The layer LayeredLexer activates over each CONDITIONAL_BRANCH token.
     *
     * LayeredLexer hands a layer the *whole* document buffer and asks it to
     * start lexing at the base token's own (generally nonzero) offset. But
     * RakuLexer.start only honours a nonzero startOffset by resuming from a
     * resume point cached on that same instance by an earlier offset-0 call
     * (see RakuLexer#start); a freshly constructed instance has no such
     * cache, and asking it to start straight at a nonzero offset throws a
     * NullPointerException on its resumePoints map.
     *
     * So this adapter re-lexes the branch body as an independent snippet:
     * it starts the wrapped RakuHighlighterLexer at offset 0 on the
     * substring covering just the branch, then shifts every reported
     * offset back into document coordinates. That also sidesteps
     * RakuLexer#getTokenStart/#getTokenEnd, which are unsafe to call once
     * the delegate is exhausted (its cursor stack is empty by then) --
     * LayeredLexer does call getTokenEnd() right after it observes a null
     * token type, so this class never forwards that call to the delegate.
     */
    private static final class BranchBodyLexer extends LexerBase {
        private final RakuHighlighterLexer delegate = new RakuHighlighterLexer();
        private CharSequence buffer = "";
        private int shift;
        private int endOffset;

        @Override
        public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
            this.buffer = buffer;
            this.shift = startOffset;
            this.endOffset = endOffset;
            delegate.start(buffer.subSequence(startOffset, endOffset), 0, endOffset - startOffset, 0);
        }

        @Override
        public void advance() {
            delegate.advance();
        }

        @Override
        public int getState() {
            return delegate.getState();
        }

        @Nullable
        @Override
        public IElementType getTokenType() {
            return delegate.getTokenType();
        }

        @Override
        public int getTokenStart() {
            return delegate.getTokenType() != null ? delegate.getTokenStart() + shift : endOffset;
        }

        @Override
        public int getTokenEnd() {
            return delegate.getTokenType() != null ? delegate.getTokenEnd() + shift : endOffset;
        }

        @NotNull
        @Override
        public CharSequence getBufferSequence() {
            return buffer;
        }

        @Override
        public int getBufferEnd() {
            return endOffset;
        }
    }
}
