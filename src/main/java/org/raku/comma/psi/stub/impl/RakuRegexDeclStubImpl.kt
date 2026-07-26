package org.raku.comma.psi.stub.impl

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import org.raku.comma.parsing.RakuElementTypes
import org.raku.comma.psi.RakuRegexDecl
import org.raku.comma.psi.stub.RakuRegexDeclStub
import org.raku.comma.psi.stub.RakuScopedDeclStub

class RakuRegexDeclStubImpl(stub: StubElement<*>?, private val regexName: String, private val isExported: Boolean) :
    StubBase<RakuRegexDecl>(stub, RakuElementTypes.REGEX_DECLARATION), RakuRegexDeclStub {

    override fun getRegexName(): String {
        return regexName
    }

    override fun getScope(): String {
        val parent = getParentStub()
        return if (parent is RakuScopedDeclStub) parent.getScope() else "has"
    }

    override fun isExported(): Boolean {
        return isExported
    }
}
