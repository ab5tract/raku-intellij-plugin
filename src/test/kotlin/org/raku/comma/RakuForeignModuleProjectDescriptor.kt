package org.raku.comma

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.EmptyModuleType
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project

// A Raku module sitting next to a module of some other kind -- the shape of every
// project where Raku code happens to live alongside another build system's
// sources, this plugin's own Gradle project being the one that prompted it.
object RakuForeignModuleProjectDescriptor : RakuLightProjectDescriptor() {
    override val baseDirPrefix: String get() = "raku-light-foreign-module"

    const val FOREIGN_MODULE_NAME = "foreign"

    override fun setUpProject(project: Project, handler: SetupHandler) {
        super.setUpProject(project, handler)
        WriteAction.run<RuntimeException> {
            val model = ModuleManager.getInstance(project).getModifiableModel()
            model.newModule(
                baseDir.resolve("$FOREIGN_MODULE_NAME.iml"),
                EmptyModuleType.EMPTY_MODULE
            )
            model.commit()
        }
    }

    fun foreignModule(project: Project): Module =
        ModuleManager.getInstance(project).findModuleByName(FOREIGN_MODULE_NAME)
            ?: error("the descriptor did not create the foreign module")
}
