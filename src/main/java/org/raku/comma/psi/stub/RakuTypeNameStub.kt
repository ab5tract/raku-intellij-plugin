package org.raku.comma.psi.stub

import com.intellij.psi.stubs.StubElement
import org.raku.comma.psi.RakuTypeName

interface RakuTypeNameStub : StubElement<RakuTypeName> {
    fun getTypeName(): String
}
