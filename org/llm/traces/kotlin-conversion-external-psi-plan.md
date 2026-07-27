# Convert external/synthetic PSI layer to Kotlin (roadmap item 1)

## Context

Next step of the agreed Kotlin-conversion roadmap, right after the full suite went green (1034/1034, commit `ad192073`, branch `2026.1-beta.2`). The `psi/external/` package (7 Java files) plus `sdk/RakuExternalNamesParser.java` is the highest-value target because conversion here also fixes two structural problems:

1. **Implicit wire contract.** The JSON emitted by `raku-module-symbols.raku` / `raku-core-symbols.raku` (keys `k,n,t,d,m,s,r,p,x,b,a,mro,key,nn,rakudo`) is parsed with `org.json` string lookups; the contract exists only implicitly on both sides. A typed kotlinx.serialization model (house style: `ExternalMetaFile.kt`) makes it explicit and would have surfaced the "methods silently missing from role entries" class of bug immediately.
2. **Duplicated stub boilerplate.** ~60 no-op/throw overrides live in `RakuExternalPsiElement.java` (base for the 5 element classes) and a second, independent ~65-method copy inline in `ExternalRakuFile.java`.

Build is ready: Kotlin 2.3.20, `kotlin("plugin.serialization")` applied, `kotlinx-serialization-json:1.7.3` on classpath.

## Key design decisions

### JSON model: sealed interface + `JsonContentPolymorphicSerializer` on `"k"` — not `classDiscriminator`

`classDiscriminator` can't work: several `"k"` values map to one shape (`"m"/"s"/"r"` → routine, `"mm"/"c"/"ro"` → package), the routine shape also appears *nested* (non-polymorphically) inside package `"m"` arrays, and the code needs the raw `k` string (`getRoutineKind`). A `JsonContentPolymorphicSerializer` selecting on `k` handles all of this and leaves `k` readable as a plain property.

New file `src/main/java/org/raku/comma/sdk/ExternalSymbolsJson.kt`:

- `sealed interface ExternalSymbolEntry { val k: String; val n: String }` with `@Serializable` data classes: `NativeTypeEntry` (`k,n,t`), `VariableEntry` (`k,n,t,d?`), `EnumOrSubsetEntry` (`k,n,t,d?`), `RoutineEntry` (`k,n,m:Int,s:SignatureJson,d?,x?,p:Boolean=false,rakudo:Boolean=false`), `PackageEntry` (`k,n,t,b,key?,d?,mro:List<String> = emptyList(),m:List<RoutineEntry>? = null,a:List<AttributeEntry> = emptyList()`).
- `AttributeEntry(n,t,d?)`, `SignatureJson(r, p:List<ParameterJson> = emptyList())`, `ParameterJson(n,t,nn:List<String> = emptyList())`.
- `PackageEntry.m` **must be nullable**: `"mm"` entries emit literal `"m":null` (verified in `CORE.fallback`). Parser uses `entry.m.orEmpty()`.
- Local lenient config per house style: `Json { ignoreUnknownKeys = true; isLenient = true }`.
- Decode the top-level array element-by-element: unknown `k` → silent skip (matches Java's switch default); decode failure → warn + skip that element. **This is the only permitted behavior delta** vs Java (which aborts the remainder of the array on first error) — strictly more tolerant; covered by a test and called out in the commit message.

### PSI structure: keep the base class, reject interface delegation

`RakuExternalPsiElement` becomes an `abstract` Kotlin class, same FQN (it's `instanceof`-checked in `RakuInspection.kt:25`, `RakuDocumentationProvider.java`, `RakuLineMarkerProvider.java`, `RakuParameterInfoHandler.java` — the named supertype must survive). Delegation loses: identity-returning members (`getContainingFile()`/`copy()`/`getOriginalFile()` return `this`) are delegation traps, and every project-dependent method would need re-overriding on top of the delegate anyway. `ExternalRakuFile` stays a standalone class implementing `RakuFile` with a clearly-marked region of one-line expression-body stubs; no second stub base for a single class.

## Commit plan (one subsystem per commit; full suite green after each)

Order is inverted from "parser first": the parser hands `org.json.JSONObject` into `ExternalRakuRoutineDecl`/`ExternalRakuSignature` constructors, so converting elements first (keeping `JSONObject` params temporarily) means the later parser commit changes constructor types in already-Kotlin files instead of editing Java.

### Commit 0 — Characterization tests (safety net)
New `src/test/kotlin/org/raku/comma/sdk/RakuExternalNamesParserTest.kt` extending `CommaFixtureTestCase`, using the **String** parser constructor (survives the API change) and asserting only through PSI interfaces (`RakuRoutineDecl`, `RakuPackageDecl`, …):
- Per-kind snippets: `"n"`, `"v"` (docs `"a\nb"` → `getDocsString() == "a<br>b"`), `"m"/"s"/"r"` (scope has/our; `m:0`→only, `m:1`→multi; `s.r="Str:D"` → return type `Str`; `x`→deprecated; `p`→pure; `rakudo`→implementation detail), `"e"/"ss"` (→ packageKind class), `"mm"` then `"c"` (metaclass linked via `metamodelCache[packageKind]`; `"m":null` tolerated), nested routines + attributes (MOP contribution: `.method` aliases, `!private` gating, `$.attr` accessors).
- Parameter semantics via signature params: `*%_`, `:$x`, `$x?`, `$x!`, `nn` → `getVariableNames()`.
- Garbage input → empty result; `"[]"` → empty; `CORE.fallback` round-trip pinning exact symbol count + resolving `Any` with satisfied multiness routing.
- Do NOT yet add a "bad element mid-array" test (expected behavior changes in Commit B).

### Commit A — Convert base + 5 element classes
`RakuExternalPsiElement`, `ExternalRakuPackageDecl`, `ExternalRakuRoutineDecl`, `ExternalRakuVariableDecl`, `ExternalRakuParameter`, `ExternalRakuSignature` → Kotlin. Same FQNs, same API; `org.json.JSONObject` ctor params kept temporarily in RoutineDecl/Signature. Zero caller changes.

### Commit B — Typed model + parser conversion
Add `ExternalSymbolsJson.kt`; convert `RakuExternalNamesParser` to Kotlin. API (parser is called **only from Kotlin** — `RakuProjectSdkService.kt`, `CoreSettingDiagTest.kt`):
- Constructors `(project, file, jsonText: String)` and `(project, file, entries: List<ExternalSymbolEntry>)`; **drop** the `JSONArray` overload.
- `companion object { fun tryDecode(text: String): List<ExternalSymbolEntry>? }` replaces the "parse-to-validate before caching" idiom at `RakuProjectSdkService.kt:433-440`.
- Keep `parse()` fluent, `result()`, `getPackages()` (dead but trivial).
- Swap `ExternalRakuRoutineDecl`/`ExternalRakuSignature` ctor params `JSONObject → SignatureJson`; update `RakuProjectSdkService.kt` call sites (~328, 367, 433-440) and `CoreSettingDiagTest.kt:10-14`; drop `org.json` imports there.
- Add the "bad element skipped, rest survives" test.

### Commit C — Convert `ExternalRakuFile`
No API change (`RakuFileImpl.java:376` and `RakuProjectSdkService.kt` construct it identically).

## Risk flags per file (the four known conversion bug patterns apply throughout)

- **RakuExternalPsiElement**: keep `override fun getDocsString()` (dropping it falls back to `RakuDocumented`'s default which walks real PSI → crash on synthetics); `setDocs` keeps `\n→<br>`; null-returning stubs keep nullable types, **never `!!`**; mutators throw `IncorrectOperationException`, not `TODO()`.
- **ExternalRakuPackageDecl**: preserve three verified quirks verbatim — (1) getters pool computed only in the long ctor from `attrs`, and `setAttributes()` (which the parser actually uses) does *not* recompute it, so getter suppression never fires at runtime — do not "fix"; (2) no `isSatisfied()` check inside the MRO loop (only after routines/attrs loops); (3) `getSignature()` stays a TODO stub. `setName` keeps assignment *and* returns null. `inferType()` uses `myType` (JSON `t`), not `myName`.
- **ExternalRakuRoutineDecl**: `:D`/`:U` trim is conditional single-suffix drop; keep dead `"sm"`/`"submethod"` branches; `offerSymbol` vs `offerMultiSymbol(sym, false)` routing — offers are statements (discarded-return-value pattern); avoid a clashing `signature` property vs `getSignature(): String`; `isImplementationDetail` as `var` (read property-style from `RakudoImplementationDetailInspection.kt`).
- **ExternalRakuVariableDecl**: guard order + `isSatisfied` early-returns between offers; `Char + String` concat must not become arithmetic.
- **ExternalRakuParameter**: `NAME_PATTERN = "([|$@%&\\w+...])"` regex into a companion; prefix/suffix flag logic (`*%`, `:`, `*`, `+`, `!`, `?`) verbatim; keep `getText()` override returning `myName`.
- **ExternalRakuSignature**: `summary()` string shape exact (`", "` join, `" --> "` return); intentionally no `getName()` override.
- **RakuExternalNamesParser**: single-pass order-dependence — `"mm"` fills `metamodelCache` before `"c"/"ro"` looks up by `psi.packageKind` (key is `"class"`/`"role"`, counterintuitive — do **not** "correct" to `entry.key`); `"mm"` sequence exactly cache→setName(key)→externalClasses→result; top-level routine scope `k=="m" ? "has" : "our"`, nested always `"has"`; `"e"/"ss"` constructed with kind `"c"`, base `"A"`.
- **ExternalRakuFile**: `contributeGlobals` multiness routing + `isSatisfied` after *every* offer; identity returns `this` (`getContainingFile`/`copy`/`getOriginalFile`); `getName()`/`toString()` = `myFile.name` (docs provider compares to `SETTING_FILE_NAME`; `CallArgGotoElementProviderBase` does `startsWith("Cro::WebApp::Template")`); keep exception split (file stubs → `UnsupportedOperationException`, except `checkSetName` → `IncorrectOperationException`); `myViewProvider` stored nullable, returned via `!!` in the `@NotNull` override (noted in commit).

## Must NOT change

Wire contract keys and both Raku emitter scripts; `CORE.fallback`; FQNs of all converted classes; `build.gradle.kts` (org.json stays — used by other subsystems); `RakuProjectSdkService` caching/threading flow (only call shapes); the behavioral quirks listed above.

## Verification

Every test shell needs `eval "$(rakubrew init Zsh)"` first, else all tests fail in setUp.

- After Commit 0 and each of A/B/C: full `./gradlew test` (expect 1034 + new tests green; known pre-existing order-dependent leak-detector flake may fire on an arbitrary late test).
- Targeted while iterating: `./gradlew test --tests "org.raku.comma.sdk.RakuExternalNamesParserTest"` and `--tests "org.raku.comma.CoreSettingDiagTest"`.
- After Commit C additionally: `./gradlew test --tests "org.raku.comma.completion.*" --tests "org.raku.comma.docs.*"` (heaviest exercisers of `contributeGlobals`/MOP paths).
