package org.raku.comma.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import org.raku.comma.pod.PodDomBuildingContext;
import org.raku.comma.pod.PodDomEnumDeclarator;
import org.raku.comma.psi.*;
import org.raku.comma.psi.stub.RakuEnumStub;
import org.raku.comma.psi.stub.RakuEnumStubElementType;
import org.raku.comma.psi.symbols.RakuExplicitAliasedSymbol;
import org.raku.comma.psi.symbols.RakuSymbolCollector;
import org.raku.comma.psi.symbols.RakuSymbolKind;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.raku.comma.parsing.RakuElementTypes.STRING_LITERAL;
import static org.raku.comma.parsing.RakuTokenTypes.PAIR_KEY;

public class RakuEnumImpl extends RakuTypeStubBasedPsi<RakuEnumStub> implements RakuEnum {
    public RakuEnumImpl(@NotNull ASTNode node) {
        super(node);
    }

    public RakuEnumImpl(RakuEnumStub stub, RakuEnumStubElementType type) {
        super(stub, type);
    }

    @Override
    public String getEnumName() {
        return getName();
    }

    @Override
    public Collection<String> getEnumValues() {
        RakuEnumStub stub = getStub();
        if (stub != null) {
            return stub.getEnumValues();
        }

        List<String> values = new ArrayList<>();
        PsiElement literal = findChildByType(STRING_LITERAL);
        if (literal != null) {
            String text = literal.getText();
            if (text.length() < 3)
                return values;
            text = text.substring(1, text.length()-1);
            String[] result = text.split("\\s+");
            values.addAll(Arrays.stream(result).filter(s -> !s.isEmpty()).toList());
            return values;
        }
        PsiElement semilist = PsiTreeUtil.findChildOfType(this, RakuSemiList.class);
        if (semilist != null) {
            Collection<RakuFatArrow> keys = PsiTreeUtil.findChildrenOfType(semilist, RakuFatArrow.class);
            for (RakuFatArrow key : keys) {
                PsiElement child = key.getFirstChild();
                if (child != null && child.getNode().getElementType() == PAIR_KEY)
                    values.add(child.getText());
            }
        }
        return values;
    }

    @Override
    public void contributeLexicalSymbols(RakuSymbolCollector collector) {
        super.contributeLexicalSymbols(collector);
        if (collector.isSatisfied()) return;
        String enumName = getEnumName();
        for (String type : getEnumValues()) {
            collector.offerSymbol(new RakuExplicitAliasedSymbol(RakuSymbolKind.TypeOrConstant, this, type));
            if (collector.isSatisfied()) return;
            collector.offerSymbol(new RakuExplicitAliasedSymbol(RakuSymbolKind.TypeOrConstant, this, enumName + "::" + type));
            if (collector.isSatisfied()) return;
        }
    }

    public String toString() {
        return getClass().getSimpleName() + "(Raku:ENUM)";
    }

    @Override
    public void collectPodAndDocumentables(PodDomBuildingContext context) {
        String name = getName();
        if (name != null && !name.isEmpty()) {
            String[] parts = name.split("::");
            String globalName = context.prependGlobalNameParts(name);
            boolean isLexical = !getScope().equals("our");
            RakuTrait exportTrait = findTrait("is", "export");
            boolean visible = !isLexical && globalName != null || exportTrait != null;
            if (visible) {
                String shortName = parts[parts.length - 1];
                context.addType(new PodDomEnumDeclarator(getTextOffset(), shortName, globalName,
                        getDocBlocks(), exportTrait, getEnumValues()));
            }
        }
    }
}
