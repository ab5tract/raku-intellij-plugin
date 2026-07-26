package org.raku.comma.psi.stub.impl

import com.intellij.psi.stubs.PsiFileStubImpl
import com.intellij.psi.stubs.Stub
import com.intellij.psi.tree.IStubFileElementType
import org.raku.comma.parsing.RakuElementTypes
import org.raku.comma.psi.RakuFile
import org.raku.comma.psi.RakuPsiDeclaration
import org.raku.comma.psi.stub.RakuDeclStub
import org.raku.comma.psi.stub.RakuFileStub

class RakuFileStubImpl(
    file: RakuFile?,
    private val compilationUnitName: String?
) : PsiFileStubImpl<RakuFile>(file), RakuFileStub {

    override fun getType(): IStubFileElementType<*> {
        return RakuElementTypes.FILE
    }

    override fun getCompilationUnitName(): String? {
        return compilationUnitName
    }

    override fun getExports(): List<RakuPsiDeclaration> {
        val exports = ArrayList<RakuPsiDeclaration>()
        val toTry = ArrayList<Stub>()
        toTry.add(this)
        while (toTry.isNotEmpty()) {
            val current = toTry.removeAt(0)
            for (child in current.childrenStubs) {
                if (child is RakuDeclStub<*>) {
                    if (child.isExported()) exports.add(child.psi)
                }
                toTry.add(child)
            }
        }
        return exports
    }
}
