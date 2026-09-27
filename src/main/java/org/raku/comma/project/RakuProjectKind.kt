package org.raku.comma.project

import com.intellij.openapi.application.readAction
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.waitForSmartMode
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
        if (project.isDisposed || DumbService.isDumb(project)) return false
        val scope = GlobalSearchScope.projectScope(project)
        return WAKING_FILE_TYPES.any { FileTypeIndex.containsFileOfType(it, scope) }
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
     */
    suspend fun awaitRakuFiles(project: Project): Boolean {
        project.waitForSmartMode()
        // hasRakuFiles queries FileTypeIndex, which asserts it is called from
        // inside a read action -- true regardless of thread, and not granted
        // merely by having waited for smart mode. A caller off the platform's
        // own coroutine dispatchers (this method has no control over who
        // calls it) has no read lock to inherit, so one is taken explicitly.
        return readAction { hasRakuFiles(project) }
    }
}
