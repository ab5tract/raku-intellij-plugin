package org.raku.comma.rakuast

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AstJsonTest {

    @Test
    fun decodesAnalyzeTree() {
        val json = """
            {"tree":{"class":"RakuAST::StatementList","path":[],"span":{"from":0,"to":19},
             "attrs":[],
             "children":[{"class":"RakuAST::IntLiteral","path":[0],"span":{"from":8,"to":10},
              "attrs":[{"name":"value","kind":"scalar","display":"41","editable":true}],
              "children":[]}]}}
        """.trimIndent()

        val result = AstJson.decodeAnalyze(json)

        assertNull(result.error)
        val root = result.tree!!
        assertEquals("RakuAST::StatementList", root.nodeClass)
        assertEquals(emptyList<Int>(), root.path)
        val child = root.children.single()
        assertEquals("RakuAST::IntLiteral", child.nodeClass)
        assertEquals(listOf(0), child.path)
        val childSpan = child.span!!
        assertEquals(8, childSpan.from)
        assertEquals(10, childSpan.to)
        val attr = child.attrs.single()
        assertEquals("value", attr.name)
        assertEquals("scalar", attr.kind)
        assertTrue(attr.editable)
    }

    @Test
    fun decodesErrorPayload() {
        val result = AstJson.decodeAnalyze("""{"error":"Invalid typename 'Foo'"}""")
        assertNull(result.tree)
        assertEquals("Invalid typename 'Foo'", result.error)
    }

    @Test
    fun decodesEditResult() {
        val json = """{"text":"99","span":{"from":8,"to":10},
                       "tree":{"class":"RakuAST::StatementList","path":[],
                               "span":{"from":0,"to":19},"attrs":[],"children":[]}}"""
        val result = AstJson.decodeEdit(json)
        assertNull(result.error)
        assertEquals("99", result.text)
        assertEquals(8, result.span!!.from)
        assertEquals("RakuAST::StatementList", result.tree!!.nodeClass)
    }

    // `null` span (undefined .origin on the backend, e.g. a synthetic
    // RakuAST::Type::Setting node) must decode to a null AstNode.span, never
    // to a real-looking AstSpan(0, 0) -- the two are indistinguishable to a
    // caller unless the wire format keeps them apart.
    @Test
    fun decodesNullSpanAsNull() {
        val json = """
            {"tree":{"class":"RakuAST::Type::Setting","path":[0],"span":null,
             "attrs":[],"children":[]}}
        """.trimIndent()

        val result = AstJson.decodeAnalyze(json)

        assertNull(result.error)
        assertNull(result.tree!!.span)
    }

    // Garbage must not throw: the subprocess can emit anything, including
    // an empty string when RakuCommandLine swallows a non-zero exit.
    @Test
    fun malformedInputBecomesError() {
        for (bad in listOf("", "not json", "[]", "{")) {
            val result = AstJson.decodeAnalyze(bad)
            assertNull("tree should be null for: $bad", result.tree)
            assertTrue("error should be set for: $bad", result.error != null)
        }
    }
}
