package org.raku.comma.rakuast

/**
 * A place a dragged node can be written: an attribute of [node], and the type
 * that attribute is declared to hold.
 *
 * Every drop resolves to one of these, whichever widget it landed on —
 * dropping onto an attribute row names its slot outright, while dropping onto
 * a tree node means replacing it, which is a write to the slot on its parent
 * that currently holds it.
 */
data class DropSlot(
    val node: AstNode,
    val attrName: String,
    val declaredType: String,
)
