package org.raku.comma.psi.stub

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import com.intellij.util.io.StringRef
import org.raku.comma.RakuLanguage
import org.raku.comma.psi.RakuPackageDecl
import org.raku.comma.psi.impl.RakuPackageDeclImpl
import org.raku.comma.psi.stub.impl.RakuPackageDeclStubImpl
import org.raku.comma.psi.stub.index.RakuStubIndexKeys
import java.io.IOException
import java.util.Objects

class RakuPackageDeclStubElementType : IStubElementType<RakuPackageDeclStub, RakuPackageDecl>("PACKAGE_DECLARATION", RakuLanguage.INSTANCE) {
    override fun shouldCreateStub(node: ASTNode): Boolean {
        return (node.psi as RakuPackageDecl).packageName != null
    }

    override fun createPsi(stub: RakuPackageDeclStub): RakuPackageDecl {
        return RakuPackageDeclImpl(stub, this)
    }

    override fun createStub(psi: RakuPackageDecl, parentStub: StubElement<*>?): RakuPackageDeclStub {
        return RakuPackageDeclStubImpl(parentStub, psi.packageKind, psi.packageName, psi.isExported)
    }

    override fun getExternalId(): String {
        return "raku.stub.packageDeclaration"
    }

    @Throws(IOException::class)
    override fun serialize(stub: RakuPackageDeclStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.getPackageKind())
        dataStream.writeName(stub.getTypeName())
        dataStream.writeBoolean(stub.isExported())
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): RakuPackageDeclStub {
        val packageKindRef = dataStream.readName()
        val packageNameRef = dataStream.readName()
        val exported = dataStream.readBoolean()
        return RakuPackageDeclStubImpl(
            parentStub,
            Objects.requireNonNull<StringRef?>(packageKindRef).string,
            Objects.requireNonNull<StringRef?>(packageNameRef).string,
            exported
        )
    }

    override fun indexStub(stub: RakuPackageDeclStub, sink: IndexSink) {
        val globalName = stub.getGlobalName()
        if (globalName != null) {
            sink.occurrence(RakuStubIndexKeys.GLOBAL_TYPES, globalName)
        } else {
            val lexicalName = stub.getLexicalName()
            if (lexicalName != null) sink.occurrence(RakuStubIndexKeys.LEXICAL_TYPES, lexicalName)
        }
    }
}
