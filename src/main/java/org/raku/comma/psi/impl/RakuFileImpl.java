package org.raku.comma.psi.impl;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.meta.PsiMetaData;
import com.intellij.psi.stubs.Stub;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.JBFont;
import org.raku.comma.RakuLanguage;
import org.raku.comma.filetypes.*;
import org.raku.comma.psi.*;
import org.raku.comma.psi.stub.*;
import org.raku.comma.psi.symbols.*;
import org.raku.comma.highlighter.RakuHighlightVisitor;
import org.raku.comma.pod.PodDomBuildingContext;
import org.raku.comma.pod.PodDomDeclarator;
import org.raku.comma.pod.PodDomNode;
import org.raku.comma.pod.PodRenderingContext;
import org.raku.comma.psi.external.ExternalRakuFile;
import org.raku.comma.readerMode.RakuActionProvider;
import org.raku.comma.readerMode.RakuReaderModeState;
import org.raku.comma.repl.RakuReplState;
import org.raku.comma.sdk.RakuSettingTypeId;
import org.raku.comma.services.project.RakuDependencyService;
import org.raku.comma.services.project.RakuProjectSdkService;
import org.raku.comma.utils.RakuUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RakuFileImpl extends PsiFileBase implements RakuFile {
    public static final Map<String, RakuSettingTypeId> VARIABLE_SYMBOLS = new HashMap<>();
    private static final String POD_HTML_TEMPLATE = RakuUtils.getResourceAsString("podPreview/template.html");

    private String moduleName;
    private String originalPath;
    private Boolean dependencyFile = Boolean.FALSE;

    static {
        // compile time variables
        VARIABLE_SYMBOLS.put("$?FILE", RakuSettingTypeId.Str);
        VARIABLE_SYMBOLS.put("$?LINE", RakuSettingTypeId.Int);
        VARIABLE_SYMBOLS.put("$?LANG", null);
        VARIABLE_SYMBOLS.put("%?RESOURCES", null);
        VARIABLE_SYMBOLS.put("$?PACKAGE", null);
        // pod vars
        VARIABLE_SYMBOLS.put("$=pod", RakuSettingTypeId.Array);
        // special variables
        VARIABLE_SYMBOLS.put("$_", null);
        VARIABLE_SYMBOLS.put("$/", RakuSettingTypeId.Match);
        VARIABLE_SYMBOLS.put("$!", RakuSettingTypeId.Exception);
        // dynamic variables
        VARIABLE_SYMBOLS.put("$*ARGFILES", null);
        VARIABLE_SYMBOLS.put("@*ARGS", RakuSettingTypeId.Array);
        VARIABLE_SYMBOLS.put("$*IN", RakuSettingTypeId.IO__Handle);
        VARIABLE_SYMBOLS.put("$*OUT", RakuSettingTypeId.IO__Handle);
        VARIABLE_SYMBOLS.put("$*ERR", RakuSettingTypeId.IO__Handle);
        VARIABLE_SYMBOLS.put("%*ENV", RakuSettingTypeId.Hash);
        VARIABLE_SYMBOLS.put("$*REPO", null);
        VARIABLE_SYMBOLS.put("$*INIT-DISTANT", RakuSettingTypeId.Instant);
        VARIABLE_SYMBOLS.put("$*TZ", RakuSettingTypeId.Int);
        VARIABLE_SYMBOLS.put("$*CWD", RakuSettingTypeId.IO__Path);
        VARIABLE_SYMBOLS.put("$*KERNEL", RakuSettingTypeId.Kernel);
        VARIABLE_SYMBOLS.put("$*DISTRO", RakuSettingTypeId.Distro);
        VARIABLE_SYMBOLS.put("$*VM", RakuSettingTypeId.VM);
        VARIABLE_SYMBOLS.put("$*PERL", RakuSettingTypeId.Perl);
        VARIABLE_SYMBOLS.put("$*RAKU", RakuSettingTypeId.Raku);
        VARIABLE_SYMBOLS.put("$*PID", RakuSettingTypeId.Int);
        VARIABLE_SYMBOLS.put("$*PROGRAM-NAME", RakuSettingTypeId.Str);
        VARIABLE_SYMBOLS.put("$*PROGRAM", RakuSettingTypeId.IO__Path);
        VARIABLE_SYMBOLS.put("&*EXIT", null);
        VARIABLE_SYMBOLS.put("$*EXECUTABLE", RakuSettingTypeId.IO__Path);
        VARIABLE_SYMBOLS.put("$*EXECUTABLE-NAME", RakuSettingTypeId.Str);
        VARIABLE_SYMBOLS.put("$*USER", RakuSettingTypeId.IntStr);
        VARIABLE_SYMBOLS.put("$*GROUP", RakuSettingTypeId.IntStr);
        VARIABLE_SYMBOLS.put("$*HOMEDRIVE", null);
        VARIABLE_SYMBOLS.put("$*HOMEPATH", null);
        VARIABLE_SYMBOLS.put("$*HOME", RakuSettingTypeId.IO__Path);
        VARIABLE_SYMBOLS.put("$*SPEC", RakuSettingTypeId.IO__Spec);
        VARIABLE_SYMBOLS.put("$*TMPDIR", RakuSettingTypeId.IO__Path);
        VARIABLE_SYMBOLS.put("$*THREAD", RakuSettingTypeId.Thread);
        VARIABLE_SYMBOLS.put("$*SCHEDULER", RakuSettingTypeId.ThreadPoolScheduler);
        VARIABLE_SYMBOLS.put("$*SAMPLER", null);
        VARIABLE_SYMBOLS.put("$*COLLATION", RakuSettingTypeId.Collation);
        VARIABLE_SYMBOLS.put("$*TOLERANCE", RakuSettingTypeId.Num);
        VARIABLE_SYMBOLS.put("$*DEFAULT-READ-ELEMS", RakuSettingTypeId.Int);
    }

    // We may use CacheValue/CachedValueManager mechanism, but a field is easier to try
    private List<RakuSymbol> EXPORT_CACHE = null;
    private AtomicBoolean isCalculatingExport = new AtomicBoolean();

    public void dropExportCache() {
        EXPORT_CACHE = null;
    }

    private static final RakuMultiExtensionFileType[] RAKU_FILE_TYPES = new RakuMultiExtensionFileType[]{
        RakuModuleFileType.INSTANCE,
        RakuScriptFileType.INSTANCE,
        RakuTestFileType.INSTANCE,
        RakuPodFileType.INSTANCE
    };

    public RakuFileImpl(FileViewProvider viewProvider) {
        super(viewProvider, RakuLanguage.INSTANCE);
    }

    @Override
    public boolean isReal() {
        return true;
    }

    @Override
    public String renderPod() {
        // Translate all Pod and documentable program elements into PodDom for rendering.
        PodDomBuildingContext context = new PodDomBuildingContext();
        collectPodAndDocumentables(context);

        // If there is a title or subtitle, use it has a header.
        StringBuilder builder = new StringBuilder();
        Map<String, PodDomNode> semanticBlocks = context.getSemanticBlocks();
        if (semanticBlocks.containsKey("TITLE")) {
            builder.append("<header>\n<h1>");
            semanticBlocks.get("TITLE").renderInto(builder, new PodRenderingContext());
            builder.append("</h1>\n");
            if (semanticBlocks.containsKey("SUBTITLE")) {
                builder.append("<h3>");
                semanticBlocks.get("SUBTITLE").renderInto(builder, new PodRenderingContext());
                builder.append("</h3>\n");
            }
            builder.append("</header>\n");
        } else if (getFileType() instanceof RakuModuleFileType) {
            String moduleName = getEnclosingRakuModuleName();
            if (moduleName != null) {
                builder.append("<header>\n<h1>");
                builder.append(RakuUtils.escapeHTML(moduleName));
                builder.append("</h1>\n</header>\n");
            }
        }

        // Render all of the non-semantic blocks.
        for (PodDomNode dom : context.getBlocks())
            dom.renderInto(builder, new PodRenderingContext());

        // If there are documentable types and subs, render those API docs.
        List<PodDomDeclarator> types = context.getTypes();
        if (!types.isEmpty()) {
            builder.append("<h2>Types</h2>\n");
            for (PodDomDeclarator type : types)
                type.renderInto(builder, new PodRenderingContext());
        }
        List<PodDomDeclarator> subs = context.getSubs();
        if (!subs.isEmpty()) {
            builder.append("<h2>Subroutines</h2>\n");
            for (PodDomDeclarator sub : subs)
                sub.renderInto(builder, new PodRenderingContext());
        }

        // Substitute HTML into template.
        Map<String, String> substitute = new HashMap<>();
        substitute.put("BODY", builder.toString());
        substitute.put("BACKGROUND", htmlColor(JBColor.background()));
        substitute.put("FOREGROUND", htmlColor(JBColor.foreground()));
        substitute.put("BACKGROUND-HOVER", htmlColor(new JBColor(Gray._223, new Color(76, 80, 82))));
        substitute.put("FONT", JBFont.label().getFamily());
        substitute.put("LINK", htmlColor(JBColor.BLUE));
        substitute.put("HEADING-BORDER", htmlColor(JBColor.foreground().darker()));
        substitute.put(
                "MODE_BUTTON",
                getUserData(RakuActionProvider.RAKU_EDITOR_MODE_STATE) == RakuReaderModeState.SPLIT
                ? "<button class=\"button\" onclick=\"window.JavaPanelBridge.goToDocumentationMode()\">Documentation</button>"
                : "<button class=\"button\" onclick=\"window.JavaPanelBridge.goToSplitMode()\">Live preview</button>");
        String rendered = POD_HTML_TEMPLATE;
        for (Map.Entry<String, String> entry : substitute.entrySet())
            rendered = rendered.replace("[[" + entry.getKey() + "]]", entry.getValue());
        return rendered;
    }

    private static String htmlColor(Color color) {
        return String.format("%s, %s, %s", color.getRed(), color.getGreen(), color.getBlue());
    }

    @NotNull
    @Override
    public FileType getFileType() {
        String name = getName();
        for (RakuMultiExtensionFileType type : RAKU_FILE_TYPES) {
            for (String ext : type.extensions()) {
                if (name.endsWith(ext)) {
                    return (FileType) type;
                }
            }
        }
        return RakuModuleFileType.INSTANCE;
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return getContainingFile();
    }

    @Override
    public void contributeGlobals(RakuSymbolCollector collector, Set<String> seen) {
        // TODO: Migrate this to a dedicated service so it can be pushed out of EDT

        // We ignore the entire stubbing process for our dependency files, but we also
        // create them as regular (ie, not external) RakuFiles because I haven't gotten
        // them to work as expected using that representation.
        if (Boolean.TRUE.equals(dependencyFile)) return;

        // Walk from the top of the PSI tree to find top-level, our-scoped packages.
        // Contribute those.
        Stub stub = this.getStub();
        if (stub != null) {
            Queue<Stub> visit = new LinkedList<>();
            visit.add(stub);
            while (!visit.isEmpty()) {
                if (collector.isSatisfied()) {
                    return;
                }
                Stub current = visit.remove();
                boolean addChildren = false;
                if (current == stub) {
                    addChildren = true;
                } else if (current instanceof RakuVariableDeclStub) {
                    if (((RakuVariableDeclStub) current).isExported() || ((RakuVariableDeclStub) current).getScope()
                                                                                                         .equals("our"))
                    {
                        ((RakuVariableDeclStub) current).getPsi().contributeLexicalSymbols(collector);
                    }
                } else if (current instanceof RakuPackageDeclStub nested) {
                    if (nested.getPackageKind().equals("module")) {
                        addChildren = true;
                    }
                    String scope = nested.getScope();
                    if (scope.equals("our") || scope.equals("unit")) {
                        String topName = nested.getTypeName();
                        if (topName != null && !topName.isEmpty()) {
                            RakuPackageDecl psi = nested.getPsi();
                            collector.offerSymbol(new RakuExplicitAliasedSymbol(RakuSymbolKind.TypeOrConstant,
                                                                                psi, topName));
                            if (!collector.isSatisfied()) {
                                psi.contributeNestedPackagesWithPrefix(collector, topName + "::");
                            }
                        }
                    }
                } else if (current instanceof RakuRoutineDeclStub) {
                    if (((RakuRoutineDeclStub) current).isExported() || ((RakuRoutineDeclStub) current).getScope()
                                                                                                       .equals("our"))
                    {
                        ((RakuRoutineDeclStub) current).getPsi().contributeLexicalSymbols(collector);
                    }
                } else if (current instanceof RakuEnumStub) {
                    if (((RakuEnumStub) current).isExported() || ((RakuEnumStub) current).getScope().equals("our")) {
                        ((RakuEnumStub) current).getPsi().contributeLexicalSymbols(collector);
                    }
                } else if (current instanceof RakuSubsetStub) {
                    if (((RakuSubsetStub) current).isExported() || ((RakuSubsetStub) current).getScope()
                                                                                             .equals("our"))
                    {
                        collector.offerSymbol(new RakuExplicitSymbol(RakuSymbolKind.TypeOrConstant, ((RakuSubsetStub) current).getPsi()));
                    }
                } else if (current instanceof RakuUseStatementStub use) {
                    contributeTransitive(collector, seen, "use", use.getModuleName());
                } else if (current instanceof RakuNeedStatementStub need) {
                    for (String name : need.getModuleNames())
                        contributeTransitive(collector, seen, "need", name);
                } else {
                    addChildren = true;
                }

                if (addChildren) {
                    visit.addAll(current.getChildrenStubs());
                }
            }
        } else {
            Queue<RakuPsiElement> visit = new LinkedList<>();
            visit.add(this);
            while (!visit.isEmpty()) {
                if (collector.isSatisfied()) {
                    return;
                }
                RakuPsiElement current = visit.remove();
                boolean addChildren = false;
                if (current == this) {
                    addChildren = true;
                } else if (current instanceof RakuVariableDecl) {
                    if (((RakuVariableDecl) current).isExported() || ((RakuVariableDecl) current).getScope()
                                                                                                 .equals("our"))
                    {
                        ((RakuVariableDecl) current).contributeLexicalSymbols(collector);
                    }
                } else if (current instanceof RakuPackageDecl nested) {
                    if (nested.getPackageKind().equals("module")) {
                        addChildren = true;
                    }
                    String scope = nested.getScope();
                    if (scope.equals("our") || scope.equals("unit")) {
                        String topName = nested.getName();
                        if (topName != null && !topName.isEmpty()) {
                            collector.offerSymbol(new RakuExplicitAliasedSymbol(RakuSymbolKind.TypeOrConstant,
                                                                                nested, topName));
                            if (!collector.isSatisfied()) {
                                nested.contributeNestedPackagesWithPrefix(collector, topName + "::");
                            }
                        }
                    }
                } else if (current instanceof RakuRoutineDecl) {
                    // Maybe contribute sub EXPORT
                    if (((RakuRoutineDecl) current).isExported() || ((RakuRoutineDecl) current).getScope()
                                                                                               .equals("our"))
                    {
                        ((RakuRoutineDecl) current).contributeLexicalSymbols(collector);
                    }
                    if (Objects.equals(current.getName(), "EXPORT")) {
                        // Contributing asynchronously would hand symbols to a
                        // collector the resolution walk has already abandoned;
                        // only the cache-warming may happen off-thread.
                        if (Objects.nonNull(EXPORT_CACHE)) {
                            contributeSymbolsFromEXPORT(collector);
                        } else {
                            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                                contributeSymbolsFromEXPORT(collector);
                            });
                        }
                    }
                } else if (current instanceof RakuEnum rakuEnum) {
                    String scope = rakuEnum.getScope();
                    if (scope.equals("our")) {
                        rakuEnum.contributeLexicalSymbols(collector);
                    }
                } else if (current instanceof RakuSubset) {
                    if (((RakuSubset) current).isExported() || ((RakuSubset) current).getScope().equals("our")) {
                        collector.offerSymbol(new RakuExplicitSymbol(RakuSymbolKind.TypeOrConstant, (PsiNamedElement) current));
                    }
                } else if (current instanceof RakuUseStatement use) {
                    contributeTransitive(collector, seen, "use", use.getModuleName());
                } else if (current instanceof RakuNeedStatement need) {
                    for (String name : need.getModuleNames())
                        contributeTransitive(collector, seen, "need", name);
                } else if (!(current instanceof RakuPsiScope)) {
                    addChildren = true;
                }
                if (addChildren) {
                    for (PsiElement e : current.getChildren())
                        if (e instanceof RakuPsiElement) {
                            visit.add((RakuPsiElement) e);
                        }
                }
            }
        }
    }

    private void contributeSymbolsFromEXPORT(RakuSymbolCollector collector) {
        if (isCalculatingExport.compareAndSet(false, true)) {
            if (Objects.nonNull(EXPORT_CACHE)) {
                EXPORT_CACHE.forEach(collector::offerSymbol);
            } else {
                LightVirtualFile dummy = new LightVirtualFile(getName());
                ExternalRakuFile rakuFile = new ExternalRakuFile(getProject(), dummy);
                String invocation = "use " + getEnclosingRakuModuleName();
                List<RakuSymbol> symbols = getProject().getService(RakuProjectSdkService.class)
                                                       .getSymbolCache()
                                                       .loadModuleSymbols(rakuFile,
                                                                          getName(),
                                                                          invocation,
                                                                          new HashMap<>(),
                                                                          true);
                EXPORT_CACHE = symbols;
                symbols.forEach(collector::offerSymbol);
            }
            isCalculatingExport.set(false);
        }
    }

    private void contributeTransitive(RakuSymbolCollector collector, Set<String> seen, String directive, String name) {
        if (Objects.isNull(name) || seen.contains(name)) {
            return;
        }
        var shortName = RakuUtils.stripAuthVerApi(name);

        seen.add(name);
        seen.add(shortName);

        Project project = getProject();
        var file = getProject().getService(RakuDependencyService.class).provideToRakuFile(shortName);

        if (file != null) {
            file.contributeGlobals(collector, seen);
        } else {
            // We only have globals, not exports, transitively available.
            RakuFile needFile = project.getService(RakuProjectSdkService.class)
                                       .getSymbolCache()
                                       .getPsiFileForModule(name, directive + " " + name);
            if (needFile != null) {
                needFile.contributeGlobals(collector, new HashSet<>());
            }
        }
    }

    @Override
    public void contributeScopeSymbols(RakuSymbolCollector collector) {
        for (String symbol : VARIABLE_SYMBOLS.keySet()) {
            collector.offerSymbol(new RakuImplicitSymbol(RakuSymbolKind.Variable, symbol));
            if (collector.isSatisfied()) {
                return;
            }
        }
        RakuFile coreSettings = getProject().getService(RakuProjectSdkService.class)
                                            .getSymbolCache()
                                            .getCoreSettingFile();
        if (coreSettings == null) return;

        coreSettings.contributeGlobals(collector, new HashSet<>());
        if (collector.isSatisfied()) return;

        PsiElement list = PsiTreeUtil.getChildOfType(this, RakuStatementList.class);
        if (Objects.nonNull(list)) {
            PsiElement finish = PsiTreeUtil.findChildOfType(list, PodBlockFinish.class);
            if (Objects.nonNull(finish)) {
                RakuSymbol finishBlock = new RakuImplicitSymbol(RakuSymbolKind.Variable, "$=finish");
                collector.offerSymbol(finishBlock);
            }
        }

        VirtualFile virtualFile = getOriginalFile().getVirtualFile();
        if (Objects.nonNull(virtualFile)) {
            RakuReplState replState = virtualFile.getUserData(RakuReplState.RAKU_REPL_STATE);
            if (Objects.nonNull(replState)) {
                replState.contributeFromHistory(collector);
            }
        }
    }

    @Nullable
    @Override
    public PsiMetaData getMetaData() {
        String name = getEnclosingRakuModuleName();
        if (Objects.isNull(name)) {
            name = getName();
        }
        String finalName = name;
        return new PsiMetaData() {
            @Override
            public PsiElement getDeclaration() {
                return RakuFileImpl.this;
            }

            @Override
            public String getName(PsiElement context) {
                return finalName;
            }

            @Override
            public String getName() {
                return finalName;
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

    /* Builds a map from statement start line number to the line(s) that the statement
     * spans. The start line is always included, but any line numbers where an inner
     * statement starts will be excluded (so the values may be sparse). Any line that
     * does not have an entry is not "interesting" statement (e.g. one that is meaningful
     * in coverage or could be hit by a breakpoint).
     */
    @Override
    public Map<Integer, List<Integer>> getStatementLineMap() {
        Map<Integer, List<Integer>> result = new HashMap<>();
        Set<Integer> covered = new HashSet<>();
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread() && application.isWriteAccessAllowed()) {
            PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
        }
        FileViewProvider fileViewProvider = getViewProvider();
        Document document = fileViewProvider.getDocument();
        RakuStatementList stmts = PsiTreeUtil.getChildOfType(this, RakuStatementList.class);
        buildStatementLineMap(result, covered, document, stmts);
        return result;
    }

    private void buildStatementLineMap(Map<Integer, List<Integer>> result,
                                       Set<Integer> covered,
                                       Document document,
                                       RakuStatementList stmts)
    {
        if (stmts == null) {
            return;
        }
        for (RakuStatement stmt : PsiTreeUtil.getChildrenOfTypeAsList(stmts, RakuStatement.class)) {
            // Get the start line and, if not seen already add it to the set of
            // covered statements.
            int startLine = document.getLineNumber(stmt.getTextOffset());
            boolean seen = covered.contains(startLine);
            List<Integer> spanned = null;
            if (!seen) {
                if (!isUncoverableDeclarator(stmt)) {
                    covered.add(startLine);
                    if (!isSymbolDeclarator(stmt)) {
                        spanned = new ArrayList<>();
                        result.put(startLine, spanned);
                        spanned.add(startLine);
                    }
                }
            }

            // Visit statement lists enclosed in this file.
            findNestedStatements(result, covered, document, stmt);

            // Now add uncovered lines up to the end of this statement.
            if (!seen) {
                try {
                    int endLine = document.getLineNumber(stmt.getTextOffset() +
                                                                 stmt.getText().replaceFirst("\\s+$", "").length() - 1);
                    for (int i = startLine + 1; i <= endLine; i++) {
                        if (!covered.contains(i)) {
                            covered.add(i);
                            if (spanned != null) {
                                spanned.add(i);
                            }
                        }
                    }
                } catch (IndexOutOfBoundsException ignored) {
                    // Code piece was updated in the middle of building statement line map,
                    // so just ignore the exception until next rebuilding
                }
            }
        }
    }

    private static boolean isSymbolDeclarator(RakuStatement stmt) {
        PsiElement scoped = PsiTreeUtil.getChildOfType(stmt, RakuScopedDecl.class);
        RakuPsiElement declChild = PsiTreeUtil.getChildOfAnyType(scoped != null
                                                                 ? scoped
                                                                 : stmt,
                                                                 RakuPackageDecl.class, RakuUseStatement.class, RakuNeedStatement.class,
                                                                 RakuSubset.class, RakuEnum.class, RakuStubCode.class);
        return declChild != null;
    }

    private static boolean isUncoverableDeclarator(RakuStatement stmt) {
        RakuScopedDecl scopedDecl = PsiTreeUtil.getChildOfType(stmt, RakuScopedDecl.class);
        RakuPsiElement consider = scopedDecl == null
                                  ? stmt
                                  : scopedDecl;

        RakuPsiElement codeChild = PsiTreeUtil.getChildOfAnyType(consider,
                                                                 RakuRoutineDecl.class, RakuMultiDecl.class, RakuRegexDecl.class);
        if (codeChild != null) {
            return true;
        }

        RakuVariableDecl varChild = PsiTreeUtil.getChildOfType(consider, RakuVariableDecl.class);
        if (varChild != null && !varChild.hasInitializer()) {
            return true;
        }

        return false;
    }

    private void findNestedStatements(Map<Integer, List<Integer>> result, Set<Integer> covered, Document document, RakuPsiElement node) {
        for (PsiElement child : node.getChildren()) {
            if (child instanceof RakuStatementList) {
                buildStatementLineMap(result, covered, document, (RakuStatementList) child);
            } else {
                findNestedStatements(result, covered, document, (RakuPsiElement) child);
            }
        }
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof RakuHighlightVisitor) {
            ((RakuHighlightVisitor) visitor).visitRakuElement(this);
        } else {
            super.accept(visitor);
        }
    }

    @Override public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
    @Override public String getModuleName() {
        return moduleName;
    }

    @Override public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }
    @Override public String getOriginalPath() {
        return originalPath;
    }

    @Override public void setDependencyFile(Boolean dependencyFile) { this.dependencyFile = dependencyFile; }
    @Override public Boolean getDependencyFile() { return this.dependencyFile; }

}
