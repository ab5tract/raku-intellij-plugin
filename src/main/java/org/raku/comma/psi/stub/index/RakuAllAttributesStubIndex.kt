package org.raku.comma.psi.stub.index

import com.intellij.openapi.extensions.InternalIgnoreDependencyViolation
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import org.raku.comma.psi.RakuVariableDecl

@InternalIgnoreDependencyViolation
class RakuAllAttributesStubIndex : StringStubIndexExtension<RakuVariableDecl>() {
    override fun getVersion(): Int {
        return INDEX_VERSION
    }

    override fun getKey(): StubIndexKey<String, RakuVariableDecl> {
        return RakuStubIndexKeys.ALL_ATTRIBUTES
    }

    companion object {
        private const val INDEX_VERSION = 4
        private val instance = RakuAllAttributesStubIndex()

        @JvmStatic
        fun getInstance(): RakuAllAttributesStubIndex {
            return instance
        }
    }
}
