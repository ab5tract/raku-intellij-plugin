package org.raku.comma.psi.stub

import org.raku.comma.psi.RakuSubset

interface RakuSubsetStub : RakuTypeStub<RakuSubset> {
    fun getSubsetBaseTypeName(): String
}
