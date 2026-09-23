package org.raku.comma.docs

import org.raku.comma.CommaFixtureTestCase

/**
 * The provider's language guard is the code path the live plugin.xml
 * registration actually invokes -- psiTargetProvider is not language-filtered
 * by the platform, so RakuPsiDocumentationTargetProvider.documentationTarget
 * is called for elements of every language and must reject non-Raku ones
 * itself. Every other test in this package constructs RakuDocumentationTarget
 * directly and never goes through the provider, so that guard was otherwise
 * untested.
 */
class RakuPsiDocumentationTargetProviderTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String = "testData/docs"

    private val provider = RakuPsiDocumentationTargetProvider()

    fun testRakuElementYieldsATarget() {
        myFixture.configureByFile("quickDocsClass.p6")
        val target = provider.documentationTarget(myFixture.elementAtCaret, null)
        assertNotNull("a Raku element must produce a documentation target", target)
        assertTrue(target is RakuDocumentationTarget)
    }

    fun testNonRakuElementYieldsNull() {
        val file = myFixture.configureByText("plain.txt", "hello world")
        assertNull(provider.documentationTarget(file, null))
    }
}
