package org.raku.comma.intention;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.IPopupChooserBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import org.raku.comma.parsing.RakuTokenTypes;
import org.raku.comma.psi.RakuElementFactory;
import org.raku.comma.psi.RakuPackageDecl;
import org.raku.comma.psi.RakuTrait;
import org.raku.comma.utils.RakuConstants;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ChangePackageTypeIntention extends PsiElementBaseIntentionAction implements IntentionAction {
    @Override
    public com.intellij.codeInsight.intention.preview.IntentionPreviewInfo generatePreview(@NotNull Project project,
                                                                                           @NotNull Editor editor,
                                                                                           @NotNull PsiFile file) {
        // The result depends on a popup selection; there is nothing sensible
        // to preview (invoking the popup during preview calculation is what
        // originally got this intention disabled).
        return com.intellij.codeInsight.intention.preview.IntentionPreviewInfo.EMPTY;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        ColoredListCellRenderer<String> renderer = new ColoredListCellRenderer<>() {
            @Override
            protected void customizeCellRenderer(@NotNull JList<? extends String> list,
                                                 String value,
                                                 int index,
                                                 boolean selected,
                                                 boolean hasFocus) {
                setIcon(RakuConstants.PACKAGE_TYPES.get(value));
                append(value);
            }
        };

        List<String> options = new ArrayList<>(
            ContainerUtil.filter(RakuConstants.PACKAGE_TYPES.keySet(), s -> !s.equals(element.getText()))
        );

        IPopupChooserBuilder<String> builder = JBPopupFactory.getInstance()
            .createPopupChooserBuilder(options)
            .setRenderer(renderer)
            .setItemChosenCallback(type -> invokeImpl(project, editor, element, type))
            .setNamerForFiltering(p -> p);
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            // For testing just take the next element in list
            options = new ArrayList<>(RakuConstants.PACKAGE_TYPES.keySet());
            String newType = options.get(options.indexOf(element.getText()) % (options.size() - 1) + 1);
            if (element.getText().equals("role"))
                newType = "class";
            else if (element.getText().equals("class"))
                newType = "role";
            invokeImpl(project, editor, element, newType);
        } else {
            builder.createPopup().showInBestPositionFor(editor);
        }
    }

    private static void invokeImpl(Project project, Editor editor, PsiElement element, String type) {
        WriteCommandAction.runWriteCommandAction(
            project, "Change Package Type", null, () -> {
                PsiFile containingFile = element.getContainingFile();
                boolean shouldAddMonitorUsage = type.equals("monitor")
                    && !org.raku.comma.utils.RakuUseUtils.usesModule(element, "OO::Monitors");

                RakuPackageDecl decl = PsiTreeUtil.getParentOfType(element, RakuPackageDecl.class);
                if (decl == null)
                    return;
                String oldType = decl.getPackageKind();
                PsiElement declarator = decl.getPackageKeywordNode();
                if (declarator == null)
                    return;
                declarator.replace(RakuElementFactory.createPackageDeclarator(project, type));

                PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());

                if (shouldAddMonitorUsage) {
                    org.raku.comma.utils.RakuUseUtils.addUse(editor, containingFile, "OO::Monitors", "OO::Monitors");
                    PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
                }

                updateChildren(decl, oldType, type);
            });
    }

    private static void updateChildren(RakuPackageDecl decl, String oldType, String newType) {
        if (!(oldType.equals("class") && newType.equals("role")) &&
            !(oldType.equals("role") && newType.equals("class")))
            return;

        List<RakuPackageDecl> children = decl.collectChildren();

        String packageName = decl.getPackageName();

        for (RakuPackageDecl child : children) {
            for (RakuTrait trait : child.getTraits()) {
                String modifier = trait.getTraitModifier();
                String name = trait.getTraitName();
                if (oldType.equals("class") && modifier.equals("is") && name.equals(packageName))
                    trait.changeTraitMod("does");
                else if (oldType.equals("role") && modifier.equals("does") && name.equals(packageName))
                    trait.changeTraitMod("is");
            }
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        IElementType elementType = element.getNode().getElementType();
        if (elementType == RakuTokenTypes.PACKAGE_DECLARATOR)
            return true;
        return elementType == RakuTokenTypes.PACKAGE_NAME;
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Change package type";
    }

    @NotNull
    @Override
    public String getText() {
        return getFamilyName();
    }
}
