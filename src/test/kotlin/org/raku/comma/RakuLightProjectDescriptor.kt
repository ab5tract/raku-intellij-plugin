package org.raku.comma

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.testFramework.LightProjectDescriptor
import org.raku.comma.module.RakuModuleType
import org.raku.comma.project.wizard.RakuModuleWizardBuilder
import org.raku.comma.utils.RakuProjectType
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

open class RakuLightProjectDescriptor : LightProjectDescriptor() {
    /** Distinguishes this descriptor class's on-disk layout from its siblings'. */
    protected open val baseDirPrefix: String get() = "raku-light-project"

    /**
     * Where this descriptor's module and project files live.
     *
     * Deliberately *not* `FileUtil.getTempDirectory()`: `UsefulTestCase.setUp`
     * redirects that to a fresh `/tmp/unitTest_<testName>_*` per test and
     * deletes it again in `tearDown`. Since the light project is now reused
     * across tests (see [INSTANCE]), rooting the module there leaves the second
     * test pointing at a deleted content root, and every `configureByText`
     * fails with `InvalidVirtualFileAccessException`. The redirect only touches
     * FileUtil's cache, not the `java.io.tmpdir` property, so anchoring here
     * gives a directory that outlives any individual test.
     */
    protected val baseDir: Path by lazy { createStableBaseDir(baseDirPrefix) }

    override fun generateProjectPath(): Path = baseDir.resolve("light_temp.ipr")

    override fun getModuleTypeId(): String = RakuModuleType.ID

    override fun setUpProject(project: Project, handler: SetupHandler) {
        WriteAction.run<RuntimeException> {
            createRakuModule(
                project,
                handler,
                "Module::Outer",
                baseDir.resolve("$TEST_MODULE_NAME.iml").toString()
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

    companion object {
        // LightProjectDescriptor declares no equals/hashCode, so
        // LightPlatformTestCase.doSetup's "reuse the project unless the
        // descriptor changed" check is an identity comparison. Handing back a
        // fresh instance per test therefore tears down and rebuilds the light
        // project every single time -- and with it RakuProjectSdkService
        // (@Service(PROJECT)), whose per-instance settingsStarted/settingJson
        // means another 4s `raku raku-core-symbols.raku` subprocess plus a ~3MB
        // JSON re-parse. Every test must hand back this one instance.
        val INSTANCE: RakuLightProjectDescriptor = RakuLightProjectDescriptor()

        internal fun createStableBaseDir(prefix: String): Path =
            Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "$prefix-")
                .also { dir ->
                    Runtime.getRuntime().addShutdownHook(Thread { dir.toFile().deleteRecursively() })
                }
    }
}
