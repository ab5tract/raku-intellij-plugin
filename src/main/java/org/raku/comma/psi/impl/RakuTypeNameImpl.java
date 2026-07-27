package org.raku.comma.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import org.raku.comma.parsing.RakuTokenTypes;
import org.raku.comma.psi.*;
import org.raku.comma.psi.type.*;
import org.raku.comma.psi.stub.RakuTypeNameStub;
import org.raku.comma.psi.stub.RakuTypeNameStubElementType;
import org.raku.comma.utils.RakuPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RakuTypeNameImpl extends StubBasedPsiElementBase<RakuTypeNameStub> implements RakuTypeName {
    public RakuTypeNameImpl(@NotNull ASTNode node) {
        super(node);
    }

    public RakuTypeNameImpl(RakuTypeNameStub stub, RakuTypeNameStubElementType type) {
        super(stub, type);
    }

    @Override
    public PsiReference getReference() {
        return new RakuTypeNameReference(this);
    }

    @Override
    public String getTypeName() {
        RakuTypeNameStub stub = getStub();
        if (stub != null)
            return stub.getTypeName();
        RakuLongName longName = findChildByClass(RakuLongName.class);
        if (longName == null) { // For cases like "::?CLASS" not parsed as a long name
            return getFirstChild().getText();
        } else {
            return longName.getNameWithoutColonPairs();
        }
    }

    public String toString() {
        return getClass().getSimpleName() + "(Raku:TYPE_NAME)";
    }

    @Override
    public @NotNull RakuType inferType() {
        return inferType(true);
    }

    /**
     * Like {@link #inferType()}, but never resolves the type name.
     *
     * Resolution goes through {@link RakuTypeNameReference#resolve()}, which
     * consults the stub index -- illegal while a stub is being built, since it
     * makes indexing depend on the very index it is populating. In production
     * that degrades silently (the resolution just comes back empty); under the
     * test harness the platform's Logger.error is escalated to a hard failure.
     *
     * Skipping resolution costs the stub nothing: RakuResolvedType.getName()
     * and RakuUnresolvedType.getName() both return the bare type name, and
     * RakuRoutineDeclStubElementType.createStub only ever stores that name.
     * Real callers keep using inferType() and still get the resolution.
     */
    public @NotNull RakuType inferTypeForStub() {
        return inferType(false);
    }

    private @NotNull RakuType inferType(boolean resolve) {
        PsiElement resolution = resolve ? getReference().resolve() : null;
        return tweakType(resolution instanceof RakuPsiElement
               ? new RakuResolvedType(getTypeName(), (RakuPsiElement)resolution)
               : new RakuUnresolvedType(getTypeName()),
                         resolve);
    }

    // Nested type names (coercions, type parameters) have to inherit the
    // no-resolution mode, or the walk finds its way back into the index.
    private static RakuType inferNested(PsiElement element, boolean resolve) {
        if (!resolve && element instanceof RakuTypeNameImpl typeName)
            return typeName.inferTypeForStub();
        return element instanceof RakuPsiElement psi ? psi.inferType() : RakuUntyped.INSTANCE;
    }

    private RakuType tweakType(RakuType type, boolean resolve) {
        // Handle definedness type
        RakuLongName longName = findChildByClass(RakuLongName.class);
        if (longName != null) {
            for (RakuColonPair pair : longName.getColonPairs()) {
                if (pair.getKey().equals("D")) {
                    type = new RakuDefinednessType(type, true);
                    break;
                }
                if (pair.getKey().equals("U")) {
                    type = new RakuDefinednessType(type, false);
                    break;
                }
            }
        }

        // Coercion or parametric type.
        if (getNode().findChildByType(RakuTokenTypes.TYPE_COERCION_PARENTHESES_CLOSE) != null) {
            // Coercion is another embedded type name.
            RakuTypeName from = findChildByClass(RakuTypeName.class);
            if (from != null)
                type = new RakuCoercionType(type, inferNested(from, resolve));
        }
        else  {
            ASTNode curToken = getNode().findChildByType(RakuTokenTypes.TYPE_PARAMETER_BRACKET);
            if (curToken != null) {
                List<RakuType> typeArgs = new ArrayList<>();
                PsiElement arg = RakuPsiUtil.skipSpaces(curToken.getPsi().getNextSibling(), true);
                if (arg instanceof RakuInfixApplication && ((RakuInfixApplication)arg).isCommaOperator()) {
                    // List of parameters (for now, we assume all are types).
                    PsiElement[] operands = ((RakuInfixApplication)arg).getOperands();
                    for (PsiElement operand : operands) {
                        if (operand instanceof RakuPsiElement)
                            typeArgs.add(inferNested(operand, resolve));
                    }
                }
                else {
                    // One parameter (for now, we assume it's a type).
                    if (arg instanceof RakuPsiElement)
                        typeArgs.add(inferNested(arg, resolve));
                }
                if (!typeArgs.isEmpty())
                    type = new RakuParametricType(type, typeArgs.toArray(new RakuType[0]));
            }
        }

        return type;
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        RakuLongName type = RakuElementFactory
            .createTypeName(getProject(), name);
        RakuLongName longName = findChildByClass(RakuLongName.class);
        if (longName != null) {
            ASTNode keyNode = longName.getNode();
            ASTNode newKeyNode = type.getNode();
            getNode().replaceChild(keyNode, newKeyNode);
        }
        return this;
    }
}
