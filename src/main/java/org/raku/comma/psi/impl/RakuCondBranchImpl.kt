package org.raku.comma.psi.impl

import com.intellij.lang.ASTNode
import org.raku.comma.parsing.RakuConditionalCompilation
import org.raku.comma.psi.RakuASTWrapperPsiElement
import org.raku.comma.psi.RakuCondBranch

class RakuCondBranchImpl(node: ASTNode) : RakuASTWrapperPsiElement(node), RakuCondBranch {
    override val condition: String?
        get() = RakuConditionalCompilation.conditionAt(containingFile.text, textOffset)

    override fun toString(): String = "RakuCondBranch(${condition ?: "?"})"
}
