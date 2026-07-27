package org.raku.comma.psi.stub.index

import com.intellij.openapi.extensions.InternalIgnoreDependencyViolation
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import org.raku.comma.psi.RakuFile

@InternalIgnoreDependencyViolation
class ProjectModulesStubIndex : StringStubIndexExtension<RakuFile>() {
    override fun getKey(): StubIndexKey<String, RakuFile> {
        return RakuStubIndexKeys.PROJECT_MODULES
    }

    override fun getVersion(): Int {
        return INDEX_VERSION
    }

    companion object {
        private const val INDEX_VERSION = 3
        private val instance = ProjectModulesStubIndex()

        @JvmStatic
        fun getInstance(): ProjectModulesStubIndex {
            return instance
        }
    }
}
