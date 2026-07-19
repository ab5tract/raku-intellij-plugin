package org.raku.comma.services.project

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportProgress
import java.util.concurrent.atomic.AtomicBoolean
import com.intellij.psi.PsiFile
import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import org.raku.comma.metadata.ExternalMetaFile
import org.raku.comma.psi.RakuFile
import org.raku.comma.services.application.RakuEcosystem
import org.raku.comma.services.support.moduleDetails.DependencyDetails
import org.raku.comma.services.support.moduleDetails.ProjectModelSync
import org.raku.comma.utils.CommaProjectUtil

@Service(Service.Level.PROJECT)
class RakuDependencyService(private val project: Project, private val runScope: CoroutineScope) {

    private val initializationStatus = AtomicBoolean(false)
    var isInitialized: Boolean
        get() = initializationStatus.get()
        set(value) { initializationStatus.set(value) }
    val isNotInitialized: Boolean get() = !isInitialized

    private val refreshStatus = AtomicBoolean(false)
    var isRefreshing: Boolean
        get() = refreshStatus.get()
        set(value) { refreshStatus.set(value) }
    val isNotRefreshing: Boolean
        get() = !isRefreshing

    private val dependencyDetails = DependencyDetails(project, runScope)
    private val modelSync = ProjectModelSync(project, runScope)

    private var moduleDetailsState = ModuleDetailsState(project.service<RakuProjectSdkService>().sdkPath ?: "")
    val moduleDetails: ModuleDetailsState
        get() = moduleDetailsState

    private suspend fun doInitialize(state: ModuleDetailsState? = null) {
        return runScope.launch {
            if (isNotRefreshing) {
                var newState = state ?: moduleDetailsState.copy()

                val currentSdk = project.service<RakuProjectSdkService>().sdkPath
                                    ?: throw RuntimeException("There is no Raku SDK set, so cannot generate dependency details")
                if (newState.generatedForSdk != currentSdk) {
                    newState = newState.copy(provideToRakuFile = mutableMapOf(),
                                             dependencyToPath = mutableMapOf(),
                                             generatedForSdk = currentSdk)
                }

                isRefreshing = true
                runScope.future {
                    newState = dependencyDetails.fillState(newState)
                    moduleDetailsState = dependencyDetails.dependenciesToRakuFiles(newState).await()
                    modelSync.syncExternalLibraries(moduleDetailsState.currentDependenciesDeep)
                    isRefreshing = false
                    runScope.future {
                        withContext(Dispatchers.EDT) {
                            ProjectView.getInstance(project).refresh()
                            DaemonCodeAnalyzer.getInstance(project).restart()
                        }
                    }.join()
                }.join()
            }
        }.join()
    }

    @Synchronized
    fun initialize(state: ModuleDetailsState? = null): Job {
        return runScope.launch {
            val projectService = project.service<RakuProjectDetailsService>()
            if (projectService.noModuleServiceDidNotStartup) {
                isInitialized = false
                runScope.future {
                    withBackgroundProgress(project, "Refreshing ecosystem and dependency information") {
                        reportProgress {  reporter ->
                            reporter.sizedStep(23, "Refreshing ecosystem and dependency information") {
                                doInitialize(state)
                            }
                        }
                    }
                    projectService.moduleServiceDidStartup = true
                    isInitialized = true
                }.join()
            }
        }
    }

    fun moduleByProvide(provide: String): String? {
        return moduleDetailsState.ecoProvideToModule[provide]
    }

    fun provideToPsiFile(provide: String): PsiFile? {
        return provideToRakuFile(provide)
    }

    fun provideToRakuFile(provide: String): RakuFile? {
        return moduleDetailsState.provideToRakuFile[provide]
    }

    fun allProvides(): Set<String> {
        // META6 provides maps module name -> file path; the module names are
        // the provides.
        return moduleDetailsState.metaFiles.filter { it.provides.isNotEmpty() }
                                           .flatMap { it.provides.keys }
                                           .toSet()
    }

    fun moduleToRakuFiles(module: String): List<RakuFile> {
        val provides = moduleDetailsState.ecoModuleToProvides[module] ?: return listOf()
        return provides.mapNotNull { provideToRakuFile(it) }
    }

    fun dependencyInMeta(provide: String): Boolean {
        // First attempt resolution against loaded direct dependencies
        val currentDirect = moduleDetailsState.directDependencies.toSet()
        if (currentDirect.contains(provide)) return true
        // Look up the module via provides and try resolving that way
        val module = moduleByProvide(provide) ?: return false

        // Otherwise check whether the metaFile contents are different
        val metaDirect = CommaProjectUtil.projectDependencies(project).toSet()
        if (moduleDetailsState.directDependencies.toSet() != metaDirect) {
            moduleDetailsState = moduleDetailsState.copy(directDependencies = metaDirect)
            return metaDirect.contains(module)
        }

        // None of the above resolved, so the file isn't included
        return false
     }
}

data class ModuleDetailsState(
    val generatedForSdk: String,

    val directDependencies: Set<String> = setOf(),
    val secondaryDependencies: Set<String> = setOf(),
    val currentDependenciesDeep: Set<String> = setOf(),
    val provideDependenciesDeep: Set<String> = setOf(),

    val dependencyToPath: Map<String, String> = mapOf(),
    val provideToRakuFile: Map<String, RakuFile> = mapOf()
) {
    // Delegate to the RakuEcosystem service so that we don't need to open it
    val ecoProvideToPath: Map<String, String>               get() = service<RakuEcosystem>().ecosystem.ecoProvideToPath
    val ecoProvideToModule: Map<String, String>             get() = service<RakuEcosystem>().ecosystem.ecoProvideToModule
    val ecoModuleToProvides: Map<String, List<String>>      get() = service<RakuEcosystem>().ecosystem.ecoModuleToProvides

    val ecosystemRepository: Map<String, ExternalMetaFile>  get() = service<RakuEcosystem>().ecosystem.ecosystemRepository
    val moduleNames: List<String>                           get() = service<RakuEcosystem>().ecosystem.moduleNames
    val metaFiles: List<ExternalMetaFile>                   get() = service<RakuEcosystem>().ecosystem.metaFiles
}