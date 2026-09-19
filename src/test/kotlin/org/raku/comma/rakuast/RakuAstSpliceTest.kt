package org.raku.comma.rakuast

import org.raku.comma.CommaFixtureTestCase

/**
 * The list-position verb. The edit verb sets a whole attribute and so cannot
 * name one item of a list; this is what adding, replacing and reordering go
 * through.
 */
class RakuAstSpliceTest : CommaFixtureTestCase() {

    private fun service() = RakuAstService.getInstance(project)

    private fun splice(
        source: String,
        path: List<Int> = emptyList(),
        attr: String = "statements",
        index: Int,
        count: Int = 0,
        value: String,
    ) = service().splice(source, path, attr, index, count, value)

    private fun applied(source: String, result: EditResult): String {
        val span = result.span!!
        return source.substring(0, span.from) + result.text + source.substring(span.to)
    }

    private val twoStatements = "say 1;\nsay 2;\n"

    fun testInsertsAtTheFront() {
        val result = splice(twoStatements, index = 0, value = "say 0")

        assertNull(result.error)
        assertEquals("say 0;\nsay 1;\nsay 2;\n", applied(twoStatements, result))
    }

    fun testInsertsBetween() {
        val result = splice(twoStatements, index = 1, value = "say 9")

        assertNull(result.error)
        assertEquals("say 1;\nsay 9;\nsay 2;\n", applied(twoStatements, result))
    }

    // Appending has to come out terminated. It does so without this code
    // knowing that statements end in ';': whatever followed the last item
    // stays put after the new one.
    fun testAppendsAtTheEnd() {
        val result = splice(twoStatements, index = 2, value = "say 3")

        assertNull(result.error)
        assertEquals("say 1;\nsay 2;\nsay 3;\n", applied(twoStatements, result))
    }

    fun testReplacesOneItem() {
        val result = splice(twoStatements, index = 1, count = 1, value = "say 7")

        assertNull(result.error)
        assertEquals("say 1;\nsay 7;\n", applied(twoStatements, result))
        // Narrow: only the replaced statement, never its neighbours.
        assertEquals("say 2", twoStatements.substring(result.span!!.from, result.span!!.to))
    }

    // The separator is read from between two existing items rather than
    // guessed, so a comma-separated list comes out comma-separated with no
    // special-casing anywhere.
    fun testReusesTheSeparatorTheListAlreadyUses() {
        val source = "say(1, 2);"
        val argList = listOf(0, 0, 1)

        val inserted = service().splice(source, argList, "args", 1, 0, "99")
        assertNull(inserted.error)
        assertEquals("say(1, 99, 2);", applied(source, inserted))

        val appended = service().splice(source, argList, "args", 2, 0, "99")
        assertNull(appended.error)
        assertEquals("say(1, 2, 99);", applied(source, appended))
    }

    // With one item there is no gap to read, so the owner's type supplies a
    // default. A wrong guess cannot corrupt anything -- the result is
    // re-parsed before it is returned.
    fun testSingleItemListFallsBackToADefaultSeparator() {
        val source = "say 1;\n"
        val result = splice(source, index = 1, value = "say 2")

        assertNull(result.error)
        assertEquals("say 1;\nsay 2;\n", applied(source, result))
    }

    fun testRejectsAnIndexOutsideTheList() {
        val result = splice(twoStatements, index = 9, value = "say 3")

        assertNotNull(result.error)
        assertNull("nothing should be returned to write", result.text)
    }

    fun testRejectsASnippetThatDoesNotParse() {
        val result = splice(twoStatements, index = 0, value = "this is not raku ===")

        assertNotNull(result.error)
        assertNull(result.text)
    }

    fun testRejectsAnAttributeThatIsNotAList() {
        val result = service().splice(twoStatements, listOf(0), "expression", 0, 0, "say 3")

        assertNotNull(result.error)
        assertNull(result.text)
    }
}
