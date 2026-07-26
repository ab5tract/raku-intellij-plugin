package org.raku.comma.psi.stub

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import org.raku.comma.RakuLanguage
import org.raku.comma.psi.RakuConstant
import org.raku.comma.psi.impl.RakuConstantImpl
import org.raku.comma.psi.stub.impl.RakuConstantStubImpl
import org.raku.comma.psi.stub.index.RakuStubIndexKeys
import java.io.IOException

class RakuConstantStubElementType : IStubElementType<RakuConstantStub, RakuConstant>("CONSTANT", RakuLanguage.INSTANCE) {
    override fun createPsi(stub: RakuConstantStub): RakuConstant {
        return RakuConstantImpl(stub, this)
    }

    override fun createStub(psi: RakuConstant, parentStub: StubElement<*>?): RakuConstantStub {
        return RakuConstantStubImpl(parentStub, psi.constantName, psi.isExported)
    }

    override fun getExternalId(): String {
        return "raku.stub.constant"
    }

    @Throws(IOException::class)
    override fun serialize(stub: RakuConstantStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.getConstantName())
        dataStream.writeBoolean(stub.isExported())
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): RakuConstantStub {
        val constantNameRef = dataStream.readName()
        val isExport = dataStream.readBoolean()
        return RakuConstantStubImpl(parentStub, constantNameRef?.string, isExport)
    }

    override fun indexStub(stub: RakuConstantStub, sink: IndexSink) {
        sink.occurrence(RakuStubIndexKeys.ALL_CONSTANTS, stub.getConstantName()!!)
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        return (node.psi as RakuConstant).constantName != null
    }
}
