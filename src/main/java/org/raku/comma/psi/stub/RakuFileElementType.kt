package org.raku.comma.psi.stub

import com.intellij.psi.PsiFile
import com.intellij.psi.StubBuilder
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.tree.IStubFileElementType
import org.raku.comma.RakuLanguage
import org.raku.comma.psi.stub.impl.RakuFileStubImpl
import org.raku.comma.psi.stub.index.RakuStubIndexKeys
import java.io.IOException

class RakuFileElementType : IStubFileElementType<RakuFileStub>(RakuLanguage.INSTANCE) {
    override fun getStubVersion(): Int {
        return STUB_VERSION
    }

    override fun getBuilder(): StubBuilder {
        return RakuFileStubBuilder()
    }

    override fun getExternalId(): String {
        return "raku.stub.file"
    }

    @Throws(IOException::class)
    override fun serialize(stub: RakuFileStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.getCompilationUnitName())
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): RakuFileStub {
        val compilationUnitName = dataStream.readName()
        return RakuFileStubImpl(null, compilationUnitName?.string)
    }

    // The base class's real indexStub(PsiFileStub, IndexSink) is not generic on T
    // (unlike serialize/deserialize), so overriding it with a RakuFileStub-typed
    // parameter -- what the original Java did -- isn't accepted by Kotlin's
    // override checker here. Matching the platform's exact erased signature and
    // casting internally still overrides correctly at the JVM level, since only
    // RakuFileStubImpl instances ever flow through this element type.
    override fun indexStub(stub: PsiFileStub<PsiFile>, sink: IndexSink) {
        val fileStub = stub as RakuFileStub
        val compUnitName = fileStub.getCompilationUnitName()
        if (compUnitName != null) sink.occurrence(RakuStubIndexKeys.PROJECT_MODULES, compUnitName)
    }

    companion object {
        const val STUB_VERSION: Int = 29
    }
}
