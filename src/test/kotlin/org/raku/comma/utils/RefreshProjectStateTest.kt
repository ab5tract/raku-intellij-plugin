package org.raku.comma.utils

import com.intellij.openapi.components.service
import com.intellij.testFramework.PlatformTestUtil
import kotlinx.coroutines.runBlocking
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.project.RakuProjectKind
import org.raku.comma.services.project.RakuDependencyService
import org.raku.comma.services.project.RakuProjectDetailsService
import java.util.concurrent.CompletableFuture

class RefreshProjectStateTest : CommaFixtureTestCase() {

    /**
     * The ecosystem fetch and the dependency service were gated together
     * behind `isRakuDistribution`, and line 153 was the only call to
     * `RakuDependencyService.initialize()` anywhere in the codebase. So a
     * Raku project with no META6.json never initialised it, and
     * `UsedModuleInspection` took its uninitialised branch permanently:
     * every non-pragma `use Foo;` was flagged "Cannot find Foo which is
     * specified as a dependency in META6.json" -- in a project that has no
     * META6.json.
     *
     * Only the fetch and the zef prompt should be gated. This asserts the
     * other half still happens.
     *
     * `refreshProjectState` runs on a worker rather than inline: the test
     * body is on the EDT, `initialize()` finishes with a
     * `withContext(Dispatchers.EDT)` block, and a `runBlocking` on the EDT
     * would deadlock against it. Production never has that problem -- the
     * only startup caller is a background project activity.
     */
    fun testAProjectWithoutMeta6StillGetsItsDependencyService() {
        val meta = java.io.File(project.basePath!!, "META6.json")
        val savedMeta = if (meta.exists()) meta.readText() else null
        if (savedMeta != null) meta.delete()

        val details = project.service<RakuProjectDetailsService>()
        val deps = project.service<RakuDependencyService>()
        val savedDidStartup = details.moduleServiceDidStartup
        val savedInitialized = deps.isInitialized

        // "Already ran, nothing left to do." Only a call that reaches
        // initializeDependencies -- which resets the flag before calling
        // initialize() -- can move isInitialized from here.
        details.moduleServiceDidStartup = true
        deps.isInitialized = false
        try {
            assertFalse("precondition: with no META6.json this is not a distribution",
                        RakuProjectKind.isRakuDistribution(project))

            val done = CompletableFuture<Unit>()
            Thread({
                try {
                    runBlocking { CommaProjectUtil.refreshProjectState(project) }
                    done.complete(Unit)
                } catch (t: Throwable) {
                    done.completeExceptionally(t)
                }
            }, "refreshProjectState-under-test").start()
            PlatformTestUtil.waitWithEventsDispatching(
                "refreshProjectState did not return", { done.isDone }, 180)
            done.get()

            assertTrue("a project of loose scripts must still get its dependency service: " +
                       "it is what resolves `use` against installed modules, and without it " +
                       "UsedModuleInspection warns about a META6.json that is not there",
                       deps.isInitialized)
        } finally {
            // The light project is shared JVM-wide, so hand back the flags the
            // inspections read. The filled-in module details cannot be undone,
            // but with isInitialized restored nothing consults them.
            deps.isInitialized = savedInitialized
            details.moduleServiceDidStartup = savedDidStartup
            if (savedMeta != null) meta.writeText(savedMeta)
        }
    }
}
