package org.raku.comma.psi.stub

import org.raku.comma.psi.RakuEnum

interface RakuEnumStub : RakuTypeStub<RakuEnum> {
    fun getEnumValues(): Collection<String>
}
