package org.raku.comma.psi.stub

import com.intellij.psi.stubs.PsiFileStub
import org.raku.comma.psi.RakuFile
import org.raku.comma.psi.RakuPsiDeclaration

interface RakuFileStub : PsiFileStub<RakuFile> {
    // The name, inferred from path, that a `use` statement would be followed by
    // to resolve to this module.
    fun getCompilationUnitName(): String?

    // Locates everything that is exported and returns the matching PSI elements.
    fun getExports(): List<RakuPsiDeclaration>
}
