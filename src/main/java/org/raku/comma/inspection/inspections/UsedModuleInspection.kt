package org.raku.comma.inspection.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiEditorUtil
import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.highlighter.RakuHighlighter
import org.raku.comma.inspection.InspectionConstants.UsedModuleInspection.*
import org.raku.comma.inspection.RakuInspection
import org.raku.comma.inspection.fixes.CreateLocalModuleFix
import org.raku.comma.inspection.fixes.InstallMissingDependencyFix
import org.raku.comma.inspection.fixes.MissingModuleFix
import org.raku.comma.psi.RakuColonPair
import org.raku.comma.psi.RakuLongName
import org.raku.comma.psi.RakuModuleName
import org.raku.comma.services.project.RakuDependencyService
import org.raku.comma.services.RakuServiceConstants
import org.raku.comma.services.project.RakuMetaDataComponent
import org.raku.comma.utils.CommaProjectUtil

class UsedModuleInspection : RakuInspection() {
    override fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement) {
        val project = holder.project

        if (element !is RakuModuleName) return

        val moduleNameNode = PsiTreeUtil.findChildOfType(element, RakuLongName::class.java) ?: return
        val moduleName = moduleNameNode.firstChild.text

        // We don't need to annotate late-bound modules
        if (moduleName.startsWith("::")) return
        // ... or preinstalled ones ...
        if (RakuServiceConstants.PREINSTALLED_MODULES.contains(moduleName)) return
        // ... or pragmas ...
        if (RakuServiceConstants.PRAGMAS.contains(moduleName)) return
        // ... or Rakudo core's internal modules (Raku::Actions, NQPP6QRegex,
        // ...), which live in the compiler build, never in the ecosystem or
        // a META6.json.
        if (CommaProjectUtil.isRakudoCoreFile(holder.file)) return

        // There is no point in doing this highlighting prior to the loading of dependency information
        // ... but let's default to marking them as warnings when not in the provides list
        val metadata = project.service<RakuMetaDataComponent>()
        val moduleDetails = project.service<RakuDependencyService>()
        if (moduleDetails.isNotInitialized) {
            if (! CommaProjectUtil.projectProvides(project).contains(moduleName)) {
                holder.registerProblem(element, DESCRIPTION_META6_FORMAT.format(moduleName), ProblemHighlightType.WARNING)
            }
            return
        }

        // The situation may have changed, so remove all custom highlighters
        removeHighlighters(moduleNameNode)

        val params = PsiTreeUtil.findChildrenOfType(moduleNameNode, RakuColonPair::class.java)
        for (colonPair in params) {
            val key = colonPair.key
            if (key == "from") return
        }

        if (checkDependency(moduleName, metadata, moduleDetails, element, holder.file.virtualFile)) return

        val holderPackage = moduleDetails.moduleByProvide(moduleName)
        if (holderPackage != null) {
            // script-only project, so load installed modules in dependency service
            if (metadata.hasNoMeta) {
                    val editor = PsiEditorUtil.findEditor(element) ?: return
                    customHighlight(editor, highlightTextRange(moduleNameNode), RakuHighlighter.ALT_WARNING, HighlighterLayer.ERROR)
                    holder.registerProblem(element,
                                           DESCRIPTION_MODULE_NOT_INSTALLED.format(moduleName),
                                           ProblemHighlightType.WARNING,
                                           *arrayOf(InstallMissingDependencyFix(moduleName)))
            } else {
                if (moduleDetails.dependencyInMeta(moduleName)) {
                    val editor = PsiEditorUtil.findEditor(element) ?: return
                    customHighlight(editor, highlightTextRange(moduleNameNode), RakuHighlighter.ALT_WARNING, HighlighterLayer.ERROR)
                    holder.registerProblem(element,
                                           DESCRIPTION_IN_META6_BUT_MISSING_FORMAT.format(moduleName),
                                           ProblemHighlightType.WARNING,
                                           *arrayOf(InstallMissingDependencyFix(moduleName)))
                } else {
                    holder.registerProblem(element, DESCRIPTION_META6_FORMAT.format(moduleName), MissingModuleFix(moduleName))
                }
            }
        } else {
            holder.registerProblem(element, DESCRIPTION_MISSING_FROM_ECO_FORMAT.format(moduleName), CreateLocalModuleFix(moduleName))
        }
    }

    private fun checkDependency(
        moduleName: String,
        metadata: RakuMetaDataComponent,
        moduleDetails: RakuDependencyService,
        element: PsiElement,
        file: VirtualFile
    ): Boolean {
        return  metadata.providedNames.contains(moduleName)
                || (moduleDetails.moduleDetails.provideToRakuFile[moduleName] != null
                    && (metadata.hasNoMeta || (metadata.hasMeta && moduleDetails.dependencyInMeta(moduleName))))
                || (file.url.startsWith("mock://") && element.reference?.resolve() != null)
    }
}