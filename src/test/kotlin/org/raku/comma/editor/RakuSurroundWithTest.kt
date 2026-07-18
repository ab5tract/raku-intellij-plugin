package org.raku.comma.editor

import com.intellij.codeInsight.generation.surroundWith.SurroundWithHandler
import com.intellij.lang.LanguageSurrounders
import com.intellij.lang.surroundWith.Surrounder
import com.intellij.psi.PsiElement
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.RakuLanguage
import org.raku.comma.descriptors.RakuRegexGroupSurrounder
import org.raku.comma.descriptors.RakuRegexNamedSurrounder
import org.raku.comma.descriptors.RakuRegexPositionalSurrounder
import org.raku.comma.descriptors.surrounder.RakuArrayContextSurrounder
import org.raku.comma.descriptors.surrounder.RakuArraySurrounder
import org.raku.comma.descriptors.surrounder.RakuForSurrounder
import org.raku.comma.descriptors.surrounder.RakuGivenSurrounder
import org.raku.comma.descriptors.surrounder.RakuHashContextSurrounder
import org.raku.comma.descriptors.surrounder.RakuHashSurrounder
import org.raku.comma.descriptors.surrounder.RakuIfSurrounder
import org.raku.comma.descriptors.surrounder.RakuPointyBlockSurrounder
import org.raku.comma.descriptors.surrounder.RakuStartSurrounder
import org.raku.comma.descriptors.surrounder.RakuTryCatchDefaultSurrounder
import org.raku.comma.descriptors.surrounder.RakuTryCatchWhenSurrounder
import org.raku.comma.descriptors.surrounder.RakuTrySurrounder
import org.raku.comma.descriptors.surrounder.RakuUnlessSurrounder
import org.raku.comma.descriptors.surrounder.RakuWheneverSurrounder
import org.raku.comma.descriptors.surrounder.RakuWhenSurrounder
import org.raku.comma.descriptors.surrounder.RakuWithoutSurrounder
import org.raku.comma.descriptors.surrounder.RakuWithSurrounder

class RakuSurroundWithTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/surroundWith"
    }

    fun testIfSurround() = doTest(RakuIfSurrounder(true))
    fun testWithSurround() = doTest(RakuWithSurrounder(true))
    fun testUnlessSurround() = doTest(RakuUnlessSurrounder(true))
    fun testWithoutSurround() = doTest(RakuWithoutSurrounder(true))
    fun testGivenSurround() = doTest(RakuGivenSurrounder(true))
    fun testForSurround() = doTest(RakuForSurrounder(true))
    fun testWheneverSurround() = doTest(RakuWheneverSurrounder(true))
    fun testWhenSurround() = doTest(RakuWhenSurrounder(true))
    fun testTrySurround() = doTest(RakuTrySurrounder(true))
    fun testTryWhenSurround() = doTest(RakuTryCatchWhenSurrounder(true))
    fun testTryDefaultSurround() = doTest(RakuTryCatchDefaultSurrounder(true))
    fun testStartSurround() = doTest(RakuStartSurrounder(true))
    fun testPointyBlockSurround() = doTest(RakuPointyBlockSurrounder(true))
    fun testHashComposerSurround() = doTest(RakuHashSurrounder(true))
    fun testArrayComposerSurround() = doTest(RakuArraySurrounder(true))
    fun testArrayContextualizerSurround() = doTest(RakuArrayContextSurrounder(true))
    fun testHashContextualizerSurround() = doTest(RakuHashContextSurrounder(true))
    fun testIfExpr() = doTest(RakuIfSurrounder(false))
    fun testUnlessExpr() = doTest(RakuUnlessSurrounder(false))
    fun testTryExpr() = doTest(RakuTrySurrounder(false))
    fun testStartExpr() = doTest(RakuStartSurrounder(false))
    fun testPointyBlockExpr() = doTest(RakuPointyBlockSurrounder(false))
    fun testHashContextExpr() = doTest(RakuHashContextSurrounder(false))
    fun testRegexGroup() = doTest(RakuRegexGroupSurrounder())
    fun testRegexPositional() = doTest(RakuRegexPositionalSurrounder())
    fun testRegexNamed() = doTest(RakuRegexNamedSurrounder())

    private fun doTest(surrounder: Surrounder) {
        myFixture.configureByFile(getTestName(true) + "Before.p6")
        val descriptors = LanguageSurrounders.INSTANCE.allForLanguage(RakuLanguage.INSTANCE)
        val selectionModel = myFixture.editor.selectionModel
        var elements: Array<PsiElement>? = null
        for (descriptor in descriptors) {
            elements = descriptor.getElementsToSurround(
                myFixture.file, selectionModel.selectionStart, selectionModel.selectionEnd)
            if (elements.isNotEmpty()) break
        }
        assertNotNull(elements)
        assertFalse(elements!!.isEmpty())
        assertTrue(surrounder.isApplicable(elements))
        SurroundWithHandler.invoke(project, myFixture.editor, myFixture.file, surrounder)
        myFixture.checkResultByFile(getTestName(true) + ".p6")
    }
}
