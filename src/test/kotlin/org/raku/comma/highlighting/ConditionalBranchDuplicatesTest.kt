package org.raku.comma.highlighting

import com.intellij.lang.annotation.HighlightSeverity
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

class ConditionalBranchDuplicatesTest : CommaFixtureTestCase() {

    private fun errorTexts(code: String): List<String> {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, code)
        return myFixture.doHighlighting()
            .filter { it.severity == HighlightSeverity.ERROR }
            .map { it.description ?: "" }
    }

    fun testCrossBranchTwinsDoNotFlag() {
        val errors = errorTexts(
            "#?if jvm\nsub backend-name() { \"jvm\" }\n#?endif\n" +
            "#?if !jvm\nsub backend-name() { \"not jvm\" }\n#?endif\n"
        )
        assertTrue("cross-branch twins flagged: $errors", errors.isEmpty())
    }

    fun testDifferentBackendsDoNotFlag() {
        val errors = errorTexts(
            "#?if jvm\nsub backend-name() { \"jvm\" }\n#?endif\n" +
            "#?if js\nsub backend-name() { \"js\" }\n#?endif\n"
        )
        assertTrue("jvm-vs-js twins flagged: $errors", errors.isEmpty())
    }

    fun testSameBranchDuplicatesStillFlag() {
        val errors = errorTexts(
            "#?if jvm\nsub backend-name() { 1 }\nsub backend-name() { 2 }\n#?endif\n"
        )
        assertTrue("same-branch duplicate NOT flagged", errors.isNotEmpty())
    }

    fun testPlainDuplicatesStillFlag() {
        val errors = errorTexts("sub twice() { 1 }\nsub twice() { 2 }\n")
        assertTrue("plain duplicate NOT flagged", errors.isNotEmpty())
    }

    fun testActiveCodeVsCompatibleBranchStillFlags() {
        // !js is live under moar, so a shared decl genuinely collides with it.
        val errors = errorTexts(
            "sub twice() { 1 }\n#?if !js\nsub twice() { 2 }\n#?endif\n"
        )
        assertTrue("shared-vs-active-branch duplicate NOT flagged", errors.isNotEmpty())
    }
}
