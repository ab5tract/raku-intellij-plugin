package org.raku.comma.highlighter;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInsight.daemon.impl.*;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.openapi.extensions.InternalIgnoreDependencyViolation;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.raku.comma.psi.*;
import org.raku.comma.psi.symbols.RakuLexicalSymbolContributor;
import org.jetbrains.annotations.NotNull;
import org.raku.comma.utils.CommaProjectUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@InternalIgnoreDependencyViolation
public class RakuHighlightVisitor extends RakuElementVisitor implements HighlightVisitor {
    private HighlightInfoHolder myHolder;
    private final Map<String, List<RakuPsiElement>> ourScopedPackagesPool = new HashMap<>();
    private PsiFile myFile;

    @Override
    public boolean suitableForFile(@NotNull PsiFile file) {
        return file instanceof RakuFile;
    }

    @Override
    public void visit(@NotNull PsiElement element) {
        element.accept(this);
    }

    @Override
    public boolean analyze(@NotNull PsiFile file,
                           boolean updateWholeFile,
                           @NotNull HighlightInfoHolder holder,
                           @NotNull Runnable highlight)
    {
        try {
            myHolder = holder;
            myFile = file;
            if (updateWholeFile) {
                ProgressIndicator progress = ProgressManager.getInstance().getProgressIndicator();
                if (progress == null) throw new IllegalStateException("Must be run under progress");
                highlight.run();
                ProgressManager.checkCanceled();
            } else {
                highlight.run();
            }
        } finally {
            myHolder = null;
            ourScopedPackagesPool.clear();
        }

        return true;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public @NotNull HighlightVisitor clone() {
        return new RakuHighlightVisitor();
    }

    @Override
    public void visitScope(RakuPsiScope element) {
        Map<String, List<RakuPsiElement>> duplicateClassesPool = new HashMap<>();
        Map<String, List<RakuSignatureHolder>> duplicateRoutinesPool = new HashMap<>();
        Map<String, List<RakuPsiElement>> duplicateVariablesPool = new HashMap<>();
        List<RakuLexicalSymbolContributor> decls = element.getSymbolContributors();
        for (RakuLexicalSymbolContributor contributor : decls) {
            if (contributor instanceof RakuUseStatement) {
                visitUseStatement(duplicateClassesPool, duplicateRoutinesPool, contributor);
            }

            if (!(contributor instanceof RakuPsiDeclaration decl)) continue;

            switch (decl) {
                case RakuRoutineDecl rakuRoutineDecl -> {
                    if (rakuRoutineDecl.getRoutineName() == null || decl.getNameIdentifier() == null) {
                        return;
                    }
                    boolean wasReported = visitSignatureHolder(myHolder,
                                                               (RakuSignatureHolder) decl,
                                                               rakuRoutineDecl.getRoutineName(),
                                                               duplicateRoutinesPool);
                    if (!wasReported && rakuRoutineDecl.isSub() && Objects.equals(rakuRoutineDecl.getMultiness(), "only")) {
                        // If a subroutine, expose `&foo` variable
                        TextRange textRange = new TextRange(((RakuRoutineDecl) decl).getDeclaratorNode()
                                                                                    .getTextOffset(),
                                                            decl.getNameIdentifier().getTextRange().getEndOffset());
                        visitVariableDecl(
                                duplicateVariablesPool, new RakuPsiElement[]{decl}, new String[]{"&" + ((RakuRoutineDecl) decl).getRoutineName()},
                                new TextRange[]{textRange});
                    }
                }
                case RakuRegexDecl rakuRegexDecl -> {
                    if (decl.getName() == null || decl.getNameIdentifier() == null) return;
                    visitSignatureHolder(myHolder, (RakuSignatureHolder) decl, decl.getName(), duplicateRoutinesPool);
                }
                case RakuVariableSource rakuVariableSource -> {
                    if (decl instanceof RakuParameter) {
                        if (PsiTreeUtil.getParentOfType(decl, RakuVariableDecl.class, RakuRoutineDecl.class) instanceof RakuVariableDecl) {
                            continue;
                        }
                        if (PsiTreeUtil.getParentOfType(decl, RakuPointyBlock.class, RakuRoutineDecl.class) instanceof RakuPointyBlock) {
                            continue;
                        }
                    }
                    visitVariableDecl(
                            duplicateVariablesPool, rakuVariableSource.getVariables(),
                            rakuVariableSource.getVariableNames(),
                            ContainerUtil.map2Array(rakuVariableSource.getVariables(), TextRange.class, PsiElement::getTextRange)
                    );
                }
                case RakuPackageDecl rakuPackageDecl -> {
                    if (decl.getGlobalName() != null && !rakuPackageDecl.isStubbed()) {
                        boolean marked = false;
                        if (decl.getScope().equals("our")) {
                            marked = visitPackageDecl(decl, ourScopedPackagesPool, true);
                        }
                        visitPackageDecl(decl, duplicateClassesPool, !marked);
                    }
                }
                case RakuSubset rakuSubset -> {
                    if (decl.getGlobalName() != null) {
                        boolean marked = false;
                        if (decl.getScope().equals("our")) {
                            marked = visitPackageDecl(decl, ourScopedPackagesPool, true);
                        }
                        visitPackageDecl(decl, duplicateClassesPool, !marked);
                    }
                }
                default -> {
                }
            }
        }
    }

    private void visitUseStatement(Map<String,
                                   List<RakuPsiElement>> packagesPool,
                                   Map<String,
                                   List<RakuSignatureHolder>> routinesPool,
                                   RakuLexicalSymbolContributor contributor)
    {

// TODO: Visiting `use` statements has been broken forever. Exported routines have never worked. Yet this routine is
// there, always spinning and doing too much work for an operation on EDT.
//        RakuVariantsSymbolCollector collector = new RakuVariantsSymbolCollector(RakuSymbolKind.TypeOrConstant,
//                                                                                RakuSymbolKind.Routine);
//        contributor.contributeLexicalSymbols(collector);
//        for (RakuSymbol symbol : collector.getVariants()) {
//            PsiElement psi = symbol.getPsi();
//            if (psi instanceof RakuPackageDecl && ((RakuPackageDecl) psi).getGlobalName() != null) {
//                visitPackageDecl((RakuPackageDecl) psi, packagesPool, false);
//            } else if (psi instanceof RakuRoutineDecl) {
//                visitSignatureHolder(myHolder,
//                                     (RakuSignatureHolder) psi,
//                                     ((RakuRoutineDecl) psi).getRoutineName(),
//                                     routinesPool);
//            }
//        }
    }

    private boolean visitPackageDecl(RakuPsiElement decl,
                                     Map<String, List<RakuPsiElement>> pool,
                                     boolean shouldMark)
    {
        List<RakuPsiElement> value = pool.compute(((RakuPsiDeclaration) decl).getGlobalName(), (k, v) -> {
            if (v == null) {
                v = new ArrayList<>();
            }
            v.add(decl);
            return v;
        });
        if (shouldMark && value.size() > 1) {
            return markDuplicateValue(decl, value);
        }
        return false;
    }

    private boolean markDuplicateValue(RakuPsiElement decl, List<RakuPsiElement> v) {
        // Elements living in mutually exclusive #?if branches can never both
        // exist in one build, so they are not candidates for a duplicate
        // report against `decl` (or against each other).
        List<RakuPsiElement> coexisting = v.stream()
                                           .filter(other -> other == decl
                                                   || !RakuConditionalBranches.INSTANCE.inMutuallyExclusiveBranches(other, decl))
                                           .collect(Collectors.toList());
        if (coexisting.size() < 2) return false;

        AtomicBoolean marked = new AtomicBoolean(false);
        Optional<RakuPsiElement> maxRedecl = coexisting.stream()
                                              .filter(d -> d.getContainingFile()
                                                            .isEquivalentTo(decl.getContainingFile()))
                                              .max(Comparator.comparingInt(PsiElement::getTextOffset));
        if (maxRedecl.isEmpty()) {
            maxRedecl = coexisting.stream().max(Comparator.comparingInt(PsiElement::getTextOffset));
        }

        maxRedecl.ifPresent(redecl -> {
            if (redecl instanceof RakuPackageDecl
                    && (((RakuPackageDecl) redecl).getPackageKeywordNode() == null
                    || ((RakuPackageDecl) redecl).getNameIdentifier() == null)
                    || redecl instanceof RakuSubset
                    && ((RakuSubset) redecl).getNameIdentifier() == null)
            {
                return;
            }
            TextRange range;
            //noinspection ConstantConditions
            range = new TextRange(redecl.getTextOffset(), ((RakuPsiDeclaration) redecl).getNameIdentifier()
                                                                                       .getTextRange()
                                                                                       .getEndOffset());
            if (redecl instanceof RakuPackageDecl && ((RakuPackageDecl) redecl).getPackageKind().equals("role")) {
                RakuParameter[] newParams = decl instanceof RakuPackageDecl
                                            ? ((RakuPackageDecl) decl).getSignature()
                                            : new RakuParameter[0];
                for (RakuPsiElement role : coexisting) {
                    if (role instanceof RakuPackageDecl) {
                        RakuParameter[] oldParams = ((RakuPackageDecl) role).getSignature();
                        // Different numbers of args
                        if (newParams.length == oldParams.length && role.getTextOffset() != decl.getTextOffset()
                                && !RakuConditionalBranches.INSTANCE.inMutuallyExclusiveBranches(role, redecl)) {
                            marked.set(true);
                            myHolder.add(getDuplicateHighlightInfo(role,
                                                                   redecl,
                                                                   range,
                                                                   ((RakuPackageDecl) redecl).getGlobalName(),
                                                                   HighlightInfoType.ERROR));
                            break;
                        }
                    }
                }
            } else {
                Optional<RakuPsiElement> minRedecl = coexisting.stream()
                                                      .filter(d -> !d.getContainingFile()
                                                                     .isEquivalentTo(decl.getContainingFile()))
                                                      .min(Comparator.comparingInt(PsiElement::getTextOffset));
                if (minRedecl.isEmpty()) {
                    minRedecl = coexisting.stream().min(Comparator.comparingInt(PsiElement::getTextOffset));
                }
                minRedecl.ifPresent(packageDecl -> {
                    if (RakuConditionalBranches.INSTANCE.inMutuallyExclusiveBranches(packageDecl, redecl)) return;
                    marked.set(true);
                    myHolder.add(getDuplicateHighlightInfo(packageDecl,
                                                           redecl,
                                                           range,
                                                           ((RakuPsiDeclaration) packageDecl).getGlobalName(),
                                                           HighlightInfoType.ERROR));
                });
            }
        });
        return marked.get();
    }

    protected boolean visitSignatureHolder(HighlightInfoHolder infoHolder,
                                           RakuSignatureHolder holder,
                                           String name, Map<String,
            List<RakuSignatureHolder>> pool)
    {
        if (name == null) return false;

        return pool.compute(name, (k, v) -> {
            if (v == null) {
                v = new ArrayList<>();
            } else {
                Pair<RakuSignatureHolder, RakuSignatureHolder> oldAndNewHolders = isElementInPool(holder, v);
                if (oldAndNewHolders != null) {
                    TextRange textRange = null;
                    if (oldAndNewHolders.second instanceof RakuRoutineDecl decl) {
                        if (decl.getNameIdentifier() != null) {
                            PsiElement declaratorNode = decl.getDeclaratorNode();
                            if (declaratorNode != null) {
                                textRange = new TextRange(declaratorNode.getTextOffset(),
                                                          decl.getNameIdentifier().getTextRange().getEndOffset());
                            } else if (decl.getParent() instanceof RakuMultiDecl) {
                                textRange = new TextRange(decl.getTextOffset(),
                                                          decl.getNameIdentifier().getTextRange().getEndOffset());
                            }
                        }
                    } else if (oldAndNewHolders.second instanceof RakuRegexDecl decl) {
                        if (decl.getNameIdentifier() != null) {
                            textRange = new TextRange(decl.getTextOffset(),
                                                      decl.getNameIdentifier().getTextRange().getEndOffset());
                        }
                    }
                    if (textRange != null
                            && !RakuConditionalBranches.INSTANCE.inMutuallyExclusiveBranches(
                                    (PsiElement) oldAndNewHolders.first, (PsiElement) oldAndNewHolders.second))
                    {
                        infoHolder.add(getDuplicateHighlightInfo((RakuPsiElement) oldAndNewHolders.first,
                                                                 (PsiElement) holder,
                                                                 textRange,
                                                                 name,
                                                                 HighlightInfoType.ERROR));
                    }
                }
            }
            v.add(holder);
            return v;
        }).size() > 1;
    }

    private static Pair<RakuSignatureHolder, RakuSignatureHolder> isElementInPool(RakuSignatureHolder holder, List<RakuSignatureHolder> v) {
        String scope = holder instanceof RakuPsiDeclaration
                       ? ((RakuPsiDeclaration) holder).getScope()
                       : "";

        // For multi routines we need to compare signatures
        RakuParameter[] paramsNew = holder.getSignatureNode() == null
                                    ? new RakuParameter[0]
                                    : holder.getSignatureNode().getParameters();
        RakuPsiScope newScope = PsiTreeUtil.getParentOfType((PsiElement) holder, RakuPsiScope.class);
        List<RakuSignatureHolder> matchingDecls = new ArrayList<>();
        ROUTINES:
        for (RakuSignatureHolder checkedDecl : v) {
            if (holder == checkedDecl) continue;

            String checkedDeclScope = checkedDecl instanceof RakuPsiDeclaration
                                      ? ((RakuPsiDeclaration) checkedDecl).getScope()
                                      : "";

            if (!Objects.equals(checkedDeclScope, scope)) continue;

            RakuPsiScope oldScope = PsiTreeUtil.getParentOfType((PsiElement) checkedDecl, RakuPsiScope.class);
            if (newScope != null
                    && oldScope != null
                    && !(newScope instanceof RakuFile)
                    && !(oldScope instanceof RakuFile)
                    && !PsiEquivalenceUtil.areElementsEquivalent(newScope, oldScope))
            {
                continue;
            }

            if (!Objects.equals(holder.getMultiness(), checkedDecl.getMultiness())) continue;
            if (Objects.equals(checkedDecl.getMultiness(), "only")) {
                matchingDecls.add(checkedDecl);
                continue;
            }
            RakuParameter[] paramsKnown = checkedDecl.getSignatureNode() == null
                                          ? new RakuParameter[0]
                                          : checkedDecl.getSignatureNode().getParameters();
            // No params == both are duplicates
            if (paramsKnown.length == 0 && paramsNew.length == 0) {
                matchingDecls.add(checkedDecl);
                continue;
            }
            // Different signature length - not a duplicate
            if (paramsKnown.length != paramsNew.length) {
                continue;
            }
            // Compare by type as precise as we can for now
            for (int j = 0; j < paramsKnown.length; j++) {
                RakuParameter parameter1 = paramsKnown[j];
                RakuParameter parameter2 = paramsNew[j];
                if (!parameter1.equalsParameter(parameter2)) {
                    continue ROUTINES;
                }
            }
            matchingDecls.add(checkedDecl);
            break;
        }

        if (!matchingDecls.isEmpty()) {
            matchingDecls.add(holder);
            Optional<PsiElement> maybeMin = matchingDecls.stream()
                                                         .filter(d -> d instanceof PsiElement)
                                                         .map(d -> (PsiElement) d)
                                                         .min(Comparator.comparingInt(PsiElement::getTextOffset));

            Optional<PsiElement> maybeMax = matchingDecls.stream()
                                                         .filter(d -> d instanceof PsiElement)
                                                         .map(d -> (PsiElement) d)
                                                         .max(Comparator.comparingInt(PsiElement::getTextOffset));

            if (maybeMin.isPresent() && maybeMax.isPresent()) {
                return Pair.create((RakuSignatureHolder) maybeMin.get(), (RakuSignatureHolder) maybeMax.get());
            }
        }
        return null;
    }

    public void visitVariableDecl(Map<String,
            List<RakuPsiElement>> pool,
                                  RakuPsiElement[] variables,
                                  String[] variableNames,
                                  TextRange[] ranges)
    {
        for (int i = 0; i < variables.length; i++) {
            String varName = variableNames[i];
            // Don't visit anonymous ones or dynamic
            if (varName == null || varName.length() < 2 || varName.contains("*")) {
                continue;
            }
            int finalI = i;
            pool.compute(varName, (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                } else {
                    List<RakuPsiElement> coexisting = v.stream()
                            .filter(prior -> !RakuConditionalBranches.INSTANCE.inMutuallyExclusiveBranches(prior, variables[finalI]))
                            .collect(Collectors.toList());
                    if (!coexisting.isEmpty()) {
                        TextRange finalRange = ranges[finalI];
                        RakuPsiElement originalDecl = variables[finalI];
                        for (RakuPsiElement decl : coexisting) {
                            if (!PsiEquivalenceUtil.areElementsEquivalent(decl.getContainingFile(), variables[finalI].getContainingFile())) {
                                finalRange = decl.getTextRange();
                                originalDecl = decl;
                                break;
                            } else {
                                if (finalRange.getEndOffset() < decl.getTextRange().getEndOffset()) {
                                    finalRange = decl.getTextRange();
                                }
                                if (originalDecl.getTextOffset() > decl.getTextOffset()) {
                                    originalDecl = decl;
                                }
                            }
                        }
                        myHolder.add(getDuplicateHighlightInfo(originalDecl,
                                                               variables[finalI],
                                                               finalRange,
                                                               varName,
                                                               varName.contains("!")
                                                               ? HighlightInfoType.ERROR
                                                               : HighlightInfoType.WARNING));
                    }
                }
                v.add(variables[finalI]);
                return v;
            });
        }
    }

    private HighlightInfo getDuplicateHighlightInfo(RakuPsiElement originalDecl,
                                                    PsiElement currentDecl,
                                                    TextRange range,
                                                    String name,
                                                    HighlightInfoType infoType)
    {
        if (!originalDecl.isValid()) return null;
        if (!currentDecl.getContainingFile().equals(myFile)) return null;

        if (checkForMultiDuplicate(originalDecl, currentDecl)) return null;

        // TODO: All other usages of this check have been in Annotations, which are straightforward to
        // migrate over to Inspections.. This is much less so. However, the cost should only be incurred
        // when there are actual duplicates seen. Thus this check *should* be far less costly than
        // the others that have been migrated. Still, duplicate detection would ideally be an inspection,
        // rather than a side effect of the highlight visitor
        if (CommaProjectUtil.isRakudoCoreProject(originalDecl.getProject())) return null;

        PsiFile containingFile = originalDecl.getContainingFile();
        String previousPos = "%s:%s".formatted(containingFile.getName(),
                                               StringUtil.offsetToLineNumber(containingFile.getText(), originalDecl.getTextOffset()) + 1);

        return HighlightInfo.newHighlightInfo(infoType)
                            .range(range)
                            .descriptionAndTooltip(String.format("Re-declaration of %s from %s", name, previousPos))
                            .create();
    }

    private boolean checkForMultiDuplicate(PsiElement original, PsiElement current) {
        return (current.getParent() instanceof RakuMultiDecl && original.getParent() instanceof RakuMultiDecl)
                    && ((((RakuMultiDecl) current.getParent()).getSignature() != null)
                          && ((RakuMultiDecl) original.getParent()).getSignature() != null)
                    && ! (((RakuMultiDecl) current.getParent()).getSignature()
                                                               .isEquivalentTo((((RakuMultiDecl) original.getParent()).getSignature())));
    }
}
