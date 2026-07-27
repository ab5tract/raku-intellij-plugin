package org.raku.comma.psi.stub.impl

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import org.raku.comma.parsing.RakuElementTypes
import org.raku.comma.psi.RakuTrait
import org.raku.comma.psi.stub.RakuTraitStub

class RakuTraitStubImpl(parent: StubElement<*>?, private val modifier: String, private val name: String) :
    StubBase<RakuTrait>(parent, RakuElementTypes.TRAIT), RakuTraitStub {

    override fun getTraitModifier(): String {
        return modifier
    }

    override fun getTraitName(): String {
        return name
    }
}
