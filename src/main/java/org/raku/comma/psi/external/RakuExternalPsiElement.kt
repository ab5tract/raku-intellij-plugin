package org.raku.comma.psi.external

import com.intellij.lang.FileASTNode
import com.intellij.lang.Language
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReference
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.IncorrectOperationException
import org.raku.comma.RakuLanguage
import org.raku.comma.psi.RakuDocumented
import javax.swing.Icon

abstract class RakuExternalPsiElement(
    protected val myProject: Project,
    protected val myParent: PsiElement?,
) : PsiNamedElement, NavigatablePsiElement, RakuDocumented {

    private var myDocs: String? = null

    // Must stay an override: RakuDocumented's default walks real PSI trees,
    // which synthetic elements do not have.
    override fun getDocsString(): String? = myDocs

    fun setDocs(docs: String) {
        myDocs = docs.replace("\n", "<br>")
    }

    override fun getProject(): Project = myProject
    override fun getLanguage(): Language = RakuLanguage.INSTANCE
    override fun getManager(): PsiManager = PsiManager.getInstance(myProject)
    override fun getParent(): PsiElement? = myParent
    override fun getResolveScope(): GlobalSearchScope = GlobalSearchScope.projectScope(myProject)
    override fun getUseScope(): SearchScope = GlobalSearchScope.projectScope(myProject)
    override fun isValid(): Boolean = true
    override fun setName(name: String): PsiElement? = null
    override fun getName(): String = ""

    // ---- inert PSI surface: no tree, no text, no mutation ----

    override fun getChildren(): Array<PsiElement> = PsiElement.EMPTY_ARRAY
    override fun getFirstChild(): PsiElement? = null
    override fun getLastChild(): PsiElement? = null
    override fun getNextSibling(): PsiElement? = null
    override fun getPrevSibling(): PsiElement? = null
    override fun getContainingFile(): PsiFile? = null
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
    override fun getPresentation(): ItemPresentation? = null
    override fun navigate(requestFocus: Boolean) {}
    override fun canNavigate(): Boolean = false
    override fun canNavigateToSource(): Boolean = false
    override fun textMatches(text: CharSequence): Boolean = false
    override fun textMatches(element: PsiElement): Boolean = false
    override fun textContains(c: Char): Boolean = false
    override fun accept(visitor: PsiElementVisitor) {}
    override fun acceptChildren(visitor: PsiElementVisitor) {}
    override fun copy(): PsiElement? = null
    override fun add(element: PsiElement): PsiElement = throw IncorrectOperationException()
    override fun addBefore(element: PsiElement, anchor: PsiElement?): PsiElement = throw IncorrectOperationException()
    override fun addAfter(element: PsiElement, anchor: PsiElement?): PsiElement = throw IncorrectOperationException()
    override fun checkAdd(element: PsiElement): Unit = throw IncorrectOperationException()
    override fun addRange(first: PsiElement?, last: PsiElement?): PsiElement = throw IncorrectOperationException()
    override fun addRangeBefore(first: PsiElement, last: PsiElement, anchor: PsiElement?): PsiElement =
        throw IncorrectOperationException()
    override fun addRangeAfter(first: PsiElement?, last: PsiElement?, anchor: PsiElement?): PsiElement =
        throw IncorrectOperationException()
    override fun delete(): Unit = throw IncorrectOperationException()
    override fun checkDelete(): Unit = throw IncorrectOperationException()
    override fun deleteChildRange(first: PsiElement?, last: PsiElement?): Unit = throw IncorrectOperationException()
    override fun replace(newElement: PsiElement): PsiElement = throw IncorrectOperationException()
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
    override fun getNode(): FileASTNode? = null
    override fun isEquivalentTo(another: PsiElement?): Boolean = false
    override fun <T> getUserData(key: Key<T>): T? = null
    override fun <T> putUserData(key: Key<T>, value: T?) {}
    override fun getIcon(flags: Int): Icon? = null
}
