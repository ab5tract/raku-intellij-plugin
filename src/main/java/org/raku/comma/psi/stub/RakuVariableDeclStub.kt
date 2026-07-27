package org.raku.comma.psi.stub

import org.raku.comma.psi.RakuVariableDecl

interface RakuVariableDeclStub : RakuDeclStub<RakuVariableDecl> {
    fun getVariableNames(): Array<String>
    fun getVariableType(): String?
}
