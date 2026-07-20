package org.raku.comma

import org.raku.comma.psi.symbols.RakuSymbolKind

// Temporary diagnostic: verifies CORE.setting symbols actually load and
// resolve inside the test sandbox. Delete once the suite is stable.
class CoreSettingDiagTest : CommaFixtureTestCase() {
    fun testParserDirectly() {
        val fallback = org.raku.comma.utils.RakuUtils.getResourceAsFile("symbols/CORE.fallback")!!
        val json = java.nio.file.Files.readString(fallback.toPath())
        val file = org.raku.comma.psi.external.ExternalRakuFile(
            project, com.intellij.testFramework.LightVirtualFile("SETTINGS.rakumod"))
        val parser = org.raku.comma.sdk.RakuExternalNamesParser(project, file, json).parse()
        val symbols = parser.result()
        println("DIAG parsed symbol count=${symbols.size}")
        file.setSymbols(symbols)
        val sym = file.resolveLexicalSymbol(org.raku.comma.psi.symbols.RakuSymbolKind.TypeOrConstant, "Any")
        println("DIAG direct-parse resolve Any -> $sym")
    }

    fun testCoreSettingResolves() {
        val setting = sdkService.symbolCache.getCoreSettingFile()
        println("DIAG setting name=${setting?.name} class=${setting?.javaClass?.simpleName}")
        for (name in listOf("Any", "Mu", "IO::Socket::Async")) {
            val collector = org.raku.comma.psi.symbols.RakuSingleResolutionSymbolCollector(
                name, RakuSymbolKind.TypeOrConstant)
            setting!!.contributeGlobals(collector, HashSet())
            println("DIAG contributeGlobals $name -> satisfied=${collector.isSatisfied}")
            if (collector.isSatisfied && name == "Any") {
                val decl = collector.result.psi as org.raku.comma.psi.RakuPackageDecl
                val methods = org.raku.comma.psi.symbols.RakuVariantsSymbolCollector(
                    org.raku.comma.psi.symbols.RakuSymbolKind.Method)
                decl.contributeMOPSymbols(methods, org.raku.comma.psi.symbols.MOPSymbolsAllowed(false, false, false, false))
                val names = methods.variants.map { it.name }
                println("DIAG Any methods count=${names.size} hasSink=${names.contains(".sink")} hasMinpairs=${names.contains(".minpairs")}")
            }
        }
        assertNotNull(setting)
        assertFalse("DUMMY" == setting!!.name)
    }
}
