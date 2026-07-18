package org.raku.comma.actions

import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.language.RakuLanguageVersion
import org.raku.comma.module.builder.RakuModuleBuilderModule
import org.raku.comma.module.builder.RakuModuleBuilderScript
import java.nio.file.Paths

class NewActionsTest : CommaFixtureTestCase() {
    fun testNewScriptAction() {
        val basePath = Paths.get(project.basePath!!)
        RakuModuleBuilderScript.stubScript(basePath, "test.rakuidea", true, RakuLanguageVersion.D)
        val path = basePath.resolve("test.rakuidea").toFile()
        assertTrue(path.exists())
    }

    fun testNewTestAction() {
        val basePath = project.basePath!!
        RakuModuleBuilderModule.stubTest(Paths.get(basePath, "t"), "10-sanity", emptyList(), RakuLanguageVersion.D)
        RakuModuleBuilderModule.stubTest(Paths.get(basePath, "t"), "20-sanity.rakutest", emptyList(), RakuLanguageVersion.D)
        assertTrue(Paths.get(basePath, "t", "10-sanity.rakutest").toFile().exists())
        assertTrue(Paths.get(basePath, "t", "20-sanity.rakutest").toFile().exists())
    }
}
