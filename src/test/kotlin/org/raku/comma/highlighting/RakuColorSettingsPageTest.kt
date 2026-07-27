package org.raku.comma.highlighting

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.raku.comma.highlighter.RakuColorSettingsPage
import org.raku.comma.highlighter.RakuHighlighter

/**
 * Guards the color-attribute surface. Every assertion here corresponds to a
 * way the highlighter, the settings page and the bundled schemes had actually
 * drifted apart while they were three hand-maintained lists.
 */
class RakuColorSettingsPageTest : BasePlatformTestCase() {
    private val page = RakuColorSettingsPage()

    /**
     * External names are persisted in users' saved color schemes. Renaming
     * one silently discards that user's customization of it, so this list is
     * frozen; adding a key is expected and means adding a line here.
     *
     * Note the irregular ones: the Pod group predates its POD_ field prefix
     * (RAKU_DIRECTIVE, RAKU_TEXT, RAKU_CODE, ...) and REGEX_CCLASS_SYNTAX is
     * RAKU_CCLASS_SYNTAX. They look like typos and are not.
     */
    private val frozenExternalNames = setOf(
        "RAKU_ALT_WARNING",
        "RAKU_ARRAY_COMPOSER",
        "RAKU_ARRAY_INDEXER",
        "RAKU_BAD_CHARACTER",
        "RAKU_BLOCK_CURLY_BRACKETS",
        "RAKU_BUILTIN_CALL",
        "RAKU_BUILTIN_VARIABLE",
        "RAKU_CAPTURE_TERM",
        "RAKU_CCLASS_SYNTAX",
        "RAKU_CODE",
        "RAKU_COMMENT",
        "RAKU_CONFIGURATION",
        "RAKU_CONTEXTUALIZER",
        "RAKU_DIRECTIVE",
        "RAKU_FORMAT_CODE",
        "RAKU_FORMAT_QUOTES",
        "RAKU_HASH_COMPOSER",
        "RAKU_HASH_INDEXER",
        "RAKU_INFIX",
        "RAKU_LABEL_COLON",
        "RAKU_LABEL_NAME",
        "RAKU_LAMBDA",
        "RAKU_METAOP",
        "RAKU_METHOD_CALL_NAME",
        "RAKU_MULTI_DECLARATOR",
        "RAKU_NAMED_PARAMETER_NAME_ALIAS",
        "RAKU_NAMED_PARAMETER_SYNTAX",
        "RAKU_NUMERIC_LITERAL",
        "RAKU_ONLY_STAR",
        "RAKU_PACKAGE_DECLARATOR",
        "RAKU_PAIR_KEY",
        "RAKU_PARAMETER_QUANTIFIER",
        "RAKU_PARAMETER_SEPARATOR",
        "RAKU_PARENTHESES",
        "RAKU_PHASER",
        "RAKU_POSTFIX",
        "RAKU_PREFIX",
        "RAKU_QUASI",
        "RAKU_QUOTE_MOD",
        "RAKU_QUOTE_PAIR",
        "RAKU_QUOTE_REGEX",
        "RAKU_REASSIGNED_LOCAL_VARIABLE",
        "RAKU_REASSIGNED_PARAMETER",
        "RAKU_REGEX_ANCHOR",
        "RAKU_REGEX_ASSERTION_ANGLE",
        "RAKU_REGEX_BACKSLASH_BAD",
        "RAKU_REGEX_BUILTIN_CCLASS",
        "RAKU_REGEX_CAPTURE",
        "RAKU_REGEX_GROUP_BRACKET",
        "RAKU_REGEX_INFIX",
        "RAKU_REGEX_LOOKAROUND",
        "RAKU_REGEX_MOD",
        "RAKU_REGEX_QUANTIFIER",
        "RAKU_REGEX_SIG_SPACE",
        "RAKU_RETURN_ARROW",
        "RAKU_ROUTINE_DECLARATOR",
        "RAKU_ROUTINE_NAME",
        "RAKU_SCOPE_DECLARATOR",
        "RAKU_SELF",
        "RAKU_SHAPE_DECLARATION",
        "RAKU_STATEMENT_CONTROL",
        "RAKU_STATEMENT_MOD",
        "RAKU_STATEMENT_PREFIX",
        "RAKU_STATEMENT_TERMINATOR",
        "RAKU_STRING_LITERAL_BAD_ESCAPE",
        "RAKU_STRING_LITERAL_CHAR",
        "RAKU_STRING_LITERAL_ESCAPE",
        "RAKU_STRING_LITERAL_QUOTE",
        "RAKU_STUB_CODE",
        "RAKU_SUB_CALL_NAME",
        "RAKU_TERM",
        "RAKU_TERM_DECLARATION_BACKSLASH",
        "RAKU_TEXT",
        "RAKU_TEXT_BOLD",
        "RAKU_TEXT_ITALIC",
        "RAKU_TEXT_UNDERLINE",
        "RAKU_TRAIT",
        "RAKU_TRANS_BAD",
        "RAKU_TRANS_CHAR",
        "RAKU_TRANS_ESCAPE",
        "RAKU_TRANS_RANGE",
        "RAKU_TYPENAME",
        "RAKU_TYPE_COERCION_PARENTHESES",
        "RAKU_TYPE_DECLARATOR",
        "RAKU_TYPE_NAME",
        "RAKU_TYPE_PARAMETER_BRACKET",
        "RAKU_UNUSED",
        "RAKU_VARIABLE",
        "RAKU_VERSION",
        "RAKU_WHATEVER",
        "RAKU_WHERE_CONSTRAINT",
    )

    fun testExternalNamesAreFrozen() {
        assertEquals(frozenExternalNames, RakuHighlighter.entries.map { it.key.externalName }.toSet())
    }

    fun testExternalNamesAreUnique() {
        val names = RakuHighlighter.entries.map { it.key.externalName }
        assertEquals("Duplicate external name", names.size, names.toSet().size)
    }

    /** Every key was given a fallback, so Raku follows the user's theme. */
    fun testEveryKeyHasAFallback() {
        val orphans = RakuHighlighter.entries
            .filter { it.key.fallbackAttributeKey == null }
            .map { it.key.externalName }
        assertEquals(emptyList<String>(), orphans)
    }

    /**
     * The old page hand-listed descriptors, and two keys drifted out of it
     * entirely (UNUSED, ALT_WARNING) while a third was shadowed.
     */
    fun testEveryPanelKeyHasExactlyOneDescriptor() {
        val descriptorKeys = page.attributeDescriptors.map { it.key }
        assertEquals(RakuHighlighter.panelEntries.map { it.key }, descriptorKeys)
        assertEquals("Two descriptors share a key", descriptorKeys.size, descriptorKeys.toSet().size)
    }

    fun testDescriptorNamesAreUniqueAndGrouped() {
        val names = page.attributeDescriptors.map { it.displayName }
        assertEquals("Two descriptors share a name", names.size, names.toSet().size)
        // Only "Bad syntax" sits at the top level; everything else nests.
        val ungrouped = names.filterNot { it.contains("//") }
        assertEquals(listOf("Bad syntax"), ungrouped)
    }

    /**
     * Characterizes a known gap rather than asserting a fix: no lexer token
     * maps to HASH_COMPOSER, because `{...}` arrives as a block brace. The
     * old page offered a "Hash Composer" row wired to ARRAY_COMPOSER, so
     * editing it recolored array composers instead.
     */
    fun testHashComposerIsDeclaredButNotOffered() {
        val entry = RakuHighlighter.entries.single { it.key == RakuHighlighter.HASH_COMPOSER }
        assertFalse("HASH_COMPOSER is not applied by anything; do not offer a dead control", entry.inPanel)
        assertFalse(page.attributeDescriptors.any { it.key == RakuHighlighter.HASH_COMPOSER })
    }

    fun testDemoTextLoads() {
        assertTrue(page.demoText.contains("grammar IPv4"))
        // The Java text block this replaced ended with an escaped backslash,
        // which rendered as a stray bad-character token in the preview.
        assertFalse("Demo text ends with a stray backslash", page.demoText.trimEnd().endsWith("\\"))
    }

    /**
     * An additional-highlighting tag with no counterpart in the demo text is
     * dead config; a tag in the demo text with no counterpart in the map
     * renders as literal `<builtinCall>` junk in the preview.
     */
    fun testDemoTextTagsMatchTheTagMap() {
        val declared = page.additionalHighlightingTagToDescriptorMap.keys
        val used = Regex("</(\\w+)>").findAll(page.demoText).map { it.groupValues[1] }.toSet()
        assertEquals(declared, used)
        for (tag in declared) {
            assertTrue("Unclosed <$tag> in demo text", page.demoText.contains("<$tag>"))
        }
    }

    /**
     * The bundled schemes are overrides on top of the fallbacks, so anything
     * they name must still exist -- a renamed key would leave a silently
     * inert entry behind.
     */
    fun testSchemeOverridesReferenceDeclaredKeys() {
        for (scheme in listOf("RakuDefault", "RakuDarcula")) {
            val unknown = schemeKeys(scheme) - frozenExternalNames
            assertEquals("$scheme.xml overrides undeclared keys", emptySet<String>(), unknown)
        }
    }

    /**
     * Default and Darcula had drifted to 37 and 39 keys. They are meant to be
     * the same set of overrides differing only in color values.
     */
    fun testBothSchemesOverrideTheSameKeys() {
        assertEquals(schemeKeys("RakuDefault"), schemeKeys("RakuDarcula"))
    }

    /**
     * The point of the fallback approach: overrides are the exception. If
     * this number climbs, the schemes are drifting back towards restating
     * colors the active theme already supplies.
     */
    fun testSchemesOverrideOnlyWhatFallbacksCannotExpress() {
        assertEquals(
            setOf(
                "RAKU_TEXT_BOLD", "RAKU_TEXT_ITALIC", "RAKU_TEXT_UNDERLINE",
                "RAKU_REGEX_SIG_SPACE", "RAKU_ALT_WARNING",
            ),
            schemeKeys("RakuDefault"),
        )
    }

    private fun schemeKeys(name: String): Set<String> {
        val stream = RakuHighlighter::class.java.getResourceAsStream("/colorSchemes/$name.xml")
        assertNotNull("Missing scheme resource $name.xml", stream)
        val xml = stream!!.bufferedReader().use { it.readText() }
        return Regex("<option name=\"(RAKU_\\w+)\"").findAll(xml)
            .map { it.groupValues[1] }
            .toSet()
    }
}
