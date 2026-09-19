package org.raku.comma.rakuast

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
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
import com.intellij.ui.JBColor
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Component
import java.awt.EventQueue
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.InputEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.text.BreakIterator
import javax.swing.BorderFactory
import javax.swing.DropMode
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.tree.TreePath
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.JTree
import javax.swing.TransferHandler
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

/**
 * One AST analysis: its tree, its attribute table, and the editor it was
 * analyzed from.
 *
 * Everything here belongs to a single analysis, which is what makes the pane
 * the right boundary rather than some shared model. In particular [snippet],
 * [analysisContext] and the tree must travel together: an [AstNode.path] is
 * only meaningful against the analysis that produced it, so any operation that
 * names a path -- editing, rendering a gist -- has to ship the snippet and
 * context alongside it.
 *
 * Display preferences are not per-analysis and live in [options], owned by the
 * containing [RakuAstViewerPanel].
 */
class RakuAstPane(
    private val project: Project,
    private val options: RakuAstViewerOptions,
    val paneId: PaneId,
    // Called when the user focuses anything in this pane. A lambda rather than
    // a reference back to the container, so the dependency stays one-way.
    private val onActivated: (RakuAstPane) -> Unit = {},
) : JPanel(BorderLayout()) {

    // Names the analysis this pane is showing, and carries the active-pane
    // accent. Without it "active" would be invisible state -- the analyze
    // action fires from the editor, where the tool window is not focused, so
    // the user has no other way to know which pane their next analysis lands in.
    private val header = JBLabel(NOTHING_ANALYZED).apply {
        font = UIUtil.getLabelFont(UIUtil.FontSize.SMALL)
        foreground = UIUtil.getInactiveTextColor()
    }

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
    // Only value cells of attributes the backend marked editable. The (gist)
    // and (context) rows are never edited: a gist is a rendering of the node,
    // not a field of it, and the context rows are the file's imports rather
    // than anything belonging to the selection.
    private val attrModel = object : DefaultTableModel(arrayOf("Attribute", "Value"), 0) {
        override fun isCellEditable(row: Int, column: Int): Boolean =
            column == 1 && editableAttrs[row] != null
    }

    // Row index -> the attribute that row edits, or null for a row that is
    // not an attribute at all. Kept beside the model because DefaultTableModel
    // stores only display strings and cannot answer "which attribute is this".
    private val editableAttrs = mutableMapOf<Int, AstAttr>()
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

    // Gists are immutable for a given analysis, so a node visited twice is
    // free the second time.
    private val gistCache = mutableMapOf<List<Int>, String>()

    // Identifies the in-flight request. A slow gist for a node you have since
    // clicked away from must not overwrite the one you are now looking at.
    //
    // Strictly per-pane, and not to be tidied into shared state: one pane's
    // analysis bumping another's token would cancel that pane's in-flight
    // gist, and the failure is silent -- the stale-request check returns early,
    // so the row never leaves "Rendering…".
    private var gistRequest = 0

    // The context those paths were built against -- recovered from the root,
    // since the gist verb has to be given the same one or it walks elsewhere.
    private var analysisContext: List<String> = emptyList()

    // What each class in this analysis conforms to, so a drag leaving this
    // pane can carry its own list rather than hoping the destination happens
    // to have seen the same class.
    private var conformance: Map<String, List<String>> = emptyMap()

    // The analysis root, kept so a drop can resolve a path back to the node it
    // names. The Swing tree holds the same objects, but walking a path is
    // cheaper and clearer against the AstNode tree itself.
    private var rootNode: AstNode? = null

    // The node whose attributes the table currently shows, so a committed
    // cell knows which node it belongs to.
    private var editedNode: AstNode? = null

    // Guards the table listener while the pane rewrites rows itself --
    // repopulating after an analysis, filling in a gist, restoring a rejected
    // value. Without it those writes would read as user edits and be sent
    // back to Raku.
    private var suppressEditEvents = false

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

        attrModel.addTableModelListener { event ->
            if (event.type != javax.swing.event.TableModelEvent.UPDATE) return@addTableModelListener
            if (event.column != 1) return@addTableModelListener
            val row = event.firstRow
            val attr = editableAttrs[row] ?: return@addTableModelListener
            val node = editedNode ?: return@addTableModelListener
            val entered = attrModel.getValueAt(row, 1) as? String ?: return@addTableModelListener
            // Rows are also rewritten programmatically -- the gist landing,
            // a rejected edit being put back. Only a genuine change from the
            // value the backend reported is an edit.
            if (entered == attr.display || suppressEditEvents) return@addTableModelListener
            applyEdit(node, attr.name, attr.kind, entered) {
                // Put the old text back, so the table never shows a value the
                // document does not actually contain.
                if (row < attrModel.rowCount) {
                    withoutEditEvents { attrModel.setValueAt(attr.display, row, 1) }
                }
            }
        }

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

        // Keyed per pane, not shared: an unkeyed splitter resets to 0.6 every
        // session, and one key across both panes would make whichever divider
        // moved last silently reposition the other on the next open.
        val splitter = JBSplitter(true, "$TREE_SPLIT_KEY.$paneId", 0.6f)
        splitter.firstComponent = JBScrollPane(tree)
        splitter.secondComponent = JBScrollPane(attrTable)
        add(JPanel(BorderLayout()).apply {
            add(header, BorderLayout.NORTH)
            add(status, BorderLayout.CENTER)
        }, BorderLayout.NORTH)
        add(splitter, BorderLayout.CENTER)
        setActiveAppearance(false)

        // Focusing anything in the pane makes it the target for the next
        // analysis. Both widgets, since either can be what the user clicks.
        val activate = object : FocusAdapter() {
            override fun focusGained(event: FocusEvent) = onActivated(this@RakuAstPane)
        }
        tree.addFocusListener(activate)
        attrTable.addFocusListener(activate)

        // The status area wraps, so its height depends on the width it is
        // wrapping within, and it only recomputes that on revalidate. Before
        // the panes were split there was nothing that changed a pane's width,
        // so the table's own resize hook was enough; now dragging the divider
        // between panes re-wraps a long compile error to a different number of
        // lines and would leave it clipped at the old height.
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(event: ComponentEvent) {
                status.revalidate()
            }
        })

        // One handler on both widgets: it resolves the drop through whichever
        // DropLocation it is handed, so the tree and the attribute table need
        // no separate implementations. DropMode.ON because every target here
        // is an existing slot to overwrite; inserting into a list needs an
        // index the edit verb cannot yet express.
        val transfer = NodeTransferHandler()
        tree.dragEnabled = true
        tree.dropMode = DropMode.ON
        tree.transferHandler = transfer
        attrTable.dropMode = DropMode.ON
        attrTable.transferHandler = transfer

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

    /**
     * Marks this pane as the one the next analysis will land in.
     *
     * Both states reserve the same two pixels so switching panes does not
     * shift the layout.
     */
    fun setActiveAppearance(active: Boolean) {
        header.border = BorderFactory.createCompoundBorder(
            if (active) JBUI.Borders.customLine(ACCENT, 0, 0, 2, 0) else JBUI.Borders.emptyBottom(2),
            JBUI.Borders.empty(2, 6),
        )
        header.repaint()
    }

    /**
     * Re-renders the current selection so a [RakuAstViewerOptions.showGist]
     * toggle takes effect now rather than on the next node click.
     */
    fun onShowGistChanged() {
        selectedNode()?.let { showAttributes(it) }
    }

    /**
     * Applies a [RakuAstViewerOptions.expandAll] toggle immediately, so the
     * checkbox visibly does something when clicked rather than only on the
     * next analysis.
     */
    fun onExpandAllChanged() {
        if (options.expandAll) withBulkToggle { expandSubtree(TreePath(treeRoot)) }
    }

    /**
     * Resolves "replace this node" into the slot that actually gets written.
     *
     * Dropping onto a tree node means putting something in its place, and a
     * node's place is a slot on its *parent* — which is what via-attr names.
     * Returns null when there is nowhere to write: the root hangs off nothing,
     * and an element held in a list needs an index that the edit verb cannot
     * express.
     */
    private fun replacementSlotFor(target: AstNode): DropSlot? {
        val attrName = target.viaAttr ?: return null
        if (target.viaIndex != null) return null
        val parent = nodeAt(target.path.dropLast(1)) ?: return null
        val declaredType = parent.attrs.firstOrNull { it.name == attrName }?.type ?: return null
        return DropSlot(parent, attrName, declaredType)
    }

    /**
     * Drag out of the tree, and drop onto either widget.
     *
     * Copy only. A drop writes the dragged source into the target slot and
     * leaves the source alone — removing a node from a slot that requires one
     * would leave the AST with a hole and nothing valid to put in it.
     *
     * The same handler serves as source and target, and the same instance is
     * installed on both widgets, because the drop resolves through
     * [slotUnder] either way.
     */
    private inner class NodeTransferHandler : TransferHandler() {

        override fun getSourceActions(c: JComponent): Int = COPY

        override fun createTransferable(c: JComponent): Transferable? {
            val node = selectedNode() ?: return null
            val text = sourceTextOf(node) ?: return null
            return RakuAstTransferable(
                RakuAstDragPayload(paneId, node.nodeClass, conformance[node.nodeClass], text))
        }

        override fun canImport(support: TransferSupport): Boolean {
            val payload = payloadOf(support) ?: return false
            val slot = slotUnder(support) ?: return false
            return payload.fitsSlot(slot.declaredType)
        }

        override fun importData(support: TransferSupport): Boolean {
            val payload = payloadOf(support) ?: return false
            val slot = slotUnder(support) ?: return false
            if (!payload.fitsSlot(slot.declaredType)) return false
            // 'node' rather than 'scalar': the text is Raku source for the
            // backend to parse into a node, which is what the edit verb
            // already does for a node-valued attribute.
            applyEdit(slot.node, slot.attrName, "node", payload.text) {}
            return true
        }

        private fun payloadOf(support: TransferSupport): RakuAstDragPayload? {
            if (!support.isDataFlavorSupported(RakuAstDragPayload.FLAVOR)) return null
            return runCatching {
                support.transferable.getTransferData(RakuAstDragPayload.FLAVOR)
            }.getOrNull() as? RakuAstDragPayload
        }

        /** Resolves wherever the cursor is into a writable slot, or null. */
        private fun slotUnder(support: TransferSupport): DropSlot? =
            when (val location = support.dropLocation) {
                is JTree.DropLocation -> treeSlot(location)
                is JTable.DropLocation -> tableSlot(location)
                else -> null
            }

        private fun treeSlot(location: JTree.DropLocation): DropSlot? {
            val path = location.path ?: return null
            val target = (path.lastPathComponent as? DefaultMutableTreeNode)?.userObject as? AstNode
                ?: return null
            return replacementSlotFor(target)
        }

        /** Dropping onto an attribute row names its slot outright. */
        private fun tableSlot(location: JTable.DropLocation): DropSlot? {
            val attr = editableAttrs[location.row] ?: return null
            val node = editedNode ?: return null
            return DropSlot(node, attr.name, attr.type)
        }
    }

    /** Walks [path] from the analysis root. */
    private fun nodeAt(path: List<Int>): AstNode? {
        var current = rootNode ?: return null
        for (index in path) {
            current = current.children.getOrNull(index) ?: return null
        }
        return current
    }

    /**
     * The Raku source for [node], lifted from the analyzed snippet.
     *
     * Taken from the text rather than deparsed so a dragged node keeps its
     * formatting and comments; [AstNode.sourceSpan] already accounts for the
     * nodes whose own span would not yield standalone source.
     */
    private fun sourceTextOf(node: AstNode): String? {
        val span = node.sourceSpan() ?: return null
        val from = graphemeUtf16Offset(span.from) ?: return null
        val to = graphemeUtf16Offset(span.to) ?: return null
        if (from > to || to > snippet.length) return null
        return snippet.substring(from, to).takeIf { it.isNotBlank() }
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
        // The editor belongs to the caller, not this pane. It may already have been
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
        this.conformance = result.conformance
        this.rootNode = result.tree
        gistCache.clear()
        gistRequest++
        header.text = FileDocumentManager.getInstance().getFile(editor.document)?.name
            ?: NOTHING_ANALYZED

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
        if (options.expandAll) withBulkToggle { expandSubtree(TreePath(treeRoot)) }
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

    private fun showAttributes(node: AstNode) = withoutEditEvents {
        attrModel.rowCount = 0
        editableAttrs.clear()
        editedNode = node
        // The imports compiled in front of the selection. They have no nodes in
        // the tree -- they are not part of what the user selected -- so this is
        // the only place they are visible, and without it the tree silently
        // depends on text nobody can see.
        for (statement in node.context) attrModel.addRow(arrayOf(CONTEXT_ROW_LABEL, statement))
        // Rakudo's summary is the tree label (see AstNode.toString), not a row
        // here -- the identity it carries is what distinguishes sibling nodes,
        // which is a tree problem rather than a detail-pane one.
        for (attr in node.attrs) {
            if (attr.editable) editableAttrs[attrModel.rowCount] = attr
            attrModel.addRow(arrayOf(attr.name, attr.display))
        }

        if (options.showGist) {
            val cached = gistCache[node.path]
            attrModel.addRow(arrayOf(GIST_ROW_LABEL, cached ?: GIST_LOADING))
            if (cached == null) requestGist(node)
        }

        updateRowHeights()
    }

    /**
     * Applies one attribute edit, then re-analyses.
     *
     * Re-analysing rather than trusting the `tree` the edit returns: those
     * nodes carry their PRE-edit origins, so after an edit that changes text
     * length every span at or after the edit point is wrong. Reusing it would
     * mean the next click highlights the wrong range. One extra round trip
     * buys a tree that describes the text now on screen -- which also
     * refreshes the gist, since its cache belongs to the analysis.
     */
    private fun applyEdit(
        node: AstNode,
        attrName: String,
        attrKind: String,
        newValue: String,
        // What to undo in the UI if the edit is refused. A table edit has to
        // put the old cell text back; a drop changed nothing on screen, so it
        // has nothing to restore.
        restore: () -> Unit,
    ) {
        val editor = currentEditor ?: return
        val forSnippet = snippet
        val forContext = analysisContext
        val forBase = baseOffset

        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Applying RakuAST edit", true) {
                override fun run(indicator: ProgressIndicator) {
                    val result = RakuAstService.getInstance(project)
                        .edit(forSnippet, node.path, attrName, newValue, attrKind, forContext)
                    ApplicationManager.getApplication().invokeLater {
                        finishEdit(editor, forSnippet, forBase, node, result, restore)
                    }
                }
            })
    }

    private fun finishEdit(
        editor: Editor,
        forSnippet: String,
        forBase: Int,
        node: AstNode,
        result: EditResult,
        restore: () -> Unit,
    ) {
        fun reject(message: String) {
            setStatus(message)
            restore()
            updateRowHeights()
        }

        if (result.error != null) return reject(result.error)
        val text = result.text ?: return reject("The edit produced no replacement text.")
        val span = result.span ?: return reject("The edited node has no source span to replace.")

        // Trap one: the backend counts in NFG graphemes, the document in
        // UTF-16 code units. They diverge on astral characters and combining
        // marks, so an unconverted span silently replaces the wrong range.
        val fromUtf16 = graphemeUtf16Offset(span.from) ?: return reject(UNMAPPABLE)
        val toUtf16 = graphemeUtf16Offset(span.to) ?: return reject(UNMAPPABLE)

        // Trap two: the document may have moved since the analysis this edit
        // was computed against. Replacing on stale coordinates would corrupt
        // unrelated text, so verify before writing rather than after.
        val start = forBase + fromUtf16
        val end = forBase + toUtf16
        if (editor.isDisposed) return reject("The editor was closed before the edit could apply.")
        if (end > editor.document.textLength ||
            editor.document.getText(TextRange(start, end)) != forSnippet.substring(fromUtf16, toUtf16)
        ) {
            return reject("Selection has changed since analysis — re-run Analyze")
        }

        if (!RakuAstEditApplier.replace(project, editor, start, end, text)) {
            return reject("The edit could not be written to the document.")
        }

        // Recompute the selection locally rather than re-reading it: the edit
        // just changed its length, and the editor's own selection still
        // describes the text as it was.
        val newSnippet = forSnippet.replaceRange(fromUtf16, toUtf16, text)
        editor.selectionModel.setSelection(forBase, forBase + newSnippet.length)
        setStatus("")
        reanalyzeAfterEdit(editor, forBase, newSnippet, node.path)
    }

    private fun reanalyzeAfterEdit(editor: Editor, base: Int, newSnippet: String, path: List<Int>) {
        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Re-analyzing RakuAST", true) {
                override fun run(indicator: ProgressIndicator) {
                    val service = RakuAstService.getInstance(project)
                    val context = if (service.supportsFileContext()) analysisContext else emptyList()
                    val result = service.analyze(newSnippet, context)
                    ApplicationManager.getApplication().invokeLater {
                        showAnalysis(editor, base, newSnippet, result)
                        // Put the user back where they were editing. The path
                        // survives a scalar edit, since the tree's shape does
                        // not change -- only the value at one node.
                        selectPath(path)
                    }
                }
            })
    }

    private fun selectPath(path: List<Int>) {
        var current: DefaultMutableTreeNode = treeRoot
        for (index in path) {
            if (index >= current.childCount) return
            current = current.getChildAt(index) as? DefaultMutableTreeNode ?: return
        }
        val treePath = TreePath(current.path)
        tree.selectionPath = treePath
        tree.scrollPathToVisible(treePath)
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
                withoutEditEvents { attrModel.setValueAt(text, row, 1) }
                updateRowHeights()
                return
            }
        }
    }

    /** Rewrites rows without the table listener mistaking them for edits. */
    private fun withoutEditEvents(action: () -> Unit) {
        suppressEditEvents = true
        try {
            action()
        } finally {
            suppressEditEvents = false
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
     *
     * One instance per pane rather than a shared one. Rubber-stamp renderers
     * mutate `this` and return it, so sharing would make [updateRowHeights]'s
     * immediately-following `preferredSize.height` read depend on whatever
     * else rendered in between.
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

        private const val GIST_ROW_LABEL = "(gist)"
        private const val GIST_LOADING = "Rendering…"

        private const val COPY_CONFIRMATION_MS = 2000L

        // Application-scoped, unlike the project-scoped display preferences --
        // a divider position is about this screen, not about this project.
        private const val TREE_SPLIT_KEY = "org.raku.comma.rakuast.treeAttrSplit"

        private const val NOTHING_ANALYZED = "(nothing analyzed)"

        // The IDE's own focus accent, so the active pane reads the same way as
        // every other focused component rather than inventing a colour.
        private val ACCENT = JBColor.namedColor("Component.focusColor", JBColor.BLUE)

        private const val UNMAPPABLE =
            "That node's span could not be mapped into the document — re-run Analyze"
    }
}
