package org.raku.comma.inspection.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import org.raku.comma.inspection.InspectionConstants.UnimportedType.DESCRIPTION_FORMAT
import org.raku.comma.inspection.RakuInspection
import org.raku.comma.inspection.fixes.AddUseStatementFix
import org.raku.comma.psi.RakuFile
import org.raku.comma.psi.RakuTypeName
import org.raku.comma.psi.stub.RakuFileStubBuilder
import org.raku.comma.psi.stub.index.RakuIndexableType
import org.raku.comma.psi.stub.index.RakuStubIndexKeys

/**
 * Reports a type name that does not resolve, but whose declaration another
 * project file provides as a global (`our`-scoped) type, and offers to `use`
 * that file's module. Names the index has never heard of stay unreported:
 * indirect lookups, `require`d symbols and the like make a blanket
 * "unresolved type" check far too noisy, so this only fires when there is a
 * concrete import to offer.
 */
class UnimportedTypeInspection : RakuInspection() {

    override fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement) {
        if (element !is RakuTypeName) return

        val project = element.project
        // Stub index queries are illegal while indexing is still running.
        if (DumbService.isDumb(project)) return
        if (element.reference?.resolve() != null) return

        val typeName = element.typeName
        val hits = StubIndex.getElements(
            RakuStubIndexKeys.GLOBAL_TYPES,
            typeName,
            project,
            GlobalSearchScope.projectScope(project),
            RakuIndexableType::class.java
        )

        val modules = hits.asSequence()
            .mapNotNull { hit -> hit.containingFile as? RakuFile }
            .filter { file -> file != element.containingFile }
            .mapNotNull { file -> RakuFileStubBuilder.generateCompilationUnitName(file) }
            .distinct()
            .sorted()
            .toList()
        if (modules.isEmpty()) return

        holder.registerProblem(element,
                               DESCRIPTION_FORMAT.format(typeName),
                               *modules.map { AddUseStatementFix(it) }.toTypedArray())
    }
}
