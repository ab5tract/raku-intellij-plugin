package org.raku.comma.stub

import com.intellij.psi.StubBuilder
import com.intellij.psi.impl.DebugUtil
import com.intellij.psi.stubs.SerializationManagerEx
import com.intellij.psi.stubs.StubElement
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.psi.stub.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

// Characterization test for the psi/stub package's Java-to-Kotlin conversion (see
// the roadmap plan). RakuStubTest.kt compares two in-memory-built stub trees but
// never actually serializes/deserializes -- the exact mechanism every commit in
// this conversion touches, and the one STUB_VERSION exists to protect. This test
// drives every stub kind through a real StubOutputStream/StubInputStream round
// trip (via the platform's registered StubTreeSerializer, which resolves each
// node's IStubElementType by its externalId) and asserts both tree shape AND
// individual field values survive -- shape alone (what DebugUtil.stubTreeToString
// shows) would not catch a wrong read order or a swapped/dropped field.
class RakuStubSerializationTest : CommaFixtureTestCase() {
    private lateinit var myBuilder: StubBuilder

    override fun setUp() {
        super.setUp()
        myBuilder = RakuFileStubBuilder()
    }

    private fun roundTrip(source: String): StubElement<*> {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, source)
        val file = myFixture.file
        val stubTree = myBuilder.buildStubTree(file)

        val serializer = SerializationManagerEx.getInstanceEx()
        val bytes = ByteArrayOutputStream()
        serializer.serialize(stubTree, bytes)
        val roundTripped = serializer.deserialize(ByteArrayInputStream(bytes.toByteArray())) as StubElement<*>

        assertEquals(DebugUtil.stubTreeToString(stubTree), DebugUtil.stubTreeToString(roundTripped))
        return roundTripped
    }

    fun testConstant() {
        val stub = roundTrip("constant \$foo = 5;").childrenStubs[0] as RakuConstantStub
        assertEquals("\$foo", stub.constantName)
    }

    fun testEnum() {
        val stub = roundTrip("enum Class <Wizard Crusader Priest>;").childrenStubs[0] as RakuEnumStub
        assertEquals("Class", stub.typeName)
        assertTrue(stub.isExported)
        assertEquals("our", stub.scope)
        assertEquals(listOf("Wizard", "Crusader", "Priest"), stub.enumValues)
    }

    fun testRegex() {
        val stub = roundTrip("regex aa <1 2 3 4 5>").childrenStubs[0] as RakuRegexDeclStub
        assertEquals("aa", stub.regexName)
    }

    fun testSubset() {
        val stub = roundTrip("subset Alpha of Int;").childrenStubs[0] as RakuSubsetStub
        assertEquals("Alpha", stub.typeName)
        assertEquals("Int", stub.subsetBaseTypeName)
    }

    fun testNeed() {
        val root = roundTrip("need Foo::Bar; need Foo::Baz;")
        val stub1 = root.childrenStubs[0] as RakuNeedStatementStub
        assertEquals(listOf("Foo::Bar"), stub1.moduleNames)
        val stub2 = root.childrenStubs[1] as RakuNeedStatementStub
        assertEquals(listOf("Foo::Baz"), stub2.moduleNames)
    }

    fun testUse() {
        val root = roundTrip("use Foo::Bar; use Foo::Baz;")
        val stub1 = root.childrenStubs[0] as RakuUseStatementStub
        assertEquals("Foo::Bar", stub1.moduleName)
        val stub2 = root.childrenStubs[1] as RakuUseStatementStub
        assertEquals("Foo::Baz", stub2.moduleName)
    }

    fun testTrait() {
        val root = roundTrip("role One {}; class Two does One {};")
        val trait = root.childrenStubs[1].childrenStubs[0] as RakuTraitStub
        val typeName = trait.childrenStubs[0] as RakuTypeNameStub
        assertEquals("One", typeName.getTypeName())
    }

    fun testVariableAndScopedDecl() {
        val root = roundTrip("has Int \$.bar;")
        val scoped = root.childrenStubs[0] as RakuScopedDeclStub
        assertEquals("has", scoped.scope)
        val typeName = scoped.childrenStubs[0] as RakuTypeNameStub
        assertEquals("Int", typeName.getTypeName())
        val variable = scoped.childrenStubs[1] as RakuVariableDeclStub
        assertEquals("\$!bar", variable.variableNames[0])
        assertEquals("\$.bar", variable.variableNames[1])
        assertEquals("Int", variable.variableType)
    }

    fun testPackageDeclAndRoutineDecl() {
        val root = roundTrip("class Foo { method mm {}; method !kk {}; }")
        val pkg = root.childrenStubs[0] as RakuPackageDeclStub
        assertEquals("class", pkg.packageKind)
        val method1 = pkg.childrenStubs[0] as RakuRoutineDeclStub
        assertFalse(method1.isPrivate)
        assertEquals("method", method1.routineKind)
        assertEquals("mm", method1.routineName)
        val method2 = pkg.childrenStubs[1] as RakuRoutineDeclStub
        assertTrue(method2.isPrivate)
        assertEquals("!kk", method2.routineName)
    }

    fun testSubCall() {
        val root = roundTrip("foo(1, 2);")
        // Sub calls aren't stubbed at top-level statement scope in the same way
        // declarations are; this smoke-tests that a file with a plain call still
        // round-trips its (empty) child stub list without throwing.
        assertNotNull(root)
    }

    // RakuTypeStub.getGlobalName()/getLexicalName() are default methods, inherited
    // by RakuPackageDeclStub/RakuEnumStub/RakuSubsetStub, with no existing direct
    // test coverage (only indirect, through RakuGlobalTypeStubIndex/
    // RakuLexicalTypeStubIndex population). Pin both branches here, through the
    // round-tripped (not just freshly-built) stub, so a future RakuTypeStub
    // conversion is checked against real post-serialization data too.
    fun testGlobalNameAcrossPackageNesting() {
        val root = roundTrip("class Outer { class Inner {} }")
        val outer = root.childrenStubs[0] as RakuPackageDeclStub
        assertEquals("Outer", outer.globalName)
        val inner = outer.childrenStubs[0] as RakuPackageDeclStub
        assertEquals("Outer::Inner", inner.globalName)
    }

    fun testMyScopedPackageBehavesLikeOurForNaming() {
        // Surprising but confirmed real behavior, not a test bug: RakuScopedDeclStubElementType
        // .shouldCreateStub() only ever creates a RakuScopedDeclStub wrapper for "has" or
        // exported-"our" declarations (see its source) -- "my" is explicitly excluded. So
        // RakuPackageDeclStubImpl.getScope()'s "is my parent stub scoped my?" check can never
        // see a match, and a `my class Foo {}` is indistinguishable, at the stub level, from
        // plain `class Foo {}`: getGlobalName()'s "my" short-circuit is unreachable dead code
        // for packages. Pinning this as-is; not something to fix as part of this conversion.
        val root = roundTrip("my class Outer { class Inner {} }")
        val outer = root.childrenStubs[0] as RakuPackageDeclStub
        assertEquals("Outer", outer.globalName)
        assertNull(outer.lexicalName)
        val inner = outer.childrenStubs[0] as RakuPackageDeclStub
        assertEquals("Outer::Inner", inner.globalName)
        assertNull(inner.lexicalName)
    }

    fun testLexicalNameIsAlwaysNullForPackages() {
        // Consequence of the same dead-code path: getLexicalName() only ever returns
        // non-null by finding a "my"-scoped RakuScopedDeclStub ancestor, which never
        // happens for packages -- so it's always null regardless of nesting or "my"/"our".
        val root = roundTrip("class Outer { class Inner {} }")
        val outer = root.childrenStubs[0] as RakuPackageDeclStub
        assertNull(outer.lexicalName)
        val inner = outer.childrenStubs[0] as RakuPackageDeclStub
        assertNull(inner.lexicalName)
    }
}
