package org.raku.comma.psi.impl;

import org.raku.comma.psi.RakuASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.meta.PsiMetaData;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.raku.comma.parsing.RakuTokenTypes;
import org.raku.comma.psi.*;
import org.raku.comma.psi.symbols.RakuExplicitAliasedSymbol;
import org.raku.comma.psi.symbols.RakuExplicitSymbol;
import org.raku.comma.psi.symbols.RakuSymbolCollector;
import org.raku.comma.psi.symbols.RakuSymbolKind;
import org.raku.comma.psi.type.RakuParametricType;
import org.raku.comma.psi.type.RakuType;
import org.raku.comma.psi.type.RakuUntyped;
import org.raku.comma.sdk.RakuSettingTypeId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static org.raku.comma.parsing.RakuElementTypes.TYPE_NAME;

public class RakuParameterVariableImpl extends RakuASTWrapperPsiElement implements RakuParameterVariable {
    public RakuParameterVariableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return PsiTreeUtil.getChildOfType(this, RakuVariable.class);
    }

    @NotNull
    @Override
    public String getName() {
        PsiElement nameIdent = getNameIdentifier();
        return nameIdent != null ? nameIdent.getText() : "";
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        RakuRoutineDecl parent = PsiTreeUtil.getParentOfType(this, RakuRoutineDecl.class);
        if (parent == null)
            return super.getUseScope();
        if (Objects.equals(parent.getScope(), "unit"))
            return new LocalSearchScope(parent.getContainingFile(), parent.getContainingFile().getName());
        return new LocalSearchScope(parent, getName());
    }

    @Override
    public @NotNull String getScope() {
        return getName().contains("!") ? "has" : "my";
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        String variableName = getName();
        char sigil = RakuVariable.getSigil(variableName);
        char twigil = RakuVariable.getTwigil(variableName);
        String prefix = twigil != ' ' ?
                        sigil + String.valueOf(twigil) :
                        String.valueOf(sigil);
        RakuVariable variable =
            RakuElementFactory.createVariable(getProject(), prefix + name);
        PsiElement keyNode = findChildByType(RakuTokenTypes.VARIABLE);
        if (keyNode != null) {
            ASTNode newKeyNode = variable.getNode();
            getNode().replaceChild(keyNode.getNode(), newKeyNode);
        }
        return this;
    }

    @Override
    public String summary(boolean includeName) {
        String base = includeName ? getName() : getSigil();
        PsiElement defaultValue = PsiTreeUtil.getNextSiblingOfType(this, RakuParameterDefault.class);
        if (defaultValue != null)
            base += '?';
        return base;
    }

    @NotNull
    public String getSigil() {
        String name = getName();
        return name.length() == 0 ? "$" : String.valueOf(name.charAt(0));
    }

    @Override
    public @NotNull RakuType inferType() {
        // If there's a direct type placed on the element, go by that.
        RakuType sigilType = inferBySigil();
        PsiElement type = PsiTreeUtil.findSiblingBackward(this, TYPE_NAME, null);
        if (type instanceof RakuTypeName) {
            return sigilType == null
                   ? ((RakuTypeName)type).inferType()
                   : new RakuParametricType(sigilType, new RakuType[]{((RakuTypeName)type).inferType()});
        }

        // Otherwise, sometimes we have a context that can indicate a parameter type.
        RakuParameter parameter = PsiTreeUtil.getParentOfType(this, RakuParameter.class);
        RakuSignature signature = PsiTreeUtil.getParentOfType(parameter, RakuSignature.class);
        if (signature != null) {
            PsiElement signatureOwner = signature.getParent();
            if (signatureOwner instanceof RakuPointyBlock) {
                PsiElement binder = signatureOwner.getParent();
                if (binder instanceof RakuForStatement) {
                    int parameterIndex = ArrayUtil.indexOf(signature.getParameters(), parameter);
                    RakuType parameterType = ((RakuForStatement)binder).inferLoopParameterType(parameterIndex);
                    if (parameterType != RakuUntyped.INSTANCE)
                        return parameterType;
                }
            }
        }

        return sigilType != null ? sigilType : RakuUntyped.INSTANCE;
    }

    private @Nullable RakuType inferBySigil() {
        String sigil = getSigil();
        return switch (sigil) {
            case "@" -> lookupGlobalSymbol(RakuSettingTypeId.List);
            case "%" -> lookupGlobalSymbol(RakuSettingTypeId.Map);
            case "&" -> lookupGlobalSymbol(RakuSettingTypeId.Callable);
            default -> null;
        };
    }

    @Override
    public void contributeLexicalSymbols(RakuSymbolCollector collector) {
        String name = getName();
        if (name.length() > 1) {
            collector.offerSymbol(new RakuExplicitSymbol(RakuSymbolKind.Variable, this));
            if (!collector.isSatisfied() && name.startsWith("&") && getScope().equals("my"))
                collector.offerSymbol(new RakuExplicitAliasedSymbol(RakuSymbolKind.Routine,
                                                                    this, name.substring(1)));
        }
    }

    @Nullable
    @Override
    public PsiMetaData getMetaData() {
        String desigilname = getName();
        PsiElement decl = this;
        // Chop off sigil, if it's not sigil-only name
        if (desigilname.length() > 1)
            desigilname = desigilname.substring(1);
        // Chop off twigil if any
        if (desigilname.length() >= 2 && !Character.isLetter(desigilname.charAt(0)))
            desigilname = desigilname.substring(1);
        String finaldesigilname = desigilname;
        return new PsiMetaData() {
            @Override
            public PsiElement getDeclaration() {
                return decl;
            }

            @Override
            public String getName(PsiElement context) {
                return finaldesigilname;
            }

            @Override
            public String getName() {
                return finaldesigilname;
            }

            @Override
            public void init(PsiElement element) {
            }

            @Override
            public Object @NotNull [] getDependencies() {
                return ArrayUtil.EMPTY_OBJECT_ARRAY;
            }
        };
    }
}
