package org.raku.comma

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootModificationUtil

// A Kotlin `object` for the same reason RakuLightProjectDescriptor keeps an
// INSTANCE: the light-project fixture reuses a project only while the
// descriptor stays identity-equal across tests.
object RakuMultiModuleProjectDescriptor : RakuLightProjectDescriptor() {
    override val baseDirPrefix: String get() = "raku-light-multi-module"

    override fun setUpProject(project: Project, handler: SetupHandler) {
        super.setUpProject(project, handler)
        WriteAction.run<RuntimeException> {
            createRakuModule(
                project, handler, "Module::Inner",
                baseDir.resolve("Inner").resolve("${TEST_MODULE_NAME}_inner.iml").toString()
            )
            val modules = ModuleManager.getInstance(project).modules
            ModuleRootModificationUtil.addDependency(modules[0], modules[1])
        }
    }
}
