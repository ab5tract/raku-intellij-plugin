package org.raku.comma.highlighter

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ColorUtil
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.GridLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Every `DefaultLanguageHighlighterColors` key as a swatch, in the live scheme,
 * marked with which Raku constructs already inherit it.
 *
 * Exists because `docs/color-principles.md` makes choosing a *fallback* the
 * unit of work, and neither the color panel nor Language Defaults answers the
 * two questions that decision needs: what do all the platform keys look like
 * side by side in THIS theme, and which are already spoken for.
 *
 * A development aid. Delete it freely.
 */
class ShowPlatformPaletteAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        PaletteDialog().show()
    }

    private class PaletteDialog : DialogWrapper(true) {
        init {
            title = "Raku: Platform Color Palette"
            init()
        }

        override fun createActions() = arrayOf(okAction)

        override fun createCenterPanel(): JComponent {
            val scheme = EditorColorsManager.getInstance().globalScheme
            val swatches = PlatformPalette.swatches(scheme)
            val claimed = swatches.count { it.isClaimed }

            val rows = JPanel(GridLayout(0, 1, 0, JBUI.scale(2)))
            rows.background = scheme.defaultBackground
            for (s in swatches) rows.add(row(s, scheme.defaultBackground, scheme.defaultForeground))

            val header = JBLabel(
                "  ${swatches.size} platform keys · $claimed already inherited by Raku · " +
                "scheme: ${scheme.name}"
            )
            header.border = JBUI.Borders.empty(6)

            val panel = JPanel(BorderLayout())
            panel.add(header, BorderLayout.NORTH)
            panel.add(JBScrollPane(rows).apply {
                preferredSize = Dimension(JBUI.scale(920), JBUI.scale(620))
            }, BorderLayout.CENTER)
            return panel
        }

        private fun row(s: PlatformPalette.Swatch, bg: Color, fg: Color): JComponent {
            val attrs: TextAttributes? = s.attributes
            val foreground = attrs?.foregroundColor ?: fg

            // The swatch: foreground over the key's own background when it sets
            // one, so a key that only paints a background is still visible.
            val chip = JPanel()
            chip.preferredSize = Dimension(JBUI.scale(46), JBUI.scale(20))
            chip.background = attrs?.backgroundColor ?: bg
            chip.isOpaque = true
            chip.border = JBUI.Borders.customLine(foreground, JBUI.scale(3))

            // The name rendered IN the attribute, so font style shows too.
            val sample = JBLabel(s.name)
            sample.foreground = foreground
            attrs?.backgroundColor?.let { sample.background = it; sample.isOpaque = true }
            sample.font = sample.font.deriveFont(attrs?.fontType ?: Font.PLAIN)
            sample.preferredSize = Dimension(JBUI.scale(330), JBUI.scale(20))

            val detail = buildString {
                append(attrs?.foregroundColor?.let { "#" + ColorUtil.toHex(it) } ?: "(no fg)")
                attrs?.backgroundColor?.let { append("  bg #" + ColorUtil.toHex(it)) }
                val effect = attrs?.effectType
                if (effect != null && attrs.effectColor != null && effect != EffectType.BOXED) {
                    append("  ").append(effect.name.lowercase())
                }
                if (s.isClaimed) append("   ← ").append(s.claimedBy.joinToString(", "))
            }
            val info = JBLabel(detail)
            info.foreground = if (s.isClaimed) foreground else fg

            val left = JPanel(BorderLayout(JBUI.scale(8), 0))
            left.isOpaque = false
            left.add(chip, BorderLayout.WEST)
            left.add(sample, BorderLayout.CENTER)

            val row = JPanel(BorderLayout(JBUI.scale(10), 0))
            row.background = bg
            row.border = JBUI.Borders.empty(1, 8)
            row.add(left, BorderLayout.WEST)
            row.add(info, BorderLayout.CENTER)
            return row
        }
    }
}
