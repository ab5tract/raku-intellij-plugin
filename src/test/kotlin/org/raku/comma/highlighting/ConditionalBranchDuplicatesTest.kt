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

    fun testExclusiveBranchTwinsAreNeverPairedAgainstEachOther() {
        // Pins markDuplicateValue's pairwise gap (class/subset path, not the sub path
        // covered above): an unconditional decl coexists with BOTH the #?if jvm and
        // #?if js twins, so unconditional-vs-branch collisions are real duplicates
        // under either backend and must still flag. But jvm and js can never both be
        // true, so the two branch twins must never be reported against EACH OTHER.
        // Line 1 is the unconditional decl, line 3 is the jvm twin, line 6 is the js
        // twin; every "Re-declaration of ... from aaa.raku:N" must cite line 1, never
        // line 3 or line 6 (which would mean a branch twin was used as the "original"
        // side of a report against the other branch twin).
        val errors = errorTexts(
            "class Foo {}\n#?if jvm\nclass Foo {}\n#?endif\n#?if js\nclass Foo {}\n#?endif\n"
        )
        assertTrue("expected unconditional-vs-branch collisions to still flag: $errors", errors.isNotEmpty())
        for (error in errors) {
            assertTrue(
                "duplicate report should cite the unconditional decl (line 1), not a branch twin: $error",
                error.contains("aaa.raku:1")
            )
        }
    }
}
