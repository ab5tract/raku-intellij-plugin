package org.raku.comma.highlighting

import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

class RakuHighlightTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/highlight"
    }

    fun testDuplicatedSubs() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "sub foo { 1 }; <error descr=\"Re-declaration of foo from aaa.raku:1\">sub foo</error> { 2 }; foo;")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "multi sub b {}; multi <error descr=\"Re-declaration of b from aaa.raku:1\">sub b</error> {}")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "multi sub c {};\nmulti sub c(Int \$n) { \$n; };\nmulti <error descr=\"Re-declaration of c from aaa.raku:1\">sub c</error> {}")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "\n\nmulti sub d {};\nmulti sub d(Int \$n) { \$n; };\nmulti sub d(Int \$n) { \$n; };\nmulti <error descr=\"Re-declaration of d from aaa.raku:3\">sub d</error> {}")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "multi trait_mod:<is>(Parameter:D \$param, :\$header! --> Nil) is export { \$param does Header; \$header; };\nmulti trait_mod:<is>(Parameter:D \$param, :\$header! --> Nil) is export { \$param does Header; \$header; };")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "multi sub format(&fn where *.signature.params == 1) { \"one param\"; fn() }\n" +
                                  "multi sub format(&fn where *.signature.params == 2) { \"two params\"; fn(); }")
        checkHighlightingDumpable()
    }

    fun testDuplicatedMethods() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class A { method a {}; method b {}; <error descr=\"Re-declaration of a from aaa.raku:1\">method a</error> {} }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "grammar A { rule my-rule {<?>}; rule <error descr=\"Re-declaration of my-rule from aaa.raku:1\">my-rule</error> {<?>}; }")
        checkHighlightingDumpable()
    }

    fun testDuplicatedRules() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "grammar A {\nrule my-rule {<?>};\ntoken <error descr=\"Re-declaration of my-rule from aaa.raku:2\">my-rule</error> {<?>};\n<error descr=\"Re-declaration of my-rule from aaa.raku:2\">method my-rule</error> { 42 }; { 42 };\n}")
        checkHighlightingDumpable()
    }

    fun testDuplicateMultiMethods() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class B { multi method b {}; multi <error descr=\"Re-declaration of b from aaa.raku:1\">method b</error> {} }")
        checkHighlightingDumpable()
    }

    fun testDuplicatedPackages() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "\n\nclass A {};\n\nclass <error descr=\"Re-declaration of A from aaa.raku:3\">A</error> {}")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "grammar Baz {}; grammar <error descr=\"Re-declaration of Baz from aaa.raku:1\">Baz</error> {}")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class A { class A {} };")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "\nclass A {\nclass B {}\n};\nclass <error descr=\"Re-declaration of A::B from aaa.raku:3\">A::B</error> {};")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "\nclass A {\nclass B {}\n};\nclass <error descr=\"Re-declaration of A::B from aaa.raku:3\">A::B</error> {};")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "\nclass A::B {};\nclass A {\nclass <error descr=\"Re-declaration of A::B from aaa.raku:2\">B</error> {}\n};")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class Agrammon::Outputs { ... }\nclass Agrammon::Outputs { }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "subset CCCC1 of Int; class <error descr=\"Re-declaration of CCCC1 from aaa.raku:1\">CCCC1</error> {}")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class DDDD1 {}; subset <error descr=\"Re-declaration of DDDD1 from aaa.raku:1\">DDDD1</error> of Int;")
        checkHighlightingDumpable()
    }

    fun testDuplicatedRoles() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role Baz {}; role <error descr=\"Re-declaration of Baz from aaa.raku:1\">Baz</error> {}")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "role Bar[Type] {}; role <error descr=\"Re-declaration of Bar from aaa.raku:1\">Bar</error>[Type] {}; role Bar {};")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role Saz {}; role Saz[Type] {}; role Saz[Type1, Type2] {};")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role R1 {};\nrole R1[Type] {};\nrole <error descr=\"Re-declaration of R1 from aaa.raku:2\">R1</error>[Type] {};")
        checkHighlightingDumpable()
    }

    fun testDuplicatedVariables() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my \$foo; say \$foo; my <warning descr=\"Re-declaration of \$foo from aaa.raku:1\">\$foo</warning>; say \$foo;")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "\nmy \$bar;\nsay \$bar;\n{ my \$bar; say \$bar; };\nmy <warning descr=\"Re-declaration of \$bar from aaa.raku:2\">\$bar</warning>;\nsay \$bar;")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "sub blah(\$x) { my <warning descr=\"Re-declaration of \$x from aaa.raku:1\">\$x</warning> = 42; say \$x; }; blah(3);")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "sub x() {}; my <warning descr=\"Re-declaration of &x from aaa.raku:1\">&x</warning> = -> {}")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "\nmy my &y;\n<warning descr=\"Re-declaration of &y from aaa.raku:2\">sub y() {}</warning>;")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "with -> (Int() \$v) { say \$v; }\n")
        checkHighlightingDumpable()
    }

    fun testDuplicateAttributes() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class C { has \$!a; has <error descr=\"Re-declaration of \$!a from aaa.raku:1\">\$.a</error>; }")
        checkHighlightingDumpable()
    }

    fun testDuplicatesInExternal() {
        myFixture.configureByFiles("User.rakumod", "Base.rakumod")
        checkHighlightingDumpable()
    }

    fun testRoutineScopes() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class C {\n    method m() {}\n    sub m() {}\n}\nclass D {\n    my method m() {}\n    <error descr=\"Re-declaration of m from aaa.raku:6\">sub m</error>() {}\n}")
        checkHighlightingDumpable()
    }

    // TODO should warn
    fun testPackageScopes() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my class A { class B {} };\n" +
                                  "class A::B {}")
        checkHighlightingDumpable()
    }
}
