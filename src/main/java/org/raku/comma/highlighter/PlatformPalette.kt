package org.raku.comma.highlighter

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.CodeInsightColors
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
        /**
         * Raku keys that chose this one -- the first platform key on their
         * fallback chain, by panel label.
         */
        val claimedBy: List<String>,
        /**
         * Raku keys that only reach this one because the *platform* chains it
         * behind the key they actually chose. Reported separately because
         * this key is still free for a Raku key to point at directly.
         */
        val inheritedBy: List<String>,
    ) {
        val isClaimed: Boolean get() = claimedBy.isNotEmpty()
    }

    /**
     * The classes a Raku key is allowed to fall back into.
     *
     * `DefaultLanguageHighlighterColors` is the bulk of it, but three Raku
     * keys reach past it -- BAD_CHARACTER into [HighlighterColors], UNUSED and
     * ALT_WARNING into [CodeInsightColors] -- and a palette that omits those
     * cannot answer "what is this key inheriting" for them at all.
     */
    private val KEY_HOLDERS = listOf(
        DefaultLanguageHighlighterColors::class.java,
        HighlighterColors::class.java,
        CodeInsightColors::class.java,
    )

    /** Every `TextAttributesKey` constant a Raku key can fall back to. */
    fun platformKeys(): List<Pair<String, TextAttributesKey>> =
        KEY_HOLDERS
            .flatMap { it.fields.asList() }
            .filter { TextAttributesKey::class.java.isAssignableFrom(it.type) }
            .mapNotNull { field ->
                (field.get(null) as? TextAttributesKey)?.let { field.name to it }
            }
            // The same key can be exposed from more than one holder; keep one
            // row per key rather than one per constant that names it.
            .distinctBy { it.second }
            .sortedBy { it.first }

    /**
     * Walks [key]'s fallback chain. A Raku key usually points straight at a
     * platform key, but nothing stops it pointing at another Raku key that
     * does -- so follow the chain rather than reading one link.
     */
    private fun fallbackChain(key: TextAttributesKey): Sequence<TextAttributesKey> =
        generateSequence(key.fallbackAttributeKey) { it.fallbackAttributeKey }

    /**
     * Attribution stops at the *first* platform key on each chain.
     *
     * Crediting every link was the obvious reading of "which platform keys
     * are spoken for" and the wrong one. The platform chains its own keys --
     * `REASSIGNED_PARAMETER` falls back to `PARAMETER`, which falls back to
     * `IDENTIFIER` -- so one Raku key crediting its whole chain reported
     * three keys as taken, and the keys nearest the root came out claimed by
     * almost everything. The question the tool exists to answer, "what is
     * still free to point at", became unanswerable.
     *
     * The first platform key on the chain is the one a developer actually
     * chose in `RakuHighlighter.kt`. The walk still has to follow the chain
     * to find it, because a Raku key may point at another Raku key that
     * points at the platform -- that is what the skipping is for. Everything
     * past that first hit is the platform's own arrangement, reported as
     * [Swatch.inheritedBy] so it is visible without being counted.
     */
    fun swatches(scheme: EditorColorsScheme): List<Swatch> {
        val platform = platformKeys()
        val isPlatformKey = platform.mapTo(HashSet()) { it.second }

        val chosen = mutableMapOf<TextAttributesKey, MutableList<String>>()
        val reached = mutableMapOf<TextAttributesKey, MutableList<String>>()

        for (entry in RakuHighlighter.entries) {
            var pastTheChoice = false
            for (target in fallbackChain(entry.key)) {
                if (target !in isPlatformKey) continue
                val into = if (pastTheChoice) reached else chosen
                into.getOrPut(target) { mutableListOf() }.add(entry.label)
                pastTheChoice = true
            }
        }

        return platform.map { (name, key) ->
            Swatch(
                name, key, scheme.getAttributes(key),
                chosen[key].orEmpty().sorted(),
                reached[key].orEmpty().sorted(),
            )
        }
    }
}
