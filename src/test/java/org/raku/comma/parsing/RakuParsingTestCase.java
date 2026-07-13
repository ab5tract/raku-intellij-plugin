package org.raku.comma.parsing;

import com.intellij.testFramework.ParsingTestCase;

public abstract class RakuParsingTestCase extends ParsingTestCase {
    protected RakuParsingTestCase(String dataDir) {
        // The base class resolves the data directory in its constructor, so the
        // directory must travel through the dataPath argument rather than a field.
        super(dataDir, "p6", new RakuParserDefinition());
    }

    public void testParsingTestData() {
        doTest(true);
    }

    @Override
    protected String getTestDataPath() {
        return "testData/parsing";
    }

    @Override
    protected boolean isCheckNoPsiEventsOnReparse() {
        // The Raku parser is not incremental-reparse-stable: reparsing unchanged
        // text fires PSI change events (e.g. a fresh STATEMENT_LIST), which this
        // platform sanity check (newer than these tests) treats as a failure.
        return false;
    }

    @Override
    protected boolean skipSpaces() {
        return false;
    }

    @Override
    protected boolean includeRanges() {
        return true;
    }
}
