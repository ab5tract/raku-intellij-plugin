# Arity matcher / lexical resolution: exploration + plan (roadmap item 3)

## Exploration report (full, from the research agent)

### 1. `RakuSignature.java`

Path: `src/main/java/org/raku/comma/psi/RakuSignature.java` (231 lines)

Full interface member listing:

- `String summary(RakuType type)` — abstract (line 11)
- `RakuParameter[] getParameters()` — abstract (line 12)
- `default SignatureCompareResult acceptsArguments(PsiElement[] argsArray, boolean isCompleteCall, boolean isMethodCall)` — lines 14-105 — **the arity matcher**
- `default String prepareParamName(String variableName)` — lines 107-128 (strips sigils/twigils/`!`/`?` off a named-parameter variable name for comparison)
- `default int eatPositionalSlurpy(...)` — lines 130-139
- `default void eatNamedSlurpy(...)` — lines 141-152
- `default void failMatch(SignatureCompareResult result, Pair<Integer, MatchFailureReason>... failures)` — lines 154-159
- `default void categorizeArguments(List<PsiElement> arguments, List<PsiElement> positionalArgs, Map<String, PsiElement> namedArgs)` — lines 161-169 (splits call arguments into positional vs. `RakuColonPair`/`RakuFatArrow` named args)
- `enum MatchFailureReason` — lines 171-182: `NOT_ENOUGH_ARGS, TOO_MANY_ARGS, SURPLUS_NAMED, TYPE_MISMATCH, CONSTRAINT_MISMATCH, MISSING_REQUIRED_NAMED` (plus a mutable `@Nullable public String name` field bolted onto the enum for the missing-named-arg's name — flagged below as a real bug, not just a smell)
- `class SignatureCompareResult` — lines 184-230, a small mutable result object (`isAccepted`, `argToParam` map, `failures` map, `nextParameterIndex`) with getters/setters.

**The arity-matching default method** (lines 14-105) walks parameters left to right, splitting call arguments into positional/named buckets first (`categorizeArguments`), then for each parameter: a leading `|`/`|c` parameter accepts everything and short-circuits; positional-slurpy eats all remaining positional args; named-slurpy eats all remaining named args; otherwise matches the next positional arg or the named arg matching `prepareParamName(variableName)`, and records `NOT_ENOUGH_ARGS`/`MISSING_REQUIRED_NAMED` failures for required-but-missing parameters when `isCompleteCall`. After the loop, surplus named args (unless it's a method call, which tolerates them) and surplus positional args are flagged.

**Call sites for `acceptsArguments` (every one, repo-wide):**

1. `src/main/java/org/raku/comma/inspection/inspections/CallArityInspection.kt:61`
2. `src/main/java/org/raku/comma/RakuParameterInfoHandler.java:151`
3. `src/main/java/org/raku/comma/cro/template/CroTemplateParameterInfoHandler.java:62` — calls the *sibling* `CroTemplateSignature.acceptsArguments` (a separate, independent matcher for Cro templates, not sharing code with `RakuSignature`'s — see item 7 below)
4. `src/test/kotlin/org/raku/comma/signatures/RakuSignatureComparatorTest.kt:33` — direct unit test
5. `tests/edument/rakuidea/signatures/RakuSignatureComparatorTest.java:22` — legacy duplicate under a `tests/` tree that is **not wired into `build.gradle.kts`/`settings.gradle.kts`** (no sourceSet points at it; last touched Sep 2024) — dead scaffolding, safe to ignore.

Implementors of `RakuSignature`: `RakuSignatureImpl` (native PSI, Java, adds no logic beyond the interface) and `ExternalRakuSignature.kt` (already converted in item 1) — both get `acceptsArguments` for free via the default method.

### 2. `RakuPsiElement.java`

Path: `src/main/java/org/raku/comma/psi/RakuPsiElement.java` (183 lines). 12 default methods: `getEnclosingRakuModuleName` (28-51), `resolveLexicalSymbol` (53-57), `resolveLexicalSymbolAllowingMulti` (59-64), `getLexicalSymbolVariants` (66-70), `applyExternalSymbolCollector` (72-97), `applyLexicalSymbolCollector` (99-110), `inferType` (112-115), `getSelfType` (117-145), `skipWhitespacesBackward`/`Forward` (147-167), `collectPodAndDocumentables` (169-177), `inferEffects` (179-182).

Call-site survey: lexical-resolution consumers are **almost entirely Java** reference classes (`RakuIsTraitReference`, `RakuRegexCallReference`, `RakuTypeNameReference`, `RakuMethodReference`, `RakuSubCallReference`, `RakuVariableReference`, `RakuOpReference`, plus refactoring handlers) — roughly 20+ Java call sites vs. only 4-5 Kotlin ones (`UndeclaredVariableInspection.kt`, `RakuProjectSdkService.kt`, `ExternalRakuPackageDecl.kt`, `RakuPackageDeclImpl.kt`, one test).

### 3. `RakuParameter.java` and implementations

Interface (28 lines, all abstract, no default methods): `summary`, `getVariableName`, `getInitializer`, `isPositional`, `isNamed`, `getWhereConstraint`, `getValueConstraint`, `isSlurpy`, `isRequired`, `isOptional`, `isExplicitlyOptional`, `isCopy`, `isRW`, `isRaw`, `equalsParameter`.

Facts the matcher actually consults: `parameter.getText()` (leading `|` check), `isPositional()`, `isSlurpy()`, `isOptional()`, `isNamed()`, `getVariableName()`.

Two implementations, semantics compared and confirmed aligned:
- `RakuParameterImpl.java` (native PSI, 300 lines) — reads quantifier tokens/child PSI (`PARAMETER_QUANTIFIER`, `RakuNamedParameterImpl`, `RakuParameterDefault`).
- `ExternalRakuParameter.kt` (already converted, item 1) — reads prefix/suffix off the raw name string (`*%`, `:`, `*`, `+`, `!`, `?`).
One real difference: native `isOptional()` also treats presence of a default-value initializer as optional; `ExternalRakuParameter` has no initializer concept (always null) so that trigger is inapplicable there — not a bug, just a different substrate with fewer ways to be optional.

### 4. `CallArityInspection` and other consumers

`src/main/java/org/raku/comma/inspection/inspections/CallArityInspection.kt` (125 lines, already Kotlin) does not reimplement matching — it resolves the call to candidate `RakuRoutineDecl`s, calls `signature.acceptsArguments(args, true, element is RakuMethodCall)` per candidate (line 61), and turns `MatchFailureReason` values into problem annotations. For `MISSING_REQUIRED_NAMED` it reads `reason.name` (line 87) — the mutated-enum-singleton field described below.

`RakuParameterInfoHandler.java:151` also just calls `acceptsArguments` directly (for the parameter-info popup, doesn't touch `.name`). `CroTemplateParameterInfoHandler.java:62` uses the independent `CroTemplateSignature.acceptsArguments`.

### 5. Existing test coverage

- `src/test/kotlin/org/raku/comma/signatures/RakuSignatureComparatorTest.kt` — 9 test methods covering positional/named/slurpy/surplus/required-named/incomplete-call cases. Strong existing harness; reused almost as-is for the extraction.
- `tests/edument/rakuidea/signatures/RakuSignatureComparatorTest.java` — dead duplicate (see above), ignore.
- `CallArityInspection`: no dedicated test class; exercised via `AllRakuInspections.kt` plus two methods in the large `AnnotationTest.kt` (`testCallArityMismatchAnnotating` line 1132, `testCallArityMismatchAnnotatingOnAccessorCall` line 1163) using `testData/annotation/CallArity.pm6` / `CallArityExtended.pm6`.
- `src/test/kotlin/org/raku/comma/parameterInfo/RakuParameterInfoTest.kt` — 9 tests.
- `src/test/kotlin/org/raku/comma/reference/GoToDeclarationTest.kt` — 25 tests, primary lexical-resolution coverage (all indirect, through PSI reference resolution — no unit test targets `applyLexicalSymbolCollector`/`applyExternalSymbolCollector` directly).

### 6. Other large default-method interfaces in `org.raku.comma.psi` (for a future session)

`RakuPsiDeclaration.java` (5 defaults, 87 lines — `getGlobalName()` walks up enclosing packages), `RakuPsiScope.java` (3 defaults, 58 lines — BFS-shaped, tightly coupled to the lexical-resolution walk, a natural pairing with a future `RakuPsiElement` conversion), `RakuDocumented.java` (2 defaults, 94 lines — self-contained doc-comment-gathering walk), `RakuSignatureHolder.java` (2 defaults, 39 lines — thin, direct consumer of `RakuSignature.summary`).

**Second independent arity matcher**: `src/main/java/org/raku/comma/cro/template/psi/CroTemplateSignature.java` (94 lines) reimplements the same algorithm shape for Cro web-template parameters against `CroTemplateParameter`, reusing `RakuSignature.SignatureCompareResult`/`MatchFailureReason` as data types only. Not touched by this item; noted for later generalization if ever wanted.

### 7. Build facts

No new Kotlin build config needed — Kotlin 2.3.20 + existing `kotlin.stdlib.default.dependency=false` setup (from items 1-2) is sufficient. This is pure logic extraction, no serialization involved.

---

## Design decision: extract, don't convert the interface

**`RakuSignature.java` and `RakuPsiElement.java` stay Java.** Converting either to a Kotlin interface risks a real Java-interop hazard: Kotlin interface default methods compiled in the project's current mode (no `-Xjvm-default` setting found in `build.gradle.kts` → Kotlin's `disable` default) generate an abstract method plus a separate `$DefaultImpls` companion, and **Java classes implementing the interface do not automatically inherit the Kotlin-compiled default** — they'd need an explicit override or hit `AbstractMethodError` at runtime. `RakuSignatureImpl.java` (Java, relies on the inherited default today) and the ~20+ Java implementors/callers of `RakuPsiElement` would all be at risk. No existing Kotlin interface with a default method implemented by a Java class exists anywhere in this codebase yet, so there's no established, verified-safe precedent to build on.

Per the roadmap's own phrasing — "the arity matcher... should become a **standalone, directly unit-testable Kotlin class**" — extraction was always the intended shape, not a file-for-file interface conversion. So:

1. **New file `RakuArityMatcher.kt`** (same package, `org.raku.comma.psi`, as a `object` with `@JvmStatic` — the established idiom in this codebase, e.g. `RakuSdkUtil`) holds the full algorithm: `acceptsArguments`, `prepareParamName`, `eatPositionalSlurpy`, `eatNamedSlurpy`, `failMatch`, `categorizeArguments`, all as private helpers except the public entry point.
2. **`RakuSignature.java`'s default `acceptsArguments` shrinks to a one-line delegation**: `return RakuArityMatcher.acceptsArguments(this, argsArray, isCompleteCall, isMethodCall);`. The other five default methods are deleted from the interface entirely (nothing outside `RakuSignature.java` called them — verified by repo-wide grep). `MatchFailureReason` and `SignatureCompareResult` stay as nested types on `RakuSignature.java` (plain data types, not default methods — zero interop risk) so every existing import/reference (`RakuSignature.MatchFailureReason`, `RakuSignature.SignatureCompareResult`) keeps working unchanged.
3. **`RakuPsiElement.java` is left for a later session.** Its default methods are individually small (2-10 lines) and don't "hide" complexity the way the 90-line arity matcher did — the real value in touching it is bundling it with `RakuPsiScope.java` (structurally the same BFS-over-scopes shape) as a dedicated, larger interop-risk-assessed conversion, not something to rush alongside this item.

## A real bug found and fixed along the way

`RakuSignature.java:81-83` (current, pre-fix):
```java
MatchFailureReason failReason = MatchFailureReason.MISSING_REQUIRED_NAMED;
failReason.name = parameter.getVariableName();
failMatch(result, new Pair<>(posArgIndex, failReason));
```
`MatchFailureReason.MISSING_REQUIRED_NAMED` is a **single shared enum instance**. Setting `.name` on it mutates global state: if a signature has two missing required named parameters, the second `failMatch` call overwrites `.name` before `CallArityInspection.kt:87` ever reads it, so **both** failures report the same (last) parameter's name. This is exactly the "logic hides from review" failure mode the roadmap called out — it was invisible inside a one-line-looking mutation buried in a 90-line default method.

**Fix**: move the per-failure name out of the enum and into `SignatureCompareResult`, keyed by argument index — the same place every other piece of per-failure detail already lives:
```java
// SignatureCompareResult, new members
private final Map<Integer, String> failureDetails = new HashMap<>();
public void setFailureDetail(int argIndex, @Nullable String detail) { if (detail != null) failureDetails.put(argIndex, detail); }
@Nullable public String getFailureDetail(int argumentIndex) { return failureDetails.getOrDefault(argumentIndex, null); }
```
`MatchFailureReason` loses its mutable `name` field entirely (confirmed no other reader: only `CallArityInspection.kt:87` used `.name`; `CroTemplateSignature.java` never sets or reads it). `CallArityInspection.kt` changes to `result.getFailureDetail(i)`. A new characterization test in `RakuSignatureComparatorTest.kt` pins two simultaneous missing required named parameters resolving to two distinct, correct detail strings — impossible to assert correctly before this fix.

## Commit plan

- **Single commit** (this item is small enough not to need the multi-commit staging of items 1-2): add `RakuArityMatcher.kt`, shrink `RakuSignature.java` to the two abstract methods + one-line delegating default + the two nested types (with the `failureDetails` addition and `name`-field removal), update `CallArityInspection.kt`'s one read site, extend `RakuSignatureComparatorTest.kt` with the two-missing-named-args regression case.
- Verify: `RakuSignatureComparatorTest`, `RakuParameterInfoTest`, `AnnotationTest` (arity-specific methods at minimum, ideally the full class since it's already a known-flaky-adjacent suite), then a full-suite run for the final sign-off against the established baseline (6 pre-existing failures + roaming leak flake).

## Must not change

- `RakuSignature`/`CroTemplateSignature` public API shapes (`acceptsArguments` signature, `SignatureCompareResult`/`MatchFailureReason` as nested types with their existing getters).
- `RakuParameter` interface and both implementations (native `RakuParameterImpl.java`, `ExternalRakuParameter.kt`) — untouched, the matcher only consumes their existing facts.
- `CroTemplateSignature.java` — independent, not refactored to share the new class in this pass.
- The `RakuSignatureComparatorTest.kt` existing 9 assertions (behavior-preserving except the one bug fix, which has no existing test pinning the old broken behavior to break).
