package org.raku.comma.rakuast

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import org.raku.comma.RakuIcons

class RakuAstViewerFactory : ToolWindowFactory, DumbAware {
    override fun init(toolWindow: ToolWindow) {
        toolWindow.setIcon(RakuIcons.CAMELIA_13x13)
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = RakuAstViewerPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        content.putUserData(PANEL_KEY, panel)
        toolWindow.contentManager.addContent(content)
    }

    companion object {
        val PANEL_KEY = com.intellij.openapi.util.Key.create<RakuAstViewerPanel>("RakuAstViewerPanel")
        const val TOOL_WINDOW_ID = "RakuAST Viewer"

        fun findPanel(project: Project): RakuAstViewerPanel? =
            com.intellij.openapi.wm.ToolWindowManager.getInstance(project)
                .getToolWindow(TOOL_WINDOW_ID)
                ?.contentManager?.contents
                ?.firstNotNullOfOrNull { it.getUserData(PANEL_KEY) }
    }
}
