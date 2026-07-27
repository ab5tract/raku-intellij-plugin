package org.raku.comma.services.project

import com.intellij.execution.ExecutionException
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.serviceContainer.AlreadyDisposedException
import com.intellij.testFramework.LightVirtualFile
import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import org.raku.comma.pm.RakuPackageManager
import org.raku.comma.pm.impl.RakuZefPM
import org.raku.comma.psi.RakuFile
import org.raku.comma.psi.RakuPackageDecl
import org.raku.comma.psi.RakuPsiElement
import org.raku.comma.psi.external.ExternalRakuFile
import org.raku.comma.psi.symbols.*
import org.raku.comma.psi.type.RakuResolvedType
import org.raku.comma.psi.type.RakuType
import org.raku.comma.psi.type.RakuUnresolvedType
import org.raku.comma.sdk.*
import org.raku.comma.services.RakuServiceConstants
import org.raku.comma.services.application.RakuGlobalSdkSymbolCache
import org.raku.comma.utils.CommaProjectUtil.isRakudoCoreProject
import org.raku.comma.utils.RakuCommandLine
import org.raku.comma.utils.RakuUtils
import java.io.IOException
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.exists

/**
 * When the Raku plugin is used in products such as IDEA or Webstorm,
 * setting Raku SDK is either impossible or interferes with main project code.
 * To work around this, we provide some UI to the user to set specific Raku SDK
 * for a project. This service maintains state of this SDK and can be used to set/obtain
 * the SDK for Raku parts.
 */
@Service(Service.Level.PROJECT)
@State(name = "Raku.SDK", storages = [Storage(value = RakuServiceConstants.PROJECT_SETTINGS_FILE)])
class RakuProjectSdkService(
    private val project: Project,
    val runScope: CoroutineScope
) : PersistentStateComponent<RakuSDKState>, DumbAware {
    private var sdkState: RakuSDKState = RakuSDKState()
    private var symbolCacheInstance: ProjectSdkSymbolCache? = null

    val symbolCache: ProjectSdkSymbolCache
        get() = provideSymbolCache()

    var sdkPath: String?
        get() = state.projectSdkPath
        set(newPath) = setProjectSdkPath(newPath!!)

    val rakuPath: String?
        get() = if (sdkPath != null) sdkPath!! + "/raku" else null

    val zefPath: String
        get() = calculateZefPath(sdkPath!!)

    val sdkName: String
        get() = "Raku ${sdkState.projectSdkVersion ?: "<UNKNOWN>"}"

    val moarBuildConfig: Map<String, String>
        get() = generateMoarBuildConfiguration()

    private val isLoading = AtomicBoolean(false)

    val sdkIsSet: Boolean
        get() = ! sdkState.projectSdkPath.isNullOrEmpty()
    val sdkIsNotSet: Boolean
        get() = ! sdkIsSet

    override fun getState(): RakuSDKState {
        if (sdkIsNotSet && !isLoading.get()) {
            isLoading.set(true)
            runScope.future {
                promptForSdkPath()
                isLoading.set(false)
            }.join()
        }
        return sdkState
    }

    override fun loadState(rakuSDKState: RakuSDKState) {
        if (rakuSDKState.projectZefPath.isNullOrEmpty() && sdkIsSet) {
            rakuSDKState.projectZefPath = calculateZefPath(sdkPath!!)
        }
        sdkState = rakuSDKState
    }

    private suspend fun promptForSdkPath() {
        if (! project.service<RakuProjectDetailsService>().hasProjectSdkPrompted) {
            runScope.async {
                withContext(Dispatchers.EDT) {
                    val chooser = RakuSdkChooserUI(project, null)
                    chooser.show()
                }
            }.await()
        }
    }

    fun calculateZefPath(sdkPath: String): String {
        if (sdkPath.isBlank()) return ""
        try {
            val resolution = Path.of(sdkPath).parent.resolve("share/perl6/site/bin/zef")
            return if (resolution.exists()) resolution.toString() else ""
        } catch (_: InvalidPathException) {}
        return ""
    }

    fun setProjectSdkPath(sdkPath: String) {
        // Re-setting the path it already has is a no-op that used to cost a
        // `raku -e` subprocess (versionString), a full symbol-cache invalidation
        // -- and so a re-parse of the ~3MB CORE.setting JSON -- and a
        // dependency-refreshing meta build. In the IDE that fires whenever the
        // settings dialog is closed unchanged; under test, CommaFixtureTestCase
        // .setUp does it before every single test.
        if (sdkState.projectSdkPath == sdkPath && symbolCache.sdkPath == sdkPath) return

        sdkState.projectSdkPath    = sdkPath
        sdkState.projectSdkVersion = RakuSdkUtil.versionString(sdkPath)
        sdkState.projectZefPath    = calculateZefPath(sdkPath)

        symbolCache.sdkPath = sdkPath
        symbolCache.invalidateCache()

        // TODO: Move to Facets for module / allow for multiple metas + modules in a single project
//        for (module in ModuleManager.getInstance(project).modules) {
//            val component = module.getService(RakuMetaDataComponent::class.java)
//            component?.triggerMetaBuild()
//        }
        project.service<RakuMetaDataComponent>().triggerMetaBuild(refreshDependencies = true)
    }

    val zef: RakuPackageManager?
        get() {
            val zefPath = calculateZefPath(sdkPath ?: "")
            return  if (zefPath.isNotEmpty())
                        RakuZefPM(project, zefPath)
                    else null
        }

    private fun generateMoarBuildConfiguration(): Map<String, String> {
        val subs: List<String>

        try {
            val cmd = RakuCommandLine(project)
            cmd.setWorkDirectory(System.getProperty("java.io.tmpdir"))
            cmd.addParameter("--show-config")
            subs = cmd.executeAndRead()
            val buildConfig: MutableMap<String, String> = TreeMap()

            for (line in subs) {
                val equalsPosition = line.indexOf('=')
                if (equalsPosition > 0) {
                    val key = line.substring(0, equalsPosition)
                    val value = line.substring(equalsPosition + 1)
                    buildConfig[key] = value
                }
            }
            return buildConfig
        } catch (e: ExecutionException) {
            RakuSdkUtil.reactToSdkIssue(project, "Unable to generate moar build config", ex = e)
            return mapOf()
        }
    }

    private fun provideSymbolCache(): ProjectSdkSymbolCache {
        if (symbolCacheInstance == null) {
            symbolCacheInstance = ProjectSdkSymbolCache(project, sdkPath, runScope)
        }
        return symbolCacheInstance!!
    }
}

class RakuSDKState : BaseState() {
    var projectSdkPath by string()
    var projectSdkVersion by string()
    var projectZefPath by string()
}

private class ProjectSymbolCache() {
    // Symbol caches
    var useNameFileCache: MutableMap<String, RakuFile> = ConcurrentHashMap()
    var needNameFileCache: MutableMap<String, RakuFile> = ConcurrentHashMap()
    var setting: RakuFile? = null
    var settingTypeCache: MutableMap<RakuSettingTypeId, RakuType>? = null

    fun invalidateFileCache() {
        useNameFileCache = ConcurrentHashMap()
        needNameFileCache = ConcurrentHashMap()
        setting = null
        settingTypeCache = null
    }
}

class ProjectSdkSymbolCache(private val project: Project, var sdkPath: String?, private val runScope: CoroutineScope) {
        // Project-specific cache with PsiFile instances
    private val projectSymbolCache: ProjectSymbolCache = ProjectSymbolCache()

    companion object {
        const val SETTING_FILE_NAME: String = "SETTINGS.rakumod"
    }

    fun getCoreSettingFile(): RakuFile? {
        if (projectSymbolCache.setting != null) return projectSymbolCache.setting

        val settingSdkPath = sdkPath
        val globalCache = service<RakuGlobalSdkSymbolCache>()

        // Another project (or an earlier incarnation of this one) may already
        // have paid for this SDK's symbols. Re-check on every call, not just the
        // first: while a load is in flight this is also how callers polling for
        // a non-DUMMY file pick the result up.
        val cachedJson = settingSdkPath?.let { globalCache.settingJsonCache[it] }
        if (cachedJson != null) {
            projectSymbolCache.setting = makeSettingSymbols(cachedJson)
            return projectSymbolCache.setting
        }

        val coreSymbols = RakuUtils.getResourceAsFile("symbols/raku-core-symbols.raku")
        val coreDocs = RakuUtils.getResourceAsFile("docs/core.json")

        if (coreSymbols == null || coreDocs == null) {
            val errorMessage =
                if (coreSymbols == null)
                    "getCoreSettingFile is called with corrupted resources bundle, using fallback"
                else
                    "getCoreSettingFile is called with corrupted resources bundle"
            RakuSdkUtil.reactToSdkIssue(project, "Issue Gathering Core Symbols", message = errorMessage)
            return getFallback()
        }

        try {
            if (settingSdkPath == null) return getFallback()

            if (globalCache.settingLoadsStarted.add(settingSdkPath)) {
                val cmd = RakuCommandLine(settingSdkPath)
                cmd.setWorkDirectory(System.getProperty("java.io.tmpdir"))
                cmd.addParameter(coreSymbols.absolutePath)
                cmd.addParameter(coreDocs.absolutePath)
                runScope.launch {
                    var loaded = false
                    try {
                        val settingLines = java.lang.String.join("\n", cmd.executeAndRead(coreSymbols))
                        try {
                            coreSymbols.delete()
                            coreDocs.delete()
                        } catch (ignored: Exception) {}

                        if (settingLines.isEmpty()) {
                            RakuSdkUtil.reactToSdkIssue(project,
                                                        "Issue Gathering Core Symbols",
                                                        "getCoreSettingFile got no symbols from Raku, using fallback")
                            getFallback()
                        } else {
                            loaded = true
                            globalCache.settingJsonCache[settingSdkPath] = settingLines
                            projectSymbolCache.setting = makeSettingSymbols(settingLines)
                        }
                        RakuSdkUtil.triggerCodeAnalysis(project)
                    } catch (e: AssertionError) {
                        // If the project was already disposed, do not die in a background thread
                    } finally {
                        // Only a successful load gets to keep the claim. Leaving
                        // it held after a failure would pin every project on this
                        // SDK to the fallback symbols for the rest of the session,
                        // with no way to retry.
                        if (!loaded) globalCache.settingLoadsStarted.remove(settingSdkPath)
                    }
                }
            } else {
                try {
                    coreSymbols.delete()
                    coreDocs.delete()
                } catch (ignored: Exception) { }
            }
            return ExternalRakuFile(project, LightVirtualFile("DUMMY"))
        } catch (e: ExecutionException) {
            RakuSdkUtil.reactToSdkIssue(project, "Problem encountered while digesting CORE.setting symbols", ex = e)
            return getFallback()
        }
    }

    fun getCoreSettingType(type: RakuSettingTypeId): RakuType {
        // Ensure we have the setting type cache.
        if (projectSymbolCache.settingTypeCache == null) {
            val setting = projectSymbolCache.setting
            if (setting != null) {
                val typeCache: MutableMap<RakuSettingTypeId, RakuType> = EnumMap(org.raku.comma.sdk.RakuSettingTypeId::class.java)
                for (typeId in RakuSettingTypeId.entries) {
                    val typeName = unmangleTypeId(typeId.name)
                    val symbol = setting.resolveLexicalSymbol(RakuSymbolKind.TypeOrConstant, typeName)
                    if (symbol != null) {
                        val resolution = symbol.psi
                        if (resolution is RakuPsiElement) {
                            typeCache[typeId] = RakuResolvedType(typeName, resolution)
                        }
                    }
                }
                projectSymbolCache.settingTypeCache = typeCache
            }
        }

        // Try to resolve.
        val typeCache = projectSymbolCache.settingTypeCache
        if (typeCache != null) {
            val result = typeCache[type]
            if (result != null) return result
        }

        // Return an unresolved type if all else fails.
        return RakuUnresolvedType(unmangleTypeId(type.name))
    }

    private fun getFallback(): RakuFile? {
        val fallback = RakuUtils.getResourceAsFile("symbols/CORE.fallback")
        if (fallback == null) {
            RakuSdkUtil.reactToSdkIssue(
                project,
                "Issue Gathering Core Symbols",
            """
                    Could not get CORE.setting symbols, perhaps due to a corrupted resources bundle.
                    Try to set a proper SDK for this project.
                    """
            )
            return ExternalRakuFile(project, LightVirtualFile(SETTING_FILE_NAME))
        }

        try {
            return makeSettingSymbols(Files.readString(fallback.toPath()))?.also { projectSymbolCache.setting = it }
        } catch (e: IOException) {
            RakuSdkUtil.reactToSdkIssue(project, "Could not provide CORE.setting fallback", ex = e)
            return ExternalRakuFile(project, LightVirtualFile(SETTING_FILE_NAME))
        }
    }

    private fun makeSettingSymbols(json: String): RakuFile? {
        try {
            val entries = RakuExternalNamesParser.tryDecode(json)
                ?: return ExternalRakuFile(project, LightVirtualFile(SETTING_FILE_NAME))
            if (project.isDisposed) return null
            val rakuFile = ExternalRakuFile(project, LightVirtualFile(SETTING_FILE_NAME))
            val parser = RakuExternalNamesParser(project, rakuFile, entries).parse()
            rakuFile.setSymbols(parser.result())
            return rakuFile
        } catch (_: Exception) {}

        return ExternalRakuFile(project, LightVirtualFile(SETTING_FILE_NAME))
    }

    private fun unmangleTypeId(id: String): String {
        return id.replace("__", "::")
    }

    fun getPsiFileForModule(name: String, invocation: String): RakuFile? {
        val fileCache = if (invocation.startsWith("use"))
                            projectSymbolCache.useNameFileCache
                        else
                            projectSymbolCache.needNameFileCache
        // If we have anything in file cache, return it
        if (fileCache.containsKey(name)) return fileCache[name]

        val globalCache = service<RakuGlobalSdkSymbolCache>()

        // if not, check if we have symbol cache, if yes, parse, save and return it
        val symbolCache: MutableMap<String, String> = if (invocation.startsWith("use"))
                                                          globalCache.useNameSymbolCache
                                                      else
                                                          globalCache.needNameSymbolCache
        val packagesStarted: MutableSet<String> = if (invocation.startsWith("use"))
                                                      globalCache.myUsePackagesStarted
                                                  else
                                                      globalCache.myNeedPackagesStarted
        if (symbolCache.containsKey(name)) {
            return fileCache.compute(name) { n: String, v: RakuFile? ->
                constructExternalPsiFile(project, n, symbolCache[n]!!)
            }
        }

        if (! packagesStarted.contains(name)) {
            packagesStarted.add(name)
            ApplicationManager.getApplication().executeOnPooledThread {
                // packagesStarted is on the *application*-level symbol cache
                // (keyed by module name only, shared across every project), so
                // a queued pooled-thread task can easily still be pending when
                // the project that requested it is long gone -- bail out
                // before spawning the raku subprocess rather than keeping a
                // disposed project reachable for the duration of that work.
                try {
                    if (project.isDisposed) return@executeOnPooledThread
                    // if no symbol cache, compute as usual
                    fileCache.compute(name) { n: String, v: RakuFile? ->
                        constructExternalPsiFile(project, name, invocation, symbolCache)
                    }
                    RakuSdkUtil.triggerCodeAnalysis(project)
                } catch (e: java.lang.AssertionError) {
                    // If the project was already disposed, do not die in a background thread
                } finally {
                    // packagesStarted is a permanent "already requested" claim.
                    // If this run did not actually populate the shared symbol
                    // cache -- disposed project, or a failure -- leaving the
                    // name in it poisons that module for the rest of the JVM:
                    // every later request short-circuits here, nothing is ever
                    // loaded, and callers that wait for real symbols (e.g.
                    // CommaFixtureTestCase.ensureModuleIsLoaded) burn their full
                    // timeout. Release the claim so a later caller can retry.
                    if (! symbolCache.containsKey(name)) packagesStarted.remove(name)
                }
            }
        }
        return ExternalRakuFile(project, LightVirtualFile("DUMMY"))
    }

    private fun constructExternalPsiFile(project: Project, name: String, externalsJSON: String): RakuFile? {
        var rakuFile: ExternalRakuFile? = null
        try {
            val dummy = LightVirtualFile("$name.rakumod")
            rakuFile = ExternalRakuFile(project, dummy)
            val parser = RakuExternalNamesParser(project, rakuFile, externalsJSON)
            rakuFile.setSymbols(parser.parse().result())
        } catch (ignored: AlreadyDisposedException) {
        }
        return rakuFile
    }

    private fun constructExternalPsiFile(
        project: Project,
        name: String,
        invocation: String,
        symbolCache: MutableMap<String, String>
    ): RakuFile {
        val dummy = LightVirtualFile("$name.rakumod")
        val rakuFile = ExternalRakuFile(project, dummy)
        val symbols = loadModuleSymbols(rakuFile, name, invocation, symbolCache, false)
        rakuFile.setSymbols(symbols)
        return rakuFile
    }

    fun loadModuleSymbols(
        rakuFile: RakuFile,
        name: String,
        invocation: String,
        symbolCache: MutableMap<String, String>,
        addLib: Boolean
    ): List<RakuSymbol> {
        if (invocation == "use nqp" || isRakudoCoreProject(project)) return getNQPSymbols(rakuFile)

        val moduleSymbols = RakuUtils.getResourceAsFile("symbols/raku-module-symbols.raku")
                                    ?: return ArrayList()

        try {
            val cmd = RakuCommandLine(project)
            cmd.setWorkDirectory(project.basePath)
            if (addLib) {
                cmd.addParameter("-I.")
            }
            cmd.addParameters(moduleSymbols.getPath(), invocation)
            val text = java.lang.String.join("\n", cmd.executeAndRead(moduleSymbols))
            val symbols = RakuExternalNamesParser.tryDecode(text) ?: return ArrayList()
            symbolCache[name] = text
            return RakuExternalNamesParser(project, rakuFile, symbols).parse().result()
        } catch (e: ExecutionException) {
            return ArrayList()
        }
    }

    private fun getNQPSymbols(rakuFile: RakuFile): List<RakuSymbol> {
        var ops: MutableList<String?> = ArrayList()
        val nqpSymbols = RakuUtils.getResourceAsFile("symbols/nqp.ops")
        if (nqpSymbols == null) {
            ops.add("[]")
        } else {
            val nqpSymbolsPath = nqpSymbols.toPath()
            try {
                ops = Files.readAllLines(nqpSymbolsPath)
                nqpSymbols.delete()
            } catch (e: IOException) {
                ops.add("[]")
            }
        }

        // TODO: Investigate whether this fixes anything
//        List<String> allOps = new ArrayList<>(ops);
//        ops.forEach(op -> allOps.add("nqp::" + op));
        return RakuExternalNamesParser(project, rakuFile, java.lang.String.join("\n", ops)).parse().result()
    }

    fun contributeParentSymbolsFromCore(
        collector: RakuSymbolCollector,
        coreSetting: RakuFile,
        parentName: String,
        allowed: MOPSymbolsAllowed
    ) {
        val parentCollector = RakuSingleResolutionSymbolCollector(parentName, RakuSymbolKind.TypeOrConstant)

        coreSetting.contributeGlobals(parentCollector, HashSet())
        if (parentCollector.isSatisfied) {
            val decl = parentCollector.result.psi as RakuPackageDecl
            decl.contributeMOPSymbols(collector, allowed)
        }
    }

    fun invalidateCache() {
        projectSymbolCache.invalidateFileCache()
    }
}