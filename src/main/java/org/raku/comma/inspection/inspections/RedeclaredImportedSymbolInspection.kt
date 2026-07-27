package org.raku.comma.inspection.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.inspection.InspectionConstants.RedeclaredImportedSymbol.DESCRIPTION_FORMAT
import org.raku.comma.inspection.RakuInspection
import org.raku.comma.psi.RakuPackageDecl
import org.raku.comma.psi.RakuPsiDeclaration
import org.raku.comma.psi.RakuRoutineDecl
import org.raku.comma.psi.RakuUseStatement
import org.raku.comma.psi.symbols.RakuSymbolKind
import org.raku.comma.psi.symbols.RakuVariantsSymbolCollector

/**
 * Flags a declaration that re-declares a symbol the file already imported with
 * `use`. Rakudo rejects this outright -- `use Base; sub foo { }` against a Base
 * that exports `foo` dies with "Redeclaration of routine 'foo'. Did you mean to
 * declare a multi-sub?" -- so this is an error, not a style note.
 *
 * This replaces `RakuHighlightVisitor.visitUseStatement`, which fed imported
 * declarations into the same duplicate pool as in-file ones. That code was
 * commented out in 3f854f3c ("Migrate all annotations to inspections") because
 * it ran on the EDT for every highlighting pass and did far too much work. As
 * an inspection the walk happens off the EDT, the imported-name map is cached
 * per file, and users can switch the check off.
 */
class RedeclaredImportedSymbolInspection : RakuInspection() {

    override fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement) {
        val name: String
        val range: TextRange
        when (element) {
            is RakuRoutineDecl -> {
                // A multi is the *supported* way to extend an imported routine,
                // which is exactly what Rakudo's error message suggests.
                if (element.multiness != "only") return
                name = element.routineName ?: return
                val declarator = element.declaratorNode ?: return
                val identifier = element.nameIdentifier ?: return
                range = TextRange(declarator.textOffset, identifier.textRange.endOffset)
            }
            is RakuPackageDecl -> {
                name = element.globalName ?: return
                val identifier = element.nameIdentifier ?: return
                range = identifier.textRange
            }
            else -> return
        }
        if (name.isEmpty() || name == "<anon>") return

        val file = element.containingFile ?: return
        val origin = importedSymbols(file)[name] ?: return
        // In-file duplicates are RakuHighlightVisitor's job; only report the
        // cross-file case here, or the same declaration gets marked twice.
        val originFile = origin.containingFile ?: return
        if (originFile.isEquivalentTo(file)) return

        val line = StringUtil.offsetToLineNumber(originFile.text, origin.textOffset) + 1
        holder.registerProblem(
            holder.manager.createProblemDescriptor(
                element,
                range.shiftLeft(element.textRange.startOffset),
                DESCRIPTION_FORMAT.format(name, originFile.name, line),
                ProblemHighlightType.GENERIC_ERROR,
                holder.isOnTheFly
            )
        )
    }

    /**
     * Names this file pulls in via `use`, mapped to the PSI that declares them.
     *
     * Walking every `use` statement's globals is the expensive part, so it is
     * done once per file rather than once per declaration. The result depends on
     * the *imported* files as much as this one, hence the global modification
     * tracker as the cache dependency.
     */
    private fun importedSymbols(file: PsiFile): Map<String, PsiElement> =
        CachedValuesManager.getCachedValue(file) {
            CachedValueProvider.Result.create(
                collectImportedSymbols(file),
                PsiModificationTracker.MODIFICATION_COUNT
            )
        }

    private fun collectImportedSymbols(file: PsiFile): Map<String, PsiElement> {
        val useStatements = PsiTreeUtil.findChildrenOfType(file, RakuUseStatement::class.java)
        if (useStatements.isEmpty()) return emptyMap()
        // Imported symbols resolve through the stub index, which is unavailable
        // mid-indexing; report nothing rather than everything.
        if (DumbService.isDumb(file.project)) return emptyMap()

        val collector = RakuVariantsSymbolCollector(RakuSymbolKind.Routine, RakuSymbolKind.TypeOrConstant)
        for (useStatement in useStatements) {
            useStatement.contributeLexicalSymbols(collector)
        }

        val result = HashMap<String, PsiElement>()
        for (symbol in collector.variants) {
            val symbolName = symbol.name ?: continue
            val psi = symbol.psi ?: continue
            result.putIfAbsent(symbolName, psi)
        }
        return result
    }
}
