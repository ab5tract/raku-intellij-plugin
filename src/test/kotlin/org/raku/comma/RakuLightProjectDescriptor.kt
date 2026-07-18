package org.raku.comma

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.util.io.FileUtil
import com.intellij.testFramework.LightProjectDescriptor
import org.raku.comma.module.RakuModuleType
import org.raku.comma.project.wizard.RakuModuleWizardBuilder
import org.raku.comma.utils.RakuProjectType

open class RakuLightProjectDescriptor : LightProjectDescriptor() {
    override fun getModuleTypeId(): String = RakuModuleType.ID

    override fun setUpProject(project: Project, handler: SetupHandler) {
        WriteAction.run<RuntimeException> {
            createRakuModule(
                project,
                handler,
                "Module::Outer",
                FileUtil.join(FileUtil.getTempDirectory(), "$TEST_MODULE_NAME.iml")
            )
        }
    }

    protected fun createRakuModule(
        project: Project,
        handler: SetupHandler,
        moduleName: String,
        moduleFilePath: String,
    ) {
        val builder = RakuModuleWizardBuilder()
        builder.ensureProjectType(RakuProjectType.RAKU_MODULE)
        builder.moduleFilePath = moduleFilePath
        builder.ensureModuleName(moduleName)

        val module = createModule(project, moduleFilePath)
        ModuleRootModificationUtil.updateModel(module) { model -> builder.setupRootModel(model) }
        handler.moduleCreated(module)

        ModuleRootManager.getInstance(module).sourceRoots
            .filter { it.name == "lib" }
            .forEach(handler::sourceRootCreated)
    }
}
