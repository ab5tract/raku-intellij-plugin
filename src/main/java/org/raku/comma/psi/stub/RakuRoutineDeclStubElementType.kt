package org.raku.comma.psi.stub

import com.intellij.psi.stubs.*
import org.raku.comma.RakuLanguage
import org.raku.comma.psi.RakuRoutineDecl
import org.raku.comma.psi.impl.RakuRoutineDeclImpl
import org.raku.comma.psi.stub.impl.RakuRoutineDeclStubImpl
import org.raku.comma.psi.stub.index.RakuStubIndexKeys
import org.raku.comma.psi.type.RakuUntyped
import com.intellij.util.io.StringRef
import java.io.IOException
import java.util.Objects

class RakuRoutineDeclStubElementType : IStubElementType<RakuRoutineDeclStub, RakuRoutineDecl>("ROUTINE_DECLARATION", RakuLanguage.INSTANCE) {
    override fun createPsi(stub: RakuRoutineDeclStub): RakuRoutineDecl {
        return RakuRoutineDeclImpl(stub, this)
    }

    override fun createStub(psi: RakuRoutineDecl, parentStub: StubElement<*>?): RakuRoutineDeclStub {
        val returnType = psi.returnType
        val returnTypeName = if (returnType is RakuUntyped) "" else returnType.name
        return RakuRoutineDeclStubImpl(
            parentStub, psi.routineName, psi.routineKind,
            psi.isPrivate, psi.isExported, psi.multiness, returnTypeName
        )
    }

    override fun getExternalId(): String {
        return "raku.stub.routineDeclaration"
    }

    @Throws(IOException::class)
    override fun serialize(stub: RakuRoutineDeclStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.getRoutineName())
        dataStream.writeName(stub.getRoutineKind())
        dataStream.writeBoolean(stub.isPrivate())
        dataStream.writeBoolean(stub.isExported())
        dataStream.writeName(stub.getMultiness())
        dataStream.writeName(stub.getReturnType())
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): RakuRoutineDeclStub {
        val routineNameRef = dataStream.readName()
        val routineKindRef = dataStream.readName()
        val isPrivate = dataStream.readBoolean()
        val exported = dataStream.readBoolean()
        val multiness = dataStream.readName()
        val returnType = dataStream.readName()
        return RakuRoutineDeclStubImpl(
            parentStub,
            Objects.requireNonNull<StringRef?>(routineNameRef).string,
            Objects.requireNonNull<StringRef?>(routineKindRef).string,
            isPrivate,
            exported,
            Objects.requireNonNull<StringRef?>(multiness).string,
            returnType?.string
        )
    }

    override fun indexStub(stub: RakuRoutineDeclStub, sink: IndexSink) {
        sink.occurrence(RakuStubIndexKeys.ALL_ROUTINES, stub.getRoutineName())
    }
}
