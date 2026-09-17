package org.raku.comma.parsing;

import com.intellij.lang.cacheBuilder.VersionedWordsScanner;
import com.intellij.lang.cacheBuilder.WordOccurrence;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

// A straightforward copy of `DefaultWordsScanner` class,
// tweaked to handle Raku attributes
public class RakuWordsScanner extends VersionedWordsScanner {
    private final Lexer myLexer;
    private final TokenSet myIdentifierTokenSet;
    private final TokenSet myCommentTokenSet;
    private final TokenSet myLiteralTokenSet;

    public RakuWordsScanner() {
        // Branch-aware so words inside inactive #?if branches are find-usages-indexed.
        myLexer = RakuHighlighterLexer.branchAware();
        myIdentifierTokenSet = TokenSet.create(RakuTokenTypes.NAME,
                                               RakuTokenTypes.SUB_CALL_NAME,
                                               RakuTokenTypes.METHOD_CALL_NAME,
                                               RakuTokenTypes.VARIABLE,
                                               RakuTokenTypes.ROUTINE_NAME,
                                               RakuTokenTypes.PACKAGE_NAME);
        myCommentTokenSet = TokenSet.create(RakuTokenTypes.COMMENT,
                                            RakuTokenTypes.POD_TEXT,
                                            RakuTokenTypes.POD_FINISH_TEXT);
        myLiteralTokenSet = TokenSet.create(RakuTokenTypes.INTEGER_LITERAL,
                                            RakuTokenTypes.COMPLEX_LITERAL,
                                            RakuTokenTypes.NUMBER_LITERAL,
                                            RakuTokenTypes.RAT_LITERAL);
    }

    // Bump whenever a lexer/parser change can alter the emitted words for
    // unchanged file text, so persisted IdIndex data is rebuilt (v2:
    // branch-aware lexing; v3: nqp::const:: no-args terms un-swallowed
    // formerly-BAD_CHARACTER regions). ParserChangeVersionGuardTest enforces
    // the pairing.
    public static final int VERSION = 3;

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public void processWords(@NotNull CharSequence fileText, @NotNull Processor<? super WordOccurrence> processor) {
        myLexer.start(fileText);
        WordOccurrence occurrence = new WordOccurrence(fileText, 0, 0, null); // shared occurrence

        IElementType type;
        while ((type = myLexer.getTokenType()) != null) {
            if (myIdentifierTokenSet.contains(type)) {
                String text = myLexer.getTokenText();
                if (type == RakuTokenTypes.VARIABLE) {
                    if (text.length() > 1) {
                        String sigilLess = text.substring(Character.isLetter(text.charAt(1)) ? 1 : 2);
                        if (indexByDash(processor, occurrence, sigilLess)) return;
                    }
                    if (text.startsWith("$")) {
                        occurrence.init("$", 0, 1, WordOccurrence.Kind.CODE);
                        if (!processor.process(occurrence)) return;
                        occurrence.init(text, 0, text.length(), WordOccurrence.Kind.CODE);
                        if (!processor.process(occurrence)) return;
                    }
                } else if (type == RakuTokenTypes.NAME) {
                    String fixedName = text;
                    // If it is nested NAME(`Foo` inside of `Bar` gives `Bar::Foo`), provide a last part
                    // version so that usages like `Bar::Foo` could we matched with `Foo` in `class Foo`
                    if (fixedName.indexOf(':') != -1) {
                        int lastPartIndex = fixedName.lastIndexOf(':') + 1;
                        fixedName = fixedName.substring(lastPartIndex);
                    }
                    occurrence.init(fixedName, 0, fixedName.length(), WordOccurrence.Kind.CODE);
                    if (!processor.process(occurrence)) return;
                } else if (type == RakuTokenTypes.METHOD_CALL_NAME || type == RakuTokenTypes.ROUTINE_NAME ||
                           type == RakuTokenTypes.SUB_CALL_NAME) {
                    if (text.indexOf('-') == -1) {
                        occurrence.init(text, 0, text.length(), WordOccurrence.Kind.CODE);
                        if (!processor.process(occurrence)) return;
                    } else {
                        if (indexByDash(processor, occurrence, text)) return;
                    }
                } else {
                    occurrence.init(text, 0, text.length(), WordOccurrence.Kind.CODE);
                    if (!processor.process(occurrence)) return;
                }
            }
            else if (myCommentTokenSet.contains(type)) {
                if (!stripWords(processor, fileText, myLexer.getTokenStart(), myLexer.getTokenEnd(), WordOccurrence.Kind.COMMENTS, occurrence)) return;
            }
            else if (myLiteralTokenSet.contains(type)) {
                if (!stripWords(processor, fileText, myLexer.getTokenStart(), myLexer.getTokenEnd(), WordOccurrence.Kind.LITERALS, occurrence)) return;
            }
            myLexer.advance();
        }
    }

    private static boolean indexByDash(Processor<? super WordOccurrence> processor, WordOccurrence occurrence, String text) {
        String[] splitted = text.split("-");
        for (int i = 0; i < splitted.length; i++) {
            occurrence.init(splitted[i], 0, splitted[i].length(), WordOccurrence.Kind.CODE);
            if (!processor.process(occurrence)) return true;
            if (i + 1 != splitted.length) {
                occurrence.init("-", 0, 1, WordOccurrence.Kind.CODE);
                if (!processor.process(occurrence)) return true;
            }
        }
        return false;
    }

    protected static boolean stripWords(final Processor<? super WordOccurrence> processor,
                                        final CharSequence tokenText,
                                        int from,
                                        int to,
                                        final WordOccurrence.Kind kind,
                                        @NotNull WordOccurrence occurrence
    ) {
        // This code seems strange, but it is more effective as Character.isJavaIdentifier_xxx_ is quite costly operation due to unicode
        int index = from;

        ScanWordsLoop:
        while (true) {
            while (true) {
                if (index == to) break ScanWordsLoop;
                char c = tokenText.charAt(index);
                if (isAsciiIdentifierPart(c) || Character.isJavaIdentifierStart(c)) {
                    break;
                }
                index++;
            }
            int wordStart = index;
            while (true) {
                index++;
                if (index == to) break;
                char c = tokenText.charAt(index);
                if (isAsciiIdentifierPart(c)) continue;
                if (!Character.isJavaIdentifierPart(c)) break;
            }
            int wordEnd = index;
            occurrence.init(tokenText, wordStart, wordEnd, kind);

            if (!processor.process(occurrence)) return false;
        }
        return true;
    }

    private static boolean isAsciiIdentifierPart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '$';
    }
}
