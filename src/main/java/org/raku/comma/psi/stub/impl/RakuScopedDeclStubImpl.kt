package org.raku.comma.psi.stub.impl

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import org.raku.comma.parsing.RakuElementTypes
import org.raku.comma.psi.RakuScopedDecl
import org.raku.comma.psi.stub.RakuScopedDeclStub

class RakuScopedDeclStubImpl(parent: StubElement<*>?, private val scope: String) :
    StubBase<RakuScopedDecl>(parent, RakuElementTypes.SCOPED_DECLARATION), RakuScopedDeclStub {

    override fun getScope(): String {
        return scope
    }
}
