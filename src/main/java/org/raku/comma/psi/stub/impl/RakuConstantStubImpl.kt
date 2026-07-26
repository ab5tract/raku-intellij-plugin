package org.raku.comma.psi.stub.impl

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import org.raku.comma.parsing.RakuElementTypes
import org.raku.comma.psi.RakuConstant
import org.raku.comma.psi.stub.RakuConstantStub
import org.raku.comma.psi.stub.RakuScopedDeclStub

class RakuConstantStubImpl(stub: StubElement<*>?, private val constantName: String?, private val isExported: Boolean) :
    StubBase<RakuConstant>(stub, RakuElementTypes.CONSTANT), RakuConstantStub {

    override fun getConstantName(): String? {
        return constantName
    }

    override fun getScope(): String {
        val parent = getParentStub()
        return if (parent is RakuScopedDeclStub) parent.getScope() else "our"
    }

    override fun isExported(): Boolean {
        return isExported
    }
}
