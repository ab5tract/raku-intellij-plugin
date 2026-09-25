# RakuAST Viewer — Known Issues and Follow-ups

Companion to `2026-09-19-rakuast-viewer-design.md`. Records what was
deliberately left undone, and what must be settled before the editing UI
ships. Everything here was found by review or measurement, not speculation.

## What shipped

A **read-only** viewer. The edit path exists end to end and is tested —
`rakuast-tool.raku`'s `edit` verb, `RakuAstService.edit`, and
`RakuAstEditApplier` — but has **no production callers**, verified by grep
during the whole-plan review. The only document mutations the shipped path
performs are `setSelection` and `moveToOffset`, both bounds-checked.

## Must be fixed before the editing UI is wired

### Grapheme→UTF-16 conversion lives only in the panel

Raku's `origin.from`/`.to` are **NFG grapheme indices**; IntelliJ document
offsets are **UTF-16 code units**. They diverge on astral characters (one
grapheme, two UTF-16 units) and on combining marks.

`RakuAstViewerPanel` converts via a `BreakIterator` map built once per
analysis. `RakuAstEditApplier.apply()` does **not** — it has no snippet to
map against. Wiring the edit UI without moving or sharing that conversion
means replacing the wrong span, which is the one thing this design exists to
prevent.

Regression test to keep working: `testHighlightConvertsGraphemeIndicesAcrossAstralCharacter`.

Note a guard that does *not* help here: comparing the document text at the
computed range against the analyzed snippet detects document *edits*, but is
mathematically incapable of detecting encoding skew — both `snippet` and
`baseOffset` come from IntelliJ, so the two expressions are equal for any
indices whenever the document is unedited.

### The `edit` response's `tree` is stale

`rakuast-tool.raku`'s `edit` branch returns `node-json($ast, [])` from the
mutated AST, but every node's `origin` is still the **pre-edit** parse origin.
After an edit that changes text length, every span at or after the edit point
is wrong, including the edited node's own. The response also omits the new
snippet, so a caller cannot refresh its cache from it as the design assumed.

Fix by either returning the spliced snippet and re-parsing backend-side, or
dropping `tree` from the edit response and having the caller re-analyze.

### Two weak spots in unreachable edit code

- `testEditNodeValuedAttributeParsesSnippet` asserts only that no error
  occurred and text is non-null. It would pass even if the wrong child were
  selected during `StatementList`/`Statement::Expression` unwrapping.
- That unwrap loop takes only `@kids[0]` per level, silently discarding
  sibling statements in a multi-statement value snippet.

## Environment and upstream

### MoarVM: null-backed `Str` from RakuAST introspection

`.^attributes` + `Attribute.get_value` on RakuAST nodes can yield a `Str`
that passes `.defined`, matches its type and prints fine, but is backed by a
null `MVMString` — **any** real operation (`.gist`, `.NFD`, `.chars`)
segfaults the VM. Reproduces on 2025.08, 2026.01, 2026.03 and 2026.08-445, so
it is long-standing rather than a regression.

The script defends structurally via `safe-str`, which detects the condition by
unboxing without touching the value:

```raku
sub safe-str(Mu $raw) {
    return True unless nqp::istype($raw, Str);
    !nqp::isnull_s(nqp::unbox_s($raw));
}
```

`Mu` is required — attribute values include NQP-level objects that are not
`Any`. The guard is empirically, not structurally, complete: `.DEPARSE` on a
node-valued attribute can still touch a null-backed `Str` nested deeper, which
no Raku `try` can catch. Verified clean over 12 code shapes × 4 builds.

An upstream fix exists on the MoarVM fork, branch
`ab5tract/strtocodes-null-guard`: `MVM_unicode_string_to_codepoints` read
`s->body.num_graphs` with no null check, the only `MVMString` entry point in
`normalize.c` lacking the guard every sibling string op uses. It converts the
segfault into `strtocodes requires a concrete string, but got null`, which is
also how the offending attribute was identified at all.

### `BEGIN`/`CHECK` snippets fail outright on moar-2026.03

Any snippet containing a `BEGIN` or `CHECK` block fails `.AST` with
`Unknown compilation input 'qast'`. Unrelated to this feature, but users
selecting such code will get an error rather than a tree.

### `Failed to determine cwd` when the working directory is gone

Unrelated to this feature; recorded here because it was hit during the same
work and has not been triaged.

```
$ raku -MZef::CLI -e'' install Raylib::Bindings
Failed to determine cwd: no such file or directory
  at SETTING::src/core.c/Process.rakumod:227
```

Line 227 is `IO::Path.new(:CWD(INIT nqp::cwd()), nqp::execname())` in the
`$*EXECUTABLE` initialiser, so `nqp::cwd()` — i.e. `getcwd()` — failed with
ENOENT. That happens when the process's working directory has been removed
out from under it, which is a shell-state problem rather than a zef or module
problem. `cd` to any directory that exists and it goes away.

Two things are still worth triaging:

1. **Diagnosis quality.** The failure surfaces during setting load with a
   30-frame NQP backtrace and no mention of the working directory being the
   culprit. Raku cannot do much when `getcwd()` fails this early, but the
   message could name the likely cause rather than leaving the user reading
   `ModuleLoader.nqp` frames.
2. **Whether it should be fatal at all.** `$*EXECUTABLE` needs a CWD to build
   an absolute path, but a missing CWD arguably warrants a degraded
   `$*EXECUTABLE` rather than refusing to start. Worth deciding deliberately.

### Performance is not the design's ~310ms for real files

Measured on a 378-line Raku file: ~2.1 s and 568 KB of JSON (was 958 KB before
the display cap). The ~310 ms figure holds for one-liners. Time is dominated
by Rakudo compile/walk, not payload size, so the display cap did not reduce
it. Acceptable for an explicit user action on a selection; it would not be
acceptable for live caret-following, which is one reason that was rejected.

## Plugin-wide, beyond this feature

### No-SDK paths are untestable

`RakuProjectSdkService.getState()` shows a modal SDK chooser and blocks on
`.join()` when no SDK is configured, with no unit-test-mode guard. A headless
test that triggers the condition hangs. This makes **every** no-SDK error path
in the plugin untestable, not just this feature's. A test-mode guard on that
service, or injecting the SDK path as a dependency, would be small and
high-leverage.

### `executeAndRead` discards failure information

It reads stdout only and returns an empty list on any non-zero exit, dropping
the reason entirely — which is how `sub EXPORT` symbol loading fails silently
elsewhere in the plugin. This feature works around it by reporting errors as
JSON on stdout with `exit 0`. Also unaddressed: stderr is never drained (a
selection producing >64 KB of compile-time worries would deadlock the child),
there is no timeout, and `Task.Backgroundable`'s cancel never kills the
process. `CapturingProcessHandler` with a timeout would fix drain, timeout and
cancel together.

## Cosmetic, deferred

`actions.xml` indentation on the new action block; `val tree` shadowing the
`tree` field in `showAnalysis`; fully-qualified inline class references instead
of imports in the panel, factory and action.
