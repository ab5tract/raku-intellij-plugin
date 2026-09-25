package org.raku.comma.rakuast

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

object RakuAstEditApplier {

    /**
     * Replaces [start]..[end] with [text]. Nothing wider: anything beyond the
     * edited node's own span would delete ordinary `#` comments, which have no
     * RakuAST node and cannot be restored by deparsing.
     *
     * Takes absolute document offsets in UTF-16 code units, deliberately.
     * An earlier signature took the backend's `EditResult` plus a base offset
     * and documented that callers must first convert `span` out of Raku's NFG
     * grapheme indices and must separately verify the document still matches
     * the analysed snippet. Both are easy to forget and neither is visible at
     * the call site when forgotten -- and the failure is silent replacement of
     * the wrong range, which is the one outcome this whole design exists to
     * prevent. Requiring finished offsets means the conversion and the
     * freshness check have to have happened before the call can even be
     * written. [RakuAstViewerPanel] owns both.
     */
    fun replace(project: Project, editor: Editor, start: Int, end: Int, text: String): Boolean {
        val document = editor.document
        if (start < 0 || end > document.textLength || start > end) return false

        // A RangeMarker keeps the target valid if the document shifts under us.
        val marker = document.createRangeMarker(start, end)
        var changed = false
        try {
            WriteCommandAction.runWriteCommandAction(project, "Apply RakuAST Edit", null, {
                if (marker.isValid) {
                    document.replaceString(marker.startOffset, marker.endOffset, text)
                    changed = true
                }
            })
        } finally {
            marker.dispose()
        }
        // Return false if the marker was invalidated before the write action ran,
        // so callers can reliably trust that true means the document changed.
        return changed
    }
}
