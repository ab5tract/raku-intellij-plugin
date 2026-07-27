package org.raku.comma.psi.stub

import com.intellij.psi.stubs.StubElement
import org.raku.comma.psi.RakuScopedDecl

interface RakuScopedDeclStub : StubElement<RakuScopedDecl> {
    fun getScope(): String
}
