package org.raku.comma.parsing;

/**
 * Rakudo's core sources are not parsed as-is: {@code tools/build/gen-cat.nqp} runs over
 * them first and blanks out the branches that do not apply to the backend being built.
 * The markers are {@code #?if <name>} / {@code #?if !<name>} ... {@code #?endif}, and
 * they are not part of the Raku grammar at all -- the compiler never sees them.
 *
 * <p>Treating them as ordinary comments (which is what they look like) breaks badly,
 * because the alternatives routinely open a bracket that only one of them closes:
 *
 * <pre>
 * #?if !js
 *     my constant $valid-units = nqp::hash(
 * #?endif
 * #?if js
 *     my $valid-units := nqp::hash(
 * #?endif
 *       'day', 1,
 *     );
 * </pre>
 *
 * Both {@code nqp::hash(} lines survive, so the parser sees two opening parens and one
 * closing one, and everything after it is swallowed into an argument list that never
 * ends. In Date.rakumod that mis-parse reaches as far as the next method declaration.
 *
 * <p>We do what gen-cat does: overwrite the inactive lines with spaces. Newlines are
 * kept so line numbers do not move, and because every character is replaced one-for-one
 * the buffer keeps its length and every offset in the file stays valid.
 */
public final class RakuConditionalCompilation {
    private RakuConditionalCompilation() {}

    /**
     * The backend the plugin assumes when deciding which branch is live. A Raku SDK the
     * plugin can talk to is a Rakudo on MoarVM, so anything guarded by {@code #?if jvm}
     * or {@code #?if js} is dead code as far as the IDE is concerned.
     */
    private static final String BACKEND = "moar";

    private static final String IF_MARKER = "#?if";
    private static final String ENDIF_MARKER = "#?endif";

    /**
     * Returns {@code text} with the contents of inactive conditional regions replaced by
     * spaces, or {@code text} itself when there is nothing to do.
     *
     * <p>The {@code #?if} and {@code #?endif} lines are deliberately left alone. They are
     * ordinary Raku comments, so they cost the parser nothing and keeping them means they
     * still highlight as comments rather than turning into a blank gap.
     */
    public static CharSequence preprocess(CharSequence text) {
        // The overwhelming majority of files have no conditionals at all; skip the copy.
        if (indexOf(text, IF_MARKER) < 0) return text;

        char[] masked = null;
        int length = text.length();
        int pos = 0;
        boolean omitting = false;

        while (pos < length) {
            int lineEnd = lineEnd(text, pos);
            int nextLine = nextLineStart(text, lineEnd, length);

            if (startsWith(text, pos, ENDIF_MARKER)) {
                // An #?endif with no #?if open is what gen-cat warns about and ignores.
                omitting = false;
            }
            else if (startsWith(text, pos, IF_MARKER)) {
                // Nested conditionals are an error in gen-cat, so a second #?if simply
                // re-decides rather than pushing a level.
                omitting = isOmitted(text, pos + IF_MARKER.length(), lineEnd);
            }
            else if (omitting) {
                if (masked == null) masked = toCharArray(text);
                for (int i = pos; i < lineEnd; i++) masked[i] = ' ';
            }

            pos = nextLine;
        }

        return masked == null ? text : new String(masked);
    }

    /**
     * Decides whether the region introduced by an {@code #?if} is dead, given the text
     * between the marker and the end of its line.
     *
     * <p>gen-cat requires the whole line to be {@code #?if} plus optional {@code !} plus
     * a bare word: anything else is not a marker and the line is left as the comment it
     * looks like. A region we cannot make sense of is treated as live, so an
     * unrecognised marker can never delete code.
     */
    private static boolean isOmitted(CharSequence text, int from, int lineEnd) {
        int pos = skipSpaces(text, from, lineEnd);
        if (pos == from) return false; // '#?ifsomething' is not a marker

        boolean negated = pos < lineEnd && text.charAt(pos) == '!';
        if (negated) pos = skipSpaces(text, pos + 1, lineEnd);

        int nameStart = pos;
        while (pos < lineEnd && isWordChar(text.charAt(pos))) pos++;
        if (pos == nameStart) return false;

        String name = text.subSequence(nameStart, pos).toString();
        if (skipSpaces(text, pos, lineEnd) != lineEnd) return false; // trailing junk

        return negated == name.equals(BACKEND);
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private static int skipSpaces(CharSequence text, int from, int limit) {
        int pos = from;
        while (pos < limit && (text.charAt(pos) == ' ' || text.charAt(pos) == '\t')) pos++;
        return pos;
    }

    /** Offset of the line terminator at or after {@code from}, or the end of the text. */
    private static int lineEnd(CharSequence text, int from) {
        int pos = from;
        int length = text.length();
        while (pos < length && text.charAt(pos) != '\n' && text.charAt(pos) != '\r') pos++;
        return pos;
    }

    private static int nextLineStart(CharSequence text, int lineEnd, int length) {
        int pos = lineEnd;
        if (pos < length && text.charAt(pos) == '\r') pos++;
        if (pos < length && text.charAt(pos) == '\n') pos++;
        return pos > lineEnd ? pos : lineEnd + 1;
    }

    private static boolean startsWith(CharSequence text, int at, String marker) {
        if (at + marker.length() > text.length()) return false;
        for (int i = 0; i < marker.length(); i++)
            if (text.charAt(at + i) != marker.charAt(i)) return false;
        return true;
    }

    private static int indexOf(CharSequence text, String marker) {
        int limit = text.length() - marker.length();
        for (int at = 0; at <= limit; at++)
            if (startsWith(text, at, marker)) return at;
        return -1;
    }

    private static char[] toCharArray(CharSequence text) {
        int length = text.length();
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) chars[i] = text.charAt(i);
        return chars;
    }
}
