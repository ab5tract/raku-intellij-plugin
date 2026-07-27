package org.raku.comma.psi.stub

import com.intellij.psi.stubs.StubElement
import org.raku.comma.psi.RakuUseStatement

interface RakuUseStatementStub : StubElement<RakuUseStatement> {
    fun getModuleName(): String?
}
