package org.raku.comma.metadata

import kotlinx.serialization.json.Json
import org.raku.comma.CommaFixtureTestCase

/**
 * Characterization tests for MetaFile's `depends` parsing. The Raku ecosystem allows
 * `depends` entries to be conditional objects (not just plain module-name strings), e.g.
 * `{ "by-kernel.name": { "linux": "Foo::Linux", ... } }`. A real-world META6.json with such
 * an entry used to throw during parsing instead of degrading gracefully.
 */
class MetaFileTest : CommaFixtureTestCase() {

    fun testPlainStringDepends() {
        val meta = Json.decodeFromString<MetaFile>("""{"depends": ["Foo::Bar", "Baz::Quux"]}""")
        assertEquals(listOf("Foo::Bar", "Baz::Quux"), meta.depends)
    }

    fun testByKernelNameDoesNotThrow() {
        val meta = Json.decodeFromString<MetaFile>(
            """{"depends": ["Foo::Bar", {"by-kernel.name": {"linux": "Foo::Linux", "win32": "Foo::Win32"}}]}"""
        )
        assertEquals(listOf("Foo::Bar", "Foo::Linux"), meta.depends)
    }

    fun testNestedByKernelDoesNotThrow() {
        val meta = Json.decodeFromString<MetaFile>(
            """{"depends": [{"by-kernel": {"name": {"linux": "Foo::Linux", "win32": "Foo::Win32"}}}]}"""
        )
        assertEquals(listOf("Foo::Linux"), meta.depends)
    }

    fun testUnresolvableConditionalDependsIsSkippedNotThrown() {
        val meta = Json.decodeFromString<MetaFile>(
            """{"depends": ["Foo::Bar", {"by-kernel.name": {"plan9": "Foo::Plan9"}}]}"""
        )
        assertEquals(listOf("Foo::Bar"), meta.depends)
    }

    fun testNameWrappedConditionalDepends() {
        val meta = Json.decodeFromString<MetaFile>("""{"depends": [{"name": "Foo::Bar"}]}""")
        assertEquals(listOf("Foo::Bar"), meta.depends)
    }
}
