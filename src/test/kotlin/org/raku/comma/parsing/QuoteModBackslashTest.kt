package org.raku.comma.parsing

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.CommaFixtureTestCase

/**
 * Quoting constructs that assigned a dynamic variable nothing had declared.
 *
 * `CursorStack.assignDynamicVariable` walks the cursor stack for an existing
 * declaration and throws when it finds none, so each of these aborted indexing
 * of any file containing the construct. That is why rakudo's `t/spec` -- full
 * of quoting and transliteration torture tests -- had to be excluded from the
 * IDE wholesale; the exclusion was a workaround for these two defects.
 */
class QuoteModBackslashTest : CommaFixtureTestCase() {

    private fun parse(source: String) {
        myFixture.configureByText("quote-mod.raku", source)
        // Walking the tree forces the lexer across every token.
        PsiTreeUtil.processElements(myFixture.file) { _: PsiElement -> true }
    }

    // --- $*Q_BACKSLASHES: quote_mod_Q assigned a name that never existed. ---
    // `qb` and `Qb` appear in t/spec/S02-literals/quoting.t, the file that threw.

    fun testBareBackslashModifier() {
        parse("""my ${'$'}t = qb/a/;""")
    }

    fun testBareBackslashModifierUppercase() {
        parse("""my ${'$'}t = Qb/a/;""")
    }

    fun testBackslashAdverbBareDelimiter() {
        parse("""my ${'$'}t = q:b /\n\n\n/;""")
    }

    // Sibling modifiers run through the same rule; they worked before and must
    // keep working.
    fun testSiblingQuoteModifiersStillParse() {
        parse("""my ${'$'}s = qs/a/; my ${'$'}a = qa/b/; my ${'$'}h = qh/c/;""")
        parse("""my ${'$'}f = qf/d/; my ${'$'}c = qc/e/;""")
    }

    // --- $*RX_S: quote_tr uses quotepair_rx but declares no $*RX_S. ---
    // tr:s / TR:s appear in t/spec/S05-transliteration/, the other files that threw.

    fun testSigspaceAdverbOnTransliteration() {
        parse("""my ${'$'}x = "abc"; ${'$'}x ~~ tr:s/a/b/;""")
    }

    fun testSigspaceAdverbOnUppercaseTransliteration() {
        parse("""my ${'$'}x = "abc"; ${'$'}x ~~ TR:s/a/b/;""")
    }

    fun testSigspaceLongNameOnTransliteration() {
        parse("""my ${'$'}x = "abc"; ${'$'}x ~~ tr:sigspace/a/b/;""")
    }

    // Adverbs that do not touch $*RX_S, and the bare form, already worked.
    fun testOtherTransliterationAdverbsStillParse() {
        parse("""my ${'$'}x = "abc"; ${'$'}x ~~ tr:c/a/b/;""")
        parse("""my ${'$'}x = "abc"; ${'$'}x ~~ tr:d/a/b/;""")
        parse("""my ${'$'}x = "abc"; ${'$'}x ~~ tr/a/b/;""")
    }
}
