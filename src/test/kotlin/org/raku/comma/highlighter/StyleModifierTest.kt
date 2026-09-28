package org.raku.comma.highlighter

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.TextAttributes
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.inspection.inspections.PodFormatterInspection
import java.awt.Color
import java.awt.Font

/**
 * [RakuHighlighter.Style] and [RakuHighlighter.styledAttributes] from a
 * caller's side: what you get when you actually layer a style over something.
 *
 * The contract worth defending is "the text keeps its colour and gains the
 * style". These drive it through the two ways the codebase reaches it -- a
 * key handed to a range highlighter (Pod `B<>`/`I<>`) and attributes computed
 * against a scheme -- rather than asserting the shape of the keys themselves,
 * which would pass just as happily if nothing ever applied them.
 */
class StyleModifierTest : CommaFixtureTestCase() {

    private fun scheme(): EditorColorsScheme = EditorColorsManager.getInstance().globalScheme

    /* Sanity: the modifiers are what they claim, before anything uses them. */

    fun testAModifierContributesAStyleAndNothingElse() {
        val italic = scheme().getAttributes(RakuHighlighter.Style.ITALIC)
        assertNotNull("must resolve with no colorSchemes/ entry, or it reaches no third-party theme",
                      italic)
        assertEquals(Font.ITALIC, italic!!.fontType)
        assertNull("a foreground here would overwrite whatever it layers over",
                   italic.foregroundColor)
        assertNull(italic.backgroundColor)
        assertNull("an effect needs a colour, which a static modifier cannot supply",
                   italic.effectColor)
    }

    fun testModifiersAreNotOfferedInTheColorPanel() {
        val offered = RakuHighlighter.panelEntries.map { it.key.externalName }
        assertFalse("a style modifier is not a colour a user configures",
                    offered.any { it.startsWith("RAKU_STYLE_") })
    }

    /* Usage 1: a modifier key applied as a range highlighter -- Pod B<> / I<>. */

    private fun podHighlighters(source: String): List<RangeHighlighter> {
        myFixture.enableInspections(PodFormatterInspection())
        myFixture.configureByText(RakuScriptFileType.INSTANCE, source)
        myFixture.doHighlighting()
        return myFixture.editor.markupModel.allHighlighters.toList()
    }

    /** The attributes applied over [word], if anything was applied over it. */
    private fun styleAppliedOver(source: String, word: String): TextAttributes? {
        val at = source.indexOf(word)
        assertTrue("fixture error: '$word' not in the source", at >= 0)
        return podHighlighters(source)
            .filter { it.startOffset <= at && it.endOffset >= at + word.length }
            .mapNotNull { it.getTextAttributes(scheme()) }
            .firstOrNull { it.fontType != Font.PLAIN }
    }

    // The shape all three share: the key gives the colour, the helper gives
    // the decoration. Left unconfigured the keys fall back alongside
    // POD_TEXT, so a formatted run reads as the surrounding Pod text plus its
    // decoration -- if that colour ever diverges, B<> starts looking like a
    // different kind of thing from the prose it sits in.
    fun testPodBoldIsBoldInThePodTextColour() {
        val applied = styleAppliedOver("=begin pod\nB<loud> and plain\n=end pod\n", "loud")
        assertNotNull("B<> should have something bold applied over it", applied)
        assertEquals(Font.BOLD, applied!!.fontType)
        assertEquals("B<> should read as the Pod text around it, just bold",
                     scheme().getAttributes(RakuHighlighter.POD_TEXT).foregroundColor,
                     applied.foregroundColor)
    }

    fun testPodItalicIsItalicInThePodTextColour() {
        val applied = styleAppliedOver("=begin pod\nI<soft> and plain\n=end pod\n", "soft")
        assertNotNull("I<> should have something italic applied over it", applied)
        assertEquals(Font.ITALIC, applied!!.fontType)
        assertEquals(scheme().getAttributes(RakuHighlighter.POD_TEXT).foregroundColor,
                     applied.foregroundColor)
    }

    // The keys are live controls again, not decoration: configuring one has
    // to actually change what B<> renders as, or the symmetry is a fiction.
    fun testConfiguringTheBoldKeyRecoloursOnlyBold() {
        val offered = RakuHighlighter.panelEntries.map { it.key }
        assertTrue("Text (Bold) should be offered in the colour panel",
                   offered.contains(RakuHighlighter.POD_TEXT_BOLD))
        assertTrue("Text (Underlined) should be offered too",
                   offered.contains(RakuHighlighter.POD_TEXT_UNDERLINE))

        val custom = scheme().clone() as EditorColorsScheme
        custom.setAttributes(
            RakuHighlighter.POD_TEXT_BOLD,
            TextAttributes(Color.MAGENTA, null, null, null, Font.PLAIN)
        )
        val bold = RakuHighlighter.styledAttributes(custom, RakuHighlighter.POD_TEXT_BOLD, Font.BOLD)
        assertEquals("a configured Text (Bold) colour must reach the rendered run",
                     Color.MAGENTA, bold.foregroundColor)
        assertEquals(Font.BOLD, bold.fontType)
    }

    /* The declaration is the single source: decorations live beside the key. */

    // The refactor's whole point. Every key that declares a decoration must
    // actually deliver it through decoratedAttributes -- if a declaration is
    // added and nothing routes it, or a caller hands the bare key to a range
    // highlighter instead, the decoration silently vanishes and only a human
    // looking at Pod would notice.
    fun testEveryDeclaredDecorationIsDelivered() {
        val decorated = RakuHighlighter.entries
            .filter { it.fontStyle != Font.PLAIN || it.effect != null }
        assertTrue("expected some keys to declare a decoration", decorated.isNotEmpty())

        for (entry in decorated) {
            val drawn = RakuHighlighter.decoratedAttributes(scheme(), entry.key)
            if (entry.effect != null) {
                assertNotNull("${entry.label} declares an effect but is drawn without one",
                              drawn.effectColor)
            } else {
                assertEquals("${entry.label} declares a font style but is drawn without it",
                             entry.fontStyle, drawn.fontType and entry.fontStyle)
            }
        }
    }

    // A key that declares nothing must come back unchanged, or decorating
    // becomes something you have to remember to opt out of.
    fun testAnUndecoratedKeyIsUnchanged() {
        val plain = RakuHighlighter.POD_TEXT
        val direct = scheme().getAttributes(plain)
        val viaHelper = RakuHighlighter.decoratedAttributes(scheme(), plain)
        assertEquals(direct.foregroundColor, viaHelper.foregroundColor)
        assertEquals(direct.fontType, viaHelper.fontType)
    }

    /* Usage 2: composing a modifier over a coloured base, as the editor does. */

    fun testLayeringKeepsTheColourUnderneathAndAddsTheStyle() {
        val custom = scheme().clone() as EditorColorsScheme
        custom.setAttributes(
            DefaultLanguageHighlighterColors.DOC_COMMENT,
            TextAttributes(Color.MAGENTA, null, null, null, Font.PLAIN)
        )

        val merged = TextAttributes.merge(
            custom.getAttributes(DefaultLanguageHighlighterColors.DOC_COMMENT),
            custom.getAttributes(RakuHighlighter.Style.ITALIC)
        )

        assertEquals("the colour below must survive", Color.MAGENTA, merged.foregroundColor)
        assertEquals("and the style above must be added", Font.ITALIC, merged.fontType)
    }

    fun testLayeringOrsStylesRatherThanReplacingThem() {
        val custom = scheme().clone() as EditorColorsScheme
        custom.setAttributes(
            DefaultLanguageHighlighterColors.DOC_COMMENT,
            TextAttributes(Color.MAGENTA, null, null, null, Font.ITALIC)
        )
        val merged = TextAttributes.merge(
            custom.getAttributes(DefaultLanguageHighlighterColors.DOC_COMMENT),
            custom.getAttributes(RakuHighlighter.Style.BOLD)
        )
        assertEquals("bold over italic is both", Font.BOLD or Font.ITALIC, merged.fontType)
    }

    /* Usage 3: the eager form, for callers that hand over attributes. */

    // The invariant that silently broke once already: B<> and I<> layer a
    // colourless Style over the lexer's Pod text, so they cannot drift from
    // it -- but U<> reads a real colour out of its own key, which can. If
    // POD_TEXT_UNDERLINE stops matching POD_TEXT, the underline is drawn in a
    // different colour from the text above it and nothing else notices.
    fun testPodUnderlineIsDrawnInThePodTextColour() {
        val custom = scheme().clone() as EditorColorsScheme
        custom.setAttributes(
            DefaultLanguageHighlighterColors.DOC_COMMENT_MARKUP,
            TextAttributes(Color.MAGENTA, null, null, null, Font.PLAIN)
        )
        // A different colour on the key the siblings used to name, so pointing
        // POD_TEXT_UNDERLINE back at it would be caught rather than coincide.
        custom.setAttributes(
            DefaultLanguageHighlighterColors.DOC_COMMENT,
            TextAttributes(Color.CYAN, null, null, null, Font.PLAIN)
        )

        val underline = RakuHighlighter.StyleEffect.LINE_UNDERSCORE
            .of(custom, RakuHighlighter.POD_TEXT_UNDERLINE)

        assertEquals(
            "U<> must underline in the colour of the Pod text it underlines",
            custom.getAttributes(RakuHighlighter.POD_TEXT).foregroundColor,
            underline.effectColor,
        )
    }

    /* Usage 4: an effect stacked on top of a styled run. */

    // The thing Style cannot do as a key, done as a call: a derived effect
    // layered over an already-merged base+style, keeping both. If this stops
    // working, effects and styles no longer compose and each has to be
    // applied alone.
    fun testAnEffectStacksOverAStyledRunAndKeepsBoth() {
        val custom = scheme().clone() as EditorColorsScheme
        custom.setAttributes(
            DefaultLanguageHighlighterColors.STRING,
            TextAttributes(Color.MAGENTA, null, null, null, Font.ITALIC)
        )

        val styled = TextAttributes.merge(
            custom.getAttributes(DefaultLanguageHighlighterColors.STRING),
            custom.getAttributes(RakuHighlighter.Style.BOLD)
        )
        val withEffect = RakuHighlighter.StyleEffect.BOLD_DOTTED_LINE.addedTo(styled, custom)

        assertEquals("the base colour survives both layers", Color.MAGENTA, withEffect.foregroundColor)
        assertEquals("bold and italic both survive", Font.BOLD or Font.ITALIC, withEffect.fontType)
        assertEquals("the effect is drawn in the text's own colour",
                     Color.MAGENTA, withEffect.effectColor)
        assertEquals(EffectType.BOLD_DOTTED_LINE, withEffect.effectType)
    }

    // of() and over() must agree, or reaching for the convenient one
    // silently diverges from what U<> and sigspace actually get.
    fun testOfAndOverAgreeForTheSameKey() {
        val custom = scheme().clone() as EditorColorsScheme
        val key = DefaultLanguageHighlighterColors.STRING
        custom.setAttributes(key, TextAttributes(Color.MAGENTA, null, null, null, Font.PLAIN))

        val viaKey = RakuHighlighter.StyleEffect.BOLD_DOTTED_LINE.of(custom, key)
        val viaAttributes =
            RakuHighlighter.StyleEffect.BOLD_DOTTED_LINE.over(custom.getAttributes(key), custom)

        assertEquals(viaKey.effectColor, viaAttributes.effectColor)
        assertEquals(viaKey.effectType, viaAttributes.effectType)
        assertEquals(viaKey.foregroundColor, viaAttributes.foregroundColor)
    }

    // Each value must route its own EffectType, or the enum is decoration.
    fun testEachValueRoutesItsOwnEffectType() {
        val custom = scheme().clone() as EditorColorsScheme
        val key = DefaultLanguageHighlighterColors.STRING
        custom.setAttributes(key, TextAttributes(Color.MAGENTA, null, null, null, Font.PLAIN))

        assertEquals(EffectType.BOLD_DOTTED_LINE,
                     RakuHighlighter.StyleEffect.BOLD_DOTTED_LINE.of(custom, key).effectType)
        assertEquals(EffectType.LINE_UNDERSCORE,
                     RakuHighlighter.StyleEffect.LINE_UNDERSCORE.of(custom, key).effectType)
    }

    fun testStyledAttributesIsThatKeyButStyled() {
        val custom = scheme().clone() as EditorColorsScheme
        custom.setAttributes(
            DefaultLanguageHighlighterColors.STRING,
            TextAttributes(Color.MAGENTA, Color.DARK_GRAY, null, null, Font.ITALIC)
        )

        val styled = RakuHighlighter.styledAttributes(
            custom, DefaultLanguageHighlighterColors.STRING, Font.BOLD
        )

        assertEquals(Color.MAGENTA, styled.foregroundColor)
        assertEquals("everything the key resolved to is kept", Color.DARK_GRAY, styled.backgroundColor)
        assertEquals(Font.BOLD or Font.ITALIC, styled.fontType)
    }
}
