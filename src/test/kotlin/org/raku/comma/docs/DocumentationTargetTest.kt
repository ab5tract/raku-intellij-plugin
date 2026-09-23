package org.raku.comma.docs

import org.raku.comma.CommaFixtureTestCase

class DocumentationTargetTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String = "testData/docs"

    private fun elementAt(fixture: String): com.intellij.psi.PsiElement {
        myFixture.configureByFile("$fixture.p6")
        return myFixture.elementAtCaret
    }

    // The icon and container carry the kind, so the keyword is redundant in a
    // narrow slot -- but only for routines. Stripping the first word of
    // "class Magician is Cool does Int" would leave a fragment.
    fun testRoutinePresentableTextDropsTheKeyword() {
        val element = elementAt("methodExternalFromCORE")
        assertEquals("method Capture(--&gt; Mu)", RakuDocRendering.hintLine(element))
        assertEquals("Capture(--&gt; Mu)", RakuDocRendering.presentableText(element))
    }

    fun testPackagePresentableTextIsUnchanged() {
        val element = elementAt("quickDocsClass")
        assertEquals("class Magician is Cool does Int", RakuDocRendering.presentableText(element))
    }

    // ProjectSdkSymbolCache.SETTING_FILE_NAME is the real value used to name
    // the synthetic file backing CORE's external symbols -- "SETTINGS.rakumod",
    // not the docs.raku.org-flavored "CORE.setting" one might guess.
    fun testCoreSymbolIsLocatedInTheSetting() {
        assertEquals("SETTINGS.rakumod", RakuDocRendering.locationText(elementAt("methodExternalFromCORE")))
    }

    fun testProjectSymbolIsLocatedInItsFile() {
        assertEquals("quickDocsClass.p6", RakuDocRendering.locationText(elementAt("quickDocsClass")))
    }

    // A top-level declaration has no owner; the slot stays empty rather than
    // inventing one.
    fun testTopLevelDeclarationHasNoContainer() {
        assertNull(RakuDocRendering.containerText(elementAt("quickDocsClass")))
    }
}
