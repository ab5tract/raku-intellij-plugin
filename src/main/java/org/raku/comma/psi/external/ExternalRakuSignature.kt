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

    override fun summary(retType: RakuType): String =
        myParameters.joinToString(", ") { it.summary(false) } + " --> " + retType.name

    override fun getParameters(): Array<RakuParameter> = myParameters
}
