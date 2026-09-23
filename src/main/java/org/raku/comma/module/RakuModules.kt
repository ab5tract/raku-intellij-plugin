package org.raku.comma.module

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ExternalProjectSystemRegistry

/**
 * Which modules this plugin may touch.
 *
 * A Raku file somewhere in a project is not a licence to rewrite that project's
 * module model. Gradle, Maven and the rest own the modules they generate --
 * content roots, SDK and, above all, the compile classpath -- and they rebuild
 * them from the build script on every sync, so anything we add there is either
 * discarded or, worse, survives as a change nobody asked for.
 */
object RakuModules {

    // Compared by id rather than via RakuModuleType.instance: findByID hands back
    // an UnknownModuleType when the type is not registered, and asking `is` about
    // that is a ClassCastException rather than a false.
    fun isRakuModule(module: Module): Boolean = ModuleType.get(module).id == RakuModuleType.ID

    fun isOwnedByAnotherBuildSystem(module: Module): Boolean =
        ExternalProjectSystemRegistry.getInstance().getExternalSource(module) != null

    /**
     * The single module the plugin manages, or null when the project has none to
     * offer.
     *
     * A Raku module is unambiguously ours -- the wizard and the project open
     * processor both create one. Failing that we fall back to the project's first
     * module, as this has always done, but only while no other build system has
     * claimed it.
     */
    fun managedModule(project: Project): Module? {
        val modules = ModuleManager.getInstance(project).modules
        return modules.firstOrNull(::isRakuModule)
            ?: modules.firstOrNull()?.takeUnless(::isOwnedByAnotherBuildSystem)
    }
}
