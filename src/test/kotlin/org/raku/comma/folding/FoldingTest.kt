package org.raku.comma.folding

import org.raku.comma.CommaFixtureTestCase

import java.nio.file.Paths

class FoldingTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/folding"
    }

    fun testFolding() {
        myFixture.configureByFiles("FoldingTestData.p6")
        myFixture.testFolding(Paths.get(getTestDataPath(), "FoldingTestData.p6").toString())
    }
}
