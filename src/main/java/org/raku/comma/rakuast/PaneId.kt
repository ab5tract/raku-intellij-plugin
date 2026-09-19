package org.raku.comma.rakuast

/**
 * Which of the viewer's two analysis panes this is.
 *
 * More than a label: a pane's identity is what distinguishes its persisted
 * divider position from its neighbour's, and it is how a drag knows whether it
 * started in the pane it is being dropped into.
 */
enum class PaneId { LEFT, RIGHT }
