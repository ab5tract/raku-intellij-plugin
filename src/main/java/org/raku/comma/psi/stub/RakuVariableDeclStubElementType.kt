package org.raku.comma.psi.stub

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import com.intellij.util.ArrayUtil
import com.intellij.util.io.StringRef
import org.raku.comma.RakuLanguage
import org.raku.comma.psi.RakuVariable
import org.raku.comma.psi.RakuVariableDecl
import org.raku.comma.psi.impl.RakuVariableDeclImpl
import org.raku.comma.psi.stub.impl.RakuVariableDeclStubImpl
import org.raku.comma.psi.stub.index.RakuStubIndexKeys
import java.io.IOException
import java.util.Objects

class RakuVariableDeclStubElementType : IStubElementType<RakuVariableDeclStub, RakuVariableDecl>("VARIABLE_DECLARATION", RakuLanguage.INSTANCE) {
    override fun createPsi(stub: RakuVariableDeclStub): RakuVariableDecl {
        return RakuVariableDeclImpl(stub, this)
    }

    override fun createStub(psi: RakuVariableDecl, parentStub: StubElement<*>?): RakuVariableDeclStub {
        // inferType() (via RakuIsTraitReference.resolve()) can require cross-file
        // symbol resolution for @/%-sigil variables with an `is` trait, which
        // queries the stub index -- illegal from within stub building itself.
        // inferTypeForStub() computes the same type without that resolution.
        val type = (psi as RakuVariableDeclImpl).inferTypeForStub()
        return RakuVariableDeclStubImpl(parentStub, psi.variableNames, type.name, psi.isExported)
    }

    override fun getExternalId(): String {
        return "raku.stub.variableDeclaration"
    }

    @Throws(IOException::class)
    override fun serialize(stub: RakuVariableDeclStub, dataStream: StubOutputStream) {
        // We might have an arbitrary number of names declared, so save a counter too
        val names = stub.getVariableNames()
        dataStream.writeInt(names.size)
        for (name in names) dataStream.writeName(name)
        dataStream.writeName(stub.getVariableType())
        dataStream.writeBoolean(stub.isExported())
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): RakuVariableDeclStub {
        val numberOfNames = dataStream.readInt()
        val names = ArrayList<String>()
        for (i in 0 until numberOfNames) {
            names.add(Objects.requireNonNull<StringRef?>(dataStream.readName()).string)
        }
        val variableTypeRef = dataStream.readName()
        val type = variableTypeRef?.string
        val exported = dataStream.readBoolean()
        return RakuVariableDeclStubImpl(parentStub, ArrayUtil.toStringArray(names), type, exported)
    }

    override fun indexStub(stub: RakuVariableDeclStub, sink: IndexSink) {
        for (name in stub.getVariableNames()) {
            if (RakuVariable.getTwigil(name) == '*') {
                sink.occurrence(RakuStubIndexKeys.DYNAMIC_VARIABLES, name)
            } else {
                sink.occurrence(RakuStubIndexKeys.ALL_ATTRIBUTES, name)
            }
        }
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        val variableDecl = node.psi as RakuVariableDecl
        // Maybe it is dynamic, then we need to stub it
        for (name in variableDecl.variableNames) {
            if (RakuVariable.getTwigil(name) == '*') return true
        }
        // Attributes are stubbed as well
        val scope = variableDecl.scope
        return scope == "has" || scope == "our" && variableDecl.isExported
    }
}
