package org.raku.comma.highlighting

import com.intellij.openapi.components.service
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.services.project.RakuProjectDetailsService

// The "Implicit <.ws> call" annotation on sigspace in rules is a real
// feature for ordinary projects, but Rakudo's own sources are grammar-heavy
// (every whitespace run in every rule lights up), so it is suppressed there
// via the existing isProjectRakudoCore switch — the same lever
// RakuHighlightVisitor already uses for duplicate checks.
class SigSpaceAnnotatorTest : CommaFixtureTestCase() {

    private val code = "grammar G {\n    rule r { 'a' 'b' }\n}\n"

    private fun implicitWsAnnotations(): Int {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, code)
        return myFixture.doHighlighting().count { it.description == "Implicit <.ws> call" }
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

    fun testSigspaceAnnotatedInOrdinaryProjects() {
        setRakudoCore(false)
        assertTrue("expected implicit-ws annotations in a rule", implicitWsAnnotations() > 0)
    }

    fun testSigspaceSuppressedInRakudoCore() {
        setRakudoCore(true)
        assertEquals("implicit-ws annotations must be suppressed in Rakudo core", 0, implicitWsAnnotations())
    }
}
