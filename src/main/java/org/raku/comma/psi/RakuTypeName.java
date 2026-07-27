package org.raku.comma.psi;

import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.StubBasedPsiElement;
import org.jetbrains.annotations.NotNull;
import org.raku.comma.psi.stub.RakuTypeNameStub;
import org.raku.comma.psi.type.RakuType;

public interface RakuTypeName extends RakuPsiElement, StubBasedPsiElement<RakuTypeNameStub>,
                                      PsiNamedElement, RakuExtractable {
    String getTypeName();

    /** {@link #inferType()} without the index-backed name resolution; safe to call during stub building. */
    @NotNull
    RakuType inferTypeForStub();
}
