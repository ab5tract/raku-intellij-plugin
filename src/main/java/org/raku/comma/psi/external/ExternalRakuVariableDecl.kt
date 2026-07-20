package org.raku.comma.psi.external

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import org.raku.comma.psi.RakuVariable
import org.raku.comma.psi.RakuVariableDecl
import org.raku.comma.psi.stub.RakuVariableDeclStub
import org.raku.comma.psi.symbols.MOPSymbolsAllowed
import org.raku.comma.psi.symbols.RakuExplicitAliasedSymbol
import org.raku.comma.psi.symbols.RakuExplicitSymbol
import org.raku.comma.psi.symbols.RakuSymbolCollector
import org.raku.comma.psi.symbols.RakuSymbolKind
import org.raku.comma.psi.type.RakuType
import org.raku.comma.psi.type.RakuUnresolvedType

class ExternalRakuVariableDecl(
    project: Project,
    parent: PsiElement?,
    private val myName: String,
    private val myScope: String,
    private val myType: String,
) : RakuExternalPsiElement(project, parent), RakuVariableDecl {

    override fun getName(): String = myName

    override fun getVariableNames(): Array<String> = arrayOf(name)

    override fun getVariables(): Array<RakuVariable> = emptyArray()

    override fun hasInitializer(): Boolean = false

    override fun getInitializer(variable: RakuVariable): PsiElement? = null

    override fun getInitializer(): PsiElement? = null

    override fun removeVariable(variable: RakuVariable) {}

    override fun getElementType(): IStubElementType<*, *>? = null

    override fun getStub(): RakuVariableDeclStub? = null

    override fun getScope(): String = myScope

    override fun inferType(): RakuType = RakuUnresolvedType(myType)

    override fun getNameIdentifier(): PsiElement? = null

    override fun contributeLexicalSymbols(collector: RakuSymbolCollector) {
        if (scope == "has") return

        val name = name
        if (name.length <= 1) return

        // Our scoped term definitions are not yet implemented in rakudo
        collector.offerSymbol(RakuExplicitSymbol(RakuSymbolKind.Variable, this))
        if (collector.isSatisfied) return
        if (name.startsWith("&")) {
            collector.offerSymbol(RakuExplicitAliasedSymbol(RakuSymbolKind.Routine, this, name.substring(1)))
        }
    }

    override fun contributeMOPSymbols(collector: RakuSymbolCollector, symbolsAllowed: MOPSymbolsAllowed) {
        if (scope != "has") return

        val name = name
        if (name.length < 3) return

        if (RakuVariable.getTwigil(name) == '!' && symbolsAllowed.privateAttributesVisible) {
            collector.offerSymbol(RakuExplicitSymbol(RakuSymbolKind.Variable, this))
        } else if (RakuVariable.getTwigil(name) == '.') {
            collector.offerSymbol(RakuExplicitSymbol(RakuSymbolKind.Variable, this))
            if (collector.isSatisfied) return
            if (symbolsAllowed.privateAttributesVisible) {
                collector.offerSymbol(RakuExplicitAliasedSymbol(
                    RakuSymbolKind.Variable, this, "${name[0]}!${name.substring(2)}"))
                if (collector.isSatisfied) return
            }
            // Offer self.foo;
            collector.offerMultiSymbol(RakuExplicitAliasedSymbol(
                RakuSymbolKind.Method, this, "." + name.substring(2)), false)
        }
    }
}
