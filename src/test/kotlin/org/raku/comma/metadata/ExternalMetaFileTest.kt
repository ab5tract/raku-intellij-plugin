package org.raku.comma.metadata

import kotlinx.serialization.json.Json
import org.raku.comma.CommaFixtureTestCase

/**
 * `depends` in an ecosystem META6 is not reliably a list of module names. The hash form
 * keyed by phase used to be flattened into one fake dependency per key -- a string like
 * `runtime => {"requires":[...]}` -- which reached ProjectModelSync and was formatted
 * into a `raku://` library URL, leaving the VFS to stat a path named after the JSON.
 */
class ExternalMetaFileTest : CommaFixtureTestCase() {

    private fun depends(json: String): List<String> =
        Json.decodeFromString<ExternalMetaFile>(json).depends

    fun testPlainStringDepends() {
        assertEquals(listOf("Foo::Bar", "Baz::Quux"),
                     depends("""{"depends": ["Foo::Bar", "Baz::Quux"]}"""))
    }

    fun testRuntimeRequiresHash() {
        assertEquals(listOf("Foo::Bar", "Baz::Quux"),
                     depends("""{"depends": {"runtime": {"requires": ["Foo::Bar", "Baz::Quux"]}}}"""))
    }

    /** The shape from the App::Rak META that produced the "File name too long" warning. */
    fun testRuntimeRequiresHashYieldsNoJsonBlob() {
        val result = depends(
            """{"depends": {"runtime": {"requires": ["rak:ver<0.0.67+>:auth<zef:lizmat>"]}}}"""
        )
        assertEquals(listOf("rak:ver<0.0.67+>:auth<zef:lizmat>"), result)
        assertFalse("A dependency name must never carry JSON",
                    result.any { it.contains("=>") || it.contains("{") })
    }

    /** Some distributions put `requires` at the top level rather than under a phase. */
    fun testFlatRequiresHash() {
        assertEquals(listOf("Foo::Bar"),
                     depends("""{"depends": {"requires": ["Foo::Bar"]}}"""))
    }

    /** `build` and `test` have their own META6 fields; they are not runtime dependencies. */
    fun testBuildAndTestPhasesAreNotRuntimeDepends() {
        assertEquals(listOf("Runtime::Only"),
                     depends("""{"depends": {"runtime": {"requires": ["Runtime::Only"]},
                                             "build": {"requires": ["Build::Only"]},
                                             "test": {"requires": ["Test::Only"]}}}"""))
    }

    fun testConditionalEntriesStillResolve() {
        assertEquals(listOf("Foo::Bar", "Foo::Linux"),
                     depends("""{"depends": ["Foo::Bar",
                                 {"by-kernel.name": {"linux": "Foo::Linux", "win32": "Foo::Win32"}}]}"""))
    }

    fun testUnrecognisedEntriesAreDroppedNotStringified() {
        assertEquals(listOf("Foo::Bar"),
                     depends("""{"depends": ["Foo::Bar", {"totally-unknown": {"a": "b"}}]}"""))
    }

    fun testMissingDependsIsEmpty() {
        assertEquals(listOf<String>(), depends("""{"name": "Foo"}"""))
    }
}
