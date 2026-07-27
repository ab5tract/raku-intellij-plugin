package org.raku.comma.psi.stub

import org.raku.comma.psi.RakuPackageDecl

interface RakuPackageDeclStub : RakuTypeStub<RakuPackageDecl> {
    fun getPackageKind(): String
}
