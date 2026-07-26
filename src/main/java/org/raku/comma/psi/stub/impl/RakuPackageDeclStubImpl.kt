package org.raku.comma.psi.stub.impl

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import org.raku.comma.parsing.RakuElementTypes
import org.raku.comma.psi.RakuPackageDecl
import org.raku.comma.psi.stub.RakuPackageDeclStub
import org.raku.comma.psi.stub.RakuScopedDeclStub

class RakuPackageDeclStubImpl(
    parent: StubElement<*>?,
    private val packageKind: String,
    private val packageName: String,
    private val isExported: Boolean
) : StubBase<RakuPackageDecl>(parent, RakuElementTypes.PACKAGE_DECLARATION), RakuPackageDeclStub {

    override fun getPackageKind(): String {
        return packageKind
    }

    override fun getTypeName(): String {
        return packageName
    }

    override fun getScope(): String {
        val parent = getParentStub()
        return if (parent is RakuScopedDeclStub) parent.getScope() else "our"
    }

    override fun isExported(): Boolean {
        return isExported
    }
}
