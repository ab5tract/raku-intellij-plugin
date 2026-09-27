package org.raku.comma.ui.editorMenu

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.*
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.raku.comma.sdk.RakuSdkChooserUI
import org.raku.comma.services.application.RakuEcosystem
import org.raku.comma.services.project.RakuProjectSdkService
import javax.swing.Icon

class RakuStatusBarListPopupStep(
    val project: Project,
    private val scope: CoroutineScope,
) : ListPopupStep<String> {
    override fun getTitle(): String? {
        return "Current SDK: ${ project.service<RakuProjectSdkService>().sdkName }"
    }

    override fun canceled() {
        return
    }

    override fun isMnemonicsNavigationEnabled(): Boolean {
        return false
    }

    override fun getMnemonicNavigationFilter(): MnemonicNavigationFilter<String>? {
        return null
    }

    override fun isSpeedSearchEnabled(): Boolean {
        return false
    }

    override fun getSpeedSearchFilter(): SpeedSearchFilter<String>? {
        return null
    }

    override fun isAutoSelectionEnabled(): Boolean {
        return false
    }

    override fun getFinalRunnable(): Runnable? {
        return null
    }

    override fun getValues(): MutableList<String> {
        return mutableListOf("Select SDK...", "Add SDK", "Launch REPL", "Refresh Ecosystem")
    }

    override fun getDefaultOptionIndex(): Int {
        return -1
    }

    override fun getSeparatorAbove(aboveThis: String?): ListSeparator? {
        return if (aboveThis == "Launch REPL") ListSeparator() else null
    }

    override fun getTextFor(item: String?): String {
        return item!!
    }

    override fun getIconFor(item: String?): Icon? {
        return null
    }

    override fun isSelectable(item: String?): Boolean {
        return true
    }

    override fun hasSubstep(item: String?): Boolean {
        return item == "Select SDK..."
    }

    override fun onChosen(item: String?, choice: Boolean): PopupStep<*>? {
        when (item) {
            "Select SDK..."      -> return RakuSdkListPopupStep(project)
            "Add SDK"            -> RakuSdkChooserUI(project).show()
            "Launch REPL"        -> launchRepl()
            "Refresh Ecosystem"  -> refreshEcosystem()
        }
        return null
    }

    private fun launchRepl() {
        val action = ActionManager.getInstance().getAction("org.raku.comma.repl.RakuLaunchReplAction")
        ActionManager.getInstance().tryToExecute(action, null, null, null, true)
    }

    /**
     * Gated projects never fetch on their own, so this is the only way in for
     * a project with no META6.json -- and the only way to re-fetch a stale
     * ecosystem for one that has it.
     *
     * Under a progress indicator because the fetch is a network round trip:
     * refresh() itself returns immediately, and without this the only
     * evidence anything happened would be the ecosystem quietly changing
     * some seconds later.
     */
    private fun refreshEcosystem() {
        scope.launch {
            withBackgroundProgress(project, "Refreshing the Raku ecosystem...") {
                withContext(Dispatchers.IO) {
                    service<RakuEcosystem>().refresh().get()
                }
            }
        }
    }
}