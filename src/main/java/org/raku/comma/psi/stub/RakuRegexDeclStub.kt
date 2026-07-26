package org.raku.comma.psi.stub

import org.raku.comma.psi.RakuRegexDecl

interface RakuRegexDeclStub : RakuDeclStub<RakuRegexDecl> {
    fun getRegexName(): String
}
