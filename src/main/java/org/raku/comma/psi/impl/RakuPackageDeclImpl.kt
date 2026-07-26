package org.raku.comma.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.meta.PsiMetaData
import com.intellij.psi.meta.PsiMetaOwner
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.Stub
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ArrayUtil
import org.raku.comma.highlighter.RakuElementVisitor
import org.raku.comma.pod.PodDomBuildingContext
import org.raku.comma.pod.PodDomClassyDeclarator
import org.raku.comma.psi.RakuElementFactory
import org.raku.comma.psi.RakuEnum
import org.raku.comma.psi.RakuFile
import org.raku.comma.psi.RakuIsTraitName
import org.raku.comma.psi.RakuMultiDecl
import org.raku.comma.psi.RakuPackageDecl
import org.raku.comma.psi.RakuParameter
import org.raku.comma.psi.RakuPsiElement
import org.raku.comma.psi.RakuRegexDecl
import org.raku.comma.psi.RakuRoleSignature
import org.raku.comma.psi.RakuRoutineDecl
import org.raku.comma.psi.RakuScopedDecl
import org.raku.comma.psi.RakuStatementList
import org.raku.comma.psi.RakuStubCode
import org.raku.comma.psi.RakuSubset
import org.raku.comma.psi.RakuTrait
import org.raku.comma.psi.RakuTrusts
import org.raku.comma.psi.RakuTypeName
import org.raku.comma.psi.RakuTypeStubBasedPsi
import org.raku.comma.psi.RakuVariableDecl
import org.raku.comma.psi.stub.RakuPackageDeclStub
import org.raku.comma.psi.stub.RakuRegexDeclStub
import org.raku.comma.psi.stub.RakuRoutineDeclStub
import org.raku.comma.psi.stub.RakuScopedDeclStub
import org.raku.comma.psi.stub.RakuTraitStub
import org.raku.comma.psi.stub.RakuTypeNameStub
import org.raku.comma.psi.stub.RakuVariableDeclStub
import org.raku.comma.psi.stub.index.RakuGlobalTypeStubIndex
import org.raku.comma.psi.stub.index.RakuIndexableType
import org.raku.comma.psi.stub.index.RakuLexicalTypeStubIndex
import org.raku.comma.psi.symbols.MOPSymbolsAllowed
import org.raku.comma.psi.symbols.RakuExplicitAliasedSymbol
import org.raku.comma.psi.symbols.RakuExplicitSymbol
import org.raku.comma.psi.symbols.RakuImplicitSymbol
import org.raku.comma.psi.symbols.RakuMOPSymbolContributor
import org.raku.comma.psi.symbols.RakuSingleResolutionSymbolCollector
import org.raku.comma.psi.symbols.RakuSymbolCollector
import org.raku.comma.psi.symbols.RakuSymbolKind
import org.raku.comma.sdk.RakuSdkUtil
import org.raku.comma.services.project.RakuProjectSdkService
import java.util.ArrayDeque
import java.util.Queue

class RakuPackageDeclImpl : RakuTypeStubBasedPsi<RakuPackageDeclStub>, RakuPackageDecl, PsiMetaOwner {

    private var cachedTrustsOthers: Boolean? = null

    constructor(node: ASTNode) : super(node)

    constructor(stub: RakuPackageDeclStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun setMetaClass(metaClass: RakuPackageDecl?) {}

    override fun getMetaClass(): RakuPackageDecl? {
        try {
            val collector = RakuSingleResolutionSymbolCollector(packageKind, RakuSymbolKind.TypeOrConstant)
            applyLexicalSymbolCollector(collector)
            val psi = if (collector.isSatisfied) collector.result.psi else null
            if (psi is RakuPackageDecl) {
                return psi
            }
        } catch (ignored: AssertionError) {
            // If resolution goes out of a stub, we cannot do a lot without breaking stub rules
        }
        return null
    }

    override fun getPackageKind(): String {
        val stub = stub
        if (stub != null) {
            return stub.packageKind
        }

        val declarator = declarator
        return declarator?.text ?: "package"
    }

    override fun getPackageName(): String? = name

    override fun isStubbed(): Boolean {
        val list = PsiTreeUtil.findChildOfType(this, RakuStatementList::class.java) ?: return false
        return list.children.any { it.firstChild is RakuStubCode }
    }

    override fun getPackageKeywordNode(): PsiElement? = declarator

    override fun getSignature(): Array<RakuParameter>? {
        val signature = PsiTreeUtil.getChildOfType(this, RakuRoleSignature::class.java)
            ?: return emptyArray()
        return PsiTreeUtil.getChildrenOfType(signature, RakuParameter::class.java)
    }

    override fun toString(): String = javaClass.simpleName + "(Raku:PACKAGE_DECLARATION)"

    override fun contributeScopeSymbols(collector: RakuSymbolCollector) {
        packageName ?: return
        collector.offerSymbol(RakuExplicitAliasedSymbol(RakuSymbolKind.Variable, this, "\$?PACKAGE"))
        if (collector.isSatisfied) return
        when (packageKind) {
            "class", "grammar" ->
                collector.offerSymbol(RakuExplicitAliasedSymbol(RakuSymbolKind.Variable, this, "\$?CLASS"))
            "role" -> {
                collector.offerSymbol(RakuExplicitAliasedSymbol(RakuSymbolKind.Variable, this, "\$?ROLE"))
                collector.offerSymbol(RakuImplicitSymbol(RakuSymbolKind.Variable, "\$?CLASS", this))
            }
        }
    }

    override fun contributeLexicalSymbols(collector: RakuSymbolCollector) {
        super.contributeLexicalSymbols(collector)
        contributeNestedPackagesWithPrefix(collector, "$packageName::")
    }

    override fun contributeMOPSymbols(collector: RakuSymbolCollector, symbolsAllowed: MOPSymbolsAllowed) {
        contributeInternals(collector, symbolsAllowed)
        if (collector.isSatisfied) return
        collector.decreasePriority()
        val packageName = packageName
        if (packageName != null && !collector.shouldTraverse(packageName)) return
        contributeFromElders(collector, symbolsAllowed)

        val metaClass = getMetaClass()
        if (metaClass != null) {
            collector.decreasePriority()
            metaClass.contributeMOPSymbols(collector, symbolsAllowed)
        }
    }

    // TODO Re-instate trusts support somehow
    private fun getTrusts(): List<String> {
        val stub = stub
        if (stub != null) {
            return stub.childrenStubs.filterIsInstance<RakuTypeNameStub>().map { it.getTypeName() }
        }
        val statementList = PsiTreeUtil.findChildOfType(this, RakuStatementList::class.java) ?: return emptyList()
        return statementList.children
            .map { it.firstChild }
            .filterIsInstance<RakuTrusts>()
            .map { it.typeName }
    }

    override fun trustsOthers(): Boolean {
        val cached = cachedTrustsOthers ?: getTrusts().isNotEmpty().also { cachedTrustsOthers = it }
        return cached
    }

    override fun subtreeChanged() {
        cachedTrustsOthers = null
    }

    /**
     * Enumerates the MOP-relevant member declarations (routines, has-scoped
     * variable declarations, regex declarations) from the stub tree when
     * present, else from the statement list. Visibility gating (privates,
     * submethods, has-scope) lives inside each member's own
     * contributeMOPSymbols, driven by stub-aware getters, so the walk needs no
     * gates of its own.
     */
    private fun memberContributors(): List<RakuMOPSymbolContributor> {
        val stub = stub
        if (stub != null) {
            val members = ArrayList<RakuMOPSymbolContributor>()
            for (nested in stub.childrenStubs) {
                when (nested) {
                    is RakuRoutineDeclStub -> members.add(nested.psi)
                    is RakuScopedDeclStub ->
                        nested.childrenStubs
                            .filterIsInstance<RakuVariableDeclStub>()
                            .forEach { members.add(it.psi) }
                    is RakuRegexDeclStub -> members.add(nested.psi)
                }
            }
            return members
        }

        val list = PsiTreeUtil.findChildOfType(this, RakuStatementList::class.java) ?: return emptyList()
        val members = ArrayList<RakuMOPSymbolContributor>()
        for (child in list.children) {
            when (val firstChild = child.firstChild) {
                is RakuRoutineDecl -> members.add(firstChild)
                is RakuMultiDecl ->
                    PsiTreeUtil.getChildOfType(firstChild, RakuRoutineDecl::class.java)
                        ?.let { members.add(it) }
                is RakuScopedDecl ->
                    if (firstChild.scope == "has") {
                        PsiTreeUtil.getChildOfType(firstChild, RakuVariableDecl::class.java)
                            ?.let { members.add(it) }
                    }
                is RakuRegexDecl -> members.add(firstChild)
            }
        }
        return members
    }

    private fun contributeInternals(collector: RakuSymbolCollector, symbolsAllowed: MOPSymbolsAllowed) {
        for (member in memberContributors()) {
            member.contributeMOPSymbols(collector, symbolsAllowed)
            if (collector.isSatisfied) return
        }
    }

    /** A does/is parent: resolved locally, or an external/unresolvable name. */
    private class ParentRef(val isDoes: Boolean, val decl: RakuPackageDecl?, val externalName: String?)

    /**
     * Resolves the does/is trait targets. The stub lens resolves through the
     * lexical/global type stub indexes; the AST lens through trait references.
     * Returns the parents plus whether the implicit Any parent applies (a
     * declared Mu parent suppresses it; sticky, matching the AST branch's
     * historical semantics — the stub branch used to let a later trait
     * resurrect Any).
     */
    private fun parentCandidates(): Pair<List<ParentRef>, Boolean> {
        val parents = ArrayList<ParentRef>()
        var isAny = true
        val stub = stub
        if (stub != null) {
            for (child in stub.childrenStubs) {
                if (child !is RakuTraitStub) continue
                if (child.getTraitModifier() != "does" && child.getTraitModifier() != "is") continue
                val name = child.getTraitName()
                val project = project
                val indexables = ArrayList<RakuIndexableType>()
                indexables.addAll(StubIndex.getElements(
                    RakuLexicalTypeStubIndex.getInstance().key, name, project,
                    GlobalSearchScope.projectScope(project), RakuIndexableType::class.java))
                indexables.addAll(StubIndex.getElements(
                    RakuGlobalTypeStubIndex.getInstance().key, name, project,
                    GlobalSearchScope.projectScope(project), RakuIndexableType::class.java))
                if (indexables.size == 1) {
                    parents.add(ParentRef(child.getTraitModifier() == "does", indexables.first() as RakuPackageDecl, null))
                } else {
                    parents.add(ParentRef(child.getTraitModifier() == "does", null, name))
                }
                if (name == "Mu") {
                    isAny = false
                }
            }
        } else {
            for (trait in traits) {
                if (trait.traitModifier != "does" && trait.traitModifier != "is") continue
                val element: PsiElement? = if (trait.traitModifier == "does") {
                    PsiTreeUtil.findChildOfType(trait, RakuTypeName::class.java)
                } else {
                    PsiTreeUtil.findChildOfType(trait, RakuIsTraitName::class.java)
                }
                val ref = element?.reference ?: continue
                val decl = ref.resolve()
                if (decl is RakuPackageDecl) {
                    parents.add(ParentRef(trait.traitModifier == "does", decl, null))
                } else {
                    parents.add(ParentRef(trait.traitModifier == "does", null, trait.traitName))
                }
                if (trait.traitName == "Mu") {
                    isAny = false
                }
            }
        }
        return parents to isAny
    }

    private fun contributeFromElders(collector: RakuSymbolCollector, symbolsAllowed: MOPSymbolsAllowed) {
        val (parents, isAny) = parentCandidates()
        val isGrammar = packageKind == "grammar"

        // Contribute from explicit parents, either local or external
        for (parent in parents) {
            val allowed = if (parent.isDoes) symbolsAllowed.does() else symbolsAllowed.`is`()
            val local = parent.decl
            if (local != null) {
                local.contributeMOPSymbols(collector, allowed)
                if (collector.isSatisfied) return
                local.contributeScopeSymbols(collector)
                if (collector.isSatisfied) return
            } else {
                // It can be either an external package or a non-existent one.
                // Chop off possible parametrized roles.
                val extType = parent.externalName!!.substringBefore('[')
                contributeExternalPackage(collector, extType, allowed)
                if (collector.isSatisfied) return
            }
        }

        // Contribute implicit symbols from Any/Mu and Cursor for grammars
        val coreSetting: RakuFile = project.getService(RakuProjectSdkService::class.java)
            .symbolCache
            .getCoreSettingFile() ?: return

        val allowed = MOPSymbolsAllowed(false, false, false, packageKind == "role")

        if (parents.isNotEmpty()) return

        collector.decreasePriority()
        if (isGrammar) {
            RakuSdkUtil.contributeParentSymbolsFromCore(collector, coreSetting, "Cursor", allowed)
        }
        collector.decreasePriority()
        if (isAny) {
            RakuSdkUtil.contributeParentSymbolsFromCore(collector, coreSetting, "Any", allowed)
        }
        collector.decreasePriority()
        // Always contribute Mu
        RakuSdkUtil.contributeParentSymbolsFromCore(collector, coreSetting, "Mu", allowed)
    }

    private fun contributeExternalPackage(
        collector: RakuSymbolCollector,
        typeName: String,
        symbolsAllowed: MOPSymbolsAllowed,
    ) {
        val extCollector = RakuSingleResolutionSymbolCollector(typeName, RakuSymbolKind.TypeOrConstant)
        applyExternalSymbolCollector(extCollector)
        val result = extCollector.result
        val externalPackage = result?.psi
        if (externalPackage is RakuPackageDecl) {
            externalPackage.contributeMOPSymbols(collector, symbolsAllowed)
        }
    }

    /** A nested-package candidate seen through the stub or AST lens. */
    private class NestedPackageView(val scope: String, val name: String?, val psi: RakuPackageDecl)

    override fun contributeNestedPackagesWithPrefix(collector: RakuSymbolCollector, prefix: String) {
        // Walk to find immediately nested packages, but not those within them
        // (we make a recursive contribute call on those). One BFS over either
        // the stub tree or the PSI tree; package facts are read stub-first so
        // stubbed dependencies are never force-parsed.
        val stub = stub
        val root: Any = stub ?: this
        val visit: Queue<Any> = ArrayDeque()
        visit.add(root)
        while (visit.isNotEmpty()) {
            val current = visit.remove()
            var addChildren = false
            val pkg = if (current === root) null else nestedPackageView(current)
            if (current === root) {
                addChildren = true
            } else if (pkg == null) {
                addChildren = true
            } else if (pkg.scope == "our") {
                val nestedName = pkg.name
                if (!nestedName.isNullOrEmpty()) {
                    collector.offerSymbol(RakuExplicitAliasedSymbol(
                        RakuSymbolKind.TypeOrConstant, pkg.psi, prefix + nestedName))
                    if (collector.isSatisfied) return
                    pkg.psi.contributeNestedPackagesWithPrefix(collector, "$prefix$nestedName::")
                }
            }
            if (addChildren) {
                visit.addAll(walkChildren(current))
            }
        }
    }

    private fun nestedPackageView(node: Any): NestedPackageView? = when (node) {
        is RakuPackageDeclStub -> NestedPackageView(node.getScope(), node.typeName, node.psi)
        is RakuPackageDecl -> NestedPackageView(node.scope, node.packageName, node)
        else -> null
    }

    private fun walkChildren(node: Any): List<Any> = when (node) {
        is Stub -> node.childrenStubs
        is PsiElement -> generateSequence(node.firstChild) { it.nextSibling }
            .filterIsInstance<RakuPsiElement>()
            .toList()
        else -> emptyList()
    }

    override fun collectParents(): List<RakuPackageDecl> {
        val parents = ArrayList<RakuPackageDecl>()
        for (trait in traits) {
            val isTrait = PsiTreeUtil.findChildOfType(trait, RakuIsTraitName::class.java)
            val target: PsiElement? = isTrait
                ?: PsiTreeUtil.findChildOfType(trait, RakuTypeName::class.java)
            val resolved = target?.reference?.resolve()
            if (resolved is RakuPackageDecl) {
                parents.add(resolved)
            }
        }
        return parents
    }

    override fun collectChildren(): List<RakuPackageDecl> {
        val children = ArrayList<RakuPackageDecl>()
        val index = RakuGlobalTypeStubIndex.getInstance()
        val project = project
        val name = packageName
        for (key in index.getAllKeys(project)) {
            val psi = StubIndex.getElements(
                index.key, key, project, GlobalSearchScope.allScope(project), RakuIndexableType::class.java)
            if (psi.size != 1) continue
            for (type in psi) {
                if (type !is RakuPackageDecl) continue
                if (type.findTrait("does", name) != null || type.findTrait("is", name) != null) {
                    children.add(type)
                }
            }
        }
        return children
    }

    override fun findTrait(mod: String, name: String): RakuTrait? {
        val stub = stub ?: return super<RakuTypeStubBasedPsi>.findTrait(mod, name)
        return stub.childrenStubs
            .filterIsInstance<RakuTraitStub>()
            .firstOrNull { it.getTraitModifier() == mod && it.getTraitName() == name }
            ?.psi
    }

    override fun getMetaData(): PsiMetaData {
        val decl: PsiElement = this
        val shortName = (packageName ?: "").substringAfterLast(':')
        return object : PsiMetaData {
            override fun getDeclaration(): PsiElement = decl
            override fun getName(context: PsiElement?): String = shortName
            override fun getName(): String = shortName
            override fun init(element: PsiElement?) {}
            override fun getDependencies(): Array<Any> = ArrayUtil.EMPTY_OBJECT_ARRAY
        }
    }

    override fun setName(name: String): PsiElement {
        val nameElement = RakuElementFactory.createTypeDeclarationName(project, name)
        nameIdentifier?.replace(nameElement)
        return this
    }

    override fun collectPodAndDocumentables(context: PodDomBuildingContext) {
        val kind = packageKind
        val name = packageName
        if (name.isNullOrEmpty()) {
            super<RakuTypeStubBasedPsi>.collectPodAndDocumentables(context)
            return
        }
        val shortName = name.split("::").last()
        val globalName = context.prependGlobalNameParts(name)
        val scope = scope
        val isLexical = !(scope == "our" || scope == "unit")
        val exportTrait = findTrait("is", "export")
        if (isLexical) {
            context.enterLexicalPackage()
        } else {
            context.enterGlobalNamePart(name)
        }
        val visible = !isLexical && globalName != null || exportTrait != null
        if (visible && !(kind == "package" || kind == "module")) {
            val type = PodDomClassyDeclarator(
                textOffset, shortName, globalName, docBlocks, exportTrait, kind)
            context.addType(type)
            context.enterClassyType(type)
        } else {
            context.enterClassyType(null)
        }
        super<RakuTypeStubBasedPsi>.collectPodAndDocumentables(context)
        context.exitClassyType()
        if (isLexical) {
            context.exitLexicalPackage()
        } else {
            context.exitGlobalNamePart()
        }
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is RakuElementVisitor) {
            visitor.visitRakuElement(this)
        } else {
            super<RakuTypeStubBasedPsi>.accept(visitor)
        }
    }
}
