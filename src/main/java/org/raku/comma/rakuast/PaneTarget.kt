package org.raku.comma.rakuast

/**
 * Which pane an analysis should land in.
 *
 * [ACTIVE] is the default because the common loop is to analyze once for
 * reference and then iterate in the other pane: sending each new analysis to
 * the pane you are working in keeps the reference where you put it, with no
 * extra gesture. [OTHER] is how you seed the second pane without first clicking
 * into it. [LEFT] and [RIGHT] are for callers that must name a side outright --
 * tests, and a drop that has to target the pane it was dropped on.
 */
enum class PaneTarget { ACTIVE, OTHER, LEFT, RIGHT }
