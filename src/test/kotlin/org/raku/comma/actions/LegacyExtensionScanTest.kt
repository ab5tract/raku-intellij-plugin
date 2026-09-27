package org.raku.comma.actions

import org.raku.comma.CommaFixtureTestCase

class LegacyExtensionScanTest : CommaFixtureTestCase() {

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
