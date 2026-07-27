# Re-declaration of imported symbols: annotator → inspection

**Landed:** this session. Test: `RedeclaredImportedSymbolInspectionTest` (6 cases) plus
the end-to-end golden case `RakuHighlightTest.testDuplicatesInExternal`.

## The symptom

`RakuHighlightTest.testDuplicatesInExternal` had been red since **September 2024**. Its
golden file `testData/highlight/User.rakumod` expects

```
use Base;

<error descr="Re-declaration of foo from Base.rakumod:1">sub foo</error> {}
class <error descr="Re-declaration of C from Base.rakumod:3">C</error> {}
```

but neither error was produced.

## Why

`RakuHighlightVisitor.visitUseStatement` — the code that fed declarations imported via
`use` into the same duplicate pool as in-file ones — was commented out wholesale in
`3f854f3c` ("Migrate all annotations to inspections"), with the note:

> Visiting `use` statements has been broken forever. Exported routines have never
> worked. Yet this routine is there, always spinning and doing too much work for an
> operation on EDT.

So the test was asserting a feature that had been deliberately switched off. It was not
a regression, and it was not going to be fixed by re-enabling the old code — that code
was disabled *because* it was expensive and ran on the EDT.

## The check is real, though

Rakudo rejects this outright, so ERROR severity is correct and it applies to any
`use`d module, not just project-local ones:

```
$ raku -e 'use lib "lib"; use Base; sub foo { }'
===SORRY!=== Redeclaration of routine 'foo'. Did you mean to declare a multi-sub?
```

Note the hint in that message — a `multi` is the supported way to add a candidate to an
imported routine, so the inspection must not flag multis.

## The reimplementation

`RedeclaredImportedSymbolInspection` (registered `RakuRedeclaredImportedSymbol`,
level `ERROR`, group `Declarations`). It addresses each of the original's problems:

- **Off the EDT.** Inspections run in the daemon, not in the highlighting visitor.
- **Cheap.** The imported name → declaring-PSI map is built once per file via
  `CachedValuesManager`, keyed on `PsiModificationTracker.MODIFICATION_COUNT` (the
  result depends on the imported files as much as on this one), rather than once per
  declaration. Files with no `use` statement short-circuit immediately.
- **Disableable**, which was the stated motivation for the whole 2024 migration.

Behaviour, with a test pinning each: fires for a re-declared imported routine and for a
re-declared imported package; does **not** fire for a `multi` candidate, for an
unrelated name, for a same-file duplicate (that is still `RakuHighlightVisitor`'s job,
and double-reporting would be worse than not reporting), or when there is no `use`.

Ranges match the existing in-file duplicate marking exactly — `declaratorNode`..end of
name identifier for routines (`sub foo`), name identifier only for packages (`C`).

`RakuHighlightTest` does not call `enableInspections`, since its other cases come from
the always-on `RakuHighlightVisitor`; `testDuplicatesInExternal` now enables this one
inspection explicitly, which is also the clearest signal to the next reader that this
particular check moved.

## Transferable principle

A long-red golden test is worth `git log -L`-ing before debugging. This one had a
two-year-old, explicitly-documented cause sitting in a comment at the exact call site,
and no amount of tracing the resolution path would have found it.
