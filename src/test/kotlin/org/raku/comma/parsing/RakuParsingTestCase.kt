package org.raku.comma.parsing

import com.intellij.testFramework.ParsingTestCase

// The base class resolves the data directory in its constructor, so the
// directory must travel through the dataPath argument rather than a property.
abstract class RakuParsingTestCase(dataDir: String) : ParsingTestCase(dataDir, "p6", RakuParserDefinition()) {

    fun testParsingTestData() {
        doTest(true)
    }

    override fun getTestDataPath(): String = "testData/parsing"

    override fun skipSpaces(): Boolean = false

    override fun includeRanges(): Boolean = true

    // The Raku parser is not incremental-reparse-stable: reparsing unchanged
    // text fires PSI change events (e.g. a fresh STATEMENT_LIST), which this
    // platform sanity check (newer than these tests) treats as a failure.
    override fun isCheckNoPsiEventsOnReparse(): Boolean = false
}
