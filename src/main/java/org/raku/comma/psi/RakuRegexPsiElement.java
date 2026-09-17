package org.raku.comma.psi;

public interface RakuRegexPsiElement extends RakuPsiElement {
    default boolean mightMatchZeroWidth() {
        return false;
    }

    default boolean atomsMightMatchZeroWidth(RakuRegexAtom[] atoms) {
        // No atoms means we cannot prove anything about the contents (a
        // group holding an alternation or an aliased assertion has no direct
        // atom children) -- treating that as "might match nothing" flagged
        // regexes like `(\w | ':')+` that always progress. A warning
        // heuristic must only fire on what it can actually show.
        if (atoms == null || atoms.length == 0) return false;
        // Everything in the sequence of atoms must potentially match nothing.
        for (RakuRegexAtom atom : atoms) {
            if (!atom.mightMatchZeroWidth())
                return false;
        }
        return true;
    }
}
