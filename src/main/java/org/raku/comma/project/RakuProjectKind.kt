package org.raku.comma.project

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import org.raku.comma.filetypes.RakuModuleFileType
import org.raku.comma.filetypes.RakuPodFileType
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.utils.CommaProjectUtil

/**
 * Whether a project has earned the plugin's attention.
 *
 * Two levels, because the features differ in cost. Fetching the ecosystem is
 * network plus zef plus a background task and only means anything where there
 * are dependencies to resolve; the status bar widget and the SDK prompt are
 * useful the moment someone edits Raku.
 */
object RakuProjectKind {

    /**
     * Raku Test is deliberately absent. It claims bare `.t`, which Perl 5 uses
     * too, and a lone test file should not wake the plugin. Nothing reachable
     * is lost: a real Raku project carries a script, module or pod file as
     * well. Shebang-typed files (`#!/usr/bin/env raku`) are counted, because
     * the registered detectors give them a Raku file type.
     */
    private val WAKING_FILE_TYPES: List<FileType> = listOf(
        RakuScriptFileType.INSTANCE,
        RakuModuleFileType.INSTANCE,
        RakuPodFileType.INSTANCE,
    )

    /**
     * False while indexing. Erring toward silence is the right direction here:
     * the problem being solved is noise, so a widget that appears a moment
     * late is better than one that appears in a project with no Raku at all.
     */
    fun hasRakuFiles(project: Project): Boolean {
        if (project.isDisposed) return false
        // Blocking: both callers of this form are non-suspend and have no read
        // lock to inherit -- RakuStatusBarWidgetFactory.isAvailable and
        // RakuSdkUtil.reactToSdkIssue. The dumb check is inside the lock
        // because holding it is what stops a dumb-mode transition landing
        // between the check and the query.
        return ReadAction.compute<Boolean, RuntimeException> {
            if (DumbService.isDumb(project)) false else indexHasRakuFiles(project)
        }
    }

    /** A META6.json at the project root -- no dependencies exist without one. */
    fun isRakuDistribution(project: Project): Boolean =
        CommaProjectUtil.projectHasMetaFile(project)

    /**
     * For callers that get one shot. A background post-startup activity runs
     * while the index is still building, so `hasRakuFiles` would tell it
     * `false` and it would never ask again -- costing a real Raku project its
     * ecosystem for the whole session. Waiting is cheap: a project that never
     * becomes Raku simply gets `false` a moment later.
     *
     * `smartReadAction` rather than a wait followed by a read action: it holds
     * the read lock across the check and the query, and re-runs if indexing
     * restarts underneath. A wait that merely returns can be stale by the time
     * anything acts on it.
     */
    suspend fun awaitRakuFiles(project: Project): Boolean {
        if (project.isDisposed) return false
        return smartReadAction(project) { indexHasRakuFiles(project) }
    }

    /** Requires a read action and smart mode; both entry points arrange that. */
    private fun indexHasRakuFiles(project: Project): Boolean {
        val scope = GlobalSearchScope.projectScope(project)
        return WAKING_FILE_TYPES.any { FileTypeIndex.containsFileOfType(it, scope) }
    }
}
