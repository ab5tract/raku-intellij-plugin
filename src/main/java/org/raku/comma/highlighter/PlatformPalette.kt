package org.raku.comma.highlighter

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes

/**
 * The platform's `DefaultLanguageHighlighterColors` keys, resolved against a
 * scheme, annotated with which Raku keys already fall back to each.
 *
 * `docs/color-principles.md` says retheming Raku means choosing a *fallback*
 * rather than setting a colour -- so the question when adding a key is "what
 * do the platform keys actually look like here, and which are already spoken
 * for?". That is not answerable from the color panel, which shows one key at a
 * time and never says who inherits it.
 *
 * Data only, no UI, so it can be tested without a dialog.
 */
object PlatformPalette {

    data class Swatch(
        val name: String,
        val key: TextAttributesKey,
        val attributes: TextAttributes?,
        /** Raku keys whose fallback chain reaches this one, by panel label. */
        val claimedBy: List<String>,
    ) {
        val isClaimed: Boolean get() = claimedBy.isNotEmpty()
    }

    /** Every `TextAttributesKey` constant on DefaultLanguageHighlighterColors. */
    fun platformKeys(): List<Pair<String, TextAttributesKey>> =
        DefaultLanguageHighlighterColors::class.java.fields
            .filter { TextAttributesKey::class.java.isAssignableFrom(it.type) }
            .mapNotNull { field ->
                (field.get(null) as? TextAttributesKey)?.let { field.name to it }
            }
            .sortedBy { it.first }

    /**
     * Walks [key]'s fallback chain. A Raku key usually points straight at a
     * platform key, but nothing stops it pointing at another Raku key that
     * does -- so follow the chain rather than reading one link.
     */
    private fun fallbackChain(key: TextAttributesKey): Sequence<TextAttributesKey> =
        generateSequence(key.fallbackAttributeKey) { it.fallbackAttributeKey }

    fun swatches(scheme: EditorColorsScheme): List<Swatch> {
        val claims = mutableMapOf<TextAttributesKey, MutableList<String>>()
        for (entry in RakuHighlighter.entries) {
            for (target in fallbackChain(entry.key)) {
                claims.getOrPut(target) { mutableListOf() }.add(entry.label)
            }
        }
        return platformKeys().map { (name, key) ->
            Swatch(name, key, scheme.getAttributes(key), claims[key].orEmpty().sorted())
        }
    }
}
