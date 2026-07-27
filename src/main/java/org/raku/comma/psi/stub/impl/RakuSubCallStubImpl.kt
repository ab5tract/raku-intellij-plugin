package org.raku.comma.psi.stub.impl

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import org.raku.comma.parsing.RakuElementTypes
import org.raku.comma.psi.RakuSubCall
import org.raku.comma.psi.stub.RakuSubCallStub

class RakuSubCallStubImpl(
    parent: StubElement<*>?,
    private val name: String,
    private val frameworkData: Map<String?, String?>
) : StubBase<RakuSubCall>(parent, RakuElementTypes.SUB_CALL), RakuSubCallStub {

    override fun getName(): String {
        return name
    }

    override fun getAllFrameworkData(): Map<String?, String?> {
        return frameworkData
    }

    override fun getFrameworkData(frameworkName: String, key: String): String? {
        return frameworkData[frameworkName + "." + key]
    }
}
