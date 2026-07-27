# Reduce-metaop mis-lexing that collapsed highlighting mid-file

**Fix:** merged to `main` via PR #47 ("2026.2 beta.3"); originally committed on
branch `2026.2-beta.3` as `4aade331`. Regression test:
`testData/parsing/reduce-metaop-bug/` + `ReduceMetaopBugTest`.

**Prerequisite reading:** `parser-generated-lexer-architecture.md` (this is a fix to
that generated machine; the mirror-fix discipline and debug methodology come from
there).

## Symptom

A user reported syntax highlighting "stops completely at line 307" of a ~930-line
`Math::Matrix.rakumod`. Everything after that point rendered as one undifferentiated
blob. It was not a crash — it was a single mis-parsed statement swallowing the rest
of the file, which then made an unrelated inspection (`UselessUseInspection`, "useless
use in sink context") appear to highlight the entire tail of the file.

The trigger on line 307 was a reduce meta-operator applied to a `map` whose block
contained a nested `map`:
```raku
my Bool $rowwise = [and] map { &$greater(@!rows[$^r][$^r] * 2,
                              [+](map {abs $^c}, @!rows[$^r].flat)) }, ^$!row-count;
```

## Two distinct root causes (both: "the metaop machinery accepted something it
shouldn't", so `[...]` fell through to the array-composer branch and left trailing
tokens orphaned as `BAD_CHARACTER`)

### Bug 1 — wordy reduce operators (`[and]`, `[or]`, `[xor]`) not recognized

`term_reduce`'s lookahead (`perl6.pm6`) is `<?before ['[' [infixish('red')] ']']>`.
It calls `infixish` → `token infix`, which ends with a precedence guard:
```
<!{ $*PREC le $*PRECLIM }>          # perl6.pm6 line ~4260
```
That guard is an **expression-precedence-climbing** concern. But when we're merely
*identifying the base operator of a meta-operator*, `$*PRECLIM` still holds the
enclosing assignment-RHS limit — so loose-precedence operators get filtered out.
Symbolic ops slipped through (`[+]` is `t=`, `[*]` is `u=`); wordy ones did not
(`and` is `d=`, `or` is `c=`). Confirmed empirically by tracing `term_reduce`: for
`[+]`/`[*]` it reached its final state; for `[and]` it never left state 0 (the
lookahead failed).

**Fix:** only apply the precedence guard in ordinary infix position (`$*IN_META`
empty). Generated code `MAINBraid.java` `_225_infix()` case 177:
```java
if (!this.isValueTruthy(this.findDynamicVariable("$*IN_META"))
        && this.isValueTruthy(this.testStrLE(findDynamicVariable("$*PREC"),
                                              findDynamicVariable("$*PRECLIM")))) { ... }
```
Grammar mirror `perl6.pm6`: `[ <?{ $*IN_META }> || <!{ $*PREC le $*PRECLIM }> ]`.

### Bug 2 — `[[]]` (empty inner array) mis-parsed as a reduce metaop

`[[]]` was lexed as a reduce metaop `[ [] ]`: the reduce lookahead accepted `[[]`
because `infixish` matched an **empty / incomplete bracketed infix** (a `[` with no
operator inside) via the editor-tolerance fallback in `infixish_non_assignment_meta`.
That consumed the *inner* `]` as the reduce's closing bracket and orphaned the *outer*
`]` → `BAD_CHARACTER` cascade to EOF. Minimization proved `xx` was a red herring:
`[[]]` alone breaks; `[[1] xx 3]` (non-empty inner) is fine; `([] xx 3)` (parens) is
fine. The trigger is specifically `[[` — an array composer whose first element is an
empty array.

**Fix:** a meta-operator's base must be a *complete* operator, so the incomplete
bracketed-infix fallback must not fire in a meta context. `MAINBraid.java`
`_223_infixish_non_assignment_meta()` case 14 (the standalone incomplete fallback):
guard it with `if (isValueTruthy(findDynamicVariable("$*IN_META"))) { backtrack... }`
so it fails in meta context, letting `[[]]` fall through to nested array composers.
Grammar mirror `perl6.pm6` line ~4063: prefix the fallback alternative with
`<!{ $*IN_META }>`.

## The transferable principle

> **A meta-operator (`[op]` reduce, `Xop` cross, `Zop` zip, `>>op<<` hyper, `Rop`,
> `Sop`, negated) wraps a *complete* base operator, and does so regardless of the
> surrounding expression's precedence limit.**

Both fixes are the same idea from two angles: guard-on-`$*IN_META`. Any future
"metaop X isn't recognized" or "metaop Y swallows the file" bug should first be
checked against this principle — is some ordinary-infix-position constraint
(precedence, completeness, a stopper) leaking into meta-operator identification?

## Why it looked intermittent / "sometimes worked"

Symbolic reduces always worked, wordy ones never did, and the file-swallow only
became visually catastrophic when a nested brace (`map {...}` inside `map {...}`)
made error-recovery mis-count and swallow to EOF instead of recovering at the next
`}`. Same mis-lex underneath; different blast radius. Don't be misled by "it works
sometimes" — reduce it to the minimal trigger (canary method).

## Regression coverage

`testData/parsing/reduce-metaop-bug/ParsingTestData.p6` exercises wordy/symbolic/
nested/bracketed reduces and empty-array composers; its golden `.txt` has no error or
`BAD_CHARACTER` nodes. That absence is the assertion.
