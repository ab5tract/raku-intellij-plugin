package org.raku.comma.inspection

import com.intellij.lang.annotation.HighlightSeverity
import org.raku.comma.ALL_RAKU_INSPECTIONS
import org.raku.comma.CommaFixtureTestCase
import com.intellij.openapi.components.service
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.services.project.RakuProjectDetailsService

// Follow-ups from the Grammar.nqp gutter sweep:
//  - a lowercase lexical role/class used as a term (`stop.HOW.curry(...)`)
//    resolved as an undeclared subroutine -- bare argument-less names must
//    also try type/constant resolution;
//  - the not-progressing-quantifier heuristic treated a group with no
//    direct atom children (alternations, aliased assertions) as vacuously
//    zero-width, flagging `(\w | ':')+`-style regexes that always progress.
class GrammarNoiseFollowupsTest : CommaFixtureTestCase() {

    private fun warnings(code: String): List<String> {
        myFixture.enableInspections(*ALL_RAKU_INSPECTIONS)
        myFixture.configureByText(RakuScriptFileType.INSTANCE, code)
        return myFixture.doHighlighting()
            .filter { it.severity >= HighlightSeverity.WARNING && it.description != null }
            .map { it.description }
    }

    fun testLexicalRoleUsedAsTermIsNotUndeclaredSub() {
        val w = warnings(
            "my role stop[\$s] { method m() { \$s } };\n" +
            "my \$x := stop.HOW.curry(stop, 'x');\n"
        )
        assertTrue("role term flagged: $w", w.none { it.contains("Subroutine stop is not declared") })
    }

    fun testActuallyUndeclaredBareCallStillFlagged() {
        val w = warnings("my \$x := nosuchthing.HOW;\n")
        assertTrue("expected undeclared warning, got: $w", w.any { it.contains("Subroutine nosuchthing is not declared") })
    }

    fun testAlternationGroupQuantifierNotFlagged() {
        val w = warnings("my \$m = 'ab' ~~ /( \\w | ':' )+/;\n")
        assertTrue("alternation group flagged: $w", w.none { it.contains("may not progress") })
    }

    fun testAliasedAssertionGroupQuantifierNotFlagged() {
        val w = warnings("grammar G { token top { [ <name=.alpha> ]+ % ',' } }\n")
        assertTrue("aliased assertion group flagged: $w", w.none { it.contains("may not progress") })
    }

    private fun setRakudoCore(value: Boolean) {
        val details = project.service<RakuProjectDetailsService>()
        // getState()'s one-time refresh re-derives the flag from the project
        // name; mark the scan done first so the manual value sticks
        // regardless of test order.
        details.hasScannedForRakuFiles = true
        details.projectState.isProjectRakudoCore = value
    }

    override fun tearDown() {
        try {
            setRakudoCore(false)
        } finally {
            super.tearDown()
        }
    }

    // NQP-dialect constructs (bare no-listop names like `$v ?? a1 !! a0`,
    // NQP setting subs like `subst(...)`) are valid in Rakudo core but not
    // in plain Raku; undeclared-routine and META6-dependency checks judge
    // that dialect by Raku rules, so they are suppressed for core files.
    fun testUndeclaredSubSuppressedInRakudoCore() {
        setRakudoCore(true)
        val w = warnings("my \$x := subst('a', /a/, 'b');\n")
        assertTrue("core file flagged: $w", w.none { it.contains("is not declared") })
    }

    fun testUndeclaredVariableSuppressedInRakudoCore() {
        setRakudoCore(true)
        val w = warnings("say \$category;\n")
        assertTrue("core variable flagged: $w", w.none { it.contains("is not declared") })
    }

    fun testUnknownUseModuleSuppressedInRakudoCore() {
        setRakudoCore(true)
        val w = warnings("use Raku::Actions;\n")
        assertTrue("core use flagged: $w", w.none { it.contains("Cannot find") })
    }

    fun testGenuinelyEmptyableGroupStillFlagged() {
        val w = warnings("my \$m = 'ab' ~~ /[ \\w* ]+/;\n")
        assertTrue("expected may-not-progress warning, got: $w", w.any { it.contains("may not progress") })
    }
}
