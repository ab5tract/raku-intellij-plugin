package org.raku.comma.highlighter

import com.intellij.openapi.editor.colors.EditorColorsManager
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
}
