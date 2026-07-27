package org.raku.comma.psi.stub

import org.raku.comma.psi.RakuConstant

interface RakuConstantStub : RakuDeclStub<RakuConstant> {
    fun getConstantName(): String?
}
