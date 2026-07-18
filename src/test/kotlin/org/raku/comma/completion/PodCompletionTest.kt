package org.raku.comma.completion

import com.intellij.codeInsight.completion.CompletionType
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType
import org.jetbrains.annotations.NotNull


class PodCompletionTest : CommaFixtureTestCase() {
    fun testPodCompletion() {
        testContains("=<caret>", "begin", "for", "head1", "finish")
        testContains("=begin\n=<caret>", "end")
        testContains("=begin h<caret>", "head1")
        testContains("=begin d<caret>", "defn")
        testContains("=for <caret>", "head1", "defn", "code")
        testDoesntContain("=for <caret>", "begin", "end", "finish")
    }

    private fun testContains(text: String, vararg contains: String) {
        val directives = getStrings(text)
        assertContainsElements(directives, *contains)
    }

    private fun testDoesntContain(text: String, vararg contains: String) {
        val directives = getStrings(text)
        assertDoesntContain(directives, *contains)
    }

    @NotNull
    private fun getStrings(text: String): List<String> {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, text)
        myFixture.complete(CompletionType.BASIC, 1)
        val directives = myFixture.getLookupElementStrings()!!
        assertNotNull(directives)
        return directives
    }
}
