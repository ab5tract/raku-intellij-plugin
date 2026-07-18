package org.raku.comma.cro.completion

import com.intellij.codeInsight.completion.CompletionType
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.cro.template.CroTemplateFileType

import java.util.Arrays
import java.util.List

class CroTemplateCompletionTest : CommaFixtureTestCase() {
    fun testCompletionOfSubArguments() {
        myFixture.configureByText(CroTemplateFileType.INSTANCE,
            "<:sub foo(\$v1, \$v2, \$v3)> <\$<caret> </:>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()
        assertNotNull(vars)
        assertSize(3, vars!!)
        assertContainsElements(vars!!, Arrays.asList("\$v1", "\$v2", "\$v3"))
    }

    fun testCompletionOfMacroArguments() {
        myFixture.configureByText(CroTemplateFileType.INSTANCE,
          "<:macro foo(\$v1, \$v2)> <\$<caret> </:>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()
        assertNotNull(vars)
        assertSize(2, vars!!)
        assertContainsElements(vars!!, Arrays.asList("\$v1", "\$v2"))
    }

    fun testCompletionOfIterationVariable() {
        myFixture.configureByText(CroTemplateFileType.INSTANCE,
          "<@foo : \$item> <@bar : \$another> <\$<caret> </@> </@>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()
        assertNotNull(vars)
        assertSize(2, vars!!)
        assertContainsElements(vars!!, Arrays.asList("\$item", "\$another"))
    }

    fun testCompletionOfSub() {
        myFixture.configureByText(CroTemplateFileType.INSTANCE,
          "<:sub s1()>abc</:> <:sub s2(\$x, \$y)>def</:> <&<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()
        assertNotNull(vars)
        assertSize(2, vars!!)
        assertContainsElements(vars!!, Arrays.asList("s1", "s2"))
    }

    fun testCompletionOfMacro() {
        myFixture.configureByText(CroTemplateFileType.INSTANCE,
          "<:macro m1()>abc</:> <:macro m2(\$x, \$y)>def</:> <|<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()
        assertNotNull(vars)
        assertSize(2, vars!!)
        assertContainsElements(vars!!, Arrays.asList("m1", "m2"))
    }

    fun testCompletionOfSubFromOtherFile() {
        myFixture.configureByText("stuff.crotmp", "<:sub s3()>abc</:> <:sub s4(\$x, \$y)>def</:>")
        myFixture.configureByText(CroTemplateFileType.INSTANCE,
                "<:use 'stuff.crotmp'> <&<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()
        assertNotNull(vars)
        assertSize(2, vars!!)
        assertContainsElements(vars!!, Arrays.asList("s3", "s4"))
    }

    fun testCompletionOfMacroFromOtherFile() {
        myFixture.configureByText("stuff.crotmp", "<:macro m3()>abc</:> <:macro m4(\$x, \$y)>def</:>")
        myFixture.configureByText(CroTemplateFileType.INSTANCE,
                "<:use 'stuff.crotmp'> <|<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()
        assertNotNull(vars)
        assertSize(2, vars!!)
        assertContainsElements(vars!!, Arrays.asList("m3", "m4"))
    }
}
