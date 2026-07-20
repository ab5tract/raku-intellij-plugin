package org.raku.comma.psi

import org.raku.comma.psi.symbols.RakuLexicalSymbolContributor
import org.raku.comma.psi.symbols.RakuSymbolCollector
import java.util.LinkedList
import java.util.Queue

interface RakuPsiScope : RakuPsiElement {
    fun getSymbolContributors(): List<RakuLexicalSymbolContributor> {
        val results = ArrayList<RakuLexicalSymbolContributor>()
        val visit: Queue<RakuPsiElement> = LinkedList()
        visit.add(this)
        while (visit.isNotEmpty()) {
            val current = visit.remove()
            var addChildren = false
            if (current === this) {
                addChildren = true
            } else {
                if (current is RakuLexicalSymbolContributor) {
                    results.add(current)
                }
                if (current !is RakuPsiScope) {
                    addChildren = true
                }
            }
            if (addChildren) {
                for (e in current.children) {
                    if (e is RakuPsiElement) {
                        visit.add(e)
                    }
                }
            }
        }
        return results
    }

    fun getDeclarations(): List<RakuPsiDeclaration> {
        val decls = ArrayList<RakuPsiDeclaration>()
        val visit: Queue<RakuPsiElement> = LinkedList()
        visit.add(this)
        while (visit.isNotEmpty()) {
            val current = visit.remove()
            var addChildren = false
            if (current === this) {
                addChildren = true
            } else {
                if (current is RakuPsiDeclaration) {
                    decls.add(current)
                }
                if (current !is RakuPsiScope) {
                    addChildren = true
                }
            }
            if (addChildren) {
                for (e in current.children) {
                    if (e is RakuPsiElement) {
                        visit.add(e)
                    }
                }
            }
        }
        return decls
    }

    fun contributeScopeSymbols(collector: RakuSymbolCollector) {}
}
