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

    // isRakuDistribution is a raw filesystem check against project.basePath, so
    // addFileToProject cannot drive it -- that writes into the source root, not
    // the base dir. Worse, the shared light fixture may scaffold a META6.json
    // during setup, which would make a naive positive test pass for the wrong
    // reason and the negative test impossible. So each of these controls the
    // file itself and restores what it found.
    private fun meta6File() = java.io.File(project.basePath!!, "META6.json")

    fun testMeta6MakesItADistribution() {
        val meta = meta6File()
        val existing = if (meta.exists()) meta.readText() else null
        if (existing == null) meta.writeText("""{"name":"Thing"}""")
        try {
            assertTrue(RakuProjectKind.isRakuDistribution(project))
        } finally {
            if (existing == null) meta.delete()
        }
    }

    fun testRakuFilesWithoutMeta6AreNotADistribution() {
        myFixture.addFileToProject("lib/Thing.rakumod", "unit module Thing;")
        assertTrue(RakuProjectKind.hasRakuFiles(project))

        val meta = meta6File()
        val existing = if (meta.exists()) meta.readText() else null
        if (existing != null) meta.delete()
        try {
            assertFalse("Raku files without a META6.json are not a distribution",
                        RakuProjectKind.isRakuDistribution(project))
        } finally {
            if (existing != null) meta.writeText(existing)
        }
    }
}
