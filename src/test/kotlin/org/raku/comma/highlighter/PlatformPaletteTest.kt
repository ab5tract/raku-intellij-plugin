package org.raku.comma.highlighter

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color
import java.awt.Font
import org.raku.comma.CommaFixtureTestCase

class PlatformPaletteTest : CommaFixtureTestCase() {

    fun testEnumeratesEveryPlatformKey() {
        val keys = PlatformPalette.platformKeys()
        // 58 TextAttributesKey constants on IU-262; a platform bump may add
        // more, so assert a floor rather than pinning the exact count.
        assertTrue("expected the full DefaultLanguageHighlighterColors set, got ${keys.size}",
                   keys.size >= 50)
        assertTrue(keys.any { it.first == "KEYWORD" })
        assertTrue(keys.any { it.first == "OPERATION_SIGN" })
    }

    // The bug this tool shipped with: crediting a Raku key to every link of
    // its fallback chain. The platform chains its own keys, so one Raku key
    // reported three as taken and the keys near the root looked claimed by
    // everything -- which is precisely the question the tool exists to answer.
    //
    // RAKU_REASSIGNED_PARAMETER names REASSIGNED_PARAMETER, and the platform
    // falls that back to PARAMETER. Only the first is a choice anyone made.
    fun testOnlyTheChosenPlatformKeyIsClaimed() {
        val swatches = PlatformPalette.swatches(EditorColorsManager.getInstance().globalScheme)
        val label = RakuHighlighter.entries
            .single { it.key == RakuHighlighter.REASSIGNED_PARAMETER }.label

        val chosen = swatches.single { it.name == "REASSIGNED_PARAMETER" }
        val chainedBehind = swatches.single { it.name == "PARAMETER" }

        assertTrue("the key Raku actually named should be claimed",
                   chosen.claimedBy.contains(label))
        assertFalse("PARAMETER is only reached through REASSIGNED_PARAMETER, so it is not spoken for",
                    chainedBehind.claimedBy.contains(label))
        assertTrue("but it should still be reported as reached, not hidden",
                   chainedBehind.inheritedBy.contains(label))
    }

    // A Raku key is allowed to fall back to another Raku key; the walk has to
    // skip past those to find the platform key that was chosen. Without the
    // skip, such a key would be credited to nothing at all.
    fun testIntermediateRakuKeysAreSkippedNotCounted() {
        val swatches = PlatformPalette.swatches(EditorColorsManager.getInstance().globalScheme)
        val everyRakuLabel = RakuHighlighter.entries.map { it.label }.toSet()
        val attributed = swatches.flatMap { it.claimedBy }.toSet()
        val unattributed = everyRakuLabel - attributed
        assertEquals("every Raku key should reach some platform key", emptySet<String>(), unattributed)
    }

    // The point of the tool: knowing which platform keys Raku already inherits
    // from. If nothing is attributed, the fallback walk is broken and the tool
    // would silently report every key as free.
    fun testAttributesRakuKeysToThePlatformKeysTheyInherit() {
        val swatches = PlatformPalette.swatches(EditorColorsManager.getInstance().globalScheme)
        val claimed = swatches.filter { it.isClaimed }
        assertTrue("no platform key reported as claimed; the fallback walk is broken",
                   claimed.isNotEmpty())
        assertTrue("Raku declares keyword-ish constructs, so KEYWORD should be claimed",
                   swatches.single { it.name == "KEYWORD" }.isClaimed)
    }

    fun testEverySwatchResolvesAgainstTheScheme() {
        val swatches = PlatformPalette.swatches(EditorColorsManager.getInstance().globalScheme)
        assertEquals(PlatformPalette.platformKeys().size, swatches.size)
        assertTrue("a scheme should resolve attributes for at least the common keys",
                   swatches.count { it.attributes != null } > 10)
    }

    // What the palette dialog's Refresh button rests on: swatches must read the
    // scheme they are handed, every call, rather than anything computed once.
    // If this regresses, Refresh and the scheme-change listener both go quiet
    // while still appearing to work.
    fun testSwatchesReadTheSchemeTheyAreGivenEachTime() {
        val base = EditorColorsManager.getInstance().globalScheme
        val recoloured = base.clone() as EditorColorsScheme
        recoloured.setAttributes(
            DefaultLanguageHighlighterColors.KEYWORD,
            TextAttributes(Color.MAGENTA, null, null, null, Font.PLAIN)
        )

        val keyword = PlatformPalette.swatches(recoloured).single { it.name == "KEYWORD" }
        assertEquals("KEYWORD should resolve from the scheme passed in",
                     Color.MAGENTA, keyword.attributes?.foregroundColor)

        // And the original is unaffected, so the first call cached nothing.
        val unchanged = PlatformPalette.swatches(base).single { it.name == "KEYWORD" }
        assertFalse("the global scheme should not have been mutated",
                    unchanged.attributes?.foregroundColor == Color.MAGENTA)
    }
}
