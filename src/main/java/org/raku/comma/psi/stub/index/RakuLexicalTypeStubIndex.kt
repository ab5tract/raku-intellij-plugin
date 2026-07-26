package org.raku.comma.psi.stub.index

import com.intellij.openapi.extensions.InternalIgnoreDependencyViolation
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

@InternalIgnoreDependencyViolation
class RakuLexicalTypeStubIndex : StringStubIndexExtension<RakuIndexableType>() {
    override fun getVersion(): Int {
        return INDEX_VERSION
    }

    override fun getKey(): StubIndexKey<String, RakuIndexableType> {
        return RakuStubIndexKeys.LEXICAL_TYPES
    }

    companion object {
        private const val INDEX_VERSION = 3
        private val instance = RakuLexicalTypeStubIndex()

        @JvmStatic
        fun getInstance(): RakuLexicalTypeStubIndex {
            return instance
        }
    }
}
