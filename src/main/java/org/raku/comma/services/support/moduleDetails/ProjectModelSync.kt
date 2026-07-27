package org.raku.comma.services.support.moduleDetails

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.roots.*
import com.intellij.openapi.roots.impl.libraries.LibraryEx
import com.intellij.serviceContainer.AlreadyDisposedException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.raku.comma.library.RakuLibraryType
import org.raku.comma.services.project.RakuMetaDataComponent
import org.raku.comma.services.project.RakuProjectSdkService
import java.util.concurrent.ConcurrentHashMap

class ProjectModelSync(private val project: Project, private val runScope: CoroutineScope) {

    private val module: Module? = project.modules.firstOrNull()

    fun syncExternalLibraries(completeDependencies: Set<String>, sdkName: String? = null) {
        if (ApplicationManager.getApplication().isUnitTestMode) return
        if (module == null) return

        val sdk = sdkName ?: project.service<RakuProjectSdkService>().sdkName

        try {
            runScope.launch {
                ModuleRootModificationUtil.updateModel(module) { model: ModifiableRootModel ->
                    val completeMETADependencies: MutableSet<String> = ConcurrentHashMap.newKeySet()
                    completeMETADependencies.addAll(completeDependencies)
                    val entriesPresentInMETA: MutableSet<String> = HashSet()

                    // TODO: As noted elsewhere, we are currently hard-wiring the plugin to only support a single
                    // "IntelliJ module" per project.
                    val metadata = project.service<RakuMetaDataComponent>()

                    // Every name here becomes part of a raku:// URL below, and the VFS
                    // will try to resolve whatever we hand it. A malformed META6 once
                    // produced a "dependency" holding a whole JSON blob, which the
                    // platform then tried to stat as a path; keep anything that cannot
                    // be a module name out of the model rather than relying on the
                    // parsers upstream never slipping again.
                    for (metaDep in completeMETADependencies.filter(::looksLikeModuleName)) {
                        // If local, project module, attach it as dependency
                        if (metadata.name == metaDep) {
                            val moduleOfMetaDep = metadata.module
                            if (moduleOfMetaDep != null) {
                                if (ModuleRootManager.getInstance(module).isDependsOn(moduleOfMetaDep)) {
                                    entriesPresentInMETA.add(moduleOfMetaDep.name)
                                    removeDuplicateEntries(model, moduleOfMetaDep.name)
                                } else {
                                    val entry: OrderEntry = model.addModuleOrderEntry(moduleOfMetaDep)
                                    entriesPresentInMETA.add(entry.presentableName)
                                }
                            }
                        } else {
                            entriesPresentInMETA.add(metaDep)

                            val maybeLibrary = model.moduleLibraryTable.getLibraryByName(metaDep)
                            if (maybeLibrary == null) {
                                // otherwise create and mark
                                val library = model.moduleLibraryTable.createLibrary(metaDep) as LibraryEx
                                val libraryModel = library.modifiableModel
                                // TODO: Figure out how we actually want to envision a raku:// protocol
                                // At the very least, it needs to be registered.
                                val url = String.format("raku://%d:%s!/", sdk.hashCode(), metaDep)
                                libraryModel.kind = RakuLibraryType.LIBRARY_KIND
                                libraryModel.addRoot(url, OrderRootType.SOURCES)
                                val entry = checkNotNull(model.findLibraryOrderEntry(library)) { library }
                                entry.scope = DependencyScope.COMPILE
                                runWriteAction { libraryModel.commit() }
                            } else {
                                removeDuplicateEntries(model, maybeLibrary.name)
                            }
                        }
                    }
                    removeOrderEntriesNotInMETA(model, entriesPresentInMETA)
                }
            }
        } catch (ignore: AlreadyDisposedException) {
            // If the application or the project was closed while we did not finish the job,
            // just ignore it hoping we will be more lucky next time: this sync
            // algorithm should be robust enough not to leave any incurable "leftovers"
            // and if it is so it's better to fix the underlying reasons.
            throw ignore
        }
    }

    // A Raku module name is identifier parts joined by `::`, optionally carrying
    // :ver/:auth/:api adverbs. It never contains whitespace, quotes or braces, which is
    // what a stringified chunk of JSON is made of.
    private fun looksLikeModuleName(name: String): Boolean =
        name.isNotBlank() && name.none { it.isWhitespace() || it in "{}[]\"" }

    private fun removeOrderEntriesNotInMETA(
        model: ModifiableRootModel,
        entriesPresentInMETA: MutableSet<String>
    ) {
        val currentEntries = model.orderEntries
        currentEntries.forEach { entry: OrderEntry ->
            if (! entriesPresentInMETA.contains(entry.presentableName)) {
                model.removeOrderEntry(entry)
            }
        }
    }

    private fun removeDuplicateEntries(model: ModifiableRootModel, name: String?) {
        var seen = false
        for (entry in model.orderEntries) {
            if (entry.presentableName == name) {
                if (seen) {
                    model.removeOrderEntry(entry)
                } else {
                    seen = true
                }
            }
        }
    }
}