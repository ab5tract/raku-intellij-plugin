package org.raku.comma.psi.stub.index

import com.intellij.openapi.extensions.InternalIgnoreDependencyViolation
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import org.raku.comma.psi.RakuConstant

@InternalIgnoreDependencyViolation
class RakuAllConstantsStubIndex : StringStubIndexExtension<RakuConstant>() {
    override fun getVersion(): Int {
        return INDEX_VERSION
    }

    override fun getKey(): StubIndexKey<String, RakuConstant> {
        return RakuStubIndexKeys.ALL_CONSTANTS
    }

    companion object {
        private const val INDEX_VERSION = 3
        private val instance = RakuAllConstantsStubIndex()

        @JvmStatic
        fun getInstance(): RakuAllConstantsStubIndex {
            return instance
        }
    }
}
