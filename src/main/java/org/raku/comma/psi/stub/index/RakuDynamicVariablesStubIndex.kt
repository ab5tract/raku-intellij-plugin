package org.raku.comma.psi.stub.index

import com.intellij.openapi.extensions.InternalIgnoreDependencyViolation
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import org.raku.comma.psi.RakuVariableDecl

@InternalIgnoreDependencyViolation
class RakuDynamicVariablesStubIndex : StringStubIndexExtension<RakuVariableDecl>() {
    override fun getVersion(): Int {
        return INDEX_VERSION
    }

    override fun getKey(): StubIndexKey<String, RakuVariableDecl> {
        return RakuStubIndexKeys.DYNAMIC_VARIABLES
    }

    companion object {
        private const val INDEX_VERSION = 1
        private val instance = RakuDynamicVariablesStubIndex()

        @JvmStatic
        fun getInstance(): RakuDynamicVariablesStubIndex {
            return instance
        }
    }
}
