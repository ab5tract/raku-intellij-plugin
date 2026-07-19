package org.raku.comma.utils;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.raku.comma.services.project.RakuMetaDataComponent;
import org.raku.comma.psi.RakuStatement;
import org.raku.comma.psi.RakuUseStatement;
import org.raku.comma.psi.RakuPsiScope;
import org.raku.comma.psi.RakuStatementList;

public class RakuUseUtils {
    public static boolean usesModule(PsiElement element, String expected) {
        RakuPsiScope scope = PsiTreeUtil.getParentOfType(element, RakuPsiScope.class);
        int elementTextOffset = element.getTextOffset();
        while (scope != null) {
            RakuStatementList list = PsiTreeUtil.findChildOfType(scope, RakuStatementList.class);
            if (list == null) break;
            RakuStatement[] stats = PsiTreeUtil.getChildrenOfType(list, RakuStatement.class);
            if (stats == null) stats = new RakuStatement[0];
            for (RakuStatement statement : stats) {
                if (statement.getTextOffset() > elementTextOffset) break;
                for (PsiElement child : statement.getChildren()) {
                    if (!(child instanceof RakuUseStatement)) continue;
                    String moduleName = ((RakuUseStatement)child).getModuleName();
                    if (expected.equals(moduleName)) {
                        return true;
                    }
                }
            }
            scope = PsiTreeUtil.getParentOfType(scope, RakuPsiScope.class);
        }

        return false;
    }

    public static void addUse(Editor editor, PsiFile file, String useName, String moduleName) {
        editor.getDocument().insertString(0, "use " + useName + ";\n");
        var metaData = file.getProject().getService(RakuMetaDataComponent.class);
        if (! metaData.getDepends().contains(moduleName)) {
            metaData.addDepends(moduleName);
        }
    }
}
