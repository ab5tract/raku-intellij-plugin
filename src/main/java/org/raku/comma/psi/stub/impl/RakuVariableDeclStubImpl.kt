package org.raku.comma.psi.stub.impl

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import org.raku.comma.parsing.RakuElementTypes
import org.raku.comma.psi.RakuVariableDecl
import org.raku.comma.psi.stub.RakuScopedDeclStub
import org.raku.comma.psi.stub.RakuVariableDeclStub

class RakuVariableDeclStubImpl(
    stub: StubElement<*>?,
    private val variableNames: Array<String>,
    private val variableType: String?,
    private val isExported: Boolean
) : StubBase<RakuVariableDecl>(stub, RakuElementTypes.VARIABLE_DECLARATION), RakuVariableDeclStub {

    override fun getVariableNames(): Array<String> {
        return variableNames
    }

    override fun getVariableType(): String? {
        return variableType
    }

    override fun getScope(): String {
        val parent = getParentStub()
        // Shouldn't ever happen.
        return if (parent is RakuScopedDeclStub) parent.getScope() else ""
    }

    override fun isExported(): Boolean {
        return isExported
    }
}
