package org.raku.comma.psi.stub

import com.intellij.psi.stubs.StubElement
import org.raku.comma.psi.RakuTrait

interface RakuTraitStub : StubElement<RakuTrait> {
    fun getTraitModifier(): String
    fun getTraitName(): String
}
