package org.raku.comma.completion

import com.intellij.codeInsight.completion.CompletionType
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

import java.util.Arrays

class ClassRoleCompletionTest : CommaFixtureTestCase() {
    fun testSimpleRole() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "role Foo { has \$!foo; method test { say \$!<caret> } }")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertTrue(vars.containsAll(Arrays.asList("\$!foo", "\$!")))
    }

    fun testCompositionInternals() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "role Foo { has \$!foo; has \$!bar; }; class A does Foo { method test { say \$!<caret> } }")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertTrue(vars.containsAll(Arrays.asList("\$!foo", "\$!bar")))
    }

    fun testInheritanceInternals() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class Foo { has \$!foo; has \$!bar; }; class A is Foo { method test { say \$!<caret> } }")
        myFixture.complete(CompletionType.BASIC, 1)
        assertNull(myFixture.getLookupElementStrings())
    }

    fun testRoleIntoRoleComposiitonInternals() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "role Foo { has \$!foo; has \$!bar; }; role A does Foo { method test { say \$!<caret> } }")
        myFixture.complete(CompletionType.BASIC, 1)
        assertNull(myFixture.getLookupElementStrings())
    }

    fun testClassIntoClassInheritanceInternals() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class Foo { has \$!foo; has \$!bar; }; class A is Foo { method test { say \$!<caret> } }")
        myFixture.complete(CompletionType.BASIC, 1)
        assertNull(myFixture.getLookupElementStrings())
    }

    fun testRoleInMiddleDoesNotHaveAttrs1() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "role A { has \$!foo }; role B does A { method test { say \$!<caret> } }; class C does B {}")
        myFixture.complete(CompletionType.BASIC, 1)
        assertNull(myFixture.getLookupElementStrings())
    }

    fun testRoleInMiddleDoesNotHaveAttrs2() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "role A { has \$!foo }; role B does A {}; class C does B { method test { say \$!<caret> } }")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        assertNotNull(vars)
        assertTrue(vars.containsAll(Arrays.asList("\$!foo", "\$!")))
    }

    fun testRoleInMiddleHasMethods1() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "role A { method !a {} }; role B does A { method test { self!<caret> } }; class C does B {}")
        myFixture.complete(CompletionType.BASIC, 1)
        assertNull(myFixture.getLookupElementStrings())
    }

    fun testRoleInMiddleHasMethods2() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "role A { method !a {} }; role B does A {}; class C does B { method test { self!<caret> } }")
        myFixture.complete(CompletionType.BASIC, 1)
        assertNull(myFixture.getLookupElementStrings())
    }

    fun testAlsoTraitComposition() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class A { method mmm1 {} }; class B { also is A; method mmm2 { self.mmm<caret> } }")
        myFixture.complete(CompletionType.BASIC, 1)
        val methods = myFixture.getLookupElementStrings()!!
        assertNotNull(methods)
        assertTrue(methods.containsAll(Arrays.asList(".mmm2", ".mmm1")))
    }

    fun testAlsoTraitInheritance() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "role A { method mmm1 {} }; class B { also does A; method mmm2 { self.mmm<caret> } }")
        myFixture.complete(CompletionType.BASIC, 1)
        val methods = myFixture.getLookupElementStrings()!!
        assertNotNull(methods)
        assertTrue(methods.containsAll(Arrays.asList(".mmm2", ".mmm1")))
    }

    fun testAlsoTraitWrongTraitIsNotCompleted() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "role A { method mmm1 {} }; class B { also doe A; method mmm2 { self.mmm<caret> } }")
        myFixture.complete(CompletionType.BASIC, 1)
        assertNull(myFixture.getLookupElementStrings())
    }

    // TODO Re-instate this test when we can provide suggestions for trusts
    //public void testTrustedMethod1() {
    //    myFixture.configureByText(RakuScriptFileType.INSTANCE,
    //                              "class A { trusts B; method !mmm1 {}; method !mmm2 {}; }; class B { method test { A!<caret> } }")
    //    myFixture.complete(CompletionType.BASIC, 1)
    //    List<String> methods = myFixture.getLookupElementStrings()!!
    //    assertNotNull(methods)
    //    assertTrue(methods.containsAll(Arrays.asList("!A::mmm1", "!A::mmm2")))
    //    assertEquals(2, methods.size)
    //}
}
