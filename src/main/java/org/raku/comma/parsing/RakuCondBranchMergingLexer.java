package org.raku.comma.parsing;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Wraps the cursor-machine lexer and merges the tokens covering each
 * inactive #?if region body (blanked to whitespace by preprocess) into a
 * single CONDITIONAL_BRANCH token. Delegate tokens straddling a region edge
 * are split, so the merged token covers exactly the region body lines.
 * The delegate is untouched otherwise; on files without directives this is
 * pass-through.
 */
public class RakuCondBranchMergingLexer extends LexerBase {
    private final Lexer delegate;

    private List<RakuConditionalCompilation.Region> regions;
    private int regionIndex;
    private int pos;              // wrapper's current token start
    private int tokenEnd;
    private IElementType tokenType;
    private boolean splitting;    // mid-split of a delegate token: state is not restartable

    /* Whether the delegate's *current* token has already been folded in full
     * into a wrapper token we already reported, and so must be advanced past
     * before it is looked at again. Position comparisons alone cannot tell
     * "already reported" apart from "not yet reported" for a zero-width
     * token (the cursor-machine lexer emits plenty of these as grammar
     * signals, e.g. END_OF_EXPR): both have start == end == pos. Relying on
     * "delegate token end <= pos" to mean "already behind us" silently ate
     * those signal tokens, which broke ordinary files with no directives at
     * all. This flag disambiguates by tracking consumption explicitly
     * instead of re-deriving it from position. */
    private boolean delegateConsumed;

    /* Never re-use a state, so lexer resumption never mistakes a split
     * position for a clean restart point (same cheat as RakuLexer). */
    private int freshState = 1;

    public RakuCondBranchMergingLexer(Lexer delegate) {
        this.delegate = delegate;
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        regions = RakuConditionalCompilation.inactiveRegions(buffer);
        delegate.start(buffer, startOffset, endOffset, initialState);
        regionIndex = 0;
        pos = delegate.getTokenStart();
        delegateConsumed = false;
        computeToken();
    }

    @Override
    public void advance() {
        pos = tokenEnd;
        computeToken();
    }

    private void computeToken() {
        // Skip regions that end at or before the current position.
        while (regionIndex < regions.size() && regions.get(regionIndex).end <= pos) regionIndex++;

        // Catch the delegate up to `pos`. A token already folded into a
        // previously emitted wrapper token (delegateConsumed) is always
        // skipped; otherwise a token is skipped only when it lies wholly
        // behind pos: end <= pos AND start < pos. The start < pos half
        // matters only for zero-width tokens -- without it, a zero-width
        // signal token sitting exactly at pos (start == end == pos) looks
        // identical to one already behind us and gets eaten before it is
        // ever reported.
        while (delegate.getTokenType() != null &&
               (delegateConsumed || (delegate.getTokenEnd() <= pos && delegate.getTokenStart() < pos))) {
            delegate.advance();
            delegateConsumed = false;
        }

        if (delegate.getTokenType() == null && (regionIndex >= regions.size() || pos >= getBufferEnd())) {
            tokenType = null;
            tokenEnd = pos;
            splitting = false;
            delegateConsumed = false;
            return;
        }

        RakuConditionalCompilation.Region region =
            regionIndex < regions.size() ? regions.get(regionIndex) : null;

        if (region != null && region.start <= pos) {
            // Inside a region: one merged token to the region end.
            tokenType = RakuElementTypes.CONDITIONAL_BRANCH;
            tokenEnd = region.end;
            // The delegate token containing region.end may extend past it.
            splitting = delegate.getTokenType() != null && delegate.getTokenStart() < region.end
                        && delegate.getTokenEnd() > region.end;
        }
        else {
            // Outside any region: pass the delegate token through, clipped at the
            // next region start if it straddles one.
            tokenType = delegate.getTokenType();
            int dEnd = delegate.getTokenEnd();
            if (region != null && region.start < dEnd) {
                tokenEnd = region.start;
                splitting = true;
            } else {
                tokenEnd = dEnd;
                splitting = false;
            }
        }

        // The delegate's current token is fully folded into the token we
        // just built exactly when our end has caught up to (or passed, in
        // the region-jump case) its own end; that is when it must be
        // advanced before being looked at again.
        delegateConsumed = delegate.getTokenType() != null && tokenEnd >= delegate.getTokenEnd();
    }

    @Override
    public int getState() {
        return splitting || insideRegion() ? freshState++ : delegate.getState();
    }

    private boolean insideRegion() {
        return tokenType == RakuElementTypes.CONDITIONAL_BRANCH;
    }

    @Override
    public IElementType getTokenType() {
        return tokenType;
    }

    @Override
    public int getTokenStart() {
        return pos;
    }

    @Override
    public int getTokenEnd() {
        return tokenEnd;
    }

    @NotNull
    @Override
    public CharSequence getBufferSequence() {
        return delegate.getBufferSequence();
    }

    @Override
    public int getBufferEnd() {
        return delegate.getBufferEnd();
    }
}
