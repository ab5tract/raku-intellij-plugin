package org.raku.comma.rakuast

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.TextRange
import com.intellij.ui.awt.RelativePoint
import com.intellij.ide.util.PropertiesComponent
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Component
import java.awt.EventQueue
import java.awt.datatransfer.StringSelection
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.InputEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.text.BreakIterator
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.tree.TreePath
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class RakuAstViewerPanel(private val project: Project) : JPanel(BorderLayout()) {

    // A JLabel would clip these to one line, and the messages that matter most
    // here are Rakudo compile errors: long, already multi-line, and useless
    // truncated. Editable is off but selection is left on so the text can be
    // copied out; the label look comes from dropping the border and inheriting
    // the panel's background.
    private val status = JTextArea().apply {
        lineWrap = true
        wrapStyleWord = true
        isEditable = false
        isOpaque = false
        border = JBUI.Borders.empty(4, 6)
        font = UIUtil.getLabelFont()
        foreground = UIUtil.getLabelForeground()
    }
    private val treeRoot = DefaultMutableTreeNode("(nothing analyzed)")
    private val treeModel = DefaultTreeModel(treeRoot)
    private val tree = Tree(treeModel)
    private val attrModel = DefaultTableModel(arrayOf("Attribute", "Value"), 0)
    private val attrTable = object : JTable(attrModel) {
        // Only the gist label advertises itself as clickable; the other rows
        // hold values short enough to read in place.
        override fun getToolTipText(event: MouseEvent): String? {
            val row = rowAtPoint(event.point)
            val column = columnAtPoint(event.point)
            return if (column == 0 && row >= 0 && getValueAt(row, 0) == GIST_ROW_LABEL)
                "Click to copy this gist to the clipboard"
            else
                null
        }
    }

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
    private var bulkToggle = false

    // On by default: an AST tree is deep and narrow, and a root node alone
    // tells you nothing about the code you just selected. Remembered per
    // project, so someone who prefers to drill down is not re-deciding it
    // every session.
    private val expandAll = JBCheckBox("Expand all after analyzing").apply {
        isSelected = PropertiesComponent.getInstance(project).getBoolean(EXPAND_ALL_KEY, true)
        border = JBUI.Borders.empty(2, 6)
    }

    // Off by default: each selection costs a round trip to Raku, which is
    // worth paying only for someone who actually reads gists.
    private val showGist = JBCheckBox("Show node gist").apply {
        isSelected = PropertiesComponent.getInstance(project).getBoolean(SHOW_GIST_KEY, false)
        border = JBUI.Borders.empty(2, 6)
    }

    // Gists are immutable for a given analysis, so a node visited twice is
    // free the second time.
    private val gistCache = mutableMapOf<List<Int>, String>()

    // Identifies the in-flight request. A slow gist for a node you have since
    // clicked away from must not overwrite the one you are now looking at.
    private var gistRequest = 0

    // The context those paths were built against -- recovered from the root,
    // since the gist verb has to be given the same one or it walks elsewhere.
    private var analysisContext: List<String> = emptyList()

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

        // A gist is the one value here that is routinely too long to read in
        // a cell and worth taking elsewhere, so clicking its label copies it.
        attrTable.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(event: MouseEvent) {
                val row = attrTable.rowAtPoint(event.point)
                val column = attrTable.columnAtPoint(event.point)
                if (column != 0 || row < 0) return
                if (attrTable.getValueAt(row, 0) != GIST_ROW_LABEL) return
                if (copyGist(row)) confirmCopy(RelativePoint(event))
            }
        })

        val splitter = JBSplitter(true, 0.6f)
        splitter.firstComponent = JBScrollPane(tree)
        splitter.secondComponent = JBScrollPane(attrTable)
        add(status, BorderLayout.NORTH)
        add(splitter, BorderLayout.CENTER)
        add(JPanel(BorderLayout()).apply {
            add(expandAll, BorderLayout.WEST)
            add(showGist, BorderLayout.CENTER)
        }, BorderLayout.SOUTH)

        showGist.addActionListener {
            PropertiesComponent.getInstance(project)
                .setValue(SHOW_GIST_KEY, showGist.isSelected, false)
            // Redraw the current selection so the row appears or disappears
            // now, rather than on the next node click.
            selectedNode()?.let { showAttributes(it) }
        }

        expandAll.addActionListener {
            PropertiesComponent.getInstance(project)
                .setValue(EXPAND_ALL_KEY, expandAll.isSelected, true)
            // Apply on toggle rather than only on the next analysis, so the
            // checkbox visibly does something when you click it.
            if (expandAll.isSelected) withBulkToggle { expandSubtree(TreePath(treeRoot)) }
        }

        tree.addTreeSelectionListener {
            val selected = tree.lastSelectedPathComponent as? DefaultMutableTreeNode
            val node = selected?.userObject as? AstNode ?: return@addTreeSelectionListener
            showAttributes(node)
            highlight(node)
        }

        // Shift+toggle applies to the whole subtree. An AST tree is deep and
        // narrow -- opening one statement a level at a time is tedious when
        // what you want is "show me all of this".
        //
        // Driven from the expansion event rather than by intercepting the
        // mouse: the tree UI installs its own mouse listener, so consuming the
        // click from ours is order-dependent and fragile. Reading the
        // modifiers off the event currently being dispatched is not, and it
        // picks up keyboard toggles for free.
        tree.addTreeExpansionListener(object : TreeExpansionListener {
            override fun treeExpanded(event: TreeExpansionEvent) {
                if (shiftHeld()) withBulkToggle { expandSubtree(event.path) }
            }

            override fun treeCollapsed(event: TreeExpansionEvent) {
                if (shiftHeld()) withBulkToggle { collapseSubtree(event.path) }
            }
        })
    }

    private fun shiftHeld(): Boolean =
        (EventQueue.getCurrentEvent() as? InputEvent)?.isShiftDown == true

    /**
     * Runs a bulk expand/collapse without re-entering the expansion listener.
     * Each expandPath/collapsePath fires its own event, and without this guard
     * the listener would recurse into work it is already doing.
     */
    private fun withBulkToggle(action: () -> Unit) {
        if (bulkToggle) return
        bulkToggle = true
        try {
            action()
        } finally {
            bulkToggle = false
        }
    }

    private fun expandSubtree(path: TreePath) {
        tree.expandPath(path)
        val node = path.lastPathComponent as? DefaultMutableTreeNode ?: return
        for (i in 0 until node.childCount) {
            expandSubtree(path.pathByAddingChild(node.getChildAt(i)))
        }
    }

    /**
     * Collapses depth-first: a JTree only remembers the expanded state of a
     * visible path, so collapsing the parent first would leave the children
     * expanded again the next time it is opened.
     */
    private fun collapseSubtree(path: TreePath) {
        val node = path.lastPathComponent as? DefaultMutableTreeNode ?: return
        for (i in 0 until node.childCount) {
            collapseSubtree(path.pathByAddingChild(node.getChildAt(i)))
        }
        tree.collapsePath(path)
    }

    fun showAnalysis(
        editor: Editor,
        baseOffset: Int,
        snippet: String,
        result: AnalyzeResult,
        contextAvailable: Boolean = true,
    ) {
        // The editor belongs to the caller, not this panel. It may already have been
        // released (e.g. the file tab closed while the ~310ms analyze() call was in
        // flight) by the time this reaches us. Never hold on to a dead editor.
        this.currentEditor = editor.takeUnless { it.isDisposed }
        this.baseOffset = baseOffset
        this.snippet = snippet
        this.graphemeUtf16Offsets = buildGraphemeUtf16Offsets(snippet)
        this.nodes = 0
        // Paths and gists both belong to one analysis. A stale gist would be
        // for a node that no longer exists at that path.
        this.analysisContext = result.tree?.context ?: emptyList()
        gistCache.clear()
        gistRequest++

        treeRoot.removeAllChildren()
        attrModel.rowCount = 0

        val error = result.error
        val tree = result.tree
        if (error != null || tree == null) {
            setStatus(error ?: "The Raku backend returned no tree.")
            treeRoot.userObject = "(no tree)"
        } else {
            // Say so when the selection was compiled without its file's
            // imports, since that changes what the tree can show: a type from
            // an unimported module cannot resolve, and the user should know
            // the difference between "this is what your code means" and "this
            // is what your code means in isolation".
            setStatus(
                if (contextAvailable) ""
                else "Analyzed without file context: the project root has no META6.json or lib/, " +
                     "so imports cannot be resolved.")
            treeRoot.userObject = tree
            addChildren(treeRoot, tree)
            nodes = countNodes(tree)
        }
        treeModel.reload()
        // reload() collapses everything, so this has to come after it.
        if (expandAll.isSelected) withBulkToggle { expandSubtree(TreePath(treeRoot)) }
    }

    fun statusText(): String = status.text

    /**
     * Sets the status text and re-lays out. The revalidate is required, not
     * defensive: a wrapping [JTextArea] derives its preferred height from the
     * width it is currently wrapping within, so without it a newly-set long
     * message keeps the height computed for the previous one and is clipped.
     */
    private fun setStatus(text: String) {
        status.text = text
        status.isVisible = text.isNotEmpty()
        status.revalidate()
        status.repaint()
    }

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
        // The imports compiled in front of the selection. They have no nodes in
        // the tree -- they are not part of what the user selected -- so this is
        // the only place they are visible, and without it the tree silently
        // depends on text nobody can see.
        for (statement in node.context) attrModel.addRow(arrayOf(CONTEXT_ROW_LABEL, statement))
        // Rakudo's summary is the tree label (see AstNode.toString), not a row
        // here -- the identity it carries is what distinguishes sibling nodes,
        // which is a tree problem rather than a detail-pane one.
        for (attr in node.attrs) attrModel.addRow(arrayOf(attr.name, attr.display))

        if (showGist.isSelected) {
            val cached = gistCache[node.path]
            attrModel.addRow(arrayOf(GIST_ROW_LABEL, cached ?: GIST_LOADING))
            if (cached == null) requestGist(node)
        }

        updateRowHeights()
    }

    private fun selectedNode(): AstNode? =
        (tree.lastSelectedPathComponent as? DefaultMutableTreeNode)?.userObject as? AstNode

    /**
     * Fetches one node's gist off the EDT and fills its row in when it lands.
     *
     * A gist is not shipped with the tree because it nests its whole subtree,
     * so every level would re-serialise the levels beneath it. That makes it
     * a per-selection round trip, hence the request token: clicking through
     * the tree faster than Raku answers would otherwise let an earlier node's
     * gist arrive last and overwrite the one now on screen.
     */
    private fun requestGist(node: AstNode) {
        val request = ++gistRequest
        val forSnippet = snippet
        val forContext = analysisContext

        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Rendering RakuAST gist", true) {
                override fun run(indicator: ProgressIndicator) {
                    val result = RakuAstService.getInstance(project)
                        .gist(forSnippet, node.path, forContext)
                    val text = result.gist ?: result.error ?: "No gist was returned."
                    ApplicationManager.getApplication().invokeLater {
                        if (request != gistRequest) return@invokeLater
                        if (result.gist != null) gistCache[node.path] = text
                        replaceGistRow(text)
                    }
                }
            })
    }

    /**
     * Copies the gist at [row] to the clipboard.
     *
     * @return false when there is nothing worth copying — the placeholder
     *   shown while Raku is still rendering, or an error message in the
     *   gist's place. Putting either on the clipboard would be worse than
     *   doing nothing, since the user would not discover it until pasting.
     */
    private fun copyGist(row: Int): Boolean {
        val text = attrModel.getValueAt(row, 1) as? String ?: return false
        if (text.isBlank() || text == GIST_LOADING) return false
        if (gistCache.values.none { it == text }) return false
        CopyPasteManager.getInstance().setContents(StringSelection(text))
        return true
    }

    private fun confirmCopy(at: RelativePoint) {
        JBPopupFactory.getInstance()
            .createHtmlTextBalloonBuilder("Gist copied to clipboard", MessageType.INFO, null)
            .setFadeoutTime(COPY_CONFIRMATION_MS)
            .createBalloon()
            .show(at, Balloon.Position.above)
    }

    private fun replaceGistRow(text: String) {
        for (row in 0 until attrModel.rowCount) {
            if (attrModel.getValueAt(row, 0) == GIST_ROW_LABEL) {
                attrModel.setValueAt(text, row, 1)
                updateRowHeights()
                return
            }
        }
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
            setStatus("Selection has changed since analysis — re-run Analyze")
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
            setStatus("Selection has changed since analysis — re-run Analyze")
            return
        }

        setStatus("")
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

        // Parenthesised to read as metadata rather than as an attribute of the
        // node, which is what every other row in this table is.
        private const val CONTEXT_ROW_LABEL = "(context)"

        private const val EXPAND_ALL_KEY = "org.raku.comma.rakuast.expandAll"
        private const val SHOW_GIST_KEY = "org.raku.comma.rakuast.showGist"

        private const val GIST_ROW_LABEL = "(gist)"
        private const val GIST_LOADING = "Rendering…"

        private const val COPY_CONFIRMATION_MS = 2000L
    }
}
