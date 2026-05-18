package org.raku.comma.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.util.containers.toArray
import org.jetbrains.annotations.NonNls
import java.util.*

class NewActionGroup : ActionGroup() {
    override fun getChildren(event: AnActionEvent?): Array<out AnAction> {
        val actions =
            (ActionManager.getInstance().getAction(IdeActions.GROUP_WEIGHING_NEW) as ActionGroup).getChildren(event)
        if (event == null || ActionPlaces.isMainMenuOrActionSearch(event.place)) {
            val newGroup = ActionManager.getInstance().getAction(PROJECT_OR_MODULE_GROUP_ID)
            if (newGroup != null) {
                val newProjectActions = (newGroup as ActionGroup).getChildren(event)
                if (newProjectActions.size > 0) {
                    val mergedActions: MutableList<AnAction?> =
                        ArrayList<AnAction?>(newProjectActions.size + 1 + actions.size)
                    Collections.addAll<AnAction?>(mergedActions, *newProjectActions)
                    mergedActions.add(Separator.getInstance())
                    Collections.addAll<AnAction?>(mergedActions, *actions)
                    return mergedActions.toArray<AnAction?>(EMPTY_ARRAY) as Array<out AnAction>
                }
            }
        }
        return actions
    }

    companion object {
        private const val PROJECT_OR_MODULE_GROUP_ID: @NonNls String = "NewProjectOrModuleGroup"
    }
}
