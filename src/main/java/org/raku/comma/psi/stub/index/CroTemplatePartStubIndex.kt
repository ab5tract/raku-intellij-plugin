package org.raku.comma.psi.stub.index

import com.intellij.openapi.extensions.InternalIgnoreDependencyViolation
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import org.raku.comma.cro.template.psi.CroTemplatePart
import org.raku.comma.cro.template.psi.stub.index.CroTemplateStubIndexKeys

@InternalIgnoreDependencyViolation
class CroTemplatePartStubIndex : StringStubIndexExtension<CroTemplatePart>() {
    override fun getVersion(): Int {
        return INDEX_VERSION
    }

    override fun getKey(): StubIndexKey<String, CroTemplatePart> {
        return CroTemplateStubIndexKeys.TEMPLATE_PART
    }

    companion object {
        private const val INDEX_VERSION = 6
        private val instance = CroTemplatePartStubIndex()

        @JvmStatic
        fun getInstance(): CroTemplatePartStubIndex {
            return instance
        }
    }
}
