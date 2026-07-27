package org.raku.comma.psi.impl

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.util.IncorrectOperationException
import org.raku.comma.extensions.RakuFrameworkCall
import org.raku.comma.parsing.RakuTokenTypes
import org.raku.comma.psi.RakuElementFactory
import org.raku.comma.psi.RakuRoutineDecl
import org.raku.comma.psi.RakuSubCall
import org.raku.comma.psi.RakuSubCallName
import org.raku.comma.psi.stub.RakuSubCallStub
import org.raku.comma.psi.stub.RakuSubCallStubElementType
import org.raku.comma.psi.type.RakuType
import org.raku.comma.psi.type.RakuUntyped

class RakuSubCallImpl : StubBasedPsiElementBase<RakuSubCallStub?>, RakuSubCall {
    constructor(node: ASTNode) : super(node)

    constructor(stub: RakuSubCallStub, type: RakuSubCallStubElementType) : super(stub, type)

    @Throws(IncorrectOperationException::class)
    override fun setName(name: String): PsiElement {
        val call = RakuElementFactory.createSubCallName(getProject(), name)
        val callName = this.subCallNameNode
        if (callName != null) {
            val keyNode = callName.getNode()
            val newKeyNode = call.getNode()
            getNode().replaceChild(keyNode, newKeyNode)
        }
        return this
    }

    val subCallNameNode: RakuSubCallName?
        get() = findChildByClass<RakuSubCallName?>(RakuSubCallName::class.java)

    override fun getCallName(): String {
        val name = this.subCallNameNode
        return if (name == null)
            ""
        else
            name.getCallName()
    }

    override fun getWholeCallNode(): PsiElement {
        return this
    }

    override fun getCallArguments(): Array<PsiElement?>? {
        return super<RakuSubCall>.getCallArguments()
    }

    override fun inferType(): RakuType {
        val name = firstChild
        if (name !is RakuSubCallName) {
            return RakuUntyped.INSTANCE
        }
        val ref = name.reference
        if (ref == null) {
            return RakuUntyped.INSTANCE
        }
        val resolved = ref.resolve()
        if (resolved is RakuRoutineDecl) {
            return resolved.getReturnType()
        }
        return RakuUntyped.INSTANCE
    }

    override fun getName(): String? {
        val presentation = getPresentation()
        return if (presentation == null)
            getCallName()
        else
            presentation.presentableText
    }

    override fun getPresentation(): ItemPresentation? {
        val extensions = RakuFrameworkCall.EP_NAME.extensions
        val stub = getStub()
        if (stub != null) {
            val allFrameworkData = stub.getAllFrameworkData()
            for (ext in extensions) {
                val prefix = ext.frameworkName
                val frameworkData: MutableMap<String, String> = mutableMapOf()
                for (entry in allFrameworkData.entries) if (entry.key!!.startsWith("$prefix.")) {
                    frameworkData.put(entry.key!!.substring(prefix.length + 1), entry.value!!)
                }
                if (!frameworkData.isEmpty()) {
                    return ext.getNavigatePresentation(this, frameworkData)
                }
            }
        } else {
            for (ext in extensions) {
                if (ext.isApplicable(this)) {
                    return ext.getNavigatePresentation(this, ext.getFrameworkData(this))
                }
            }
        }
        return null
    }

    override fun maybeCoercion(): Boolean {
        return node.getChildren(OPEN_PAREN_TOKEN_SET).size == 1
    }

    override fun toString(): String {
        return javaClass.getSimpleName() + "(Raku:SUB_CALL)"
    }

    companion object {
        val OPEN_PAREN_TOKEN_SET: TokenSet = TokenSet.create(RakuTokenTypes.PARENTHESES_OPEN)
    }
}
