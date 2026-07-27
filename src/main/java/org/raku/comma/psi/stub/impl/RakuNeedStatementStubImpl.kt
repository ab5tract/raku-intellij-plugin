package org.raku.comma.psi.stub.impl

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import org.raku.comma.parsing.RakuElementTypes
import org.raku.comma.psi.RakuNeedStatement
import org.raku.comma.psi.stub.RakuNeedStatementStub

class RakuNeedStatementStubImpl(parent: StubElement<*>?, private val moduleNames: List<String>) :
    StubBase<RakuNeedStatement>(parent, RakuElementTypes.NEED_STATEMENT), RakuNeedStatementStub {

    override fun getModuleNames(): List<String> {
        return moduleNames
    }
}
