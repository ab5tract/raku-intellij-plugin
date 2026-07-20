package org.raku.comma.psi

import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.filetypes.RakuModuleFileType
import org.raku.comma.parsing.RakuTokenTypes.UNV_WHITE_SPACE
import org.raku.comma.pod.PodDomBuildingContext
import org.raku.comma.psi.effects.Effect
import org.raku.comma.psi.effects.EffectCollection
import org.raku.comma.psi.symbols.RakuLexicalSymbolContributor
import org.raku.comma.psi.symbols.RakuSingleResolutionSymbolCollector
import org.raku.comma.psi.symbols.RakuSymbol
import org.raku.comma.psi.symbols.RakuSymbolCollector
import org.raku.comma.psi.symbols.RakuSymbolKind
import org.raku.comma.psi.symbols.RakuVariantsSymbolCollector
import org.raku.comma.psi.type.RakuType
import org.raku.comma.psi.type.RakuUntyped

interface RakuPsiElement : NavigatablePsiElement {
    /* Name-manages the enclosing file name into a module name, if possible.
     * Returns null if that's not possible or this doesn't seem to be a module. */
    fun getEnclosingRakuModuleName(): String? {
        // Make sure it's Raku module file, and trim the extension.
        val file = containingFile.virtualFile ?: return null
        if (FileTypeManager.getInstance().getFileTypeByFile(file) !is RakuModuleFileType) return null
        var path = file.path
        val extension = file.extension
        path = path.substring(0, path.length - (if (extension == null) 0 else extension.length + 1))

        // Make sure it's within the project and trim the project path
        // off the start.
        val projectPath = project.basePath ?: return null
        if (!path.startsWith(projectPath)) return null
        path = path.substring(projectPath.length + 1)

        // Mangle it, removing a leading lib:: since lib/ is the standard place
        // for libraries.
        val libraryName = path.replace(Regex("[/\\\\]"), "::")
        return StringUtil.trimStart(libraryName, "lib::")
    }

    fun resolveLexicalSymbol(kind: RakuSymbolKind, name: String): RakuSymbol? {
        val collector = RakuSingleResolutionSymbolCollector(name, kind)
        applyLexicalSymbolCollector(collector)
        return collector.result
    }

    fun resolveLexicalSymbolAllowingMulti(kind: RakuSymbolKind, name: String): List<RakuSymbol> {
        val collector = RakuSingleResolutionSymbolCollector(name, kind)
        applyLexicalSymbolCollector(collector)
        return collector.results
    }

    fun getLexicalSymbolVariants(vararg kinds: RakuSymbolKind): Collection<RakuSymbol> {
        val collector = RakuVariantsSymbolCollector(*kinds)
        applyLexicalSymbolCollector(collector)
        return collector.variants
    }

    fun applyExternalSymbolCollector(collector: RakuSymbolCollector) {
        var scope = PsiTreeUtil.getParentOfType(this, RakuPsiScope::class.java)
        while (scope != null) {
            // If we are at top level already, we need to contribute CORE external symbols too
            if (scope is RakuFile) {
                scope.contributeScopeSymbols(collector)
            }

            val list = PsiTreeUtil.findChildOfType(scope, RakuStatementList::class.java) ?: return
            val stats = PsiTreeUtil.getChildrenOfType(list, RakuStatement::class.java) ?: arrayOf()
            // Just go one level up, skipping the for loop below
            for (statement in stats) {
                // Do not iterate further If we already passed current element
                if (statement.textOffset > textOffset) break
                for (maybeImport in statement.children) {
                    if (maybeImport !is RakuUseStatement && maybeImport !is RakuNeedStatement) continue
                    val cont = maybeImport as RakuLexicalSymbolContributor
                    cont.contributeLexicalSymbols(collector)
                    if (collector.isSatisfied) return
                }
            }
            scope = PsiTreeUtil.getParentOfType(scope, RakuPsiScope::class.java)
        }
    }

    fun applyLexicalSymbolCollector(collector: RakuSymbolCollector) {
        var scope = PsiTreeUtil.getParentOfType(this, RakuPsiScope::class.java)
        while (scope != null) {
            for (cont in scope.getSymbolContributors()) {
                cont.contributeLexicalSymbols(collector)
                if (collector.isSatisfied) return
            }
            scope.contributeScopeSymbols(collector)
            if (collector.isSatisfied) return
            scope = PsiTreeUtil.getParentOfType(scope, RakuPsiScope::class.java)
        }
    }

    fun inferType(): RakuType = RakuUntyped.INSTANCE

    fun getSelfType(): RakuPackageDecl? {
        // There's only a self type if we're inside of a method or in the declaration of
        // an attribute.
        var current: RakuPsiElement? = this
        var foundSelfProvider = false
        while (current != null) {
            current = PsiTreeUtil.getParentOfType(
                current, RakuRoutineDecl::class.java, RakuRegexDecl::class.java,
                RakuPackageDecl::class.java, RakuVariableDecl::class.java)
            if (current is RakuPackageDecl) {
                return if (foundSelfProvider) current else null
            }
            if (foundSelfProvider) return null
            when (current) {
                is RakuRoutineDecl -> if (current.scope == "has") foundSelfProvider = true
                is RakuRegexDecl -> if (current.scope == "has") foundSelfProvider = true
                is RakuVariableDecl -> if (current.scope == "has") foundSelfProvider = true
                else -> {}
            }
        }
        return null
    }

    fun skipWhitespacesBackward(): PsiElement? {
        var temp = prevSibling
        while (temp != null && (temp is PsiWhiteSpace || temp.node.elementType == UNV_WHITE_SPACE)) {
            temp = temp.prevSibling
        }
        return temp
    }

    fun skipWhitespacesForward(): PsiElement? {
        var temp = nextSibling
        while (temp != null && (temp is PsiWhiteSpace || temp.node.elementType == UNV_WHITE_SPACE)) {
            temp = temp.nextSibling
        }
        return temp
    }

    fun collectPodAndDocumentables(context: PodDomBuildingContext) {
        var child = firstChild
        while (child != null) {
            if (child is RakuPsiElement) {
                child.collectPodAndDocumentables(context)
            }
            child = child.nextSibling
        }
    }

    fun inferEffects(): EffectCollection = EffectCollection.of(Effect.IMPURE)
}
