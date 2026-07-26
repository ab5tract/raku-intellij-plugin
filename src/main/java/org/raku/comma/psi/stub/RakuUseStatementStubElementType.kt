package org.raku.comma.psi.stub

import com.intellij.psi.stubs.*
import org.raku.comma.RakuLanguage
import org.raku.comma.psi.RakuUseStatement
import org.raku.comma.psi.impl.RakuUseStatementImpl
import org.raku.comma.psi.stub.impl.RakuUseStatementStubImpl
import java.io.IOException

class RakuUseStatementStubElementType : IStubElementType<RakuUseStatementStub, RakuUseStatement>("USE_STATEMENT", RakuLanguage.INSTANCE) {
    override fun createPsi(stub: RakuUseStatementStub): RakuUseStatement {
        return RakuUseStatementImpl(stub, this)
    }

    override fun createStub(psi: RakuUseStatement, parentStub: StubElement<*>?): RakuUseStatementStub {
        return RakuUseStatementStubImpl(parentStub, psi.moduleName)
    }

    override fun getExternalId(): String {
        return "raku.stub.useStatement"
    }

    @Throws(IOException::class)
    override fun serialize(stub: RakuUseStatementStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.getModuleName())
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): RakuUseStatementStub {
        val ref = dataStream.readName()
        return RakuUseStatementStubImpl(parentStub, ref?.string)
    }

    override fun indexStub(stub: RakuUseStatementStub, sink: IndexSink) {
    }
}
