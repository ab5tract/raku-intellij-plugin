package org.raku.comma.reference

import org.raku.comma.CommaFixtureTestCase

// Rakudo's src/Raku/ast/*.rakumod files are concatenated at build time, so
// no `use` statements connect them. Lexical resolution of RakuAST::Node
// either fails or -- worse -- lands on the SDK setting's synthetic external
// symbol, which has no containing file, so ctrl+click goes nowhere. For
// Rakudo-core files the project's own GLOBAL_TYPES declaration must win.
class RakudoCoreGlobalTypeResolveTest : CommaFixtureTestCase() {

    fun testIsTraitResolvesAcrossCoreFiles() {
        myFixture.addFileToProject(
            "rakudo/src/Raku/ast/base.rakumod",
            "class RakuAST::Node {\n    method IMPL-WRAP-LIST() { }\n}\n"
        )
        val call = myFixture.addFileToProject(
            "rakudo/src/Raku/ast/call.rakumod",
            "class RakuAST::Call is RakuAST::Node {\n}\n"
        )
        myFixture.openFileInEditor(call.virtualFile)
        val offset = call.text.indexOf("RakuAST::Node {") .let { call.text.indexOf("is RakuAST::Node") + 4 }
        val reference = call.findReferenceAt(offset)
        assertNotNull("no reference at 'is RakuAST::Node'", reference)
        val resolved = reference!!.resolve()
        assertNotNull("is-trait type did not resolve across core files", resolved)
        assertEquals("base.rakumod", resolved!!.containingFile?.name)
    }

    fun testTypeNameResolvesAcrossCoreFiles() {
        myFixture.addFileToProject(
            "rakudo/src/Raku/ast/base.rakumod",
            "class RakuAST::Node {\n}\n"
        )
        val use = myFixture.addFileToProject(
            "rakudo/src/Raku/ast/other.rakumod",
            "class RakuAST::Other {\n    method m() { RakuAST::Node.new }\n}\n"
        )
        myFixture.openFileInEditor(use.virtualFile)
        val offset = use.text.indexOf("RakuAST::Node.new") + 2
        val reference = use.findReferenceAt(offset)
        assertNotNull("no reference at type name", reference)
        val resolved = reference!!.resolve()
        assertNotNull("type name did not resolve across core files", resolved)
        assertEquals("base.rakumod", resolved!!.containingFile?.name)
    }

    // Regular core Raku (src/core.c) declares its types my-scoped -- they
    // land in the LEXICAL_TYPES index, not GLOBAL_TYPES -- and the setting
    // concatenation makes them visible across files all the same.
    fun testDoesTraitResolvesAcrossCoreSettingFiles() {
        myFixture.addFileToProject(
            "rakudo/src/core.c/Baggy.rakumod",
            "my role Baggy does QuantHash {\n    method total() { }\n}\n"
        )
        val bag = myFixture.addFileToProject(
            "rakudo/src/core.c/Bag.rakumod",
            "my class Bag does Baggy {\n}\n"
        )
        myFixture.openFileInEditor(bag.virtualFile)
        val offset = bag.text.indexOf("does Baggy") + 6
        val reference = bag.findReferenceAt(offset)
        assertNotNull("no reference at 'does Baggy'", reference)
        val resolved = reference!!.resolve()
        assertNotNull("does-trait type did not resolve across core setting files", resolved)
        assertEquals("Baggy.rakumod", resolved!!.containingFile?.name)
    }

    fun testMyScopedTypeNameResolvesAcrossCoreSettingFiles() {
        myFixture.addFileToProject(
            "rakudo/src/core.c/Baggy.rakumod",
            "my role Baggy {\n}\n"
        )
        val user = myFixture.addFileToProject(
            "rakudo/src/core.c/Mix.rakumod",
            "my class Mix {\n    method m() { Baggy.new }\n}\n"
        )
        myFixture.openFileInEditor(user.virtualFile)
        val offset = user.text.indexOf("Baggy.new") + 2
        val resolved = user.findReferenceAt(offset)?.resolve()
        assertNotNull("my-scoped type did not resolve across core setting files", resolved)
        assertEquals("Baggy.rakumod", resolved!!.containingFile?.name)
    }

    fun testOrdinaryProjectFilesDoNotGetTheFallback() {
        myFixture.addFileToProject("lib/A.rakumod", "class Some::Type {\n}\n")
        val b = myFixture.addFileToProject("lib/B.rakumod", "class Other is Some::Type {\n}\n")
        myFixture.openFileInEditor(b.virtualFile)
        val offset = b.text.indexOf("is Some::Type") + 4
        val resolved = b.findReferenceAt(offset)?.resolve()
        assertNull("cross-file resolve without use must still fail outside core", resolved)
    }
}
