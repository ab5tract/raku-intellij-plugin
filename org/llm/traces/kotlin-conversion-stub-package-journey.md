# Converting `psi/stub` to Kotlin: journey notes

Working notes on the `psi/stub` package conversion (roadmap item: the last
"mostly mechanical, still needs care" item). Companion to the plan at
`~/.claude/plans/cheeky-napping-nova.md` — this doc is the *why*, the plan is
the *what/when*. ~56 files, one commit per PSI kind (interface + impl +
element type together), lightest-risk kinds first, file-level machinery
(where `STUB_VERSION` lives) last.

## The shape of the package

Four layers, each with a distinct job:

- **Stub interfaces** (`RakuConstantStub`, `RakuVariableDeclStub`, ...) — the
  public contract. Tiny: a handful of accessor methods.
- **`*StubImpl` classes** (`psi/stub/impl/`) — dumb data holders. Each extends
  `StubBase<T>`, stores a few constructor-supplied fields, returns them.
- **`*StubElementType` classes** — the only place serialization actually
  happens. `createStub`/`createPsi` (stub ↔ real PSI), `serialize`/
  `deserialize` (stub ↔ bytes), `indexStub` (stub → search index), and
  `shouldCreateStub` (should this AST node get a stub at all).
- **Stub indices** (`psi/stub/index/`) — `StringStubIndexExtension`
  subclasses, each with its own independent `INDEX_VERSION`.

The interfaces form a small hierarchy: `RakuDeclStub<T>` (scope +
exported-ness) is the base for most declaration kinds; `RakuTypeStub<T>`
extends it and adds `getGlobalName()`/`getLexicalName()` as real default
methods (not just accessors) for the three package-like kinds (`class`/
`role`/`grammar` via `RakuPackageDeclStub`, `enum`, `subset`).

## Why `StringRef` exists

`StubOutputStream`/`StubInputStream` aren't just wrapped `DataOutputStream`/
`DataInputStream` — they're constructed with an `AbstractStringEnumerator`:

```java
public StubOutputStream(OutputStream out, AbstractStringEnumerator enumerator)
public StubInputStream(InputStream in, AbstractStringEnumerator enumerator)
```

That enumerator is a shared, persistent string-interning table. `writeName(String)`
doesn't write the string's bytes at all if it's seen that exact string before —
it writes a small integer ID into the enumerator's table instead. `readName()`
returns a `StringRef`, a *lazy* handle that resolves to the real string
(`getString()`) either immediately or by looking up the ID later. This is why
`writeName`/`readName` are paired (not `writeUTFFast`/`readUTFFast`, the other
pair on those classes, which do plain modified-UTF-8 with no interning).

The tradeoff `writeName` is for: package/module/variable/routine *names* repeat
constantly across a codebase's stub trees (`Str`, `Int`, `$self`, `new`, ...),
so interning them once is a real space win. Free-text payloads that are
unlikely to repeat (like `RakuSubCallStub`'s Cro-framework metadata values)
use `writeUTFFast`/plain `writeUTF` instead — nothing to intern.

Practical fallout for the conversion: every `deserialize()` that calls
`dataStream.readName()` gets back a `StringRef?`, not a `String?` — Kotlin
sees the nullable platform return type honestly, where Java's un-annotated
`StringRef` return let call sites pretend it couldn't be null. Every
`deserialize()` in this package has to decide, explicitly, what "the name
storage doesn't have this ID" actually means for that field — see below.

## The recurring interop trap: Kotlin doesn't auto-property its own funs

Kotlin maps a **Java** getter (`String getFoo()`) to a synthetic property
(`.foo`) automatically. It does **not** do this for a getter-shaped function
it compiles from **Kotlin source** — `fun getFoo(): String` stays a plain
function, callable only as `.getFoo()`.

Existing Kotlin call sites in this codebase (written back when these stub
interfaces were Java) universally used the property form — `stub.moduleName`,
`node.scope`, `entry.value`, etc. — because that's what the synthetic mapping
made idiomatic at the time. The moment an interface converts, every such call
site breaks, and — this was the surprising part — it breaks **transitively**:
converting just `RakuDeclStub` (the base) broke property-syntax access to
`.scope`/`.isExported` through five different *still-Java* subtypes
(`RakuVariableDeclStub`, `RakuPackageDeclStub`, `RakuRoutineDeclStub`,
`RakuEnumStub`, `RakuSubsetStub`), because the mapping is keyed on where the
method is *actually declared*, not which interface you access it through.

Decided early (during `RakuTraitStub`, the first conversion) to keep every
converted interface method as a plain `fun getX(): T`, matching the
already-established house style from `RakuSubCallStubElementType.kt` (the one
file converted before this effort started), rather than switching to Kotlin
`val`/property declarations that would have stayed call-site-compatible with
zero fixes. Consistency within the package won out over saving some grep-and-fix
effort — the compiler catches every break loudly (never a silent bug), so the
cost of the stricter style is bounded and visible, not a real risk.

Practical process note this produced: grep **both** `src/main/` and
`src/test/` for property-syntax usage before compiling each commit. Missed
`src/test/` twice early on (commits 3 and 4) because Gradle's incremental
Kotlin compilation didn't re-flag the break until a later, unrelated
recompile — don't trust "it compiled last time" as proof a test file wasn't
affected by an interface change since.

## Nullability: matching the existing contract, not just the wire format

Every `deserialize()` in this package has at least one spot where the
original Java quietly allowed a code path Java's type system couldn't see
(a `String`-typed field that could actually hold `null`, a `List<String>`
whose loop could `add(null)`). Kotlin forces each of these into the open.
The resolution wasn't one rule — it was a case-by-case judgment call, weighed
against **what the surrounding code already assumes**:

- **`RakuTraitStub`/`RakuTypeNameStub`** — Java dereferenced the read value
  with no null check at all (`ref.getString()`, `typename.toString()`). This
  is "null was never a real possibility, just unchecked" — faithfully ported
  as a crash-on-null via `Objects.requireNonNull<StringRef?>(...)`, *not*
  Kotlin's bare `!!` (matching the one pre-existing Kotlin precedent's style)
  and *not* a new silent null-tolerant path (Kotlin's `Any?.toString()` is
  null-safe by default and would have silently turned a missing name into the
  literal string `"null"` — a real behavior change hiding in what looks like
  a mechanical port).
- **`RakuUseStatementStub`** — Java's ternary (`ref == null ? null : ...`)
  is explicit, deliberate null-tolerance, and the consuming PSI code
  (`RakuUseStatementImpl.getModuleName(): String?`) already treated it as
  legitimately nullable. Went nullable (`String?`) all the way through —
  contained to one field, cheap to do faithfully.
- **`RakuNeedStatementStub`** — same per-element null-tolerance shape as
  `UseStatementStub`, but `GlobalsFacts.Need(val moduleNames: List<String>)`
  and its consumers already assume non-null elements. Making the list
  `List<String?>` would have rippled into unrelated code for a rare
  corrupted-stream edge case. Chose containment: kept `List<String>`
  everywhere, used `Objects.requireNonNull` per element instead.
- **`RakuConstantStub`** — the Java field was `@Nullable`-annotated. Not a
  judgment call at all; just carried the existing contract through as
  `String?`.
- **`RakuRegexDeclStub`** — Java used `assert regexNameRef != null;` (a
  statement that's a no-op unless `-ea` is set) before dereferencing. Read as
  "this is genuinely expected to always be present," not "null is tolerated"
  — ported the same way as Trait/TypeName's unchecked case.

The general principle that fell out of this: **prefer the option with the
smallest blast radius that's still honest about the one call site that
matters**, rather than mechanically propagating nullability outward just
because the wire format could theoretically produce it. A rare
corrupted-index edge case doesn't justify making a dozen unrelated call sites
handle a null they've never had to think about.

One case forced a fix at the call site rather than in the stub itself:
`RakuSubCallStub.getAllFrameworkData()` had to become `Map<String?, String?>`
to match what the *already-Kotlin* `RakuSubCallStubElementType.kt` actually
constructs (`HashMap<String?, String?>`) — that surfaced two downstream
consumers (`indexStub`, `RakuSubCallImpl.getPresentation()`) copying entries
into a `MutableMap<String, String>` without a null check. Used `!!` there,
same reasoning as the roadmap's earlier `RakuPsiElement` conversion: making a
pre-existing implicit assumption explicit is a faithful fix, not a new
defensive branch.

## `STUB_VERSION` discipline

One constant (`RakuFileElementType.STUB_VERSION`, currently 29) governs the
serialized shape of the *entire* stub tree. Bump it and every user's cached
index silently invalidates on next open; forget to bump it when the shape
really changed and you get phantom/stale data with no error anywhere. There's
no test that catches a wrong call either way (closed part of that gap with
`RakuStubSerializationTest.kt`'s round-trip test, added as commit 0, but it
only catches *this session's* regressions, not a genuine future shape change
someone forgets to version-bump).

Policy: this conversion is a pure transliteration — same fields, same order,
same wire types — so it should never need a bump. If a file ever seems to
*need* an actual shape change to be correct in Kotlin, that's a stop-and-ask
moment, not a unilateral call.

## Real findings along the way (not fixed, just characterized)

- **`RakuTypeStub.getGlobalName()`/`getLexicalName()`'s "my" branch is dead
  code for packages.** Both walk ancestor stubs looking for a
  `RakuScopedDeclStub` with `getScope() == "my"`. But
  `RakuScopedDeclStubElementType.shouldCreateStub()` only ever creates that
  wrapper for `"has"` or exported-`"our"` declarations — `"my"` is explicitly
  excluded (`if (!scope.equals("our")) return false;`). So `my class Foo {}`
  is indistinguishable, at the stub level, from plain `class Foo {}`. Pinned
  with a test (`testMyScopedPackageBehavesLikeOurForNaming`); not fixed —
  out of scope for a conversion, and worth a deliberate look on its own.
- **`CroTemplatePartStubIndex`'s `getInstance()` returned the wrong type**
  (`RakuAllRoutinesStubIndex` instead of itself) — a copy-paste bug, confirmed
  dead via grep (`getInstance()` is never called; the index is only reached
  through plugin.xml's no-arg-constructor instantiation). Ported verbatim in
  the stub-indices commit, then fixed as its own separately-labeled final
  commit (18/19), not silently folded into the mechanical port.

## Process notes

- One commit per PSI kind, interface+impl+element-type together (a "vertical
  slice") — not one commit for "all interfaces," then "all impls," etc. Each
  commit stays independently bisectable and revertable.
- Verification cadence: fast in-process stub tests
  (`org.raku.comma.stub.*`, no real file I/O) after every commit; the fuller
  set (`GoToDeclarationTest`, `ModuleNameCompletionTest`,
  `MultiModuleCompletion`, `cro.*`) as a checkpoint every ~5 commits rather
  than every single one, to keep iteration fast — the fast suite already
  exercises every changed serialize/deserialize path, and multi-module/
  index-persistence coverage is much slower per-run for comparatively little
  marginal signal at this granularity.
- `org.raku.comma.cro.annotation.AnnotationTest`'s two failures during the
  5-commit checkpoint are a pre-existing, unrelated environment issue (a
  `PluginException` from the bundled Kotlin plugin's
  `KotlinScriptDefinitionCodeVisionProvider`, traced back to the 2026.1→2026.2
  SDK bump earlier this session) — hits any test routing through
  `checkHighlighting()`/`doHighlighting()`, confirmed by reproducing it against
  completely untouched test classes. Not this conversion's doing.
- For the final two commits (17, 18 — file-level machinery and the
  `CroTemplatePartStubIndex` fix), a full `./gradlew test` run stopped being
  usable for verification: three attempts each failed for reasons unconnected
  to any stub code (stale-daemon OOM, then two runs that stalled/timed out
  with no `psi/stub` frames in any collected stack trace). Fell back to the
  checkpoint-tier subset for those two commits instead — see the
  `full-test-suite-instability-2026-07` memory entry for the full
  investigation.

## Completion

All 19 planned commits landed 2026-07-27. `STUB_VERSION` never needed a bump
— every file turned out to be a pure transliteration, no shape changes. The
whole `psi/stub` package (56 files) is now Kotlin.
