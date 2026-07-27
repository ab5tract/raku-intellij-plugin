package org.raku.comma.psi;

import com.intellij.psi.util.PsiTreeUtil;
import org.raku.comma.psi.type.RakuType;
import org.raku.comma.psi.type.RakuUnresolvedType;
import org.raku.comma.psi.type.RakuUntyped;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RakuSignatureHolder extends RakuMultiHolder {
    String getSignature();
    @Nullable
    RakuSignature getSignatureNode();
    @Nullable
    String getReturnsTrait();

    default String summarySignature() {
        RakuSignature signature = getSignatureNode();
        RakuType returnType = getReturnType();
        if (signature != null)
            return signature.summary(returnType);

        return returnType instanceof RakuUntyped ? "()" : "(--> " + returnType.getName() + ")";
    }

    default @NotNull RakuType getReturnType() {
        RakuReturnConstraint constraint = findReturnConstraint();
        if (constraint == null)
            return returnTypeWithoutConstraint();
        return constraint.getReturnType();
    }

    /**
     * {@link #getReturnType()} without index-backed name resolution, for use
     * from StubElementType.createStub -- stub building must not query indexes.
     * Yields the same type *name*, which is all the stub records.
     */
    default @NotNull RakuType getReturnTypeForStub() {
        RakuReturnConstraint constraint = findReturnConstraint();
        if (constraint == null)
            return returnTypeWithoutConstraint();
        return constraint.getReturnTypeForStub();
    }

    private @Nullable RakuReturnConstraint findReturnConstraint() {
        if (getReturnsTrait() != null)
            return null;
        RakuSignature signature = getSignatureNode();
        if (signature == null)
            return null;
        return PsiTreeUtil.getChildOfType(signature, RakuReturnConstraint.class);
    }

    private @NotNull RakuType returnTypeWithoutConstraint() {
        String retTrait = getReturnsTrait();
        return retTrait != null ? new RakuUnresolvedType(retTrait) : RakuUntyped.INSTANCE;
    }
}
