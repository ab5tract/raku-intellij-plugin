package org.raku.comma.services.project

import com.intellij.openapi.components.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import org.raku.comma.services.RakuServiceConstants
import java.util.concurrent.atomic.AtomicBoolean

@Service(Service.Level.PROJECT)
@State(name = "Raku.Project.Details", storages = [Storage(value = RakuServiceConstants.PROJECT_SETTINGS_FILE)])
class RakuProjectDetailsService(
    val project: Project,
    private val runScope: CoroutineScope
) : PersistentStateComponent<RakudoProjectState>, DumbAware {
    var projectState: RakudoProjectState = RakudoProjectState()

    override fun getState(): RakudoProjectState {
        return refreshState(projectState)
    }

    override fun loadState(state: RakudoProjectState) {
        this.projectState = refreshState(state)
    }

    // moduleServiceStatus
    // Tracks whether the module details and dependencies have been loaded. Reset prior to refreshing these services.
    private val moduleServiceStatus = AtomicBoolean(false)
    var moduleServiceDidStartup: Boolean
        get() = moduleServiceStatus.get()
        set(value) = moduleServiceStatus.set(value)
    val noModuleServiceDidNotStartup: Boolean
        get() = !moduleServiceDidStartup

    // missingDependenciesNotificationStatus
    // Tracks whether we have already prompted about missing dependencies. This avoids a pile-up of notification prompts.
    private val missingDependencyNotificationStatus = AtomicBoolean(false)
    var hasNotifiedMissingDependencies: Boolean
        get() = missingDependencyNotificationStatus.get()
        set(value) = missingDependencyNotificationStatus.set(value)

    // projectSdkPromptedStatus
    // Tracks whether the SDK prompt has already been shown to the user. This allows 'Cancel' to be
    // meaningfully selected without resulting in the popup repeatedly appearing as a result.
    private val projectSdkPromptedStatus = AtomicBoolean(false)
    var hasProjectSdkPrompted: Boolean
        get() = projectSdkPromptedStatus.get()
        set(value) = projectSdkPromptedStatus.set(value)

    // Detail:  isProjectProjectRakudoCore
    // Purpose: To allow toggling certain annotations and other features that make sense in regular
    //          Raku projects but which make hacking on the Rakudo internals annoying.
    val isProjectRakudoCore: Boolean
        get() = state.isProjectRakudoCore

    // rakudoCoreDeterminedStatus
    // determineIfProjectIsRakudoCore() is derived once and then left alone, so a
    // manual override (tests pin a project to "is Rakudo core" without renaming
    // it) survives later reads instead of being silently re-derived away.
    private val rakudoCoreDeterminedStatus = AtomicBoolean(false)
    var hasDeterminedRakudoCore: Boolean
        get() = rakudoCoreDeterminedStatus.get()
        set(value) = rakudoCoreDeterminedStatus.set(value)

    private fun determineIfProjectIsRakudoCore(): Boolean {
        return project.name == "rakudo" || project.basePath?.endsWith("rakudo") == true
    }

    private fun refreshState(state: RakudoProjectState): RakudoProjectState {
        if (!hasDeterminedRakudoCore) {
            state.isProjectRakudoCore = determineIfProjectIsRakudoCore()
        }
        hasDeterminedRakudoCore = true
        return state
    }
}

class RakudoProjectState : BaseState() {
    var isProjectRakudoCore by property(false)
}