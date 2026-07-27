package org.raku.comma.psi.stub

import org.raku.comma.psi.RakuRoutineDecl

interface RakuRoutineDeclStub : RakuDeclStub<RakuRoutineDecl> {
    fun getRoutineKind(): String
    fun getRoutineName(): String
    fun isPrivate(): Boolean
    fun getMultiness(): String
    fun getReturnType(): String?
}
