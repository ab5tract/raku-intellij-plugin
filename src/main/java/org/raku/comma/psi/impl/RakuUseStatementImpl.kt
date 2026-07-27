package org.raku.comma.psi.impl

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbService.Companion.isDumb
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import org.raku.comma.psi.RakuFile
import org.raku.comma.psi.RakuModuleName
import org.raku.comma.psi.RakuUseStatement
import org.raku.comma.psi.stub.RakuUseStatementStub
import org.raku.comma.psi.stub.RakuUseStatementStubElementType
import org.raku.comma.psi.stub.index.ProjectModulesStubIndex
import org.raku.comma.psi.symbols.RakuSymbolCollector
import org.raku.comma.services.project.RakuDependencyService
import org.raku.comma.services.project.RakuProjectSdkService
import org.raku.comma.utils.RakuUtils

class RakuUseStatementImpl : StubBasedPsiElementBase<RakuUseStatementStub?>, RakuUseStatement {
    constructor(node: ASTNode) : super(node)

    constructor(stub: RakuUseStatementStub, type: RakuUseStatementStubElementType) : super(stub, type)

    override fun contributeLexicalSymbols(collector: RakuSymbolCollector) {
        // TODO: Figure out what to do about multiple versions of a dependency. This feels like something that Raku
        // only theoretically supports. Let's deal with it when we encounter it as a problem.
        val moduleName = moduleName ?: return
        // We cannot contribute based on stubs when indexing is in progress
        if (isDumb(project)) return

        val shortName = RakuUtils.stripAuthVerApi(moduleName)

        // Modules living in this project resolve through the stub index; the
        // dependency service only knows about external dependencies. Lib
        // directories can sit outside the content root, hence the allScope
        // fallback. Several files can share a module name (e.g. a scaffolded
        // Foo.rakumod next to a real Foo.pm6), so contribute from all of them.
        val projectModules = StubIndex.getElements(ProjectModulesStubIndex.getInstance().key,
                                                   shortName,
                                                   project,
                                                   GlobalSearchScope.projectScope(project),
                                                   RakuFile::class.java)
            .ifEmpty {
                StubIndex.getElements(ProjectModulesStubIndex.getInstance().key,
                                      shortName,
                                      project,
                                      GlobalSearchScope.allScope(project),
                                      RakuFile::class.java)
            }
        val files: Collection<RakuFile> = projectModules.ifEmpty {
            listOfNotNull(
                project.service<RakuDependencyService>().provideToRakuFile(shortName)
                    // SDK-provided modules (NativeCall, nqp, ...) are known to
                    // neither index nor dependency service and come from the
                    // SDK symbol cache.
                    ?: project.service<RakuProjectSdkService>().symbolCache.getPsiFileForModule(shortName, text)
            )
        }

        for (file in files) {
            file.contributeGlobals(collector, HashSet())
            file.contributeGlobals(collector, mutableSetOf(shortName, moduleName))
        }
    }

    override fun getModuleName(): String? {
        val stub = stub
        if (stub != null) return stub.getModuleName()

        val moduleName = findChildByClass(RakuModuleName::class.java)
        return moduleName?.text
    }

    override fun toString(): String {
        return javaClass.simpleName + "(Raku:USE_STATEMENT)"
    }
}
