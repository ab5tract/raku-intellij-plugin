package org.raku.comma.stub

import com.intellij.lang.FileASTNode
import com.intellij.psi.PsiFile
import com.intellij.psi.StubBuilder
import com.intellij.psi.impl.DebugUtil
import com.intellij.psi.stubs.StubElement
import com.intellij.util.containers.ContainerUtil
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.psi.RakuPackageDecl
import org.raku.comma.psi.stub.*
import org.raku.comma.psi.symbols.MOPSymbolsAllowed
import org.raku.comma.psi.symbols.RakuSymbol
import org.raku.comma.psi.symbols.RakuSymbolKind
import org.raku.comma.psi.symbols.RakuVariantsSymbolCollector

import java.util.Arrays
import java.util.Collections

class RakuStubTest : CommaFixtureTestCase() {
    private lateinit var myBuilder: StubBuilder

    override fun setUp() {
        super.setUp()
        myBuilder = RakuFileStubBuilder()
    }

    fun testEmpty() {
        doTest("",
                "RakuFileStubImpl\n")
    }

    fun testConstant() {
        val e = doTest("constant \$foo = 5;",
                "RakuFileStubImpl\n" +
                        "  CONSTANT:RakuConstantStubImpl\n")
        val childrenStubs = e.getChildrenStubs()
        assertEquals(1, childrenStubs.size)
        val stub = childrenStubs.get(0) as RakuConstantStub
        assertEquals("\$foo", stub.getConstantName())
    }

    fun testEnum() {
        val e = doTest("enum Class <Wizard Crusader Priest>;",
                "RakuFileStubImpl\n" +
                        "  ENUM:RakuEnumStubImpl\n")
        val childrenStubs = e.getChildrenStubs()
        assertEquals(1, childrenStubs.size)
        val enumStub = childrenStubs.get(0) as RakuEnumStub
        assertEquals("Class", enumStub.getTypeName())
        assertTrue(enumStub.isExported())
        assertEquals("our", enumStub.getScope())
        assertEquals(Arrays.asList("Wizard", "Crusader", "Priest"), enumStub.getEnumValues())
    }

    fun testRegex() {
        val e = doTest("regex aa <1 2 3 4 5>",
                "RakuFileStubImpl\n" +
                        "  REGEX_DECLARATION:RakuRegexDeclStubImpl\n")
        val childrenStubs = e.getChildrenStubs()
        assertEquals(1, childrenStubs.size)
        val stub = childrenStubs.get(0) as RakuRegexDeclStub
        assertEquals("aa", stub.getRegexName())
    }

    fun testSubset() {
        val e = doTest("subset Alpha of Int;",
                "RakuFileStubImpl\n" +
                        "  SUBSET:RakuSubsetStubImpl\n" +
                        "    TYPE_NAME:RakuTypeNameStubImpl\n")
        val childrenStubs = e.getChildrenStubs()
        assertEquals(1, childrenStubs.size)
        val stub = childrenStubs.get(0) as RakuSubsetStub
        assertEquals("Alpha", stub.getTypeName())
        assertEquals("Int", stub.getSubsetBaseTypeName())
    }

    fun testNeed() {
        val e = doTest("need Foo::Bar; need Foo::Baz;",
                "RakuFileStubImpl\n" +
                        "  NEED_STATEMENT:RakuNeedStatementStubImpl\n" +
                        "  NEED_STATEMENT:RakuNeedStatementStubImpl\n")
        val childrenStubs = e.getChildrenStubs()
        assertEquals(2, childrenStubs.size)
        val stub1 = childrenStubs.get(0) as RakuNeedStatementStub
        assertEquals(Collections.singletonList("Foo::Bar"), stub1.getModuleNames())
        val stub2 = childrenStubs.get(1) as RakuNeedStatementStub
        assertEquals(Collections.singletonList("Foo::Baz"), stub2.getModuleNames())
    }

    fun testUse() {
        val e = doTest("use Foo::Bar; use Foo::Baz;",
                "RakuFileStubImpl\n" +
                        "  USE_STATEMENT:RakuUseStatementStubImpl\n" +
                        "  USE_STATEMENT:RakuUseStatementStubImpl\n")
        val childrenStubs = e.getChildrenStubs()
        assertEquals(2, childrenStubs.size)
        val stub1 = childrenStubs.get(0) as RakuUseStatementStub
        assertEquals("Foo::Bar", stub1.getModuleName())
        val stub2 = childrenStubs.get(1) as RakuUseStatementStub
        assertEquals("Foo::Baz", stub2.getModuleName())
    }

    fun testVariableScopedDependantStubbing() {
        doTest("my \$foo; our \$baz",
                "RakuFileStubImpl\n" +
                "  SCOPED_DECLARATION:RakuScopedDeclStubImpl\n" +
                "    VARIABLE_DECLARATION:RakuVariableDeclStubImpl\n")
        doTest("has \$foo;",
                "RakuFileStubImpl\n" +
                        "  SCOPED_DECLARATION:RakuScopedDeclStubImpl\n" +
                        "    VARIABLE_DECLARATION:RakuVariableDeclStubImpl\n")
        doTest("our \$bar is export = 10;",
                "RakuFileStubImpl\n" +
                        "  SCOPED_DECLARATION:RakuScopedDeclStubImpl\n" +
                        "    VARIABLE_DECLARATION:RakuVariableDeclStubImpl\n" +
                        "      TRAIT:RakuTraitStubImpl\n")
    }

    fun testClassWithAttributesAndMethods() {
        val e = doTest("class Foo { method mm {}; method !kk {}; has \$!foo; has Int \$.bar }",
                "RakuFileStubImpl\n" +
                        "  PACKAGE_DECLARATION:RakuPackageDeclStubImpl\n" +
                        "    ROUTINE_DECLARATION:RakuRoutineDeclStubImpl\n" +
                        "    ROUTINE_DECLARATION:RakuRoutineDeclStubImpl\n" +
                        "    SCOPED_DECLARATION:RakuScopedDeclStubImpl\n" +
                        "      VARIABLE_DECLARATION:RakuVariableDeclStubImpl\n" +
                        "    SCOPED_DECLARATION:RakuScopedDeclStubImpl\n" +
                        "      TYPE_NAME:RakuTypeNameStubImpl\n" +
                        "      VARIABLE_DECLARATION:RakuVariableDeclStubImpl\n")
        val childrenStubs = e.getChildrenStubs()
        assertEquals(1, childrenStubs.size)
        assertTrue(childrenStubs.get(0) is RakuPackageDeclStub)
        val packageDeclStub = childrenStubs.get(0) as RakuPackageDeclStub
        assertEquals("class", packageDeclStub.getPackageKind())
        val packageChildren = packageDeclStub.getChildrenStubs()
        assertEquals(4, packageChildren.size)
        val routine1 = packageChildren.get(0) as RakuRoutineDeclStub
        val routine2 = packageChildren.get(1) as RakuRoutineDeclStub
        assertFalse(routine1.isPrivate())
        assertEquals("method", routine1.getRoutineKind())
        assertEquals("mm", routine1.getRoutineName())
        assertTrue(routine2.isPrivate())
        assertEquals("method", routine2.getRoutineKind())
        assertEquals("!kk", routine2.getRoutineName())

        val scopedDeclStub1 = packageChildren.get(2) as RakuScopedDeclStub
        val scopedDeclStub2 = packageChildren.get(3) as RakuScopedDeclStub

        // Test of first attr
        assertEquals("has", scopedDeclStub1.getScope())
        val attr1Children = scopedDeclStub1.getChildrenStubs()
        assertEquals(1, attr1Children.size)
        val variableDeclStub1 = attr1Children.get(0) as RakuVariableDeclStub
        assertEquals("\$!foo", variableDeclStub1.getVariableNames()[0])
        assertEquals("Any", variableDeclStub1.getVariableType())

        // Test of second attr
        assertEquals("has", scopedDeclStub2.getScope())
        val attr2Children = scopedDeclStub2.getChildrenStubs()
        assertEquals(2, attr2Children.size)
        val typeNameStub = attr2Children.get(0) as RakuTypeNameStub
        assertEquals("Int", typeNameStub.getTypeName())
        val variableDeclStub2 = attr2Children.get(1) as RakuVariableDeclStub
        assertEquals("\$!bar", variableDeclStub2.getVariableNames()[0])
        assertEquals("\$.bar", variableDeclStub2.getVariableNames()[1])
        assertEquals("Int", variableDeclStub2.getVariableType())
    }

    fun testPackageTrait() {
        val e = doTest("role One {}; class Two does One {}; class Three is Two {};",
                "RakuFileStubImpl\n" +
                        "  PACKAGE_DECLARATION:RakuPackageDeclStubImpl\n" +
                        "  PACKAGE_DECLARATION:RakuPackageDeclStubImpl\n" +
                        "    TRAIT:RakuTraitStubImpl\n" +
                        "      TYPE_NAME:RakuTypeNameStubImpl\n" +
                        "  PACKAGE_DECLARATION:RakuPackageDeclStubImpl\n" +
                        "    TRAIT:RakuTraitStubImpl\n")
        val childrenStubs = e.getChildrenStubs()
        assertEquals(3, childrenStubs.size)
    }

    fun testAlso() {
        val e = doTest("role One {}; class Base {}; class Two { also does One; also is Base; }",
                "RakuFileStubImpl\n" +
                        "  PACKAGE_DECLARATION:RakuPackageDeclStubImpl\n" +
                        "  PACKAGE_DECLARATION:RakuPackageDeclStubImpl\n" +
                        "  PACKAGE_DECLARATION:RakuPackageDeclStubImpl\n" +
                        "    TRAIT:RakuTraitStubImpl\n" +
                        "      TYPE_NAME:RakuTypeNameStubImpl\n" +
                        "    TRAIT:RakuTraitStubImpl\n")
    }

    fun testTrusts() {
        val e = doTest("class Two { trusts One; trusts Base; }",
                "RakuFileStubImpl\n" +
                        "  PACKAGE_DECLARATION:RakuPackageDeclStubImpl\n" +
                        "    TYPE_NAME:RakuTypeNameStubImpl\n" +
                        "    TYPE_NAME:RakuTypeNameStubImpl\n")
    }

    fun testEmptyConstant() {
        doTest("constant = 1;",
               "RakuFileStubImpl\n")
    }

    fun testStubbedRoleUsageInComposition() {
        val e = doTest("my role Base { method mmm {}; method bbb {}; }; class C does Base { method ddd {}; };",
                "RakuFileStubImpl\n" +
                        "  PACKAGE_DECLARATION:RakuPackageDeclStubImpl\n" +
                        "    ROUTINE_DECLARATION:RakuRoutineDeclStubImpl\n" +
                        "    ROUTINE_DECLARATION:RakuRoutineDeclStubImpl\n" +
                        "  PACKAGE_DECLARATION:RakuPackageDeclStubImpl\n" +
                        "    TRAIT:RakuTraitStubImpl\n" +
                        "      TYPE_NAME:RakuTypeNameStubImpl\n" +
                        "    ROUTINE_DECLARATION:RakuRoutineDeclStubImpl\n")
        val stub = e.getChildrenStubs().get(1)
        val decl = stub.psi as RakuPackageDecl
        val collector = RakuVariantsSymbolCollector(RakuSymbolKind.Method)
        decl.contributeMOPSymbols(collector, MOPSymbolsAllowed(false, false, false, false))
        val names = ContainerUtil.map(collector.getVariants(), RakuSymbol::getName)
        assertTrue(names.containsAll(Arrays.asList(".ddd", ".mmm", ".bbb")))
    }

    private fun doTest(source: String, expected: String): StubElement<*> {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, source)
        val file = myFixture.file
        assertNotNull(file.node)

        val stubTree = myBuilder.buildStubTree(file)

        file.node.getChildren(null) // force switch to AST
        val astBasedTree = myBuilder.buildStubTree(file)

        assertEquals(expected, DebugUtil.stubTreeToString(stubTree))
        assertEquals(expected, DebugUtil.stubTreeToString(astBasedTree))
        return stubTree
    }
}
