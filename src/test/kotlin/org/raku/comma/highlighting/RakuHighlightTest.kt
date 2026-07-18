package org.raku.comma.highlighting

import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

class RakuHighlightTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/highlight"
    }

    fun testDuplicatedSubs() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "sub foo { 1 }; <error descr=\"Re-declaration of foo from aaa.rakuidea:1\">sub <weak_warning descr=\"Unused subroutine\">foo</weak_warning></error> { 2 }; foo;")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "multi sub b {}; multi <error descr=\"Re-declaration of b from aaa.rakuidea:1\">sub b</error> {}")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "multi sub c {};\nmulti sub c(Int \$n) { \$n; };\nmulti <error descr=\"Re-declaration of c from aaa.rakuidea:1\">sub c</error> {}")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "\n\nmulti sub d {};\nmulti sub d(Int \$n) { \$n; };\nmulti <error descr=\"Re-declaration of d from aaa.rakuidea:4\">sub d</error>(Int \$n) { \$n; };\nmulti <error descr=\"Re-declaration of d from aaa.rakuidea:3\">sub d</error> {}")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "multi trait_mod:<is>(Parameter:D \$param, :\$header! --> Nil) is export { \$param does Header; \$header; };\n" +
                                  "multi <error descr=\"Re-declaration of trait_mod:<is> from aaa.rakuidea:1\">trait_mod:<is></error>(Parameter:D \$param, :\$header! --> Nil) is export { \$param does Header; \$header; };")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "multi sub format(&fn where *.signature.params == 1) { \"one param\"; fn() }\n" +
                                  "multi sub format(&fn where *.signature.params == 2) { \"two params\"; fn(); }")
        myFixture.checkHighlighting()
    }

    fun testDuplicatedMethods() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class A { method a {}; method b {}; <error descr=\"Re-declaration of a from aaa.rakuidea:1\">method a</error> {} }")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "grammar A { rule my-rule {<?>}; rule <error descr=\"Re-declaration of my-rule from aaa.rakuidea:1\">my-rule</error> {<?>}; }")
        myFixture.checkHighlighting()
    }

    fun testDuplicatedRules() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "grammar A {\nrule my-rule {<?>};\ntoken <error descr=\"Re-declaration of my-rule from aaa.rakuidea:2\">my-rule</error> {<?>};\n<error descr=\"Re-declaration of my-rule from aaa.rakuidea:2\">method my-rule</error> { 42 }; { 42 };\n}")
        myFixture.checkHighlighting()
    }

    fun testDuplicateMultiMethods() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class B { multi method b {}; multi <error descr=\"Re-declaration of b from aaa.rakuidea:1\">method b</error> {} }")
        myFixture.checkHighlighting()
    }

    fun testDuplicatedPackages() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "\n\nclass A {};\n\nclass <error descr=\"Re-declaration of A from aaa.rakuidea:3\">A</error> {}")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "grammar Baz {}; grammar <error descr=\"Re-declaration of Baz from aaa.rakuidea:1\">Baz</error> {}")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class A { class A {} };")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "\nclass A {\nclass B {}\n};\nclass <error descr=\"Re-declaration of A::B from aaa.rakuidea:3\">A::B</error> {};")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "\nclass A {\nclass B {}\n};\nclass <error descr=\"Re-declaration of A::B from aaa.rakuidea:3\">A::B</error> {};")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "\nclass A::B {};\nclass A {\nclass <error descr=\"Re-declaration of A::B from aaa.rakuidea:2\">B</error> {}\n};")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class Agrammon::Outputs { ... }\nclass Agrammon::Outputs { }")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "subset CCCC1 of Int; class <error descr=\"Re-declaration of CCCC1 from aaa.rakuidea:1\">CCCC1</error> {}")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class DDDD1 {}; subset <error descr=\"Re-declaration of DDDD1 from aaa.rakuidea:1\">DDDD1</error> of Int;")
        myFixture.checkHighlighting()
    }

    fun testDuplicatedRoles() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role Baz {}; role <error descr=\"Re-declaration of Baz from aaa.rakuidea:1\">Baz</error> {}")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "role Bar[Type] {}; role <error descr=\"Re-declaration of Bar from aaa.rakuidea:1\">Bar</error>[Type] {}; role Bar {};")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role Saz {}; role Saz[Type] {}; role Saz[Type1, Type2] {};")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role R1 {};\nrole R1[Type] {};\nrole <error descr=\"Re-declaration of R1 from aaa.rakuidea:2\">R1</error>[Type] {};")
        myFixture.checkHighlighting()
    }

    fun testDuplicatedVariables() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$foo; say \$foo; my <warning descr=\"Re-declaration of \$foo from aaa.rakuidea:1\"><weak_warning descr=\"Unused variable\">\$foo</weak_warning></warning>; say \$foo;")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "\nmy \$bar;\nsay \$bar;\n{ my \$bar; say \$bar; };\nmy <warning descr=\"Re-declaration of \$bar from aaa.rakuidea:2\"><weak_warning descr=\"Unused variable\">\$bar</weak_warning></warning>;\nsay \$bar;")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "sub blah(\$x) { my <warning descr=\"Re-declaration of \$x from aaa.rakuidea:1\"><weak_warning descr=\"Unused variable\">\$x</weak_warning></warning> = 42; say \$x; }; blah(3);")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "sub x() {}; my <warning descr=\"Re-declaration of &x from aaa.rakuidea:1\"><weak_warning descr=\"Unused variable\">&x</weak_warning></warning> = -> {}")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "\nmy my <weak_warning descr=\"Unused variable\">&y</weak_warning>;\n<warning descr=\"Re-declaration of &y from aaa.rakuidea:2\">sub y() {}</warning>;")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "with -> (Int() \$v) { say \$v; }\n")
        myFixture.checkHighlighting()
    }

    fun testDuplicateAttributes() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class C { has <weak_warning descr=\"Unused attribute\">\$!a</weak_warning>; has <error descr=\"Re-declaration of \$!a from aaa.rakuidea:1\">\$.a</error>; }")
        myFixture.checkHighlighting()
    }

    fun testDuplicatesInExternal() {
        myFixture.configureByFiles("User.rakumod", "Base.rakumod")
        myFixture.checkHighlighting()
    }

    fun testRoutineScopes() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class C {\n" +
                                  "    method m() {}\n" +
                                  "    sub <weak_warning descr=\"Unused subroutine\">m</weak_warning>() {}\n" +
                                  "}\n" +
                                  "class D {\n" +
                                  "    my method m() {}\n" +
                                  "    <error descr=\"Re-declaration of m from aaa.rakuidea:6\">sub m</error>() {}\n" +
                                  "}")
        myFixture.checkHighlighting()
    }

    // TODO should warn
    fun testPackageScopes() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my class A { class B {} };\n" +
                                  "class A::B {}")
        myFixture.checkHighlighting()
    }
}
