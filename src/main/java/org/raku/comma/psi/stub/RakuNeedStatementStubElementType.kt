package org.raku.comma.psi.stub

import com.intellij.psi.stubs.*
import org.raku.comma.RakuLanguage
import org.raku.comma.psi.RakuNeedStatement
import org.raku.comma.psi.impl.RakuNeedStatementImpl
import org.raku.comma.psi.stub.impl.RakuNeedStatementStubImpl
import com.intellij.util.io.StringRef
import java.io.IOException
import java.util.Objects

class RakuNeedStatementStubElementType : IStubElementType<RakuNeedStatementStub, RakuNeedStatement>("NEED_STATEMENT", RakuLanguage.INSTANCE) {
    override fun createPsi(stub: RakuNeedStatementStub): RakuNeedStatement {
        return RakuNeedStatementImpl(stub, this)
    }

    override fun createStub(psi: RakuNeedStatement, parentStub: StubElement<*>?): RakuNeedStatementStub {
        return RakuNeedStatementStubImpl(parentStub, psi.moduleNames)
    }

    override fun getExternalId(): String {
        return "raku.stub.needStatement"
    }

    @Throws(IOException::class)
    override fun serialize(stub: RakuNeedStatementStub, dataStream: StubOutputStream) {
        val names = stub.getModuleNames()
        dataStream.writeInt(names.size)
        for (name in names) dataStream.writeName(name)
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): RakuNeedStatementStub {
        val elems = dataStream.readInt()
        val names = ArrayList<String>()
        for (i in 0 until elems) {
            val ref = dataStream.readName()
            names.add(Objects.requireNonNull<StringRef?>(ref).string)
        }
        return RakuNeedStatementStubImpl(parentStub, names)
    }

    override fun indexStub(stub: RakuNeedStatementStub, sink: IndexSink) {
    }
}
