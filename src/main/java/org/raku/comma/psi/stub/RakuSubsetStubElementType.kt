package org.raku.comma.psi.stub

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import com.intellij.util.io.StringRef
import org.raku.comma.RakuLanguage
import org.raku.comma.psi.RakuSubset
import org.raku.comma.psi.impl.RakuSubsetImpl
import org.raku.comma.psi.stub.impl.RakuSubsetStubImpl
import org.raku.comma.psi.stub.index.RakuStubIndexKeys
import java.io.IOException
import java.util.Objects

class RakuSubsetStubElementType : IStubElementType<RakuSubsetStub, RakuSubset>("SUBSET", RakuLanguage.INSTANCE) {
    override fun createPsi(stub: RakuSubsetStub): RakuSubset {
        return RakuSubsetImpl(stub, this)
    }

    override fun createStub(psi: RakuSubset, parentStub: StubElement<*>?): RakuSubsetStub {
        return RakuSubsetStubImpl(parentStub, psi.subsetName, psi.isExported, psi.subsetBaseTypeName)
    }

    override fun getExternalId(): String {
        return "raku.stub.subset"
    }

    @Throws(IOException::class)
    override fun serialize(stub: RakuSubsetStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.getTypeName())
        dataStream.writeBoolean(stub.isExported())
        dataStream.writeName(stub.getSubsetBaseTypeName())
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): RakuSubsetStub {
        val subsetNameRef = dataStream.readName()
        val exported = dataStream.readBoolean()
        val subsetBaseRef = dataStream.readName()
        return RakuSubsetStubImpl(
            parentStub,
            Objects.requireNonNull<StringRef?>(subsetNameRef).string,
            exported,
            Objects.requireNonNull<StringRef?>(subsetBaseRef).string
        )
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        val subsetName = (node.psi as RakuSubset).subsetName
        return subsetName != null && subsetName != "<anon>"
    }

    override fun indexStub(stub: RakuSubsetStub, sink: IndexSink) {
        val globalName = stub.getGlobalName()
        if (globalName != null) {
            sink.occurrence(RakuStubIndexKeys.GLOBAL_TYPES, globalName)
        } else {
            val lexicalName = stub.getLexicalName()
            if (lexicalName != null) sink.occurrence(RakuStubIndexKeys.LEXICAL_TYPES, lexicalName)
        }
    }
}
