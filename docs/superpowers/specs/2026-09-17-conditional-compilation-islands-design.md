# Conditional-Compilation Islands

**Date:** 2026-09-17
**Status:** Approved

## Problem

Rakudo sources use gen-cat compiler directives (`#?if jvm` / `#?if !moar` …
`#?endif`, markers at column 0). Today `RakuConditionalCompilation.preprocess`
blanks every branch not live under the hardcoded `moar` backend before lexing,
so inactive branches have no highlighting and no PSI, and the incremental
highlighter can break around the blanked regions.

## Goal

Every branch of a conditional is lexed and parsed "on its own" — as if its
directive were true — so all branches get syntax highlighting, real PSI,
navigation, and indexing. The active (moar) selection continues to define the
file's outer structure, which therefore can never be damaged by a branch.

## Decisions (user-approved)

1. **Full PSI for all branches** — not highlighting-only.
2. **Torn branches are contained in islands** — a branch that opens a bracket
   closed only by shared code after `#?endif` (~11% of the 626 regions in
   Rakudo `src/`) produces parse errors confined inside its island; the outer
   tree always follows the active selection.
3. **Index everything** — declarations inside inactive branches contribute to
   stubs, completion, find-usages, and structure view. A guard keeps
   cross-branch duplicate declarations from flagging each other.

## Design

### 1. Region model

`RakuConditionalCompilation` gains an entry point that, alongside the masked
text, reports the list of **inactive regions**: the body lines strictly
between an inactive `#?if <cond>` line and its `#?endif`. Marker lines stay
ordinary comments. Region boundaries are always line boundaries. Each region
records its condition text (e.g. `jvm`, `!moar`) for the duplicate guard.

### 2. Token stream

A thin wrapper lexer around `RakuLexer` (returned by
`RakuParserDefinition.createLexer`) merges the blanked-whitespace tokens
covering an inactive region body into a single `CONDITIONAL_BRANCH` token,
splitting any delegate token that straddles a region edge. The cursor-machine
lexer is untouched. `CONDITIONAL_BRANCH` is registered in
`ParserDefinition.getCommentTokens()`, so `PsiBuilder` places it into the
tree without `RakuParser` (22.8k generated lines, whitespace-significant)
seeing it — zero parser changes. (Amended 2026-09-17: the original
whitespace-set variant cannot work — `PsiBuilderImpl.createLeaf` returns
`PsiWhiteSpaceImpl` unconditionally for whitespace-set tokens, verified
empirically and by decompilation. Comment-set tokens take the normal leaf
path, where the `ILazyParseableElementType` check engages; JavaDoc's
`DOC_COMMENT` — a lazy parseable registered as a comment token — is the
platform precedent for exactly this pattern.)

### 3. Island PSI

`CONDITIONAL_BRANCH` is an `ILazyParseableElementType`. On expansion, the
branch's original text (token text comes from the unmasked buffer, which
`RakuLexer.getBufferSequence` already preserves) is lexed and parsed as a
Raku statement list rooted in a new `RakuCondBranch` PSI element. Torn
branches yield error elements inside the island only. Expansion is pure
lex/parse — no symbol resolution — so it is safe during stub building (the
indexes → stub building → resolve circularity fixed in 4a05b048 must not be
reintroduced).

### 4. Highlighting

`RakuSyntaxHighlighter` wraps its lexer in the platform `LayeredLexer`: the
base emits `CONDITIONAL_BRANCH`; a registered layer re-lexes that token's
text with a fresh `RakuHighlighterLexer`. Branch code gets full syntax
colors, and the incremental highlighter gets a clean single-token boundary at
the region edges where it breaks today. Semantic (annotator) highlighting
inside branches comes free via the island PSI.

### 5. Scopes, indexing, navigation

`RakuCondBranch` implements the plugin PSI element interface and acts as a
transparent statement container, so existing recursive walkers — declaration
collection, `DefaultStubBuilder` (walking expands lazy nodes), structure
view, find-usages — descend into islands without per-consumer changes.
jvm/js-only symbols become indexed, completable, and navigable.

### 6. Duplicate-symbol guard

A utility (`RakuCondBranch.inMutuallyExclusiveBranches(a, b)`) reports when
two elements live under conditions that cannot both be true. gen-cat has no
else-chains, so exclusivity is defined by the conditions themselves, not
adjacency: `X` vs `!X`, two different positive backends (`jvm` vs `js`), or
active-selection code vs any branch whose condition contradicts the active
backend. Redeclaration-style checks consult it so cross-branch twins (e.g.
`method now()` under `#?if jvm` and `#?if !jvm`) never flag each other.
Applied only where a real duplicate warning exists today.

## Testing

- **Parsing golden tests:** balanced branch → island containing real
  statement PSI; torn branch → errors confined to the island with the
  following file intact; adjacent alternative groups. Existing
  `ConditionalCompilationTest` golden output is regenerated deliberately.
- **Highlighter lexer test:** branch bodies produce real code tokens, not
  whitespace.
- **Editing test:** type near a directive and re-highlight (the reported
  incremental-highlighter breakage).
- **Index test:** a `sub` declared only under `#?if jvm` is findable in the
  stub index.
- **Guard test:** same-name declarations in `#?if jvm` / `#?if !jvm` do not
  flag each other.

## Out of scope

- Incremental reparse of islands (the file reparses fully on edit, as today).
- Backend names in structure-view presentation.
- `#?if` inside strings/regexes (unobserved in the 626 surveyed regions).
- Nested `#?if` (gen-cat treats nesting as an error; a second `#?if`
  re-decides, matching current behavior).
