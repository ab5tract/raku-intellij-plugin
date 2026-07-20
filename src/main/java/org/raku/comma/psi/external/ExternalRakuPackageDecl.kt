package org.raku.comma.psi.external

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import org.raku.comma.psi.RakuFile
import org.raku.comma.psi.RakuPackageDecl
import org.raku.comma.psi.RakuParameter
import org.raku.comma.psi.RakuRoutineDecl
import org.raku.comma.psi.RakuVariable
import org.raku.comma.psi.RakuVariableDecl
import org.raku.comma.psi.stub.RakuPackageDeclStub
import org.raku.comma.psi.symbols.MOPSymbolsAllowed
import org.raku.comma.psi.symbols.RakuSymbolCollector
import org.raku.comma.psi.symbols.RakuSymbolKind
import org.raku.comma.psi.type.RakuResolvedType
import org.raku.comma.psi.type.RakuType

class ExternalRakuPackageDecl(
    project: Project,
    file: RakuFile,
    kind: String,
    name: String,
    private val myType: String,
    @Suppress("UNUSED_PARAMETER") base: String,
) : RakuExternalPsiElement(project, file), RakuPackageDecl {

    private val myPackageKind: String = when (kind) {
        "ro" -> "role"
        "c" -> "class"
        else -> ""
    }
    private var myName: String = name
    private var myRoutines: List<RakuRoutineDecl> = ArrayList()
    private var myAttributes: List<RakuVariableDecl> = ArrayList()
    private var myMRO: List<String> = ArrayList()
    private val myGettersPool = HashSet<String>()
    private var myMetaClass: RakuPackageDecl? = null

    constructor(
        project: Project,
        file: RakuFile,
        kind: String,
        name: String,
        type: String,
        base: String,
        routines: List<RakuRoutineDecl>,
        attrs: List<RakuVariableDecl>,
        mro: List<String>,
        metaClass: RakuPackageDecl?,
    ) : this(project, file, kind, name, type, base) {
        myMetaClass = metaClass
        myMRO = mro
        myRoutines = routines
        myAttributes = attrs
        for (decl in myAttributes) {
            for (getterName in decl.variableNames) {
                if (RakuVariable.getTwigil(getterName) == '.') {
                    myGettersPool.add(getterName.substring(2)) // cut off sigil
                }
            }
        }
    }

    override fun setMetaClass(metaClass: RakuPackageDecl?) {
        myMetaClass = metaClass
    }

    override fun getMetaClass(): RakuPackageDecl? = myMetaClass

    override fun getPackageKind(): String = myPackageKind

    override fun getPackageName(): String = name

    override fun isStubbed(): Boolean = false

    override fun setName(name: String): PsiElement? {
        myName = name
        return null
    }

    override fun getName(): String = myName

    override fun getPackageKeywordNode(): PsiElement? = null

    override fun contributeNestedPackagesWithPrefix(collector: RakuSymbolCollector, prefix: String) {}

    override fun collectChildren(): List<RakuPackageDecl> = ArrayList()

    override fun collectParents(): List<RakuPackageDecl> = ArrayList()

    override fun trustsOthers(): Boolean = false

    override fun getElementType(): IStubElementType<*, *>? = null

    override fun getStub(): RakuPackageDeclStub? = null

    override fun getScope(): String = "our"

    override fun getNameIdentifier(): PsiElement? = null

    override fun contributeLexicalSymbols(collector: RakuSymbolCollector) {}

    override fun inferType(): RakuType = RakuResolvedType(myType, this)

    override fun contributeMOPSymbols(collector: RakuSymbolCollector, symbolsAllowed: MOPSymbolsAllowed) {
        for (routine in myRoutines) {
            val name = routine.routineName
            if (!symbolsAllowed.privateMethodsVisible && name.startsWith("!")) continue
            if (!symbolsAllowed.submethodsVisible && routine.routineKind == "submethod") continue
            if (myGettersPool.contains(name)) continue
            routine.contributeMOPSymbols(collector, symbolsAllowed)
            if (collector.isSatisfied) return
        }
        for (variable in myAttributes) {
            variable.contributeMOPSymbols(collector, symbolsAllowed)
            if (collector.isSatisfied) return
        }
        for (mroParent in myMRO) {
            val parent = resolveLexicalSymbol(RakuSymbolKind.TypeOrConstant, mroParent)
            val decl = parent?.psi
            if (decl is RakuPackageDecl) {
                decl.contributeMOPSymbols(collector, symbolsAllowed)
            }
        }
        val metaClass = getMetaClass() ?: return

        collector.decreasePriority()
        metaClass.contributeMOPSymbols(collector, symbolsAllowed)
    }

    fun setRoutines(routines: List<RakuRoutineDecl>) {
        myRoutines = routines
    }

    fun setAttributes(attributes: List<RakuVariableDecl>) {
        myAttributes = attributes
    }

    override fun getSignature(): Array<RakuParameter> {
        // TODO
        return emptyArray()
    }
}
