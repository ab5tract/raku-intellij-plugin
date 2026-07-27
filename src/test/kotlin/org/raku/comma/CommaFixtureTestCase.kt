package org.raku.comma

import com.intellij.openapi.components.service
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.LoggedErrorProcessor
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ThrowableRunnable
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

    // Like checkResultByFile, but when -Draku.test.dump.actual=true also writes
    // the actual editor contents next to build/actual-dumps/ so behavior drift
    // in golden-file tests can be reviewed (the platform's overwrite flag does
    // not cover fixture-based comparisons).
    protected fun checkResultByFileDumpable(expectedFile: String, ignoreTrailingWhitespace: Boolean = true) {
        try {
            myFixture.checkResultByFile(expectedFile, ignoreTrailingWhitespace)
        } catch (error: Throwable) {
            if (java.lang.Boolean.getBoolean("raku.test.dump.actual")) {
                val dump = File("build/actual-dumps/${testDataPath.removePrefix("testData/")}/$expectedFile")
                dump.parentFile.mkdirs()
                val text = myFixture.editor.document.text
                val offset = myFixture.editor.caretModel.offset
                dump.writeText(text.substring(0, offset) + "<caret>" + text.substring(offset))
            }
            throw error
        }
    }

    private var highlightCheckIndex = 0

    // With -Draku.test.dump.actual=true, prints the source annotated with the
    // ACTUAL highlighting before every check (sequence-indexed per test) and
    // swallows comparison failures so later checks in the test still dump.
    protected fun checkHighlightingDumpable() {
        val dumping = java.lang.Boolean.getBoolean("raku.test.dump.actual")
        if (!dumping) {
            myFixture.checkHighlighting()
            return
        }
        // Run the check FIRST: it strips the expectation markup out of the
        // document, so the post-check dump reflects real source offsets.
        var failure: Throwable? = null
        try {
            myFixture.checkHighlighting()
        } catch (error: Throwable) {
            failure = error
        }
        val text = myFixture.editor.document.text
        data class Mark(val offset: Int, val closing: Boolean, val order: Int, val tag: String)
        val marks = ArrayList<Mark>()
        for ((n, hi) in myFixture.doHighlighting().withIndex()) {
            val tag = when (hi.severity) {
                com.intellij.lang.annotation.HighlightSeverity.ERROR -> "error"
                com.intellij.lang.annotation.HighlightSeverity.WARNING -> "warning"
                com.intellij.lang.annotation.HighlightSeverity.WEAK_WARNING -> "weak_warning"
                else -> continue
            }
            val descr = hi.description ?: continue
            marks += Mark(hi.startOffset, false, n, "<$tag descr=\"$descr\">")
            marks += Mark(hi.endOffset, true, -n, "</$tag>")
        }
        val builder = StringBuilder(text)
        // Insert from the end; at equal offsets closing tags go after
        // opening ones in the final text (they are inserted first).
        marks.sortedWith(compareByDescending<Mark> { it.offset }
                             .thenBy { it.closing }
                             .thenBy { it.order })
            .forEach { builder.insert(it.offset, it.tag) }
        println("HIGHLIGHT-ACTUAL ${getTestName(false).trim()}#${highlightCheckIndex} <<<$builder>>>")
        if (failure != null) {
            println("HIGHLIGHT-FAILED ${getTestName(false).trim()}#${highlightCheckIndex}")
        }
        highlightCheckIndex++
    }

    // The bundled Kotlin plugin's CodeVision initialization can fail with a
    // PluginException from KotlinScriptDefinitionCodeVisionProvider --
    // 'hints.codevision.script.definition.name' actually exists in the
    // plugin's own properties file (verified by inspecting the jar), so this
    // is a resource-bundle *lookup* failure in this specific
    // test-sandbox/IDE-build combination, not a genuinely missing
    // translation upstream. In real usage that failure is just a swallowed
    // Logger.error(); -Dintellij.testFramework.rethrow.logged.errors=true
    // (set for all test JVMs here) escalates it to a hard test failure.
    //
    // It's fired from a background coroutine (CodeVisionInitializer,
    // launched on project startup) rather than synchronously from
    // checkHighlighting() itself, and only for whichever test first touches
    // a "real" project/file in the whole run -- confirmed by testing that
    // wrapping just checkHighlighting(), and then just runTestRunnable()
    // (the whole test method body), both still let it slip through when it
    // triggers during setUp() instead. runBare() is the outermost
    // overridable hook (UsefulTestCase) spanning setUp -> test body ->
    // tearDown, so wrapping there is the only reliable way to catch it
    // regardless of when the coroutine's Logger.error() actually lands.
    // Scope-suppress only this known message so a real regression elsewhere
    // still fails loudly.
    override fun runBare(testRunnable: ThrowableRunnable<Throwable>) {
        LoggedErrorProcessor.executeWith<Throwable>(object : LoggedErrorProcessor() {
            override fun processError(
                category: String,
                message: String,
                details: Array<String>,
                t: Throwable?
            ): MutableSet<Action> {
                return if (message.contains("KotlinScriptDefinitionCodeVisionProvider"))
                    Action.NONE
                else
                    super.processError(category, message, details, t)
            }
        }) {
            super.runBare(testRunnable)
        }
    }

    companion object {
        private const val LOAD_TIMEOUT_MS = 120_000L
    }
}
