package org.raku.comma.reference

import com.intellij.ide.actions.GotoRelatedSymbolAction
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.psi.RakuMethodCall
import org.raku.comma.psi.RakuPackageDecl
import org.raku.comma.psi.RakuPsiElement
import org.raku.comma.psi.RakuSelf
import org.raku.comma.psi.RakuSubCallName
import org.raku.comma.psi.RakuTypeName
import org.raku.comma.psi.RakuVariable
import org.raku.comma.psi.RakuVariableDecl

class GoToDeclarationTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/reference"
    }

    fun testLocalVariable1() {
        doTest("my \$a = 5; say \$a<caret>;", 1, RakuVariable::class.java) { decl ->
            assertNotNull(decl)
            assertEquals(3, decl!!.textOffset)
        }
    }

    fun testLocalVariable2() {
        doTest("our \$a = 5; say \$a<caret>;", 1, RakuVariable::class.java) { decl ->
            assertNotNull(decl)
            assertEquals(4, decl!!.textOffset)
        }
    }

    fun testExternalVariable1() {
        myFixture.configureByFiles("IdeaFoo/Baz.pm6", "IdeaFoo/Bar.pm6")
        val usage = myFixture.file.findElementAt(42)
        val variable = PsiTreeUtil.getParentOfType(usage, RakuVariable::class.java)
        val resolved = variable!!.reference!!.resolve()
        assertNull(resolved)
    }

    fun testExternalVariable2() {
        myFixture.configureByFiles("IdeaFoo/Baz.pm6", "IdeaFoo/Bar.pm6")
        val usage = myFixture.file.findElementAt(60)
        val variable = PsiTreeUtil.getParentOfType(usage, RakuVariable::class.java)
        val resolved = variable!!.reference!!.resolve()
        assertNotNull(resolved)
        assertEquals("Bar.pm6", resolved!!.containingFile.name)
    }

    fun testUseLocalType() {
        doTest("class Foo; Fo<caret>o.new;", 1, RakuTypeName::class.java) { decl ->
            assertNotNull(decl)
            assertEquals(6, decl!!.textOffset)
        }
    }

    fun testUseLocalTypeMultiPart() {
        doTest("class Foo::Bar::Baz; Foo::<caret>Bar::Baz.new;", 1, RakuTypeName::class.java) { decl ->
            assertNotNull(decl)
            assertEquals(6, decl!!.textOffset)
        }
    }

    fun testUseLocalTypeParametrized() {
        doTest("class Foo::Bar[TypeName]; Foo::<caret>Bar.new;", 1, RakuTypeName::class.java) { decl ->
            assertNotNull(decl)
            assertEquals(6, decl!!.textOffset)
        }
    }

    fun testUseExternalType() {
        myFixture.configureByFiles("IdeaFoo/Baz.pm6", "IdeaFoo/Bar.pm6")
        val usage = myFixture.file.findElementAt(25)
        val type = PsiTreeUtil.getParentOfType(usage, RakuTypeName::class.java)
        val resolved = type!!.reference!!.resolve()
        assertNotNull(resolved)
        assertEquals("Bar.pm6", resolved!!.containingFile.name)
    }

    fun testUseExternalRoutine1() {
        myFixture.configureByFiles("IdeaFoo/Baz.pm6", "IdeaFoo/Bar.pm6")
        val usage = myFixture.file.findElementAt(80)
        val call = PsiTreeUtil.getParentOfType(usage, RakuSubCallName::class.java)
        val resolved = call!!.reference!!.resolve()
        assertNotNull(resolved)
        assertEquals("Bar.pm6", resolved!!.containingFile.name)
    }

    fun testUseExternalRoutine2() {
        myFixture.configureByFiles("IdeaFoo/Baz.pm6", "IdeaFoo/Bar.pm6")
        val usage = myFixture.file.findElementAt(100)
        val call = PsiTreeUtil.getParentOfType(usage, RakuSubCallName::class.java)
        val resolved = call!!.reference!!.resolve()
        assertNull(resolved)
    }

    fun testPrivateMethodsReference() {
        doTest("class Foo { has \$.foo; method test { \$.fo<caret>o; } }", 0, RakuMethodCall::class.java) { call ->
            assertTrue(call is RakuVariableDecl)
        }
    }

    fun testOverloadedPrivateMethodReference() {
        myFixture.configureByText(
            RakuScriptFileType.INSTANCE,
            "role Foo { method !a{} }; class Bar does Foo { method !a{}; method !b{ self!<caret>a; } }"
        )
        val usage = myFixture.file.findElementAt(myFixture.caretOffset)!!.parent.parent
        val resolved = usage.reference!!.resolve()
        val enclosingClass = PsiTreeUtil.getParentOfType(resolved, RakuPackageDecl::class.java)
        assertNotNull(enclosingClass)
        assertEquals("Bar", enclosingClass!!.packageName)
    }

    fun testAttributeByCall() {
        doTest("class Foo { has \$.foo; method test { \$.fo<caret>o; } }", 0, RakuMethodCall::class.java) { call ->
            assertTrue(call is RakuVariableDecl)
        }
    }

    fun testMultipleInheritance() {
        doTest("role Foo {}; class Bar does Foo {}; Ba<caret>r.new;", 1, RakuTypeName::class.java) { decl ->
            assertNotNull(decl)
            assertEquals(19, decl!!.textOffset)
        }
    }

    fun testEnumType() {
        doTest("enum Foos <Foo1 Foo2>; my Fo<caret>o1 \$foo;", 1, RakuTypeName::class.java) { decl ->
            assertNotNull(decl)
            assertEquals(5, decl!!.textOffset)
        }
    }

    fun testEnumFullType() {
        doTest("enum Foos <Foo1 Foo2>; my Foos::Fo<caret>o1 \$foo;", 1, RakuTypeName::class.java) { decl ->
            assertNotNull(decl)
            assertEquals(5, decl!!.textOffset)
        }
    }

    fun testNamelessPackageSelf() {
        doTest("say 13; class { method { say se<caret>lf; } }", 1, RakuSelf::class.java) { decl ->
            assertNotNull(decl)
            assertEquals(8, decl!!.textOffset)
        }
    }

    fun testMultiVariableDeclaration() {
        doTest("class A { has (\$.aaa, \$.bbb); method { say \$!a<caret>aa } }", 1, RakuVariable::class.java) { variable ->
            assertNotNull(variable)
            assertEquals("(\$.aaa, \$.bbb)", variable!!.text)
        }
    }

    fun testIndirectPrivateMethod() {
        doTest(
            "class A { class C { method !p() { say 42 }; method m() { my \$c = A::C.new; \$c!<caret>p } }; }; A::C.m;",
            1,
            RakuMethodCall::class.java
        ) { decl ->
            assertNotNull(decl)
            assertEquals("method !p() { say 42 }", decl!!.text)
        }
    }

    fun testFormalParameters() {
        doTest("sub foo { \$^a + \$<caret>a }", 1, RakuVariable::class.java) { variable ->
            assertNotNull(variable)
            assertEquals("\$^a", variable!!.text)
        }
        doTest("sub foo { \$:a + \$<caret>a }", 1, RakuVariable::class.java) { variable ->
            assertNotNull(variable)
            assertEquals("\$:a", variable!!.text)
        }
    }

    fun testDeferredType() {
        doTest("class A {...}; class B { has <caret>A \$.x; }; class A { has \$.foo; }", 0, RakuTypeName::class.java) { pkg ->
            assertNotNull(pkg)
        }
    }

    fun testDynamicVariable() {
        doTest("{ say \$*DYNAM<caret>IC; }; { my \$*DYNAMIC }", 0, RakuVariable::class.java) { decl ->
            assertNotNull(decl)
            assertEquals(25, decl!!.textOffset)
        }
    }

    fun testJumpToTemplateFile() {
        ensureModuleIsLoaded("Cro::WebApp::Template")
        myFixture.configureByFiles(
            "IdeaFoo/TemplateUser.pm6",
            "IdeaFoo/content.crotmp",
            "IdeaFoo/templates/inner-content.crotmp",
            "IdeaFoo/templates2/inner-content2.crotmp"
        )
        myFixture.editor.caretModel.moveToOffset(96)
        val items = GotoRelatedSymbolAction.getItems(myFixture.file, myFixture.editor, DataContext.EMPTY_CONTEXT)
        assertEquals(1, items.size)
    }

    fun testJumpToTemplateFileInDirectory() {
        ensureModuleIsLoaded("Cro::WebApp::Template")
        myFixture.configureByFiles(
            "IdeaFoo/TemplateUser.pm6",
            "IdeaFoo/content.crotmp",
            "IdeaFoo/templates/inner-content.crotmp",
            "IdeaFoo/templates2/inner-content2.crotmp"
        )
        myFixture.editor.caretModel.moveToOffset(207)
        val items = GotoRelatedSymbolAction.getItems(myFixture.file, myFixture.editor, DataContext.EMPTY_CONTEXT)
        assertEquals(1, items.size)
    }

    fun testJumpToTemplateFileInDirectoryAbsolute() {
        ensureModuleIsLoaded("Cro::WebApp::Template")
        myFixture.configureByFiles(
            "IdeaFoo/TemplateUser.pm6",
            "IdeaFoo/content.crotmp",
            "IdeaFoo/templates/inner-content.crotmp",
            "IdeaFoo/templates/inner-content2.crotmp",
            "IdeaFoo/templates2/inner-content2.crotmp"
        )
        myFixture.editor.caretModel.moveToOffset(405)
        val items = GotoRelatedSymbolAction.getItems(myFixture.file, myFixture.editor, DataContext.EMPTY_CONTEXT)
        assertEquals(1, items.size)
    }

    private fun doTest(text: String, offset: Int, clazz: Class<out RakuPsiElement>, check: (PsiElement?) -> Unit) {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, text)
        val usage = myFixture.file.findElementAt(myFixture.caretOffset - offset)
        val element = PsiTreeUtil.getParentOfType(usage, clazz)
        check(element!!.reference!!.resolve())
    }
}
