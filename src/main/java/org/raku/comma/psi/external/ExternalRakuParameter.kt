package org.raku.comma.psi.external

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.raku.comma.psi.RakuParameter
import org.raku.comma.psi.RakuPsiElement
import org.raku.comma.psi.RakuVariable
import org.raku.comma.psi.RakuWhereConstraint
import org.raku.comma.psi.symbols.RakuSymbolCollector
import org.raku.comma.psi.type.RakuType
import org.raku.comma.psi.type.RakuUnresolvedType
import java.util.regex.Pattern

class ExternalRakuParameter(
    project: Project,
    parent: PsiElement?,
    private val myName: String,
    names: List<String>?,
    private val myType: String,
) : RakuExternalPsiElement(project, parent), RakuParameter {

    private val myNames: List<String> = names ?: emptyList()

    override fun summary(includeName: Boolean): String = "$myType $myName"

    override fun getVariableName(): String {
        val matcher = NAME_PATTERN.matcher(myName)
        return if (matcher.find()) matcher.group(0) else myName
    }

    override fun getVariables(): Array<RakuVariable> = emptyArray()

    override fun getVariableNames(): Array<String> =
        if (myNames.isEmpty()) arrayOf(variableName)
        else myNames.map { "\$$it" }.toTypedArray()

    override fun getText(): String = myName

    override fun getInitializer(): PsiElement? = null

    override fun isPositional(): Boolean = !myName.startsWith("*%") && !myName.startsWith(":")

    override fun isNamed(): Boolean = myName.startsWith("*%") || myName.startsWith(":")

    override fun getWhereConstraint(): RakuWhereConstraint? = null

    override fun getValueConstraint(): RakuPsiElement? = null

    override fun isSlurpy(): Boolean = myName.startsWith("*") || myName.startsWith("+")

    override fun isRequired(): Boolean = myName.endsWith("!")

    override fun isExplicitlyOptional(): Boolean = myName.endsWith("?")

    override fun isOptional(): Boolean = isExplicitlyOptional || !isPositional

    override fun isCopy(): Boolean = false

    override fun isRW(): Boolean = false

    override fun isRaw(): Boolean = false

    override fun getScope(): String = "my"

    override fun getNameIdentifier(): PsiElement? = null

    override fun getName(): String = myName

    override fun inferType(): RakuType = RakuUnresolvedType(myType)

    override fun contributeLexicalSymbols(collector: RakuSymbolCollector) {}

    override fun equalsParameter(other: RakuParameter): Boolean = false

    companion object {
        private val NAME_PATTERN = Pattern.compile("([|\$@%&]\\w+)")
    }
}
