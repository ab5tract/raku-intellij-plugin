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

    // Deliberately the literal "CORE.setting", not ProjectSdkSymbolCache
    // .SETTING_FILE_NAME ("SETTINGS.rakumod"). That constant names the
    // synthetic virtual file backing CORE symbols -- an implementation
    // detail -- while this user-facing slot uses the name a Raku developer
    // and docs.raku.org actually use.
    fun testCoreSymbolIsLocatedInTheSetting() {
        assertEquals("CORE.setting", RakuDocRendering.locationText(elementAt("methodExternalFromCORE")))
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
