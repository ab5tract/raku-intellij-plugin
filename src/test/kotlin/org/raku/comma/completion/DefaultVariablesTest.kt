package org.raku.comma.completion

import com.intellij.codeInsight.completion.CompletionType
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType
import org.jetbrains.annotations.Nullable

import java.util.Arrays
import java.util.Collections

class DefaultVariablesTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/codeInsight/defaultVariables"
    }

    fun testCompletion() {
        doFileTest("DefaultTestData1.pm6",
                   Arrays.asList("\$_", "\$/", "\$!", "\$=pod", "\$?FILE", "\$?LANG", "\$?LINE", "\$?PACKAGE"),
                   Collections.singletonList("\$=finish"))
    }

    fun testCompletionResources() {
        doFileTest("DefaultTestData2.pm6",
                   Arrays.asList("%?RESOURCES", "%hash"),
                   Collections.emptyList())
    }

    fun testCompletionInClass() {
        doFileTest("DefaultTestData3.pm6",
                   Arrays.asList("\$?CLASS", "\$?PACKAGE"),
                   Collections.singletonList("\$?ROLE"))
    }

    fun testCompletionInRole() {
        doFileTest("DefaultTestData4.pm6", Arrays.asList("\$?CLASS", "\$?PACKAGE", "\$?ROLE"), Collections.emptyList())
    }

    fun testCompletionInGrammar() {
        doFileTest("DefaultTestData5.pm6",
                   Arrays.asList("\$?CLASS", "\$?PACKAGE"),
                   Collections.singletonList("\$?ROLE"))
    }

    fun testCompletionInBlock() {
        doTextTest("my \$x = { &?<caret>",
                   Collections.singletonList("&?BLOCK"),
                   Collections.singletonList("&?ROUTINE"))
    }

    fun testCompletionInPointyBlock() {
        doTextTest("my \$x = -> \$y { &?<caret>",
                   Collections.singletonList("&?BLOCK"),
                   Collections.singletonList("&?ROUTINE"))
    }

    fun testCompletionInSub() {
        doTextTest("sub foo() { &?<caret>",
                   Arrays.asList("&?ROUTINE", "&?BLOCK"),
                   Collections.emptyList()
        )
    }

    fun testNamedArgsHashCompletionInMethod() {
        doTextTest("method foo() { %<caret>",
                   Collections.singletonList("%_"), Collections.emptyList())
    }

    fun testNamedArgsHashCompletionInSubmethod() {
        doTextTest("submethod foo() { %<caret>",
                   Collections.singletonList("%_"), Collections.emptyList())
    }

    fun testNamedArgsHashCompletionInSub() {
        doTextTest("sub foo() { %<caret>",
                   Collections.emptyList(),
                   Collections.singletonList("%_"))
    }

    fun testPodFinishCompletion() {
        doTextTest("say \$=<caret>\n\n=for finish\n\n",
                   Arrays.asList("\$=pod", "\$=finish"),
                   Collections.emptyList())
    }

    fun testPodFinishInBlockCompletion() {
        doTextTest("if True {\nsay \$=<caret>\n}\n\n=for finish\n\n",
                   Arrays.asList("\$=pod", "\$=finish"),
                   Collections.emptyList())
    }

    fun testMAINDynamic() {
        doTextTest("sub MAIN { say \$*<caret> }",
                   Arrays.asList("\$*USAGE", "\$*THREAD"),
                   Collections.emptyList())
    }

    fun testGenerateUsageDynamic() {
        doTextTest("sub GENERATE-USAGE { say &*<caret> }",
                   Collections.singletonList("&*GENERATE-USAGE"),
                   Collections.singletonList("&*ARGS-TO-CAPTURE"))
    }

    fun testArgsToCaptureDynamic() {
        doTextTest("sub ARGS-TO-CAPTURE { say &*<caret> }",
                   Collections.singletonList("&*ARGS-TO-CAPTURE"),
                   Collections.singletonList("&*GENERATE-USAGE"))
    }

    fun testDynamicVariables() {
        doTextTest("\$*<caret>",
                   Arrays.asList("\$*THREAD", "\$*USER", "\$*TZ", "\$*COLLATION"),
                   Collections.singletonList("\$*USAGE"))
    }

    private fun doTextTest(text: String, assertTrue: @Nullable List<String>, assertFalse: @Nullable List<String>) {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, text)
        doTest(assertTrue, assertFalse)
    }


    private fun doFileTest(filename: String, assertTrue: @Nullable List<String>, assertFalse: @Nullable List<String>) {
        myFixture.configureByFile(filename)
        doTest(assertTrue, assertFalse)
    }

    private fun doTest(assertTrue: @Nullable List<String>, assertFalse: @Nullable List<String>) {
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertTrue(vars.containsAll((assertTrue)))
        for (falsePositive in assertFalse)
            assertFalse(vars.contains(falsePositive))
    }
}
