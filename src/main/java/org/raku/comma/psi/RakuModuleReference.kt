package org.raku.comma.psi

import com.intellij.openapi.components.service
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.IncorrectOperationException
import org.raku.comma.psi.stub.index.ProjectModulesStubIndex
import org.raku.comma.psi.stub.index.RakuStubIndexKeys
import org.raku.comma.services.project.RakuDependencyService
import org.raku.comma.services.RakuServiceConstants
import java.util.function.Consumer

class RakuModuleReference(moduleName: RakuModuleName) :
    PsiReferenceBase<RakuModuleName?>(moduleName, TextRange(0, moduleName.textLength)) {
    private val project = moduleName.project
    private val psiManager = PsiManager.getInstance(project)
    private var cachedFile: CachedValue<PsiFile>? = null

    override fun resolve(): PsiElement? {
        if (cachedFile?.value == null) {
            val tryAgain = doResolve() as? PsiFile ?: return null
            cachedFile = createCachedValue(tryAgain)
        }
        return cachedFile?.value
    }

    private fun doResolve(): PsiElement? {
        val keys = StubIndex.getElements(
            RakuStubIndexKeys.PROJECT_MODULES,
            value,
            project,
            GlobalSearchScope.projectScope(project),
            RakuFile::class.java
        )
        val rakuFile = if (keys.isNotEmpty()) keys.first() else return resolveExternal()

        return psiManager.findFile(rakuFile.virtualFile)
    }

    private fun resolveExternal(): PsiFile? {
        return project.service<RakuDependencyService>().provideToPsiFile(value)
    }

    private fun createCachedValue(file: PsiFile): CachedValue<PsiFile> {
        return CachedValuesManager.getManager(project).createCachedValue {
            CachedValueProvider.Result(file, PsiModificationTracker.MODIFICATION_COUNT)
        }
    }

    override fun getVariants(): Array<Any> {
        val project = this.element.project
        val projectModules = ProjectModulesStubIndex.getInstance().getAllKeys(project)
        val reallyInThisProject: MutableList<String> = ArrayList()
        projectModules.forEach(Consumer { module: String ->
            val index = ProjectModulesStubIndex.getInstance()
            val matching = StubIndex.getElements(
                index.key,
                module,
                project,
                GlobalSearchScope.projectScope(project),
                RakuFile::class.java
            )
            if (!matching.isEmpty()) {
                reallyInThisProject.add(module)
            }
        })

        // Ecosystem-wide provides only once the dependency service is up
        // (mirrors the old RakuModuleListFetcher.isReady() gate).
        val dependencyService = project.service<RakuDependencyService>()
        if (dependencyService.isInitialized) {
            reallyInThisProject.addAll(dependencyService.allProvides())
        }
        reallyInThisProject.addAll(RakuServiceConstants.PREINSTALLED_MODULES)
        reallyInThisProject.addAll(RakuServiceConstants.PRAGMAS)

        return reallyInThisProject.toTypedArray()
    }

    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String): PsiElement {
        val name = element
        return name.setName(newElementName)
    }

    @Throws(IncorrectOperationException::class)
    override fun bindToElement(element: PsiElement): PsiElement {
        // Our RakuFile, so can calculate new path
        if (element is RakuPsiElement) {
            getElement().setName(element.getEnclosingRakuModuleName()!!)
        }
        return element
    }
}
