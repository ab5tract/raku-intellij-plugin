# The light project was rebuilt for every test (~69 min → ~2 min)

**Symptom:** `./gradlew test` took over an hour for 1087 tests; a single class
(`AnnotationTest`, 183 methods) took ~12 minutes. Two tests failed with
`AssertionError: Too many projects leaked`, and a scattering of symbol-dependent
highlighting assertions failed non-deterministically. Earlier notes in this directory
concluded the `checkHighlighting()` pipeline was "broken in this environment" and
advised running a curated subset instead of the full suite. All of that was one bug.

**Fix:** this session. Suite is now green: 1087 tests, ~2m15s.

## Root cause

`LightPlatformTestCase.doSetup` reuses the light project only while the descriptor
compares equal to the previous test's:

```java
if (ourProject == null || ourProjectDescriptor == null || !ourProjectDescriptor.equals(descriptor)) {
    initProject(descriptor);   // closeAndDeleteProject() + ProjectManagerEx.newProject()
}
```

`com.intellij.testFramework.LightProjectDescriptor` declares **no `equals`/`hashCode`**
— that comparison is object identity. `CommaFixtureTestCase.getProjectDescriptor()`
returned `RakuLightProjectDescriptor()`, a *fresh instance every call*, so the branch
was taken on every one of ~1087 test methods. (The platform's own default returns the
`EMPTY_PROJECT_DESCRIPTOR` **static singleton** — that is how reuse normally happens.)

Everything else followed from that:

- `RakuProjectSdkService` is `@Service(Service.Level.PROJECT)` and its
  `settingsStarted`/`settingJson` were per-instance, so each new project respawned
  `raku raku-core-symbols.raku` (**measured 4.0s**) and re-parsed ~3MB of JSON. 1087 ×
  4s ≈ 69 minutes.
- `TestProjectManager.checkProjectLeaksInTests` (threshold `MAX_LEAKY_PROJECTS = 5`,
  `LEAK_CHECK_INTERVAL` 30 min) is called from `newProject`. At ~69 minutes of wall
  clock it fired exactly twice → exactly two leak failures, landing on whichever test
  happened to be running. `DocumentationTest.testQuickConstant` and
  `RenameTest.testRenameOfPrivateMethodFromNameWithdash` were arbitrary victims and
  always passed in isolation.
- Under that subprocess load a `raku` spawn would occasionally fail or return empty,
  and `getCoreSettingFile()` **silently** fell back to the bundled
  `symbols/CORE.fallback` — an older, less complete dump. That is why
  `AnnotationTest.testCallArityMismatchAnnotating` lost its `.perl` deprecation
  warning: live Rakudo symbols carry `"x":"raku"` for `Mu.perl`, `CORE.fallback` does
  not. Passed alone, failed in a full run.

## The trap when you fix it

Making the descriptor a singleton on its own breaks 182 of 183 tests with
`InvalidVirtualFileAccessException: Accessing invalid virtual file:
/tmp/unitTest_<firstTestName>_*/lib`.

`UsefulTestCase.setUp` redirects FileUtil's canonical temp path to a fresh
`/tmp/unitTest_<testName>_*` per test and deletes it in `tearDown`.
`RakuLightProjectDescriptor` rooted its module at `FileUtil.getTempDirectory()`, so a
*reused* project kept pointing at the first test's content root after that directory
was deleted.

The redirect changes FileUtil's cache, **not** the `java.io.tmpdir` system property.
So `RakuLightProjectDescriptor` now derives its base directory from
`System.getProperty("java.io.tmpdir")` once per JVM and overrides
`generateProjectPath()` to sit under it. `RakuMultiModuleProjectDescriptor` is a
Kotlin `object` with its own `baseDirPrefix`.

## Everything changed

- `CommaFixtureTestCase.getProjectDescriptor()` → `RakuLightProjectDescriptor.INSTANCE`;
  `RakuMultiModuleProjectDescriptor` → `object`; both anchored to a stable base dir.
- `RakuProjectSdkService.setProjectSdkPath` early-returns when the path is unchanged.
  `CommaFixtureTestCase.setUp` calls it before every test, and it was paying for a
  `raku -e` version probe, a symbol-cache invalidation, and a dependency-refreshing
  meta build each time.
- CORE.setting JSON moved to `RakuGlobalSdkSymbolCache` (`@Service(APP)`), keyed by SDK
  path, alongside the pre-existing module caches. The PSI stays project-scoped; only
  the string is shared. A failed load releases its `settingLoadsStarted` claim so a
  later caller can retry instead of being pinned to the fallback forever.
- Same claim-release bug fixed in `getPsiFileForModule`: `packagesStarted` is a
  permanent app-level set, and the pooled task bailed on `project.isDisposed` without
  removing the name — poisoning that module for the rest of the JVM and making every
  later `ensureModuleIsLoaded` burn its full 120s timeout.
- `tasks.test { maxHeapSize = "3g" }` — Gradle's 512MB default GC-thrashes an
  in-process IntelliJ test application long before it OOMs.

## Transferable principle

Anything the platform keys on **identity** (light project descriptors here) must be a
singleton, and a per-test factory call is the natural way to get that wrong silently —
nothing fails, it just gets slow. And when a slow suite starts producing flaky,
symptom-diverse failures, suspect one shared resource degrading under load before
believing several unrelated diagnoses. Two of the "6 pre-existing failures" in the
accepted baseline had no bug of their own at all.
