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

    // computeDocumentationBlocking dereferences the pointer before computing,
    // so this covers createPointer() as well as the content. External elements
    // are synthesised from the symbol cache and have no file for a smart
    // pointer to track, which is why they use a hard pointer.
    fun testTargetSurvivesItsOwnPointer() {
        val target = RakuDocumentationTarget(elementAt("methodExternalFromCORE"), null)
        val data = com.intellij.platform.backend.documentation.impl.computeDocumentationBlocking(
            target.createPointer())
        assertNotNull("the pointer must still dereference to a live target", data)
        assertTrue(data!!.html!!.contains("Throws X::Cannot::Capture"))
    }

    fun testTargetHintMatchesTheRenderedLine() {
        val element = elementAt("methodExternalFromCORE")
        assertEquals(RakuDocRendering.hintLine(element),
                     RakuDocumentationTarget(element, null).computeDocumentationHint())
    }

    fun testTargetPresentationCarriesAllFourSlots() {
        val presentation = RakuDocumentationTarget(elementAt("methodExternalFromCORE"), null)
            .computePresentation()
        assertEquals("Capture(--&gt; Mu)", presentation.presentableText)
        assertEquals("Int", presentation.containerText)
        assertEquals("CORE.setting", presentation.locationText)
        assertNotNull("a Raku symbol should carry the Camelia icon", presentation.icon)
    }

    // computeDocumentation()'s externalUrl(...) call sits on an immutable
    // builder just like TargetPresentationBuilder does -- discarding its
    // return value silently drops the URL, and nothing else exercises that
    // wiring: DocumentationTest.testURL asserts against
    // RakuDocRendering.externalUrl(...) directly and never touches the
    // target. DocumentationData's link data is only reachable via its
    // Kotlin-internal accessor, hence the reflection.
    fun testTargetWiresTheExternalUrl() {
        val target = RakuDocumentationTarget(elementAt("methodExternalFromCORE"), null)
        val data = com.intellij.platform.backend.documentation.impl.computeDocumentationBlocking(
            target.createPointer())
        assertNotNull("the pointer must still dereference to a live target", data)
        val linksMethod = data!!.javaClass.getMethod("getLinks\$intellij_platform_lang_impl")
        val links = linksMethod.invoke(data)
        assertNotNull("computeDocumentation must produce link data", links)
        val externalUrlMethod = links!!.javaClass.getMethod("getExternalUrl")
        assertEquals("https://docs.raku.org/routine/Capture", externalUrlMethod.invoke(links))
    }

    // containerText's owner-resolution branch is otherwise untested: Task 2
    // only pinned the null case. This is the slot where a wrong answer is
    // plausible, because a CORE method's owner is reached by hopping to an
    // ExternalRakuPackageDecl rather than a real PSI package.
    //
    // Expected owner is "Int", not "Mu": the fixture is `Int.Capture`, and
    // Int.rakumod declares its own `method Capture() { X::Cannot::Capture...
    // .throw }` rather than inheriting Mu's. The reference resolves to the
    // nearest declaration in the MRO, so its owner is Int.
    fun testCoreMethodIsOwnedByItsType() {
        val owner = RakuDocRendering.containerText(elementAt("methodExternalFromCORE"))
        assertNotNull("a CORE method must report the type that owns it", owner)
        assertEquals("Int", owner)
    }
}
