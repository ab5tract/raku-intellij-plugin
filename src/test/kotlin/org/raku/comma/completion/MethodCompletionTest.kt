package org.raku.comma.completion

import com.intellij.codeInsight.completion.CompletionType
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType


class MethodCompletionTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/completion"
    }

    private fun complete(isNull: Boolean): List<String> {
        myFixture.complete(CompletionType.BASIC, 1)
        val methods = myFixture.getLookupElementStrings()
        if (java.lang.Boolean.getBoolean("raku.test.dump.actual"))
            println("LOOKUP-ACTUAL ${getTestName(false)} <<<${methods?.sorted()}>>>")
        if (isNull) {
            assertNullOrEmpty(methods)
            return emptyList()
        }
        assertNotNull(methods)
        return methods!!
    }

    private fun doTestContainsAll(text: String, vararg contains: String) {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, text)
        val methods = complete(false)
        assertContainsElements(methods, *contains)
    }

    private fun doTestContainsAllTwoFiles(fileA: String, fileB: String, vararg contains: String) {
        myFixture.configureByFiles(fileA, fileB)
        val methods = complete(false)
        assertContainsElements(methods, *contains)
    }

    private fun doTestNotContainsAll(text: String, vararg contains: String) {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, text)
        val methods = complete(false)
        assertDoesntContain(methods, *contains)
    }

    fun testMethodOnSelfCompletion() {
        doTestContainsAll("class Foo { method a{}; method b{ self.<caret> } }", ".a", ".b")
    }

    fun testMethodOnSelfFromRoleCompletion() {
        doTestContainsAll("role Foo { method a {} }; class Bar does Foo { method b{ self.<caret> } }", ".a", ".b")
    }

    fun testMethodOnSelfFromParent() {
        doTestContainsAll("class Foo { method a {} }; class Bar is Foo { method b{ self.<caret> } }", ".a", ".b")
    }

    fun testMethodOnSelfFromOuterParent() {
        doTestContainsAllTwoFiles("IdeaFoo/Bar7.pm6", "IdeaFoo/Baz.pm6", ".visible")
    }

    fun testMethodOnSelfFromParentsRole() {
        doTestContainsAll("role Role { method role {} }; class Foo does Role { method a {} }; class Bar is Foo { method b{ self.<caret> } }",
                          ".a", ".b", ".role")
    }

    fun testMethodOnSelfFromAnyInheritance() {
        doTestContainsAll("class Foo { method foo { self.<caret> } }", ".sink", ".minpairs")
    }

    fun testMethodOnTypeNameOuterFileCompletion() {
        doTestContainsAllTwoFiles("IdeaFoo/Bar4.pm6", "IdeaFoo/Baz.pm6", ".visible")
    }

    fun testMethodOnLongTypeNameOuterFileCompletion() {
        doTestContainsAllTwoFiles("IdeaFoo/Bar5.pm6", "IdeaFoo/Baz.pm6", ".visible")
    }

    fun testMethodOnTypeNameCompletion() {
        doTestContainsAll("class Foo { method a{}; method b{ Foo.<caret> } }", ".a", ".b")
    }

    fun testMethodOnTypeNameFromRoleCompletion() {
        doTestContainsAll("role Foo { method a {} }; class Bar does Foo { method b{ Bar.<caret> } }", ".a", ".b")
    }

    fun testMethodOnTypeNameFromParent() {
        doTestContainsAll("class Foo { method a {} }; class Bar is Foo { method b{ Bar.<caret> } }", ".a", ".b")
    }

    fun testMethodOnTypeNameFromParentsRole() {
        doTestContainsAll("role Role { method role {} }; class Foo does Role { method a {} }; class Bar is Foo { method b{ Bar.<caret> } }",
                          ".a", ".b", ".role")
    }

    fun testMethodOnTypeNameFromAnyInheritance() {
        doTestContainsAll("class Foo { method foo { Foo.<caret> } }", ".sink", ".minpairs")
    }

    fun testMethodOnTypeFromCORE() {
        doTestContainsAll("Int.<caret>", ".Range")
    }

    fun testMethodOnTypeFromModule() {
        ensureModuleIsLoaded("NativeCall")
        doTestContainsAll("use NativeCall; Pointer.<caret>", ".of")
    }

    fun testPrivateMethodCompletion() {
        doTestContainsAll("class Foo { method !a{}; method !b{ self!<caret> } }", "!a", "!b")
    }

    fun testPrivateMethodFromRoleCompletion() {
        doTestContainsAll("role Bar { method !bar {}; }; class Foo does Bar { method !a{ self!<caret> } }", "!a", "!bar")
    }

    fun testPrivateMethodFromOuterRoleCompletion() {
        doTestContainsAllTwoFiles("IdeaFoo/Bar6.pm6", "IdeaFoo/Baz.pm6", "!private")
    }

    fun testPrivateMethodFromNestedRoleCompletion() {
        doTestContainsAll("role Baz { method !baz {} }; role Bar does Baz { method !bar {} }; class Foo does Bar { method !a { self!<caret> } }",
                          "!a", "!bar")
    }

    fun testPrivateMethodFromExternalRoleCompletion() {
        ensureModuleIsLoaded("NativeCall")
        doTestContainsAll("use NativeCall; role Foo does NativeCall::Native { method bar { self!<caret> } }", "!setup")
    }

    fun testCorrectImportGathering() {
        ensureModuleIsLoaded("NativeCall")
        // We don't get methods from NativeCall in another block, so `!setup` is not available
        doTestNotContainsAll("class Foo { { use NativeCall; }; class Bar does NativeCall::Native { method !b {}; method !a { self!<caret> } } }", "!setup")
    }

    fun testUnknownTypeHasAnyMuMethods() {
        doTestContainsAll("UnknownTypeName.<caret>", ".note", ".reduce", ".return-rw")
    }

    fun testSubmethodCompletion() {
        doTestContainsAll("class Foo { submethod subm {}; method foo { self.<caret> } }", ".foo", ".subm")
    }

    fun testSubmethodFromParent() {
        doTestNotContainsAll("class Base { submethod subm {} }; class Foo is Base { method foo { self.<caret> } }", ".subm")
    }

    fun testSubmethodFromRole() {
        doTestContainsAll("role Base { submethod subm {} }; class Foo does Base { method foo { self.<caret> } }", ".subm")
    }

    fun testSubmethodCalledFromOutside() {
        doTestContainsAll("class Foo { submethod foo {}; method bar {} }; Foo.<caret>", ".foo", ".foo")
    }

    fun testNarrowingOfPrivateMethods() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class Foo { method !bar {}; method !foo { self!<caret> } }")
        val methods = complete(false)
        assertTrue(methods.all { it.startsWith("!") })
    }

    fun testAccessors1() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class Foo { has \$!foo; method !a { self!<caret> } }")
        complete(true)
    }

    fun testAccessors2() {
        doTestContainsAll("class Foo { has \$.foo; method a { self.<caret> } }", ".foo")
    }

    fun testAccessors3() {
        doTestContainsAll("class Foo { has \$.foo; method !x() { }; method !a { self!<caret> } }", "!a", "!x")
    }

    fun testMethodsFromParametrizedRole() {
        ensureModuleIsLoaded("NativeCall")
        doTestContainsAll("use NativeCall; role Foo does NativeCall::Native[Foo, lib-path] { method bar { self!<caret> } }", "!setup")
    }

    fun testPrivateGettersInChildClasses() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class Bar { has \$.bar }; class Foo is Bar { method a { \$!<caret> } }")
        complete(true)
    }

    fun testPublicGettersInChildClasses() {
        doTestContainsAll("class Bar { has \$.bar }; class Foo is Bar { method a { self.ba<caret> } }", ".bar")
    }

    fun testParentPrivateMethodIsPrivate() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class Bar { method !private {}; }; class Foo is Bar { method a { self!<caret> } }")
        complete(true)
    }

    fun testParentPrivateAttributeIsPrivate() {
        myFixture
            .configureByText(RakuScriptFileType.INSTANCE, "class Bar { has \$!private; }; class Foo is Bar { method a { \$!<caret> } }")
        complete(true)
    }

    fun testCOREClassMethodCompletion() {
        doTestContainsAll("class C is IO::Socket::Async { method a { self.<caret> } }", ".listen")
    }

    // Literal cases test
    fun testIntCompletion() {
        doTestContainsAll("1.<caret>", ".acosh", ".abs")
    }

    fun testRatCompletion() {
        doTestContainsAll("1.15.<caret>", ".denominator", ".polymod")
    }

    fun testNumCompletion() {
        doTestContainsAll("1e5.<caret>", ".is-prime", ".abs")
    }

    fun testStrCompletion() {
        doTestContainsAll("'Foo'.<caret>", ".match", ".samespace")
    }

    fun testComplexCompletion() {
        // FIXME `1i` is parsed as Int, not as Complex
        doTestContainsAll("1.<caret>", ".acosh", ".abs")
    }

    fun testArrayCompletion() {
        doTestContainsAll("[1, 2, 3].<caret>", ".reification-target", ".prepend")
    }

    fun testArrayEnclosedCompletion() {
        doTestContainsAll("([1, 2, 3]).<caret>", ".reification-target", ".prepend")
    }

    fun testCaptureCompletion() {
        doTestContainsAll("\\(1).<caret>", ".from-args", ".elems")
    }

    fun testColonPairCompletion() {
        doTestContainsAll(":foo.<caret>", ".freeze", ".antipair")
    }

    fun testGatherCompletion() {
        doTestContainsAll("gather {}.<caret>", ".iterator", ".lazy")
    }

    fun testStartCompletion() {
        doTestContainsAll("start {}.<caret>", ".kept", ".broken")
    }

    fun testSupplyCompletion() {
        doTestContainsAll("supply {}.<caret>", ".live", ".on-demand")
    }

    fun testVersionCompletion() {
        doTestContainsAll("(v1).<caret>", ".parts", ".plus")
    }

    fun testSpecialVariable1() {
        doTestContainsAll("\$/.<caret>", ".made", ".prematch")
    }

    fun testSpecialVariable2() {
        doTestContainsAll("\$3.<caret>", ".made", ".prematch")
    }

    fun testSpecialVariable3() {
        doTestContainsAll("\$!.<caret>", ".resume", ".backtrace")
    }

    fun testSpecialVariable4() {
        doTestContainsAll("\$<foo>.<caret>", ".made", ".prematch")
    }

    fun testTypedVar() {
        doTestContainsAll("my Exception \$foo; \$foo.<caret>", ".resume", ".backtrace")
    }

    fun testTypedVarWithSmiley() {
        doTestContainsAll("my Exception:D \$foo; \$foo.<caret>", ".resume", ".backtrace")
    }

    fun testTypedParameter() {
        doTestContainsAll("sub foo(Exception \$foo) { \$foo.<caret> }", ".resume", ".backtrace")
    }

    fun testTypedParameterWithSmiley() {
        doTestContainsAll("sub foo(Exception:D \$foo) { \$foo.<caret> }", ".resume", ".backtrace")
    }

    fun testTypedParameterWithCoercion() {
        doTestContainsAll("sub foo(Exception(Str) \$foo) { \$foo.<caret> }", ".resume", ".backtrace")
    }

    fun testTypedArrayVar() {
        doTestContainsAll("my @foo; @foo.<caret>", ".reification-target")
    }

    fun testTypedListParameter() {
        doTestContainsAll("sub foo(@foo) { @foo.<caret> }", ".reification-target")
    }

    fun testTypedHashVar1() {
        doTestContainsAll("my %foo; %foo.<caret>", ".dynamic")
    }
    fun testTypedHashVar2() {
        doTestNotContainsAll("my %foo; %foo.<caret>", ".reification-target")
    }

    fun testTypedMapParameter1() {
        doTestContainsAll("sub foo(%foo) { %foo.<caret> }",".IterationBuffer")
    }
    fun testTypedMapParameter2() {
        doTestNotContainsAll("sub foo(%foo) { %foo.<caret> }", ".reification-target")
    }

    fun testNewMethodOnTypeObjectCompletion() {
        doTestContainsAll("Int.new.<caret>", ".acos")
    }

    fun testAssignmentInference() {
        doTestContainsAll("my \$foo = Int.new; \$foo.<caret>", ".acos")
    }

    fun testAssignmentInferenceWithArguments() {
        doTestContainsAll("my \$foo = Int.new('Non-existent argument'); \$foo.<caret>", ".acos")
    }

    fun testLocalClassCase() {
        doTestContainsAll("class Cow { method moo {} }; my Cow \$c .= new; \$c.<caret>;", ".moo")
    }

    fun testBuiltInRegexCompletion() {
        doTestContainsAll("grammar A { rule a { <<caret> } }", "alpha")
    }

    fun testOwnRegexCompletion() {
        doTestContainsAll("grammar A { rule rule-a { <<caret> } }", "rule-a")
    }

    fun testInheritedRegexCompletion() {
        doTestContainsAll("grammar B { regex regex-a {''} }; grammar A  is B{ rule rule-a { <<caret> } }", "regex-a")
    }

    fun testMethodAsARuleInGrammarCompletion1() {
        doTestContainsAll("grammar B { method panic() {}; regex regex-a { <.<caret> } }", "panic")
    }

    fun testMethodAsARuleInGrammarCompletion2() {
        doTestContainsAll("grammar B { method panic() {}; regex regex-a { <.<caret> } }", "ast")
    }

    fun testGrammarFromSelfHasCursorMethods() {
        doTestContainsAll("grammar B { method panic() {}; method foo() { self.<caret> }; regex regex-a { <?> } }", ".ast", ".panic")
    }

    fun testGeneralizedMethodInferenceOnKeyword() {
        doTestContainsAll("my \$foo = start {}; \$foo.<caret>", ".keep")
    }

    fun testGeneralizedMethodInferenceOnLiteral() {
        doTestContainsAll("my \$foo = 50; \$foo.<caret>", ".acos")
    }

    fun testInferenceOnMultiPartTypeName() {
        doTestContainsAll("class Foo::Bar { method foo-bar {} }; Foo::Bar.<caret>", ".foo-bar")
    }

    fun testInferenceOnMultiPartTypedVar() {
        doTestContainsAll("class Foo::Bar { method foo-bar {} }; my Foo::Bar \$foo = Foo::Bar.new; \$foo.<caret>", ".foo-bar")
    }

    fun testAnnotatedTypeOverridesAssigned() {
        doTestContainsAll("my Int \$foo = Str.new; \$foo.<caret>", ".acos")
    }

    fun testRolesCompositionIsFlattened1() {
        doTestContainsAll("role A { has \$!foo = 5 }; role B does A {}; class C does B { method a { say \$!<caret> } }", "\$!foo")
    }

    fun testRolesCompositionIsFlattened2() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "role A { has \$!foo = 5 }; role B does A { method a { say \$!<caret> } }")
        myFixture.complete(CompletionType.BASIC, 1)
        assertNull(myFixture.getLookupElementStrings())
    }

    fun testCompletionOfMultiMethodByType() {
        doTestContainsAll("class T { multi method test-me {}; multi method test-me {} }; sub foo(T \$t) { \$t.test-m<caret> };", ".test-me")
    }

    fun testCompletionOfSubsetExternalType() {
        doTestContainsAll("subset Frame of Backtrace::Frame; my Frame \$frame; \$frame.<caret>", ".is-hidden", ".is-setting")
    }

    fun testCompletionOfSubsetLocalType() {
        doTestContainsAll("class Local { method aaaa {}; method bbbbbbb {}; }; subset Frame of Local; my Frame \$frame; \$frame.<caret>",
                          ".aaaa", ".bbbbbbb")
    }

    fun testCompletionOfSubsetUndefinedType() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "subset Frame where * > 0; my Frame \$frame; \$frame.<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val methods = myFixture.getLookupElementStrings()!!
        assertTrue(methods.size > 0)
    }

    fun testPackageWithoutNameCompletion() {
        doTestContainsAll("class { method b {}; method a { self.<caret> } }", ".a", ".b")
    }

    fun testRequireDoesNotThrow() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "require Test.<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val methods = myFixture.getLookupElementStrings()!!
        assertTrue(methods.size > 0)
    }

    fun testReturnConstraint1() {
        doTestContainsAll("class A { method b(--> A) {}; method a(--> A) { self.a.<caret> } }", ".a", ".b")
    }

    fun testReturnConstraint2() {
        doTestContainsAll("class A { method b(--> A) {}; method a(--> A) { self.a.b.<caret> } }", ".a", ".b")
    }

    fun testReturnTrait1() {
        doTestContainsAll("class A { method b returns A {}; method a returns A { self.a.<caret> } }", ".a", ".b")
    }

    fun testReturnTrait2() {
        doTestContainsAll("class A { method b returns A {}; method a returns A { self.a.b.<caret> } }", ".a", ".b")
    }

    fun testMethodReturnTypeExternal() {
        doTestContainsAll("class A { method a returns A {}; method mmmmm {} }; A.a.<caret>", ".a", ".mmmmm")
    }

    fun testMethodReturnTypeWithParentheses() {
        doTestContainsAll("class A { method a(Int \$foo) returns A { A.new; }; method mmmmm {} }; A.a(42).<caret>", ".a", ".mmmmm")
    }

    fun testSubReturnType() {
        doTestContainsAll("class C { method mmmm(--> C) { } }; sub foo(--> C) { C.new }; foo.<caret>", ".mmmm")
    }

    fun testNoExceptionForOf() {
        doTestContainsAll("class C { method mmmm() of C { } }; C.mmmm.<caret>", ".mmmm")
    }

    fun testReturnTypeNotSpecified() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class C { method mmmm { } }; C.mmmm.<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        assertNoThrowable { myFixture.getLookupElementStrings() }
    }

    fun testPrivateMethodReturnType() {
        doTestContainsAll("class C { method !bbbb {}; method !mmmm(--> C) { self!<caret> } }; >", "!mmmm", "!bbbb")
    }

    fun testAttributeTypeUsageAsCallReturn() {
        doTestContainsAll("class C { has C \$.left; method mmm(--> C) { \$.left.<caret> } }", ".mmm", ".left")
    }

    fun testTypedAttributeTypeUsageAsCallReturn() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class C { has C \$.left; method mmm(--> C) { self.left.<caret> } }")
        myFixture.complete(CompletionType.BASIC, 1)
        assertNoThrowable {
            val methods = myFixture.getLookupElementStrings()!!
            assertTrue(methods.contains(".left"))
        }
    }

    fun testTypelessAttributeInference() {
        doTestNotContainsAll("class C { has \$.a; method mmm(--> C) { self.a.<caret> } }", ".STORE_AT_KEY", ".returns")
    }

    fun testTypelessArrayAttributeInference() {
        doTestContainsAll("class C { has @.a; method mmm(--> C) { self.a.<caret> } }", ".FLATTENABLE_LIST")
    }

    fun testTypelessHashAttributeInference() {
        doTestContainsAll("class C { has %.a; method mmm(--> C) { self.a.<caret> } }", ".EXISTS-KEY")
    }

    fun testTypelessCallableAttributeInference() {
        doTestContainsAll("class C { has &.a; method mmm(--> C) { self.a.<caret> } }", ".returns")
    }

    fun testEnumValueCompletion() {
        doTestContainsAll("enum A <One Two>; say Two.<caret>", ".enums", ".acos")
    }

    fun testFullNameEnumValueCompletion() {
        doTestContainsAll("enum A <One Two>; say A::Two.<caret>", ".enums", ".acos")
    }

    fun testDynamicVariableMethodCompletion() {
        doTestContainsAll("\$*COLLATION.<caret>", ".secondary", ".set")
    }

    fun testRecursiveVariableCompletion() {
        assertNoThrowable {
            myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                      "my \$foo = \$foo.<caret>")
            myFixture.complete(CompletionType.BASIC, 1)
        }
    }

    fun testRecursiveClassDefCompletion() {
        assertNoThrowable {
            myFixture.configureByText(RakuScriptFileType.INSTANCE, "class Foo is Foo {}; Foo.<caret>")
            myFixture.complete(CompletionType.BASIC, 1)
        }
        assertNoThrowable {
            myFixture.configureByText(RakuScriptFileType.INSTANCE, "class Foo does Foo {}; Foo.<caret>")
            myFixture.complete(CompletionType.BASIC, 1)
        }
    }

    fun testPrivateMethodsAreNotVisibleFromOutside() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class A { method !test2 {}; method !test {} }; A!<caret>;")
        complete(true)
    }

    fun testPrivateMethodsAreNotLeakedIntoLexicalClasses() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class A { method !test {}; my class B { method a { self!<caret> } } }")
        complete(true)
    }

    fun testAccessorsPrivacy1() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "class A { has \$.test; my class B { method a { self!te<caret> } } }")
        complete(true)
    }

    fun testAccessorsPrivacy2() {
        doTestNotContainsAll("class A { has \$.test; my class B { method a { self.tes<caret> } } }", ".test")
    }

    fun testReturnTypeBasedExternal() {
        doTestContainsAll("42.abs.<caret>", ".polymod", ".chr")
    }

    fun testCompletionIsInOrder() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class A { method mmm {} }; class B is A { method m {} }; B.<caret>")
        val methods = complete(false)
        assertEquals(".m", methods.get(0))
        assertEquals(".mmm", methods.get(1))
    }

    fun testNativeTypesCompletionDoNotThrow() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "my atomicint \$t; \$t.<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        assertNoThrowable { myFixture.getLookupElementStrings() }
    }

    fun testMethodOnExplicitlyTypedTopicCompletion() {
        doTestContainsAll("class Foo { has \$.aaa; has \$.bbb; }\n" +
                          "sub foo(Foo \$_) { .<caret> }",
                          ".aaa", ".bbb")
    }

    fun testMethodOnTopicSetByGivenCompletion() {
        doTestContainsAll("class Foo { has \$.aaa; has \$.bbb; }\n" +
                        "sub foo() { given Foo.new { .<caret> } }",
                ".aaa", ".bbb")
    }

    fun testMethodOnTopicSetByWithCompletion() {
        doTestContainsAll("class Foo { has \$.aaa; has \$.bbb; }\n" +
                        "sub foo() { with Foo.new { .<caret> } }",
                ".aaa", ".bbb")
    }

    fun testMethodOnTopicSetByWithoutCompletion() {
        doTestContainsAll("class Foo { has \$.aaa; has \$.bbb; }\n" +
                        "sub foo() { without Foo.new { .<caret> } }",
                ".aaa", ".bbb")
    }

    fun testMethodOnTopicSetByGivenNotHiddenBySignaturedGivenCompletion() {
        doTestContainsAll("class Foo { has \$.aaa; has \$.bbb; }\n" +
                        "sub foo() { given Foo.new { given 42 -> \$x { .<caret> } } }",
                ".aaa", ".bbb")
    }

    fun testMethodOnTopicSetByGivenNotHiddenBySignaturedWithCompletion() {
        doTestContainsAll("class Foo { has \$.aaa; has \$.bbb; }\n" +
                        "sub foo() { given Foo.new { with 42 -> \$x { .<caret> } } }",
                ".aaa", ".bbb")
    }

    fun testMethodOnTopicSetByGivenNotHiddenBySignaturedWithoutCompletion() {
        doTestContainsAll("class Foo { has \$.aaa; has \$.bbb; }\n" +
                        "sub foo() { given Foo.new { without 42 -> \$x { .<caret> } } }",
                ".aaa", ".bbb")
    }

    fun testMethodOnTopicSetByCatchCompletion() {
        doTestContainsAll("CATCH { default { .<caret> } }",
                ".resume")
    }

    fun testMethodOnTopicSetByControlCompletion() {
        doTestContainsAll("CONTROL { default { .<caret> } }",
                ".resume")
    }

    fun testMethodOnTopicCompletionRefinedByWhen() {
        doTestContainsAll("CATCH { when X::NYI { .<caret> } }",
                ".resume", ".feature")
    }

    fun testDeferredDefinition() {
        doTestContainsAll("class Foo {...}; Foo.m<caret>; class Foo { method mm {}; method mmm {}; method mmmm {} }",
                          ".mm", ".mmm", ".mmmm")
    }

    fun testTypedArrayForLoopTopicVariable() {
        doTestContainsAll(
            "class Pivo {\n" +
            "    has Str \$.brewery;\n" +
            "    has Int \$.ibu;\n" +
            "}\n" +
            "my Pivo @piva;\n" +
            "for @piva {\n" +
            "    .<caret>\n" +
            "}",
  ".brewery", ".ibu")
    }

    fun testTypedArrayForLoopTopicParameter() {
        doTestContainsAll(
            "class Pivo {\n" +
            "    has Str \$.brewery;\n" +
            "    has Int \$.ibu;\n" +
            "}\n" +
            "sub param(Pivo @piva) {\n" +
            "    for @piva {\n" +
            "        .<caret>\n" +
            "    }\n" +
            "}",
            ".brewery", ".ibu")
    }

    fun testTypedArrayForLoopTopicAttribute() {
        doTestContainsAll(
            "class Pivo {\n" +
            "    has Str \$.brewery;\n" +
            "    has Int \$.ibu;\n" +
            "}\n" +
            "class Store {\n" +
            "    has Pivo @.piva;\n" +
            "    method m() {\n" +
            "        for @!piva {\n" +
            "            .<caret>\n" +
            "        }\n" +
            "    }\n" +
            "}",
            ".brewery", ".ibu")
    }

    fun testTypedArrayForLoopTopicAccessor() {
        doTestContainsAll(
            "class Pivo {\n" +
            "    has Str \$.brewery;\n" +
            "    has Int \$.ibu;\n" +
            "}\n" +
            "class Store {\n" +
            "    has Pivo @.piva;\n" +
            "}\n" +
            "for Store.new.piva {\n" +
            "    .<caret>\n" +
            "}",
            ".brewery", ".ibu")
    }

    fun testTypedArrayForLoopParameterVariable() {
        doTestContainsAll(
            "class Pivo {\n" +
            "    has Str \$.brewery;\n" +
            "    has Int \$.ibu;\n" +
            "}\n" +
            "my Pivo @piva;\n" +
            "for @piva -> \$pivo {\n" +
            "    \$pivo.<caret>\n" +
            "}",
            ".brewery", ".ibu")
    }

    fun testTypedArrayForLoopParameterParameter() {
        doTestContainsAll(
            "class Pivo {\n" +
            "    has Str \$.brewery;\n" +
            "    has Int \$.ibu;\n" +
            "}\n" +
            "sub param(Pivo @piva) {\n" +
            "    for @piva -> \$pivo {\n" +
            "        \$pivo.<caret>\n" +
            "    }\n" +
            "}",
            ".brewery", ".ibu")
    }

    fun testTypedArrayForLoopParameterAttribute() {
        doTestContainsAll(
            "class Pivo {\n" +
            "    has Str \$.brewery;\n" +
            "    has Int \$.ibu;\n" +
            "}\n" +
            "class Store {\n" +
            "    has Pivo @.piva;\n" +
            "    method m() {\n" +
            "        for @!piva -> \$pivo {\n" +
            "            \$pivo.<caret>\n" +
            "        }\n" +
            "    }\n" +
            "}",
            ".brewery", ".ibu")
    }

    fun testTypedArrayForLoopParameterAccessor() {
        doTestContainsAll(
            "class Pivo {\n" +
            "    has Str \$.brewery;\n" +
            "    has Int \$.ibu;\n" +
            "}\n" +
            "class Store {\n" +
            "    has Pivo @.piva;\n" +
            "}\n" +
            "for Store.new.piva -> \$pivo {\n" +
            "    \$pivo.<caret>\n" +
            "}",
            ".brewery", ".ibu")
    }

    fun testUntypedHashIterationGivePairs() {
        doTestContainsAll(
            "my %h;\n" +
            "for %h {\n" +
            "    .<caret>\n" +
            "}",
            ".key", ".value")
    }

    fun testResolutionDoesNotDependOnTypeBeingInScope() {
        doTestContainsAll(
            "class Owner {\n" +
            "    my class Inner {\n" +
            "        has \$.some-attr;\n" +
            "    }\n" +
            "    method m(--> Inner) {\n" +
            "        Inner.new\n" +
            "    }\n" +
            "}\n" +
            "Owner.m.<caret>",
            ".some-attr")
    }

    fun testMetaMethodCompletion() {
        ensureModuleIsLoaded("OO::Monitors")
        doTestContainsAll("Int.^me<caret>", ".^methods")
        doTestContainsAll("use OO::Monitors; monitor Foo {}; Foo.^met<caret>", ".^methods")
        // Modern OO::Monitors has no add_condition; setup_monitor is the
        // MonitorHOW-specific meta-method that proves the metaclass resolved.
        doTestContainsAll("use OO::Monitors; monitor Foo { method add {}; }; Foo.^ad<caret>", ".^add_method", ".^add_conc_to_cache")
        doTestContainsAll("use OO::Monitors; monitor Foo { method add {}; }; Foo.^set<caret>", ".^setup_monitor")
        doTestNotContainsAll("{ use OO::Monitors; monitor Foo { method add {}; }; }; { monitor Foo {}; Foo.^set<caret> }", ".^setup_monitor")
        doTestContainsAll("{ use OO::Monitors; monitor Foo { method add {}; }; }; { monitor Foo {}; Foo.^ad<caret> }", ".^add_method")
    }

    fun testCompletionUsingIsTypeNameOnVariableDeclaration() {
        doTestContainsAll("my %h is SetHash; %h.g<caret>", ".grab", ".grabpairs")
    }

    fun testCompletionUsingOfTypeToSupplyArrayElementType() {
        doTestContainsAll("my @a of Int; for @a { .m<caret> }", ".msb")
    }
}