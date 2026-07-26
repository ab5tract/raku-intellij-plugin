package org.raku.comma.psi.stub.impl

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import org.raku.comma.parsing.RakuElementTypes
import org.raku.comma.psi.RakuSubset
import org.raku.comma.psi.stub.RakuScopedDeclStub
import org.raku.comma.psi.stub.RakuSubsetStub

class RakuSubsetStubImpl(
    stub: StubElement<*>?,
    private val subsetName: String,
    private val isExported: Boolean,
    private val baseTypeName: String
) : StubBase<RakuSubset>(stub, RakuElementTypes.SUBSET), RakuSubsetStub {

    override fun getTypeName(): String {
        return subsetName
    }

    override fun getScope(): String {
        val parent = getParentStub()
        return if (parent is RakuScopedDeclStub) parent.getScope() else "our"
    }

    override fun isExported(): Boolean {
        return isExported
    }

    override fun getSubsetBaseTypeName(): String {
        return baseTypeName
    }
}
