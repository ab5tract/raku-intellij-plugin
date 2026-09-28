package org.raku.comma.utils

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import org.raku.comma.CommaFixtureTestCase
import java.nio.file.Files
import java.nio.file.Path

/**
 * `pathContainsRakuCode` is the predicate for callers that have no project
 * and no index: `canOpenFileAsProject` runs before a project exists, and
 * `scriptOnlyProject` shares it. The registered HashBang detectors cannot
 * answer for either, so the shebang case has to be handled here.
 *
 * Driven against a scratch directory rather than the fixture project: the
 * light project's basePath already holds `t/00-sanity.rakutest`, whose
 * extension alone would answer every one of these questions.
 */
class PathContainsRakuCodeTest : CommaFixtureTestCase() {

    private var scratch: Path? = null

    override fun tearDown() {
        try {
            scratch?.toFile()?.deleteRecursively()
        } finally {
            super.tearDown()
        }
    }

    private fun dirContaining(name: String, content: String): VirtualFile {
        val dir = Files.createTempDirectory("path-contains-raku-code-").also { scratch = it }
        val file = dir.resolve(name)
        Files.createDirectories(file.parent)
        Files.writeString(file, content)
        return LocalFileSystem.getInstance().refreshAndFindFileByNioFile(dir)!!
    }

    fun testAnExtensionlessShebangScriptIsRakuCode() {
        val dir = dirContaining("bin/runner", "#!/usr/bin/env raku\nsay 42;\n")
        assertTrue("an extensionless `#!/usr/bin/env raku` script is Raku code -- " +
                   "without this a project of loose scripts trips notifyMissingMETA " +
                   "and loses the installed-modules branch of DependencyDetails",
                   CommaProjectUtil.pathContainsRakuCode(dir))
    }

    // The false positive the substring version had. Merely mentioning raku is
    // not a shebang, and the clause must agree with what the HashBang
    // detectors match.
    fun testAnExtensionlessFileThatOnlyMentionsRakuIsNot() {
        val dir = dirContaining("NOTES", "this project is about raku one day\n")
        assertFalse("mentioning raku on the first line is not a shebang",
                    CommaProjectUtil.pathContainsRakuCode(dir))
    }

    // An empty extensionless file is ordinary -- a touched placeholder, a
    // lock file -- and this walk runs on every project open.
    fun testAnEmptyExtensionlessFileIsNotRakuCode() {
        val dir = dirContaining("PLACEHOLDER", "")
        assertFalse("an empty extensionless file is not Raku code",
                    CommaProjectUtil.pathContainsRakuCode(dir))
    }

    fun testAShebangForSomethingElseIsNot() {
        val dir = dirContaining("bin/perlish", "#!/usr/bin/perl\nprint 1;\n")
        assertFalse("a Perl shebang is not Raku code",
                    CommaProjectUtil.pathContainsRakuCode(dir))
    }
}
