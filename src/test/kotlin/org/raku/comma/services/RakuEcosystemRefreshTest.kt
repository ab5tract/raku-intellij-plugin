package org.raku.comma.services

import com.intellij.openapi.components.service
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.services.application.RakuEcosystem

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
}
