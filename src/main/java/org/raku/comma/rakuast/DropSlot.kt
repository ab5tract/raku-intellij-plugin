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
    // Where in a list-valued attribute the write lands, or null when the whole
    // attribute is being set. A list position cannot be expressed as an
    // attribute write — "the statements of this block" says nothing about
    // which statement — so these two go to different backend verbs.
    val listIndex: Int? = null,
    // How many existing items the write replaces. 0 inserts between them.
    val replaceCount: Int = 0,
)
