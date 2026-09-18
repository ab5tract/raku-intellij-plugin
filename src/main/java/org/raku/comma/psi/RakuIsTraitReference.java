package org.raku.comma.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.raku.comma.psi.symbols.RakuSymbol;
import org.raku.comma.psi.symbols.RakuSymbolKind;
import org.raku.comma.psi.symbols.RakuVariantsSymbolCollector;
import org.raku.comma.psi.type.RakuType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class RakuIsTraitReference extends PsiReferenceBase<RakuPsiElement> {
    static final String[] ROUTINE_DEFAULT_TRAITS = new String[]{
        "assoc", "tighter", "looser", "equiv", "default",
        "export", "rw", "raw", "nodal", "pure"
    };
    static final String[] VARIABLE_DEFAULT_TRAITS = new String[]{
        "default", "required", "DEPRECATED", "rw"
    };
    static final String[] PARAMETER_DEFAULT_TRAITS = new String[]{
        "copy", "raw", "rw", "readonly", "required"
    };
    static final String[] REGEX_DEFAULT_TRAITS = new String[]{
        "DEPRECATED", "export"
    };

    public RakuIsTraitReference(RakuPsiElement element) {
        super(element, new TextRange(0, element.getTextLength()));
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        RakuPsiElement ref = getElement();
        String typeName = ref.getText();
        PsiElement lexical = null;
        RakuSymbol result = ref.resolveLexicalSymbol(RakuSymbolKind.TypeOrConstant, typeName);
        if (result != null) {
            PsiElement psi = result.getPsi();
            if (psi != null) {
                // It's fine if it's either imported or declared ahead of the point
                // it is being referenced.
                if (psi.getContainingFile() != ref.getContainingFile())
                    lexical = psi;
                else if (psi.getTextOffset() < ref.getTextOffset())
                    lexical = psi;
            }
        }
        // In Rakudo core, an in-project declaration beats a fileless
        // SDK-setting symbol (or nothing) -- see RakudoCoreTypeResolution.
        return RakudoCoreTypeResolution.preferProjectGlobalType(ref, typeName, lexical);
    }

    @Override
    public Object @NotNull [] getVariants() {
        RakuTrait trait = PsiTreeUtil.getParentOfType(getElement(), RakuTrait.class);
        if (trait == null) return ArrayUtil.EMPTY_OBJECT_ARRAY;
        PsiElement owner = trait.getParent();
        if (owner == null) return ArrayUtil.EMPTY_OBJECT_ARRAY;

        if (owner instanceof RakuParameter) {
            return PARAMETER_DEFAULT_TRAITS;
        }
        else if (owner instanceof RakuVariableDecl) {
            List<Object> types = new ArrayList<>(Arrays.asList(new RakuTypeNameReference(myElement).getVariants()));
            types.addAll(Arrays.asList(VARIABLE_DEFAULT_TRAITS));
            if (!((RakuVariableDecl)owner).getScope().equals("my"))
                types.add("export");
            if (((RakuVariableDecl)owner).getScope().equals("has"))
                gatherExternalTraits(types, "Attribute");
            return types.toArray();
        }
        else if (owner instanceof RakuRoutineDecl) {
            List<Object> options = new ArrayList<>();
            gatherExternalTraits(options, "Routine");
            return Stream.concat(options.stream(), Arrays.stream(ROUTINE_DEFAULT_TRAITS)).toArray();
        }
        else if (owner instanceof RakuRegexDecl) {
            List<Object> traits = new ArrayList<>(Arrays.asList(REGEX_DEFAULT_TRAITS));
            traits.addAll(Arrays.asList(ROUTINE_DEFAULT_TRAITS));
            return traits.toArray();
        }
        else if (owner instanceof RakuPackageDecl) {
            List<Object> types = new ArrayList<>(Arrays.asList(new RakuTypeNameReference(myElement).getVariants()));
            types.add("export");
            return types.toArray();
        }
        else {
            return ArrayUtil.EMPTY_OBJECT_ARRAY;
        }
    }

    private void gatherExternalTraits(List<Object> types, String traitType) {
        RakuVariantsSymbolCollector subsCollector = new RakuVariantsSymbolCollector(RakuSymbolKind.Routine);
        myElement.applyLexicalSymbolCollector(subsCollector);
        myElement.applyExternalSymbolCollector(subsCollector);
        for (RakuSymbol symbol : subsCollector.getVariants()) {
            if (!symbol.getName().equals("trait_mod:<is>"))
                continue;
            RakuRoutineDecl decl = (RakuRoutineDecl)symbol.getPsi();
            RakuParameter[] params = decl.getParams();
            if (params.length != 2)
                continue;
            RakuType caller = params[0].inferType();
            if (caller.getName().equals(traitType)) {
                String[] traitNames = params[1].getVariableNames();
                for (String traitName : traitNames) {
                    if (traitName.length() > 1)
                        types.add(traitName.substring(1));
                }
            }
        }
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        RakuIsTraitName isTraitName = (RakuIsTraitName)getElement();
        return isTraitName.setName(newElementName);
    }
}
