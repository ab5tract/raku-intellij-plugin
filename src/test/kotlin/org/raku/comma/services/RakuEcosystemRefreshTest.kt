package org.raku.comma.services

import com.intellij.openapi.components.service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.services.application.RakuEcosystem
import java.util.concurrent.TimeUnit

class RakuEcosystemRefreshTest : CommaFixtureTestCase() {

    // initialize() is guarded by isNotInitializing && isNotInitialized, so once
    // the future completes it hands back cached state forever. A menu item
    // wired to it would silently do nothing, which is worse than no menu item.
    //
    // Asserted by identity, not by isInitialized: fillState returns a fresh
    // state object per fetch, so a re-fetch is observable as a different
    // instance. That holds no matter who initialised the service first.
    fun testRefreshReFetchesRatherThanReturningCachedState() {
        val eco = service<RakuEcosystem>()
        eco.initialize().get()
        val before = eco.ecosystem
        eco.refresh().get()
        assertNotSame("refresh must re-fetch, not hand back the cached state",
                      before, eco.ecosystem)
    }

    // The no-fetch-on-construction property cannot be observed at runtime in a
    // shared test JVM. It is pinned at the source instead, the way
    // ParserChangeVersionGuardTest pins its version constants -- and for the
    // same reason: the runtime value is not a trustworthy witness.
    fun testServiceDoesNotFetchFromItsFieldInitializer() {
        // Comment lines are stripped first. The KDoc above the field quotes
        // `initialize().join()` by name, to tell the next reader exactly what
        // was removed and why -- so a raw substring match fires on the correct
        // code. The property being pinned is that no CODE does this, and that
        // comment is worth more than the convenience of a one-line assertion.
        val code = java.io.File(
            "src/main/java/org/raku/comma/services/application/RakuEcosystem.kt")
            .readLines()
            .filterNot {
                val t = it.trimStart()
                t.startsWith("//") || t.startsWith("*") || t.startsWith("/*")
            }
            .joinToString("\n")
        assertFalse(
            "the field initializer must not fetch: it made gating the call sites " +
            "impossible and blocked whichever thread resolved the service",
            code.contains("initialize().join()"))
    }

    // initialize()'s coroutine used to complete whichever future was CURRENT
    // in the field at completion time, not the one it captured at launch.
    // refresh() reassigns that field, and a fetch takes seconds -- so a
    // refresh() landing while the first fetch is still in flight orphaned the
    // future the first caller was blocked on. CommaProjectUtil.refreshProjectState
    // does exactly such a blocking .get(), so the failure mode was a permanent
    // hang, not a thrown exception.
    //
    // A fresh RakuEcosystem is constructed directly here, rather than going
    // through service<RakuEcosystem>(), so the starting "not yet initialized"
    // state -- required for initialize() to actually still be in flight when
    // refresh() lands -- is guaranteed regardless of what earlier tests already
    // did to the shared application-level singleton.
    fun testRefreshDuringInFlightFetchDoesNotOrphanTheFirstFuture() {
        val scope = CoroutineScope(Dispatchers.Default)
        val eco = RakuEcosystem(scope)
        try {
            val first = eco.initialize()
            val second = eco.refresh()
            assertNotSame("refresh must hand back a distinct future from the in-flight one",
                          first, second)
            first.get(30, TimeUnit.SECONDS)
            second.get(30, TimeUnit.SECONDS)
        } finally {
            scope.cancel()
        }
    }
}
