package org.raku.comma.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import org.raku.comma.psi.RakuFile;
import org.raku.comma.psi.RakuModuleName;
import org.raku.comma.psi.RakuNeedStatement;
import org.raku.comma.psi.stub.RakuNeedStatementStub;
import org.raku.comma.psi.stub.RakuNeedStatementStubElementType;
import org.raku.comma.psi.symbols.RakuSymbolCollector;
import org.jetbrains.annotations.NotNull;
import org.raku.comma.services.project.RakuDependencyService;
import org.raku.comma.services.project.RakuProjectSdkService;
import org.raku.comma.utils.RakuUtils;

import java.util.*;

public class RakuNeedStatementImpl extends StubBasedPsiElementBase<RakuNeedStatementStub> implements RakuNeedStatement {
    public RakuNeedStatementImpl(@NotNull ASTNode node) {
        super(node);
    }

    public RakuNeedStatementImpl(RakuNeedStatementStub stub, RakuNeedStatementStubElementType type) {
        super(stub, type);
    }

    @Override
    public void contributeLexicalSymbols(RakuSymbolCollector collector) {
        // We cannot contribute based on stubs when indexing is in progress
        if (DumbService.isDumb(getProject())) return;
        for (String name : getModuleNames().stream().map(RakuUtils::stripAuthVerApi).toList()) {
            Project project = getProject();
            RakuFile found = com.intellij.psi.stubs.StubIndex
                .getElements(org.raku.comma.psi.stub.index.ProjectModulesStubIndex.getInstance().getKey(),
                             name,
                             project,
                             com.intellij.psi.search.GlobalSearchScope.projectScope(project),
                             RakuFile.class)
                .stream().findFirst().orElse(null);
            if (found == null) {
                found = project.getService(RakuDependencyService.class).provideToRakuFile(name);
            }
            if (found != null) {
                Set<String> seen = new HashSet<>();
                seen.add(name);
                found.contributeGlobals(collector, seen);
            } else {
                RakuFile file = project.getService(RakuProjectSdkService.class)
                                       .getSymbolCache()
                                       .getPsiFileForModule(name, getText());
                if (file != null) {
                    file.contributeGlobals(collector, new HashSet<>());
                }
            }
        }
    }

    @Override
    public List<String> getModuleNames() {
        RakuNeedStatementStub stub = getStub();
        if (stub != null) {
            return stub.getModuleNames();
        }

        List<String> result = new ArrayList<>();
        for (RakuModuleName moduleName : findChildrenByClass(RakuModuleName.class))
            result.add(moduleName.getText());
        return result;
    }

    public String toString() {
        return getClass().getSimpleName() + "(Raku:NEED_STATEMENT)";
    }
}
