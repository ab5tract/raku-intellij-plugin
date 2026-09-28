package org.raku.comma.utils

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.*
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.raku.comma.metadata.ExternalMetaFile
import org.raku.comma.project.RakuProjectKind
import org.raku.comma.services.application.RakuEcosystem
import org.raku.comma.services.project.*
import java.io.File
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

/**
 * Comma project-level utility functions. Often they simply pass a project on to a project-level service.
 * Also provides the super-important refreshProjectState function, which took a lot of sweat and tears to
 * get working correctly.
 */
object CommaProjectUtil {

    private val rakuExtensions = setOf("pm6", "pl6", "p6", "rakumod", "raku", "rakutest", "rakudoc")

    @JvmStatic
    fun canOpenFileAsProject(file: VirtualFile): Boolean {
        if (file.isDirectory) {
            if (file.toNioPath().resolve("META6.json").toFile().exists()) {
                return true
            }
            return pathContainsRakuCode(file)
        }
        val fileName = file.name
        return fileName == "META6.json" || fileName == "META.info"
    }

    @JvmStatic
    fun isRakudoCoreProject(project: Project): Boolean {
        return project.service<RakuProjectDetailsService>().isProjectRakudoCore
    }

    // The project-level flag misses the common case of browsing a Rakudo
    // checkout from some OTHER project (loose files, or a parent directory
    // opened as the project). A file inside a `rakudo/src/` tree is Rakudo
    // core regardless of which project it is viewed from.
    @JvmStatic
    fun isRakudoCoreFile(file: com.intellij.psi.PsiFile?): Boolean {
        if (file == null) return false
        if (isRakudoCoreProject(file.project)) return true
        return isRakudoCorePath(file.viewProvider.virtualFile.path)
    }

    @JvmStatic
    fun isRakudoCorePath(path: String?): Boolean {
        return path != null && path.contains("/rakudo/src/")
    }

    @JvmStatic
    fun projectContainsRakuCode(project: Project): Boolean {
        val basePath = project.basePath ?: return false
        val path = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(Path.of(basePath)) ?: return false
        return pathContainsRakuCode(path)
    }

    @JvmStatic
    fun pathContainsRakuCode(path: VirtualFile): Boolean {
        val foundFiles = mutableListOf<VirtualFile>()
        val filter = VirtualFileFilter { file ->
            // No content sniffing: an extensionless file whose first line merely
            // contained "raku" used to make a whole project Raku, and reading
            // every such file made the walk expensive as well as wrong. The
            // registered shebang file-type detectors handle real
            // `#!/usr/bin/env raku` scripts properly, for the callers that have
            // a project to index.
            (file.isDirectory && !file.path.endsWith(".idea"))
                    || rakuExtensions.contains(file.extension)
        }
        VfsUtilCore.iterateChildrenRecursively(path, filter) {
            if (it.isFile) foundFiles.add(it)
            // No need to recurse deeper if we have found even a single Raku file
            if (foundFiles.isNotEmpty()) return@iterateChildrenRecursively false
            return@iterateChildrenRecursively true
        }
        return foundFiles.isNotEmpty()
    }

    @JvmStatic
    fun scriptOnlyProject(project: Project): Boolean {
        return projectContainsRakuCode(project) && !projectHasMetaFile(project)
    }

    // Technically this should also be available at the Facet level so that different project-modules can have their
    // own META6.json. Fix this later!
    @JvmStatic
    fun projectDependencies(project: Project): List<String> {
        return if (projectHasMetaFile(project)) metaFile(project).depends.map { RakuUtils.stripAuthVerApi(it) } else listOf()
    }

    @JvmStatic
    fun projectProvides(project: Project): Set<String> {
        return if (projectHasMetaFile(project)) metaFile(project).provides.keys.map { RakuUtils.stripAuthVerApi(it) }.toSet() else setOf()
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    fun metaFile(project: Project): ExternalMetaFile {
        val meta6path = "%s%sMETA6.json".format(project.basePath, File.separator)
        check(Path.of(meta6path).toFile().exists()) { "There is no META6.json file in project path " + project.basePath }
        return json.decodeFromString(File(meta6path).readText())
    }

    fun projectHasMetaFile(project: Project): Boolean {
        val meta6path = "%s%sMETA6.json".format(project.basePath, File.separator)
        return Path.of(meta6path).toFile().exists()
    }

    suspend fun refreshProjectState(project: Project) {
        val sdkService = project.service<RakuProjectSdkService>()

        // The ecosystem is a network fetch plus a possible zef install, and it
        // only means anything where there are declared dependencies to
        // resolve. A folder of loose scripts gets neither; Tools > the Raku
        // widget > Refresh Ecosystem is how such a project opts in.
        //
        // The gate covers ONLY those two. Everything else here has to happen
        // for every Raku project: gating the dependency service along with
        // them left it uninitialised forever in a project with no META6.json
        // -- and this is its only call site anywhere -- so UsedModuleInspection
        // took its uninitialised branch permanently and flagged every
        // non-pragma `use Foo;` as missing from a META6.json the project does
        // not have.
        if (RakuProjectKind.isRakuDistribution(project)) {
            val maybeInstallZef =   if (sdkService.zef == null)
                                        project.service<RakuModuleInstallPrompt>().installZefItself()
                                    else CompletableFuture.completedFuture(0)

            withContext(Dispatchers.IO) {
                withBackgroundProgress(project, "Loading ecosystem details...") {
                    reportProgress { progress ->
                        progress.indeterminateStep {
                            service<RakuEcosystem>().initialize().get()
                        }
                    }
                }
            }

            // Initialize metadata listeners -- and wait for the parse. The
            // result is discarded; the ordering is not. installMissing() below
            // reads metadata.allDependencies, which stays empty until this
            // future has put the parsed META6.json in place, so skipping the
            // wait would silently suppress the missing-dependency prompt.
            val metaService = project.service<RakuMetaDataComponent>()
            withContext(Dispatchers.IO) { metaService.metaLoaded?.get() }

            if (withContext(Dispatchers.IO) { maybeInstallZef.get() } != 0) return
            initializeDependencies(project)
            project.service<RakuModuleInstallPrompt>().installMissing()
        } else {
            // No ecosystem and no zef for a folder of loose scripts -- but the
            // dependency service still has to come up. It is what resolves
            // `use` against installed modules, and there is no META6.json here
            // to wait on.
            project.service<RakuMetaDataComponent>()
            initializeDependencies(project)
        }
    }

    suspend fun initializeDependencies(project: Project) {
        project.service<RakuProjectDetailsService>().moduleServiceDidStartup = false
        project.service<RakuDependencyService>().initialize().join()
    }
}
