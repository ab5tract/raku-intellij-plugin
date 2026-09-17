package org.raku.comma.psi

/**
 * The PSI island for one inactive `#?if` branch: a transparent container
 * whose children are the branch's statements parsed as if the directive
 * were true. Deliberately NOT a RakuPsiScope, so scope walkers
 * (getDeclarations/getSymbolContributors) descend into it and the branch's
 * declarations join the surrounding scope — that is what makes them
 * indexed, completable, and navigable.
 */
interface RakuCondBranch : RakuPsiElement {
    /** The raw directive condition, e.g. "jvm" or "!moar"; null if unrecoverable. */
    val condition: String?
}
