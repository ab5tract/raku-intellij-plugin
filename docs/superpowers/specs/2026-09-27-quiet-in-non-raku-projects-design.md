# Staying quiet in projects that are not Raku

**Date:** 2026-09-27
**Status:** Approved

## Problem

Opening a project with no Raku in it still gets the Raku plugin's attention: the
ecosystem is fetched over the network, zef may be prompted for, an SDK warning
can fire, and the Camelia widget appears in the status bar.

The obvious guess — that these features are ungated — is wrong. They are gated:

| feature | existing gate |
|---|---|
| ecosystem fetch, zef prompt | `RakuServiceStarter` → `projectContainsRakuCode` |
| Camelia status bar widget | `RakuStatusBarWidgetFactory.isAvailable` → `doesProjectContainRakuCode` |

The gates are sound; the **predicate behind them is too loose**, and the answer
it produces is **cached in persisted project state**, so a wrong verdict
survives restarts.

`CommaProjectUtil.pathContainsRakuCode` walks the project tree and accepts a
file when it has one of seven Raku extensions **or** — the wide one —

```kotlin
file.extension.isNullOrEmpty() && file.readText().lines().first().contains("raku")
```

Any extensionless file whose first line merely *contains* the substring
`raku` makes the whole project Raku. Under a path like
`~/code/raku/…` that is easy to hit by accident. The walk also reads file
contents, ignores module exclusions, and — in the case that matters, a project
with no Raku at all — can never short-circuit, so it traverses the entire tree
to conclude "no".

## Goal

Three features stay silent unless the project earns them, the verdict is
current rather than cached, and a project that wants the ecosystem can ask for
it without qualifying automatically.

## Decisions (user-approved)

1. **Tiered by cost.** The ecosystem fetch is network plus zef plus a
   background task, so it requires a real distribution. The widget and the SDK
   prompt are useful whenever Raku is being edited, so any genuine Raku file is
   enough.
2. **Re-evaluate rather than cache.** Ask the file-type index; persist nothing.
3. **The butterfly menu gains `Refresh Ecosystem`**, so a scripts-only project
   can opt in to what it no longer gets automatically.

## What the investigation turned up

Three findings that shaped the design, none of them visible from the symptom.

**There are four Raku file types, and one claims bare `.t`.**
`plugin.xml` registers Raku Script (`p6 pl6 raku`), Raku Module
(`pm6 rakumod`), Raku Pod (`pod6 rakudoc`) and Raku **Test** (`t t6 rakutest`).
The current walk uses its own extension set, which excludes `t`. Switching
naively to the file-type index would therefore make detection *more* eager —
any Perl 5 project with `t/*.t` would become a Raku project.

**Shebang detectors already do properly what the loose clause did badly.**
`RakuFileShebangTypeDetector` and `RakudoFileShebangTypeDetector` are
registered, so an extensionless `#!/usr/bin/env raku` script is genuinely typed
as Raku and is in the index. The substring clause can be deleted rather than
replaced.

**`RakuEcosystem` fetches on construction and cannot be refreshed.**

```kotlin
private var ecosystemState = initialize().join()   // field initializer
```

Merely resolving `service<RakuEcosystem>()` triggers a fetch and blocks the
calling thread on `.join()`. Gating the call site inside `refreshProjectState`
is therefore not sufficient — `RakuDependencyService` exposes three properties
that touch the service. And `initialize()` is guarded by
`isNotInitializing && isNotInitialized`, so once the future completes it
returns cached state forever: a menu item calling it would silently do nothing.

## Design

### `RakuProjectKind` — one predicate, two levels

New object, logic only, no UI, so it can be tested directly:

```kotlin
fun hasRakuFiles(project: Project): Boolean       // FileTypeIndex
fun isRakuDistribution(project: Project): Boolean // META6.json at the root
```

`hasRakuFiles` asks `FileTypeIndex` for Raku **Script**, **Module** and **Pod**
in the project scope. That is O(index lookup) rather than O(tree), respects
excluded folders for free, and counts shebang-detected files correctly.

**Raku Test is deliberately excluded.** `.t` is shared with Perl 5, and a test
file alone should not wake the plugin. `.rakutest` loses its claim as a result,
which is accepted: any real Raku project carries a Script, Module or Pod file
too, so nothing reachable is lost.

`isRakuDistribution` is `META6.json` at the project root — the existing
`projectHasMetaFile` check. Dependencies only exist where a META6 does, which
is exactly what the ecosystem is for.

### `RakuEcosystem` — refreshable, and no longer self-starting

```kotlin
fun refresh(): CompletableFuture<EcosystemDetailsState>
```

resets `initializationFuture` and `initializationStatus`, then re-fetches.

The eager `= initialize().join()` field initializer is removed. `ecosystem`
returns empty state until something initializes it. This is what makes the
gating real — otherwise any touch of the service fetches — and it takes a
blocking `.join()` out of service construction.

### Gates

| feature | gate |
|---|---|
| ecosystem fetch + zef prompt, inside `refreshProjectState` | `isRakuDistribution` |
| `RakuServiceStarter` as a whole | `hasRakuFiles` |
| `RakuStatusBarWidgetFactory.isAvailable` | `hasRakuFiles` |
| `RakuSdkUtil.reactToSdkIssue` | early return unless `hasRakuFiles` |

### Butterfly menu

`RakuStatusBarListPopupStep` is a `ListPopupStep<String>` with a `when` on the
chosen string. `Refresh Ecosystem` joins the existing values below the
separator that currently precedes `Launch REPL`, and runs `refresh()` under
`withBackgroundProgress` so a slow fetch does not freeze the popup.

### `reactToSdkIssue` takes a nullable project

`reactToSdkIssue(project: Project?, …)`. The gate applies **only when a project
is given**; a null project still notifies. The complaint being fixed is noise
inside a specific non-Raku project, and an issue that cannot be attributed to
one is not that — silencing it would hide a genuine global SDK problem.

### Removed, and what survives

- `doesProjectContainRakuCode` from the persisted `RakudoProjectState`.
- `hasScannedForRakuFiles`.
- The substring clause inside `pathContainsRakuCode`.

**`pathContainsRakuCode` itself stays.** `canOpenFileAsProject` calls it to
decide whether a directory can be opened as a Raku project, and that runs
before any project exists — so `FileTypeIndex`, which needs a project scope, is
not available there. It keeps the extension check and loses only the substring
clause. Nothing else calls it once the three gates move to `RakuProjectKind`.

The asymmetry is deliberate and worth stating: opening a directory is a
one-shot question asked without a project, so a bounded tree walk is the only
tool available; the three gated features run repeatedly inside a live project,
where the index is both cheaper and more correct.

### Dumb mode

`FileTypeIndex` requires smart mode, and two of the four call sites run at
startup. During indexing the answer is **false** — stay quiet — and the widget
re-evaluates through `StatusBarWidgetsManager.updateWidget` on the transition
to smart mode. Quiet-then-appear is the correct failure direction: the
complaint being fixed is noise, so erring toward silence cannot make it worse.

## Testing

`FileTypeIndex` works under `BasePlatformTestCase`, so both levels get real
coverage rather than mocks. Fixtures, one project each:

- only `t/foo.t` → **not** Raku (the Perl 5 collision)
- a `.rakumod` → Raku
- an extensionless `#!/usr/bin/env raku` script → Raku (shebang detector)
- `META6.json` → distribution
- `.rakumod` but no `META6.json` → Raku, **not** a distribution
- empty → neither

Plus: `refresh()` actually re-fetches after a completed initialize (the bug
that would make the menu item a no-op), and `reactToSdkIssue` returns without
notifying when `hasRakuFiles` is false.

## Risks

**Removing the eager initializer is the real behaviour change.** Anything
reading `ecosystem` before initialization now sees empty maps instead of
blocking until the fetch completes. That is correct, but it is the change most
likely to surface somewhere unexpected — completion is the probable place,
since `RakuDependencyService.ecoProvideToModule` feeds it. Worth its own commit
so it can be reverted alone.

**The `.t` exclusion** trades one false positive for a possible false negative:
a project of nothing but `.rakutest` files would no longer register. Judged
unlikely enough to accept.

**Dumb-mode false negatives are transient** but real: a widget that appears a
few seconds after startup may read as a glitch.

## Out of scope

- `RakuLegacyExtensionsDetector`, the third startup activity, is ungated and
  walks every module source root with a raw `java.io` recursion that ignores
  IntelliJ's exclude folders — on rakudo that means descending into
  `t/spec`. It is a real problem and a separate one; this design does not
  touch it.
- Making `Refresh Ecosystem` reachable as a plain action (Tools menu / Find
  Action) as well as from the widget. Worth doing, since the widget is now
  hidden in more projects, but it is additive and can follow.
