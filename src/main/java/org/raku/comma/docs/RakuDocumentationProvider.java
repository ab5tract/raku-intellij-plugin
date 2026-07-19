package org.raku.comma.docs;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.raku.comma.psi.*;
import org.raku.comma.psi.external.ExternalRakuFile;
import org.raku.comma.psi.external.ExternalRakuPackageDecl;
import org.raku.comma.psi.external.RakuExternalPsiElement;
import org.raku.comma.services.project.ProjectSdkSymbolCache;
import org.raku.comma.utils.RakuUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RakuDocumentationProvider implements DocumentationProvider {
    @Nullable
    @Override
    public synchronized String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        if (element instanceof RakuConstant) {
            if (element.getText().length() < 50)
                return RakuUtils.escapeHTML(element.getText());
            return RakuUtils.escapeHTML("constant " + ((RakuConstant)element).getConstantName() + " = ...");
        } else if (element instanceof RakuEnum enumEl) {
            return RakuUtils.escapeHTML("enum " + enumEl.getEnumName() + " (" + String.join(", ", enumEl.getEnumValues()) + ")");
        } else if (element instanceof RakuPackageDecl decl) {
            Optional<String> traits = decl.getTraits().stream().map(t -> t.getTraitModifier() + " " + t.getTraitName()).reduce((s1, s2) -> s1 + " " + s2);
            return RakuUtils.escapeHTML(String.format("%s %s%s", decl.getPackageKind(), decl.getPackageName(),
                                                      traits.map(s -> " " + s).orElse("")));
        } else if (element instanceof RakuParameterVariable decl) {
            RakuParameter parameter = PsiTreeUtil.getParentOfType(decl, RakuParameter.class);
            if (parameter == null) return null;
            return RakuUtils.escapeHTML(parameter.getText());
        } else if (element instanceof RakuRegexDecl decl) {
            String text = element.getText();
            if (text.length() < 50)
                return text;
            return RakuUtils.escapeHTML(String.format("%s %s { ... }", decl.getRegexKind(), decl.getRegexName()));
        } else if (element instanceof RakuRoutineDecl decl) {
            String signature;
            if (decl instanceof RakuExternalPsiElement)
                signature = "(" + decl.summarySignature() + ")";
            else {
                RakuSignature node = decl.getSignatureNode();
                signature = node != null ? node.getText() : "()";
            }
            return RakuUtils.escapeHTML(String.format("%s %s%s", decl.getRoutineKind(), decl.getRoutineName(), signature));
        } else if (element instanceof RakuSubset) {
            return RakuUtils.escapeHTML("subset " + ((RakuSubset)element).getSubsetName() + " of " + ((RakuSubset)element).getSubsetBaseTypeName());
        } else if (element instanceof RakuVariableDecl decl) {
            String name = String.join(", ", decl.getVariableNames());
            String scope = decl.getScope();
            PsiElement initializer = decl.getInitializer();
            RakuPackageDecl selfType = scope.equals("has") ? PsiTreeUtil.getParentOfType(decl, RakuPackageDecl.class) : null;
            return RakuUtils.escapeHTML(String.format("%s %s%s%s",
                                                      scope, name, initializer == null ? "" : " = " + initializer.getText(),
                                 selfType == null ? "" : " (attribute of " + selfType.getPackageName() + ")"));
        }
        return null;
    }

    @Nullable
    @Override
    public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
        if (element instanceof RakuExternalPsiElement) {
            String name = ((RakuExternalPsiElement)element).getName();
            PsiElement parent = element.getParent();
            if (parent instanceof ExternalRakuPackageDecl)
                parent = parent.getParent();
            if (parent instanceof ExternalRakuFile &&
                Objects.equals(((ExternalRakuFile)parent).getName(), ProjectSdkSymbolCache.SETTING_FILE_NAME)) {
                if (element instanceof RakuPackageDecl)
                    return Collections.singletonList("https://docs.raku.org/type/" + name);
                else if (element instanceof RakuRoutineDecl)
                    return Collections.singletonList("https://docs.raku.org/routine/" + name);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public synchronized String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (element instanceof RakuMethodCall || element instanceof RakuSubCall) {
            PsiReference ref;
            if (element instanceof RakuMethodCall) {
                ref = element.getReference();
            } else {
                RakuSubCallName callName = PsiTreeUtil.findChildOfType(element, RakuSubCallName.class);
                if (callName == null) return null;
                ref = callName.getReference();
            }
            if (!(ref instanceof PsiReferenceBase.Poly))
                return null;
            ResolveResult[] resolves = ((PsiReferenceBase.Poly<?>)ref).multiResolve(false);
            for (ResolveResult result : resolves) {
                PsiElement declaration = result.getElement();
                if (declaration instanceof RakuRoutineDecl) {
                    String docs = ((RakuRoutineDecl)declaration).getDocsString();
                    if (docs != null)
                        return docs;
                }
            }
        }

        if (element instanceof RakuDocumented) {
            String docsString = ((RakuDocumented)element).getDocsString();
            return docsString == null || docsString.isEmpty() || docsString.equals("<br>")
                   ? null
                   : docsString;
        }
        return null;
    }

    @Nullable
    @Override
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
        return null;
    }

    @Nullable
    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
        return null;
    }
}
