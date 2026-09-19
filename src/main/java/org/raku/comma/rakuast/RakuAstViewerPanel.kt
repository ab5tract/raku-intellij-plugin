package org.raku.comma.rakuast

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.text.BreakIterator
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class RakuAstViewerPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val status = JBLabel("")
    private val treeRoot = DefaultMutableTreeNode("(nothing analyzed)")
    private val treeModel = DefaultTreeModel(treeRoot)
    private val tree = Tree(treeModel)
    private val attrModel = DefaultTableModel(arrayOf("Attribute", "Value"), 0)
    private val attrTable = JTable(attrModel)

    private var currentEditor: Editor? = null
    private var baseOffset: Int = 0
    private var snippet: String = ""
    // graphemeUtf16Offsets[g] is the UTF-16 offset into `snippet` of the start
    // of the g-th grapheme cluster; the array has one extra trailing entry
    // equal to snippet.length so a span's exclusive `to` (which can equal the
    // grapheme count) still resolves. Built once per showAnalysis() call --
    // see graphemeUtf16Offset().
    private var graphemeUtf16Offsets: IntArray = IntArray(0)
    private var nodes = 0

    init {
        // Attribute values are frequently whole deparsed expressions. A default
        // JTable cell paints one clipped line, so wrap instead and let the row
        // grow to fit. AUTO_RESIZE_LAST_COLUMN gives the value column the slack.
        attrTable.setDefaultRenderer(Any::class.java, WrappingCellRenderer())
        attrTable.autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
        attrTable.columnModel.getColumn(0).preferredWidth = ATTRIBUTE_COLUMN_WIDTH
        // Wrapping depends on column width, so re-measure whenever that changes.
        attrTable.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(event: ComponentEvent) = updateRowHeights()
        })

        val splitter = JBSplitter(true, 0.6f)
        splitter.firstComponent = JBScrollPane(tree)
        splitter.secondComponent = JBScrollPane(attrTable)
        add(status, BorderLayout.NORTH)
        add(splitter, BorderLayout.CENTER)

        tree.addTreeSelectionListener {
            val selected = tree.lastSelectedPathComponent as? DefaultMutableTreeNode
            val node = selected?.userObject as? AstNode ?: return@addTreeSelectionListener
            showAttributes(node)
            highlight(node)
        }
    }

    fun showAnalysis(editor: Editor, baseOffset: Int, snippet: String, result: AnalyzeResult) {
        // The editor belongs to the caller, not this panel. It may already have been
        // released (e.g. the file tab closed while the ~310ms analyze() call was in
        // flight) by the time this reaches us. Never hold on to a dead editor.
        this.currentEditor = editor.takeUnless { it.isDisposed }
        this.baseOffset = baseOffset
        this.snippet = snippet
        this.graphemeUtf16Offsets = buildGraphemeUtf16Offsets(snippet)
        this.nodes = 0

        treeRoot.removeAllChildren()
        attrModel.rowCount = 0

        val error = result.error
        val tree = result.tree
        if (error != null || tree == null) {
            status.text = error ?: "The Raku backend returned no tree."
            treeRoot.userObject = "(no tree)"
        } else {
            status.text = ""
            treeRoot.userObject = tree
            addChildren(treeRoot, tree)
            nodes = countNodes(tree)
        }
        treeModel.reload()
    }

    fun statusText(): String = status.text

    fun nodeCount(): Int = nodes

    private fun countNodes(node: AstNode): Int =
        1 + node.children.sumOf { countNodes(it) }

    private fun addChildren(parent: DefaultMutableTreeNode, node: AstNode) {
        for (child in node.children) {
            val swingChild = DefaultMutableTreeNode(child)
            parent.add(swingChild)
            addChildren(swingChild, child)
        }
    }

    private fun showAttributes(node: AstNode) {
        attrModel.rowCount = 0
        for (attr in node.attrs) attrModel.addRow(arrayOf(attr.name, attr.display))
        updateRowHeights()
    }

    /**
     * Grows each row to whatever height its wrapped content needs. Must run
     * after the model changes and after any column resize, since wrapping --
     * and therefore height -- depends on the column's current width.
     */
    private fun updateRowHeights() {
        for (row in 0 until attrTable.rowCount) {
            var height = attrTable.rowHeight
            for (column in 0 until attrTable.columnCount) {
                val renderer = attrTable.getCellRenderer(row, column)
                val rendered = attrTable.prepareRenderer(renderer, row, column)
                height = maxOf(height, rendered.preferredSize.height)
            }
            if (attrTable.getRowHeight(row) != height) attrTable.setRowHeight(row, height)
        }
    }

    /**
     * Renders a cell as wrapped text rather than one clipped line.
     *
     * The [setSize] call before returning is what makes this work: a
     * [JTextArea] only reports a meaningful wrapped height once it has been
     * given the width it must wrap within, and [updateRowHeights] reads
     * `preferredSize.height` straight afterwards.
     */
    private class WrappingCellRenderer : JTextArea(), TableCellRenderer {
        init {
            lineWrap = true
            wrapStyleWord = true
            isOpaque = true
            border = JBUI.Borders.empty(2, 4)
        }

        override fun getTableCellRendererComponent(
            table: JTable,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int,
        ): Component {
            font = table.font
            text = value?.toString().orEmpty()
            background = if (isSelected) table.selectionBackground else table.background
            foreground = if (isSelected) table.selectionForeground else table.foreground
            setSize(table.columnModel.getColumn(column).width, Short.MAX_VALUE.toInt())
            return this
        }
    }

    private fun highlight(node: AstNode) {
        val editor = currentEditor ?: return
        if (editor.isDisposed) {
            // The tab closed between showAnalysis() and this selection. Drop the
            // stale reference so later clicks short-circuit on the null check above.
            currentEditor = null
            return
        }
        // Undefined .origin on the backend (e.g. a synthetic
        // RakuAST::Type::Setting node) comes through as a null span, distinct
        // from a real zero-length span at offset 0. Nothing to highlight.
        val span = node.span ?: return

        // Raku's origin offsets (span.from/span.to) are NFG grapheme indices
        // into the analyzed snippet; IntelliJ document/string offsets are
        // UTF-16 code units. The two diverge on astral characters (one
        // grapheme, a UTF-16 surrogate pair) and combining marks. Convert
        // grapheme indices to UTF-16 offsets into `snippet` via the map built
        // once in showAnalysis() before treating them as document offsets.
        // graphemeUtf16Offset() also rejects an out-of-range index, which
        // doubles as the old "span.from/to within snippet bounds" check.
        val fromUtf16 = graphemeUtf16Offset(span.from)
        val toUtf16 = graphemeUtf16Offset(span.to)
        if (fromUtf16 == null || toUtf16 == null || fromUtf16 > toUtf16) {
            status.text = "Selection has changed since analysis — re-run Analyze"
            return
        }

        val document = editor.document
        val start = baseOffset + fromUtf16
        val end = baseOffset + toUtf16
        if (start < 0 || end > document.textLength || start > end) return

        // The grapheme conversion above only accounts for encoding -- it says
        // nothing about whether the document has actually changed since
        // analyze() ran (e.g. a line typed above the selection shifts
        // baseOffset out from under the computed range). Verify the live text
        // at the converted range still matches the snippet text the backend
        // analyzed before acting on it.
        val expected = snippet.substring(fromUtf16, toUtf16)
        if (document.getText(TextRange(start, end)) != expected) {
            status.text = "Selection has changed since analysis — re-run Analyze"
            return
        }

        status.text = ""
        editor.selectionModel.setSelection(start, end)
        editor.caretModel.moveToOffset(start)
    }

    // Builds the grapheme-index -> UTF-16-offset map for `text`, using
    // java.text.BreakIterator's character-instance (grapheme cluster)
    // boundaries. BreakIterator's grapheme clusters and Raku's NFG graphemes
    // agree for the cases this feature cares about (astral characters,
    // common combining marks), but the two are not guaranteed identical in
    // every exotic case (e.g. some multi-codepoint NFG synthetics) -- this is
    // a practical approximation, not a proven equivalence.
    //
    // The result has one entry per grapheme boundary, including a trailing
    // entry equal to text.length, so index g is valid for
    // 0..numberOfGraphemes inclusive (an exclusive span `to` can equal the
    // grapheme count). For pure-ASCII text this is the identity map.
    private fun buildGraphemeUtf16Offsets(text: String): IntArray {
        val boundary = BreakIterator.getCharacterInstance()
        boundary.setText(text)
        val offsets = mutableListOf(boundary.first())
        var end = boundary.next()
        while (end != BreakIterator.DONE) {
            offsets.add(end)
            end = boundary.next()
        }
        return offsets.toIntArray()
    }

    // Converts a grapheme index (as used by span.from/span.to) into a
    // UTF-16 offset into `snippet`, or null if the index is out of range for
    // the map built in showAnalysis() -- callers must refuse rather than
    // highlight in that case, the same way an invalid span was already
    // handled.
    private fun graphemeUtf16Offset(index: Int): Int? =
        graphemeUtf16Offsets.getOrNull(index)

    companion object {
        // Attribute names are short; the value column gets the remaining width
        // so wrapped expressions have room.
        private const val ATTRIBUTE_COLUMN_WIDTH = 160
    }
}
