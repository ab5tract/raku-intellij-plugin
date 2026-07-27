# Symbol-contribution walks: redesign + Kotlin conversion (roadmap item 2)

## Context

`RakuFileImpl.java` (622 lines) and `RakuPackageDeclImpl.java` (608 lines) are the load-bearing wall for completion, resolution, find-usages, and docs. The stub-vs-AST traversal duplication exists in **four** places, with behavioral drift between branches; the `sub EXPORT` path combines an async contribution to potentially-abandoned collectors with an `AtomicBoolean` CAS that silently skips work under contention. This is the "redesign once, deliberately" item from the roadmap.

## Verified facts that drive the design

1. **`isExported()` is AST-walking.** The `RakuPsiDeclaration` default calls `getTraits()` → `PsiTreeUtil` over children. The stub branches read `stub.isExported()` precisely to avoid forcing a parse of indexed dependency files. Therefore the redesign must NOT collapse to "map `stub.getPsi()` and dispatch on PSI interfaces" — cheap facts (scope, exported, name, packageKind, typeName) must come through a stub-or-AST lens, with PSI materialized only at contribution points (which is what both current branches already do).

2. **Branch divergences in `contributeGlobals`** (RakuFileImpl:229-367):
   - Enum gate: stub `isExported() || our` vs AST `our` only → unify to `isExported() || our`.
   - `sub EXPORT` special-case exists only in the AST branch → after the EXPORT redesign, both lenses trigger it.
   - Descent rules differ legitimately (stub tree contains only indexed decls; AST walk stops at `RakuPsiScope` boundaries) → keep per-lens structure rules.
   - Package top-name: stub `getTypeName()` vs AST `getName()` → keep per-lens.

3. **`contributeFromElders` is algorithm-split, not just accessor-split**: stub branch resolves trait names via `RakuLexicalTypeStubIndex`/`RakuGlobalTypeStubIndex`; AST branch via `PsiReference.resolve()`. Keep the two resolution strategies behind a `resolvedParents()` seam; write the contribution *ordering* (local parents → external parents → Cursor/Any/Mu chain, with `does()`/`is()` and `decreasePriority()`) once.

4. **EXPORT machinery** (RakuFileImpl:331-390): `EXPORT_CACHE` field + `isCalculatingExport` CAS + `executeOnPooledThread` handing symbols to a collector the walk already abandoned. `dropExportCache()` is called by `RakuRoutineDeclImpl.java:427` via downcast — keep the name.

5. Test-fixture pattern for cross-module behavior: `RakuMultiModuleProjectDescriptor` + `testData/multi-module` + `copyFileToProject("...", "../lib/...")` (see `MultiModuleCompletion.testCrossModules`).

## Design

### Unified walk with a facts lens

New Kotlin sealed abstraction (lives with the impls, `org.raku.comma.psi.impl`):

```kotlin
sealed interface WalkNode {
    fun children(): List<WalkNode>          // stub: childrenStubs; AST: RakuPsiElement children with scope-boundary rule
    val category: Category?                 // Variable / Package / Routine / Enum / Subset / Use / Need / Other
    fun scope(): String                     // stub-first
    fun exported(): Boolean                 // stub-first (never forces AST for stubbed nodes)
    fun name(): String?
    fun psi(): RakuPsiElement               // materialize only at contribution points
}
class StubWalkNode(...) : WalkNode
class AstWalkNode(...) : WalkNode
```

`contributeGlobals`, `contributeInternals`, `contributeNestedPackagesWithPrefix` each become ONE BFS written against `WalkNode`. `contributeFromElders` keeps its two parent-resolution strategies behind a seam but single contribution ordering.

### EXPORT: explicit single-flight future

Replace cache+CAS+fire-and-forget with one `AtomicReference<CompletableFuture<List<RakuSymbol>>>`:
- future completed → contribute synchronously to the live collector;
- absent → single-flight create + schedule on pooled thread; on completion call `RakuSdkUtil.triggerCodeAnalysis(project)` so abandoned resolutions re-run (this is the piece that fixes "cold cache = symbols never appear until something else invalidates");
- pending → contribute nothing (same observable as today's cold path, minus the silent CAS skip).
- `dropExportCache()` clears the reference (keeps the Java caller working).
- After redesign, the EXPORT trigger fires from both lenses, not just AST.

## Commit plan

- **Commit 0 — characterization tests.** New `SymbolContributionWalkTest` using the multi-module fixture: our-sub/our-class transitive via `use`; `need` contribution; nested-package prefix (`Module::Inner::Deep`); exported-but-not-our enum from a stubbed dependency (pins the gate we're unifying to); a `sub EXPORT` module fixture pinning that nothing crashes and nothing contributes synchronously cold (strengthened in Commit C).
- **Commit A — `RakuPackageDeclImpl` → Kotlin** with unified `contributeInternals` + `contributeNestedPackagesWithPrefix` (single BFS over WalkNode) and `contributeFromElders` behind the parents seam. Same FQN; `PsiMetaOwner` and stub-index usage unchanged.
- **Commit B — `RakuFileImpl` → Kotlin** with unified `contributeGlobals` (single BFS), enum gate unified to `isExported() || our`, EXPORT semantics preserved verbatim (still AST-lens-only, still cache+CAS) to keep the commit mechanical-equivalent.
- **Commit C — EXPORT redesign** as above + tests that resolution works once the future completes, and that the trigger fires from the stub lens too.

Verification per commit: `MethodCompletionTest` (139 tests), `GoToDeclarationTest`, `FindUsageTest`, `DocumentationTest`, `MultiModuleCompletion`, the new walk tests, then full suite at the end. Green bar = no failures beyond the 6 known pre-existing + roaming leak flake. `eval "$(~/.rakubrew/bin/rakubrew init Zsh)"` before every gradle run.

## Must not change

- FQNs `org.raku.comma.psi.impl.RakuFileImpl` / `RakuPackageDeclImpl` (downcasts exist, e.g. `RakuRoutineDeclImpl.java:427`).
- `STUB_VERSION` stays 29 (no stub serialization changes in this item).
- Collector interface + both implementations (their asymmetric `isSatisfied` semantics are consumed everywhere; redesigning collectors is out of scope here).
- `MOPSymbolsAllowed.does()/is()` semantics; the Cursor/Any/Mu implicit chain ordering; `decreasePriority()` call points.
- `dependencyFile` early-return in `contributeGlobals` (known wart, but load-bearing; do not remove in this item).
