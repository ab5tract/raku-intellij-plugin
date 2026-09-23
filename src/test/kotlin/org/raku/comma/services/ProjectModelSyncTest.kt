package org.raku.comma.services

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ModuleOrderEntry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ModuleSourceOrderEntry
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.impl.libraries.LibraryEx
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.raku.comma.RakuForeignModuleProjectDescriptor
import org.raku.comma.library.RakuLibraryType
import org.raku.comma.services.support.moduleDetails.ProjectModelSync

// The dependency sync owns exactly the `raku://` libraries it creates itself.
// Everything else on the module -- the JDK, the module's own sources, and
// anything another build system put there -- belongs to somebody else, and
// pruning it is how the plugin used to eat its own IDE classpath: opening this
// project in IntelliJ left `raku-intellij-plugin.main` with nothing but 78 Raku
// libraries, so every `com.intellij.*` import in the plugin's own sources went
// unresolved.
class ProjectModelSyncTest : BasePlatformTestCase() {

    override fun getProjectDescriptor(): LightProjectDescriptor = RakuForeignModuleProjectDescriptor

    private val foreignLibrary = "Gradle: com.jetbrains.intellij.platform:ide-impl:262.8665.258"
    private val staleRakuLibrary = "Stale::Dependency"

    private val sync: ProjectModelSync
        get() = ProjectModelSync(project, CoroutineScope(Dispatchers.Default))

    fun testPruningRemovesOnlyRakuLibrariesItOwns() {
        ModuleRootModificationUtil.updateModel(myFixture.module) { model ->
            model.moduleLibraryTable.createLibrary(foreignLibrary)

            val stale = model.moduleLibraryTable.createLibrary(staleRakuLibrary) as LibraryEx
            val staleModel = stale.modifiableModel
            staleModel.kind = RakuLibraryType.LIBRARY_KIND
            staleModel.addRoot("raku://0:$staleRakuLibrary!/", OrderRootType.SOURCES)
            runWriteAction { staleModel.commit() }

            // Nothing is in META any more, so the sync wants to prune everything
            // it considers its own.
            sync.removeOrderEntriesNotInMETA(model, HashSet())
        }

        val entries = ModuleRootManager.getInstance(myFixture.module).orderEntries
        val libraryNames = entries.filterIsInstance<LibraryOrderEntry>().mapNotNull { it.libraryName }

        assertTrue(
            "a library the sync does not own must survive pruning, but the entries were $libraryNames",
            libraryNames.contains(foreignLibrary)
        )
        assertFalse(
            "a Raku library no longer in META must be pruned",
            libraryNames.contains(staleRakuLibrary)
        )
        assertTrue(
            "the module's own sources must survive pruning",
            entries.any { it is ModuleSourceOrderEntry }
        )
    }

    // Deduplication is still a removal, so it obeys the same rule: a dependency
    // on a module the plugin did not create is not its to tidy up, however
    // redundant the second copy looks.
    fun testDuplicatePruningLeavesNonRakuModuleDependenciesAlone() {
        val foreign = RakuForeignModuleProjectDescriptor.foreignModule(project)

        ModuleRootModificationUtil.updateModel(myFixture.module) { model ->
            model.addModuleOrderEntry(foreign)
            model.addModuleOrderEntry(foreign)

            sync.removeDuplicateEntries(model, foreign.name)
        }

        val dependencies = ModuleRootManager.getInstance(myFixture.module).orderEntries
            .filterIsInstance<ModuleOrderEntry>()
            .filter { it.moduleName == foreign.name }

        assertEquals(
            "both entries belong to whoever created them, so both must survive",
            2,
            dependencies.size
        )
    }

    override fun tearDown() {
        try {
            ModuleRootModificationUtil.updateModel(myFixture.module) { model ->
                model.orderEntries
                    .filter { it is LibraryOrderEntry || it is ModuleOrderEntry }
                    .forEach(model::removeOrderEntry)
            }
        } finally {
            super.tearDown()
        }
    }
}
