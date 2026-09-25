# RakuAST Viewer — Design

**Date:** 2026-09-19
**Status:** Approved for planning

## Goal

A tool window that shows the real Rakudo `RakuAST` tree for selected Raku code,
lets the user edit node attributes, and writes each edit back into the editor by
replacing exactly the affected node's source span.

This is an **end-user refactoring tool**, not a plugin-developer debugging aid.
It must produce friendly errors, integrate with undo, and never corrupt a file.

Note this is a genuinely different tree from the plugin's own PSI. The plugin
parses Raku itself for highlighting and resolution; `RakuAST` is Rakudo's own
compiler AST, reachable only by running `raku`. The viewer shows the latter.

## Decisions

Each decision below was settled against a live Rakudo rather than assumed.
Unless stated otherwise, measurements are from
`x.core/raku-prefix/bin/raku` at **v2026.08-445-g7ded5ff7de**, with
comparisons against rakubrew `moar-2026.03` and `moar-2026.01`.

### Unit of analysis: the selection, not the file

`.AST` performs real compile-time resolution, not just parsing — `class Foo is
Bar` dies with *"'Foo' cannot inherit from 'Bar' because it is unknown"*. A
whole-file mode would therefore go blank whenever the file or any dependency
fails to compile, which is most of the time while editing. (The `Amazing-Mazes`
project this was designed against does not currently compile at all.)

Analyzing the selection standalone keeps the tool usable on broken files and
scopes edits tightly. The cost, accepted: a snippet referencing imported types
may not resolve in isolation.

### Refresh: explicit action, one-shot subprocess

Measured cost of a one-shot analysis: **~310ms** (bare `raku` startup is 116ms;
`use experimental :rakuast` plus AST work adds ~190ms). That is fine for a
deliberate action and too slow to follow the caret.

A persistent backend (as `repl/repl-backend.raku` already does) would cut this
to ~1ms, but reintroduces process lifecycle, crash recovery, and AST-versus-file
drift. Rejected for now. The backend verbs are identical either way, so this can
change later without redesign.

### Replacement scope: the edited node only — mandatory

`DEPARSE` does not round-trip source. Measured on v2026.08-445:

```
original                                  deparsed
my $x = 41 + 1;   # the answer, nearly    my $x = 41 + 1;
my  @spaced   =   1,2,3;                  my @spaced = 1, 2, 3;
# a standalone comment                    (gone)
```

Ordinary `#` comments are **deleted** and whitespace is normalized. This is
structural, not a bug awaiting a fix: there is no `RakuAST` node for a comment.
Comments are lexer trivia that never enter the tree, and `Deparse.rakumod` (2966
lines on `origin/main` @ 2026-08-16) mentions "comment" exactly once, referring
to Pod/heredoc types.

RakuDoc **is** in the tree — `#|` and `#=` attach to declarations as `.WHY`
(`.leading` / `.trailing`) and survive deparse intact.

Therefore any replacement wider than the edited node silently destroys user
comments. Narrow replacement is byte-exact by construction: the `41` in
`my Int $x = 41 + 1;` deparses to precisely `41` at span 8..10.

This also makes the design version-robust. Recent upstream DEPARSE work is real
and visible — `sub f ($a!)` on 2026.03 became `sub f ($a)` on 2026.08-445, and
block indentation was fixed — but it improves *generated source quality*, not
comment fidelity, so it can only make subtree edits better. Nothing here depends
on which Rakudo the user has configured.

### Addressing: structural paths, not spans

Nodes are addressed by child path (`[0,1,0]`). Spans shift as soon as an edit
changes text length, invalidating every later address; paths stay valid across
value edits.

### Edit scope

- **Scalar attributes** (`value`, `sigil`, boolean flags) — edited inline.
- **Node-valued attributes** (`type`, `initializer`, `expression`) — edited by
  typing Raku source, parsed via `.AST` and substituted.
- **RakuDoc** (`WHY` / `leading` / `trailing`) — editable; setters exist on
  `Sub`, `Method`, `Class`, `Parameter`, `Block`, `VarDeclaration::Simple`.
- **List-valued attributes** (`traits`, `parameters`, `operands`) — **read-only
  in v1**: shown with their element count and navigable into via the tree, but
  not editable. Editing them means insert/remove/reorder semantics, which needs
  its own UI affordance and is deliberately deferred. Individual elements remain
  editable by selecting them as nodes in their own right.
- **Compiler bookkeeping** (`sunk`, `thunks`, `okifnil`, `sorries`, `worries`,
  `origin`) — hidden behind a toggle, not shown by default. `origin` stays in
  the payload regardless, since the applier needs it to locate the PSI element.

Mutability is not a constraint: of 33 node types sampled across realistic code,
**all 33** expose `set-*` methods, and `nqp::bindattr` is a universal fallback
for anything that does not.

### Application: via PSI, not raw offsets

Edits are applied by resolving the PSI element at the target range and replacing
through a `RangeMarker` inside a `WriteCommandAction` — the existing
`FatarrowSimplificationFix` pattern. This gives undo for free and survives
concurrent edits elsewhere in the document.

`origin` is used only as the *correlation key* to locate that element, never as
the write coordinate directly. Origin spans can be narrower than expected: the
declaration in `my Int $x = 41 + 1;` reports its span as `$x = 41 + 1`,
excluding the leading `my Int `.

## Architecture

Five units. The script knows nothing about IntelliJ; the service knows nothing
about Swing; only the applier touches documents.

### 1. `resources/rakuast/rakuast-tool.raku`

Bundled Raku script, following the `symbols/*.raku` convention: vendored
`to-json`, no module dependencies, one JSON line on stdout. Reads a JSON request
on stdin.

**`analyze`** — `{verb, source}` → `{tree}` where each node is:

```json
{ "class": "RakuAST::VarDeclaration::Simple",
  "path": [0, 0],
  "span": { "from": 7, "to": 18 },
  "attrs": [ { "name": "type", "kind": "node",
               "display": "RakuAST::Type::Simple -> 'Int'", "editable": true } ],
  "children": [ ... ] }
```

`kind` is `scalar` | `node` | `list`.

**`edit`** — `{verb, source, path, attr, value, valueKind}` →
`{text, span, tree}`. Walks to the node by path, parses `value` via `.AST` when
`valueKind` is `node`, applies `set-<attr>` (falling back to `nqp::bindattr`),
deparses **that node only**, and returns the refreshed tree in the same response
so one round trip serves both replacement and refresh.

Errors are reported as `{"error": "..."}` on stdout with exit code 0 — see
Error Handling.

**Implementation gotcha:** attribute reads must guard against `VMNull` with
`nqp::isnull(nqp::decont($raw))` *before* any Raku-level operation. Both
`.defined` and `.elems` throw on it; this crashed three separate probes during
design.

### 2. `RakuAstService` (project service)

Owns the subprocess. Extracts the resource via `RakuUtils.getResourceAsFile`,
builds `RakuCommandLine(project)` so it uses the **configured project SDK**, runs
off the EDT, decodes with kotlinx.serialization as `RakuExternalNamesParser`
does. Public surface: `analyze(source)` and `edit(...)`. No UI knowledge.

### 3. `RakuAstViewerPanel` (tool window content)

`JBSplitter`: `com.intellij.ui.treeStructure.Tree` above, attribute table below.
Hand-rolled Swing, matching `RakuGrammarPreviewer` — the closest existing
precedent. (Kotlin UI DSL is used only for dialogs in this codebase; panels are
Swing.)

Holds the analyzed snippet, its base offset `B`, and the document modification
stamp from analyze time.

### 4. `RakuAstEditApplier`

The only unit that writes. Maps `B + span` to file coordinates, resolves the
enclosing PSI element, replaces via `RangeMarker` in a `WriteCommandAction`.

### 5. `RakuAstViewerFactory` + `AnalyzeSelectionAction`

`ToolWindowFactory` registered in `plugin.xml` next to `Raku Grammar Preview`,
plus an action to populate the panel from the current selection.

## Data flow

**Read** — on Analyze Selection:

```
selection ──► base offset B + snippet
                  │
                  ▼  off the EDT, ~310ms
        RakuAstService.analyze(snippet)
                  │  JSON in/out
                  ▼
        raku: $snippet.AST ──► walk ──► tree JSON
                  │
                  ▼
        AstTree ──► tree view + attribute table
```

Spans are snippet-relative; file coordinates are always `B + span`. Selecting a
tree node highlights `B + span` in the editor.

**Write** — on committing an attribute edit:

```
edit(snippet, path, attr, value)
        │
        ▼
raku: re-parse ──► walk to path ──► validate value ──► set-<attr> / bindattr
        │                                          └─► abort on parse failure
        ▼
{text, span, tree}          one round trip: replacement + refresh
        │
        ▼
applier: PSI element at B+span ──► RangeMarker ──► WriteCommandAction
        │
        ▼
document.replaceString(marker.start, marker.end, text)
```

The backend is **stateless**: every `edit` re-parses from the snippet the IDE
holds, so no server-side AST can drift from the file. The panel updates its
cached snippet from the same response rather than re-analyzing.

## Error handling

`RakuCommandLine.executeAndRead` reads stdout only and returns an **empty list
on any non-zero exit**, discarding the reason — the exact mechanism by which
`sub EXPORT` symbol loading fails silently today. The script therefore never
signals failure by exit code: its `CATCH` emits `{"error": ...}` on stdout and
exits 0.

| Case | Behavior |
|---|---|
| Snippet doesn't parse/resolve | Show Rakudo's own message, no tree, nothing written. Expected, not an error state |
| No SDK / invalid SDK | `RakuCommandLine` throws `"No SDK for project"`; panel says so, points at SDK settings |
| Edit value doesn't parse | Validated by `.AST` **before** mutation; reported inline on the field; file untouched |
| Setter missing / bindattr fails | Names the failing attribute; file untouched |
| Deparsed output doesn't re-parse | Backend sanity-checks its own output before returning; refuses rather than emit invalid source |
| Document changed since analyze | Re-analyze, then apply. Refuse only if the path no longer resolves |

## Testing

**Without a subprocess** (the bulk — fast and deterministic): JSON decoding,
mirroring `RakuExternalNamesParserTest`; path addressing; `B + span` coordinate
mapping; staleness/modification-stamp logic; error-payload handling.

**Against a real SDK** (few): analyze a known snippet, assert tree *shape* —
node classes and paths.

> Assert structure, never deparse formatting. `sub f ($a!)` on 2026.03 became
> `sub f ($a)` on 2026.08-445; an assertion on that text is red on one Rakudo and
> green on another. This is the same failure mode that flipped the `.perl`
> deprecation expectation twice (`1b187385` removed it, `d3a76ead` restored it).

**Fixture-level:** an edit produces the expected document text, and undo restores
it.

The script stays debuggable standalone:
`echo '{"verb":"analyze","source":"my $x = 1"}' | raku rakuast-tool.raku`.

## Out of scope

- Whole-file analysis (blocked by compile-resolution; revisit if useful)
- Persistent backend session (deferred; verbs are transport-agnostic)
- Editing ordinary `#` comments — impossible, no AST node exists
- Any attempt to preserve formatting outside the edited node

## References

- Bundled-script + JSON precedent: `RakuProjectSdkService.loadModuleSymbols`,
  `RakuExternalNamesParser`, `resources/symbols/raku-module-symbols.raku`
- Persistent-backend precedent: `resources/repl/repl-backend.raku`
- Replacement precedent: `inspection/fixes/FatarrowSimplificationFix`
- Tool window precedent: `grammar/RakuGrammarPreviewFactory`, `RakuGrammarPreviewer`
