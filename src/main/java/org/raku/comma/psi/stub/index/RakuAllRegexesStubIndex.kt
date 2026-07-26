package org.raku.comma.psi.stub.index

import com.intellij.openapi.extensions.InternalIgnoreDependencyViolation
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import org.raku.comma.psi.RakuRegexDecl

@InternalIgnoreDependencyViolation
class RakuAllRegexesStubIndex : StringStubIndexExtension<RakuRegexDecl>() {
    override fun getVersion(): Int {
        return INDEX_VERSION
    }

    override fun getKey(): StubIndexKey<String, RakuRegexDecl> {
        return RakuStubIndexKeys.ALL_REGEXES
    }

    companion object {
        private const val INDEX_VERSION = 3
        private val instance = RakuAllRegexesStubIndex()

        @JvmStatic
        fun getInstance(): RakuAllRegexesStubIndex {
            return instance
        }
    }
}
