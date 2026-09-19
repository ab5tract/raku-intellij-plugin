package org.raku.comma.rakuast

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
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
        val start = baseOffset + node.span.from
        val end = baseOffset + node.span.to
        if (start < 0 || end > editor.document.textLength || start > end) return
        editor.selectionModel.setSelection(start, end)
        editor.caretModel.moveToOffset(start)
    }
}
