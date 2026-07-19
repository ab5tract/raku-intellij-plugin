package org.raku.comma.completion

import com.intellij.codeInsight.completion.CompletionType
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

import java.util.ArrayList
import java.util.Arrays
import java.util.HashSet

class SubCompletionTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/completion"
    }

    fun testCompletionFromLocal() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "sub foo() {}\nfo<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertContainsElements(vars, "foo")
        assertEquals(2, vars.toSet().size)
    }

    fun testCompletionFromOuter() {
        myFixture.configureByFiles("IdeaFoo/Bar8.pm6", "IdeaFoo/Baz.pm6")
        myFixture.complete(CompletionType.BASIC, 1)
        assertNull(myFixture.getLookupElementStrings())
    }

    fun testCompletionFromOurLocal() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our sub fooooo() {}\nfooo<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        assertNull(myFixture.getLookupElementStrings())
    }

    fun testCompletionFromCORE() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "se<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        if (java.lang.Boolean.getBoolean("raku.test.dump.actual")) println("LOOKUP-ACTUAL ${getTestName(false)} <<<${vars.sorted()}>>>")
        assertNotNull(vars)
        assertContainsElements(vars, Arrays.asList("sec", "sech", "set"))
        assertEquals(18, vars.toSet().size)
    }

    fun testCompletionFromImport() {
        ensureModuleIsLoaded("Test")
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "use Test;\nis-<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertContainsElements(vars, Arrays.asList("is-approx", "is-deeply", "isa-ok"))
        assertEquals(4, vars.toSet().size)
    }

    fun testAnonymousSubIsSafeToComplete() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "sub { ase<caret> }")
        myFixture.complete(CompletionType.BASIC, 1)
        val subs = myFixture.getLookupElementStrings()!!
        assertNotNull(subs)
        assertContainsElements(subs, "asec", "asech", "samecase")
    }

    fun testNqpComplete() {
        ensureModuleIsLoaded("nqp")
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "use nqp;\nnqp::ab<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val subs = myFixture.getLookupElementStrings()!!
        assertContainsElements(subs, Arrays.asList("nqp::abs_I", "nqp::abs_i", "nqp::abs_n"))
    }
}