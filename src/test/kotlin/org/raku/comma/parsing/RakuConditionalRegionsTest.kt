package org.raku.comma.parsing

import junit.framework.TestCase

class RakuConditionalRegionsTest : TestCase() {

    private val text = """
        my ${'$'}shared = 1;
        #?if moar
        my ${'$'}m = 1;
        #?endif
        #?if jvm
        my ${'$'}j = 1;
        my ${'$'}j2 = 2;
        #?endif
        say ${'$'}shared;
    """.trimIndent()

    fun testRegionsReportsBothConditionals() {
        val regions = RakuConditionalCompilation.regions(text)
        assertEquals(2, regions.size)

        val moar = regions[0]
        assertTrue(moar.active)
        assertEquals("moar", moar.condition)
        assertEquals("my \$m = 1;\n", text.substring(moar.start, moar.end))

        val jvm = regions[1]
        assertFalse(jvm.active)
        assertEquals("jvm", jvm.condition)
        assertEquals("my \$j = 1;\nmy \$j2 = 2;\n", text.substring(jvm.start, jvm.end))
    }

    fun testInactiveRegionsOnlyReportsDeadBranches() {
        val inactive = RakuConditionalCompilation.inactiveRegions(text)
        assertEquals(1, inactive.size)
        assertEquals("jvm", inactive[0].condition)
    }

    fun testConditionAt() {
        val regions = RakuConditionalCompilation.regions(text)
        assertEquals("moar", RakuConditionalCompilation.conditionAt(text, regions[0].start))
        assertEquals("jvm", RakuConditionalCompilation.conditionAt(text, regions[1].end - 1))
        assertNull(RakuConditionalCompilation.conditionAt(text, 0))
    }

    fun testNegatedConditionKeepsBang() {
        val t = "#?if !js\nmy \$x = 1;\n#?endif\n"
        val regions = RakuConditionalCompilation.regions(t)
        assertEquals(1, regions.size)
        assertEquals("!js", regions[0].condition)
        assertTrue(regions[0].active) // !js is live under moar
    }

    fun testEmptyBodyProducesNoInactiveRegion() {
        val t = "#?if jvm\n#?endif\n"
        assertTrue(RakuConditionalCompilation.inactiveRegions(t).isEmpty())
    }

    fun testUnrecognisedMarkerIsNoRegion() {
        // '#?ifsomething' and trailing junk are not markers (mirrors isOmitted rules)
        assertTrue(RakuConditionalCompilation.regions("#?ifsomething\nfoo\n#?endif\n").isEmpty())
    }

    fun testSecondIfClosesTheFirstRegion() {
        val t = "#?if jvm\nmy \$a = 1;\n#?if js\nmy \$b = 2;\n#?endif\n"
        val regions = RakuConditionalCompilation.regions(t)
        assertEquals(2, regions.size)
        assertEquals("jvm", regions[0].condition)
        assertEquals("my \$a = 1;\n", t.substring(regions[0].start, regions[0].end))
        assertEquals("js", regions[1].condition)
    }

    fun testUnterminatedIfRunsToEndOfFile() {
        val t = "say 1;\n#?if jvm\nmy \$a = 1;\n"
        val regions = RakuConditionalCompilation.regions(t)
        assertEquals(1, regions.size)
        assertEquals(t.length, regions[0].end)
    }

    fun testPreprocessStillBlanksInactiveBody() {
        val processed = RakuConditionalCompilation.preprocess(text).toString()
        assertEquals(text.length, processed.length)
        assertFalse(processed.contains("\$j = 1"))
        assertTrue(processed.contains("\$m = 1"))
    }
}
