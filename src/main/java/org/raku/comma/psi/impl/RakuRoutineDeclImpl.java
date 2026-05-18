package org.raku.comma.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.meta.PsiMetaData;
import com.intellij.psi.meta.PsiMetaOwner;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.raku.comma.highlighter.RakuElementVisitor;
import org.raku.comma.parsing.RakuTokenTypes;
import org.raku.comma.pod.PodDomBuildingContext;
import org.raku.comma.pod.PodDomClassyDeclarator;
import org.raku.comma.pod.PodDomParameterDeclarator;
import org.raku.comma.pod.PodDomRoutineDeclarator;
import org.raku.comma.psi.*;
import org.raku.comma.psi.symbols.*;
import org.raku.comma.psi.stub.RakuRoutineDeclStub;
import org.raku.comma.psi.stub.RakuTraitStub;
import org.raku.comma.psi.type.RakuType;
import org.raku.comma.psi.type.RakuUntyped;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

import static org.raku.comma.parsing.RakuElementTypes.BLOCKOID;
import static org.raku.comma.parsing.RakuElementTypes.LONG_NAME;

public class RakuRoutineDeclImpl extends RakuMemberStubBasedPsi<RakuRoutineDeclStub>
  implements RakuRoutineDecl, RakuSignatureHolder, PsiMetaOwner {
    private static final String[] ROUTINE_SYMBOLS = { "$/", "$!", "$_", "&?ROUTINE", "&?BLOCK" };

    public RakuRoutineDeclImpl(@NotNull ASTNode node) {
        super(node);
    }

    public RakuRoutineDeclImpl(final RakuRoutineDeclStub stub, final IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @Override
    public String getRoutineKind() {
        RakuRoutineDeclStub stub = getStub();
        if (stub != null)
            return stub.getRoutineKind();

        PsiElement declarator = getDeclaratorNode();
        return declarator == null ? "sub" : declarator.getText();
    }

    @Override
    public PsiElement getDeclaratorNode() {
        return findChildByType(RakuTokenTypes.ROUTINE_DECLARATOR);
    }

    @Override
    public int getTextOffset() {
        PsiElement name = getNameIdentifier();
        return name == null ? 0 : name.getTextOffset();
    }

    @Override
    public String getSignature() {
        return getRoutineName() + summarySignature();
    }

    @Override
    public boolean isPrivate() {
        RakuRoutineDeclStub stub = getStub();
        if (stub != null) return stub.isPrivate();

        return getRoutineName().startsWith("!");
    }

    @Override
    public @NotNull RakuType getReturnType() {
        return RakuRoutineDecl.super.getReturnType();
    }

    @Override
    public boolean isMethod() {
        String kind = getRoutineKind();
        return kind.equals("method") || kind.equals("submethod");
    }

    @Override
    public boolean isSub() {
        return getRoutineKind().equals("sub");
    }

  @Override
  public boolean isPure() {
    return findTrait("is", "pure") != null;
  }

  @Override
    @Nullable
    public String getReturnsTrait() {
        RakuRoutineDeclStub stub = getStub();
        if (stub != null)
            for (StubElement<?> s : stub.getChildrenStubs()) {
                if (!(s instanceof RakuTraitStub traitStub)) continue;
                String modifier = traitStub.getTraitModifier();
                if (modifier.equals("returns") ||
                    modifier.equals("of"))
                    return ((RakuTraitStub)s).getTraitName();
            }
        Collection<RakuTrait> traits = PsiTreeUtil.findChildrenOfType(this, RakuTrait.class);
        for (RakuTrait trait : traits) {
            String modifier = trait.getTraitModifier();
            if (modifier.equals("returns") || modifier.equals("of"))
                return trait.getTraitName();
        }
        return null;
    }

    @Override
    public boolean isDeprecated() {
        RakuRoutineDeclStub stub = getStub();
        if (stub != null)
            for (StubElement<?> s : stub.getChildrenStubs()) {
                if (!(s instanceof RakuTraitStub traitStub)) continue;
                String modifier = traitStub.getTraitModifier();
                if (modifier.equals("is") && "DEPRECATED".equals(((RakuTraitStub)s).getTraitName()))
                    return true;
            }
        return findTrait("is", "DEPRECATED") != null;
    }

    @Override
    public @Nullable String getDeprecationMessage() {
        RakuTrait trait = findTrait("is", "DEPRECATED");
        if (trait != null) {
            RakuStrLiteral reason = PsiTreeUtil.findChildOfType(trait, RakuStrLiteral.class);
            if (reason != null)
                return reason.getStringText();
        }
        return null;
    }

    @Override
    public boolean isStubbed() {
        RakuBlockoid blockoid = findChildByType(BLOCKOID);
        if (blockoid == null) return false;
        PsiElement statementList = PsiTreeUtil.getChildOfType(blockoid, RakuStatementList.class);
        if (statementList == null || statementList.getChildren().length != 1) return false;
        PsiElement[] statement = statementList.getChildren()[0].getChildren();
        return statement.length != 0 && statement[0] instanceof RakuStubCode;
    }

    @Nullable
    @Override
    public RakuSignature getSignatureNode() {
        return findChildByClass(RakuSignature.class);
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return findChildByType(LONG_NAME);
    }

    @Override
    public PsiElement[] getContent() {
        RakuStatementList statementList = PsiTreeUtil.findChildOfType(this, RakuStatementList.class);
        if (statementList == null)
            return PsiElement.EMPTY_ARRAY;
        return statementList.getChildren();
    }

    @Override
    public RakuParameter[] getParams() {
        RakuSignature sig = findChildByClass(RakuSignature.class);
        if (sig != null)
            return sig.getParameters();
        return new RakuParameter[]{};
    }

    @Override
    public String getName() {
        RakuRoutineDeclStub stub = getStub();
        if (stub != null)
            return stub.getRoutineName();
        PsiElement nameIdentifier = getNameIdentifier();
        return nameIdentifier == null ? "<anon>" : nameIdentifier.getText();
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        RakuLongName newLongName = RakuElementFactory.createRoutineName(getProject(), name);
        RakuLongName longName = findChildByClass(RakuLongName.class);
        if (longName != null) {
            ASTNode keyNode = longName.getNode();
            ASTNode newKeyNode = newLongName.getNode();
            getNode().replaceChild(keyNode, newKeyNode);
        }
        return this;
    }

    @Override
    public String getRoutineName() {
        String name = getName();
        return name == null ? "<anon>" : name;
    }

    @Override
    public String presentableName() {
        return getName() + summarySignature();
    }

    @Override
    public String defaultScope() {
        return getRoutineKind().equals("sub") ? "my" : "has";
    }

    @Override
    public String getMultiness() {
        RakuRoutineDeclStub stub = getStub();
        if (stub != null)
            return stub.getMultiness();
        PsiElement parent = getParent();
        return parent instanceof RakuMultiDecl ? ((RakuMultiDecl)parent).getMultiness() : "only";
    }

    public String toString() {
        return getClass().getSimpleName() + "(Raku:ROUTINE_DECLARATION)";
    }

    @Override
    public void contributeLexicalSymbols(RakuSymbolCollector collector) {
        if (getScope().equals("unit"))
            contributeParametersOfUnit(collector);
        String name = getRoutineName();
        String scope = getScope();
        if (!name.equals("<anon>") && (scope.equals("my") || scope.equals("our"))) {
            String multiness = getMultiness();
            boolean isProto = multiness.equals("proto");
            if (isProto || multiness.equals("multi"))
                collector.offerMultiSymbol(new RakuExplicitSymbol(RakuSymbolKind.Routine, this), isProto);
            else
                collector.offerSymbol(new RakuExplicitSymbol(RakuSymbolKind.Routine, this));
            if (!collector.isSatisfied())
                collector.offerSymbol(new RakuExplicitAliasedSymbol(RakuSymbolKind.Variable,
                                                                    this, "&" + name));
        }
    }

    private void contributeParametersOfUnit(RakuSymbolCollector collector) {
        RakuParameter[] params = getParams();
        for (RakuParameter param : params) {
            RakuParameterVariable parameterVariable = PsiTreeUtil.findChildOfType(param, RakuParameterVariable.class);
            if (parameterVariable != null)
                parameterVariable.contributeLexicalSymbols(collector);
        }
    }

    @Override
    public void contributeMOPSymbols(RakuSymbolCollector collector, MOPSymbolsAllowed symbolsAllowed) {
        String name = getRoutineName();
        String scope = getScope();
        if (!name.equals("<anon>") && scope.equals("has")) {
            String routineKind = getRoutineKind();
            boolean visible;
            boolean isPrivate = name.startsWith("!");
            if (isPrivate) {
                // Private. Checked first as in theory a private submethod could exist.
                visible = symbolsAllowed.privateMethodsVisible;
            }
            else if (routineKind.equals("submethod")) {
                // It's a submethod; only contribute if they are visible here.
                visible = symbolsAllowed.submethodsVisible;
            }
            else {
                // Normal method.
                visible = true;
            }
            if (visible) {
                String multiness = getMultiness();
                boolean isProto = multiness.equals("proto");
                RakuExplicitAliasedSymbol sym = new RakuExplicitAliasedSymbol(RakuSymbolKind.Method, this,
                        isPrivate ? name : "." + name);
                if (isProto || multiness.equals("multi"))
                    collector.offerMultiSymbol(sym, isProto);
                else
                    collector.offerSymbol(sym);
            }
        }
    }

    @Nullable
    @Override
    public PsiMetaData getMetaData() {
        PsiElement decl = this;
        String un_dashed_name = getName();
        if (un_dashed_name == null) return null;
        // Chop off everything before last `-` symbol
        un_dashed_name = un_dashed_name.substring(un_dashed_name.lastIndexOf('-') + 1);
        String final_un_dashed_name = un_dashed_name;
        return new PsiMetaData() {
            @Override
            public PsiElement getDeclaration() {
                return decl;
            }

            @Override
            public String getName(PsiElement context) {
                return final_un_dashed_name;
            }

            @Override
            public String getName() {
                return final_un_dashed_name;
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

    @Override
    public void contributeScopeSymbols(RakuSymbolCollector collector) {
        for (String sym : ROUTINE_SYMBOLS) {
            collector.offerSymbol(new RakuImplicitSymbol(RakuSymbolKind.Variable, sym, this));
            if (collector.isSatisfied()) return;
        }
        String routineName = getRoutineName();
        String routineKind = getRoutineKind();
        if (routineKind.equals("method") || routineKind.equals("submethod")) {
            collector.offerSymbol(new RakuImplicitSymbol(RakuSymbolKind.Variable, "%_", this));
        }
        if (Objects.equals(routineName, "MAIN")) {
            collector.offerSymbol(new RakuImplicitSymbol(RakuSymbolKind.Variable, "$*USAGE"));
        } else if (Objects.equals(routineName, "GENERATE-USAGE")) {
            collector.offerSymbol(new RakuImplicitSymbol(RakuSymbolKind.Variable, "&*GENERATE-USAGE"));
        } else if (Objects.equals(routineName, "ARGS-TO-CAPTURE")) {
            collector.offerSymbol(new RakuImplicitSymbol(RakuSymbolKind.Variable, "&*ARGS-TO-CAPTURE"));
        }
    }

    @Override
    public void collectPodAndDocumentables(PodDomBuildingContext context) {
        // Only include things with a name.
        String name = getName();
        if (name == null || name.isEmpty())
            return;

        // Method or sub?
        if (isMethod()) {
            // Don't include private, BUILD, TWEAK or DESTROY, as these aren't for
            // external callers.
            if (isPrivate() || name.equals("BUILD") || name.equals("TWEAK") || name.equals("DESTROY"))
                return;

            // Method. Ensure there's an enclosing class.
            PodDomClassyDeclarator enclosingClass = context.currentClassyDeclarator();
            if (enclosingClass == null)
                return;

            // Build the doc node and add it to the class.
            PodDomRoutineDeclarator routine = new PodDomRoutineDeclarator(getTextOffset(), name, null,
                      getDocBlocks(), null, getRoutineKind(), getDocParameters(), getDocReturnType());
            enclosingClass.addMethod(routine);
        }
        else {
            // A sub should be our-scoped or exported.
            String globalName = getScope().equals("our")
                    ? context.prependGlobalNameParts(getRoutineName())
                    : null;
            RakuTrait exportTrait = findTrait("is", "export");
            if (globalName == null && exportTrait == null)
                return;

            PodDomRoutineDeclarator routine = new PodDomRoutineDeclarator(getTextOffset(), name, globalName,
                    getDocBlocks(), exportTrait, getRoutineKind(), getDocParameters(), getDocReturnType());
            context.addSub(routine);
        }
    }

    @Nullable
    private String getDocReturnType() {
        RakuType returnType = getReturnType();
        return returnType instanceof RakuUntyped ? null : returnType.getName();
    }

    private PodDomParameterDeclarator[] getDocParameters() {
        RakuSignature signature = getSignatureNode();
        if (signature == null)
            return new PodDomParameterDeclarator[0];
        RakuParameter[] parameters = signature.getParameters();
        PodDomParameterDeclarator[] result = new PodDomParameterDeclarator[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            RakuParameter parameter = parameters[i];
            result[i] = new PodDomParameterDeclarator(parameter.getTextOffset(), parameter.getVariableName(),
                    null, parameter.getDocBlocks(), parameter.summary(true));
        }
        return result;
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof RakuElementVisitor) {
            ((RakuElementVisitor)visitor).visitRakuElement(this);
        } else {
            super.accept(visitor);
        }
    }

    @Override
    public void subtreeChanged() {
        // Drop the possible EXPORT cache from the file if we edit EXPORT routine
        if (Objects.equals(getName(), "EXPORT")) {
            @NotNull PsiFile file = getContainingFile();
            if (file instanceof RakuFileImpl) {
                ((RakuFileImpl)file).dropExportCache();
            }
        }
        super.subtreeChanged();
    }
}
