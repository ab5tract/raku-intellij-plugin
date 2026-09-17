package org.raku.comma.inspection

import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.ALL_RAKU_INSPECTIONS

// Rakudo's grammar idioms trip the unused checks two ways:
//  - a role parameter used only inside an indirect routine name
//    (`token ::($meth_name)`) -- the usage lives inside the flat
//    ROUTINE_NAME token, invisible to ReferencesSearch;
//  - `:my $stub := ...;` regex-embedded declarations bound purely for the
//    side effect of their initializer.
// Both are legal, idiomatic, and must not flag; genuinely unused things
// still must.
class RegexDeclUnusedTest : CommaFixtureTestCase() {

    private fun unusedDescriptions(code: String): List<String> {
        myFixture.enableInspections(*ALL_RAKU_INSPECTIONS)
        myFixture.configureByText(RakuScriptFileType.INSTANCE, code)
        return myFixture.doHighlighting()
            .filter { it.description == "Unused variable" || it.description == "Unused parameter" }
            .map { "${it.description}: ${myFixture.file.text.substring(it.startOffset, it.endOffset)}" }
    }

    fun testParameterUsedInIndirectNameNotFlagged() {
        val unused = unusedDescriptions(
            "my role Circumfix[\$meth_name] {\n" +
            "    token ::(\$meth_name) { 'x' }\n" +
            "}\n"
        )
        assertTrue("flagged: $unused", unused.isEmpty())
    }

    fun testRegexEmbeddedSideEffectBindingNotFlagged() {
        val unused = unusedDescriptions(
            "grammar G {\n" +
            "    token t {\n" +
            "        :my \$stub := setup();\n" +
            "        'x'\n" +
            "    }\n" +
            "}\n"
        )
        assertTrue("flagged: $unused", unused.isEmpty())
    }

    fun testRegexEmbeddedUninitializedStillFlagged() {
        val unused = unusedDescriptions(
            "grammar G {\n" +
            "    token t {\n" +
            "        :my \$never;\n" +
            "        'x'\n" +
            "    }\n" +
            "}\n"
        )
        assertTrue("expected a flag, got none", unused.any { it.contains("\$never") })
    }

    fun testOrdinaryUnusedVariableStillFlagged() {
        val unused = unusedDescriptions("sub f() { my \$dead = 1; 42 }\n")
        assertTrue("expected a flag, got none", unused.any { it.contains("\$dead") })
    }
}
