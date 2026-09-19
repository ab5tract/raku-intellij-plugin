package org.raku.comma.rakuast

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable

/**
 * A node being dragged out of an analysis pane.
 *
 * Carries source **text**, not a node reference or a path. Each analysis is a
 * separate one-shot Raku process with no shared AST, and an [AstNode.path] is
 * only meaningful against the analysis that produced it — so the only thing
 * that can cross from one pane to another is Raku source. That turns out to be
 * exactly what the existing `edit` verb already consumes for a node-valued
 * attribute, which is why a drop needs no new backend verb.
 *
 * [conforms] travels too, rather than being looked up at the destination: the
 * target pane's own analysis may never have seen this class, and it cannot
 * report a conformance list for a class that is not in its tree.
 */
data class RakuAstDragPayload(
    val sourcePaneId: PaneId,
    val nodeClass: String,
    val conforms: List<String>?,
    val text: String,
) {
    /** Whether this node may be dropped into a slot declared to hold [declaredType]. */
    fun fitsSlot(declaredType: String): Boolean = RakuAstTypes.accepts(conforms, declaredType)

    companion object {
        val FLAVOR = DataFlavor(RakuAstDragPayload::class.java, "RakuAST node")
    }
}

/** Wraps a [RakuAstDragPayload] for Swing's transfer machinery. */
class RakuAstTransferable(private val payload: RakuAstDragPayload) : Transferable {

    override fun getTransferDataFlavors(): Array<DataFlavor> =
        arrayOf(RakuAstDragPayload.FLAVOR, DataFlavor.stringFlavor)

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean =
        flavor in transferDataFlavors

    // stringFlavor as well as the private one, so a node can also be dragged
    // straight into an editor or any other text target.
    override fun getTransferData(flavor: DataFlavor): Any = when (flavor) {
        RakuAstDragPayload.FLAVOR -> payload
        DataFlavor.stringFlavor -> payload.text
        else -> throw java.awt.datatransfer.UnsupportedFlavorException(flavor)
    }
}
