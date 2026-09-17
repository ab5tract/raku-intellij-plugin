package org.raku.comma.structureView

import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.cro.CroTemplateCall
import org.raku.comma.extensions.RakuFrameworkCall
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.psi.RakuFile
import org.raku.comma.psi.RakuSubCall

// Regression test for a shipped-plugin breakage: plugin.xml once registered
// the frameworkCall extension under a mangled class name
// (cro.org.raku.comma.CroTemplateCall instead of org.raku.comma.cro
// .CroTemplateCall), so opening the structure view threw PluginException/
// ClassNotFoundException from RakuStructureViewElement.getChildren(). These
// tests instantiate the registered extensions and walk the same code path.
class FrameworkCallStructureViewTest : CommaFixtureTestCase() {

    fun testFrameworkCallExtensionsInstantiate() {
        val extensions = RakuFrameworkCall.EP_NAME.extensions
        assertTrue("plugin.xml must register an instantiable CroTemplateCall frameworkCall extension",
                   extensions.any { it is CroTemplateCall })
    }

    fun testStructureViewCollectsCroTemplateCall() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "template-part 'header';\n")
        val children = RakuStructureViewElement(myFixture.file as RakuFile).children
        assertTrue("structure view should list the template-part framework call",
                   children.any { it is RakuStructureViewElement && it.value is RakuSubCall })
    }
}
