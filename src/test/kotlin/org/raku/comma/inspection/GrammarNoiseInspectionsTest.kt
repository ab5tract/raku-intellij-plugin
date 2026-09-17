package org.raku.comma.inspection

import com.intellij.lang.annotation.HighlightSeverity
import org.raku.comma.ALL_RAKU_INSPECTIONS
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

// Two gutter-noise sources reported from Rakudo's Grammar.nqp:
//  - `$<name>` / `$0` match variables flagged ERROR "not declared" -- they
//    are runtime lookups on $/ and never need declaration;
//  - every `key => value` fat arrow flagged WARNING "Pair literal can be
//    simplified" -- the general colonpair-style conversion is a preference,
//    not a problem; only genuinely simplifiable pairs (True/False/variable
//    matching the key) warrant a visible mark.
class GrammarNoiseInspectionsTest : CommaFixtureTestCase() {

    private fun marks(code: String, minSeverity: HighlightSeverity): List<String> {
        myFixture.enableInspections(*ALL_RAKU_INSPECTIONS)
        myFixture.configureByText(RakuScriptFileType.INSTANCE, code)
        return myFixture.doHighlighting()
            .filter { it.severity >= minSeverity && it.description != null }
            .map { it.description }
    }

    fun testMatchVariablesNeverUndeclared() {
        val errors = marks(
            "grammar G {\n" +
            "    token t { (\\d+) }\n" +
            "    method m(\$x) {\n" +
            "        my \$b := \$<babble><B>;\n" +
            "        my \$n := \$0;\n" +
            "    }\n" +
            "}\n",
            HighlightSeverity.ERROR
        )
        assertTrue("match vars flagged: $errors", errors.none { it.contains("is not declared") })
    }

    fun testActuallyUndeclaredVariableStillErrors() {
        val errors = marks("say \$nope;\n", HighlightSeverity.ERROR)
        assertTrue("expected undeclared error, got: $errors", errors.any { it.contains("\$nope is not declared") })
    }

    fun testGeneralFatArrowNotAWarning() {
        val warnings = marks("sub f(\$x) { \$x }; my \$y = 1; my %h = payload => f(\$y);\n", HighlightSeverity.WARNING)
        assertTrue("general fat arrow flagged: $warnings", warnings.none { it.contains("Pair literal") })
    }

    fun testSimplifiableFatArrowStillWarns() {
        val warnings = marks("my %h = verbose => True;\n", HighlightSeverity.WARNING)
        assertTrue("expected simplifiable-pair warning, got: $warnings", warnings.any { it.contains("Pair literal") })
    }
}
