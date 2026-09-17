package org.raku.comma.psi;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.util.IncorrectOperationException;
import org.raku.comma.psi.impl.RakuSubCallNameImpl;
import org.raku.comma.psi.symbols.RakuSymbol;
import org.raku.comma.psi.symbols.RakuSymbolKind;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class RakuSubCallReference extends PsiReferenceBase.Poly<RakuSubCallName> {
    private final boolean maybeCoercion;

    public RakuSubCallReference(RakuSubCallNameImpl call, boolean maybeCoercion) {
        super(call, new TextRange(0, call.getTextLength()), false);
        this.maybeCoercion = maybeCoercion;
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        RakuSubCallName call = getElement();
        String name = call.getCallName();

        List<RakuSymbol> symbols = call.resolveLexicalSymbolAllowingMulti(RakuSymbolKind.Routine, name);
        if (! symbols.isEmpty()) {
            return symbols.stream()
                          .map(RakuSymbol::getPsi)
                          .filter(Objects::nonNull)
                          .map(PsiElementResolveResult::new)
                          .toArray(ResolveResult[]::new);
        }

        // A coercion-shaped call (`Foo(...)`), but also any bare argument-less
        // name, can be a type or constant term: `stop.HOW.curry(...)` on a
        // lexical `my role stop[...]` is a type object, not a sub call (a
        // type term could not take listop arguments anyway, so this cannot
        // shadow a real call).
        if (maybeCoercion || callHasNoArguments(call)) {
            RakuSymbol type = call.resolveLexicalSymbol(RakuSymbolKind.TypeOrConstant, name);
            if (type != null && type.getPsi() != null) {
                return new ResolveResult[]{ new PsiElementResolveResult(type.getPsi()) };
            }
        }

        return ResolveResult.EMPTY_ARRAY;
    }

    @Override
    public Object @NotNull [] getVariants() {
        return getElement().getLexicalSymbolVariants(RakuSymbolKind.Routine, RakuSymbolKind.TypeOrConstant)
                           .stream()
                           .map(sym -> {
                               PsiElement psi = sym.getPsi();
                               if (psi instanceof RakuRoutineDecl) {
                                   return strikeoutDeprecated(LookupElementBuilder.create(psi, sym.getName())
                                                                                  .withTypeText(((RakuRoutineDecl) psi).summarySignature()), psi);
                               } else {
                                   return sym.getName();
                               }
                           }).toArray();
    }

    private static boolean callHasNoArguments(RakuSubCallName call) {
        PsiElement parent = call.getParent();
        return parent instanceof RakuSubCall subCall && subCall.getCallArguments().length == 0;
    }

    private static LookupElementBuilder strikeoutDeprecated(LookupElementBuilder item, PsiElement psi) {
        return psi instanceof RakuDeprecatable && ((RakuDeprecatable) psi).isDeprecated()
               ? item.strikeout()
               : item;
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        PsiElement call = myElement.getParent();
        if (call instanceof RakuSubCall subCall) {
            return subCall.setName(newElementName);
        }
        throw new IncorrectOperationException();
    }
}
