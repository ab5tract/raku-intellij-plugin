package org.raku.comma.psi.impl

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.meta.PsiMetaData
import com.intellij.psi.stubs.Stub
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import com.intellij.util.ArrayUtil
import com.intellij.util.ui.JBFont
import org.raku.comma.RakuLanguage
import org.raku.comma.filetypes.RakuModuleFileType
import org.raku.comma.filetypes.RakuMultiExtensionFileType
import org.raku.comma.filetypes.RakuPodFileType
import org.raku.comma.filetypes.RakuScriptFileType
import org.raku.comma.filetypes.RakuTestFileType
import org.raku.comma.highlighter.RakuHighlightVisitor
import org.raku.comma.pod.PodDomBuildingContext
import org.raku.comma.pod.PodRenderingContext
import org.raku.comma.psi.PodBlockFinish
import org.raku.comma.psi.RakuEnum
import org.raku.comma.psi.RakuFile
import org.raku.comma.psi.RakuMultiDecl
import org.raku.comma.psi.RakuNeedStatement
import org.raku.comma.psi.RakuPackageDecl
import org.raku.comma.psi.RakuPsiElement
import org.raku.comma.psi.RakuPsiScope
import org.raku.comma.psi.RakuRegexDecl
import org.raku.comma.psi.RakuRoutineDecl
import org.raku.comma.psi.RakuScopedDecl
import org.raku.comma.psi.RakuStatement
import org.raku.comma.psi.RakuStatementList
import org.raku.comma.psi.RakuStubCode
import org.raku.comma.psi.RakuSubset
import org.raku.comma.psi.RakuUseStatement
import org.raku.comma.psi.RakuVariableDecl
import org.raku.comma.psi.external.ExternalRakuFile
import org.raku.comma.psi.stub.RakuEnumStub
import org.raku.comma.psi.stub.RakuNeedStatementStub
import org.raku.comma.psi.stub.RakuPackageDeclStub
import org.raku.comma.psi.stub.RakuRoutineDeclStub
import org.raku.comma.psi.stub.RakuSubsetStub
import org.raku.comma.psi.stub.RakuUseStatementStub
import org.raku.comma.psi.stub.RakuVariableDeclStub
import org.raku.comma.psi.symbols.RakuExplicitAliasedSymbol
import org.raku.comma.psi.symbols.RakuExplicitSymbol
import org.raku.comma.psi.symbols.RakuImplicitSymbol
import org.raku.comma.psi.symbols.RakuSymbol
import org.raku.comma.psi.symbols.RakuSymbolCollector
import org.raku.comma.psi.symbols.RakuSymbolKind
import org.raku.comma.readerMode.RakuActionProvider
import org.raku.comma.readerMode.RakuReaderModeState
import org.raku.comma.repl.RakuReplState
import org.raku.comma.sdk.RakuSdkUtil
import org.raku.comma.sdk.RakuSettingTypeId
import org.raku.comma.services.project.RakuDependencyService
import org.raku.comma.services.project.RakuProjectSdkService
import org.raku.comma.utils.RakuUtils
import java.awt.Color
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

class RakuFileImpl(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, RakuLanguage.INSTANCE), RakuFile {

    private var moduleName: String? = null
    private var originalPath: String? = null
    private var dependencyFile: Boolean? = false

    // Single-flight load of `sub EXPORT` symbols: null until first requested,
    // then exactly one background computation is in flight or complete. This
    // replaces an EXPORT_CACHE + AtomicBoolean pair that (a) could hand
    // symbols to a collector the synchronous resolution walk had already
    // abandoned, by firing an unconditional executeOnPooledThread callback,
    // and (b) silently skipped contribution entirely when a concurrent caller
    // held the flag. Now: a cold/pending load contributes nothing to the
    // *current* walk (same as before), but the walk that started it always
    // finishes the computation and re-triggers code analysis so a later,
    // fresh walk picks the symbols up instead of them going stale forever.
    private val exportFuture = AtomicReference<CompletableFuture<List<RakuSymbol>>?>()

    fun dropExportCache() {
        exportFuture.set(null)
    }

    override fun isReal(): Boolean = true

    override fun renderPod(): String {
        // Translate all Pod and documentable program elements into PodDom for rendering.
        val context = PodDomBuildingContext()
        collectPodAndDocumentables(context)

        // If there is a title or subtitle, use it has a header.
        val builder = StringBuilder()
        val semanticBlocks = context.semanticBlocks
        if (semanticBlocks.containsKey("TITLE")) {
            builder.append("<header>\n<h1>")
            semanticBlocks["TITLE"]!!.renderInto(builder, PodRenderingContext())
            builder.append("</h1>\n")
            if (semanticBlocks.containsKey("SUBTITLE")) {
                builder.append("<h3>")
                semanticBlocks["SUBTITLE"]!!.renderInto(builder, PodRenderingContext())
                builder.append("</h3>\n")
            }
            builder.append("</header>\n")
        } else if (fileType is RakuModuleFileType) {
            val moduleName = getEnclosingRakuModuleName()
            if (moduleName != null) {
                builder.append("<header>\n<h1>")
                builder.append(RakuUtils.escapeHTML(moduleName))
                builder.append("</h1>\n</header>\n")
            }
        }

        // Render all of the non-semantic blocks.
        for (dom in context.blocks) {
            dom.renderInto(builder, PodRenderingContext())
        }

        // If there are documentable types and subs, render those API docs.
        val types = context.types
        if (types.isNotEmpty()) {
            builder.append("<h2>Types</h2>\n")
            for (type in types) {
                type.renderInto(builder, PodRenderingContext())
            }
        }
        val subs = context.subs
        if (subs.isNotEmpty()) {
            builder.append("<h2>Subroutines</h2>\n")
            for (sub in subs) {
                sub.renderInto(builder, PodRenderingContext())
            }
        }

        // Substitute HTML into template.
        val substitute = HashMap<String, String>()
        substitute["BODY"] = builder.toString()
        substitute["BACKGROUND"] = htmlColor(JBColor.background())
        substitute["FOREGROUND"] = htmlColor(JBColor.foreground())
        substitute["BACKGROUND-HOVER"] = htmlColor(JBColor(Gray._223, Color(76, 80, 82)))
        substitute["FONT"] = JBFont.label().family
        substitute["LINK"] = htmlColor(JBColor.BLUE)
        substitute["HEADING-BORDER"] = htmlColor(JBColor.foreground().darker())
        substitute["MODE_BUTTON"] =
            if (getUserData(RakuActionProvider.RAKU_EDITOR_MODE_STATE) == RakuReaderModeState.SPLIT)
                "<button class=\"button\" onclick=\"window.JavaPanelBridge.goToDocumentationMode()\">Documentation</button>"
            else
                "<button class=\"button\" onclick=\"window.JavaPanelBridge.goToSplitMode()\">Live preview</button>"
        var rendered = POD_HTML_TEMPLATE
        for ((key, value) in substitute) {
            rendered = rendered.replace("[[$key]]", value)
        }
        return rendered
    }

    override fun getFileType(): FileType {
        val name = name
        for (type in RAKU_FILE_TYPES) {
            for (ext in type.extensions()) {
                if (name.endsWith(ext)) {
                    return type as FileType
                }
            }
        }
        return RakuModuleFileType.INSTANCE
    }

    override fun getNameIdentifier(): PsiElement? = containingFile

    /**
     * A top-level declaration seen through the stub or AST lens of the
     * globals walk. Cheap facts (scope, exported, names) are read stub-first
     * so stubbed dependency files are never force-parsed; the PSI is
     * materialized lazily, only when a gate passes.
     */
    private sealed interface GlobalsFacts {
        class Variable(val exported: Boolean, val scope: String, val psi: () -> RakuVariableDecl) : GlobalsFacts
        class Pkg(val kind: String, val scope: String, val topName: String?, val psi: () -> RakuPackageDecl) : GlobalsFacts
        class Routine(val exported: Boolean, val scope: String, val name: String?, val psi: () -> RakuRoutineDecl) : GlobalsFacts
        class EnumDecl(val exported: Boolean, val scope: String, val psi: () -> RakuEnum) : GlobalsFacts
        class SubsetDecl(val exported: Boolean, val scope: String, val psi: () -> PsiNamedElement) : GlobalsFacts
        class Use(val moduleName: String?) : GlobalsFacts
        class Need(val moduleNames: List<String>) : GlobalsFacts
    }

    private fun factsOf(node: Any): GlobalsFacts? = when (node) {
        is RakuVariableDeclStub -> GlobalsFacts.Variable(node.isExported(), node.getScope()) { node.psi }
        is RakuPackageDeclStub -> GlobalsFacts.Pkg(node.packageKind, node.getScope(), node.typeName) { node.psi }
        is RakuRoutineDeclStub -> GlobalsFacts.Routine(node.isExported(), node.getScope(), node.routineName) { node.psi }
        is RakuEnumStub -> GlobalsFacts.EnumDecl(node.isExported(), node.getScope()) { node.psi }
        is RakuSubsetStub -> GlobalsFacts.SubsetDecl(node.isExported(), node.getScope()) { node.psi }
        is RakuUseStatementStub -> GlobalsFacts.Use(node.getModuleName())
        is RakuNeedStatementStub -> GlobalsFacts.Need(node.getModuleNames())
        is RakuVariableDecl -> GlobalsFacts.Variable(node.isExported, node.scope) { node }
        is RakuPackageDecl -> GlobalsFacts.Pkg(node.packageKind, node.scope, node.name) { node }
        is RakuRoutineDecl -> GlobalsFacts.Routine(node.isExported, node.scope, node.name) { node }
        is RakuEnum -> GlobalsFacts.EnumDecl(node.isExported, node.scope) { node }
        is RakuSubset -> GlobalsFacts.SubsetDecl(node.isExported, node.scope) { node as PsiNamedElement }
        is RakuUseStatement -> GlobalsFacts.Use(node.moduleName)
        is RakuNeedStatement -> GlobalsFacts.Need(node.moduleNames)
        else -> null
    }

    private fun walkChildren(node: Any): List<Any> = when (node) {
        is Stub -> node.childrenStubs
        is PsiElement -> generateSequence(node.firstChild) { it.nextSibling }
            .filterIsInstance<RakuPsiElement>()
            .toList()
        else -> emptyList()
    }

    // When a node matches no declaration category: the stub tree contains only
    // indexed declarations, so always descend; the AST walk must not cross
    // into nested scopes.
    private fun descendUnmatched(node: Any): Boolean = node is Stub || node !is RakuPsiScope

    override fun contributeGlobals(collector: RakuSymbolCollector, seen: MutableSet<String>) {
        // TODO: Migrate this to a dedicated service so it can be pushed out of EDT

        // We ignore the entire stubbing process for our dependency files, but we also
        // create them as regular (ie, not external) RakuFiles because I haven't gotten
        // them to work as expected using that representation.
        if (dependencyFile == true) return

        // Walk from the top of the PSI tree to find top-level, our-scoped packages.
        // Contribute those. One BFS over either the stub tree or the AST.
        val root: Any = stub ?: this
        val visit: Queue<Any> = LinkedList()
        visit.add(root)
        while (visit.isNotEmpty()) {
            if (collector.isSatisfied) return
            val current = visit.remove()
            var addChildren = false
            val facts = if (current === root) null else factsOf(current)
            when {
                current === root -> addChildren = true
                facts is GlobalsFacts.Variable ->
                    if (facts.exported || facts.scope == "our") {
                        facts.psi().contributeLexicalSymbols(collector)
                    }
                facts is GlobalsFacts.Pkg -> {
                    if (facts.kind == "module") {
                        addChildren = true
                    }
                    if (facts.scope == "our" || facts.scope == "unit") {
                        val topName = facts.topName
                        if (!topName.isNullOrEmpty()) {
                            val psi = facts.psi()
                            collector.offerSymbol(RakuExplicitAliasedSymbol(
                                RakuSymbolKind.TypeOrConstant, psi, topName))
                            if (!collector.isSatisfied) {
                                psi.contributeNestedPackagesWithPrefix(collector, "$topName::")
                            }
                        }
                    }
                }
                facts is GlobalsFacts.Routine -> {
                    if (facts.exported || facts.scope == "our") {
                        facts.psi().contributeLexicalSymbols(collector)
                    }
                    // Maybe contribute sub EXPORT. Facts come from either
                    // lens, so this fires for a stubbed dependency's EXPORT
                    // routine too, not just an AST-parsed one.
                    if (facts.name == "EXPORT") {
                        contributeSymbolsFromEXPORT(collector)
                    }
                }
                facts is GlobalsFacts.EnumDecl ->
                    // Unified gate: the AST lens historically required "our",
                    // dropping exported-but-lexical enums the stub lens kept.
                    if (facts.exported || facts.scope == "our") {
                        facts.psi().contributeLexicalSymbols(collector)
                    }
                facts is GlobalsFacts.SubsetDecl ->
                    if (facts.exported || facts.scope == "our") {
                        collector.offerSymbol(RakuExplicitSymbol(RakuSymbolKind.TypeOrConstant, facts.psi()))
                    }
                facts is GlobalsFacts.Use -> contributeTransitive(collector, seen, "use", facts.moduleName)
                facts is GlobalsFacts.Need ->
                    for (name in facts.moduleNames) {
                        contributeTransitive(collector, seen, "need", name)
                    }
                else -> addChildren = descendUnmatched(current)
            }
            if (addChildren) {
                visit.addAll(walkChildren(current))
            }
        }
    }

    private fun contributeSymbolsFromEXPORT(collector: RakuSymbolCollector) {
        var future = exportFuture.get()
        if (future == null) {
            val created = CompletableFuture<List<RakuSymbol>>()
            future = if (exportFuture.compareAndSet(null, created)) {
                startExportLoad(created)
                created
            } else {
                // Lost the race: someone else is already loading.
                exportFuture.get()
            }
        }
        if (future != null && future.isDone) {
            future.getNow(emptyList()).forEach(collector::offerSymbol)
        }
        // Else: a load is in flight. Contribute nothing to this walk (its
        // collector may be abandoned before the load finishes); startExportLoad
        // re-triggers analysis on completion so a fresh walk picks it up warm.
    }

    private fun startExportLoad(future: CompletableFuture<List<RakuSymbol>>) {
        val project = project
        val name = name
        val moduleName = getEnclosingRakuModuleName()
        ApplicationManager.getApplication().executeOnPooledThread {
            // Bail out before spawning the raku subprocess if the owning
            // project is already gone by the time this queued task actually
            // runs, rather than keeping a disposed project reachable for the
            // duration of that work. Nothing will observe the future either way.
            if (project.isDisposed) return@executeOnPooledThread
            val dummy = LightVirtualFile(name)
            val rakuFile = ExternalRakuFile(project, dummy)
            val invocation = "use $moduleName"
            val symbols = project.getService(RakuProjectSdkService::class.java)
                .symbolCache
                .loadModuleSymbols(rakuFile, name, invocation, HashMap(), true)
            future.complete(symbols)
            RakuSdkUtil.triggerCodeAnalysis(project)
        }
    }

    private fun contributeTransitive(
        collector: RakuSymbolCollector,
        seen: MutableSet<String>,
        directive: String,
        name: String?,
    ) {
        if (name == null || seen.contains(name)) {
            return
        }
        val shortName = RakuUtils.stripAuthVerApi(name)

        seen.add(name)
        seen.add(shortName)

        val project = project
        val file = project.getService(RakuDependencyService::class.java).provideToRakuFile(shortName)

        if (file != null) {
            file.contributeGlobals(collector, seen)
        } else {
            // We only have globals, not exports, transitively available.
            val needFile = project.getService(RakuProjectSdkService::class.java)
                .symbolCache
                .getPsiFileForModule(name, "$directive $name")
            needFile?.contributeGlobals(collector, HashSet())
        }
    }

    override fun contributeScopeSymbols(collector: RakuSymbolCollector) {
        for (symbol in VARIABLE_SYMBOLS.keys) {
            collector.offerSymbol(RakuImplicitSymbol(RakuSymbolKind.Variable, symbol))
            if (collector.isSatisfied) {
                return
            }
        }
        val coreSettings = project.getService(RakuProjectSdkService::class.java)
            .symbolCache
            .getCoreSettingFile() ?: return

        coreSettings.contributeGlobals(collector, HashSet())
        if (collector.isSatisfied) return

        val list = PsiTreeUtil.getChildOfType(this, RakuStatementList::class.java)
        if (list != null) {
            val finish = PsiTreeUtil.findChildOfType(list, PodBlockFinish::class.java)
            if (finish != null) {
                collector.offerSymbol(RakuImplicitSymbol(RakuSymbolKind.Variable, "\$=finish"))
            }
        }

        val virtualFile: VirtualFile? = originalFile.virtualFile
        val replState = virtualFile?.getUserData(RakuReplState.RAKU_REPL_STATE)
        replState?.contributeFromHistory(collector)
    }

    override fun getMetaData(): PsiMetaData {
        val finalName = getEnclosingRakuModuleName() ?: name
        return object : PsiMetaData {
            override fun getDeclaration(): PsiElement = this@RakuFileImpl
            override fun getName(context: PsiElement?): String = finalName
            override fun getName(): String = finalName
            override fun init(element: PsiElement?) {}
            override fun getDependencies(): Array<Any> = ArrayUtil.EMPTY_OBJECT_ARRAY
        }
    }

    /* Builds a map from statement start line number to the line(s) that the statement
     * spans. The start line is always included, but any line numbers where an inner
     * statement starts will be excluded (so the values may be sparse). Any line that
     * does not have an entry is not "interesting" statement (e.g. one that is meaningful
     * in coverage or could be hit by a breakpoint).
     */
    override fun getStatementLineMap(): Map<Int, List<Int>> {
        val result = HashMap<Int, MutableList<Int>>()
        val covered = HashSet<Int>()
        val application = ApplicationManager.getApplication()
        if (application.isDispatchThread && application.isWriteAccessAllowed) {
            PsiDocumentManager.getInstance(project).commitAllDocuments()
        }
        val document = viewProvider.document
        val stmts = PsiTreeUtil.getChildOfType(this, RakuStatementList::class.java)
        buildStatementLineMap(result, covered, document, stmts)
        return result
    }

    private fun buildStatementLineMap(
        result: MutableMap<Int, MutableList<Int>>,
        covered: MutableSet<Int>,
        document: Document?,
        stmts: RakuStatementList?,
    ) {
        if (stmts == null) {
            return
        }
        for (stmt in PsiTreeUtil.getChildrenOfTypeAsList(stmts, RakuStatement::class.java)) {
            // Get the start line and, if not seen already add it to the set of
            // covered statements.
            val startLine = document!!.getLineNumber(stmt.textOffset)
            val seen = covered.contains(startLine)
            var spanned: MutableList<Int>? = null
            if (!seen) {
                if (!isUncoverableDeclarator(stmt)) {
                    covered.add(startLine)
                    if (!isSymbolDeclarator(stmt)) {
                        spanned = ArrayList()
                        result[startLine] = spanned
                        spanned.add(startLine)
                    }
                }
            }

            // Visit statement lists enclosed in this file.
            findNestedStatements(result, covered, document, stmt)

            // Now add uncovered lines up to the end of this statement.
            if (!seen) {
                try {
                    val endLine = document.getLineNumber(
                        stmt.textOffset + stmt.text.replaceFirst("\\s+$".toRegex(), "").length - 1)
                    for (i in startLine + 1..endLine) {
                        if (!covered.contains(i)) {
                            covered.add(i)
                            spanned?.add(i)
                        }
                    }
                } catch (ignored: IndexOutOfBoundsException) {
                    // Code piece was updated in the middle of building statement line map,
                    // so just ignore the exception until next rebuilding
                }
            }
        }
    }

    private fun findNestedStatements(
        result: MutableMap<Int, MutableList<Int>>,
        covered: MutableSet<Int>,
        document: Document?,
        node: RakuPsiElement,
    ) {
        for (child in node.children) {
            if (child is RakuStatementList) {
                buildStatementLineMap(result, covered, document, child)
            } else {
                findNestedStatements(result, covered, document, child as RakuPsiElement)
            }
        }
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is RakuHighlightVisitor) {
            visitor.visitRakuElement(this)
        } else {
            super.accept(visitor)
        }
    }

    override fun setModuleName(moduleName: String?) {
        this.moduleName = moduleName
    }

    override fun getModuleName(): String? = moduleName

    override fun setOriginalPath(originalPath: String?) {
        this.originalPath = originalPath
    }

    override fun getOriginalPath(): String? = originalPath

    override fun setDependencyFile(dependencyFile: Boolean?) {
        this.dependencyFile = dependencyFile
    }

    override fun getDependencyFile(): Boolean? = dependencyFile

    companion object {
        @JvmField
        val VARIABLE_SYMBOLS: MutableMap<String, RakuSettingTypeId?> = HashMap()

        private val POD_HTML_TEMPLATE: String = RakuUtils.getResourceAsString("podPreview/template.html")

        private val RAKU_FILE_TYPES = arrayOf<RakuMultiExtensionFileType>(
            RakuModuleFileType.INSTANCE,
            RakuScriptFileType.INSTANCE,
            RakuTestFileType.INSTANCE,
            RakuPodFileType.INSTANCE,
        )

        init {
            // compile time variables
            VARIABLE_SYMBOLS["\$?FILE"] = RakuSettingTypeId.Str
            VARIABLE_SYMBOLS["\$?LINE"] = RakuSettingTypeId.Int
            VARIABLE_SYMBOLS["\$?LANG"] = null
            VARIABLE_SYMBOLS["%?RESOURCES"] = null
            VARIABLE_SYMBOLS["\$?PACKAGE"] = null
            // pod vars
            VARIABLE_SYMBOLS["\$=pod"] = RakuSettingTypeId.Array
            // special variables
            VARIABLE_SYMBOLS["\$_"] = null
            VARIABLE_SYMBOLS["\$/"] = RakuSettingTypeId.Match
            VARIABLE_SYMBOLS["\$!"] = RakuSettingTypeId.Exception
            // dynamic variables
            VARIABLE_SYMBOLS["\$*ARGFILES"] = null
            VARIABLE_SYMBOLS["@*ARGS"] = RakuSettingTypeId.Array
            VARIABLE_SYMBOLS["\$*IN"] = RakuSettingTypeId.IO__Handle
            VARIABLE_SYMBOLS["\$*OUT"] = RakuSettingTypeId.IO__Handle
            VARIABLE_SYMBOLS["\$*ERR"] = RakuSettingTypeId.IO__Handle
            VARIABLE_SYMBOLS["%*ENV"] = RakuSettingTypeId.Hash
            VARIABLE_SYMBOLS["\$*REPO"] = null
            VARIABLE_SYMBOLS["\$*INIT-DISTANT"] = RakuSettingTypeId.Instant
            VARIABLE_SYMBOLS["\$*TZ"] = RakuSettingTypeId.Int
            VARIABLE_SYMBOLS["\$*CWD"] = RakuSettingTypeId.IO__Path
            VARIABLE_SYMBOLS["\$*KERNEL"] = RakuSettingTypeId.Kernel
            VARIABLE_SYMBOLS["\$*DISTRO"] = RakuSettingTypeId.Distro
            VARIABLE_SYMBOLS["\$*VM"] = RakuSettingTypeId.VM
            VARIABLE_SYMBOLS["\$*PERL"] = RakuSettingTypeId.Perl
            VARIABLE_SYMBOLS["\$*RAKU"] = RakuSettingTypeId.Raku
            VARIABLE_SYMBOLS["\$*PID"] = RakuSettingTypeId.Int
            VARIABLE_SYMBOLS["\$*PROGRAM-NAME"] = RakuSettingTypeId.Str
            VARIABLE_SYMBOLS["\$*PROGRAM"] = RakuSettingTypeId.IO__Path
            VARIABLE_SYMBOLS["&*EXIT"] = null
            VARIABLE_SYMBOLS["\$*EXECUTABLE"] = RakuSettingTypeId.IO__Path
            VARIABLE_SYMBOLS["\$*EXECUTABLE-NAME"] = RakuSettingTypeId.Str
            VARIABLE_SYMBOLS["\$*USER"] = RakuSettingTypeId.IntStr
            VARIABLE_SYMBOLS["\$*GROUP"] = RakuSettingTypeId.IntStr
            VARIABLE_SYMBOLS["\$*HOMEDRIVE"] = null
            VARIABLE_SYMBOLS["\$*HOMEPATH"] = null
            VARIABLE_SYMBOLS["\$*HOME"] = RakuSettingTypeId.IO__Path
            VARIABLE_SYMBOLS["\$*SPEC"] = RakuSettingTypeId.IO__Spec
            VARIABLE_SYMBOLS["\$*TMPDIR"] = RakuSettingTypeId.IO__Path
            VARIABLE_SYMBOLS["\$*THREAD"] = RakuSettingTypeId.Thread
            VARIABLE_SYMBOLS["\$*SCHEDULER"] = RakuSettingTypeId.ThreadPoolScheduler
            VARIABLE_SYMBOLS["\$*SAMPLER"] = null
            VARIABLE_SYMBOLS["\$*COLLATION"] = RakuSettingTypeId.Collation
            VARIABLE_SYMBOLS["\$*TOLERANCE"] = RakuSettingTypeId.Num
            VARIABLE_SYMBOLS["\$*DEFAULT-READ-ELEMS"] = RakuSettingTypeId.Int
        }

        private fun htmlColor(color: Color): String = "${color.red}, ${color.green}, ${color.blue}"

        private fun isSymbolDeclarator(stmt: RakuStatement): Boolean {
            val scoped = PsiTreeUtil.getChildOfType(stmt, RakuScopedDecl::class.java)
            val declChild = PsiTreeUtil.getChildOfAnyType(
                scoped ?: stmt,
                RakuPackageDecl::class.java, RakuUseStatement::class.java, RakuNeedStatement::class.java,
                RakuSubset::class.java, RakuEnum::class.java, RakuStubCode::class.java)
            return declChild != null
        }

        private fun isUncoverableDeclarator(stmt: RakuStatement): Boolean {
            val scopedDecl = PsiTreeUtil.getChildOfType(stmt, RakuScopedDecl::class.java)
            val consider: RakuPsiElement = scopedDecl ?: stmt

            val codeChild = PsiTreeUtil.getChildOfAnyType(
                consider, RakuRoutineDecl::class.java, RakuMultiDecl::class.java, RakuRegexDecl::class.java)
            if (codeChild != null) {
                return true
            }

            val varChild = PsiTreeUtil.getChildOfType(consider, RakuVariableDecl::class.java)
            return varChild != null && !varChild.hasInitializer()
        }
    }
}
