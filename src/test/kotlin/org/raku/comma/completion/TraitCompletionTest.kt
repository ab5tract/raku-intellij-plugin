package org.raku.comma.completion

import com.intellij.codeInsight.completion.CompletionType
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType


class TraitCompletionTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/codeInsight/localVariables"
    }

    fun testCompletionForRoutineParameter() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "sub foo(\$a is <caret>) {}")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        if (java.lang.Boolean.getBoolean("raku.test.dump.actual")) println("LOOKUP-ACTUAL ${getTestName(false)} <<<${vars.sorted()}>>>")
        assertNotNull(vars)
        assertContainsElements(vars, "rw")
        assertEquals(5, vars.size)
    }

    fun testCompletionForRoutine() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "sub foo is e<caret> {}")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        if (java.lang.Boolean.getBoolean("raku.test.dump.actual")) println("LOOKUP-ACTUAL ${getTestName(false)} <<<${vars.sorted()}>>>")
        assertNotNull(vars)
        assertContainsElements(vars, "export")
        assertEquals(12, vars.size)
    }

    fun testCompletionForMultipleTraits() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "sub foo is rw is e<caret> {}")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        if (java.lang.Boolean.getBoolean("raku.test.dump.actual")) println("LOOKUP-ACTUAL ${getTestName(false)} <<<${vars.sorted()}>>>")
        assertNotNull(vars)
        assertContainsElements(vars, "export")
        assertEquals(12, vars.size)
    }

    fun testCompletionForPackage() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "class Foo is I<caret> {}")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        if (java.lang.Boolean.getBoolean("raku.test.dump.actual")) println("LOOKUP-ACTUAL ${getTestName(false)} <<<${vars.sorted()}>>>")
        assertNotNull(vars)
        assertContainsElements(vars, "Int")
    }

    fun testExportTraitAbsenceForMyScopedVariables() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$foo is exp<caret> {}")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        if (java.lang.Boolean.getBoolean("raku.test.dump.actual")) println("LOOKUP-ACTUAL ${getTestName(false)} <<<${vars.sorted()}>>>")
        assertFalse(vars.contains("export"))
    }

    fun testExportTraitPresenceForOurScopedVariables() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "our \$foo is exp<caret> {}")
        myFixture.complete(CompletionType.BASIC, 1)
        assertNull(myFixture.getLookupElementStrings())
    }

    fun testAttributeTrait() {
        ensureModuleIsLoaded("Cro::WebApp::Form")
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "use Cro::WebApp::Form; multi sub trait_mod:<is>(Attribute \$attr, :\$panpakapan) {}; multi sub trait_mod:<is>(Attribute \$attr, :\$pan) {}; class A { has \$foo is pa<caret> }")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        if (java.lang.Boolean.getBoolean("raku.test.dump.actual")) println("LOOKUP-ACTUAL ${getTestName(false)} <<<${vars.sorted()}>>>")
        assertContainsElements(vars, "password", "package", "panpakapan", "pan")
    }

    fun testRoutineTraits() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE,
                                  "multi sub trait_mod:<is>(Routine \$attr, :\$panpakapan) {}; multi sub trait_mod:<is>(Routine \$attr, :\$pan) {}; multi sub trait_mod:<is>(Attribute \$attr, :\$pattern) {}; sub test(:\$foo!) is pa<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        val vars = myFixture.getLookupElementStrings()!!
        if (java.lang.Boolean.getBoolean("raku.test.dump.actual")) println("LOOKUP-ACTUAL ${getTestName(false)} <<<${vars.sorted()}>>>")
        assertContainsElements(vars, "panpakapan", "pan")
        assertDoesntContain(vars, "pattern")
    }
}
