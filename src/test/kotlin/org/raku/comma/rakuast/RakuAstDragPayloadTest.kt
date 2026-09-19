package org.raku.comma.rakuast

import java.awt.datatransfer.DataFlavor
import junit.framework.TestCase

/**
 * The drop-legality rule, exercised without any UI. This is the check that
 * runs on every drag-hover, so it has to answer from data already in hand.
 */
class RakuAstDragPayloadTest : TestCase() {

    private val strLiteral = RakuAstDragPayload(
        sourcePaneId = PaneId.LEFT,
        nodeClass = "RakuAST::StrLiteral",
        conforms = listOf(
            "RakuAST::StrLiteral", "RakuAST::Literal", "RakuAST::Term",
            "RakuAST::Termish", "RakuAST::Expression", "RakuAST::Node",
        ),
        text = "\"cool\"",
    )

    fun testFitsASlotItConformsTo() {
        assertTrue(strLiteral.fitsSlot("RakuAST::Expression"))
        assertTrue(strLiteral.fitsSlot("RakuAST::StrLiteral"))
    }

    fun testRefusesASlotItDoesNotConformTo() {
        assertFalse(strLiteral.fitsSlot("RakuAST::Name"))
        assertFalse(strLiteral.fitsSlot("RakuAST::Statement"))
    }

    // Native slots (int, str, Bool) are not RakuAST types at all, so a node
    // must never land in one.
    fun testRefusesNativeSlots() {
        assertFalse(strLiteral.fitsSlot("str"))
        assertFalse(strLiteral.fitsSlot("int"))
    }

    // Mu is what bookkeeping slots declare; List says nothing about elements.
    // Neither constrains, so both defer to the compile check.
    fun testUnconstrainedSlotsAreAllowed() {
        assertTrue(strLiteral.fitsSlot("Mu"))
        assertTrue(strLiteral.fitsSlot("List"))
    }

    // A drag that crossed panes may name a class the destination never saw.
    // Refusing on absent metadata would disable dropping without explaining
    // why, so the compile check is left to decide.
    fun testMissingConformanceIsPermitted() {
        val unknown = strLiteral.copy(conforms = null)
        assertTrue(unknown.fitsSlot("RakuAST::Name"))
    }

    // The payload also offers plain text, so a node can be dragged into an
    // editor or any other text target.
    fun testTransferableOffersBothFlavors() {
        val transferable = RakuAstTransferable(strLiteral)

        assertTrue(transferable.isDataFlavorSupported(RakuAstDragPayload.FLAVOR))
        assertTrue(transferable.isDataFlavorSupported(DataFlavor.stringFlavor))
        assertEquals(strLiteral, transferable.getTransferData(RakuAstDragPayload.FLAVOR))
        assertEquals("\"cool\"", transferable.getTransferData(DataFlavor.stringFlavor))
    }
}
