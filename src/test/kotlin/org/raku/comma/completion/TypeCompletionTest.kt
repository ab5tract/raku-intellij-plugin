package org.raku.comma.completion

import com.intellij.codeInsight.completion.CompletionType
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

import java.util.Arrays

class TypeCompletionTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/completion"
    }

    fun testTypesFromSetting() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my In<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertContainsElements(vars, Arrays.asList("Instant", "Int"))
    }

    fun testMultipartTypesFromSetting() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my IO::<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertContainsElements(vars, Arrays.asList("IO::Path", "IO::Handle"))
    }

    fun testSanityNoNativeCallWithoutImport() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say NativeCal<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertEmpty(vars)
    }

    fun testUseGlobalSymbol() {
        ensureModuleIsLoaded("NativeCall")
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "use NativeCall; say NativeCal<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        if (java.lang.Boolean.getBoolean("raku.test.dump.actual")) println("LOOKUP-ACTUAL ${getTestName(false)} <<<${vars.sorted()}>>>")
        assertNotNull(vars)
        assertContainsElements(vars, Arrays.asList("NativeCall::Compiler::MSVC", "NativeCall::Compiler::GNU"))
    }

    fun testNeedGlobalSymbol() {
        ensureModuleIsLoaded("NativeCall", "need")
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "need NativeCall; say NativeCal<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        if (java.lang.Boolean.getBoolean("raku.test.dump.actual")) println("LOOKUP-ACTUAL ${getTestName(false)} <<<${vars.sorted()}>>>")
        assertNotNull(vars)
        assertContainsElements(vars, Arrays.asList("NativeCall::Compiler::MSVC", "NativeCall::Compiler::GNU"))
    }

    fun testUseFindsExportedSymbol() {
        ensureModuleIsLoaded("NativeCall")
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "use NativeCall; my lon<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertContainsElements(vars, Arrays.asList("long", "longlong"))
    }

    fun testNeedDoesNotFindExportedSymbol() {
        ensureModuleIsLoaded("NativeCall", "need")
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "need NativeCall; my lon<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertEmpty(vars)
    }

    fun testSimpleDeclaredTypeOur() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class Interesting { }\nmy In<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertContainsElements(vars, "Interesting")
    }

    fun testSimpleDeclaredTypeMy() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my class Interesting { }\nmy In<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertContainsElements(vars, "Interesting")
    }

    fun testNestedTypesOutside() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class Interesting { class Nested { class Deeper { } }; my class Lexical { } }\nmy Inter<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertContainsElements(vars, Arrays.asList("Interesting", "Interesting::Nested", "Interesting::Nested::Deeper"))
        assertDoesntContain(vars, "Lexical", "INterested::Lexical")
    }

    fun testNestedTypesInside() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class Interesting { class InterNested { class InterDeeper { } }; my class InterLexical { }; my Inter<caret> }")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertContainsElements(vars, Arrays.asList("Interesting", "Interesting::InterNested",
                                                   "Interesting::InterNested::InterDeeper",
                                                   "InterNested", "InterNested::InterDeeper", "InterLexical"))
        assertDoesntContain(vars, "Interested::InterLexical")
    }

    fun testAnonymousClassIsSafeToComplete() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my class { -<caret> }")
        myFixture.complete(CompletionType.BASIC, 1)
        val types = myFixture.getLookupElementStrings()!!
        assertNotNull(types)
    }

    fun testEnumsAreExportedByDefault() {
        myFixture.configureByFiles("IdeaFoo/Bar9.pm6", "IdeaFoo/Baz.pm6")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertContainsElements(vars, Arrays.asList("ENUM::ONE", "ENUM::TWO"))
    }
}
