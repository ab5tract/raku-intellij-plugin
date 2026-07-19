package org.raku.comma.annotation

import org.raku.comma.ALL_RAKU_INSPECTIONS
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

class AnnotationTest : CommaFixtureTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(*ALL_RAKU_INSPECTIONS)
    }

    override fun getTestDataPath(): String {
        return "testData/annotation"
    }

    fun testUndeclaredVariableAnnotatorReallyUndeclared() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say <error descr=\"Variable \$foo is not declared\">\$foo</error>;")
        checkHighlightingDumpable()
    }

    fun testUndeclaredVariableAnnotatorNoErrorIfDeclared() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$foo; say \$foo;")
        checkHighlightingDumpable()
    }

    fun testUndeclaredVariableAnnotatorDefaultsInOuterScopeOK() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say \$_, \$/, \$!")
        checkHighlightingDumpable()
    }

    fun testUndeclaredVariableAnnotatorPostdeclaredSubOK() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say &a.arity; sub a { }")
        checkHighlightingDumpable()
    }

    fun testUndeclaredVariableAnnotatorUndeclaredSubCaught() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say <error descr=\"Variable &a is not declared\">&a</error>.arity; sub ab { }; ab();")
        checkHighlightingDumpable()
    }

    fun testUndeclaredVariableAnnotatorPostdeclared() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say <error descr=\"Variable \$foo is not declared in this scope yet\">\$foo</error>; my \$foo = 42; say \$foo")
        checkHighlightingDumpable()
    }

    fun testUndeclaredVariableAnnotatorNoErrorIfConstantDeclared() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my constant \$foo = 42; say \$foo;")
        checkHighlightingDumpable()
    }

    fun testUndeclaredVariableAnnotatorFinishPresent() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "say <error descr=\"There is no =finish section in this file\">\$=finish</error>;\n\n=begin finish\n\nfoo")
        checkHighlightingDumpable()
    }

    fun testUndeclaredVariableAnnotatorFinishIsNotPresent() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "say \$=finish;")
        checkHighlightingDumpable()
    }

    fun testUndeclaredVariableAnnotatorFinishPresentInBlock() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "if 1 {\nsay <error descr=\"There is no =finish section in this file\">\$=finish</error>;\n}\n=begin finish\n\nfoo")
        checkHighlightingDumpable()
    }

    fun testUndeclaredVariableAnnotatorRoleParameter() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role A[\$foo, @foo, :\$bar] { method m() { \$foo, @foo, \$bar } }")
        checkHighlightingDumpable()
    }

    fun testCursorAvailableInToken() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "grammar G { token TOP { x { say \$¢ } } }")
        checkHighlightingDumpable()
    }

    fun testFalsePositive1() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 1, 2; my (\$a) = \$x; say \$a")
        checkHighlightingDumpable()
    }

    fun testAnonymousVariables() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$; my @; my %; my &; say \$; say @; say %; say &;")
        checkHighlightingDumpable()
    }

    fun testDeclaredSubAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our sub foo() {};\nmy sub bar() {};\nfoo;\nbar()")
        checkHighlightingDumpable()
    }

    fun testUndeclaredSubAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<warning descr=\"Subroutine foo is not declared\">foo</warning>;")
        checkHighlightingDumpable()
    }

    fun testDeclaredSubAnnotatorWhenItIsReallyACoercion() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = DateTime('2020-01-01');")
        myFixture.checkHighlighting(false, false, false, false)
    }

    fun testDeclaredAliasedCoreSubAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my @a = [1,2], [3,4]; cross(@a)")
        checkHighlightingDumpable()
    }

    fun testNoBogusSubAnnotationOnInterpolatedName() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say ::(\"Rakudo::Internals\").?LL-EXCEPTION;")
        checkHighlightingDumpable()
    }

    fun testInfixBracketedInVariableIsNotConsideredUndeclared() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my &add = &[+]; say add(1,2)")
        checkHighlightingDumpable()
    }

    fun testDeclaredOperatorNamesInVariables() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my @ops = &infix:<+>, &prefix:<!>, &postfix:<++>, &postcircumfix:<[ ]>; say @ops")
        checkHighlightingDumpable()
    }

    fun testUndeclaredOperatorNamesInVariables() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say <error descr=\"Variable &infix:<foo> is not declared\">&infix:<foo></error>;")
        checkHighlightingDumpable()
    }

    fun testLeadingZeroAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "say <warning descr=\"Leading 0 does not indicate octal in Raku: use 0o755 instead\">0755</warning>;")
        checkHighlightingDumpable()
    }

    fun testMethodNotOnRangeAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say <warning descr=\"Precedence of ^ is looser than method call; please parenthesize\">^1.map(*.is-prime)</warning>;")
        checkHighlightingDumpable()
    }

    fun testUnitKeywordAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<error descr=\"Semicolon form of 'class' without 'unit' is not supported\">class Foo</error>;")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<error descr=\"Cannot use 'unit' with block form of declaration\">unit class </error>Foo{}")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "unit class Foo;")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my")
        checkHighlightingDumpable()
    }

    fun testEmptyNameVariableAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say \$;")
        checkHighlightingDumpable()
    }

    fun testUndeclaredPrivateMethodAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "role A { method !a(\$one) {\$one} }; class B does A { method b { self<warning descr=\"Private method !c is used, but not declared\">!c</warning>(1); } }")
        checkHighlightingDumpable()
    }

    fun testDeclaredPrivateMethodAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role A { method !a {} }; class B does A { method b { self!a; } }")
        checkHighlightingDumpable()
    }

    fun testDeclaredExternalPrivateMethodAnnotator() {
        ensureModuleIsLoaded("NativeCall")
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "use <warning descr=\"Cannot find NativeCall which is specified as a dependency in META6.json\">NativeCall</warning>; role A does NativeCall::Native { method !a {} }; class B does A { method b { self<warning descr=\"Private method !setup is used, but not declared\">!setup</warning>; } }")
        checkHighlightingDumpable()
    }

    fun testNoUndeclaredPrivateMethodAnnotationInRoleAsItMayBeDeclaredInTheClass() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role Foo { method bar { self!bar } }")
        checkHighlightingDumpable()
    }

    fun testUndeclaredAttributeAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role A { has \$!a; }; class B does A { method b { say <error descr=\"Attribute \$!b is used, but not declared\">\$!b</error>; } }")
        checkHighlightingDumpable()
    }

    fun testDeclaredAttributeAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role A { has \$!a; }; class B does A { has \$!b; method b { say \$!a; say \$!b; } }")
        checkHighlightingDumpable()
    }

    fun testUndeclaredMultiAttributeAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class A { has (\$!a, \$!asdrf); method m() { \$!a, \$!asdrf } }")
        checkHighlightingDumpable()
    }

    fun testDeclaredExternalAttributeAnnotator() {
        ensureModuleIsLoaded("NativeCall")
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "use <warning descr=\"Cannot find NativeCall which is specified as a dependency in META6.json\">NativeCall</warning>; class A does NativeCall::Native { method b { say <error descr=\"Attribute \$!rettype is used, but not declared\">\$!rettype</error>; } }")
        checkHighlightingDumpable()
    }

    fun testSignatureAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "sub sift4(Str \$s1, Str \$s2, Int \$maxOffset = 100, Int \$maxDistance = 100 --> Int) is export { say \$s1, \$s2, \$maxOffset, \$maxDistance; }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "multi sub encode-base64(Bool:D :\$pad!, |c) { samewith(:pad(?\$pad ?? '=' !! ''), |c) }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "multi sub MAIN('nuke', Bool :<weak_warning descr=\"Unused parameter\">\$confirm</weak_warning>, *<weak_warning descr=\"Unused parameter\">@names</weak_warning> (\$, *@)) {}")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "sub a(Str :\$foo, <warning descr=\"Explicit `?` on a named parameter \$bar is redundant, as all nameds are optional by default\">Str :\$bar?</warning>) { say \$foo; say \$bar; }; a;")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class A { has \$!a; submethod BUILD(:\$!a = 42, :\$b!) { say \$b; say \$!a; }; }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my (:%file, :%methods (:%over-documented, :%under-documented, :%introspection, *%)); %file; %methods; %over-documented; %under-documented; %introspection;")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our sub foo(\$a?, <error descr=\"Cannot put positional parameter \$b after an optional parameter\">\$b</error>) { \$a, \$b }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our sub foo(\$a, *@b, <error descr=\"Cannot put positional parameter \$c after a variadic parameter\">\$c</error>) { \$a, @b, \$c }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our sub foo(\$a, *@b, <error descr=\"Cannot put optional parameter \$c after a variadic parameter\">\$c?</error>) { \$a, @b, \$c }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our sub foo(:\$a, <error descr=\"Cannot put positional parameter \$b after a named parameter\">\$b</error>) { \$a, \$b }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our sub foo(\$a = 42, <error descr=\"Cannot put positional parameter \$b after an optional parameter\">\$b</error>) { \$a, \$b }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our sub foo(\$a, *@as, :\$c!) { \$a, @as, \$c }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our sub foo(\$a, *@as, :\$c) { \$a, @as, \$c }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our sub foo(*%h, :\$c) { %h, \$c }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our sub web(Str \$cfg-filename, Str \$model-filename, Str \$tech-file?) is export { \$cfg-filename, \$model-filename, \$tech-file }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "multi sub MAIN('web', ExistingFile \$cfg-filename, ExistingFile \$model-filename, Str \$tech-file?) is export { \$cfg-filename, \$model-filename, \$tech-file }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "sub MAIN(Admin, 'web') {}")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our sub foo(\$a = 42, \$bar? is copy) { \$a, \$bar }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our sub foo(<warning descr=\"Explicit `?` on a named parameter \$bar is redundant, as all nameds are optional by default\">:\$bar?</warning>) { \$bar }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our sub foo(<warning descr=\"Explicit `!` on a positional parameter \$foo is redundant, as all positional parameters are required by default\">\$foo!</warning>, \$bar) { \$foo, \$bar }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our sub foo(<warning descr=\"Explicit `?` on a parameter \$foo with default is redundant, as all parameters with default value are optional by default\">\$foo? = 42</warning>) { \$foo }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our sub foo(<error descr=\"Parameter \$foo has a default value and so cannot be required\">\$foo! = 42</error>) { \$foo }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "sub template-location(IO() \$location, :\$compile-all, :\$test = { .IO.basename !~~ / ^ '.' / } --> Nil) is export { \$location; \$compile-all; \$test; }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my <weak_warning descr=\"Unused variable\">\$foo</weak_warning> = sub foo(:<weak_warning descr=\"Unused parameter\">\$a</weak_warning>!) {}")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my <weak_warning descr=\"Unused variable\">\$bar</weak_warning> = -> :<weak_warning descr=\"Unused parameter\">\$a</weak_warning>! {}")
        checkHighlightingDumpable()
    }

    fun testOptionalParameterAfterDefaultWithReturnType() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our sub a(\$a = 5, \$b? ) { \$a, \$b }")
        checkHighlightingDumpable()
    }

    fun testRawWheneverAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<error=descr=\"A whenever must be within a supply or react block\"whenever</error> \$foo {}")
        checkHighlightingDumpable()
    }

    fun testInfiniteRangeAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "1..*")
        checkHighlightingDumpable()
    }

    fun testIncompleteRangeAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say 1<error=\"The range operator must have a second argument\">..</error>;")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "say <warning descr=\"Precedence of ^ is looser than method call; please parenthesize\">1..42.Int</warning>;")
        checkHighlightingDumpable()
    }

    fun testIncompleteRangeAnnotatorWithPrefixEnding() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my (\$foo, \$bar, \$baz); \$foo .. +(\$bar // \$baz);")
        checkHighlightingDumpable()
    }

    fun testLiteralRange() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "for 5..10 {}")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$a; my \$b; for \$a..\$b {}")
        checkHighlightingDumpable()
    }

    fun testRangeWIthWhateverStarIsTooSmartForSimplification() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "[0..*-31]")
        checkHighlightingDumpable()
    }

    fun testRangeWithNewlineIsCompleted() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my <weak_warning descr=\"Unused variable\">\$range</weak_warning> = <weak_warning descr=\"Range can be simplified\">0\n..\n1</weak_warning>")
        checkHighlightingDumpable()
    }

    fun testZeroToNRange() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "for <weak_warning descr=\"Range can be simplified\">0..9</weak_warning> {}")
        checkHighlightingDumpable()
    }

    fun testZeroToExclusiveNRange() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "for <weak_warning descr=\"Range can be simplified\">0..^10</weak_warning> {}")
        checkHighlightingDumpable()
    }

    fun testZeroToVarRange() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$n = 5; for <weak_warning descr=\"Range can be simplified\">0..^\$n</weak_warning> {}")
        checkHighlightingDumpable()
    }

    fun testZeroToExclusiveVarRange() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$n = 5; for <weak_warning descr=\"Range can be simplified\">0..\$n-1</weak_warning> {}")
        checkHighlightingDumpable()
    }

    fun testZeroToExclusiveVarInParensRange() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$n = 5; for <weak_warning descr=\"Range can be simplified\">0..(\$n-1)</weak_warning> {}")
        checkHighlightingDumpable()
    }

    fun testNullRegexAnnotator1() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<error=\"Empty regex is not allowed\">//</error>;")
        checkHighlightingDumpable()
    }

    fun testNullRegexAnnotator2() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "token foo<error=\"Empty regex is not allowed\">{}</error>;")
        checkHighlightingDumpable()
    }

    fun testNullRegexAnnotator3() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "regex foo <error=\"Empty regex is not allowed\">{}</error>;")
        checkHighlightingDumpable()
    }

    fun testNullRegexAnnotator4() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "rule foo <error=\"Empty regex is not allowed\">{}</error>;")
        checkHighlightingDumpable()
    }

    fun testWheneverInReactAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$foo; react { whenever \$foo {} }")
        checkHighlightingDumpable()
    }

    fun testWheneverInSupplyAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$foo; supply { whenever \$foo {} }")
        checkHighlightingDumpable()
    }

    fun testRegexPositionalDeclAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<error descr=\"Cannot declare a regex positional match variable\">my \$0 = 42</error>;")
        checkHighlightingDumpable()
    }

    fun testTypedRegexPositionalDeclAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<error descr=\"Cannot declare a regex positional match variable\">my Int \$0 = 42</error>;")
        checkHighlightingDumpable()
    }

    fun testRegexNamedDeclScalarSigilAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<error descr=\"Cannot declare a regex named match variable\">my \$<foo> = 42</error>;")
        checkHighlightingDumpable()
    }

    fun testRegexNamedDeclArraySigilAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<error descr=\"Cannot declare a regex named match variable\">my @<foo> = 42</error>;")
        checkHighlightingDumpable()
    }

    fun testRegexNamedDeclHashSigilAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<error descr=\"Cannot declare a regex named match variable\">my %<foo> = 42</error>;")
        checkHighlightingDumpable()
    }

    fun testContextualizerDeclScalarSigilAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<error descr=\"Cannot declare a contextualizer\">my \$('x') = 42</error>;")
        checkHighlightingDumpable()
    }

    fun testContextualizerDeclArraySigilAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<error descr=\"Cannot declare a contextualizer\">my @('x') = 42</error>;")
        checkHighlightingDumpable()
    }

    fun testContextualizerDeclHashSigilAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<error descr=\"Cannot declare a contextualizer\">my %('x') = 42</error>;")
        checkHighlightingDumpable()
    }

    fun testUndeclarableAnnotatorUsesActualVariableDeclaration() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our token cidr { (\\d+) <?{ \$0 <= 32 }> }")
        checkHighlightingDumpable()
    }

    fun testUndeclaredAnnotatorInMethodCall() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "token foo { (.) <.panic(\"Unknown escape \\\\\$0\")> }")
        checkHighlightingDumpable()
    }

    fun testUndeclaredAnnotatorRegexVars() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "if <error descr=\"Variable \$sub-key is not declared\">\$sub-key</error> ~~ /^ <[\\w-]>+ \$/ {<error descr=\"Variable \$0 is not declared\">\$0</error>}")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "when m{ ^ ( '#' .+? ) \\s*? \$ } { say \$0; }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "unless m{ ^ ( '#' .+? ) \\s*? \$ } {}; say \$0;")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "if True { m{ ^ ( '#' .+? ) \\s*? \$ }; \$0; }")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "/[ ('a') (\\d+) <?{ 1920 <= \$1.tail <= 2020 }> ]/;")
        checkHighlightingDumpable()
    }

    fun testUndeclaredAnnotatorRegexVarsCorrectComparisonIsUsed() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "sub foo(\$sub-key) { if \$sub-key ~~ m:s/^ '{' (<[\\w-]>+)+ % ';' '}' \$/ { \$0 } elsif \$sub-key ~~ /^ <[\\w-]>+ \$/ {} }; foo('bar')")
        checkHighlightingDumpable()
    }

    fun testRestrictUnitKeywordToMAINSubAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<error=\"The unit sub syntax is only allowed for the sub MAIN\">unit</error> sub foo() {<warning descr=\"Useless use of value in sink (void) context\">}</warning>")
        checkHighlightingDumpable()
    }

    fun testPermitUnitKeywordForMAINSubAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "unit sub MAIN() {}")
        checkHighlightingDumpable()
    }

    fun testInfixAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$lc-and-trim := { \$_ = .lc.trim }; say \$lc-and-trim('x')")
        checkHighlightingDumpable()
    }

    fun testEVALCase1() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "EVAL \"5\";")
        checkHighlightingDumpable()
    }

    fun testEVALCase2() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "EVAL q[5];")
        checkHighlightingDumpable()
    }

    fun testEVALCase3() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "EVAL q[\$foo];")
        checkHighlightingDumpable()
    }

    fun testEVALCase4() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$foo = 5; <error descr=\"Cannot EVAL interpolated expression without MONKEY-SEE-NO-EVAL pragma\">EVAL qq[\$foo]</error>;")
        checkHighlightingDumpable()
    }

    fun testEVALCase5() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "EVAL qq[];")
        checkHighlightingDumpable()
    }

    fun testMissingStubbedMethodFromSingleRole() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "role R { method foo(\$a) {...}; method bar(\$a) {...} }; <error descr=\"Composed roles require to implement methods: bar, foo\">class C does R</error> {}")
        checkHighlightingDumpable()
    }

    fun testMissingStubbedMethodsFromManyRoles() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "role R { method foo(\$a) {...}; method bar(\$a) {...} }; role R2 { method baz {...} }; <error descr=\"Composed roles require to implement methods: bar, foo, baz\">class C does R does R2</error>{}")
        checkHighlightingDumpable()
    }

    fun testStubbedMethodFromRoleImplementedAsAccessor() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "role R { method baz {...}; method bar {...}; method foo {...} }; <error descr=\"Composed roles require to implement methods: bar, baz\">class C does R</error> { my \$.baz; has \$.foo; has \$!bar; method m() { \$!bar } }")
        checkHighlightingDumpable()
    }

    fun testMissingStubbedMethodsIncludeMulti() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role R { multi method foo(\$a) {...}; method bar(\$a) {...} }; class C does R { multi method foo(\$a) { \$a }; multi method foo(@b) { @b }; method bar(\$a) {...} }")
        checkHighlightingDumpable()
    }

    fun testMissingStubbedMethodDoNotIncludeFilledOnes1() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "role R1 { method m {...} }; role R2 does R1 { method m {...} }; <error descr=\"Composed roles require to implement methods: m\">class C does R2</error> {}")
        checkHighlightingDumpable()
    }

    fun testMissingStubbedMethodDoNotIncludeFilledOnes2() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role R1 { method m {...} }; role R2 { method m {} }; class C does R1 does R2 {}")
        checkHighlightingDumpable()
    }

    fun testMissingStubbedMethodDoNotIncludeFilledOnes3() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role R1 { method m {...} }; role R2 { method m {} }; class C does R2 does R1 {}")
        checkHighlightingDumpable()
    }

    fun testMissingStubbedMethodsCountMultidecls() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role R { method m {...}; method b {...}; }; class C does R { has (\$.m, \$.b); }")
        checkHighlightingDumpable()
    }

    fun testMissingStubbedMethodsHandlesTrait() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role R { method foo {...}; method bar {...}; method baz {...} }; class Impl { method foo {} }; class C does R { has Impl \$.impl handles <foo bar>; has Int \$.foo handles <baz> }")
        checkHighlightingDumpable()
    }

    fun testMyScopedVariableExportAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<error descr=\"`my` scoped variable cannot be exported\">my \$var is export</error>;")
        checkHighlightingDumpable()
    }

    fun testRoleDoesClassAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class C {}; role A <error descr=\"Role cannot compose a class\">does C</error> {}")
        checkHighlightingDumpable()
    }

    fun testClassDoesClassAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class C {}; class A <error descr=\"Class cannot compose a class\">does C</error> {}")
        checkHighlightingDumpable()
    }

    fun testClassDoesClassAlsoAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class C {}; class D { also <error descr=\"Class cannot compose a class\">does C</error> }")
        checkHighlightingDumpable()
    }

    fun testRoleDoesClassAlsoAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class C {}; role D { also <error descr=\"Role cannot compose a class\">does C</error> }")
        checkHighlightingDumpable()
    }

    fun testNormalComposition1() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role R {}; role A does C {}")
        checkHighlightingDumpable()
    }

    fun testNormalComposition2() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "role R {}; class A does C {}")
        checkHighlightingDumpable()
    }

    fun testNormalInheritance1() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class C {}; class A is C {}")
        checkHighlightingDumpable()
    }

    fun testNormalInheritance2() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class C {}; role A is C {}")
        checkHighlightingDumpable()
    }

    fun testTrustedMethodIsCountedAsDeclarted() {
        myFixture.configureByFile("TrustedClass.pm6")
        checkHighlightingDumpable()
    }

    fun testOOMonitors() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<error descr=\"Cannot create package of 'monitor' type without OO::Monitors imported into scope\">monitor LongName::Name</error> {}")
        checkHighlightingDumpable()
    }

    fun testFromPerl5ModuleParens() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "use <warning descr=\"Cannot find Foo::Bar which is specified as a dependency in META6.json\">Foo::Bar:from('Perl5')</warning>")
        checkHighlightingDumpable()
    }

    fun testFromPerl5ModuleAngles() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "use <warning descr=\"Cannot find Foo::Bar which is specified as a dependency in META6.json\">Foo::Bar:from<Perl5></warning>")
        checkHighlightingDumpable()
    }

    fun testSigspaceAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "grammar G { rule foo { abc<info desc=\"Implicit <.ws> call\"> </info>def<info desc=\"Implicit <.ws> call\"> </info>} }")
        myFixture.checkHighlighting(false, true, false, true)
    }

    fun testPackageDeclAnnotator1() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "package Foo {}")
        checkHighlightingDumpable()
    }

    fun testPackageDeclAnnotator2() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "module Foo {}")
        checkHighlightingDumpable()
    }

    fun testPackageDeclAnnotator3() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "module Foo <error descr=\"module cannot compose a role\">does A</error> {}")
        checkHighlightingDumpable()
    }

    fun testPackageDeclAnnotator4() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "module Foo <error descr=\"module cannot inherit a class\">is A</error> {}")
        checkHighlightingDumpable()
    }

    fun testPackageDeclAnnotator5() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "module Foo <error descr=\"module cannot compose a role\">does A</error> <error descr\"module cannot inherit a class\">is A</error> {<warning descr=\"Useless use of value in sink (void) context\">}</warning>")
        checkHighlightingDumpable()
    }

    fun testPackageDeclAnnotator6() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "package Foo <error descr=\"package cannot compose a role\">does A</error> {}")
        checkHighlightingDumpable()
    }

    fun testPackageDeclAnnotator7() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "package Foo <error descr=\"package cannot inherit a class\">is A</error> {}")
        checkHighlightingDumpable()
    }

    fun testPackageDeclAnnotator8() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "package Foo <error descr=\"package cannot compose a role\">does A</error> <error descr\"package cannot inherit a class\">is A</error> {<warning descr=\"Useless use of value in sink (void) context\">}</warning>")
        checkHighlightingDumpable()
    }

    fun testPackageDeclAlsoTraitAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "package Foo { also <error descr=\"package cannot compose a role\">does A</error> }")
        checkHighlightingDumpable()
    }

    fun testMonitorAnnotatorOnEmptyNameCase() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "use v6.d.PREVIEW; <error descr=\"Cannot create package of 'monitor' type without OO::Monitors imported into scope\">monitor Bar</error> {}")
        checkHighlightingDumpable()
    }

    fun testCompletelyFineReturn() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "sub foo() { return 42 }; foo();")
        checkHighlightingDumpable()
    }

    fun testReturnOutsideOfRoutineListOp() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "say 42; <warning descr=\"Return outside of routine\">return 100</warning>;")
        checkHighlightingDumpable()
    }

    fun testReturnOutsideOfRoutineFunctionCall() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "say 42; <warning descr=\"Return outside of routine\">return(100)</warning>;")
        checkHighlightingDumpable()
    }

    fun testReturnInStartBlock() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "await start { <warning descr=\"Cannot use return to produce a result in a start block\">return 100</warning>; }")
        checkHighlightingDumpable()
    }

    fun testReturnInSupplyBlock() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my <weak_warning descr=\"Unused variable\">\$s</weak_warning> = supply { <warning descr=\"Cannot use return to exit a supply block\">return 100</warning>; }")
        checkHighlightingDumpable()
    }

    fun testReturnInReactBlock() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "react { <warning descr=\"Cannot use return to exit a react block\">return 100</warning>; }")
        checkHighlightingDumpable()
    }

    fun testReturnInWheneverBlock() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "react { whenever Supply.interval(1) { <warning descr=\"Cannot use return in a whenever block\">return 100</warning>; } }")
        checkHighlightingDumpable()
    }

    fun testMissingClosingParenFunctionCall() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say<error descr=\"Missing closing ')'\">(</error>42;")
        checkHighlightingDumpable()
    }

    fun testMissingClosingParenMethodCall() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "\$*OUT.say<error descr=\"Missing closing ')'\">(</error>42")
        checkHighlightingDumpable()
    }

    fun testMissingClosingParenExpression() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say <error descr=\"Missing closing ')'\">(</error>42 + (4 * 3);")
        checkHighlightingDumpable()
    }

    fun testMissingClosingParenLoop() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "loop <error descr=\"Missing closing ')'\">(</error>my \$i = 0; \$i < 10; \$i++ { }")
        checkHighlightingDumpable()
    }

    fun testMissingClosingParenVarDecl() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my <error descr=\"Missing closing ')'\">(</error>\$, \$")
        checkHighlightingDumpable()
    }

    fun testMissingClosingParenSignature() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "sub foo<error descr=\"Missing closing ')'\">(</error>\$, { }; foo(4)")
        checkHighlightingDumpable()
    }

    fun testMissingClosingParenCall() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$a = { .say }; \$a<error descr=\"Missing closing ')'\">(</error>42")
        checkHighlightingDumpable()
    }

    fun testMissingClosingArrayComposer() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my <weak_warning descr=\"Unused variable\">@a</weak_warning> = <error descr=\"Missing closing ']'\">[</error>1,2,3")
        checkHighlightingDumpable()
    }

    fun testMissingClosingArrayIndexer() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my @a = 1,2,3; say @a<error descr=\"Missing closing ']'\">[</error>1;")
        checkHighlightingDumpable()
    }

    fun testMissingClosingBlockoid() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our sub foo <error descr=\"Missing closing '}'\">{</error> say 42;")
        checkHighlightingDumpable()
    }

    fun testMissingClosingRegexGroup() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say 'xxx' ~~ /a <error descr=\"Missing closing ']'\">[</error> b | c /;")
        checkHighlightingDumpable()
    }

    fun testMissingClosingRegexAssertion() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say 'xxx' ~~ /a <error descr=\"Missing closing '>'\"><</error>ident /;")
        checkHighlightingDumpable()
    }

    fun testMissingClosingRegexCapture() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say 'xxx' ~~ /a <error descr=\"Missing closing ')'\">(</error> b | c /;")
        checkHighlightingDumpable()
    }

    fun testColonPairSimplification() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my \$foo = 5; sub a(:\$foo) { \$foo }; a(<warning descr=\"Pair literal can be simplified\">:foo(\$foo)</warning>)")
        checkHighlightingDumpable()
    }

    fun testFatArrowSimplification() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my \$foo = 5; <warning descr=\"Pair literal can be simplified\">foo => \$foo</warning>")
        checkHighlightingDumpable()
    }

    fun testColonPairWithBlockExpression() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my \$foo = 5; <warning descr=\"Pair literal can be simplified\">:foo{\$foo}</warning>")
        checkHighlightingDumpable()
    }

    fun testWhileOne() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<weak_warning descr=\"Idiomatic 'loop' construction can be used instead\">while</weak_warning> 1 {}")
        checkHighlightingDumpable()
    }

    fun testWhileTrue() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<weak_warning descr=\"Idiomatic 'loop' construction can be used instead\">while</weak_warning> True {}")
        checkHighlightingDumpable()
    }

    fun testWhileCondition() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "while <error descr=\"Variable \$foo is not declared\">\$foo</error> != 10 {}")
        checkHighlightingDumpable()
    }

    fun testWithAnnotation() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "if <weak_warning descr=\"'with' construction can be used instead\">5.defined</weak_warning> {}")
        checkHighlightingDumpable()
    }

    fun testWithoutAnnotation() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "unless <weak_warning descr=\"'without' construction can be used instead\">5.defined</weak_warning> {}")
        checkHighlightingDumpable()
    }

    fun testMultiWithAnnotation() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "if <weak_warning descr=\"'with' construction can be used instead\">5.defined</weak_warning> {} elsif <weak_warning descr=\"'with' construction can be used instead\">4.defined</weak_warning> {} else {}")
        checkHighlightingDumpable()
    }

    fun testGrepFirstWhateverStarAnnotation() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<warning descr=\"Can be simplified into a single first method call\">[Nil, Nil, 42, Nil].grep(*.defined).first</warning>.say")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<warning descr=\"Can be simplified into a single first method call\">[Nil, Nil, 42, Nil].grep(*.defined).first()</warning>.say()")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<warning descr=\"Can be simplified into a single first method call\">[Nil, Nil, 42, Nil].foo-call.grep(*.defined).first</warning>.say")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<warning descr=\"Can be simplified into a single first method call\">[Nil, Nil, 42, Nil].foo-call.grep(*.defined).first()</warning>.say()")
        checkHighlightingDumpable()
    }

    fun testGrepFirstBlockAnnotation() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<warning descr=\"Can be simplified into a single first method call\">[Nil, Nil, 42, Nil].grep({ \$_.foo }).first</warning>.say")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<warning descr=\"Can be simplified into a single first method call\">[Nil, Nil, 42, Nil].grep({ \$_.foo }).first()</warning>.say()")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<warning descr=\"Can be simplified into a single first method call\">[Nil, Nil, 42, Nil].foo-call.grep({ .foo }).first</warning>.say")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<warning descr=\"Can be simplified into a single first method call\">[Nil, Nil, 42, Nil].foo-call.grep({ .foo }).first()</warning>.say()")
        checkHighlightingDumpable()

        myFixture.configureByText(RakuScriptFileType.INSTANCE, ".grep(5 ~~ 10).first")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, ".grep(*.defined).first(*.bar)")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my @a = <warning descr=\"Can be simplified into a single first method call\">(1..*).grep(* > 2).first</warning>; say @a")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my @b = <warning descr=\"Can be simplified into a single first method call\">(1..*).grep({ \$_ > 2 }).first</warning>; say @b")
        checkHighlightingDumpable()
    }

    fun testSubmethodBUILDAnnotation() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class A {\n<weak_warning descr=\"'BUILD' should be declared as a submethod\">method</weak_warning> BUILD {};\n<error descr=\"Re-declaration of BUILD from aaa.raku:2\">submethod BUILD</error> {} };\nclass B {\n<weak_warning descr=\"'TWEAK' should be declared as a submethod\">method</weak_warning> TWEAK {};\n<error descr=\"Re-declaration of TWEAK from aaa.raku:5\">submethod TWEAK</error> {} };\nsub BUILD {};\nsub TWEAK {}; BUILD(); TWEAK();")
        checkHighlightingDumpable()
    }

    fun testEmptyInitializeAnnotation() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my <warning descr=\"Initialization of empty Array is redundant\">@a = []</warning>; say @a")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my <warning descr=\"Initialization of empty Array is redundant\">@b = ()</warning>; say @b")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my <warning descr=\"Initialization of empty Hash is redundant\">%a = ()</warning>; say %a")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my <warning descr=\"Initialization of empty Hash is redundant\">%b = {}</warning>; say %b")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my @c = [1,2,3]; my %c = (1,2); say @c, %c")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my @d = (1); my %d = {1}; say @d, %d")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$e = []; say \$e")
        checkHighlightingDumpable()
    }

    fun testPerl6ExecutableAnnotation() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "say <warning descr=\"If the Raku executable is meant, consider using the \$*EXECUTABLE.absolute() call that supports many platforms (e.g. GNU/Linux, Windows, etc.)\">'perl6'</warning>; run <warning descr=\"If the Raku executable is meant, consider using the \$*EXECUTABLE.absolute() call that supports many platforms (e.g. GNU/Linux, Windows, etc.)\">'perl6'</warning>; run <warning descr=\"If the Raku executable is meant, consider using the \$*EXECUTABLE.absolute() call that supports many platforms (e.g. GNU/Linux, Windows, etc.)\">\"perl6\"</warning>;")
        checkHighlightingDumpable()
    }

    fun testListAssignmentAnnotation() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my (@a, \$x); say @a, \$x")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my (@b, \$y) := 4, (1,2,3); say @b, \$y")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my (\$z, @c) = 4, (1,2,3); say @c, \$z")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my (<warning descr=\"Array slurps everything from assignment\">@a</warning>, \$x) = (1,2,3), 4; say @a, \$x")
        checkHighlightingDumpable()
    }

    fun testReturnFromNilSubroutineAnnotation() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "sub a(--> Nil) { if True { return; } }; a();")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "sub a(--> Nil) { <error descr=\"A value is returned from subroutine returning Nil\">return 42</error>; }; a();")
        checkHighlightingDumpable()
    }

    fun testUnusedSimpleLexicalAnnotation() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my <weak_warning descr=\"Unused variable\">\$x</weak_warning>; say 42;")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "sub a() { my <weak_warning descr=\"Unused variable\">\$x</weak_warning>; say 42; }; a();")
        checkHighlightingDumpable()
    }

    fun testUnusedLexicalAnnotationOnlyCoversVariableName() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my <weak_warning descr=\"Unused variable\">\$x</weak_warning> = 99; say 42;")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "sub a() { my <weak_warning descr=\"Unused variable\">\$x</weak_warning> = 100; say 42; }; a();")
        checkHighlightingDumpable()
    }

    fun testUnusedCallableLexicalAnnotation() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my <weak_warning descr=\"Unused variable\">&unused</weak_warning>; my &used = -> {}; used();")
        checkHighlightingDumpable()
    }

    fun testUnusedLexicalMultipleDeclarationAnnotation() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my (\$a, <weak_warning descr=\"Unused variable\">\$b</weak_warning>); \$a = 100; say \$a;")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "sub a() { my (<weak_warning descr=\"Unused variable\">\$a</weak_warning>, \$b); \$b = 100; say \$b; }; a();")
        checkHighlightingDumpable()
    }

    fun testUnusedRoutineParameter() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "sub foo(\$a, <weak_warning descr=\"Unused parameter\">\$b</weak_warning>) { return \$a; }; say foo(1, 2);")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "sub foo(&a, <weak_warning descr=\"Unused parameter\">&b</weak_warning>) { a() }; say foo({ 1 }, { 2 });")
        checkHighlightingDumpable()
    }

    fun testUnusedUnitRoutineParameter() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
              "unit sub MAIN(Str :o(:\$out-dir) = 'src');\n" +
              "say \$out-dir;")
        checkHighlightingDumpable()
    }

    fun testUnusedBlockParameter() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my &foo = -> \$a, <weak_warning descr=\"Unused parameter\">\$b</weak_warning> { \$a }; say foo(1, 2);")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my &bar = -> &a, <weak_warning descr=\"Unused parameter\">&b</weak_warning> { a() }; say bar({ 1 }, { 2 });")
        checkHighlightingDumpable()
    }

    fun testUnusedAttribute() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
              "class MyAttrsClass {\n" +
              "    has \$!used;\n" +
              "    has <weak_warning descr=\"Unused attribute\">\$!unused</weak_warning>;\n" +
              "    has (\$!used-g, <weak_warning descr=\"Unused attribute\">\$!unused-g</weak_warning>);\n" +
              "    method m() { \$!used, \$!used-g }\n" +
              "}")
        checkHighlightingDumpable()
    }

    fun testImplicitUsesOfMatchVariable() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                "class Actions {\n" +
                "    method a1(<weak_warning descr=\"Unused parameter\">\$/</weak_warning>) {}\n" +
                "    method a2(\$/) { make \$0.ast; }\n" +
                "    method a3(\$/) { make \$<foo>.ast; }\n" +
                "    method a4(\$/) { make ~\$/; }\n" +
                "}")
        checkHighlightingDumpable()
    }

    fun testImplicitUsesOfTopicVariable() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                "sub topic-unused(<weak_warning descr=\"Unused parameter\">\$_</weak_warning>, \$x) {\n" +
                "    given \$x { when 1 { return 99 } }\n" +
                "    42 + \$x.abs\n" +
                "}\n" +
                "sub topic-user-a(\$_) {\n" +
                "    when 42 { return \"answer\"; }\n" +
                "}\n" +
                "sub topic-user-b(Int \$_) {\n" +
                "    .abs\n" +
                "}\n" +
                "sub topic-user-c(Int \$_) {\n" +
                "    .abs.sin\n" +
                "}\n" +
                "sub topic-user-d(Int \$_ is rw) {\n" +
                "    .=abs\n" +
                "}\n" +
                "topic-unused(1, -2), topic-user-a(2), topic-user-b(3),\n" +
                "        topic-user-c(4), topic-user-d(\$ = 5)")
        checkHighlightingDumpable()
    }

    fun testUnusedPrivateMethod() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class MyPrivateMethClass {\n    method pub() { self!used }\n    method !used() {}\n    method !unused() {}\n}")
        checkHighlightingDumpable()
    }

    fun testUnusedLexicalRoutine() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "sub used(&x) { x() }\nsub unused() {}\nused(sub used-as-argument() {});")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my sub used(&x) { x() }\nmy sub unused() {}\nused(my sub used-as-argument() {});")
        checkHighlightingDumpable()
    }

    fun testDoesNotRecurse() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "(with <error descr=\"Variable \$exclude is not declared\">\$exclude</error> { 1 ~~ \$_ }), (with <error descr=\"Variable \$only-dir is not declared\">\$only-dir</error> { 3 ~~ \$_ })"
        )
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "(with <error descr=\"Variable \$a is not declared\">\$a</error> { 42 !~~ \$_}), (with <error descr=\"Variable \$b is not declared\">\$b</error> { 42 ~~ \$_});"
        )
        checkHighlightingDumpable()
    }

    fun testSelfAvailabilityAnnotation() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
              "say <error descr=\"No invocant is available here\">self</error>;\n" +
              "say <error descr=\"No invocant is available here\">\$.a</error>;\n" +
              "class C {\n" +
              "    say <error descr=\"No invocant is available here\">self</error>;\n" +
              "    say <error descr=\"No invocant is available here\">\$.a</error>;\n" +
              "    has \$.a;\n" +
              "    has \$.b = <error descr=\"Virtual method calls are not allowed on partially constructed objects\">\$.a</error>;\n" +
              "    has \$.c = self.a;\n" +
              "    method ok() {\n" +
              "        \$.a, self, sub { \$.a, self }\n" +
              "    }\n" +
              "    submethod partly-ok() {\n" +
              "        <error descr=\"Virtual method calls are not allowed on partially constructed objects\">\$.a</error>, self, sub { <error descr=\"Virtual method calls are not allowed on partially constructed objects\">\$.a</error>, self }\n" +
              "    }\n" +
              "    sub not-ok() {\n" +
              "        <error descr=\"No invocant is available here\">\$.a</error>, <error descr=\"No invocant is available here\">self</error>\n" +
              "    }\n" +
              "}")
        checkHighlightingDumpable()
    }

    fun testSelfAvailabilityInRegexDecl() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
              "grammar G {\n" +
              "    method m() {\n" +
              "        self\n" +
              "    }\n" +
              "    token t {\n" +
              "        x { self.m }\n" +
              "    }\n" +
              "}")
        checkHighlightingDumpable()
    }

    fun testUselessMethodDeclarationAnnotation() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
              "method <warning descr=\"Useless declaration of a method outside of any package\">outside-of-class</warning>() {}\n" +
              "submethod <warning descr=\"Useless declaration of a method outside of any package\">outside-of-class-s</warning>() {}\n" +
              "package p {\n" +
              "    method <warning descr=\"Useless declaration of a method in a package\">in-a-package</warning>() {}\n" +
              "    submethod <warning descr=\"Useless declaration of a method in a package\">in-a-package-s</warning>() {}\n" +
              "}\n" +
              "module m {\n" +
              "    method <warning descr=\"Useless declaration of a method in a module\">in-a-module</warning>() {}\n" +
              "    submethod <warning descr=\"Useless declaration of a method in a module\">in-a-module-s</warning>() {}\n" +
              "}")
    }

    fun testReadOnlyScalarParameterAssignmentInSub() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "sub foo(\$a, \$b is copy, \$c is rw) {\n    <error descr=\"Cannot assign to a readonly parameter\">\$a</error> = 100;\n    \$b = 200;\n    \$c = 300;\n\n    <error descr=\"Cannot assign to a readonly parameter\">\$a</error> += \$b;\n    \$b += \$c;\n    \$c += \$a;\n\n    <error descr=\"Cannot assign to a readonly parameter\">\$a</error>++;\n    \$b++;\n    \$c++;\n\n    ++<error descr=\"Cannot assign to a readonly parameter\">\$a</error>;\n    ++\$b;\n    ++\$c;\n\n    <error descr=\"Cannot assign to a readonly parameter\">\$a</error>.=sin;\n    \$b.=sin;\n    \$c.=sin;\n\n    <error descr=\"Cannot assign to a readonly parameter\">\$a</error> .= sin;\n    \$b .= sin;\n    \$c .= sin;\n}\nfoo(\$, \$, \$);")
        checkHighlightingDumpable()
    }

    fun testReadOnlyScalarParameterAssignmentWithPointyBlocks() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "for [1..10] -> \$x {\n    <error descr=\"Cannot assign to a readonly parameter\">\$x</error>++;\n}\nfor [1..10] <-> \$x {\n    \$x++;\n}")
        checkHighlightingDumpable()
    }

    fun testAssignmentToLiteral() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<error descr=\"Cannot assign to an Int Literal\">1</error> = 2; <error descr=\"Cannot assign to a Str literal\">'foo'</error> = 2; <error descr=\"Cannot assign to a Rat literal\">3.4</error> = 2; <error descr=\"Cannot assign to a Num literal\">2E3</error> = 2;")
        checkHighlightingDumpable()
    }

    fun testAssignmentToScalar() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "constant \$foo = 42; <error descr=\"Cannot assign to a constant\">\$foo</error> = 42;")
        checkHighlightingDumpable()
    }

    fun testBogusAssignment() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "sub foo() {}; <error descr=\"Cannot assign to a routine\">&foo</error> = -> {}")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<error descr=\"Cannot assign to a routine declaration\">sub foo() {}</error> = 42;")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<error descr=\"Cannot assign to a Pair literal\">(a => 42)</error> = 50;")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<error descr=\"Cannot assign to a Pair literal\">(a => 42)</error> = 50;")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<error descr=\"Cannot assign to a signature literal\">:(\$a)</error> = 55;")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my constant x = 42; <error descr=\"Cannot assign to a constant\">x</error> = 55;")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "sub foo { \$_ = 42; \$/ = 42; \$! = 42; }; foo;")
        checkHighlightingDumpable()
    }

    fun testCallArityMismatchAnnotating() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "sub worst-case { for <a> -> \\x, \\y {} }; worst-case;")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "sub ssss(:\$named, :\$shamed = 'not set') { \$named; \$shamed; }; ssss(:5named); ssss(:5named, :5shamed);")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "sub foo(\$a, \$b, \$c) { say \$a + \$b + \$c }; my @a = 1, 2; foo(0, |@a);")
        myFixture.checkHighlighting()
        myFixture.configureByFile("CallArity.pm6")
        myFixture.checkHighlighting()
        myFixture.configureByFile("CallArityExtended.pm6")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "42.perl;")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "<error descr=\"Not enough positional arguments\">open</error>;")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "open 'foo';")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$err = 42; run 'curl', 'foo', :!out, :\$err;")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "run 'curl', 'foo', <warning descr=\"Pair literal can be simplified\">out => 42</warning>, :err(42);")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$a; \$a.emit(42);")
        myFixture.checkHighlighting()
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "sub foo(\$a) is export { \$a.emit(42) }")
        myFixture.checkHighlighting()
    }

    fun testCallArityMismatchAnnotatingOnAccessorCall() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
            "class C {\n" +
            "    my enum Context <Definition>;\n" +
            "    method completion(Int \$in-level, Str \$key, %params, Bool :\$defn = False --> Str) {\n" +
            "        %params && \$defn ?? ~\$in-level !! \$key\n" +
            "    }\n" +
            "    method another(Context \$context, \$in-level) {\n" +
            "        \$.completion(\$in-level, ‘zero’, %( ), :defn(\$context == Definition))\n" +
            "    }\n" +
            "}")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class C {\n    my enum Context <Definition>;\n    method completion(Int \$in-level, Str \$key, %params, Bool :\$defn = False --> Str) {\n        %params && \$defn ?? ~\$in-level !! \$key\n    }\n    method another(\$in-level) {\n        \$<error descr=\"Not enough positional arguments\">.completion(\$in-level, ‘zero’)</error>\n    }\n}")
        checkHighlightingDumpable()
    }

    fun testUnknownRegexModifier() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my <weak_warning descr=\"Unused variable\">\$x</weak_warning> = /<error descr=\"Unrecognized regex modifier ':foo'\">:foo</error> 1234 /;\nmy <weak_warning descr=\"Unused variable\">\$y</weak_warning> = / <error descr=\"Unrecognized regex modifier ':!bar'\">:!bar</error> /;")
        checkHighlightingDumpable()
    }

    fun testDuplicatedBranch() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
            "sub ib0(\$x, \$y) is export {\n" +
            "    if \$x {\n" +
            "        1\n" +
            "    }\n" +
            "    elsif \$y {\n" +
            "        2\n" +
            "    }\n" +
            "    elsif <warning descr=\"An identical condition appears in a previous branch\">\$x</warning> {\n" +
            "        3\n" +
            "    }\n" +
            "    else {\n" +
            "        0\n" +
            "    }\n" +
            "}")
        checkHighlightingDumpable()

        myFixture.configureByText(RakuScriptFileType.INSTANCE,
            "sub ib1(\$x, \$y) is export {\n" +
            "    if \$x == \$y {\n" +
            "        1\n" +
            "    }\n" +
            "    elsif <warning descr=\"An identical condition appears in a previous branch\">\$x  == \$y</warning> {\n" +
            "        2\n" +
            "    }\n" +
            "    else {\n" +
            "        0\n" +
            "    }\n" +
            "}")
        checkHighlightingDumpable()

        myFixture.configureByText(RakuScriptFileType.INSTANCE,
            "sub ib2(\$x) is export {\n" +
            "    if \$x {\n" +
            "        2\n" +
            "    }\n" +
            "    orwith \$x {\n" +
            "        1\n" +
            "    }\n" +
            "    else {\n" +
            "        0\n" +
            "    }\n" +
            "}")
        checkHighlightingDumpable()

        myFixture.configureByText(RakuScriptFileType.INSTANCE,
            "sub ib3(\$x, \$y) is export {\n" +
            "    if \$x == \$y {\n" +
            "        1\n" +
            "    }\n" +
            "    elsif <warning descr=\"An identical condition appears in a previous branch\">\$y == \$x</warning> {\n" +
            "        2\n" +
            "    }\n" +
            "    else {\n" +
            "        0\n" +
            "    }\n" +
            "}")

        myFixture.configureByText(RakuScriptFileType.INSTANCE,
            "sub ib4(\$x, \$y) is export {\n" +
            "    if \$x < \$y {\n" +
            "        1\n" +
            "    }\n" +
            "    elsif <warning descr=\"An identical condition appears in a previous branch\">\$x > \$y</warning> {\n" +
            "        2\n" +
            "    }\n" +
            "    else {\n" +
            "        0\n" +
            "    }\n" +
            "}")
        checkHighlightingDumpable()

        myFixture.configureByText(RakuScriptFileType.INSTANCE,
            "sub ib5(\$x, \$y) is export {\n" +
            "    if \$x ≅ \$y {\n" +
            "        1\n" +
            "    }\n" +
            "    elsif <warning descr=\"An identical condition appears in a previous branch\">\$x =~= \$y</warning> {\n" +
            "        2\n" +
            "    }\n" +
            "    else {\n" +
            "        0\n" +
            "    }\n" +
            "}")
        checkHighlightingDumpable()
    }

    fun testDeprecatedSub() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<warning descr=\"foo-a is deprecated\">sub foo-a() is DEPRECATED {}</warning>\n<warning descr=\"foo-b is deprecated ... use bar\">sub foo-b() is DEPRECATED('bar') {}</warning>\nsub foo-c() {}\nfoo-a();\nfoo-b();\nfoo-c();")
        checkHighlightingDumpable()
    }

    fun testDeprecatedMethod() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class Testing {\n    method foo-a() is DEPRECATED {}\n    method foo-b() is DEPRECATED('bar') {}\n    method foo-c() {}\n}\nTesting<warning descr=\"method 'foo-a' is deprecated\">.foo-a()</warning>;\nTesting<warning descr=\"method 'foo-b' is deprecated ... use bar\">.foo-b()</warning>;\nTesting.foo-c();")
        checkHighlightingDumpable()
    }

    fun testDubiousNonHashes() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "given 42 {\n    my \$x = 99;\n    my \$y = 101;\n    say {};\n    say { ; };\n    say { \"I'm a closure\" };\n    say { :a };\n    say { <warning descr=\"Pair literal can be simplified\">a => 66</warning> };\n    say { :\$x };\n    say { :a, :b };\n    say { <warning descr=\"Pair literal can be simplified\">a => 66</warning>, <warning descr=\"Pair literal can be simplified\">b => 666</warning> };\n    say { :\$x, :\$y };\n    say { .foo };\n    say { \$_ };\n    say <weak_warning desrc=\"This will be taken as a block, not as a hash as may have been intended\">{ :\$^a }</weak_warning>;\n    say <weak_warning desrc=\"This will be taken as a block, not as a hash as may have been intended\">{ :a, :b(\$_) }</weak_warning>;\n    say <weak_warning desrc=\"This will be taken as a block, not as a hash as may have been intended\">{ <warning descr=\"Pair literal can be simplified\">a => 1</warning>, <warning descr=\"Pair literal can be simplified\">b => \$_</warning> }</weak_warning>;\n}")
        checkHighlightingDumpable()
    }

    fun testTopLevelUnused() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "say 0; <warning descr=\"Useless use of value in sink (void) context\">0;</warning> say 0;")
        checkHighlightingDumpable()
    }

    fun testBlockUnused() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "our sub foo() { say 0; <warning descr=\"Useless use of value in sink (void) context\">0;</warning> say 0; }")
        checkHighlightingDumpable()
    }

    fun testLastTopLevel() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<warning descr=\"Useless use of value in sink (void) context\">0;</warning>")
        checkHighlightingDumpable()
    }

    fun testReturnNotSpecified() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "our sub foo() { 0; }")
        checkHighlightingDumpable()
    }

    fun testReturnNil() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "our sub foo(--> Nil) { <warning descr=\"Useless use of value in sink (void) context\">0;</warning> }")
        checkHighlightingDumpable()
    }

    fun testCoreAnnotatedAsPure() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "<warning descr=\"Useless use of value in sink (void) context\">0 + 0;</warning>")
        checkHighlightingDumpable()
    }

    fun testMultiNotAnnotatedAsPure() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "multi infix:<+>(\$, \$) {};\n" +
                                  "0 + 0;")
        checkHighlightingDumpable()
    }

    fun testSubAnnotatedAsPure() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "multi infix:<+>(\$, \$) is pure {};\n" +
                                  "<warning descr=\"Useless use of value in sink (void) context\">0 + 0;</warning>")
        checkHighlightingDumpable()
    }

    fun testProtoAnnotatedAsPure() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "proto infix:<+>(\$, \$) is pure {}\n" +
                                  "multi infix:<+>(\$, \$) {}\n" +
                                  "<warning descr=\"Useless use of value in sink (void) context\">0 + 0;</warning>")
        checkHighlightingDumpable()
    }

    fun testProtoNotAnnotatedAsPure() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "proto infix:<+>(\$, \$) {}\n" +
                                  "multi infix:<+>(\$, \$) {}\n" +
                                  "0 + 0;")
        checkHighlightingDumpable()
    }

    fun testHyphenInCharacterClass() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "/<-[abc]>/; /<:L-[abc]>/; /<[-']>/;")
        checkHighlightingDumpable()
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "/<[-a..b<warning descr=\"A hyphen is used in a character class, maybe '..' was intended to denote a range? \n            Otherwise a hyphen should be at the end of the character class.\">-</warning>cd-]>/")
        checkHighlightingDumpable()
    }

    fun testImplementationDetailUsage() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "dd 42; my @a; @a.FLATTENABLE_LIST;")
        checkHighlightingDumpable()
    }
}
