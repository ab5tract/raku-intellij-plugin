package org.raku.comma.psi.stub

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import com.intellij.util.io.StringRef
import org.raku.comma.RakuLanguage
import org.raku.comma.psi.RakuEnum
import org.raku.comma.psi.impl.RakuEnumImpl
import org.raku.comma.psi.stub.impl.RakuEnumStubImpl
import org.raku.comma.psi.stub.index.RakuStubIndexKeys
import java.io.IOException
import java.util.Objects
import java.util.StringJoiner

class RakuEnumStubElementType : IStubElementType<RakuEnumStub, RakuEnum>("ENUM", RakuLanguage.INSTANCE) {
    override fun createPsi(stub: RakuEnumStub): RakuEnum {
        return RakuEnumImpl(stub, this)
    }

    override fun createStub(psi: RakuEnum, parentStub: StubElement<*>?): RakuEnumStub {
        return RakuEnumStubImpl(parentStub, psi.enumName, psi.isExported, psi.enumValues)
    }

    override fun getExternalId(): String {
        return "raku.stub.enum"
    }

    @Throws(IOException::class)
    override fun serialize(stub: RakuEnumStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.getTypeName())
        dataStream.writeBoolean(stub.isExported())
        val joiner = StringJoiner("#")
        for (type in stub.getEnumValues()) {
            joiner.add(type)
        }
        dataStream.writeName(joiner.toString())
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): RakuEnumStub {
        val enumNameRef = dataStream.readName()
        val exported = dataStream.readBoolean()
        val values = dataStream.readName()
        val enumValues: List<String> = if (values == null) ArrayList() else ArrayList(values.string.split("#"))
        return RakuEnumStubImpl(parentStub, Objects.requireNonNull<StringRef?>(enumNameRef).string, exported, enumValues)
    }

    override fun indexStub(stub: RakuEnumStub, sink: IndexSink) {
        val globalName = stub.getGlobalName()
        if (globalName != null) {
            sink.occurrence(RakuStubIndexKeys.GLOBAL_TYPES, globalName)
        } else {
            val lexicalName = stub.getLexicalName()
            if (lexicalName != null) sink.occurrence(RakuStubIndexKeys.LEXICAL_TYPES, lexicalName)
        }
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        val psi = node.psi
        return psi is RakuEnum && psi.enumName != null
    }
}
