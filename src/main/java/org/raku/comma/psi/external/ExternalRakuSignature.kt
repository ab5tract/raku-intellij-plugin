package org.raku.comma.psi.external

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.json.JSONObject
import org.raku.comma.psi.RakuParameter
import org.raku.comma.psi.RakuSignature
import org.raku.comma.psi.type.RakuType

class ExternalRakuSignature(
    project: Project,
    parent: PsiElement?,
    signature: JSONObject,
) : RakuExternalPsiElement(project, parent), RakuSignature {

    private val myParameters: Array<RakuParameter>

    init {
        val params = ArrayList<RakuParameter>()
        for (param in signature.getJSONArray("p")) {
            if (param is JSONObject) {
                params.add(ExternalRakuParameter(
                    project, parent,
                    param.getString("n"),
                    if (param.has("nn")) param.getJSONArray("nn").toList() else null,
                    param.getString("t")))
            }
        }
        myParameters = params.toTypedArray()
    }

    override fun summary(retType: RakuType): String =
        myParameters.joinToString(", ") { it.summary(false) } + " --> " + retType.name

    override fun getParameters(): Array<RakuParameter> = myParameters
}
