package org.raku.comma.docs

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.psi.RakuConstant
import org.raku.comma.psi.RakuDocumented
import org.raku.comma.psi.RakuEnum
import org.raku.comma.psi.RakuMethodCall
import org.raku.comma.psi.RakuPackageDecl
import org.raku.comma.psi.RakuParameter
import org.raku.comma.psi.RakuParameterVariable
import org.raku.comma.psi.RakuRegexDecl
import org.raku.comma.psi.RakuRoutineDecl
import org.raku.comma.psi.RakuSignature
import org.raku.comma.psi.RakuSubCall
import org.raku.comma.psi.RakuSubCallName
import org.raku.comma.psi.RakuSubset
import org.raku.comma.psi.RakuVariableDecl
import org.raku.comma.psi.external.ExternalRakuFile
import org.raku.comma.psi.external.ExternalRakuPackageDecl
import org.raku.comma.psi.external.RakuExternalPsiElement
import org.raku.comma.services.project.ProjectSdkSymbolCache
import org.raku.comma.utils.RakuUtils

class RakuDocumentationProvider : DocumentationProvider {

    @Synchronized
    override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement?): String? {
        return when (element) {
            is RakuConstant ->
                if (element.text.length < 50) RakuUtils.escapeHTML(element.text)
                else RakuUtils.escapeHTML("constant ${element.constantName} = ...")

            is RakuEnum ->
                RakuUtils.escapeHTML("enum ${element.enumName} (${element.enumValues.joinToString(", ")})")

            is RakuPackageDecl -> {
                val traits = element.traits
                val traitsText = if (traits.isEmpty()) "" else " " + traits.joinToString(" ") { "${it.traitModifier} ${it.traitName}" }
                RakuUtils.escapeHTML("${element.packageKind} ${element.packageName}$traitsText")
            }

            is RakuParameterVariable -> {
                val parameter = PsiTreeUtil.getParentOfType(element, RakuParameter::class.java) ?: return null
                RakuUtils.escapeHTML(parameter.text)
            }

            is RakuRegexDecl -> {
                val text = element.text
                if (text.length < 50) text
                else RakuUtils.escapeHTML("${element.regexKind} ${element.regexName} { ... }")
            }

            is RakuRoutineDecl -> {
                val signature = if (element is RakuExternalPsiElement) "(${element.summarySignature()})"
                else {
                    val node: RakuSignature? = element.signatureNode
                    node?.text ?: "()"
                }
                RakuUtils.escapeHTML("${element.routineKind} ${element.routineName}$signature")
            }

            is RakuSubset ->
                RakuUtils.escapeHTML("subset ${element.subsetName} of ${element.subsetBaseTypeName}")

            is RakuVariableDecl -> {
                val name = element.variableNames.joinToString(", ")
                val scope = element.scope
                val initializer = element.initializer
                val selfType = if (scope == "has") PsiTreeUtil.getParentOfType(element, RakuPackageDecl::class.java) else null
                RakuUtils.escapeHTML(
                    scope + " " + name
                        + (if (initializer == null) "" else " = " + initializer.text)
                        + (if (selfType == null) "" else " (attribute of ${selfType.packageName})")
                )
            }

            else -> null
        }
    }

    override fun getUrlFor(element: PsiElement, originalElement: PsiElement?): List<String>? {
        if (element is RakuExternalPsiElement) {
            val name = element.name
            var parent = element.parent
            if (parent is ExternalRakuPackageDecl) {
                parent = parent.parent
            }
            if (parent is ExternalRakuFile && parent.name == ProjectSdkSymbolCache.SETTING_FILE_NAME) {
                if (element is RakuPackageDecl) {
                    return listOf("https://docs.raku.org/type/$name")
                } else if (element is RakuRoutineDecl) {
                    return listOf("https://docs.raku.org/routine/$name")
                }
            }
        }
        return null
    }

    @Synchronized
    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        if (element is RakuMethodCall || element is RakuSubCall) {
            val ref = if (element is RakuMethodCall) {
                element.reference
            } else {
                val callName = PsiTreeUtil.findChildOfType(element, RakuSubCallName::class.java) ?: return null
                callName.reference
            }
            if (ref !is PsiReferenceBase.Poly<*>) return null
            val resolves = ref.multiResolve(false)
            for (result in resolves) {
                val declaration = result.element
                if (declaration is RakuRoutineDecl) {
                    val docs = declaration.docsString
                    if (docs != null) return docs
                }
            }
        }

        if (element is RakuDocumented) {
            val docsString = element.docsString
            return if (docsString == null || docsString.isEmpty() || docsString == "<br>") null else docsString
        }
        return null
    }

    override fun getDocumentationElementForLookupItem(psiManager: PsiManager, `object`: Any?, element: PsiElement?): PsiElement? = null

    override fun getDocumentationElementForLink(psiManager: PsiManager, link: String, context: PsiElement?): PsiElement? = null
}
