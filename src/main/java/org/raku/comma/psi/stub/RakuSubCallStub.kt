package org.raku.comma.psi.stub

import com.intellij.psi.stubs.StubElement
import org.raku.comma.psi.RakuSubCall

interface RakuSubCallStub : StubElement<RakuSubCall> {
    fun getName(): String
    fun getAllFrameworkData(): Map<String?, String?>
    fun getFrameworkData(frameworkName: String, key: String): String?
}
