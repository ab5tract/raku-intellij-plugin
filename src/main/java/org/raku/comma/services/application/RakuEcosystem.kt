package org.raku.comma.services.application

import com.intellij.openapi.components.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.raku.comma.metadata.ExternalMetaFile
import org.raku.comma.services.support.moduleDetails.ModuleListFetcher
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean

@Service(Service.Level.APP)
class RakuEcosystem(private val runScope: CoroutineScope) {

    val moduleListFetcher = ModuleListFetcher(runScope)

    private var initializationFuture = CompletableFuture<EcosystemDetailsState>()
    val isInitialized: Boolean
        get() = initializationFuture.isDone
    val isNotInitialized: Boolean get() = !isInitialized

    private val initializationStatus = AtomicBoolean(false)
    var isInitializing: Boolean
        get() = initializationStatus.get()
        set(new) = initializationStatus.set(new)
    val isNotInitializing: Boolean
        get() = !isInitializing

    // Deliberately NOT `= initialize().join()`. That fetched the ecosystem the
    // moment anything resolved the service -- so gating the call sites could
    // not work, since RakuDependencyService touches it from three properties --
    // and it blocked the resolving thread while doing so.
    private var ecosystemState = EcosystemDetailsState()
    val ecosystem: EcosystemDetailsState
        get() = ecosystemState

    @Synchronized
    fun initialize(): CompletableFuture<EcosystemDetailsState> {
        if (isNotInitializing && isNotInitialized) {
            isInitializing = true
            // Capture, do not read the field at completion time. refresh()
            // reassigns it, and a fetch takes seconds -- so an in-flight
            // coroutine reading the field would complete whichever future is
            // current, orphaning the one its own caller is blocked on.
            // CommaProjectUtil.refreshProjectState does exactly such a .get().
            val target = initializationFuture
            runScope.launch {
                ecosystemState = moduleListFetcher.fillState(EcosystemDetailsState())
                target.complete(ecosystemState.copy())
                isInitializing = false
            }
            return target
        } else {
            return if (isInitializing) initializationFuture
                   else CompletableFuture.completedFuture(ecosystemState)
        }
    }

    /**
     * Re-fetch, discarding what is cached.
     *
     * [initialize] cannot do this: it short-circuits once the future is
     * complete, so calling it again returns the old state.
     */
    @Synchronized
    fun refresh(): CompletableFuture<EcosystemDetailsState> {
        initializationFuture = CompletableFuture()
        isInitializing = false
        return initialize()
    }
}

data class EcosystemDetailsState(
    val ecoProvideToPath: Map<String, String> = mapOf(),
    val ecoProvideToModule: Map<String, String> = mapOf(),
    val ecoModuleToProvides: Map<String, List<String>> = mapOf(),

    val ecosystemRepository: Map<String, ExternalMetaFile> = mapOf(),
    val moduleNames: List<String> = listOf(),
    val metaFiles: List<ExternalMetaFile> = listOf(),
)
