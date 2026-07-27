package org.raku.comma.psi.stub

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import org.raku.comma.RakuLanguage
import org.raku.comma.psi.RakuRegexDecl
import org.raku.comma.psi.impl.RakuRegexDeclImpl
import org.raku.comma.psi.stub.impl.RakuRegexDeclStubImpl
import org.raku.comma.psi.stub.index.RakuStubIndexKeys
import com.intellij.util.io.StringRef
import java.io.IOException
import java.util.Objects

class RakuRegexDeclStubElementType : IStubElementType<RakuRegexDeclStub, RakuRegexDecl>("REGEX_DECLARATION", RakuLanguage.INSTANCE) {
    override fun createPsi(stub: RakuRegexDeclStub): RakuRegexDecl {
        return RakuRegexDeclImpl(stub, this)
    }

    override fun createStub(psi: RakuRegexDecl, parentStub: StubElement<*>?): RakuRegexDeclStub {
        return RakuRegexDeclStubImpl(parentStub, psi.regexName, psi.isExported)
    }

    override fun getExternalId(): String {
        return "raku.stub.regexDeclaration"
    }

    @Throws(IOException::class)
    override fun serialize(stub: RakuRegexDeclStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.getRegexName())
        dataStream.writeBoolean(stub.isExported())
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): RakuRegexDeclStub {
        val regexNameRef = dataStream.readName()
        val exported = dataStream.readBoolean()
        return RakuRegexDeclStubImpl(parentStub, Objects.requireNonNull<StringRef?>(regexNameRef).string, exported)
    }

    override fun indexStub(stub: RakuRegexDeclStub, sink: IndexSink) {
        sink.occurrence(RakuStubIndexKeys.ALL_REGEXES, stub.getRegexName())
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        return (node.psi as RakuRegexDecl).regexName != "<anon>"
    }
}
