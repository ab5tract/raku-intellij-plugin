package org.raku.comma

import com.intellij.openapi.components.service
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.raku.comma.sdk.RakuSdkUtil
import org.raku.comma.services.project.RakuProjectSdkService
import java.io.File

abstract class CommaFixtureTestCase : BasePlatformTestCase() {
    override fun getProjectDescriptor(): LightProjectDescriptor = RakuLightProjectDescriptor()

    protected val sdkService: RakuProjectSdkService
        get() = project.service<RakuProjectSdkService>()

    override fun setUp() {
        super.setUp()
        val home = suggestSdkHome()
        assertNotNull("Found a raku in path to use in tests", home)
        sdkService.setProjectSdkPath(home!!)
        ensureSetting()
    }

    private fun suggestSdkHome(): String? {
        return System.getenv("PATH")
            .split(File.pathSeparator)
            .firstOrNull { it.isNotEmpty() && RakuSdkUtil.isValidRakuSdkHome(it) }
    }

    protected fun ensureModuleIsLoaded(moduleName: String, invocation: String = "use") {
        awaitSymbols("module $moduleName") {
            sdkService.symbolCache.getPsiFileForModule(moduleName, "$invocation $moduleName")
        }
    }

    private fun ensureSetting() {
        awaitSymbols("CORE.setting") { sdkService.symbolCache.getCoreSettingFile() }
    }

    // Symbol loading kicks off a raku subprocess in the background and hands
    // back a DUMMY placeholder file until its output has been digested.
    private fun awaitSymbols(what: String, provider: () -> com.intellij.psi.PsiFile?) {
        val deadline = System.currentTimeMillis() + LOAD_TIMEOUT_MS
        var file = provider()
        while (file == null || file.name == "DUMMY") {
            if (System.currentTimeMillis() > deadline) {
                fail("Timed out waiting for $what symbols to load")
            }
            Thread.sleep(100)
            file = provider()
        }
    }

    companion object {
        private const val LOAD_TIMEOUT_MS = 120_000L
    }
}
