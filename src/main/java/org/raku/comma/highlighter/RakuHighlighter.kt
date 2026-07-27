package org.raku.comma.highlighter

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey

/**
 * Every Raku text attribute, declared once.
 *
 * Each key carries four things at its single declaration site: the external
 * name persisted in user settings, the platform key it falls back to, the
 * group it appears under in Settings | Editor | Color Scheme | Raku, and its
 * label there. [entries] is populated in declaration order as a side effect of
 * [key], so [RakuColorSettingsPage] composes the whole color panel from this
 * file rather than from a second, hand-maintained list. Those two lists had
 * drifted: "Hash Composer" pointed at [ARRAY_COMPOSER], leaving [HASH_COMPOSER]
 * unreachable, and [UNUSED]/[ALT_WARNING] had no entry at all.
 *
 * ## Colors come from the fallback, not from us
 *
 * A key's appearance is whatever its fallback resolves to in the user's active
 * scheme -- we deliberately ship almost no color overrides. `colorSchemes/`
 * used to hardcode ~37 foregrounds for Default and Darcula, which meant Raku
 * looked one way under those two schemes and another way under every third-
 * party theme (which only ever saw the fallbacks). Now every theme gets the
 * same treatment, and retheming Raku is a matter of choosing a scheme. The
 * handful of surviving overrides are listed in `docs/color-principles.md`;
 * each encodes something no platform key expresses, such as Pod `B<>` being
 * bold. Prefer picking a better fallback over adding an override.
 *
 * ## External names are a compatibility surface
 *
 * The strings below are persisted in users' saved color schemes and in
 * the `colorSchemes/` XML. Renaming one silently discards that user's
 * customization of it, so they are frozen -- including the irregular ones the
 * Pod group grew (`POD_DIRECTIVE` is `"RAKU_DIRECTIVE"`, not
 * `"RAKU_POD_DIRECTIVE"`) and `REGEX_CCLASS_SYNTAX` (`"RAKU_CCLASS_SYNTAX"`).
 * `RakuColorSettingsPageTest.testExternalNamesAreFrozen` pins the full set.
 */
object RakuHighlighter {
    /**
     * A section of the color settings tree. [UNGROUPED] entries sit at the
     * top level; everything else nests under [title] via the `//` separator
     * `AttributesDescriptor` understands.
     *
     * These are grouped by Raku language feature -- where a user would go
     * looking for them -- which cuts across the fallback affinities the keys
     * are declared in. "Regex group brackets" belongs under Regex even though
     * it falls back to `BRACKETS` alongside the ordinary indexers.
     */
    enum class Group(val title: String?) {
        UNGROUPED(null),
        BRACES_AND_OPERATORS("Braces and Operators"),
        KEYWORDS("Keywords"),
        NAMES_AND_TYPES("Names and Types"),
        VARIABLES("Variables"),
        SIGNATURES("Signatures and Parameters"),
        LITERALS("Literals"),
        COMMENTS("Comments"),
        REGEX("Regex"),
        TRANSLITERATION("Transliteration"),
        POD("Pod"),
        SEMANTIC("Semantic"),
        DIAGNOSTICS("Diagnostics"),
        ;

        /** The `Group//Label` path a color settings descriptor is named by. */
        fun path(label: String): String = if (title == null) label else "$title//$label"
    }

    /** One key's color-panel presentation, in declaration order. */
    data class Entry(val key: TextAttributesKey, val group: Group, val label: String, val inPanel: Boolean)

    private val mutableEntries = mutableListOf<Entry>()

    /** Every declared key, in declaration order. */
    val entries: List<Entry> get() = mutableEntries

    /** The subset [RakuColorSettingsPage] offers the user, in declaration order. */
    val panelEntries: List<Entry> get() = mutableEntries.filter { it.inPanel }

    /**
     * @param inPanel false for a key nothing currently applies, so the color
     *   panel does not offer a control that visibly does nothing.
     */
    private fun key(
        externalName: String,
        fallback: TextAttributesKey,
        group: Group,
        label: String,
        inPanel: Boolean = true,
    ): TextAttributesKey {
        val key = TextAttributesKey.createTextAttributesKey(externalName, fallback)
        mutableEntries.add(Entry(key, group, label, inPanel))
        return key
    }

    /* Illegal syntax, mapped onto the platform's own rule for it. */

    @JvmField
    val BAD_CHARACTER = key(
        "RAKU_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER,
        Group.UNGROUPED, "Bad syntax"
    )

    /* Braces and operators
     * *******************
     * Brackets, braces and punctuation stay neutral -- they inherit the
     * platform's bracket/brace/comma keys and so match the rest of the IDE.
     * Operators do not: in Raku the term/infix parser interlocking makes
     * operators load-bearing syntax rather than punctuation, so they inherit
     * OPERATION_SIGN, which most schemes give a color of its own. Array and
     * hash composers sit here too -- they read as operators but are bracketed,
     * so they follow BRACKETS.
     */

    @JvmField
    val ARRAY_INDEXER = key(
        "RAKU_ARRAY_INDEXER", DefaultLanguageHighlighterColors.BRACKETS,
        Group.BRACES_AND_OPERATORS, "Array indexer"
    )

    @JvmField
    val HASH_INDEXER = key(
        "RAKU_HASH_INDEXER", DefaultLanguageHighlighterColors.BRACKETS,
        Group.BRACES_AND_OPERATORS, "Hash indexer"
    )

    @JvmField
    val BLOCK_CURLY_BRACKETS = key(
        "RAKU_BLOCK_CURLY_BRACKETS", DefaultLanguageHighlighterColors.BRACES,
        Group.BRACES_AND_OPERATORS, "Block curly braces"
    )

    @JvmField
    val PARENTHESES = key(
        "RAKU_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES,
        Group.BRACES_AND_OPERATORS, "Parentheses"
    )

    @JvmField
    val LAMBDA = key(
        "RAKU_LAMBDA", DefaultLanguageHighlighterColors.BRACES,
        Group.BRACES_AND_OPERATORS, "Lambda (-> and <->)"
    )

    @JvmField
    val STATEMENT_TERMINATOR = key(
        "RAKU_STATEMENT_TERMINATOR", DefaultLanguageHighlighterColors.SEMICOLON,
        Group.BRACES_AND_OPERATORS, "Statement terminator"
    )

    @JvmField
    val TYPE_COERCION_PARENTHESES = key(
        "RAKU_TYPE_COERCION_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES,
        Group.BRACES_AND_OPERATORS, "Type coercion parentheses"
    )

    @JvmField
    val TYPE_PARAMETER_BRACKET = key(
        "RAKU_TYPE_PARAMETER_BRACKET", DefaultLanguageHighlighterColors.CLASS_NAME,
        Group.BRACES_AND_OPERATORS, "Type parameter brackets"
    )

    @JvmField
    val ARRAY_COMPOSER = key(
        "RAKU_ARRAY_COMPOSER", DefaultLanguageHighlighterColors.BRACKETS,
        Group.BRACES_AND_OPERATORS, "Array Composer ([...])"
    )

    // Declared but never applied: the lexer has no HASH_COMPOSER token, so a
    // `{...}` composer arrives as BLOCK_CURLY_BRACKET_OPEN/CLOSE and is
    // colored as an ordinary block brace. Kept because the external name is
    // frozen, and because the slot is the right home for the distinction if
    // the lexer ever draws it -- but kept out of the color panel, since a
    // control that changes nothing is worse than an absent one.
    //
    // The old settings page did offer a "Hash Composer ({...})" row and wired
    // it to ARRAY_COMPOSER, so editing it silently recolored array composers.
    @JvmField
    val HASH_COMPOSER = key(
        "RAKU_HASH_COMPOSER", DefaultLanguageHighlighterColors.BRACKETS,
        Group.BRACES_AND_OPERATORS, "Hash Composer ({...})", inPanel = false
    )

    @JvmField
    val PREFIX = key(
        "RAKU_PREFIX", DefaultLanguageHighlighterColors.OPERATION_SIGN,
        Group.BRACES_AND_OPERATORS, "Prefix operator"
    )

    @JvmField
    val INFIX = key(
        "RAKU_INFIX", DefaultLanguageHighlighterColors.OPERATION_SIGN,
        Group.BRACES_AND_OPERATORS, "Infix operator"
    )

    @JvmField
    val POSTFIX = key(
        "RAKU_POSTFIX", DefaultLanguageHighlighterColors.OPERATION_SIGN,
        Group.BRACES_AND_OPERATORS, "Postfix operator"
    )

    @JvmField
    val METAOP = key(
        "RAKU_METAOP", DefaultLanguageHighlighterColors.OPERATION_SIGN,
        Group.BRACES_AND_OPERATORS, "Meta-operator"
    )

    @JvmField
    val CONTEXTUALIZER = key(
        "RAKU_CONTEXTUALIZER", DefaultLanguageHighlighterColors.OPERATION_SIGN,
        Group.BRACES_AND_OPERATORS, "Contextualizer"
    )

    /* Keywords
     * ********
     * Every flavour of keyword inherits KEYWORD, so they share one color the
     * user can retheme in a single place. Labels inherit the platform's LABEL.
     */

    @JvmField
    val SCOPE_DECLARATOR = key(
        "RAKU_SCOPE_DECLARATOR", DefaultLanguageHighlighterColors.KEYWORD,
        Group.KEYWORDS, "Scope keyword"
    )

    @JvmField
    val MULTI_DECLARATOR = key(
        "RAKU_MULTI_DECLARATOR", DefaultLanguageHighlighterColors.KEYWORD,
        Group.KEYWORDS, "Multi keyword"
    )

    @JvmField
    val ROUTINE_DECLARATOR = key(
        "RAKU_ROUTINE_DECLARATOR", DefaultLanguageHighlighterColors.KEYWORD,
        Group.KEYWORDS, "Routine keyword"
    )

    @JvmField
    val PACKAGE_DECLARATOR = key(
        "RAKU_PACKAGE_DECLARATOR", DefaultLanguageHighlighterColors.KEYWORD,
        Group.KEYWORDS, "Package keyword"
    )

    @JvmField
    val TYPE_DECLARATOR = key(
        "RAKU_TYPE_DECLARATOR", DefaultLanguageHighlighterColors.KEYWORD,
        Group.KEYWORDS, "Type Declarator (enum, subset, constant)"
    )

    @JvmField
    val STATEMENT_CONTROL = key(
        "RAKU_STATEMENT_CONTROL", DefaultLanguageHighlighterColors.KEYWORD,
        Group.KEYWORDS, "Statement control"
    )

    @JvmField
    val STATEMENT_PREFIX = key(
        "RAKU_STATEMENT_PREFIX", DefaultLanguageHighlighterColors.KEYWORD,
        Group.KEYWORDS, "Statement prefix"
    )

    @JvmField
    val STATEMENT_MOD = key(
        "RAKU_STATEMENT_MOD", DefaultLanguageHighlighterColors.KEYWORD,
        Group.KEYWORDS, "Statement modifier"
    )

    @JvmField
    val PHASER = key(
        "RAKU_PHASER", DefaultLanguageHighlighterColors.KEYWORD,
        Group.KEYWORDS, "Phaser"
    )

    @JvmField
    val TRAIT = key(
        "RAKU_TRAIT", DefaultLanguageHighlighterColors.KEYWORD,
        Group.KEYWORDS, "Trait keyword"
    )

    @JvmField
    val QUASI = key(
        "RAKU_QUASI", DefaultLanguageHighlighterColors.KEYWORD,
        Group.KEYWORDS, "Quasi quote"
    )

    @JvmField
    val WHERE_CONSTRAINT = key(
        "RAKU_WHERE_CONSTRAINT", DefaultLanguageHighlighterColors.KEYWORD,
        Group.KEYWORDS, "Parameter or variable constraint (where)"
    )

    @JvmField
    val LABEL_NAME = key(
        "RAKU_LABEL_NAME", DefaultLanguageHighlighterColors.LABEL,
        Group.KEYWORDS, "Label name"
    )

    @JvmField
    val LABEL_COLON = key(
        "RAKU_LABEL_COLON", DefaultLanguageHighlighterColors.LABEL,
        Group.KEYWORDS, "Label colon"
    )

    /* Names and types
     * ***************
     * Callables inherit FUNCTION_CALL/FUNCTION_DECLARATION and types inherit
     * CLASS_NAME, so Raku picks up whatever distinction the scheme already
     * draws between calling something and declaring it.
     */

    @JvmField
    val TYPE_NAME = key(
        "RAKU_TYPE_NAME", DefaultLanguageHighlighterColors.CLASS_NAME,
        Group.NAMES_AND_TYPES, "Type name"
    )

    @JvmField
    val TERM = key(
        "RAKU_TERM", DefaultLanguageHighlighterColors.CLASS_NAME,
        Group.NAMES_AND_TYPES, "Other terms (including user defined)"
    )

    @JvmField
    val ROUTINE_NAME = key(
        "RAKU_ROUTINE_NAME", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION,
        Group.NAMES_AND_TYPES, "Routine name"
    )

    @JvmField
    val SUB_CALL_NAME = key(
        "RAKU_SUB_CALL_NAME", DefaultLanguageHighlighterColors.FUNCTION_CALL,
        Group.NAMES_AND_TYPES, "Sub call name"
    )

    @JvmField
    val METHOD_CALL_NAME = key(
        "RAKU_METHOD_CALL_NAME", DefaultLanguageHighlighterColors.FUNCTION_CALL,
        Group.NAMES_AND_TYPES, "Method call name"
    )

    @JvmField
    val SELF = key(
        "RAKU_SELF", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL,
        Group.NAMES_AND_TYPES, "Current Object (self, sigil in \$.foo(...))"
    )

    @JvmField
    val WHATEVER = key(
        "RAKU_WHATEVER", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL,
        Group.NAMES_AND_TYPES, "Whatever"
    )

    @JvmField
    val ONLY_STAR = key(
        "RAKU_ONLY_STAR", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL,
        Group.NAMES_AND_TYPES, "Only Star (Protos)"
    )

    @JvmField
    val CAPTURE_TERM = key(
        "RAKU_CAPTURE_TERM", DefaultLanguageHighlighterColors.PARENTHESES,
        Group.NAMES_AND_TYPES, "Argument Capture (\\\$foo, \\(\$a, \$b))"
    )

    @JvmField
    val TERM_DECLARATION_BACKSLASH = key(
        "RAKU_TERM_DECLARATION_BACKSLASH", DefaultLanguageHighlighterColors.COMMA,
        Group.NAMES_AND_TYPES, "Term Declaration Backslash (my \\answer = 42)"
    )

    /* Variables
     * *********
     * These inherit LOCAL_VARIABLE, which schemes reliably distinguish from
     * both types and callables.
     */

    @JvmField
    val VARIABLE = key(
        "RAKU_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE,
        Group.VARIABLES, "Variable"
    )

    @JvmField
    val SHAPE_DECLARATION = key(
        "RAKU_SHAPE_DECLARATION", DefaultLanguageHighlighterColors.LOCAL_VARIABLE,
        Group.VARIABLES, "Variable shape declaration"
    )

    /* Signatures and parameters */

    @JvmField
    val PARAMETER_SEPARATOR = key(
        "RAKU_PARAMETER_SEPARATOR", DefaultLanguageHighlighterColors.COMMA,
        Group.SIGNATURES, "Parameter separator"
    )

    @JvmField
    val NAMED_PARAMETER_SYNTAX = key(
        "RAKU_NAMED_PARAMETER_SYNTAX", DefaultLanguageHighlighterColors.PARENTHESES,
        Group.SIGNATURES, "Named parameter colon and parentheses"
    )

    @JvmField
    val NAMED_PARAMETER_NAME_ALIAS = key(
        "RAKU_NAMED_PARAMETER_NAME_ALIAS", DefaultLanguageHighlighterColors.PARAMETER,
        Group.SIGNATURES, "Named parameter name alias"
    )

    @JvmField
    val PARAMETER_QUANTIFIER = key(
        "RAKU_PARAMETER_QUANTIFIER", DefaultLanguageHighlighterColors.OPERATION_SIGN,
        Group.SIGNATURES, "Parameter quantifier (slurpy, optional, required)"
    )

    @JvmField
    val RETURN_ARROW = key(
        "RAKU_RETURN_ARROW", DefaultLanguageHighlighterColors.COMMA,
        Group.SIGNATURES, "Return type arrow (-->)"
    )

    /* Literals
     * ********
     * String-ish syntax inherits STRING and numeric-ish inherits NUMBER,
     * including the quoting configuration that surrounds them.
     */

    @JvmField
    val STRING_LITERAL_QUOTE = key(
        "RAKU_STRING_LITERAL_QUOTE", DefaultLanguageHighlighterColors.STRING,
        Group.LITERALS, "String literal quote"
    )

    @JvmField
    val STRING_LITERAL_CHAR = key(
        "RAKU_STRING_LITERAL_CHAR", DefaultLanguageHighlighterColors.STRING,
        Group.LITERALS, "String literal value"
    )

    @JvmField
    val STRING_LITERAL_ESCAPE = key(
        "RAKU_STRING_LITERAL_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE,
        Group.LITERALS, "String literal escape"
    )

    @JvmField
    val STRING_LITERAL_BAD_ESCAPE = key(
        "RAKU_STRING_LITERAL_BAD_ESCAPE", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE,
        Group.LITERALS, "String literal invalid escape"
    )

    @JvmField
    val QUOTE_PAIR = key(
        "RAKU_QUOTE_PAIR", DefaultLanguageHighlighterColors.STRING,
        Group.LITERALS, "Quote Pair (on string and regex literals)"
    )

    @JvmField
    val QUOTE_MOD = key(
        "RAKU_QUOTE_MOD", DefaultLanguageHighlighterColors.STRING,
        Group.LITERALS, "Quote modifier"
    )

    @JvmField
    val PAIR_KEY = key(
        "RAKU_PAIR_KEY", DefaultLanguageHighlighterColors.STRING,
        Group.LITERALS, "Pair (colon pair or key before =>)"
    )

    @JvmField
    val NUMERIC_LITERAL = key(
        "RAKU_NUMERIC_LITERAL", DefaultLanguageHighlighterColors.NUMBER,
        Group.LITERALS, "Numeric literal"
    )

    @JvmField
    val VERSION = key(
        "RAKU_VERSION", DefaultLanguageHighlighterColors.NUMBER,
        Group.LITERALS, "Version literal"
    )

    /* Comments */

    @JvmField
    val COMMENT = key(
        "RAKU_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT,
        Group.COMMENTS, "Comment"
    )

    @JvmField
    val STUB_CODE = key(
        "RAKU_STUB_CODE", DefaultLanguageHighlighterColors.LINE_COMMENT,
        Group.COMMENTS, "Stub Code (..., ???, !!!)"
    )

    /* Regex
     * *****
     * Regex operators inherit OPERATION_SIGN and regex brackets inherit
     * BRACKETS, mirroring the main language. Character classes inherit the
     * string-escape keys, since that is what they are: an escape that stands
     * for a set of characters.
     *
     * REGEX_SIG_SPACE falls back to FUNCTION_CALL on purpose -- sigspace in a
     * `rule` is an implicit `<.ws>` call, and the underline that makes it
     * visible is one of the few overrides `colorSchemes/` still ships.
     */

    @JvmField
    val QUOTE_REGEX = key(
        "RAKU_QUOTE_REGEX", DefaultLanguageHighlighterColors.STRING,
        Group.REGEX, "Literal quote"
    )

    @JvmField
    val REGEX_INFIX = key(
        "RAKU_REGEX_INFIX", DefaultLanguageHighlighterColors.OPERATION_SIGN,
        Group.REGEX, "Infix (alternation, conjunction, goal)"
    )

    @JvmField
    val REGEX_ANCHOR = key(
        "RAKU_REGEX_ANCHOR", DefaultLanguageHighlighterColors.OPERATION_SIGN,
        Group.REGEX, "Anchor"
    )

    @JvmField
    val REGEX_QUANTIFIER = key(
        "RAKU_REGEX_QUANTIFIER", DefaultLanguageHighlighterColors.OPERATION_SIGN,
        Group.REGEX, "Quantifier"
    )

    @JvmField
    val REGEX_LOOKAROUND = key(
        "RAKU_REGEX_LOOKAROUND", DefaultLanguageHighlighterColors.OPERATION_SIGN,
        Group.REGEX, "Lookaround (? and !)"
    )

    @JvmField
    val REGEX_MOD = key(
        "RAKU_REGEX_MOD", DefaultLanguageHighlighterColors.OPERATION_SIGN,
        Group.REGEX, "Modifier"
    )

    @JvmField
    val REGEX_GROUP_BRACKET = key(
        "RAKU_REGEX_GROUP_BRACKET", DefaultLanguageHighlighterColors.BRACKETS,
        Group.REGEX, "Group (square brackets)"
    )

    @JvmField
    val REGEX_ASSERTION_ANGLE = key(
        "RAKU_REGEX_ASSERTION_ANGLE", DefaultLanguageHighlighterColors.BRACKETS,
        Group.REGEX, "Assertion angle brackets"
    )

    // Frozen as RAKU_CCLASS_SYNTAX -- predates the RAKU_REGEX_ prefix.
    @JvmField
    val REGEX_CCLASS_SYNTAX = key(
        "RAKU_CCLASS_SYNTAX", DefaultLanguageHighlighterColors.BRACKETS,
        Group.REGEX, "Character class syntax"
    )

    @JvmField
    val REGEX_CAPTURE = key(
        "RAKU_REGEX_CAPTURE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE,
        Group.REGEX, "Capture"
    )

    @JvmField
    val REGEX_BUILTIN_CCLASS = key(
        "RAKU_REGEX_BUILTIN_CCLASS", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE,
        Group.REGEX, "Built-in character class"
    )

    @JvmField
    val REGEX_BACKSLASH_BAD = key(
        "RAKU_REGEX_BACKSLASH_BAD", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE,
        Group.REGEX, "Invalid backslash sequence"
    )

    @JvmField
    val REGEX_SIG_SPACE = key(
        "RAKU_REGEX_SIG_SPACE", DefaultLanguageHighlighterColors.FUNCTION_CALL,
        Group.REGEX, "Rule Sigspace (implicit <.ws> call)"
    )

    /* Transliteration */

    @JvmField
    val TRANS_CHAR = key(
        "RAKU_TRANS_CHAR", DefaultLanguageHighlighterColors.STRING,
        Group.TRANSLITERATION, "Literal character"
    )

    @JvmField
    val TRANS_ESCAPE = key(
        "RAKU_TRANS_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE,
        Group.TRANSLITERATION, "Escape"
    )

    @JvmField
    val TRANS_RANGE = key(
        "RAKU_TRANS_RANGE", DefaultLanguageHighlighterColors.OPERATION_SIGN,
        Group.TRANSLITERATION, "Range operator"
    )

    @JvmField
    val TRANS_BAD = key(
        "RAKU_TRANS_BAD", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE,
        Group.TRANSLITERATION, "Invalid syntax"
    )

    /* Pod
     * ***
     * Inherits the platform's documentation keys. The external names here
     * predate the POD_ field prefix and are frozen without it.
     *
     * POD_TEXT_BOLD/ITALIC/UNDERLINE are the clearest case for shipping an
     * override: `B<>` has to render bold to mean anything, and no platform
     * key carries that. See `colorSchemes/`.
     */

    @JvmField
    val POD_DIRECTIVE = key(
        "RAKU_DIRECTIVE", DefaultLanguageHighlighterColors.DOC_COMMENT_TAG,
        Group.POD, "Directive"
    )

    @JvmField
    val POD_TYPENAME = key(
        "RAKU_TYPENAME", DefaultLanguageHighlighterColors.DOC_COMMENT_TAG,
        Group.POD, "Typename"
    )

    @JvmField
    val POD_CONFIGURATION = key(
        "RAKU_CONFIGURATION", DefaultLanguageHighlighterColors.DOC_COMMENT_TAG,
        Group.POD, "Configuration"
    )

    @JvmField
    val POD_TEXT = key(
        "RAKU_TEXT", DefaultLanguageHighlighterColors.DOC_COMMENT,
        Group.POD, "Text"
    )

    @JvmField
    val POD_TEXT_BOLD = key(
        "RAKU_TEXT_BOLD", DefaultLanguageHighlighterColors.DOC_COMMENT,
        Group.POD, "Text (Bold)"
    )

    @JvmField
    val POD_TEXT_ITALIC = key(
        "RAKU_TEXT_ITALIC", DefaultLanguageHighlighterColors.DOC_COMMENT,
        Group.POD, "Text (Italic)"
    )

    @JvmField
    val POD_TEXT_UNDERLINE = key(
        "RAKU_TEXT_UNDERLINE", DefaultLanguageHighlighterColors.DOC_COMMENT,
        Group.POD, "Text (Underlined)"
    )

    @JvmField
    val POD_CODE = key(
        "RAKU_CODE", DefaultLanguageHighlighterColors.DOC_COMMENT_MARKUP,
        Group.POD, "Code block"
    )

    @JvmField
    val POD_FORMAT_CODE = key(
        "RAKU_FORMAT_CODE", DefaultLanguageHighlighterColors.DOC_COMMENT_TAG,
        Group.POD, "Format code"
    )

    @JvmField
    val POD_FORMAT_QUOTES = key(
        "RAKU_FORMAT_QUOTES", DefaultLanguageHighlighterColors.DOC_COMMENT_TAG,
        Group.POD, "Format delimiters"
    )

    /* Semantic (resolution-based)
     * ***************************
     * Applied by RakuSemanticAnnotator on top of the lexer-driven keys above,
     * once a reference has actually resolved.
     */

    @JvmField
    val BUILTIN_VARIABLE = key(
        "RAKU_BUILTIN_VARIABLE", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL,
        Group.SEMANTIC, "Built-in variable"
    )

    @JvmField
    val BUILTIN_CALL = key(
        "RAKU_BUILTIN_CALL", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL,
        Group.SEMANTIC, "Built-in call"
    )

    @JvmField
    val REASSIGNED_LOCAL_VARIABLE = key(
        "RAKU_REASSIGNED_LOCAL_VARIABLE", DefaultLanguageHighlighterColors.REASSIGNED_LOCAL_VARIABLE,
        Group.SEMANTIC, "Reassigned local variable"
    )

    @JvmField
    val REASSIGNED_PARAMETER = key(
        "RAKU_REASSIGNED_PARAMETER", DefaultLanguageHighlighterColors.REASSIGNED_PARAMETER,
        Group.SEMANTIC, "Reassigned parameter"
    )

    /* Diagnostics
     * ***********
     * Applied by inspections rather than the lexer. Exposed in the color
     * panel because they are overlays a user may well want to tune -- both
     * were previously undiscoverable there.
     */

    @JvmField
    val UNUSED = key(
        "RAKU_UNUSED", CodeInsightColors.NOT_USED_ELEMENT_ATTRIBUTES,
        Group.DIAGNOSTICS, "Unused declaration"
    )

    @JvmField
    val ALT_WARNING = key(
        "RAKU_ALT_WARNING", CodeInsightColors.WEAK_WARNING_ATTRIBUTES,
        Group.DIAGNOSTICS, "Alternate weak warning"
    )
}
