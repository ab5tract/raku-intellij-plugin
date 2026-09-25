package org.raku.comma.rakuast

import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

// replace() takes absolute UTF-16 document offsets. Converting the backend's
// grapheme-indexed span and checking the document is still fresh happen in
// RakuAstViewerPanel before the call -- so the cases that used to live here
// for a null span or an error-carrying result now belong to the panel's
// tests: neither can reach an offset at all.
class RakuAstEditApplierTest : CommaFixtureTestCase() {

    fun testReplacesOnlyTheNodeSpan() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "say 1;\nmy \$x = 41;\n")
        // "41" sits at 15..17: "say 1;\n" is 7 chars, then "my $x = " is 8 more.
        val changed = RakuAstEditApplier.replace(project, myFixture.editor, 15, 17, "99")

        assertTrue(changed)
        assertEquals("say 1;\nmy \$x = 99;\n", myFixture.editor.document.text)
    }

    // Everything outside the edited node must survive byte-for-byte, including
    // comments, which DEPARSE cannot reproduce. Exact equality, so a widening
    // in either direction fails -- `contains` would miss a swallowed semicolon.
    fun testCommentsAndFormattingOutsideTheNodeSurvive() {
        val original = "my  \$x   =   41;   # keep me\n# and me\n"
        myFixture.configureByText(RakuScriptFileType.INSTANCE, original)

        RakuAstEditApplier.replace(project, myFixture.editor, 13, 15, "99")

        assertEquals("my  \$x   =   99;   # keep me\n# and me\n",
                     myFixture.editor.document.text)
    }

    fun testRefusesRangeOutsideDocument() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 41;")
        val before = myFixture.editor.document.text

        val changed = RakuAstEditApplier.replace(project, myFixture.editor, 9000, 9002, "99")

        assertFalse(changed)
        assertEquals(before, myFixture.editor.document.text)
    }

    fun testRefusesReversedRange() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 41;")
        val before = myFixture.editor.document.text

        val changed = RakuAstEditApplier.replace(project, myFixture.editor, 10, 5, "99")

        assertFalse(changed)
        assertEquals(before, myFixture.editor.document.text)
    }

    fun testRefusesNegativeStart() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 41;")
        val before = myFixture.editor.document.text

        val changed = RakuAstEditApplier.replace(project, myFixture.editor, -5, -3, "99")

        assertFalse(changed)
        assertEquals(before, myFixture.editor.document.text)
    }

    // A zero-length range is a deliberate location, not a missing one: insert
    // the text and remove nothing.
    fun testAppliesZeroLengthRangeAsInsert() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 41;")

        val changed = RakuAstEditApplier.replace(project, myFixture.editor, 3, 3, "X")

        assertTrue(changed)
        assertEquals("my X\$x = 41;", myFixture.editor.document.text)
    }
}
