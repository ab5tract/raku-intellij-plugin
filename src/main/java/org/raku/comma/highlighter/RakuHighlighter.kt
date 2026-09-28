package org.raku.comma.highlighter

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color
import java.awt.Font

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
 * When what you want is a fallback's colour *plus* a font style -- "this, but
 * italic" -- you do not need an override at all, and a fallback cannot express
 * it. Layer [Style.ITALIC] or [Style.BOLD] over the base key instead; see
 * [Style] for why a second key is the only way and how to apply one.
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

    /**
     * One key's declaration: how it is presented in the color panel, and what
     * it adds on top of the colour it resolves to.
     *
     * [fontStyle] and [effect] are the decoration a fallback cannot carry --
     * see [Style] and [StyleEffect] for why neither can live on the key
     * itself. Recording them here rather than at the point of application
     * keeps them from becoming the second hand-maintained list this class
     * exists to avoid.
     */
    data class Entry(
        val key: TextAttributesKey,
        val group: Group,
        val label: String,
        val inPanel: Boolean,
        val fontStyle: Int = Font.PLAIN,
        val effect: StyleEffect? = null,
    )

    private val mutableEntries = mutableListOf<Entry>()

    /** Every declared key, in declaration order. */
    val entries: List<Entry> get() = mutableEntries

    /** The subset [RakuColorSettingsPage] offers the user, in declaration order. */
    val panelEntries: List<Entry> get() = mutableEntries.filter { it.inPanel }

    /**
     * @param inPanel false for a key nothing currently applies, so the color
     *   panel does not offer a control that visibly does nothing.
     * @param fontStyle a [Font] constant this key is always drawn with, on
     *   top of whatever colour it resolves to. Declared here rather than at
     *   the point of application, so the two cannot drift.
     * @param effect likewise for an effect, which unlike a font style can
     *   only ever be computed against a live scheme -- see [StyleEffect].
     */
    private fun key(
        externalName: String,
        fallback: TextAttributesKey,
        group: Group,
        label: String,
        inPanel: Boolean = true,
        fontStyle: Int = Font.PLAIN,
        effect: StyleEffect? = null,
    ): TextAttributesKey {
        val key = TextAttributesKey.createTextAttributesKey(externalName, fallback)
        mutableEntries.add(Entry(key, group, label, inPanel, fontStyle, effect))
        return key
    }

    /**
     * What [key] should actually be drawn as in [scheme]: the colour it
     * resolves to, plus whatever decoration its declaration asked for.
     *
     * This is what callers want almost always. Applying a decorated key by
     * handing the *key* to a range highlighter silently drops the decoration,
     * because the decoration is exactly the part a `TextAttributesKey` cannot
     * carry -- so anything that draws one of these has to come through here.
     */
    @JvmStatic
    fun decoratedAttributes(scheme: EditorColorsScheme, key: TextAttributesKey): TextAttributes {
        val entry = mutableEntries.firstOrNull { it.key == key }
            ?: return scheme.getAttributes(key) ?: TextAttributes()
        entry.effect?.let { return it.of(scheme, key) }
        return styledAttributes(scheme, key, entry.fontStyle)
    }

    /**
     * Effects you can derive over a run: the counterpart to [Style], and the
     * only form an effect can take.
     *
     * [Style] can be a constant because a font style is just an `int` the
     * editor ORs in. An effect cannot: it needs a *colour*, and the only
     * colour that follows the user's theme is one read off the run it is
     * being drawn over -- which means it cannot exist until a scheme does.
     * See [Style] for the measurements behind that.
     *
     * So this is an enum of what we can draw, each value routing to the same
     * derivation with its own [type]. Adding an effect means adding a value
     * here, not threading another `EffectType` through call sites.
     *
     * ## Choosing a method
     *
     * ```kotlin
     * // the common case: a key's effect, in the colour that key resolves to
     * StyleEffect.BOLD_DOTTED_LINE.of(scheme, RakuHighlighter.REGEX_SIG_SPACE)
     *
     * // over a run you already composed -- this is what lets effects stack
     * // on top of Style the way Style stacks on a base
     * val styled = TextAttributes.merge(base, scheme.getAttributes(Style.BOLD))
     * StyleEffect.BOLD_DOTTED_LINE.addedTo(styled, scheme)
     * ```
     *
     * [of] and [over] return the effect **alone**, with no foreground, so the
     * run keeps the colour of whatever sits under it. [addedTo] is the merged
     * form, for when you want the whole thing back.
     *
     * **Worked example:** `PodFormatterInspection`'s `U<>` branch --
     * `StyleEffect.LINE_UNDERSCORE.of(scheme, POD_TEXT_UNDERLINE)`. It sits
     * beside the two [styledAttributes] calls for `B<>` and `I<>`, so the
     * three read as one shape: the key supplies the colour, the helper
     * supplies the decoration.
     *
     * ## A configured effect wins
     *
     * If the resolved attributes already carry an effect colour, that effect
     * is honoured verbatim and [type] is ignored: a non-null effect colour is
     * the only evidence available that a user set one, since
     * [TextAttributes.getEffectType] defaults to [EffectType.BOXED] whether
     * or not anything asked for it.
     *
     * The corollary bites when picking fallbacks: a platform key that already
     * carries an effect of its own will defeat this derivation. That is why
     * Pod `U<>` does not fall back to `REASSIGNED_LOCAL_VARIABLE`, tempting
     * as the name is -- the platform draws reassigned variables underlined,
     * so the key ships an `EFFECT_COLOR`.
     */
    enum class StyleEffect(private val type: EffectType) {
        /** Sigspace and Pod `U<>`-style emphasis: visible on blank runs. */
        BOLD_DOTTED_LINE(EffectType.BOLD_DOTTED_LINE),

        /** A plain underline, for markup that means "underlined". */
        LINE_UNDERSCORE(EffectType.LINE_UNDERSCORE),
        ;

        /** [key]'s effect, coloured from what [scheme] resolves the key to. */
        fun of(scheme: EditorColorsScheme, key: TextAttributesKey): TextAttributes =
            derive(scheme.getAttributes(key), scheme.defaultForeground)

        /** The effect alone, coloured from [under]'s own foreground. */
        fun over(under: TextAttributes?, scheme: EditorColorsScheme): TextAttributes =
            derive(under, scheme.defaultForeground)

        /** [under] with this effect merged in -- colour and style preserved. */
        fun addedTo(under: TextAttributes, scheme: EditorColorsScheme): TextAttributes =
            TextAttributes.merge(under, over(under, scheme))

        private fun derive(under: TextAttributes?, fallbackColor: Color): TextAttributes {
            val attributes = TextAttributes()
            if (under?.effectColor != null) {
                attributes.effectColor = under.effectColor
                attributes.effectType = under.effectType
            } else {
                attributes.effectColor = under?.foregroundColor ?: fallbackColor
                attributes.effectType = type
            }
            return attributes
        }
    }

    /**
     * Font styles you can layer over any other key -- the closest thing the
     * platform has to "`DOC_COMMENT` but italic".
     *
     * ## Why a second key, rather than italics on the first one
     *
     * A fallback is a *lookup*, not a composition.
     * `EditorColorsSchemeImpl.getAttributes` returns any directly defined
     * attributes **whole** and consults `getFallbackAttributeKey` only when
     * there are none -- so the moment a key carries a font style of its own it
     * leaves the fallback path and loses the colour it was inheriting. There
     * is no way to say "this fallback, plus italics" at a declaration site,
     * and `colorSchemes/` cannot say it either: a `<value>` block, even a
     * one-line `FONT_TYPE`, takes the key out of the fallback path the same
     * way. See `docs/color-principles.md`.
     *
     * Composition happens a layer up instead. Hand the editor **both** keys
     * and `TextAttributes.merge` keeps the lower layer's foreground where the
     * upper one is null and ORs the two font types together -- so a
     * colourless font-style key over a colour-bearing base renders as styled
     * text in the active theme's own colour.
     *
     * ## Using one
     *
     * `SyntaxHighlighterBase.pack` is variadic, and the editor merges the keys
     * it returns in order, so a lexer token composes at the call site:
     *
     * ```kotlin
     * // in RakuSyntaxHighlighter.getTokenHighlights
     * pack(RakuHighlighter.POD_TEXT, RakuHighlighter.Style.ITALIC)
     * ```
     *
     * For code that hands the editor explicit attributes rather than keys --
     * an annotator, or a range highlighter -- use [styledAttributes], which
     * does the same merge eagerly against a scheme. That is what Pod's `B<>`
     * and `I<>` use: see `PodFormatterInspection`, the worked example for
     * both helpers.
     *
     * Reach for [Style] over [styledAttributes] when the run underneath is
     * already coloured by something else and must keep that colour -- a
     * modifier contributes no foreground at all, where [styledAttributes]
     * imposes the one its key resolves to.
     *
     * ## Why there is no `Style.UNDERLINE` or `Style.BOLD_DOTTED_LINE`
     *
     * Font styles can be static; effects cannot, and the reason is in how
     * `TextAttributes.merge` treats the two. `fontType` is an `int`, and merge
     * **ORs** it -- so two layers genuinely combine, which is the whole trick
     * above. An effect is a colour-and-type *pair*, and merge **picks**: the
     * layer supplying a non-null `effectColor` contributes both halves and the
     * other layer's effect is discarded entire.
     *
     * Measured, so it is not an argument from the docs:
     *
     * | below | above | merged |
     * |---|---|---|
     * | no effect | type, colour null | effect dropped |
     * | no effect | type, colour set | draws the above |
     * | colour + `LINE_UNDERSCORE` | `BOLD_DOTTED_LINE`, colour null | below wins; type discarded |
     *
     * The third row is the one that kills the idea. Even when the layer below
     * already has a perfectly good effect colour sitting there, a type-only
     * modifier does not pair with it. And a modifier cannot simply carry a
     * colour: it is colourless by design -- that is what lets it layer without
     * overwriting what is underneath -- and a literal would be the
     * theme-specific hardcode described below.
     *
     * Giving it a literal colour is not the way out: that is the
     * theme-specific hardcode `docs/color-principles.md` argues against, and
     * it is the exact mistake `RAKU_TEXT_UNDERLINE` and `RAKU_REGEX_SIG_SPACE`
     * used to ship -- one literal for Default, another for Darcula, and
     * nothing at all for every other theme.
     *
     * So an effect has to have its colour resolved against the live scheme at
     * apply time, which is what [StyleEffect] is for. The split is the
     * whole story: **[Style] is the static half, [StyleEffect] the
     * dynamic half**, and which one a thing needs is decided by whether it
     * requires a colour to render.
     *
     * ## Why these carry default attributes instead of a `colorSchemes/` entry
     *
     * A font style is not a colour, so hardcoding it is not the
     * theme-specific hardcode `docs/color-principles.md` argues against --
     * italic is italic in every theme. Carrying it as the key's *default*
     * attributes rather than a `colorSchemes/` override means it applies under
     * every scheme, including third-party themes that derive from neither
     * Default nor Darcula and so never see our `additionalTextAttributes` at
     * all. It also keeps the override set at the three the
     * `RakuColorSettingsPageTest` pins.
     *
     * These deliberately do **not** go through [key]: they are not colours a
     * user should configure, so they get no entry in the color panel. Their
     * external names are still a compatibility surface, so treat them as
     * frozen like the rest.
     */
    object Style {
        /**
         * The default-attributes overload is deprecated, and used knowingly.
         *
         * The platform deprecates it to push everything onto fallback keys,
         * which is right for colours and impossible here: a fallback resolves
         * to *another key's* attributes, and there is no platform key whose
         * attributes are "italic and nothing else". The alternative is a
         * `colorSchemes/` `FONT_TYPE` entry, which is what this replaced --
         * it is invisible to any theme not derived from Default or Darcula,
         * so Pod `B<>` rendered unbolded under every third-party theme.
         *
         * If the overload is removed, the fallback is to go back to
         * `colorSchemes/` entries and accept that gap, or to drop [Style] and
         * compute attributes at apply time the way [StyleEffect] must.
         */
        @Suppress("DEPRECATION")
        private fun modifier(externalName: String, fontType: Int): TextAttributesKey =
            TextAttributesKey.createTextAttributesKey(
                externalName,
                TextAttributes(null, null, null, null, fontType)
            )

        @JvmField val BOLD: TextAttributesKey = modifier("RAKU_STYLE_BOLD", Font.BOLD)
        @JvmField val ITALIC: TextAttributesKey = modifier("RAKU_STYLE_ITALIC", Font.ITALIC)
        @JvmField val BOLD_ITALIC: TextAttributesKey =
            modifier("RAKU_STYLE_BOLD_ITALIC", Font.BOLD or Font.ITALIC)
    }

    /**
     * [key] as the scheme resolves it, with [fontType] OR-ed into its style.
     *
     * The eager counterpart to [Style], for callers that hand over
     * [TextAttributes] rather than keys. Everything else the key resolves to
     * -- foreground, background, effect -- is preserved, so this really is
     * "that key, but bold/italic".
     *
     * Use [Style] instead wherever the consumer takes keys: it defers to the
     * scheme at paint time and so keeps following the theme, where this
     * snapshots whatever the scheme said when it was called.
     *
     * Note this differs from [StyleEffect], which deliberately leaves
     * the foreground null so the run keeps the colour of whatever sits under
     * it. If you are layering over text something else already coloured and
     * must not disturb it, you want a bare font style ([Style]), not this.
     *
     * **Worked example:** `PodFormatterInspection`'s `B<>` and `I<>`
     * branches -- `styledAttributes(scheme, POD_TEXT_BOLD, Font.BOLD)`. Those
     * keys fall back alongside [POD_TEXT], so the run keeps the colour of the
     * text around it unless someone configures one, and the helper adds the
     * weight the key cannot carry.
     */
    @JvmStatic
    fun styledAttributes(
        scheme: EditorColorsScheme,
        key: TextAttributesKey,
        fontType: Int,
    ): TextAttributes {
        val resolved = scheme.getAttributes(key)?.clone() ?: TextAttributes()
        resolved.fontType = resolved.fontType or fontType
        return resolved
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
        "RAKU_HASH_COMPOSER", DefaultLanguageHighlighterColors.OPERATION_SIGN,
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
        "RAKU_POSTFIX", DefaultLanguageHighlighterColors.MARKUP_ATTRIBUTE,
        Group.BRACES_AND_OPERATORS, "Postfix operator"
    )

    @JvmField
    val METAOP = key(
        "RAKU_METAOP", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL,
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
        "RAKU_MULTI_DECLARATOR", DefaultLanguageHighlighterColors.INLINE_PARAMETER_HINT_HIGHLIGHTED,
        Group.KEYWORDS, "Multi keyword"
    )

    @JvmField
    val ROUTINE_DECLARATOR = key(
        "RAKU_ROUTINE_DECLARATOR", DefaultLanguageHighlighterColors.INLINE_PARAMETER_HINT_HIGHLIGHTED,
        Group.KEYWORDS, "Routine keyword"
    )

    @JvmField
    val PACKAGE_DECLARATOR = key(
        "RAKU_PACKAGE_DECLARATOR", DefaultLanguageHighlighterColors.KEYWORD,
        Group.KEYWORDS, "Package keyword"
    )

    @JvmField
    val TYPE_DECLARATOR = key(
        "RAKU_TYPE_DECLARATOR", DefaultLanguageHighlighterColors.CLASS_NAME,
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
        "RAKU_PHASER", DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE,
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
        "RAKU_SELF", DefaultLanguageHighlighterColors.MARKUP_ENTITY,
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
        "RAKU_CAPTURE_TERM", DefaultLanguageHighlighterColors.MARKUP_TAG,
        Group.NAMES_AND_TYPES, "Argument Capture (\\\$foo, \\(\$a, \$b))"
    )

    @JvmField
    val TERM_DECLARATION_BACKSLASH = key(
        "RAKU_TERM_DECLARATION_BACKSLASH", DefaultLanguageHighlighterColors.MARKUP_TAG,
        Group.NAMES_AND_TYPES, "Term Declaration Backslash (my \\answer = 42)"
    )

    /* Variables
     * *********
     * These inherit LOCAL_VARIABLE, which schemes reliably distinguish from
     * both types and callables.
     */

    @JvmField
    val VARIABLE = key(
        "RAKU_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE,
        Group.VARIABLES, "Variable"
    )

    @JvmField
    val SHAPE_DECLARATION = key(
        "RAKU_SHAPE_DECLARATION", DefaultLanguageHighlighterColors.INSTANCE_FIELD,
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
        "RAKU_NAMED_PARAMETER_SYNTAX", DefaultLanguageHighlighterColors.METADATA,
        Group.SIGNATURES, "Named parameter colon and parentheses"
    )

    @JvmField
    val NAMED_PARAMETER_NAME_ALIAS = key(
        "RAKU_NAMED_PARAMETER_NAME_ALIAS", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL,
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
        "RAKU_PAIR_KEY", DefaultLanguageHighlighterColors.METADATA,
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
        "RAKU_STUB_CODE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE,
        Group.LITERALS, "Stub Code (..., ???, !!!)"
    )

    /* Regex
     * *****
     * Regex operators inherit OPERATION_SIGN and regex brackets inherit
     * BRACKETS, mirroring the main language. Character classes inherit the
     * string-escape keys, since that is what they are: an escape that stands
     * for a set of characters.
     *
     * REGEX_SIG_SPACE falls back to FUNCTION_CALL on purpose -- sigspace in a
     * `rule` is an implicit `<.ws>` call. The run is blank, so the dotted
     * underline `SigSpaceAnnotator` draws is all there is to see; it takes its
     * color from that fallback via [StyleEffect] rather than from a
     * literal in `colorSchemes/`.
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
        "RAKU_REGEX_CAPTURE", DefaultLanguageHighlighterColors.METADATA,
        Group.REGEX, "Capture"
    )

    @JvmField
    val REGEX_BUILTIN_CCLASS = key(
        "RAKU_REGEX_BUILTIN_CCLASS", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE,
        Group.REGEX, "Built-in character class"
    )

    @JvmField
    val REGEX_BACKSLASH_BAD = key(
        "RAKU_REGEX_BACKSLASH_BAD", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE,
        Group.REGEX, "Invalid backslash sequence"
    )

    @JvmField
    val REGEX_SIG_SPACE = key(
        "RAKU_REGEX_SIG_SPACE", DefaultLanguageHighlighterColors.INLINE_PARAMETER_HINT_HIGHLIGHTED,
        Group.REGEX, "Rule Sigspace (implicit <.ws> call)",
        effect = StyleEffect.BOLD_DOTTED_LINE
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
     * POD_TEXT_BOLD/ITALIC are the clearest case for shipping an override:
     * `B<>` has to render bold to mean anything, and no platform key carries
     * that. Font style is also the only thing an override can add without
     * losing the fallback's color, because range-highlighter attributes merge
     * over the text beneath. See `colorSchemes/`. POD_TEXT_UNDERLINE needs an
     * effect color instead, so it goes through [StyleEffect].
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
        "RAKU_TEXT", DefaultLanguageHighlighterColors.DOC_COMMENT_MARKUP,
        Group.POD, "Text"
    )

    /**
     * The worked example for [styledAttributes]: this key supplies the
     * colour, the helper supplies the bold. `PodFormatterInspection` applies
     * it for `B<>`.
     *
     * It falls back to the same key as [POD_TEXT] so that, left alone, `B<>`
     * is the colour of the text around it and differs only in weight -- while
     * still giving anyone who wants bold Pod text in its own colour a live
     * control to do it with.
     */
    @JvmField
    val POD_TEXT_BOLD = key(
        "RAKU_TEXT_BOLD", DefaultLanguageHighlighterColors.DOC_COMMENT_MARKUP,
        Group.POD, "Text (Bold)", fontStyle = Font.BOLD
    )

    /** `I<>`, on the same shape as [POD_TEXT_BOLD]. */
    @JvmField
    val POD_TEXT_ITALIC = key(
        "RAKU_TEXT_ITALIC", DefaultLanguageHighlighterColors.DOC_COMMENT_MARKUP,
        Group.POD, "Text (Italic)", fontStyle = Font.ITALIC
    )

    @JvmField
    val POD_TEXT_UNDERLINE = key(
        // Unlike its B<> and I<> siblings this key is still applied, and has
        // to be: [Style] can be colourless and layer over whatever the lexer
        // painted, but an effect cannot -- it needs a concrete colour. This
        // key is where StyleEffect reads that colour from, which is also what
        // keeps the underline user-configurable and theme-following.
        //
        // So it must match [POD_TEXT], not the DOC_COMMENT that B<> and I<>
        // used to name: those two no longer use their keys at all, and the
        // text U<> underlines is painted POD_TEXT. Pointing this elsewhere
        // draws the underline in a different colour from the text above it.
        //
        // And not REASSIGNED_LOCAL_VARIABLE, tempting as the name is: the
        // platform paints reassigned variables *underlined*, so that key
        // ships an EFFECT_COLOR of its own, which StyleEffect reads as "the
        // user configured an effect, honour it" and stops deriving anything.
        // A fallback here has to carry a colour and no effect.
        "RAKU_TEXT_UNDERLINE", DefaultLanguageHighlighterColors.DOC_COMMENT_MARKUP,
        Group.POD, "Text (Underlined)", effect = StyleEffect.LINE_UNDERSCORE
    )

    @JvmField
    val POD_CODE = key(
        "RAKU_CODE", DefaultLanguageHighlighterColors.INTERFACE_NAME,
        Group.POD, "Code block"
    )

    @JvmField
    val POD_FORMAT_CODE = key(
        "RAKU_FORMAT_CODE", DefaultLanguageHighlighterColors.INTERFACE_NAME,
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
        "RAKU_BUILTIN_VARIABLE", DefaultLanguageHighlighterColors.CONSTANT,
        Group.SEMANTIC, "Built-in variable"
    )

    @JvmField
    val BUILTIN_CALL = key(
        "RAKU_BUILTIN_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL,
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
