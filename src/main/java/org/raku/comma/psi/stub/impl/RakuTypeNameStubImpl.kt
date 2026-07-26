package org.raku.comma.psi.stub.impl

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import org.raku.comma.parsing.RakuElementTypes
import org.raku.comma.psi.RakuTypeName
import org.raku.comma.psi.stub.RakuTypeNameStub

class RakuTypeNameStubImpl(parent: StubElement<*>?, private val typeName: String) :
    StubBase<RakuTypeName>(parent, RakuElementTypes.TYPE_NAME), RakuTypeNameStub {

    override fun getTypeName(): String {
        return typeName
    }
}
