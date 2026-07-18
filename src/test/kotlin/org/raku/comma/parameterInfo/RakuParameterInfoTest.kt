package org.raku.comma.parameterInfo

import com.intellij.testFramework.utils.parameterInfo.MockCreateParameterInfoContext
import com.intellij.testFramework.utils.parameterInfo.MockParameterInfoUIContext
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.RakuParameterInfoHandler
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.psi.RakuRoutineDecl

private typealias UICheck = (MockParameterInfoUIContext<*>) -> Unit

class RakuParameterInfoTest : CommaFixtureTestCase() {
    private fun doTest(text: String, args: String, vararg checks: UICheck) {
        val builder = StringBuilder()
        for (signature in text.split(" ||| ")) {
            builder.append(String.format("multi a(%s); ", signature))
        }
        builder.append("a(").append(args).append("<caret>")
        doTest(builder.toString(), *checks)
    }

    private fun doTest(text: String, vararg checks: UICheck) {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, text)
        val createContext = MockCreateParameterInfoContext(myFixture.editor, myFixture.file)
        val owner = HANDLER.findElementForParameterInfo(createContext)
        HANDLER.showParameterInfo(owner!!, createContext)
        val items = createContext.itemsToShow
        assertNotNull(items)
        assertTrue(items!!.isNotEmpty())
        val uiContext = MockParameterInfoUIContext(owner)
        assertEquals(items.size, checks.size)
        for (i in items.indices) {
            HANDLER.updateUI(items[i] as RakuRoutineDecl, uiContext)
            checks[i](uiContext)
        }
    }

    private fun assertParameterInfo(
        context: MockParameterInfoUIContext<*>,
        isEnabled: Boolean,
        text: String,
        start: Int,
        end: Int,
    ) {
        assertEquals(text, context.text)
        assertEquals(isEnabled, context.isUIComponentEnabled)
        assertEquals(start, context.highlightStart)
        assertEquals(end, context.highlightEnd)
    }

    fun testPosVsNamedSingle() {
        doTest("\$a ||| :\$foo", "",
               { context -> assertParameterInfo(context, true, "\$a", 0, 2) },
               { context -> assertParameterInfo(context, true, ":\$foo", 0, 5) })
    }

    fun testSingleArg() {
        doTest("\$a ||| :\$b", "42,",
               { context -> assertParameterInfo(context, true, "\$a", 0, 0) },
               { context -> assertParameterInfo(context, false, ":\$b", 0, 0) })
    }

    fun testNamedIsAnticipated() {
        doTest("\$a ||| \$a, :\$b", "42,",
               { context -> assertParameterInfo(context, true, "\$a", 0, 0) },
               { context -> assertParameterInfo(context, true, "\$a, :\$b", 4, 7) })
    }

    fun testOptionalIsAnticipated() {
        doTest("\$a ||| \$a, \$b?", "42,",
               { context -> assertParameterInfo(context, true, "\$a", 0, 0) },
               { context -> assertParameterInfo(context, true, "\$a, \$b?", 4, 7) })
    }

    fun testSlurpyIsAnticipated() {
        doTest("\$a, *@b ||| \$a, \$b", "42, 43, 44,",
               { context -> assertParameterInfo(context, true, "\$a, *@b", 4, 7) },
               { context -> assertParameterInfo(context, false, "\$a, \$b", 0, 0) })
    }

    fun testMethodParameterInfo() {
        doTest("class A { multi method a(\$a) {}; multi method a(\$a, :\$foo) {}; multi method a(:\$best) { self.a(:!best<caret> } }; ",
               { context -> assertParameterInfo(context, true, "\$a", 0, 2) },
               { context -> assertParameterInfo(context, true, "\$a, :\$foo", 0, 2) },
               { context -> assertParameterInfo(context, true, ":\$best", 0, 6) })
    }

    fun testOffset() {
        doTest("Int \$tran, Int \$dataset, Str \$module-key, Hash :\$defaults?, :\$apply-defaults = True --> Int", "1, 2,",
               { context -> assertParameterInfo(context, true, "Int \$tran, Int \$dataset, Str \$module-key, Hash :\$defaults?, :\$apply-defaults = True", 25, 40) })
    }

    fun testJumping() {
        doTest("\$a, \$b, \$asdf", "1, 3",
               { context -> assertParameterInfo(context, true, "\$a, \$b, \$asdf", 4, 6) })
    }

    fun testSyntheticConstructor() {
        // Synthetic constructor is disabled if explicit one
        doTest("class A { has \$.foo; has \$.bar; method new(\$a, \$b, :\$c) {} }; A.new(<caret>)",
               { context -> assertParameterInfo(context, true, "\$a, \$b, :\$c", 0, 2) })
        // Synthetic constructor is disabled if explicit is in parent
        doTest("class A { method new(\$a, \$b) {} }; class B is A { has \$.foo; has \$.bar; }; B.new(<caret>)",
               { context -> assertParameterInfo(context, true, "\$a, \$b", 0, 2) })
        // Synthetic constructor is enabled
        doTest("class A { has \$.foo; has Str \$.bar; method test(\$a, \$b, :\$c) {} }; A.new(<caret>)",
               { context -> assertParameterInfo(context, true, "Any :\$foo, Str :\$bar", 0, 9) })
    }

    companion object {
        val HANDLER = RakuParameterInfoHandler()
    }
}
