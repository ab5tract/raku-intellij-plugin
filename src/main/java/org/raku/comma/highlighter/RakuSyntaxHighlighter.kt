package org.raku.comma.highlighter

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import org.raku.comma.parsing.RakuHighlighterLexer
import org.raku.comma.parsing.RakuTokenTypes

/**
 * Lexer-driven highlighting: which [RakuHighlighter] key each token wears.
 *
 * The mapping is many-to-one on purpose -- an open and close bracket share a
 * key, and several distinct tokens collapse onto one concept (a method-call
 * operator and an invocant marker are both [RakuHighlighter.INFIX]) -- so the
 * user tunes one color rather than four.
 *
 * Tokens absent from this map get no lexer color. That is correct for the
 * keys applied later by an annotator or inspection instead: the semantic four,
 * REGEX_SIG_SPACE, the Pod formatting trio, UNUSED and ALT_WARNING. It is also
 * true of HASH_COMPOSER, which nothing applies at all -- see its declaration.
 */
class RakuSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = RakuHighlighterLexer()

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> =
        pack(ATTRIBUTES[tokenType])

    private companion object {
        private val ATTRIBUTES: Map<IElementType, TextAttributesKey> = mapOf(
            RakuTokenTypes.BAD_CHARACTER                   to RakuHighlighter.BAD_CHARACTER,
            RakuTokenTypes.COMMENT                         to RakuHighlighter.COMMENT,
            RakuTokenTypes.COMMENT_STARTER                 to RakuHighlighter.COMMENT,
            RakuTokenTypes.COMMENT_QUOTE_OPEN              to RakuHighlighter.COMMENT,
            RakuTokenTypes.COMMENT_QUOTE_CLOSE             to RakuHighlighter.COMMENT,
            RakuTokenTypes.STATEMENT_CONTROL               to RakuHighlighter.STATEMENT_CONTROL,
            RakuTokenTypes.PHASER                          to RakuHighlighter.PHASER,
            RakuTokenTypes.LABEL_NAME                      to RakuHighlighter.LABEL_NAME,
            RakuTokenTypes.LABEL_COLON                     to RakuHighlighter.LABEL_COLON,
            RakuTokenTypes.STATEMENT_PREFIX                to RakuHighlighter.STATEMENT_PREFIX,
            RakuTokenTypes.STATEMENT_MOD_COND              to RakuHighlighter.STATEMENT_MOD,
            RakuTokenTypes.STATEMENT_MOD_LOOP              to RakuHighlighter.STATEMENT_MOD,
            RakuTokenTypes.SCOPE_DECLARATOR                to RakuHighlighter.SCOPE_DECLARATOR,
            RakuTokenTypes.MULTI_DECLARATOR                to RakuHighlighter.MULTI_DECLARATOR,
            RakuTokenTypes.PACKAGE_DECLARATOR              to RakuHighlighter.PACKAGE_DECLARATOR,
            RakuTokenTypes.ALSO                            to RakuHighlighter.PACKAGE_DECLARATOR,
            RakuTokenTypes.NAME                            to RakuHighlighter.TYPE_NAME,
            RakuTokenTypes.PACKAGE_NAME                    to RakuHighlighter.TYPE_NAME,
            RakuTokenTypes.STATEMENT_TERMINATOR            to RakuHighlighter.STATEMENT_TERMINATOR,
            RakuTokenTypes.PREFIX                          to RakuHighlighter.PREFIX,
            RakuTokenTypes.INFIX                           to RakuHighlighter.INFIX,
            RakuTokenTypes.METAOP                          to RakuHighlighter.METAOP,
            RakuTokenTypes.METHOD_CALL_OPERATOR            to RakuHighlighter.INFIX,
            RakuTokenTypes.INVOCANT_MARKER                 to RakuHighlighter.INFIX,
            RakuTokenTypes.LAMBDA                          to RakuHighlighter.LAMBDA,
            RakuTokenTypes.POSTFIX                         to RakuHighlighter.POSTFIX,
            RakuTokenTypes.ARRAY_INDEX_BRACKET_OPEN        to RakuHighlighter.ARRAY_INDEXER,
            RakuTokenTypes.ARRAY_INDEX_BRACKET_CLOSE       to RakuHighlighter.ARRAY_INDEXER,
            RakuTokenTypes.HASH_INDEX_BRACKET_OPEN         to RakuHighlighter.HASH_INDEXER,
            RakuTokenTypes.HASH_INDEX_BRACKET_CLOSE        to RakuHighlighter.HASH_INDEXER,
            RakuTokenTypes.VARIABLE                        to RakuHighlighter.VARIABLE,
            RakuTokenTypes.CONTEXTUALIZER                  to RakuHighlighter.CONTEXTUALIZER,
            RakuTokenTypes.CONTEXTUALIZER_OPEN             to RakuHighlighter.CONTEXTUALIZER,
            RakuTokenTypes.CONTEXTUALIZER_CLOSE            to RakuHighlighter.CONTEXTUALIZER,
            RakuTokenTypes.SHAPE_DECLARATION               to RakuHighlighter.SHAPE_DECLARATION,
            RakuTokenTypes.TYPE_DECLARATOR                 to RakuHighlighter.TYPE_DECLARATOR,
            RakuTokenTypes.TERM_DECLARATION_BACKSLASH      to RakuHighlighter.TERM_DECLARATION_BACKSLASH,
            RakuTokenTypes.INTEGER_LITERAL                 to RakuHighlighter.NUMERIC_LITERAL,
            RakuTokenTypes.NUMBER_LITERAL                  to RakuHighlighter.NUMERIC_LITERAL,
            RakuTokenTypes.RAT_LITERAL                     to RakuHighlighter.NUMERIC_LITERAL,
            RakuTokenTypes.COMPLEX_LITERAL                 to RakuHighlighter.NUMERIC_LITERAL,
            RakuTokenTypes.RADIX_NUMBER                    to RakuHighlighter.NUMERIC_LITERAL,
            RakuTokenTypes.STRING_LITERAL_QUOTE_SYNTAX     to RakuHighlighter.STRING_LITERAL_QUOTE,
            RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN       to RakuHighlighter.STRING_LITERAL_QUOTE,
            RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE      to RakuHighlighter.STRING_LITERAL_QUOTE,
            RakuTokenTypes.STRING_LITERAL_CHAR             to RakuHighlighter.STRING_LITERAL_CHAR,
            RakuTokenTypes.STRING_LITERAL_ESCAPE           to RakuHighlighter.STRING_LITERAL_ESCAPE,
            RakuTokenTypes.STRING_LITERAL_REQUOTE_ESCAPE   to RakuHighlighter.STRING_LITERAL_ESCAPE,
            RakuTokenTypes.QUOTE_REGEX                     to RakuHighlighter.QUOTE_REGEX,
            RakuTokenTypes.QUOTE_PAIR                      to RakuHighlighter.QUOTE_PAIR,
            RakuTokenTypes.QUOTE_MOD                       to RakuHighlighter.QUOTE_MOD,
            RakuTokenTypes.ARRAY_COMPOSER_OPEN             to RakuHighlighter.ARRAY_COMPOSER,
            RakuTokenTypes.ARRAY_COMPOSER_CLOSE            to RakuHighlighter.ARRAY_COMPOSER,
            RakuTokenTypes.VERSION                         to RakuHighlighter.VERSION,
            RakuTokenTypes.BAD_ESCAPE                      to RakuHighlighter.STRING_LITERAL_BAD_ESCAPE,
            RakuTokenTypes.PARENTHESES_OPEN                to RakuHighlighter.PARENTHESES,
            RakuTokenTypes.PARENTHESES_CLOSE               to RakuHighlighter.PARENTHESES,
            RakuTokenTypes.SIGNATURE_BRACKET_OPEN          to RakuHighlighter.PARENTHESES,
            RakuTokenTypes.SIGNATURE_BRACKET_CLOSE         to RakuHighlighter.PARENTHESES,
            RakuTokenTypes.SUB_CALL_NAME                   to RakuHighlighter.SUB_CALL_NAME,
            RakuTokenTypes.METHOD_CALL_NAME                to RakuHighlighter.METHOD_CALL_NAME,
            RakuTokenTypes.TERM                            to RakuHighlighter.TERM,
            RakuTokenTypes.SELF                            to RakuHighlighter.SELF,
            RakuTokenTypes.WHATEVER                        to RakuHighlighter.WHATEVER,
            RakuTokenTypes.HYPER_WHATEVER                  to RakuHighlighter.WHATEVER,
            RakuTokenTypes.STUB_CODE                       to RakuHighlighter.STUB_CODE,
            RakuTokenTypes.CAPTURE_TERM                    to RakuHighlighter.CAPTURE_TERM,
            RakuTokenTypes.ROUTINE_DECLARATOR              to RakuHighlighter.ROUTINE_DECLARATOR,
            RakuTokenTypes.REGEX_DECLARATOR                to RakuHighlighter.ROUTINE_DECLARATOR,
            RakuTokenTypes.ROUTINE_NAME                    to RakuHighlighter.ROUTINE_NAME,
            RakuTokenTypes.PARAMETER_SEPARATOR             to RakuHighlighter.PARAMETER_SEPARATOR,
            RakuTokenTypes.NAMED_PARAMETER_SYNTAX          to RakuHighlighter.NAMED_PARAMETER_SYNTAX,
            RakuTokenTypes.NAMED_PARAMETER_NAME_ALIAS      to RakuHighlighter.NAMED_PARAMETER_NAME_ALIAS,
            RakuTokenTypes.PARAMETER_QUANTIFIER            to RakuHighlighter.PARAMETER_QUANTIFIER,
            RakuTokenTypes.WHERE_CONSTRAINT                to RakuHighlighter.WHERE_CONSTRAINT,
            RakuTokenTypes.RETURN_ARROW                    to RakuHighlighter.RETURN_ARROW,
            RakuTokenTypes.BLOCK_CURLY_BRACKET_OPEN        to RakuHighlighter.BLOCK_CURLY_BRACKETS,
            RakuTokenTypes.BLOCK_CURLY_BRACKET_CLOSE       to RakuHighlighter.BLOCK_CURLY_BRACKETS,
            RakuTokenTypes.ONLY_STAR                       to RakuHighlighter.ONLY_STAR,
            RakuTokenTypes.PAIR_KEY                        to RakuHighlighter.PAIR_KEY,
            RakuTokenTypes.COLON_PAIR                      to RakuHighlighter.PAIR_KEY,
            RakuTokenTypes.TRAIT                           to RakuHighlighter.TRAIT,
            RakuTokenTypes.TYPE_COERCION_PARENTHESES_OPEN  to RakuHighlighter.TYPE_COERCION_PARENTHESES,
            RakuTokenTypes.TYPE_COERCION_PARENTHESES_CLOSE to RakuHighlighter.TYPE_COERCION_PARENTHESES,
            RakuTokenTypes.TYPE_PARAMETER_BRACKET          to RakuHighlighter.TYPE_PARAMETER_BRACKET,
            RakuTokenTypes.REGEX_INFIX                     to RakuHighlighter.REGEX_INFIX,
            RakuTokenTypes.REGEX_ANCHOR                    to RakuHighlighter.REGEX_ANCHOR,
            RakuTokenTypes.REGEX_GROUP_BRACKET_OPEN        to RakuHighlighter.REGEX_GROUP_BRACKET,
            RakuTokenTypes.REGEX_GROUP_BRACKET_CLOSE       to RakuHighlighter.REGEX_GROUP_BRACKET,
            RakuTokenTypes.REGEX_CAPTURE_PARENTHESES_OPEN  to RakuHighlighter.REGEX_CAPTURE,
            RakuTokenTypes.REGEX_CAPTURE_PARENTHESES_CLOSE to RakuHighlighter.REGEX_CAPTURE,
            RakuTokenTypes.REGEX_CAPTURE_NAME              to RakuHighlighter.REGEX_CAPTURE,
            RakuTokenTypes.REGEX_QUANTIFIER                to RakuHighlighter.REGEX_QUANTIFIER,
            RakuTokenTypes.REGEX_BUILTIN_CCLASS            to RakuHighlighter.REGEX_BUILTIN_CCLASS,
            RakuTokenTypes.REGEX_BACKSLASH_BAD             to RakuHighlighter.REGEX_BACKSLASH_BAD,
            RakuTokenTypes.REGEX_ASSERTION_ANGLE_OPEN      to RakuHighlighter.REGEX_ASSERTION_ANGLE,
            RakuTokenTypes.REGEX_ASSERTION_ANGLE_CLOSE     to RakuHighlighter.REGEX_ASSERTION_ANGLE,
            RakuTokenTypes.REGEX_LOOKAROUND                to RakuHighlighter.REGEX_LOOKAROUND,
            RakuTokenTypes.REGEX_CCLASS_SYNTAX             to RakuHighlighter.REGEX_CCLASS_SYNTAX,
            RakuTokenTypes.REGEX_MOD_INTERNAL              to RakuHighlighter.REGEX_MOD,
            RakuTokenTypes.REGEX_MOD_UNKNOWN               to RakuHighlighter.REGEX_MOD,
            RakuTokenTypes.TRANS_CHAR                      to RakuHighlighter.TRANS_CHAR,
            RakuTokenTypes.TRANS_RANGE                     to RakuHighlighter.TRANS_RANGE,
            RakuTokenTypes.TRANS_ESCAPE                    to RakuHighlighter.TRANS_ESCAPE,
            RakuTokenTypes.TRANS_BAD                       to RakuHighlighter.TRANS_BAD,
            RakuTokenTypes.POD_DIRECTIVE                   to RakuHighlighter.POD_DIRECTIVE,
            RakuTokenTypes.POD_TYPENAME                    to RakuHighlighter.POD_TYPENAME,
            RakuTokenTypes.POD_CONFIGURATION               to RakuHighlighter.POD_CONFIGURATION,
            RakuTokenTypes.POD_TEXT                        to RakuHighlighter.POD_TEXT,
            RakuTokenTypes.POD_CODE                        to RakuHighlighter.POD_CODE,
            RakuTokenTypes.FORMAT_CODE                     to RakuHighlighter.POD_FORMAT_CODE,
            RakuTokenTypes.POD_FORMAT_STARTER              to RakuHighlighter.POD_FORMAT_QUOTES,
            RakuTokenTypes.POD_FORMAT_STOPPER              to RakuHighlighter.POD_FORMAT_QUOTES,
            RakuTokenTypes.POD_FORMAT_SEPARATOR            to RakuHighlighter.POD_FORMAT_QUOTES,
            RakuTokenTypes.QUASI                           to RakuHighlighter.QUASI,
        )
    }
}
