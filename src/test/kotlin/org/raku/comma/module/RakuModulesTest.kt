package org.raku.comma.module

import com.intellij.openapi.externalSystem.ExternalSystemModulePropertyManager
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.testFramework.fixtures.BasePlatformTestCase

// Which module the plugin is allowed to manage at all. Gradle, Maven and friends
// own the modules they create -- their content roots, their SDK and, crucially,
// their compile classpath -- and a Raku file lying somewhere in such a project is
// not an invitation to start rewriting that module's dependencies.
class RakuModulesTest : BasePlatformTestCase() {

    override fun tearDown() {
        try {
            ExternalSystemModulePropertyManager.getInstance(myFixture.module).unlinkExternalOptions()
        } finally {
            super.tearDown()
        }
    }

    fun testModuleOwnedByAnotherBuildSystemIsNotManaged() {
        ExternalSystemModulePropertyManager.getInstance(myFixture.module)
            .setExternalId(ProjectSystemId("GRADLE"))

        assertNull(
            "a Gradle-owned module is not the plugin's to manage",
            RakuModules.managedModule(project)
        )
    }

    fun testPlainModuleIsStillManaged() {
        assertEquals(
            "a project nobody else owns is still managed, as it always was",
            myFixture.module,
            RakuModules.managedModule(project)
        )
    }

    fun testPlainModuleIsNotARakuModule() {
        assertFalse(RakuModules.isRakuModule(myFixture.module))
    }
}
