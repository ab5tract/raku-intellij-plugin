package org.raku.comma.psi.stub

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.Stub
import org.raku.comma.psi.RakuPsiDeclaration

interface RakuTypeStub<T> : RakuDeclStub<T> where T : PsiElement, T : RakuPsiDeclaration {
    fun getTypeName(): String

    fun getGlobalName(): String? {
        val globalName = getTypeName()
        val globalNameBuilder = StringBuilder(globalName)
        var current: Stub? = getParentStub()
        while (current != null) {
            if (current is RakuScopedDeclStub) {
                if (current.getScope() == "my") return null
            }
            if (current is RakuPackageDeclStub) {
                globalNameBuilder.insert(0, current.getTypeName() + "::")
            }
            current = current.getParentStub()
        }
        return globalNameBuilder.toString()
    }

    fun getLexicalName(): String? {
        val lexicalName = getTypeName()
        val lexicalNameBuilder = StringBuilder(lexicalName)
        var current: Stub? = getParentStub()
        while (current != null) {
            if (current is RakuScopedDeclStub) {
                if (current.getScope() == "my") return lexicalNameBuilder.toString()
                return null
            }
            if (current is RakuPackageDeclStub) {
                lexicalNameBuilder.insert(0, current.getTypeName() + "::")
            }
            current = current.getParentStub()
        }
        return null
    }
}
