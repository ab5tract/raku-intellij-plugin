package org.raku.comma.psi.stub

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import org.raku.comma.psi.RakuPsiDeclaration

interface RakuDeclStub<T> : StubElement<T> where T : PsiElement, T : RakuPsiDeclaration {
    fun getScope(): String
    fun isExported(): Boolean
}
