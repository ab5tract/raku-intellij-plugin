package org.raku.comma.psi.stub.impl

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import org.raku.comma.parsing.RakuElementTypes
import org.raku.comma.psi.RakuEnum
import org.raku.comma.psi.stub.RakuEnumStub
import org.raku.comma.psi.stub.RakuScopedDeclStub

class RakuEnumStubImpl(
    stub: StubElement<*>?,
    private val enumName: String,
    private val isExported: Boolean,
    private val myEnumValues: Collection<String>
) : StubBase<RakuEnum>(stub, RakuElementTypes.ENUM), RakuEnumStub {

    override fun getTypeName(): String {
        return enumName
    }

    override fun getScope(): String {
        val parent = getParentStub()
        return if (parent is RakuScopedDeclStub) parent.getScope() else "our"
    }

    override fun isExported(): Boolean {
        return isExported
    }

    override fun getEnumValues(): Collection<String> {
        return myEnumValues
    }
}
