package org.raku.comma.completion

import com.intellij.codeInsight.completion.CompletionType
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

import java.util.Arrays

class AttributesTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/completion"
    }

    fun testOwnAttributes() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class C { has \$!abc; method a() { say \$!<caret> } }")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertTrue(vars.containsAll(Arrays.asList("\$!", "\$!abc")))
        assertEquals(2, vars.size)
    }

    fun testRoleAttributes() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "role Foo { has \$!foo; has \$.bar; }; class A does Foo { has \$!a; method a() { say \$!<caret> } }")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertTrue(vars.containsAll(Arrays.asList("\$!", "\$!a", "\$!foo", "\$!bar")))
        assertEquals(4, vars.size)
    }

    fun testNestedRoleAttributes() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "role Nested { has \$!nested; }; role Foo does Nested { has \$!foo; has \$.bar; }; class A does Foo { has \$!a; method a() { say \$!<caret> } }")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertTrue(vars.containsAll(Arrays.asList("\$!", "\$!a", "\$!foo", "\$!bar", "\$!nested")))
        assertEquals(5, vars.size)
    }

    fun testExternalAttributes() {
        ensureModuleIsLoaded("NativeCall")
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "use NativeCall; class A does NativeCall::Native { has \$!a; method a() { say \$!<caret> } }")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertContainsElements(vars, Arrays.asList("\$!", "\$!a", "\$!rettype"))
    }

    fun testOuterFileAttributes() {
        myFixture.configureByFiles("IdeaFoo/Bar1.pm6", "IdeaFoo/Baz.pm6")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertTrue(vars.containsAll(Arrays.asList("\$!private", "\$!visible", "\$.visible")))
    }

    fun testOuterFileLongFormAttributes() {
        myFixture.configureByFiles("IdeaFoo/Bar2.pm6", "IdeaFoo/Baz.pm6")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertTrue(vars.containsAll(Arrays.asList("\$!private", "\$!visible", "\$.visible")))
    }

    fun testOuterFileNestedAttributes() {
        myFixture.configureByFiles("IdeaFoo/Bar3.pm6", "IdeaFoo/Baz.pm6")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertTrue(vars.containsAll(Arrays.asList("\$!private", "\$!visible", "\$.visible")))
    }

    fun testAttributeCompletionWithInnerClasses() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class C { has \$!abc; class Inner { has \$!xyz;  method m() { say \$!<caret> } } }")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertTrue(vars.containsAll(Arrays.asList("\$!", "\$!xyz")))
        assertEquals(2, vars.size)
    }

    fun testDotAttribute() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class Foo { has \$.foo; has \$.bar; method test { \$<caret> } }")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertTrue(vars.containsAll(Arrays.asList("\$.foo", "\$.bar", "\$!foo", "\$!bar")))
    }

    fun testAfterDotCompletion() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class Foo { has \$.foo1; has \$.foo2; method test { \$.fo<caret>; } }")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertTrue(vars.containsAll(Arrays.asList(".foo1", ".foo2")))
    }

    fun testLexicalSubBindsToOuterMethod() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class A { has \$!a; has \$.b; method m { sub a { say \$<caret> } } };")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertTrue(vars.containsAll(Arrays.asList("\$!a", "\$!b", "\$.b")))
    }

    fun testLexicalSubWithoutMethodWrapper() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class A { has \$!a; has \$.b; sub a { say \$<caret> } }")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertFalse(vars.contains("\$!a"))
        assertFalse(vars.contains("\$!b"))
        assertFalse(vars.contains("\$.b"))
    }

    fun testPrivateAbsenceFromInnerClassUsingSigilAccess() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class Foo { has \$.foo1; my class Bar { method test { \$!f<caret>; } } }")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertEmpty(vars)
    }

    fun testPrivateAbsenceFromInnerClassUsingSelfAccess() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class Foo { has \$.foo1; has \$.foo2; my class Bar { method test { self!f<caret>; } } }")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertEmpty(vars)
    }

    fun testPrivateVariableAbsenceFromOutside() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class Foo { has \$.foo1; has \$.foo2; }; Foo!f<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertEmpty(vars)
    }

}
