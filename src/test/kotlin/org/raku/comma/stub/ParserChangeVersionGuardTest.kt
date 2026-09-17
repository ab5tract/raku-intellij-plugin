package org.raku.comma.stub

import junit.framework.TestCase
import java.io.File
import java.security.MessageDigest

// Guard against the twice-hit failure mode where a parser/lexer change ships
// without bumping the persisted-index versions, leaving upgraded installs
// with stale stub indexes ("PSI and index do not match",
// UpToDateStubIndexMismatch) or stale word indexes (find-usages misses).
//
// The test pairs a fingerprint of the parse-shape-defining sources with the
// two version constants. Changing any of those sources fails this test until
// the fingerprint file is regenerated -- and the failure message makes the
// developer decide, consciously, whether the change alters parse output and
// therefore requires version bumps.
class ParserChangeVersionGuardTest : TestCase() {

    private val shapeSources = listOf(
        "src/main/java/org/raku/comma/parsing/MAINBraid.java",
        "src/main/java/org/raku/comma/parsing/RakuParser.java",
        "src/main/java/org/raku/comma/parsing/RakuConditionalCompilation.java",
    )
    private val fingerprintFile = File("src/test/resources/parser-shape-fingerprint.txt")

    // The version constants are read from SOURCE, not referenced as compiled
    // constants: const/static-final ints are inlined into this test's
    // bytecode at compile time, and Gradle compile avoidance can leave that
    // bytecode stale after a bump -- the guard would then compare against the
    // old values (observed in practice).
    private fun sourceInt(path: String, pattern: String): Int =
        Regex(pattern).find(File(path).readText())!!.groupValues[1].toInt()

    private fun currentLine(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        for (path in shapeSources) digest.update(File(path).readBytes())
        val fingerprint = digest.digest().joinToString("") { "%02x".format(it) }
        val stub = sourceInt("src/main/java/org/raku/comma/psi/stub/RakuFileElementType.kt",
                             """STUB_VERSION: Int = (\d+)""")
        val words = sourceInt("src/main/java/org/raku/comma/parsing/RakuWordsScanner.java",
                              """int VERSION = (\d+)""")
        return "$fingerprint stub=$stub words=$words"
    }

    fun testParserShapeIsPairedWithIndexVersions() {
        val current = currentLine()
        assertTrue("Missing ${fingerprintFile.path} — create it containing:\n$current", fingerprintFile.exists())
        val recorded = fingerprintFile.readText().trim()
        assertEquals(
            """
            The parse-shape sources (MAINBraid/RakuParser/RakuConditionalCompilation)
            or an index version constant changed without updating the pairing file.
            If the change can alter parse output for ANY existing file text, bump
            RakuFileElementType.STUB_VERSION and RakuWordsScanner.VERSION first
            (stale persisted indexes otherwise crash or silently miss symbols on
            upgraded installs). Then regenerate ${fingerprintFile.path} to contain:
            $current
            """.trimIndent(),
            recorded,
            current
        )
    }
}
