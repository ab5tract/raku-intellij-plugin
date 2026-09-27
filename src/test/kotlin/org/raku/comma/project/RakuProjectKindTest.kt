package org.raku.comma.project

import org.raku.comma.CommaFixtureTestCase

class RakuProjectKindTest : CommaFixtureTestCase() {

    fun testAModuleFileMakesItRaku() {
        myFixture.addFileToProject("lib/Thing.rakumod", "unit module Thing;")
        assertTrue(RakuProjectKind.hasRakuFiles(project))
    }

    fun testAScriptFileMakesItRaku() {
        myFixture.addFileToProject("bin/go.raku", "say 42;")
        assertTrue(RakuProjectKind.hasRakuFiles(project))
    }

    // The Perl 5 collision. Raku Test claims bare `.t`, so counting that file
    // type would make any Perl project a Raku project.
    fun testTestFilesAloneDoNotMakeItRaku() {
        myFixture.addFileToProject("t/01-basic.t", "use Test;")
        assertFalse("a .t file alone must not wake the plugin",
                    RakuProjectKind.hasRakuFiles(project))
    }

    fun testMeta6MakesItADistribution() {
        myFixture.addFileToProject("META6.json", """{"name":"Thing"}""")
        assertTrue(RakuProjectKind.isRakuDistribution(project))
    }

    // The tiering: Raku files are not enough for the ecosystem fetch.
    fun testRakuFilesWithoutMeta6AreNotADistribution() {
        myFixture.addFileToProject("lib/Thing.rakumod", "unit module Thing;")
        assertTrue(RakuProjectKind.hasRakuFiles(project))
        assertFalse(RakuProjectKind.isRakuDistribution(project))
    }
}
