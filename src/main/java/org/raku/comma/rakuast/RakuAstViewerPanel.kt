package org.raku.comma.rakuast

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel
import javax.swing.JTable
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
    private var nodes = 0

    init {
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

        val document = editor.document
        val start = baseOffset + span.from
        val end = baseOffset + span.to
        if (start < 0 || end > document.textLength || start > end) return

        // Raku's origin offsets are NFG grapheme indices into the analyzed
        // snippet; IntelliJ document offsets are UTF-16 code units. The two
        // can diverge even immediately after analyze() (combining marks,
        // astral characters), and further still if the document changed
        // since analyze (e.g. a line typed above the selection shifts
        // baseOffset out from under the computed range). Rather than track
        // those two causes separately, verify the text actually at the
        // computed range still matches the snippet text the backend
        // analyzed before acting on it -- one guard for both.
        if (span.from < 0 || span.to > snippet.length || span.from > span.to) {
            status.text = "Selection has changed since analysis — re-run Analyze"
            return
        }
        val expected = snippet.substring(span.from, span.to)
        if (document.getText(TextRange(start, end)) != expected) {
            status.text = "Selection has changed since analysis — re-run Analyze"
            return
        }

        status.text = ""
        editor.selectionModel.setSelection(start, end)
        editor.caretModel.moveToOffset(start)
    }
}
