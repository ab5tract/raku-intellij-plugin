package org.raku.comma.utils

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import org.raku.comma.RakuIcons
import org.raku.comma.actions.UpdateExtensionsAction
import org.raku.comma.actions.UpdateExtensionsAction.RakuChooseExtensionsToUpdateDialog
import org.raku.comma.project.RakuProjectKind

class RakuLegacyExtensionsDetector : ProjectActivity {
    override suspend fun execute(project: Project) {
        // Advice about Raku source needs Raku source. The pattern matches .pm,
        // .pod and .t, which are current Perl 5 extensions -- without this a
        // Perl project is told its files are "obsolete Raku extensions".
        // awaitRakuFiles, not hasRakuFiles: this is a one-shot startup
        // activity that runs while the index may still be building, and it
        // also calls FilenameIndex below, which throws IndexNotReadyException
        // in dumb mode -- hasRakuFiles would either answer false for good or
        // let the crash through, depending on timing.
        if (! RakuProjectKind.awaitRakuFiles(project)) return

        // smartReadAction, because FilenameIndex throws IndexNotReadyException
        // in dumb mode and awaitRakuFiles only guarantees smart mode at the
        // instant it returned -- indexing can restart in the gap. This form
        // re-runs instead of letting the exception out of a startup activity.
        val filesToUpdate = smartReadAction(project) {
            UpdateExtensionsAction.collectFilesWithLegacyNames(project)
        }
        if (filesToUpdate.isNotEmpty()) {
            val notification = Notification(
                "raku.misc", "Obsolete Raku extensions are detected",
                "Obsolete file extensions are detected: " + filesToUpdate.keys.joinToString(", "),
                NotificationType.WARNING
            )
            notification.setIcon(RakuIcons.CAMELIA)
            notification.addAction(object : AnAction("Run Comma Legacy File Rename Tool") {
                override fun actionPerformed(e: AnActionEvent) {
                    notification.expire()
                    RakuChooseExtensionsToUpdateDialog(project, filesToUpdate).show()
                }
            })
            Notifications.Bus.notify(notification)
        }
    }
}
