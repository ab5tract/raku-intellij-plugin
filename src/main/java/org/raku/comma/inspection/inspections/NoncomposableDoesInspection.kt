package org.raku.comma.inspection.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.inspection.RakuInspection
import org.raku.comma.inspection.fixes.ChangeDoesToIsFix
import org.raku.comma.psi.RakuAlso
import org.raku.comma.psi.RakuPackageDecl
import org.raku.comma.psi.RakuTrait
import org.raku.comma.psi.RakuTypeName

class NoncomposableDoesInspection : RakuInspection() {

    private val roleDescription = "Role cannot compose a class"
    private val classDescription = "Class cannot compose a class"

    override fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement) {
        if (element !is RakuTrait && element !is RakuAlso) return

        // A trait inside an `also` is reached through both the RakuAlso visit
        // and its own visit; report it once, from the RakuAlso side.
        if (element is RakuTrait && element.parent is RakuAlso) return

        val trait = if (element is RakuTrait) element else (element as RakuAlso).trait
        if (trait == null) return
        if (trait.traitModifier != "does") return

        val declaration = PsiTreeUtil.getParentOfType(trait, RakuPackageDecl::class.java) ?: return
        val typeName = PsiTreeUtil.findChildOfType(trait, RakuTypeName::class.java) ?: return

        val ref = typeName.reference ?: return
        val composedDeclaration = ref.resolve() as? RakuPackageDecl ?: return

        val description: String?
        if (declaration.packageKind == "role" && composedDeclaration.packageKind == "class") {
            description = roleDescription
        } else if (declaration.packageKind == "class" && composedDeclaration.packageKind == "class") {
            description = classDescription
        } else {
            description = null
        }

        if (description != null) {
            holder.registerProblem(trait, description, ChangeDoesToIsFix())
        }
    }

}