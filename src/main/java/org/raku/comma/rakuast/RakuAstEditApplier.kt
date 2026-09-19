package org.raku.comma.rakuast

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

object RakuAstEditApplier {

    /**
     * Replaces exactly the edited node's span. Anything wider would delete
     * ordinary `#` comments, which have no RakuAST node and cannot be restored
     * by deparsing.
     */
    fun apply(project: Project, editor: Editor, baseOffset: Int, result: EditResult): Boolean {
        val text = result.text ?: return false
        val span = result.span ?: return false
        if (result.error != null) return false

        val document = editor.document
        val start = baseOffset + span.from
        val end = baseOffset + span.to
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
