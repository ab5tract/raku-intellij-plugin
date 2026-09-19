package org.raku.comma.rakuast

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

object RakuAstEditApplier {

    /**
     * Replaces exactly the edited node's span. Anything wider would delete
     * ordinary `#` comments, which have no RakuAST node and cannot be restored
     * by deparsing.
     *
     * `result.span` is given in NFG grapheme indices (Raku's `.origin`
     * units), not UTF-16 code units, so callers MUST convert it to a UTF-16
     * offset the same way [RakuAstViewerPanel] does in its `highlight()`
     * before calling this. Callers MUST ALSO verify the document is still
     * fresh with respect to the analysis -- e.g. the substring-mismatch guard
     * `highlight()` uses (compare the live document text at the converted
     * range against the snippet text the backend analyzed). This applier has
     * no snippet to compare against, so it cannot perform either the
     * grapheme-to-UTF-16 conversion or the freshness check on its own; it
     * trusts `result.span` as given, already converted. The (currently
     * unwired) edit UI follow-up owns doing both before calling [apply].
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
