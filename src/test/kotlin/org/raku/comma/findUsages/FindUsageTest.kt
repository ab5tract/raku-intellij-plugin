package org.raku.comma.findUsages

import com.intellij.usageView.UsageInfo
import org.raku.comma.CommaFixtureTestCase

import java.util.Collection

class FindUsageTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/findUsage"
    }

    fun testVariableDefinition() {
        val usageInfos = myFixture.testFindUsages("VariableDefinition.p6")
        assertEquals(3, usageInfos.size)
    }

    fun testVariable() {
        val usageInfos = myFixture.testFindUsages("Variable.p6")
        assertEquals(3, usageInfos.size)
    }

    fun testOuterVariable1() {
        myFixture.configureByFiles("IdeaFoo/User.pm6", "IdeaFoo/Base.pm6")
        val usages = myFixture.findUsages(myFixture.getElementAtCaret())
        assertEquals(2, usages.size)
    }

    fun testOuterVariable2() {
        myFixture.configureByFiles("IdeaFoo/Base.pm6", "IdeaFoo/User.pm6")
        val usages = myFixture.findUsages(myFixture.getElementAtCaret())
        assertEquals(2, usages.size)
    }

    fun testVariablesInBlock() {
        val usageInfos = myFixture.testFindUsages("VariableBlock.p6")
        assertEquals(3, usageInfos.size)
    }

    fun testVariableFromParameter1() {
        val usageInfos = myFixture.testFindUsages("VariableFromParameter.p6")
        assertEquals(3, usageInfos.size)
    }

    fun testVariableFromParameter2() {
        val usageInfos = myFixture.testFindUsages("VariableFromParameterOnDeclaration.p6")
        assertEquals(3, usageInfos.size)
    }

    fun testVariableWithDashFromParameter() {
        val usageInfos = myFixture.testFindUsages("VariableWithDashFromParameter.p6")
        assertEquals(3, usageInfos.size)
    }

    fun testPrivateAttributeOfClass() {
        val usageInfos = myFixture.testFindUsages("AttributeOfClass.p6")
        assertEquals(3, usageInfos.size)
    }

    fun testPrivateAttributeOfClass1() {
        val usageInfos = myFixture.testFindUsages("AttributeOfClass1.p6")
        assertEquals(3, usageInfos.size)
    }

    fun testPrivateAttributeFromRole() {
        val usageInfos = myFixture.testFindUsages("AttributeFromRole.p6")
        assertEquals(3, usageInfos.size)
    }

    fun testPrivateAttributeFromRole1() {
        val usageInfos = myFixture.testFindUsages("AttributeFromRole1.p6")
        assertEquals(3, usageInfos.size)
    }

    fun testPublicAttributeFromOuterRole() {
        myFixture.configureByFiles("IdeaFoo2/Base.pm6", "IdeaFoo2/User.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(24)
        val usages = myFixture.findUsages(myFixture.getElementAtCaret())
        assertEquals(4, usages.size)
    }

    fun testPublicAttributeWithDash() {
        val usageInfos = myFixture.testFindUsages("AttributeWithDash.p6")
        assertEquals(5, usageInfos.size)
    }

    fun testPrivateAttributeFromOuterRole() {
        myFixture.configureByFiles("IdeaFoo2/Base.pm6", "IdeaFoo2/User.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(44)
        val usages = myFixture.findUsages(myFixture.getElementAtCaret())
        assertEquals(5, usages.size)
    }

    fun testComposedAndInherited() {
        val usageInfos = myFixture.testFindUsages("RoleClassAttribute.p6")
        assertEquals(3, usageInfos.size)
    }

    fun testPrivateMethod() {
        val usageInfos = myFixture.testFindUsages("PrivateMethod.p6")
        assertEquals(4, usageInfos.size)
    }

    fun testPrivateMethodFromRole() {
        val usageInfos = myFixture.testFindUsages("PrivateMethodFromRole.p6")
        assertEquals(4, usageInfos.size)
    }

    fun testPrivateMethodFromRoleOverloaded() {
        val usageInfos = myFixture.testFindUsages("PrivateMethodFromRoleOverloaded.p6")
        assertEquals(2, usageInfos.size)
    }

    fun testSub() {
        val usageInfos = myFixture.testFindUsages("Sub.p6")
        assertEquals(4, usageInfos.size)
    }

    fun testOuterSub1() {
        myFixture.configureByFiles("IdeaFoo3/Base.pm6", "IdeaFoo3/User.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(6)
        val usages = myFixture.findUsages(myFixture.getElementAtCaret())
        assertEquals(4, usages.size)
    }

    fun testOuterSub2() {
        myFixture.configureByFiles("IdeaFoo3/Base.pm6", "IdeaFoo3/User.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(39)
        val usages = myFixture.findUsages(myFixture.getElementAtCaret())
        assertEquals(2, usages.size)
    }

    fun testTypeDefinition() {
        val usageInfos = myFixture.testFindUsages("TypeDefinition.p6")
        assertEquals(2, usageInfos.size)
    }

    fun testType() {
        val usageInfos = myFixture.testFindUsages("Type.p6")
        assertEquals(2, usageInfos.size)
    }

    fun testRole() {
        myFixture.configureByFiles("IdeaFoo4/Base.pm6", "IdeaFoo4/User.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(7)
        val usages = myFixture.findUsages(myFixture.getElementAtCaret())
        assertEquals(9, usages.size)
    }

    fun testGrammar() {
        val usageInfos = myFixture.testFindUsages("Grammar.p6")
        assertEquals(2, usageInfos.size)
    }

    fun testMonitor() {
        val usageInfos = myFixture.testFindUsages("Monitor.p6")
        assertEquals(2, usageInfos.size)
    }

    fun testSubsetOuter1() {
        myFixture.configureByFiles("IdeaFoo5/Base.pm6", "IdeaFoo5/User.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(8)
        val usages = myFixture.findUsages(myFixture.getElementAtCaret())
        assertEquals(2, usages.size)
    }

    fun testSubsetOuter2() {
        myFixture.configureByFiles("IdeaFoo5/Base.pm6", "IdeaFoo5/User.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(50)
        val usages = myFixture.findUsages(myFixture.getElementAtCaret())
        assertEquals(2, usages.size)
    }

    fun testEnum() {
        myFixture.configureByFiles("IdeaFoo6/Base.pm6", "IdeaFoo6/User.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(7)
        val usages = myFixture.findUsages(myFixture.getElementAtCaret())
        assertEquals(6, usages.size)
    }

    fun testMultiPartNameOuter() {
        myFixture.configureByFiles("IdeaFoo7/Base.pm6", "IdeaFoo7/User.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(8)
        val usages = myFixture.findUsages(myFixture.getElementAtCaret())
        assertEquals(1, usages.size)
    }

    fun testMultiPartNameMiddle() {
        myFixture.configureByFiles("IdeaFoo7/Base.pm6", "IdeaFoo7/User.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(24)
        val usages = myFixture.findUsages(myFixture.getElementAtCaret())
        assertEquals(1, usages.size)
    }

    fun testMultiPartNameInner() {
        myFixture.configureByFiles("IdeaFoo7/Base.pm6", "IdeaFoo7/User.pm6")
        myFixture.getEditor().getCaretModel().moveToOffset(43)
        val usages = myFixture.findUsages(myFixture.getElementAtCaret())
        assertEquals(2, usages.size)
    }

    fun testMultiPartNameGluedCase() {
        myFixture.configureByFiles("IdeaFoo8/User.pm6", "IdeaFoo8/Base.pm6")
        val usages = myFixture.findUsages(myFixture.getElementAtCaret())
        assertEquals(2, usages.size)
    }

    fun testRegexDeclarationOffsetPresence() {
        val usageInfos = myFixture.testFindUsages("RegexDecl.pm6")
        assertEquals(2, usageInfos.size)
    }
}
