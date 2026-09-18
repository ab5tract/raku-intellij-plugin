package org.raku.comma.psi

import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import org.raku.comma.psi.external.RakuExternalPsiElement
import org.raku.comma.psi.stub.index.RakuIndexableType
import org.raku.comma.psi.stub.index.RakuStubIndexKeys
import org.raku.comma.utils.CommaProjectUtil

/**
 * Rakudo's core sources (src/Raku/ast, src/core.c, ...) are concatenated at
 * build time, so no `use` statements connect their files and lexical
 * resolution of a name like RakuAST::Node either finds nothing or lands on
 * the SDK setting's synthetic external symbol -- which has no containing
 * file, so ctrl+click goes nowhere. For Rakudo-core files, prefer the
 * project's own global type declaration from the GLOBAL_TYPES stub index.
 */
object RakudoCoreTypeResolution {

    @JvmStatic
    fun preferProjectGlobalType(ref: RakuPsiElement, typeName: String, lexical: PsiElement?): PsiElement? {
        // A real, navigable in-project resolution always wins.
        if (lexical != null && lexical !is RakuExternalPsiElement && lexical.containingFile != null) {
            return lexical
        }
        val file = ref.containingFile ?: return lexical
        if (!CommaProjectUtil.isRakudoCoreFile(file)) return lexical
        if (DumbService.isDumb(ref.project)) return lexical

        val hits = StubIndex.getElements(
            RakuStubIndexKeys.GLOBAL_TYPES,
            typeName,
            ref.project,
            GlobalSearchScope.projectScope(ref.project),
            RakuIndexableType::class.java
        )
        // Deterministic pick when several core files declare the name.
        return hits.minByOrNull { it.containingFile?.virtualFile?.path ?: "" } ?: lexical
    }
}
