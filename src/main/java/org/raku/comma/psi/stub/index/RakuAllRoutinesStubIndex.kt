package org.raku.comma.psi.stub.index

import com.intellij.openapi.extensions.InternalIgnoreDependencyViolation
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import org.raku.comma.psi.RakuRoutineDecl

@InternalIgnoreDependencyViolation
class RakuAllRoutinesStubIndex : StringStubIndexExtension<RakuRoutineDecl>() {
    override fun getVersion(): Int {
        return INDEX_VERSION
    }

    override fun getKey(): StubIndexKey<String, RakuRoutineDecl> {
        return RakuStubIndexKeys.ALL_ROUTINES
    }

    companion object {
        private const val INDEX_VERSION = 6
        private val instance = RakuAllRoutinesStubIndex()

        @JvmStatic
        fun getInstance(): RakuAllRoutinesStubIndex {
            return instance
        }
    }
}
