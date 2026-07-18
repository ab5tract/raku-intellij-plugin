package org.raku.comma.traits

import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.psi.RakuPackageDecl
import org.raku.comma.psi.RakuTrait

import java.util.List

class TraitsTest : CommaFixtureTestCase() {
    fun testIsExportTraitData() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role Fo<caret>o is export {}")
        val usage = PsiTreeUtil.getParentOfType(myFixture.getFile().findElementAt(myFixture.getCaretOffset()), RakuPackageDecl::class.java)
        assertNotNull(usage)
        val traits = usage!!.getTraits()
        assertTrue(traits.size != 0)
        assertEquals("is", traits.get(0).getTraitModifier())
        assertEquals("export", traits.get(0).getTraitName())
    }

    fun testDoesTraitData() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role Fo<caret>o does Bar {}")
        val usage = PsiTreeUtil.getParentOfType(myFixture.getFile().findElementAt(myFixture.getCaretOffset()), RakuPackageDecl::class.java)
        assertNotNull(usage)
        val traits = usage!!.getTraits()
        assertTrue(traits.size != 0)
        assertEquals("does", traits.get(0).getTraitModifier())
        assertEquals("Bar", traits.get(0).getTraitName())
    }
}
