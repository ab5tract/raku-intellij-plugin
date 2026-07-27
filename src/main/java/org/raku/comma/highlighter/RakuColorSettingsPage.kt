package org.raku.comma.highlighter

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.extensions.InternalIgnoreDependencyViolation
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import org.raku.comma.RakuIcons
import javax.swing.Icon

/**
 * Settings | Editor | Color Scheme | Raku.
 *
 * The tree is composed from [RakuHighlighter.panelEntries] rather than from a
 * list of its own, so a key cannot exist without a control or wear a label
 * belonging to a different key -- both of which had happened to the previous
 * hand-maintained array.
 */
@InternalIgnoreDependencyViolation
class RakuColorSettingsPage : ColorSettingsPage {
    override fun getDisplayName(): String = "Raku"

    override fun getIcon(): Icon = RakuIcons.CAMELIA

    override fun getHighlighter(): SyntaxHighlighter = RakuSyntaxHighlighter()

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDemoText(): String = DEMO_TEXT

    /**
     * Keys no lexer token carries, so the demo has to mark them up by hand.
     * Every tag here must appear in the demo text and vice versa --
     * `RakuColorSettingsPageTest` checks both directions, since an unmatched
     * tag renders as literal `<builtinCall>` junk in the preview.
     */
    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> = mapOf(
        "builtinVariable" to RakuHighlighter.BUILTIN_VARIABLE,
        "builtinCall" to RakuHighlighter.BUILTIN_CALL,
        "reassignedVariable" to RakuHighlighter.REASSIGNED_LOCAL_VARIABLE,
        "reassignedParameter" to RakuHighlighter.REASSIGNED_PARAMETER,
    )

    companion object {
        private const val DEMO_TEXT_RESOURCE = "/colorSettings/RakuDemoText.raku"

        private val DESCRIPTORS: Array<AttributesDescriptor> =
            RakuHighlighter.panelEntries
                .map { AttributesDescriptor(it.group.path(it.label), it.key) }
                .toTypedArray()

        /**
         * Kept in a real `.raku` resource rather than inline: it is editable
         * with Raku highlighting on, and it avoids the source-level escaping
         * that previously appended a stray backslash to the rendered preview.
         */
        private val DEMO_TEXT: String =
            RakuColorSettingsPage::class.java.getResourceAsStream(DEMO_TEXT_RESOURCE)
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: error("Missing colour settings demo text resource: $DEMO_TEXT_RESOURCE")
    }
}
