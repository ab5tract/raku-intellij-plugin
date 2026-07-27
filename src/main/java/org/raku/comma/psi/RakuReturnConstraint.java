package org.raku.comma.psi;

import org.raku.comma.psi.type.RakuType;
import org.jetbrains.annotations.NotNull;

public interface RakuReturnConstraint extends RakuPsiElement {
    @NotNull
    RakuType getReturnType();

    /** {@link #getReturnType()} without index-backed resolution; safe during stub building. */
    @NotNull
    RakuType getReturnTypeForStub();
}
