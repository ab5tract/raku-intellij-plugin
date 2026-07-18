package org.raku.comma.summary

import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.psi.RakuRoutineDecl
import org.raku.comma.psi.RakuVariableDecl
import org.raku.comma.structureView.RakuStructureViewElement

class SummaryTest : CommaFixtureTestCase() {
    private fun doTestRoutine(code: String, result: String) {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, code)
        val el = PsiTreeUtil.getParentOfType(myFixture.getElementAtCaret(), RakuRoutineDecl::class.java, false)
        assertNotNull(el)
        assertEquals(result, el!!.summarySignature())
    }

    private fun doTestVariable(code: String, result: String) {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, code)
        val el = PsiTreeUtil.getParentOfType(myFixture.getElementAtCaret(), RakuVariableDecl::class.java, false)
        assertNotNull(el)
        assertEquals(result, RakuStructureViewElement(el!!).getPresentation().getPresentableText())
    }

    fun testSingleVariable() {
        doTestVariable("has \$.ab<caret>cd-abcd", "\$!abcd-abcd, \$.abcd-abcd")
    }

    fun testManyVariables() {
        doTestVariable("has (\$.a, \$.b, \$.as<caret>df, \$!qwer)", "\$!a, \$!b, \$!asdf, \$.a, \$.b, \$.asdf, \$!qwer")
    }

    fun testSingleSigil() {
        doTestRoutine("sub f<caret>oo(\$a) {}", "(\$)")
    }

    fun testMultiplySigils() {
        doTestRoutine("sub f<caret>oo(\$a, @b, %c, &d) {}", "(\$, @, %, &)")
    }

    fun testTypedVariable() {
        doTestRoutine("sub f<caret>oo(Int \$a) {}", "(Int \$)")
    }

    fun testTypedVariable2() {
        doTestRoutine("sub f<caret>oo(Int \$a, Backtrace \$foo) {}", "(Int \$, Backtrace \$)")
    }

    fun testSlurpy() {
        doTestRoutine("sub f<caret>oo(\$a, %b, *@c) {}", "(\$, %, *@)")
    }

    fun testOptional() {
        doTestRoutine("sub f<caret>oo(\$a, \$y?) {}", "(\$, \$?)")
    }

    fun testNameds() {
        doTestRoutine("sub f<caret>oo(\$a, :\$foo) {}", "(\$, :\$foo)")
        doTestRoutine("sub f<caret>oo(:\$a!, Int :\$b) {}", "(:\$a!, Int :\$b)")
    }

    fun testNamedsAlias() {
        doTestRoutine("sub f<caret>oo(:a(\$b)) {}", "(:\$a)")
        doTestRoutine("sub f<caret>oo(:a(:\$b)) {}", "(:a(:\$b))")
    }

    fun testCaptureArgs() {
        doTestRoutine("sub f<caret>oo(|c) {}", "(|)")
    }

    fun testTermArgs() {
        doTestRoutine("sub f<caret>oo(\\a) {}", "(\\a)")
    }

    fun testSubSignatures() {
        doTestRoutine("sub f<caret>oo([\$head, *@tail]) {}", "(@)")
        doTestRoutine("sub f<caret>oo((:\$x, :\$y)) {}", "(\$)")
    }

    fun testReturn() {
        doTestRoutine("sub f<caret>oo(Int \$x --> Int) {}", "(Int \$ --> Int)")
        doTestRoutine("sub f<caret>oo(Int \$x) returns Int {}", "(Int \$ --> Int)")
        doTestRoutine("sub f<caret>oo(Int \$x) of Int {}", "(Int \$ --> Int)")
    }

    fun testInvalidCases() {
        doTestRoutine("sub f<caret>oo(-- > Int) {}", "()")
        doTestRoutine("sub f<caret>oo(-->) {}", "()")
        doTestRoutine("sub f<caret>oo(Int \$a) return A {}", "(Int \$)")
        doTestRoutine("sub f<caret>oo {}", "()")
    }
}
