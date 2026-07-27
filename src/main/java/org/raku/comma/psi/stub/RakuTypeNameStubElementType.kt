package org.raku.comma.psi.stub

import com.intellij.psi.stubs.*
import org.raku.comma.RakuLanguage
import org.raku.comma.psi.RakuTypeName
import org.raku.comma.psi.impl.RakuTypeNameImpl
import org.raku.comma.psi.stub.impl.RakuTypeNameStubImpl
import java.io.IOException
import java.util.Objects

class RakuTypeNameStubElementType : IStubElementType<RakuTypeNameStub, RakuTypeName>("TYPE_NAME", RakuLanguage.INSTANCE) {
    override fun createPsi(stub: RakuTypeNameStub): RakuTypeName {
        return RakuTypeNameImpl(stub, this)
    }

    override fun createStub(psi: RakuTypeName, parentStub: StubElement<*>?): RakuTypeNameStub {
        return RakuTypeNameStubImpl(parentStub, psi.typeName)
    }

    override fun getExternalId(): String {
        return "raku.stub.typeName"
    }

    @Throws(IOException::class)
    override fun serialize(stub: RakuTypeNameStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.getTypeName())
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): RakuTypeNameStub {
        // NOTE: calls .toString() rather than .getString(), with no null check --
        // a pre-existing quirk (see RakuTraitStubElementType's siblings null-check
        // consistently; this one doesn't) preserved verbatim, not "fixed" here.
        // Objects.requireNonNull reproduces the original's implicit NPE-on-null
        // (Kotlin's Any?.toString() is null-safe by default and would silently
        // swallow a null into the literal string "null" instead of crashing).
        val typename = Objects.requireNonNull(dataStream.readName())
        return RakuTypeNameStubImpl(parentStub, typename.toString())
    }

    override fun indexStub(stub: RakuTypeNameStub, sink: IndexSink) {
    }
}
