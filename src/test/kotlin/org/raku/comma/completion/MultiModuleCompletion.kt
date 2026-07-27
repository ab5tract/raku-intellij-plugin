package org.raku.comma.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.LightProjectDescriptor
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.RakuMultiModuleProjectDescriptor


class MultiModuleCompletion : CommaFixtureTestCase() {
    override fun getProjectDescriptor(): LightProjectDescriptor {
        return RakuMultiModuleProjectDescriptor
    }

    override fun getTestDataPath(): String {
        return "testData/multi-module"
    }

    fun testCrossModules() {
        myFixture.copyFileToProject("Module/Inner.pm6", "../lib/Module/Inner.pm6")
        myFixture.configureByText("10-test.t", "use Module::Inner; Foo.mm<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val methodsFromAnotherModule = myFixture.getLookupElementStrings()!!
        assertContainsElements(methodsFromAnotherModule!!, ".mmmm", ".mmmmmmmm")
    }
}
