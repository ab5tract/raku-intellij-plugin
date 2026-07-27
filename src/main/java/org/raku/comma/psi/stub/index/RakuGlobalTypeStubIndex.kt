package org.raku.comma.psi.stub.index

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

class RakuGlobalTypeStubIndex : StringStubIndexExtension<RakuIndexableType>() {
    override fun getVersion(): Int {
        return INDEX_VERSION
    }

    override fun getKey(): StubIndexKey<String, RakuIndexableType> {
        return RakuStubIndexKeys.GLOBAL_TYPES
    }

    companion object {
        private const val INDEX_VERSION = 4
        private val instance = RakuGlobalTypeStubIndex()

        @JvmStatic
        fun getInstance(): RakuGlobalTypeStubIndex {
            return instance
        }
    }
}
