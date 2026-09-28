package org.raku.comma.actions

import org.raku.comma.CommaFixtureTestCase

class LegacyExtensionScanTest : CommaFixtureTestCase() {

    // The scan list, the pattern and the rename map used to be three
    // hand-maintained copies of the same seven extensions; an extension
    // present in the first two and missing from the third renamed a file to
    // `*.null` -- no exception, and irreversible. They are one declaration
    // now, so this pins what collapsing them must not have changed: the
    // extensions, their order in the alternation, and every one of them
    // having a replacement.
    fun testTheLegacyExtensionPatternIsUnchanged() {
        assertEquals(".+?\\.(p6|pl6|pm6|pm|pod6|pod|t)",
                     UpdateExtensionsAction.FULL_LEGACY_EXTENSION_PATTERN.pattern())
    }

    fun testFindsLegacyFilesInTheProject() {
        myFixture.addFileToProject("lib/Old.pm6", "unit module Old;")
        val found = UpdateExtensionsAction.collectFilesWithLegacyNames(project)
        assertTrue("a .pm6 file should be offered for renaming, grouped under \"pm6\"",
                   found["pm6"]?.any { it.name == "Old.pm6" } ?: false)
    }

    // The reason this task exists. FileUtil.findFilesByMask took a java.io.File
    // and so could not see IntelliJ's exclude folders -- on rakudo that meant
    // descending into t/spec, 2158 files the module model excludes. An
    // index-backed scan is scoped, so excluded content is simply absent.
    fun testIgnoresExcludedDirectories() {
        myFixture.addFileToProject("lib/Old.pm6", "unit module Old;")
        val excluded = myFixture.addFileToProject("skipme/Buried.pm6", "unit module Buried;")
        com.intellij.testFramework.PsiTestUtil.addExcludedRoot(
            myFixture.module, excluded.virtualFile.parent)
        try {
            val found = UpdateExtensionsAction.collectFilesWithLegacyNames(project)
            val names = found.values.flatten().map { it.name }
            assertTrue("the visible file should still be found", names.contains("Old.pm6"))
            assertFalse("a file in an excluded directory must not be scanned",
                        names.contains("Buried.pm6"))
        } finally {
            com.intellij.testFramework.PsiTestUtil.removeExcludedRoot(
                myFixture.module, excluded.virtualFile.parent)
        }
    }
}
