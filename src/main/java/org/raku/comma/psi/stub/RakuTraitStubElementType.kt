package org.raku.comma.psi.stub

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import com.intellij.util.io.StringRef
import org.raku.comma.RakuLanguage
import org.raku.comma.psi.RakuTrait
import org.raku.comma.psi.impl.RakuTraitImpl
import org.raku.comma.psi.stub.impl.RakuTraitStubImpl
import java.io.IOException
import java.util.Objects

class RakuTraitStubElementType : IStubElementType<RakuTraitStub, RakuTrait>("TRAIT", RakuLanguage.INSTANCE) {
    override fun createPsi(stub: RakuTraitStub): RakuTrait {
        return RakuTraitImpl(stub, this)
    }

    override fun createStub(psi: RakuTrait, parentStub: StubElement<*>?): RakuTraitStub {
        return RakuTraitStubImpl(parentStub, psi.traitModifier, psi.traitName)
    }

    override fun getExternalId(): String {
        return "raku.stub.trait"
    }

    @Throws(IOException::class)
    override fun serialize(stub: RakuTraitStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.getTraitName())
        dataStream.writeName(stub.getTraitModifier())
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): RakuTraitStub {
        val name = Objects.requireNonNull<StringRef?>(dataStream.readName()).string
        val modifier = Objects.requireNonNull<StringRef?>(dataStream.readName()).string
        return RakuTraitStubImpl(parentStub, modifier, name)
    }

    override fun indexStub(stub: RakuTraitStub, sink: IndexSink) {
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        val psi = node.psi
        if (psi !is RakuTrait) return false
        val modifier = psi.traitModifier
        return modifier == "does" || modifier == "is"
    }
}
