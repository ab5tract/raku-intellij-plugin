package org.raku.comma.inspection

import com.intellij.openapi.components.service
import junit.framework.TestCase
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.inspection.inspections.UndeclaredOrDeprecatedRoutineInspection
import org.raku.comma.services.project.RakuProjectDetailsService
import org.raku.comma.utils.CommaProjectUtil

// The Rakudo-core exemptions (nqp:: ops without `use nqp`, sigspace notices)
// were project-level only, so browsing Rakudo sources from any OTHER project
// (the common case: the checkout is opened as loose files or from a parent
// directory) still flagged them. Detection now also recognizes a FILE that
// lives inside a Rakudo source tree by its path.
class RakudoCorePathTest : TestCase() {

    fun testRakudoSrcPathsRecognized() {
        assertTrue(CommaProjectUtil.isRakudoCorePath("/home/u/code/raku/x.core/rakudo/src/Raku/Grammar.nqp"))
        assertTrue(CommaProjectUtil.isRakudoCorePath("/home/u/rakudo/src/core.c/Date.rakumod"))
    }

    fun testOrdinaryPathsNotRecognized() {
        assertFalse(CommaProjectUtil.isRakudoCorePath("/home/u/code/my-app/lib/Foo.rakumod"))
        assertFalse(CommaProjectUtil.isRakudoCorePath("/home/u/rakudo-tools/src/Foo.rakumod"))
        assertFalse(CommaProjectUtil.isRakudoCorePath(null))
    }
}

class NqpOpRakudoCoreTest : CommaFixtureTestCase() {

    private fun nqpWarnings(code: String): List<String> {
        myFixture.enableInspections(UndeclaredOrDeprecatedRoutineInspection())
        myFixture.configureByText(RakuScriptFileType.INSTANCE, code)
        return myFixture.doHighlighting()
            .mapNotNull { it.description }
            .filter { it.contains("nqp::") }
    }

    private fun setRakudoCore(value: Boolean) {
        project.service<RakuProjectDetailsService>().projectState.isProjectRakudoCore = value
    }

    override fun tearDown() {
        try {
            setRakudoCore(false)
        } finally {
            super.tearDown()
        }
    }

    fun testNqpOpFlaggedInOrdinaryProject() {
        setRakudoCore(false)
        assertTrue(nqpWarnings("my \$c := nqp::getlex('\$x');\n").isNotEmpty())
    }

    fun testNqpOpExemptInRakudoCoreProject() {
        setRakudoCore(true)
        assertTrue(nqpWarnings("my \$c := nqp::getlex('\$x');\n").isEmpty())
    }
}
