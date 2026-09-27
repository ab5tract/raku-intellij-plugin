package org.raku.comma.project.activity

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import org.raku.comma.project.RakuProjectKind
import org.raku.comma.utils.CommaProjectUtil

class RakuServiceStarter : ProjectActivity {
    override suspend fun execute(project: Project) {
        if (! RakuProjectKind.hasRakuFiles(project)) return
        CommaProjectUtil.refreshProjectState(project)
    }
}
