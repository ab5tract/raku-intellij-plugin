package org.raku.comma.parsing;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

public class RakuLexer extends LexerBase {
    private CursorStack stack;
    private SortedMap<Integer, CursorStack> resumePoints;
    private int lastTokenStart;

    /* The text as the document actually has it. The cursor machine is run over a copy
     * with inactive #?if regions blanked out (see RakuConditionalCompilation), but the
     * platform must keep seeing the real characters: PsiBuilder takes the token text
     * from getBufferSequence(), so handing it the masked copy would put spaces into the
     * PSI. Masking replaces characters one-for-one, so the two agree on every offset. */
    private CharSequence originalBuffer;

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        originalBuffer = buffer;
        buffer = RakuConditionalCompilation.preprocess(buffer);

        /* Resume at the last statement end, if possible. */
        if (startOffset != 0) {
            int lastResPos = -1;
            boolean delete = false;
            for (Integer resPos : new ArrayList<>(resumePoints.keySet()))
                if (delete) {
                    resumePoints.remove(resPos);
                }
                else {
                    if (resPos - 1 > startOffset) {
                        resumePoints.remove(resPos);
                        delete = true;
                    }
                    else {
                        lastResPos = resPos;
                    }
                }
            if (lastResPos != -1) {
                stack = resumePoints.get(lastResPos).resume(buffer);
                lastTokenStart = stack.tokenStart;
                while (stack.tokenStart < startOffset)
                    advance();
                return;
            }
        }

        /* Not possible to resume; go from the start. */
        resumePoints = new TreeMap<>();
        stack = new CursorStack(buffer);
        stack.push(new MAINBraid().initialize(stack));
        lastTokenStart = 0;
        advance();
    }

    /* This is something of a cheat: the only state that means anything to us is 0, so we
     * make sure that we never re-use any other state, to avoid confusing the lexer
     * resumption. */
    private int freshState = 1;

    @Override
    public int getState() {
        return stack.cursors.isEmpty() || stack.token == RakuTokenTypes.STATEMENT_TERMINATOR
               ? 0
               : freshState++;
    }

    @Nullable
    @Override
    public IElementType getTokenType() {
        return stack.token;
    }

    @Override
    public int getTokenStart() {
        return stack.tokenStart;
    }

    @Override
    public int getTokenEnd() {
        return stack.peek().pos;
    }

    @Override
    public void advance() {
        while (true) {
            stack.ensureTopNotFrozen();
            int outcome = stack.peek().runRule();
            switch (outcome) {
                case -1: {
                    Cursor<MAINBraid> c = stack.pop();
                    c.passed = true;
                    if (stack.isEmpty()) {
                        if (c.pos != stack.target.length())
                            System.err.println("Raku lexer: failed to lex the whole file (passed early)");
                        stack.token = null;
                        return;
                    }
                    continue;
                }
                case -2:
                    stack.pop();
                    if (stack.isEmpty()) {
                        System.err.println("Raku lexer: failed to lex the whole file (failed to match)");
                        stack.token = null;
                        return;
                    }
                    continue;
                case -3:
                    if (stack.tokenStart < lastTokenStart)
                        System.err.println("Raku lexer went backwards (from " + lastTokenStart +
                            " to " + stack.tokenStart + ") over text '" +
                            stack.target.subSequence(stack.tokenStart, lastTokenStart) + "'");
                    else
                        lastTokenStart = stack.tokenStart;
                    if (stack.token == RakuTokenTypes.STATEMENT_TERMINATOR)
                        resumePoints.put(stack.peek().pos, stack.snapshot());
                    return;
                default:
                    stack.push(stack.peek().start(outcome));
            }
        }
    }

    @NotNull
    @Override
    public CharSequence getBufferSequence() {
        return originalBuffer != null ? originalBuffer : stack.target;
    }

    @Override
    public int getBufferEnd() {
        return stack.target.length(); // XXX
    }
}
