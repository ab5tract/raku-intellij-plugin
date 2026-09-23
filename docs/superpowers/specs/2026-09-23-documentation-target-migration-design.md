# Migrating documentation to the DocumentationTarget API

**Date:** 2026-09-23
**Status:** Approved

## Problem

`RakuDocumentationProvider` implements `com.intellij.lang.documentation.DocumentationProvider`,
the pre-2022 documentation interface, registered as `lang.documentationProvider`.
It works — the platform bridges legacy providers into the modern pipeline — but
it is the older of two supported APIs, and its shape constrains what the plugin
can say about a symbol.

Concretely, `getQuickNavigateInfo` returns one flat HTML string. Everything the
IDE knows how to render separately — the icon, the symbol's owner, where it came
from — has to be either crammed into that string or left out. Today it is left
out: `method end(--> Int)` and nothing else, in every context that shows a Raku
symbol.

`DocumentationProvider` is **not deprecated** on our platform. Checked against
the 262 bytecode: no `@Deprecated`, no `@ScheduledForRemoval`. This migration is
modernization and capability, not a forced move, which also means reverting it
is cheap.

## Goal

Serve documentation through `DocumentationTarget`, with no user-visible
regression in the popup, the hover, or `Shift+F1` — and use the structured
presentation the new API affords to say more about a symbol than a flat string
can.

## Decisions (user-approved)

1. **Parity plus structured presentation.** The popup body, the hover string and
   the external URL behave exactly as they do today. `TargetPresentation` adds
   icon, owner and origin.
2. **Async only where something can actually block.** Not uniformly.
3. **No `InlineDocumentationProvider`.** Rendering declarator pod inline in the
   editor is a genuine new capability the old interface cannot provide, and it
   was considered and set aside. Out of scope here; nothing in this design
   precludes adding it later.
4. **The 25 existing expectations do not change.** They are the regression
   proof.

## What the platform gives us

Verified present in `lib/intellij.platform.lang.impl.jar` on IU-262.8665.258:

| Today | New API |
|---|---|
| `generateDoc` | `DocumentationTarget.computeDocumentation()` → `DocumentationResult.documentation(html)` |
| `getUrlFor` | `.externalUrl(…)` on the same builder |
| `getQuickNavigateInfo` | `computeDocumentationHint()` (String) and `computePresentation()` (structured) |
| — | `createPointer()` — mandatory, no equivalent today |
| `lang.documentationProvider` | `com.intellij.platform.backend.documentation.psiTargetProvider` |

`DocumentationResult.Documentation` is a builder: `html`, `externalUrl`,
`anchor`, `definitionDetails`, `images`, `updates`. `TargetPresentation` carries
`icon`, `presentableText`, `containerText`, `locationText` and per-slot
`TextAttributes`.

## Shape of the change

```
+ docs/RakuDocumentationTarget.kt              the target
+ docs/RakuPsiDocumentationTargetProvider.kt   EP entry point
+ docs/RakuDocRendering.kt                     shared rendering, extracted
- docs/RakuDocumentationProvider.kt            deleted
~ META-INF/plugin.xml                          EP swapped
```

The legacy registration is **removed, not left alongside**. The platform bridges
legacy providers into the same pipeline, so registering both would yield two
targets for every Raku symbol and duplicate the documentation in the popup.

The new extension point is **not language-filtered**, unlike
`lang.documentationProvider`. Its declaration takes a bare `implementation`
attribute and no `language`:

```xml
<platform.backend.documentation.psiTargetProvider
    implementation="org.raku.comma.docs.RakuPsiDocumentationTargetProvider"/>
```

So the provider is called for elements of *every* language and must return null
for anything that is not ours. That check is the first thing it does.

`RakuDocRendering` exists because the signature string now has two consumers —
`computeDocumentationHint()` and `presentableText` — and because the existing
`when` over element kinds (constant, enum, package, parameter, regex, routine,
subset, variable) is the part worth preserving verbatim rather than retyping
into a new class.

Nothing in `src/main` depends on `RakuDocumentationProvider` beyond its own
registration; the only references are in tests. The swap is therefore contained.

## The target

```kotlin
class RakuDocumentationTarget(
    private val element: PsiElement,
    private val originalElement: PsiElement?,
) : DocumentationTarget {

    override fun createPointer(): Pointer<out DocumentationTarget> = when (element) {
        is RakuExternalPsiElement -> Pointer.hardPointer(this)
        else -> /* SmartPointerManager, via Pointer.delegatingPointer */
    }

    override fun computeDocumentationHint(): String?
    override fun computePresentation(): TargetPresentation
    override fun computeDocumentation(): DocumentationResult?
}
```

`createPointer()` is the one genuinely new obligation: a target must survive
being carried across read actions. Two cases, and they differ:

- **File-backed PSI** — `SmartPointerManager` tracks it through edits, as usual.
- **`RakuExternalPsiElement`** — synthesised in memory from the SDK symbol cache,
  with no file and no offsets for a smart pointer to track. `Pointer.hardPointer`
  is correct here precisely *because* the element is immutable: there is no
  later state for a pointer to become stale against. If the SDK is swapped the
  whole symbol cache is rebuilt and the target is discarded with it.

`computeDocumentationHint()` returns exactly the string `getQuickNavigateInfo`
returns today. That is what makes hover parity a property of the design rather
than something to be checked afterwards.

## Presentation

| slot | CORE symbol | project code |
|---|---|---|
| icon | `RakuIcons.CAMELIA` | `RakuIcons.CAMELIA` |
| presentableText | `end(--> Int)` | `duel(Magician $a, Magician $b)` |
| containerText | owning type — `Any` | enclosing package — `Magician` |
| locationText | `CORE.setting` | containing file name |

The kind keyword is dropped from `presentableText` **for routines only** —
`method end(--> Int)` becomes `end(--> Int)` — because the icon and container
carry that context and the slot is narrow. It stays in
`computeDocumentationHint()`, so the hover string is unchanged.

For every other element kind the presentable text is today's string verbatim.
`class Magician is Cool does Int` stays whole: stripping its keyword would leave
`Magician is Cool does Int`, which reads as a fragment rather than a shorter
label. The rule is "drop a leading routine keyword", not "drop the first word".

| element | presentableText |
|---|---|
| routine | `end(--> Int)` — keyword dropped |
| package | `class Magician is Cool does Int` — unchanged |
| enum / subset / constant / variable / parameter / regex | unchanged |

`containerText` is null for a top-level sub, which renders as name plus location
with no owner — the intended degradation, not a gap.

## Threading

```kotlin
override fun computeDocumentation(): DocumentationResult? = when (element) {
    is RakuMethodCall, is RakuSubCall ->
        DocumentationResult.asyncDocumentation {
            readAction { /* multiResolve, then docsString */ }
        }
    else ->
        DocumentationResult.documentation(html).externalUrl(url)
}
```

Async is scoped to the call-site branch because that is the only branch that can
block. Tracing the other two:

- **Declarator pod** — `RakuDocumented.getDocsString()` walks PSI siblings
  gathering `#|` / `#=` comments. In-memory, read-action work.
- **CORE symbols** — `RakuExternalPsiElement.getDocsString()` returns `myDocs`,
  a field already populated when the symbol cache was built. `core.json`
  (995 KB, 461 entries) is parsed once at SDK init, not per request.

Neither touches disk or network at documentation time, so wrapping them in
`asyncDocumentation` would add coroutine machinery for no latency win. The
call-site branch is different: it runs `multiResolve(false)`, which can consult
indexes, and today it does so on the EDT path.

`@Synchronized` on `generateDoc` is removed. The data behind it is either an
immutable field or PSI that the read action already guards. That annotation
arrived in `1bef5d59` ("2026.2 beta.5"), a bulk release commit that records no
rationale for it, so we cannot tell whether it guards a known race or is
defensive. **It is therefore removed in its own commit**, separable from the
migration, so it can be reverted without unpicking anything else.

## Testing

`DocumentationTest`'s three helpers — `testQuickDoc`, `testGeneratedDoc`,
`testURL` — are reimplemented against the new API. **All 25 test bodies and
their expectations stay byte-identical.** The helpers are the seam; the
expectations are the contract. If the migration alters any user-visible string,
25 tests say which one.

New coverage:

- **Presentation slots** — three cases matching the table above: a CORE method,
  a project method, a top-level sub (the null-container case).
- **Pointer survival** — dereference `createPointer()` for a `RakuExternalPsiElement`
  target and confirm it still yields documentation. This is the obligation the
  old API never imposed, so nothing existing covers it.

The async branch is already exercised: `testMethodExternalFromCOREClass` resolves
`.end` through `multiResolve`, which is the branch being made asynchronous.

`DocDumpProbe.kt` (untracked) calls `DocumentationManager.getProviderFromElement`
and will stop compiling. It is updated in the same change, since dumping real
rendered output is how the result gets eyeballed.

## Risks

**Hover source is unverified.** The design assumes Ctrl-hover reads
`computeDocumentationHint()`. That is consistent with the API's shape but has
not been proven against 262's internals. **First implementation step is to
confirm it.** If hover instead renders `computePresentation()`, the parity claim
for hover does not hold as written and the presentation decision needs revisiting
— it would mean the kind keyword disappearing from hover, which was not agreed.

**The `@Synchronized` removal**, as above: isolated to its own commit.

**Rollback is cheap.** The legacy API is not deprecated, so reverting the EP
swap restores today's behaviour exactly. This is the main reason the migration
can be attempted without a staged rollout.

## Out of scope

- `InlineDocumentationProvider` — declarator pod rendered inline in the editor.
  Considered, set aside (decision 3).
- `DocumentationLinkHandler` — clickable links between Raku doc pages. Not
  offered today; no regression in omitting it.
- `getUrlFor` behaviour on project code. It returns null for anything that is
  not a CORE symbol, so `Shift+F1` silently does nothing on your own
  declarations. That is today's behaviour and it is preserved unchanged here;
  fixing it is a separate question from which API serves it.
