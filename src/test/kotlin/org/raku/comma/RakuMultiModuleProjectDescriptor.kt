package org.raku.comma

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.util.io.FileUtil

class RakuMultiModuleProjectDescriptor : RakuLightProjectDescriptor() {
    override fun setUpProject(project: Project, handler: SetupHandler) {
        super.setUpProject(project, handler)
        WriteAction.run<RuntimeException> {
            createRakuModule(
                project, handler, "Module::Inner",
                FileUtil.join(FileUtil.getTempDirectory(), "Inner", "${TEST_MODULE_NAME}_inner.iml")
            )
            val modules = ModuleManager.getInstance(project).modules
            ModuleRootModificationUtil.addDependency(modules[0], modules[1])
        }
    }
}
