package org.raku.comma.ui.editorMenu

import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.wm.impl.status.widget.StatusBarWidgetsManager

/**
 * RakuStatusBarWidgetFactory.isAvailable asks the file-type index, which has no
 * answer during indexing and so reports false. That is the right default -- the
 * problem being fixed is noise -- but it means the widget needs a second look
 * once the index is ready, or a real Raku project shows no butterfly until it
 * is reopened.
 */
class RakuWidgetSmartModeRefresher : ProjectActivity {
    override suspend fun execute(project: Project) {
        DumbService.getInstance(project).runWhenSmart {
            if (! project.isDisposed) {
                project.service<StatusBarWidgetsManager>()
                    .updateWidget(RakuStatusBarWidgetFactory::class.java)
            }
        }
    }
}
