package org.raku.comma.psi.stub.impl

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import org.raku.comma.parsing.RakuElementTypes
import org.raku.comma.psi.RakuUseStatement
import org.raku.comma.psi.stub.RakuUseStatementStub

class RakuUseStatementStubImpl(parent: StubElement<*>?, private val moduleName: String?) :
    StubBase<RakuUseStatement>(parent, RakuElementTypes.USE_STATEMENT), RakuUseStatementStub {

    override fun getModuleName(): String? {
        return moduleName
    }
}
