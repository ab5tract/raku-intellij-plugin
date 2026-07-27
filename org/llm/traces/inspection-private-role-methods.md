# MissingRoleMethodInspection false-positived on private role methods

**Fix:** merged to `main` via PR #47; originally `da14d6fa` on `2026.2-beta.3`.
Regression test: `src/test/kotlin/org/raku/comma/inspection/MissingRoleMethodInspectionTest.kt`.

## Symptom

`MissingRoleMethodInspection` fired `Composed roles require to implement methods:
!clone-rows` on a class that `does` a role stubbing a **private** method it didn't
implement. In the reported case, `Math::Matrix does Math::Matrix::Util`, and
`Util` contains `method !clone-rows { ... }` (a yada stub), which `Math::Matrix`
does not implement.

## The trap we nearly fell into

This looked at first like it might be a false *negative* (a multi-file test reported
0 problems) or an empty-`{}`-body handling bug. Both were wrong. The decisive move
was to **ask the actual language**:

```
$ raku -e 'role R { method foo { ... } }; class C does R { }; C.new; say "ok"'
===SORRY!=== Method 'foo' must be implemented by C because it is required by roles: R.

$ raku -e 'role R { method !foo { ... } }; class C does R { }; C.new; say "ok"'
ok
```

**Raku enforces public yada-stub role methods as hard composition requirements
(compile error if unimplemented), but does NOT enforce private (`!`-twigil) ones.**
A class may `does` a role that stubs a private method and compile fine without it.
`raku -I. -MMath::Matrix -e 'say "hi"'` loading cleanly was the tell that the
inspection, not the library, was wrong.

(The earlier confusion came from a local edit that had temporarily added
`method !clone-rows {}` to the working copy; with the real git tree, the method is
genuinely absent — and correctly so, because Raku doesn't require it.)

## Fix

`MissingRoleMethodInspection.gatherRoleStubs` now skips private methods entirely when
collecting methods-to-implement:
```kotlin
if (maybeMethod.isPrivate) continue   // RakuRoutineDecl already exposes isPrivate()
```
A private method is never a requirement, and also can never *satisfy* a public
requirement (different namespaces), so excluding them outright is correct.

## Principle

When an inspection encodes a language rule, verify the rule against the compiler
itself (`raku -e '...'`) before assuming the inspection or the user's code is at
fault. A one-line rakudo invocation settled a question that source-reading and PSI
inspection had left ambiguous.

## Regression coverage (direct-invocation, not checkHighlighting)

`MissingRoleMethodInspectionTest` instantiates the inspection and asserts on
`ProblemsHolder.results`, deliberately bypassing the broken `checkHighlighting`
pipeline (see `test-harness-and-environment.md`). Cases: public stub missing → fires;
public stub implemented (normal and empty `{}` body) → clean; private stub missing →
must NOT fire; private stub implemented → clean.
