package org.raku.comma.ui.editorMenu

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.EditorBasedStatusBarPopup
import kotlinx.coroutines.CoroutineScope
import org.raku.comma.RakuIcons
import javax.swing.Icon

class RakuStatusBarWidget(
    project: Project,
    scope: CoroutineScope
) : StatusBarWidget.IconPresentation, EditorBasedStatusBarPopup(project, false, scope)
{
    override fun ID(): String {
        return "Raku SDK Selector"
    }

    override fun createInstance(project: Project): StatusBarWidget {
        return RakuStatusBarWidget(project, scope)
    }

    override fun createPopup(context: DataContext): ListPopup? {
        return JBPopupFactory.getInstance().createListPopup(RakuStatusBarListPopupStep(project, scope))
    }

    override fun getWidgetState(file: VirtualFile?): WidgetState {
        return WidgetState(ID(), getTooltipText(), true)
    }

    override fun getIcon(): Icon {
        return RakuIcons.CAMELIA
    }

    override fun getTooltipText(): String? {
        return "\uD83E\uDD8B"
    }
}