package org.raku.comma.rakuast

/**
 * The viewer's display preferences, shared by every analysis pane.
 *
 * Deliberately a plain holder with no [com.intellij.openapi.project.Project] and
 * no persistence: the checkboxes that drive these live once in
 * [RakuAstViewerPanel], and that is also where they are read from and written
 * back to [com.intellij.ide.util.PropertiesComponent].
 *
 * Keeping the panes out of that is what makes two of them safe. Each preference
 * is stored under a single project-scoped key, so a checkbox per pane would mean
 * two widgets over one key with no cross-update -- toggling in one pane would
 * leave the other displaying a value that is no longer stored, and the
 * discrepancy would only surface as a "lost" setting after a restart.
 *
 * It also makes pane tests order-independent, since they construct their own
 * instance rather than inheriting whatever a previous test persisted.
 */
class RakuAstViewerOptions(
    var expandAll: Boolean = true,
    var showGist: Boolean = false,
)
