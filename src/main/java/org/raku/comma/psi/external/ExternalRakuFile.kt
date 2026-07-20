package org.raku.comma.psi.external

import com.intellij.lang.FileASTNode
import com.intellij.lang.Language
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReference
import com.intellij.psi.ResolveState
import com.intellij.psi.meta.PsiMetaData
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.search.SearchScope
import com.intellij.util.IncorrectOperationException
import org.raku.comma.RakuLanguage
import org.raku.comma.filetypes.RakuModuleFileType
import org.raku.comma.psi.RakuFile
import org.raku.comma.psi.RakuRoutineDecl
import org.raku.comma.psi.symbols.RakuSymbol
import org.raku.comma.psi.symbols.RakuSymbolCollector
import org.raku.comma.psi.symbols.RakuSymbolKind
import javax.swing.Icon

class ExternalRakuFile(
    private val myProject: Project,
    private val myFile: VirtualFile,
) : RakuFile {

    // PsiFile.getViewProvider() is @NotNull, but findViewProvider is not; like
    // the Java predecessor, a missing provider surfaces only if it is accessed.
    private val myViewProvider: FileViewProvider? =
        PsiManager.getInstance(myProject).findViewProvider(myFile)
    private var mySymbols: List<RakuSymbol> = ArrayList()
    private var moduleName: String? = null
    private var originalPath: String? = null

    override fun isReal(): Boolean = false

    override fun renderPod(): String {
        // No Pod from external files
        return ""
    }

    override fun setOriginalPath(originalPath: String?) {
        this.originalPath = originalPath
    }

    override fun getOriginalPath(): String? = originalPath

    override fun getDependencyFile(): Boolean = true
    override fun setDependencyFile(dependencyFile: Boolean?) {}

    override fun getModuleName(): String? = moduleName
    override fun setModuleName(moduleName: String?) {
        this.moduleName = moduleName
    }

    fun setSymbols(symbols: List<RakuSymbol>) {
        mySymbols = symbols
    }

    override fun contributeScopeSymbols(collector: RakuSymbolCollector) {
        contributeGlobals(collector, HashSet())
    }

    override fun contributeGlobals(collector: RakuSymbolCollector, seen: MutableSet<String>) {
        for (symbol in mySymbols) {
            if (symbol.kind == RakuSymbolKind.Routine) {
                val psi = symbol.psi as RakuRoutineDecl
                if (psi.multiness == "only") {
                    collector.offerSymbol(symbol)
                } else {
                    collector.offerMultiSymbol(symbol, false)
                }
            } else {
                collector.offerSymbol(symbol)
            }
            if (collector.isSatisfied) {
                return
            }
        }
    }

    override fun getLanguage(): Language = RakuLanguage.INSTANCE

    // ---- inert PsiFile surface: no tree, no text, no mutation ----

    override fun getStatementLineMap(): Map<Int, List<Int>> = HashMap()
    override fun getVirtualFile(): VirtualFile = myFile
    override fun processChildren(processor: PsiElementProcessor<in PsiFileSystemItem>): Boolean = false
    override fun getContainingDirectory(): PsiDirectory? = null
    override fun getProject(): Project = myProject
    override fun getManager(): PsiManager = PsiManager.getInstance(myProject)
    override fun getChildren(): Array<PsiElement> = PsiElement.EMPTY_ARRAY
    override fun getParent(): PsiDirectory? = null
    override fun getFirstChild(): PsiElement? = null
    override fun getLastChild(): PsiElement? = null
    override fun getNextSibling(): PsiElement? = null
    override fun getPrevSibling(): PsiElement? = null
    override fun getContainingFile(): PsiFile = this
    override fun getTextRange(): TextRange? = null
    override fun getStartOffsetInParent(): Int = 0
    override fun getTextLength(): Int = 0
    override fun findElementAt(offset: Int): PsiElement? = null
    override fun findReferenceAt(offset: Int): PsiReference? = null
    override fun getTextOffset(): Int = 0
    override fun getText(): String? = null
    override fun textToCharArray(): CharArray = CharArray(0)
    override fun getNavigationElement(): PsiElement? = null
    override fun getOriginalElement(): PsiElement? = null
    override fun textMatches(text: CharSequence): Boolean = false
    override fun textMatches(element: PsiElement): Boolean = false
    override fun textContains(c: Char): Boolean = false
    override fun accept(visitor: PsiElementVisitor) {}
    override fun acceptChildren(visitor: PsiElementVisitor) {}
    override fun copy(): PsiElement = this
    override fun add(element: PsiElement): PsiElement = throw UnsupportedOperationException()
    override fun addBefore(element: PsiElement, anchor: PsiElement?): PsiElement = throw UnsupportedOperationException()
    override fun addAfter(element: PsiElement, anchor: PsiElement?): PsiElement = throw UnsupportedOperationException()
    override fun checkAdd(element: PsiElement): Unit = throw UnsupportedOperationException()
    override fun addRange(first: PsiElement?, last: PsiElement?): PsiElement = throw UnsupportedOperationException()
    override fun addRangeBefore(first: PsiElement, last: PsiElement, anchor: PsiElement?): PsiElement =
        throw UnsupportedOperationException()
    override fun addRangeAfter(first: PsiElement?, last: PsiElement?, anchor: PsiElement?): PsiElement =
        throw UnsupportedOperationException()
    override fun delete(): Unit = throw UnsupportedOperationException()
    override fun checkDelete(): Unit = throw UnsupportedOperationException()
    override fun deleteChildRange(first: PsiElement?, last: PsiElement?): Unit = throw UnsupportedOperationException()
    override fun replace(newElement: PsiElement): PsiElement = throw UnsupportedOperationException()
    override fun isValid(): Boolean = true
    override fun isWritable(): Boolean = false
    override fun getReference(): PsiReference? = null
    override fun getReferences(): Array<PsiReference> = PsiReference.EMPTY_ARRAY
    override fun <T> getCopyableUserData(key: Key<T>): T? = null
    override fun <T> putCopyableUserData(key: Key<T>, value: T?) {}
    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement,
    ): Boolean = false
    override fun getContext(): PsiElement? = null
    override fun isPhysical(): Boolean = false
    override fun getResolveScope(): GlobalSearchScope = GlobalSearchScope.projectScope(myProject)
    override fun getUseScope(): SearchScope = GlobalSearchScope.projectScope(myProject)
    override fun isDirectory(): Boolean = false
    override fun getModificationStamp(): Long = 0
    override fun getOriginalFile(): PsiFile = this
    override fun getFileType(): FileType = RakuModuleFileType.INSTANCE
    override fun getPsiRoots(): Array<PsiFile> = PsiFile.EMPTY_ARRAY
    override fun getViewProvider(): FileViewProvider = myViewProvider!!
    override fun getNode(): FileASTNode? = null
    override fun toString(): String = myFile.name
    override fun isEquivalentTo(another: PsiElement?): Boolean = false
    override fun subtreeChanged() {}
    override fun checkSetName(name: String?): Unit = throw IncorrectOperationException()
    override fun getNameIdentifier(): PsiElement? = null
    override fun getName(): String = myFile.name
    override fun getPresentation(): ItemPresentation? = null
    override fun navigate(requestFocus: Boolean) {}
    override fun getMetaData(): PsiMetaData? = null
    override fun setName(name: String): PsiElement = throw UnsupportedOperationException()
    override fun getIcon(flags: Int): Icon? = null
    override fun <T> getUserData(key: Key<T>): T? = null
    override fun <T> putUserData(key: Key<T>, value: T?) {}
}
