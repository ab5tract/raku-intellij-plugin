package org.raku.comma.psi.external

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.raku.comma.psi.RakuParameter
import org.raku.comma.psi.RakuSignature
import org.raku.comma.psi.type.RakuType
import org.raku.comma.sdk.SignatureJson

class ExternalRakuSignature(
    project: Project,
    parent: PsiElement?,
    signature: SignatureJson,
) : RakuExternalPsiElement(project, parent), RakuSignature {

    private val myParameters: Array<RakuParameter> = signature.p
        .map<_, RakuParameter> { param ->
            ExternalRakuParameter(project, parent, param.n, param.nn.ifEmpty { null }, param.t)
        }
        .toTypedArray()

    // `*%_` is the implicit named slurpy every Raku routine carries unless it
    // opts out. It is the same on nearly every signature, so it tells a reader
    // nothing about how to call this one -- and it crowds out what does, in a
    // line that has to fit a popup. Rakudo's declared type for it is not even
    // stable: `Mu` before 2026.08, `Associative` after, which is enough to
    // break an expectation that merely quotes the summary.
    //
    // Only the implicit one goes. An explicitly declared named slurpy is named
    // something else and is part of the calling convention, so it stays.
    override fun summary(retType: RakuType): String {
        val declared = myParameters.filterNot { it.text == IMPLICIT_NAMED_SLURPY }
        val params = declared.joinToString(", ") { it.summary(false) }
        return if (params.isEmpty()) "--> ${retType.name}" else "$params --> ${retType.name}"
    }

    override fun getParameters(): Array<RakuParameter> = myParameters

    companion object {
        private const val IMPLICIT_NAMED_SLURPY = "*%_"
    }
}
