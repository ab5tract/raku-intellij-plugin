package org.raku.comma.psi.stub

import com.intellij.psi.stubs.StubElement
import org.raku.comma.psi.RakuNeedStatement

interface RakuNeedStatementStub : StubElement<RakuNeedStatement> {
    fun getModuleNames(): List<String>
}
