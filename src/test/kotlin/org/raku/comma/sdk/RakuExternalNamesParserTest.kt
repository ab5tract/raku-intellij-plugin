package org.raku.comma.sdk

import com.intellij.testFramework.LightVirtualFile
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.psi.RakuPackageDecl
import org.raku.comma.psi.RakuRoutineDecl
import org.raku.comma.psi.RakuVariableDecl
import org.raku.comma.psi.external.ExternalRakuFile
import org.raku.comma.psi.external.ExternalRakuRoutineDecl
import org.raku.comma.psi.symbols.MOPSymbolsAllowed
import org.raku.comma.psi.symbols.RakuSingleResolutionSymbolCollector
import org.raku.comma.psi.symbols.RakuSymbolKind
import org.raku.comma.psi.symbols.RakuVariantsSymbolCollector
import org.raku.comma.utils.RakuUtils
import java.nio.file.Files

/**
 * Characterization tests for the external-symbols JSON contract
 * (raku-core-symbols.raku / raku-module-symbols.raku -> RakuExternalNamesParser
 * -> ExternalRaku* PSI). These pin current behavior ahead of the Kotlin
 * conversion of the psi/external package; assertions go through the PSI
 * interfaces so they are implementation-language-agnostic.
 */
class RakuExternalNamesParserTest : CommaFixtureTestCase() {

    private fun parsed(json: String): Pair<ExternalRakuFile, RakuExternalNamesParser> {
        val file = ExternalRakuFile(project, LightVirtualFile("Test.rakumod"))
        val parser = RakuExternalNamesParser(project, file, json).parse()
        file.setSymbols(parser.result())
        return file to parser
    }

    fun testNativeType() {
        val (_, parser) = parsed("""[{"k":"n","n":"int64","t":"int64"}]""")
        val symbols = parser.result()
        assertEquals(1, symbols.size)
        val psi = symbols[0].psi
        assertTrue(psi is RakuPackageDecl)
        psi as RakuPackageDecl
        assertEquals("int64", psi.name)
        assertEquals("", psi.packageKind)
    }

    fun testVariableWithDocs() {
        val (_, parser) = parsed("""[{"k":"v","n":"${'$'}*OUT","t":"IO::Handle","d":"line one\nline two"}]""")
        val symbols = parser.result()
        assertEquals(1, symbols.size)
        val psi = symbols[0].psi
        assertTrue(psi is RakuVariableDecl)
        psi as RakuVariableDecl
        assertEquals("\$*OUT", psi.name)
        assertEquals("our", psi.scope)
        assertEquals("IO::Handle", psi.inferType().name)
        assertEquals("line one<br>line two", psi.docsString)
    }

    fun testOnlySubRoutine() {
        val (_, parser) = parsed(
            """[{"k":"s","n":"chomp","m":0,"s":{"r":"Str:D","p":[{"n":"${'$'}x","t":"Any"}]}}]""")
        val decl = parser.result().single().psi as RakuRoutineDecl
        assertEquals("chomp", decl.name)
        assertTrue(decl.isSub)
        assertFalse(decl.isMethod)
        assertEquals("sub", decl.routineKind)
        assertEquals("our", decl.scope)
        assertEquals("only", decl.multiness)
        assertEquals("Str", decl.returnType.name)   // ":D" smiley trimmed
        assertEquals("Any \$x --> Str", decl.signature)
        assertFalse(decl.isDeprecated)
        assertFalse(decl.isPure)
    }

    fun testMultiMethodRoutine() {
        val (_, parser) = parsed(
            """[{"k":"m","n":"push","m":1,"s":{"r":"Positional:U","p":[]},"x":"use append","p":true,"rakudo":true,"d":"docs"}]""")
        val decl = parser.result().single().psi as RakuRoutineDecl
        assertTrue(decl.isMethod)
        assertEquals("method", decl.routineKind)
        assertEquals("has", decl.scope)
        assertEquals("multi", decl.multiness)
        assertEquals("Positional", decl.returnType.name)  // ":U" smiley trimmed
        assertTrue(decl.isDeprecated)
        assertEquals("use append", decl.deprecationMessage)
        assertTrue(decl.isPure)
        assertTrue((decl as ExternalRakuRoutineDecl).isImplementationDetail)
        assertEquals("docs", decl.docsString)
    }

    fun testEnumAndSubset() {
        val (_, parser) = parsed(
            """[{"k":"e","n":"Order","t":"Order"},{"k":"ss","n":"UInt","t":"UInt","d":"unsigned"}]""")
        val symbols = parser.result()
        assertEquals(2, symbols.size)
        val enumPsi = symbols[0].psi as RakuPackageDecl
        assertEquals("Order", enumPsi.name)
        assertEquals("class", enumPsi.packageKind)
        val subsetPsi = symbols[1].psi as RakuPackageDecl
        assertEquals("UInt", subsetPsi.name)
        assertEquals("class", subsetPsi.packageKind)
        assertEquals("unsigned", subsetPsi.docsString)
    }

    fun testMetamodelLinking() {
        // An "mm" entry (with the literal "m":null the emitters produce) must be
        // renamed to its "key" and cached, so that a later "c" entry gets it as
        // metaclass via its packageKind.
        val (_, parser) = parsed(
            """[
              {"k":"mm","key":"class","n":"Perl6::Metamodel::ClassHOW","t":"Perl6::Metamodel::ClassHOW","b":"M","m":null,"a":[]},
              {"k":"c","n":"Str","t":"Str","b":"C","mro":["Cool","Any","Mu"],"m":[],"a":[]}
            ]""")
        val packages = parser.packages
        val metamodel = packages["class"]
        assertNotNull(metamodel)
        assertEquals("class", metamodel!!.name)
        assertEquals("Perl6::Metamodel::ClassHOW", metamodel.inferType().name)
        val str = packages["Str"]
        assertNotNull(str)
        assertSame(metamodel, str!!.metaClass)
    }

    fun testNestedRoutinesAttributesAndMOPContribution() {
        val (_, parser) = parsed(
            """[
              {"k":"c","n":"Widget","t":"Widget","b":"A","mro":[],
               "m":[{"k":"m","n":"sink","m":0,"s":{"r":"Any","p":[]}},
                    {"k":"m","n":"!secret","m":0,"s":{"r":"Any","p":[]}},
                    {"k":"m","n":"y","m":0,"s":{"r":"Any","p":[]}}],
               "a":[{"n":"${'$'}!hidden","t":"Int"},{"n":"${'$'}.y","t":"Str"}]}
            ]""")
        val widget = parser.result().single().psi as RakuPackageDecl

        val public = RakuVariantsSymbolCollector(RakuSymbolKind.Method)
        widget.contributeMOPSymbols(public, MOPSymbolsAllowed(false, false, false, false))
        val publicNames = public.variants.map { it.name }
        assertTrue(".sink" in publicNames)
        assertFalse("!secret" in publicNames)
        // Getter-suppression quirk: the parser installs attributes via
        // setAttributes(), which never fills the getters pool, so the routine
        // "y" is contributed even though attribute $.y provides an accessor.
        assertTrue(public.variants.any { it.name == ".y" && it.psi is RakuRoutineDecl })

        val privileged = RakuVariantsSymbolCollector(RakuSymbolKind.Method)
        widget.contributeMOPSymbols(privileged, MOPSymbolsAllowed(true, true, true, false))
        assertTrue("!secret" in privileged.variants.map { it.name })
    }

    fun testParameterSemantics() {
        val (_, parser) = parsed(
            """[{"k":"s","n":"f","m":0,"s":{"r":"Any","p":[
                 {"n":"*%_","t":"Mu"},
                 {"n":":${'$'}x","t":"Any"},
                 {"n":"${'$'}pos?","t":"Any"},
                 {"n":"${'$'}req!","t":"Any"},
                 {"n":"*@rest","t":"Any"},
                 {"n":":${'$'}alias","nn":["alias","other"],"t":"Any"}
               ]}}]""")
        val params = (parser.result().single().psi as RakuRoutineDecl).params
        assertEquals(6, params.size)

        val slurpyNamed = params[0]
        assertTrue(slurpyNamed.isNamed)
        assertFalse(slurpyNamed.isPositional)
        assertTrue(slurpyNamed.isSlurpy)
        assertTrue(slurpyNamed.isOptional)
        assertEquals("%_", slurpyNamed.variableName)

        val named = params[1]
        assertTrue(named.isNamed)
        assertTrue(named.isOptional)
        assertEquals("\$x", named.variableName)

        val optionalPositional = params[2]
        assertTrue(optionalPositional.isPositional)
        assertTrue(optionalPositional.isExplicitlyOptional)
        assertTrue(optionalPositional.isOptional)

        val required = params[3]
        assertTrue(required.isRequired)
        assertFalse(required.isOptional)

        val slurpyPositional = params[4]
        assertTrue(slurpyPositional.isSlurpy)
        assertTrue(slurpyPositional.isPositional)

        assertEquals(listOf("\$alias", "\$other"), params[5].variableNames.toList())
    }

    fun testGarbageAndEmptyInput() {
        assertEmpty(parsed("this is not json").second.result())
        assertEmpty(parsed("[]").second.result())
        assertEmpty(parsed("""[{"k":"zz","n":"mystery"}]""").second.result())
    }

    fun testNqpStyleStringParamsAreTolerated() {
        // symbols/nqp.ops emits signature params as strings, not {n,t} objects;
        // the routine must survive with the string params skipped.
        val (_, parser) = parsed(
            """[{"m":0,"k":"r","s":{"p":["int ${'$'}i "],"r":"--> int"},"n":"nqp::abs_i"}]""")
        val decl = parser.result().single().psi as RakuRoutineDecl
        assertEquals("nqp::abs_i", decl.name)
        assertEmpty(decl.params)
    }

    fun testMalformedElementIsSkippedRestSurvives() {
        // A structurally broken element (routine without its required "s")
        // must not take down the entries before or after it.
        val (_, parser) = parsed(
            """[
              {"k":"v","n":"${'$'}before","t":"Any"},
              {"k":"s","n":"broken","m":0},
              {"k":"v","n":"${'$'}after","t":"Any"}
            ]""")
        assertEquals(
            listOf("\$before", "\$after"),
            parser.result().map { (it.psi as RakuVariableDecl).name })
    }

    fun testMultinessRoutingThroughContributeGlobals() {
        val (file, _) = parsed(
            """[
              {"k":"s","n":"foo","m":1,"s":{"r":"Any","p":[]}},
              {"k":"s","n":"foo","m":1,"s":{"r":"Any","p":[{"n":"${'$'}x","t":"Any"}]}},
              {"k":"s","n":"bar","m":0,"s":{"r":"Any","p":[]}}
            ]""")
        val multi = RakuSingleResolutionSymbolCollector("foo", RakuSymbolKind.Routine)
        file.contributeGlobals(multi, HashSet())
        assertNotNull(multi.result)

        val only = RakuSingleResolutionSymbolCollector("bar", RakuSymbolKind.Routine)
        file.contributeGlobals(only, HashSet())
        assertTrue(only.isSatisfied)
    }

    fun testCoreFallbackRoundTrip() {
        val fallback = RakuUtils.getResourceAsFile("symbols/CORE.fallback")!!
        val (file, parser) = parsed(Files.readString(fallback.toPath()))
        val symbols = parser.result()
        assertEquals(3213, symbols.size)

        val any = RakuSingleResolutionSymbolCollector("Any", RakuSymbolKind.TypeOrConstant)
        file.contributeGlobals(any, HashSet())
        assertTrue(any.isSatisfied)
        val methods = RakuVariantsSymbolCollector(RakuSymbolKind.Method)
        (any.result.psi as RakuPackageDecl)
            .contributeMOPSymbols(methods, MOPSymbolsAllowed(false, false, false, false))
        assertTrue(methods.variants.map { it.name }.contains(".sink"))
    }
}
