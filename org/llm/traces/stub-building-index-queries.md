# Stub building must not query the stub index (a whole class of latent bug)

**Fix (partial, by design):** merged to `main` via PR #47; originally `c560d9b4` on
`2026.2-beta.3`.

## The platform rule

IntelliJ forbids stub building from depending on index data. When
`StubElementType.createStub()` (or anything it transitively calls) queries a stub
index, the platform logs `Stub building must not rely on data from indexes ...`
(`FileBasedIndexImpl.ensureUpToDate` → `Logger.error`). Building the stub for file X
must not depend on the very index it is helping populate.

- **In production:** this degrades **silently** — the offending resolution returns an
  empty/wrong result and building continues. No crash, no visible error. Symptoms are
  subtle (wrong/missing symbols until a reindex), which is why these bugs lurk.
- **Under the test harness:** `-Dintellij.testFramework.rethrow.logged.errors=true`
  (set for all test JVMs here) escalates that `Logger.error` to a hard
  `TestLoggerAssertionError`. So a test can "crash" on something that a real IDE just
  shrugs off. Keep that asymmetry in mind — see `test-harness-and-environment.md`.

## The specific instance fixed

`RakuVariableDeclStubElementType.createStub()` called `psi.inferType()`. For `@`/`%`
-sigil variables with an `is` trait (`has @!foo is SomeCustomArrayClass;`),
`inferType()` resolves the trait via `RakuIsTraitReference.resolve()` — a cross-file,
index-backed lookup — illegal during stub building.

**Fix:** added `RakuVariableDeclImpl.inferTypeForStub()`, identical to `inferType()`
except it skips the `is`-trait-as-base-type resolution, and `createStub()` calls it.
Real callers still get the fully-resolved type from `inferType()` once the PSI is
materialized. (This was the deliberately **narrow** "Option A" — fix the one illegal
path, not a broad "detect stub-building context inside resolve()".)

## Still open / known-incomplete

Verification during the fix surfaced a **second** index-query path via
`RakuTypeNameImpl.inferType()` → `RakuTypeNameReference.resolve()` (resolving explicit
type annotations like `Int`/`Bool`), which is far more pervasive — it triggers for
nearly any explicitly-typed declaration. It was **not** fixed, for two reasons:
1. The narrow Option A was the chosen scope.
2. This class of bug degrades silently in production (see above), so it is not the
   cause of any visible user-facing breakage — it is latent correctness debt, not a
   live fire.

If you pick this up: the general options are (a) keep whittling individual illegal
paths stub-safe (like Option A), or (b) a broader "am I inside stub building?" guard
that resolve() can consult. (b) is more invasive and needs care not to change
resolution semantics for real callers. Prefer (a) unless the paths multiply.

## How to detect it

Any new symptom that only fails under the test harness with the "Stub building must
not rely on data from indexes" message is this class of bug. In a scratch test you
can scope-suppress *only that message* via `LoggedErrorProcessor` to see what else is
going on underneath, but the real fix is to make the `createStub` path index-free.
