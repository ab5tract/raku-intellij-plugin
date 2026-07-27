package org.raku.comma.psi.stub.impl

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import org.raku.comma.parsing.RakuElementTypes
import org.raku.comma.psi.RakuRoutineDecl
import org.raku.comma.psi.stub.RakuRoutineDeclStub
import org.raku.comma.psi.stub.RakuScopedDeclStub

class RakuRoutineDeclStubImpl(
    stub: StubElement<*>?,
    private val routineName: String,
    private val routineKind: String,
    private val isPrivate: Boolean,
    private val isExported: Boolean,
    private val multiness: String,
    private val returnType: String?
) : StubBase<RakuRoutineDecl>(stub, RakuElementTypes.ROUTINE_DECLARATION), RakuRoutineDeclStub {

    override fun getRoutineName(): String {
        return routineName
    }

    override fun getRoutineKind(): String {
        return routineKind
    }

    override fun getScope(): String {
        val parent = getParentStub()
        return if (parent is RakuScopedDeclStub) parent.getScope()
        else if (routineKind == "sub" || routineKind.isEmpty()) "my" else "has"
    }

    override fun isPrivate(): Boolean {
        return isPrivate
    }

    override fun isExported(): Boolean {
        return isExported
    }

    override fun getMultiness(): String {
        return multiness
    }

    override fun getReturnType(): String? {
        return returnType
    }
}
