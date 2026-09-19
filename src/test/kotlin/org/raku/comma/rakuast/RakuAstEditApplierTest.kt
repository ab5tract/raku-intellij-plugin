package org.raku.comma.rakuast

import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

class RakuAstEditApplierTest : CommaFixtureTestCase() {

    fun testReplacesOnlyTheNodeSpan() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say 1;\nmy \$x = 41;\n")
        // "41" sits at offset 16 in the file; the snippet began at offset 7.
        val result = EditResult(text = "99", span = AstSpan(from = 8, to = 10))

        val changed = RakuAstEditApplier.apply(project, myFixture.editor, 7, result)

        assertTrue(changed)
        assertEquals("say 1;\nmy \$x = 99;\n", myFixture.editor.document.text)
    }

    // Everything outside the edited node must survive byte-for-byte, including
    // comments, which DEPARSE cannot reproduce. This guards against replacement
    // widening in either direction, which would silently lose what DEPARSE cannot restore.
    fun testCommentsAndFormattingOutsideTheNodeSurvive() {
        val original = "my  \$x   =   41;   # keep me\n# and me\n"
        myFixture.configureByText(RakuScriptFileType.INSTANCE, original)
        val result = EditResult(text = "99", span = AstSpan(from = 13, to = 15))

        RakuAstEditApplier.apply(project, myFixture.editor, 0, result)

        val expected = "my  \$x   =   99;   # keep me\n# and me\n"
        assertEquals(expected, myFixture.editor.document.text)
    }

    fun testRefusesResultCarryingAnError() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 41;")
        val before = myFixture.editor.document.text

        val changed = RakuAstEditApplier.apply(
            project, myFixture.editor, 0, EditResult(error = "nope"))

        assertFalse(changed)
        assertEquals(before, myFixture.editor.document.text)
    }

    fun testRefusesSpanOutsideDocument() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 41;")
        val before = myFixture.editor.document.text

        val changed = RakuAstEditApplier.apply(
            project, myFixture.editor, 9000, EditResult(text = "99", span = AstSpan(0, 2)))

        assertFalse(changed)
        assertEquals(before, myFixture.editor.document.text)
    }

    fun testRefusesReversedSpan() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 41;")
        val before = myFixture.editor.document.text

        val changed = RakuAstEditApplier.apply(
            project, myFixture.editor, 0, EditResult(text = "99", span = AstSpan(10, 5)))

        assertFalse(changed)
        assertEquals(before, myFixture.editor.document.text)
    }

    // A null span means the backend's .origin was undefined -- distinct from
    // a real zero-length span at offset 0 (see testAppliesZeroLengthSpanAsInsert
    // below). apply() must refuse rather than guess a location.
    fun testRefusesNullSpan() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 41;")
        val before = myFixture.editor.document.text

        val changed = RakuAstEditApplier.apply(
            project, myFixture.editor, 0, EditResult(text = "99", span = null))

        assertFalse(changed)
        assertEquals(before, myFixture.editor.document.text)
    }

    // A real zero-length span [B, B) is a valid, deliberate location -- it
    // means insert the deparsed text and remove nothing. Nothing previously
    // covered this case; only bounds and reversed/negative spans were tested.
    fun testAppliesZeroLengthSpanAsInsert() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 41;")
        val result = EditResult(text = "X", span = AstSpan(from = 3, to = 3))

        val changed = RakuAstEditApplier.apply(project, myFixture.editor, 0, result)

        assertTrue(changed)
        assertEquals("my X\$x = 41;", myFixture.editor.document.text)
    }

    fun testRefusesNegativeStart() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 41;")
        val before = myFixture.editor.document.text

        val changed = RakuAstEditApplier.apply(
            project, myFixture.editor, -5, EditResult(text = "99", span = AstSpan(0, 2)))

        assertFalse(changed)
        assertEquals(before, myFixture.editor.document.text)
    }
}
