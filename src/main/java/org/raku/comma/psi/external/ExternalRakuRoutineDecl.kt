package org.raku.comma.psi.external

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import org.raku.comma.psi.RakuParameter
import org.raku.comma.psi.RakuRoutineDecl
import org.raku.comma.psi.RakuSignature
import org.raku.comma.psi.stub.RakuRoutineDeclStub
import org.raku.comma.psi.symbols.MOPSymbolsAllowed
import org.raku.comma.psi.symbols.RakuExplicitAliasedSymbol
import org.raku.comma.psi.symbols.RakuSymbolCollector
import org.raku.comma.psi.symbols.RakuSymbolKind
import org.raku.comma.psi.type.RakuType
import org.raku.comma.psi.type.RakuUnresolvedType
import org.raku.comma.sdk.SignatureJson

class ExternalRakuRoutineDecl(
    project: Project,
    parent: PsiElement?,
    private val myKind: String,
    private val myScope: String,
    private val myName: String,
    private val myIsMulti: String,
    private val myDeprecationMessage: String?,
    signature: SignatureJson,
    private val myIsPure: Boolean,
) : RakuExternalPsiElement(project, parent), RakuRoutineDecl {

    private val mySignature: RakuSignature = ExternalRakuSignature(project, parent, signature)
    private val myReturnType: String

    var isImplementationDetail: Boolean = false

    init {
        var returnType = signature.r
        if (returnType.endsWith(":D") || returnType.endsWith(":U")) {
            returnType = returnType.substring(0, returnType.length - 2)
        }
        myReturnType = returnType
    }

    override fun getRoutineKind(): String = when (myKind) {
        "m" -> "method"
        "sm" -> "submethod"
        else -> "sub"
    }

    override fun isMethod(): Boolean = !isSub

    override fun isSub(): Boolean = !(myKind == "m" || myKind == "sm")

    override fun getRoutineName(): String = name

    override fun getName(): String = myName

    override fun isPrivate(): Boolean = myName.startsWith("!")

    override fun isStubbed(): Boolean = false

    override fun getContent(): Array<PsiElement> = PsiElement.EMPTY_ARRAY

    override fun getParams(): Array<RakuParameter> = mySignature.parameters

    override fun getChildren(): Array<PsiElement> = arrayOf<PsiElement>(mySignature)

    override fun getMultiness(): String = myIsMulti

    override fun getDeclaratorNode(): PsiElement? = null

    override fun getElementType(): IStubElementType<*, *>? = null

    override fun getStub(): RakuRoutineDeclStub? = null

    override fun getScope(): String = myScope

    override fun getNameIdentifier(): PsiElement? = null

    override fun getSignature(): String = mySignature.summary(RakuUnresolvedType(myReturnType))

    override fun getSignatureNode(): RakuSignature? = mySignature

    override fun getReturnsTrait(): String? = null

    override fun getReturnType(): RakuType = RakuUnresolvedType(myReturnType)

    override fun contributeLexicalSymbols(collector: RakuSymbolCollector) {}

    override fun contributeMOPSymbols(collector: RakuSymbolCollector, symbolsAllowed: MOPSymbolsAllowed) {
        if (myScope != "has" || myName == "<anon>") return
        if (!symbolsAllowed.privateMethodsVisible && myName.startsWith("!")) return
        if (!symbolsAllowed.submethodsVisible && myKind == "submethod") return

        val sym = RakuExplicitAliasedSymbol(
            RakuSymbolKind.Method, this,
            if (myName.startsWith("!")) myName else ".$myName")
        if (myIsMulti == "only") {
            collector.offerSymbol(sym)
        } else {
            collector.offerMultiSymbol(sym, false)
        }
    }

    override fun isDeprecated(): Boolean = myDeprecationMessage != null

    override fun getDeprecationMessage(): String? = myDeprecationMessage

    override fun isPure(): Boolean = myIsPure
}
