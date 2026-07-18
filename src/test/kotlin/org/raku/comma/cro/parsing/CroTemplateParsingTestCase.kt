package org.raku.comma.cro.parsing

import com.intellij.testFramework.ParsingTestCase
import org.raku.comma.cro.template.parsing.CroTemplateParserDefinition

abstract class CroTemplateParsingTestCase(dataDir: String) :
    ParsingTestCase(dataDir, "crotmp", CroTemplateParserDefinition()) {

    fun testParsingTestData() {
        doTest(true)
    }

    override fun getTestDataPath(): String = "testData/cro-template-parsing"

    override fun skipSpaces(): Boolean = false

    override fun includeRanges(): Boolean = true

    override fun isCheckNoPsiEventsOnReparse(): Boolean = false
}
