package org.raku.comma.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
import com.intellij.psi.meta.PsiMetaData;
import com.intellij.psi.meta.PsiMetaOwner;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.raku.comma.pod.PodDomAttributeDeclarator;
import org.raku.comma.pod.PodDomBuildingContext;
import org.raku.comma.pod.PodDomClassyDeclarator;
import org.raku.comma.psi.*;
import org.raku.comma.psi.symbols.*;
import org.raku.comma.psi.stub.RakuVariableDeclStub;
import org.raku.comma.psi.stub.RakuVariableDeclStubElementType;
import org.raku.comma.psi.type.RakuParametricType;
import org.raku.comma.psi.type.RakuResolvedType;
import org.raku.comma.psi.type.RakuType;
import org.raku.comma.psi.type.RakuUntyped;
import org.raku.comma.utils.RakuPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class RakuVariableDeclImpl extends RakuMemberStubBasedPsi<RakuVariableDeclStub>
  implements RakuVariableDecl, PsiMetaOwner {
    public RakuVariableDeclImpl(@NotNull ASTNode node) {
        super(node);
    }

    public RakuVariableDeclImpl(RakuVariableDeclStub stub, RakuVariableDeclStubElementType type) {
        super(stub, type);
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        RakuTermDefinition term = getTerm();
        if (term != null)
            return term;
        RakuVariable varNode = getVariable();
        return varNode != null ? varNode.getVariableToken() : null;
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        String varScope = getScope();
        if (varScope.equals("my") || varScope.equals("state")) {
            RakuStatementList parent = PsiTreeUtil.getParentOfType(this, RakuStatementList.class);
            if (parent != null)
                return new LocalSearchScope(parent);
        }
        return super.getUseScope();
    }

    @Override
    public String getName() {
        RakuVariableDeclStub stub = getStub();
        if (stub != null)
            return stub.getVariableNames()[0];
        PsiElement nameIdent = getNameIdentifier();
        return nameIdent != null ? nameIdent.getText() : null;
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        RakuVariable var = RakuElementFactory.createVariable(getProject(), name);
        RakuVariable variable = getVariable();
        if (variable != null)
            variable.replace(var);
        return this;
    }

    @Override
    public String[] getVariableNames() {
        RakuVariableDeclStub stub = getStub();
        if (stub != null)
            return stub.getVariableNames();
        RakuVariable[] variables = getVariables();
        if (variables.length == 0) {
            return ArrayUtil.EMPTY_STRING_ARRAY;
        }
        else {
            List<String> privates = new ArrayList<>();
            for (RakuVariable variable : variables) {
                String name = variable.getVariableName();
                if (name != null && RakuVariable.getTwigil(name) == '.') {
                    privates.add(name.replace('.', '!'));
                }
            }
            return Stream.concat(
                privates.stream(),
                Arrays.stream(variables).map(v -> v.getVariableName()).filter(n -> n != null)
            ).toArray(String[]::new);
        }
    }

    @Override
    public RakuVariable[] getVariables() {
        RakuSignature signature = PsiTreeUtil.getChildOfType(this, RakuSignature.class);
        if (signature == null) {
            RakuVariable variable = getVariable();
            return variable == null ? new RakuVariable[0] : new RakuVariable[]{variable};
        }
        else {
            return PsiTreeUtil.findChildrenOfType(signature, RakuVariable.class).toArray(new RakuVariable[0]);
        }
    }

    @Override
    public boolean hasInitializer() {
        return getAssignmentInfix() != null;
    }

    @Nullable
    @Override
    public PsiElement getInitializer(RakuVariable variable) {
        RakuInfix infix = getAssignmentInfix();
        if (infix == null) return null;

        PsiElement identificator = infix.getPrevSibling();
        while (identificator != null && !(identificator instanceof RakuVariable || identificator instanceof RakuSignature)) {
            identificator = identificator.getPrevSibling();
        }

        if (identificator instanceof RakuVariable) {
            return infix.skipWhitespacesForward();
        }
        else if (identificator != null) {
            PsiElement init = extractInitializerForSignatureVar((RakuSignature)identificator, variable, infix);
            if (init != null)
                return init;
        }
        return null;
    }

    @Nullable
    private static PsiElement extractInitializerForSignatureVar(RakuSignature signature,
                                                                RakuVariable variable,
                                                                @NotNull RakuInfix infix) {
        int initIndex = -1;
        RakuParameter[] parameters = signature.getParameters();
        for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
            RakuParameter parameter = parameters[i];
            RakuVariable parameterVariable = PsiTreeUtil.findChildOfType(parameter, RakuVariable.class);
            if (parameterVariable != null && Objects.equals(parameterVariable.getName(), variable.getName())) {
                initIndex = i;
                break;
            }
        }

        if (initIndex >= 0) {
            // We have an initializer index, try to find it in initializer list
            PsiElement multiInit = infix.skipWhitespacesForward();
            if (multiInit instanceof RakuInfixApplication) {
                RakuInfix[] commas = PsiTreeUtil.getChildrenOfType(multiInit, RakuInfix.class);
                if (commas != null && commas.length >= initIndex) {
                    return initIndex == 0 ?
                           commas[0].skipWhitespacesBackward() :
                           commas[initIndex - 1].skipWhitespacesForward();
                }
            }
        }
        return null;
    }

    @Nullable
    private RakuInfix getAssignmentInfix() {
        RakuInfix infix = PsiTreeUtil.getChildOfType(this, RakuInfix.class);
        if (infix == null)
            return null;
        if (!Objects.equals(infix.getOperator().getText(), "="))
            return null;
        return infix;
    }

    @Nullable
    @Override
    public PsiElement getInitializer() {
        RakuInfix assignmentInfix = getAssignmentInfix();
        return assignmentInfix == null ? null : assignmentInfix.getRightSide();
    }

    @Override
    public void removeVariable(RakuVariable variable) {
        PsiNamedElement[] variables = getDeclaredVariables();
        if (variables.length == 1) {
            RakuStatement statement = PsiTreeUtil.getParentOfType(this, RakuStatement.class);
            assert statement != null;
            PsiElement maybeWS = statement.getNextSibling();
            if (maybeWS instanceof PsiWhiteSpace)
                maybeWS.delete();
            statement.delete();
        }
        else {
            // Should we enclose resulting variable list with parentheses or no
            boolean shouldEnclose = variables.length - 1 != 1;

            StringJoiner signature = new StringJoiner(", ");
            String nameToAvoid = variable.getName();
            for (PsiNamedElement var : variables) {
                if (!Objects.equals(var.getName(), nameToAvoid))
                    signature.add(var.getName());
            }

            // Get part of multi-initializer that we have to exclude
            PsiElement deleteInit = getInitializer(variable);
            if (deleteInit == null)
                return;

            // Get all pieces, excluding deleteInit
            List<PsiElement> initPartsToPreserve = new ArrayList<>();
            if (hasInitializer()) {
                for (PsiElement initNode : Objects.requireNonNull(getInitializer()).getChildren()) {
                    if (!Objects.equals(initNode, deleteInit) && !(initNode instanceof RakuInfix))
                        initPartsToPreserve.add(initNode);
                }
            }
            // If we have only a single value left after exclusion, it is not InfixApplication anymore,
            // so we can just replace it with the value directly
            if (initPartsToPreserve.size() == 1) {
                getInitializer().replace(initPartsToPreserve.get(0));
            }
            else {
                // Otherwise, create a new application and use it
                RakuInfixApplication newApplication = RakuElementFactory.
                    createInfixApplication(getProject(), ", ", initPartsToPreserve);
                getInitializer().replace(newApplication);
            }

            PsiElement newDeclaration = RakuElementFactory.createVariableAssignment(
                variable.getProject(),
                String.format(shouldEnclose ? "(%s)" : "%s", signature),
                getInitializer().getText(), false);

            RakuStatement wholeStatement = PsiTreeUtil.getParentOfType(this, RakuStatement.class);
            if (wholeStatement != null)
                wholeStatement.replace(newDeclaration);
        }
    }

    private PsiNamedElement[] getDeclaredVariables() {
        Collection<PsiNamedElement> variables = PsiTreeUtil.findChildrenOfType(this, RakuParameterVariable.class);
        if (variables.size() == 0) {
            variables = PsiTreeUtil.findChildrenOfType(this, RakuVariable.class);
        }
        return variables.toArray(PsiNamedElement.EMPTY_ARRAY);
    }

    @Override
    public @NotNull RakuType inferType() {
        return inferType(true);
    }

    // Stub-safe type inference: identical to inferType(), except it skips the
    // `is`-trait-as-base-type resolution below, which requires cross-file
    // symbol resolution (RakuIsTraitReference.resolve(), e.g. for `has @!foo
    // is SomeCustomArrayClass;`). That resolution can query the stub index,
    // which is illegal from within stub building itself (the platform
    // detects and refuses this: "Stub building must not rely on data from
    // indexes..."). Used only by RakuVariableDeclStubElementType.createStub()
    // to compute the type string cached in the stub; real callers still get
    // the fully-resolved type via inferType() once the PSI is materialized.
    public @NotNull RakuType inferTypeForStub() {
        return inferType(false);
    }

    private @NotNull RakuType inferType(boolean resolveIsTraitBaseType) {
        RakuType baseType = calculateBaseType(resolveIsTraitBaseType);

        RakuTypeName typeName = PsiTreeUtil.getPrevSiblingOfType(this, RakuTypeName.class);
        RakuType type = typeName != null ? typeName.inferType() : getOfType();
        if (type != null) {
            return baseType == null
                   ? type
                   : new RakuParametricType(baseType, new RakuType[]{type});
        }

        if (baseType == null) {
            RakuType assignBasedType = resolveAssign();
            if (assignBasedType != null)
                return assignBasedType;
        }
        return baseType != null ? baseType : RakuUntyped.INSTANCE;
    }

    @Nullable
    private RakuType calculateBaseType(boolean resolveIsTraitBaseType) {
        // Find the variable, since we need to go on sigil.
        RakuVariable variable = PsiTreeUtil.getChildOfType(this, RakuVariable.class);
        if (variable == null)
            return null;

        // If we have an `is` trait with a type, and a % or @ sigil, that is the base type.
        char sigil = variable.getSigil();
        if (resolveIsTraitBaseType && (sigil == '@' || sigil == '%')) {
            for (RakuTrait trait : getTraits()) {
                if (!trait.getTraitModifier().equals("is"))
                    continue;
                RakuIsTraitName traitName = PsiTreeUtil.findChildOfType(trait, RakuIsTraitName.class);
                if (traitName == null)
                    continue;
                PsiReference reference = traitName.getReference();
                if (reference == null)
                    continue;
                PsiElement resolution = reference.resolve();
                if (resolution instanceof RakuPackageDecl)
                    return new RakuResolvedType(((RakuPackageDecl)resolution).getPackageName(), (RakuPsiElement)resolution);
            }
        }

        // Otherwise, fall back on sigil type.
        return variable.getTypeBySigil(variable.getText(), this);
    }

    @Nullable
    private RakuType getOfType() {
        // Visit of traits in reverse order, since `my @foo of Array of Array of Int' wants the
        // Int deepest in the structure.
        RakuType result = null;
        List<RakuTrait> traits = getTraits();
        for (int i = traits.size() - 1; i >= 0; i--) {
            RakuTrait trait = traits.get(i);
            if (!trait.getTraitModifier().equals("of"))
                continue;
            RakuTypeName typeName = trait.getCompositionTypeName();
            if (typeName == null)
                continue;
            result = result == null
                     ? typeName.inferType()
                     : new RakuParametricType(result, new RakuType[]{typeName.inferType()});
        }
        return result;
    }

    private RakuType resolveAssign() {
        PsiElement infix = PsiTreeUtil.getChildOfType(this, RakuInfixImpl.class);
        if (infix == null || !infix.getText().equals("=")) return null;
        PsiElement value = RakuPsiUtil.skipSpaces(infix.getNextSibling(), true);
        if (value instanceof RakuVariable) {
            String variableName = ((RakuVariable)value).getVariableName();
            if (Objects.equals(variableName, getVariableNames()[0])) {
                return null;
            }
        }
        else if (value instanceof RakuPostfixApplication) {
            if (value.getFirstChild() instanceof RakuVariable)
                if (Objects.equals(((RakuVariable)value.getFirstChild()).getVariableName(), getVariableNames()[0])) {
                    return null;
                }
        }
        return value instanceof RakuPsiElement ? ((RakuPsiElement)value).inferType() : null;
    }

    @Override
    public String defaultScope() {
        return "my";
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(Raku:VARIABLE_DECLARATION)";
    }

    @Override
    public void contributeLexicalSymbols(RakuSymbolCollector collector) {
        if (!getScope().equals("has")) {
            String name = getName();
            if (name != null && name.length() > 1) {
                RakuTermDefinition defterm = getTerm();
                if (defterm != null) {
                    collector.offerSymbol(new RakuExplicitSymbol(RakuSymbolKind.TypeOrConstant, this));
                }
                else {
                    collector.offerSymbol(new RakuExplicitSymbol(RakuSymbolKind.Variable, this));
                    if (collector.isSatisfied()) return;
                    if (name.startsWith("&"))
                        collector.offerSymbol(new RakuExplicitAliasedSymbol(RakuSymbolKind.Routine,
                                                                            this, name.substring(1)));
                }
            }
        }
    }

    @Override
    public void contributeMOPSymbols(RakuSymbolCollector collector, MOPSymbolsAllowed symbolsAllowed) {
        if (!getScope().equals("has"))
            return;

        RakuVariable[] attributes = getVariables();

        for (RakuVariable variable : attributes) {
            String name = variable.getVariableName();
            if (name == null || name.length() <= 2)
                continue;

            if (RakuVariable.getTwigil(name) == '!' && symbolsAllowed.privateAttributesVisible) {
                collector.offerSymbol(new RakuExplicitAliasedSymbol(RakuSymbolKind.Variable, variable, name));
            }
            else if (RakuVariable.getTwigil(name) == '.') {
                collector.offerSymbol(new RakuExplicitAliasedSymbol(RakuSymbolKind.Variable, this, name));
                if (collector.isSatisfied()) return;
                if (symbolsAllowed.privateAttributesVisible) {
                    collector.offerSymbol(new RakuExplicitAliasedSymbol(RakuSymbolKind.Variable,
                                                                        this, name.charAt(0) + "!" + name.substring(2)));
                    if (collector.isSatisfied()) return;
                }
                collector.offerSymbol(new RakuExplicitAliasedSymbol( // Offer self.foo;
                                                                     RakuSymbolKind.Method, this, '.' + name.substring(2)));
            }
        }
    }

    @Nullable
    @Override
    public PsiMetaData getMetaData() {
        PsiElement decl = this;
        String desigilname = getName();
        if (getTerm() == null) {
            // Chop off sigil, if it's not sigil-only name
            if (desigilname != null && desigilname.length() > 1)
                desigilname = desigilname.substring(1);
            // Chop off twigil if any
            if (desigilname != null && desigilname.length() >= 2 && !Character.isLetter(desigilname.charAt(0)))
                desigilname = desigilname.substring(1);
        }
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

    private RakuVariable getVariable() {
        if (findChildByClass(RakuSignature.class) != null)
            return null;
        return findChildByClass(RakuVariable.class);
    }

    private RakuTermDefinition getTerm() {
        return findChildByClass(RakuTermDefinition.class);
    }

    @Override
    public void collectPodAndDocumentables(PodDomBuildingContext context) {
        // Only document attributes.
        String scope = getScope();
        if (!(scope.equals("has") || scope.equals("HAS")))
            return;

        // Make sure there's an enclosing class, and add it.
        PodDomClassyDeclarator enclosingClass = context.currentClassyDeclarator();
        if (enclosingClass == null)
            return;
        for (String name : getVariableNames()) {
            if (RakuVariable.getTwigil(name) != '.')
                continue;
            String shortName = name.substring(2);
            boolean rw = findTrait("is", "rw") != null;
            RakuType type = inferType();
            String typename = null;
            if (!(type instanceof RakuUntyped))
                typename = type.getName();
            PodDomAttributeDeclarator attribute = new PodDomAttributeDeclarator(getTextOffset(), shortName, null,
                                                                                getDocBlocks(), rw, typename);
            enclosingClass.addAttribute(attribute);
        }
    }
}
