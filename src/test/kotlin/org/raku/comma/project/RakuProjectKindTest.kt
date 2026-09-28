package org.raku.comma.project

import com.intellij.testFramework.DumbModeTestUtils
import kotlinx.coroutines.runBlocking
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

class RakuProjectKindTest : CommaFixtureTestCase() {

    fun testAModuleFileMakesItRaku() {
        myFixture.addFileToProject("lib/Thing.rakumod", "unit module Thing;")
        assertTrue(RakuProjectKind.hasRakuFiles(project))
    }

    fun testAScriptFileMakesItRaku() {
        myFixture.addFileToProject("bin/go.raku", "say 42;")
        assertTrue(RakuProjectKind.hasRakuFiles(project))
    }

    /**
     * The spec's sole justification for deleting the substring clause from
     * `pathContainsRakuCode` was that the shebang detectors already handle
     * this, and nothing verified it.
     *
     * Deliberately NOT in a `bin` directory: RakuFileTypeDetector runs
     * order="FIRST" and would answer for the file there, so a `bin/` fixture
     * would prove nothing about shebangs.
     */
    fun testAnExtensionlessShebangScriptMakesItRaku() {
        val runner = myFixture.addFileToProject("scripts/runner", "#!/usr/bin/env raku\nsay 42;\n")
        assertFalse("precondition: this must not be answered by the bin/ detector",
                    runner.virtualFile.parent.name == "bin")
        assertSame("a `#!/usr/bin/env raku` script is a Raku script wherever it lives",
                   RakuScriptFileType.INSTANCE, runner.virtualFile.fileType)
        assertTrue(RakuProjectKind.hasRakuFiles(project))
    }

    fun testAnEmptyProjectIsNeitherRakuNorADistribution() {
        emptyTheSourceRoots()
        val meta = meta6File()
        val existing = if (meta.exists()) meta.readText() else null
        if (existing != null) meta.delete()
        try {
            assertFalse("nothing in the project, so nothing to wake the plugin for",
                        RakuProjectKind.hasRakuFiles(project))
            assertFalse("and no META6.json, so not a distribution either",
                        RakuProjectKind.isRakuDistribution(project))
        } finally {
            if (existing != null) meta.writeText(existing)
        }
    }

    // The Perl 5 collision. Raku Test claims bare `.t`, so counting that file
    // type would make any Perl project a Raku project.
    fun testTestFilesAloneDoNotMakeItRaku() {
        myFixture.addFileToProject("t/01-basic.t", "use Test;")
        assertFalse("a .t file alone must not wake the plugin",
                    RakuProjectKind.hasRakuFiles(project))
    }

    /**
     * An extensionless file used to be typed as a Raku script on the strength
     * of its parent directory's URL ending in the letters `bin` -- content
     * never read, and registered `order="FIRST"` so it beat the shebang
     * detectors. Cosmetic while nothing depended on it; decisive now that
     * `hasRakuFiles` asks the file-type index, because it made every Perl
     * distribution, Python venv, node project and Go repo with an
     * extensionless executable in `bin/` into a Raku project.
     *
     * The fixture writes under the source root, so `bin/tool` lands at
     * `<basePath>/lib/bin/tool` -- the scaffolded `<basePath>/META6.json` is
     * NOT beside that `bin`, which is exactly the negative case.
     */
    fun testAnExtensionlessBinFileOutsideADistributionIsNotRaku() {
        emptyTheSourceRoots()
        val tool = myFixture.addFileToProject("bin/tool", "echo hello\n")
        val binDir = tool.virtualFile.parent
        assertEquals("precondition: the file must really sit in a bin/ directory",
                     "bin", binDir.name)
        assertNull("precondition: nothing may make this bin/ part of a distribution",
                   binDir.parent.findChild("META6.json"))

        assertNotSame("an extensionless bin/ file outside a distribution is not a Raku script",
                      RakuScriptFileType.INSTANCE, tool.virtualFile.fileType)
        assertFalse("and so it must not wake the plugin",
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

    fun testAwaitRakuFilesWaitsForTheIndexInsteadOfAnsweringNo() {
        myFixture.addFileToProject("lib/Waited.rakumod", "unit module Waited;")
        var answered: Boolean? = null
        val worker = Thread { answered = runBlocking { RakuProjectKind.awaitRakuFiles(project) } }
        val token = DumbModeTestUtils.startEternalDumbModeTask(project)
        try {
            assertFalse("precondition: the plain predicate gives up while indexing",
                        RakuProjectKind.hasRakuFiles(project))
            worker.start()
            worker.join(500)
            assertNull("awaitRakuFiles must not answer while the index is unavailable",
                       answered)
        } finally {
            DumbModeTestUtils.endEternalDumbModeTaskAndWaitForSmartMode(project, token)
        }
        worker.join(10_000)
        assertEquals("it should answer once the index is ready", true, answered)
    }
}
