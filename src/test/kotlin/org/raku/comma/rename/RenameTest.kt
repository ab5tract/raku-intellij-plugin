package org.raku.comma.rename

import org.raku.comma.CommaFixtureTestCase

class RenameTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/rename"
    }

    fun testRenameOfPrivateMethodFromName() {
        myFixture.configureByFile("PrivateMethods.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(28)
        myFixture.renameElementAtCaret("!private-method")
        myFixture.checkResultByFile("PrivateMethodsAfter.pm6")
    }

    fun testRenameOfPrivateMethodFromNameWithdash() {
        myFixture.configureByFile("PrivateMethodsWithDash.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(28)
        myFixture.renameElementAtCaret("!private-method")
        myFixture.checkResultByFile("PrivateMethodsAfter.pm6")
    }

    fun testRenameOfPrivateMethodFromCall() {
        myFixture.configureByFile("PrivateMethods.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(136)
        myFixture.renameElementAtCaret("!private-method")
        myFixture.checkResultByFile("PrivateMethodsAfter.pm6")
    }

    fun testRenameOfSubFromName() {
        myFixture.configureByFile("Subroutines.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(5)
        myFixture.renameElementAtCaret("renamed-sub")
        myFixture.checkResultByFile("SubroutinesAfter.pm6")
    }

    fun testRenameOfSubFromCall() {
        myFixture.configureByFile("Subroutines.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(47)
        myFixture.renameElementAtCaret("renamed-sub")
        myFixture.checkResultByFile("SubroutinesAfter.pm6")
    }

    fun testRenameOfSubFromCallUppercase() {
        myFixture.configureByFile("Subroutines.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(47)
        myFixture.renameElementAtCaret("Renamed-sub")
        myFixture.checkResultByFile("SubroutinesUppercaseAfter.pm6")
    }

    fun testRenameOfSubFromIndirectName() {
        myFixture.configureByFile("Subroutines.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(71)
        myFixture.renameElementAtCaret("renamed-sub")
        myFixture.checkResultByFile("SubroutinesAfter.pm6")
    }

    fun testRenameOfTypeFromDeclaration() {
        myFixture.configureByFile("Type.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(10)
        myFixture.renameElementAtCaret("FooNewType")
        myFixture.checkResultByFile("TypeAfter.pm6")
    }

    fun testRenameOfTypeFromDeclarationIntoLowercase() {
        myFixture.configureByFile("Type.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(10)
        myFixture.renameElementAtCaret("fooNewType")
        myFixture.checkResultByFile("TypeAfterLowercase.pm6")
    }

    fun testRenameOfTypeFromUsage() {
        myFixture.configureByFile("Type.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(37)
        myFixture.renameElementAtCaret("FooNewType")
        myFixture.checkResultByFile("TypeAfter.pm6")
    }

    fun testRenameOfTypeFromReturn() {
        myFixture.configureByFile("Type.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(73)
        myFixture.renameElementAtCaret("FooNewType")
        myFixture.checkResultByFile("TypeAfter.pm6")
    }

    fun testRenameOfTypeFromTrait() {
        myFixture.configureByFile("Type.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(86)
        myFixture.renameElementAtCaret("FooNewType")
        myFixture.checkResultByFile("TypeAfter.pm6")
    }

    fun testRenameOfTypeFromIsTrait() {
        myFixture.configureByFile("Type.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(163)
        myFixture.renameElementAtCaret("FooNewType")
        myFixture.checkResultByFile("TypeAfter.pm6")
    }

    fun testRenameOfMultiPartTypeFromUsage() {
        myFixture.configureByFile("MultiPartType.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(45)
        myFixture.renameElementAtCaret("Foo::Baz")
        myFixture.checkResultByFile("MultiPartTypeAfter.pm6")
    }

    // TODO
    //public void testRenameOfMultiPartNestedTypeFromUsage() {
    //    myFixture.configureByFile("MultiPartTypeNested.pm6")
    //    myFixture.getEditor().getCaretModel().moveToOffset(138)
    //    myFixture.renameElementAtCaret("Foo::Bartender")
    //    myFixture.checkResultByFile("MultiPartTypeNestedAfter.pm6")
    //}

    fun testMultiFileSubRename() {
        myFixture.configureByFiles("IdeaFoo/Base.pm6", "IdeaFoo/User.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(9)
        myFixture.renameElementAtCaret("foo-bar-sub-new")
        myFixture.checkResultByFile("IdeaFoo/Base.pm6", "IdeaFoo/BaseAfter.pm6", true)
        myFixture.checkResultByFile("IdeaFoo/User.pm6", "IdeaFoo/UserAfter.pm6", true)
    }

    fun testLocalVariablesWithExportRename() {
        myFixture.configureByFiles("IdeaFoo2/Base.pm6", "IdeaFoo2/User.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(36)
        myFixture.renameElementAtCaret("\$foo-baz")
        myFixture.checkResultByFile("IdeaFoo2/Base.pm6", "IdeaFoo2/BaseAfter.pm6", true)
        myFixture.checkResultByFile("IdeaFoo2/User.pm6", "IdeaFoo2/UserAfter.pm6", true)
    }

    fun testParameterRename() {
        myFixture.configureByFile("Parameter.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(54)
        myFixture.renameElementAtCaret("\$just-parameter")
        myFixture.checkResultByFile("ParameterAfter.pm6")
    }

    fun testArraySigilVariableRename() {
        myFixture.configureByFile("ArraySigil.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(29)
        myFixture.renameElementAtCaret("@my-array")
        myFixture.checkResultByFile("ArraySigilAfter.pm6")
    }

    fun testPrivateAttributeRename() {
        myFixture.configureByFile("PrivateAttribute.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(137)
        myFixture.renameElementAtCaret("@!two")
        myFixture.checkResultByFile("PrivateAttributeAfter.pm6")
    }

    fun testPublicAttributeRename() {
        myFixture.configureByFiles("IdeaFoo3/Base.pm6", "IdeaFoo3/User.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(146)
        myFixture.renameElementAtCaret("\$.renamed-attribute")
        myFixture.checkResultByFile("IdeaFoo3/Base.pm6", "IdeaFoo3/BaseAfter.pm6", true)
        myFixture.checkResultByFile("IdeaFoo3/User.pm6", "IdeaFoo3/UserAfter.pm6", true)
    }

    fun testModuleRename() {
        myFixture.configureByFiles("IdeaFoo4/User.pm6", "IdeaFoo4/Base.pm6")
        myFixture.renameElementAtCaret("IdeaFoo4::BaseModule")
        myFixture.checkResultByFile("IdeaFoo4/BaseModule.pm6", "IdeaFoo4/Base.pm6",true)
        myFixture.checkResultByFile("IdeaFoo4/User.pm6", "IdeaFoo4/UserAfter.pm6",true)
    }

    fun testModuleRenameWithPathGrow() {
        myFixture.configureByFiles("IdeaFoo4/User.pm6", "IdeaFoo4/Base.pm6")
        myFixture.renameElementAtCaret("IdeaFoo4::BaseModule::More")
        myFixture.checkResultByFile("IdeaFoo4/BaseModule/More.pm6", "IdeaFoo4/Base.pm6",true)
        myFixture.checkResultByFile("IdeaFoo4/User.pm6", "IdeaFoo4/UserAfterGrow.pm6",true)
    }

    fun testModuleRenameWithPathShrink() {
        myFixture.configureByFiles("IdeaFoo4/User.pm6", "IdeaFoo4/Base.pm6")
        myFixture.renameElementAtCaret("IdeaFoo4")
        myFixture.checkResultByFile("IdeaFoo4.pm6", "IdeaFoo4/Base.pm6",true)
        myFixture.checkResultByFile("IdeaFoo4/User.pm6", "IdeaFoo4/UserAfterShrink.pm6",true)
    }

    fun testModuleMoveCausesRename() {
        // Dummy is necessary for `New` directory to be created
        myFixture.configureByFiles("IdeaFoo4/User.pm6", "IdeaFoo4/Base.pm6", "IdeaFoo4/New/Dummy.pm6")
        myFixture.moveFile("IdeaFoo4/Base.pm6", "IdeaFoo4/New")
        myFixture.checkResultByFile("IdeaFoo4/User.pm6", "IdeaFoo4/UserMovedBase.pm6", true)
    }

    fun testGrammarRuleRename() {
        myFixture.configureByFile("GrammarRule.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(52)
        myFixture.renameElementAtCaret("foo-bar")
        myFixture.checkResultByFile("GrammarRuleAfter.pm6")
    }

    fun testAttributeWithHyphenRename() {
        myFixture.configureByFile("AttrWithHyphen.pm6")
        myFixture.renameElementAtCaret("&.some-co")
        myFixture.getEditor().getCaretModel().moveToOffset(21)
        myFixture.renameElementAtCaret("&.some-code")
        myFixture.checkResultByFile("AttrWithHyphenAfter.pm6")
    }
}
