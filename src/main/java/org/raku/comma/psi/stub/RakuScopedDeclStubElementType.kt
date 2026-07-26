package org.raku.comma.psi.stub

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.io.StringRef
import org.raku.comma.RakuLanguage
import org.raku.comma.psi.RakuPsiDeclaration
import org.raku.comma.psi.RakuScopedDecl
import org.raku.comma.psi.RakuVariableDecl
import org.raku.comma.psi.impl.RakuScopedDeclImpl
import org.raku.comma.psi.stub.impl.RakuScopedDeclStubImpl
import java.io.IOException
import java.util.Objects

class RakuScopedDeclStubElementType : IStubElementType<RakuScopedDeclStub, RakuScopedDecl>("SCOPED_DECLARATION", RakuLanguage.INSTANCE) {
    override fun createPsi(stub: RakuScopedDeclStub): RakuScopedDecl {
        return RakuScopedDeclImpl(stub, this)
    }

    override fun createStub(psi: RakuScopedDecl, parentStub: StubElement<*>?): RakuScopedDeclStub {
        return RakuScopedDeclStubImpl(parentStub, psi.scope)
    }

    override fun getExternalId(): String {
        return "raku.stub.scopedDecl"
    }

    @Throws(IOException::class)
    override fun serialize(stub: RakuScopedDeclStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.getScope())
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): RakuScopedDeclStub {
        // Every consumer of getScope() (RakuConstantStubImpl, RakuRegexDeclStubImpl,
        // RakuVariableDeclStubImpl all delegate to a RakuScopedDeclStub parent and
        // return its scope as their own non-null String) already assumes this is
        // always present -- treated the same as the unchecked-crash cases
        // (RakuTraitStub/RakuTypeNameStub/RakuRegexDeclStub) rather than propagating
        // String? into three call sites for a corrupted-stream edge case, even
        // though the original Java's ternary here reads as more deliberate than
        // those files' bare unchecked derefs.
        val scope = dataStream.readName()
        return RakuScopedDeclStubImpl(parentStub, Objects.requireNonNull<StringRef?>(scope).string)
    }

    override fun indexStub(stub: RakuScopedDeclStub, sink: IndexSink) {
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        val element = node.psi
        // Scope is either `has` for attribute or `our`, but with `is export` trait
        if (element !is RakuScopedDecl) return false

        if (element.scope == "has") return true
        if (element.scope != "our") return false
        val childDeclaration = PsiTreeUtil.getChildOfType(element, RakuPsiDeclaration::class.java)
        return childDeclaration is RakuVariableDecl && childDeclaration.isExported
    }
}
