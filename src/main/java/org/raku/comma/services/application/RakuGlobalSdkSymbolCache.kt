package org.raku.comma.services.application

import com.intellij.openapi.components.Service
import com.intellij.util.containers.ContainerUtil
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.APP)
class RakuGlobalSdkSymbolCache {
    val useNameSymbolCache: ConcurrentHashMap<String, String> = ConcurrentHashMap()
    val needNameSymbolCache: ConcurrentHashMap<String, String> = ConcurrentHashMap()

    val myNeedPackagesStarted: MutableSet<String> = ContainerUtil.newConcurrentSet()
    val myUsePackagesStarted: MutableSet<String> = ContainerUtil.newConcurrentSet()

    // Raw CORE.setting JSON, keyed by SDK path. The PSI built from it is
    // project-scoped and has to stay in ProjectSdkSymbolCache, but the ~3MB of
    // JSON -- and the ~4s `raku raku-core-symbols.raku` subprocess that produces
    // it -- depend only on the SDK, so every project can share one copy. Without
    // this, each new project re-runs the subprocess; when it fails or returns
    // empty the code silently falls back to the bundled symbols/CORE.fallback,
    // which is an older and less complete dump, and symbol-dependent behaviour
    // (deprecation warnings, completion) quietly degrades.
    val settingJsonCache: ConcurrentHashMap<String, String> = ConcurrentHashMap()
    val settingLoadsStarted: MutableSet<String> = ContainerUtil.newConcurrentSet()
}