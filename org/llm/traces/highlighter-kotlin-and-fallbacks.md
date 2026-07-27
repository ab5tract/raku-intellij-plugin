# Syntax highlighting: Kotlin conversion + committing to fallbacks

## Landing point

Four Java files in `org/raku/comma/highlighter/` converted to Kotlin
(`RakuHighlighter`, `RakuSyntaxHighlighter`, `RakuSyntaxHighlighterFactory`,
`RakuColorSettingsPage`), the two bundled color schemes cut from ~37 overrides
to 5, and `RakuColorSettingsPageTest` (11 tests) added. Full suite green at
1104 tests.

## The premise was half wrong, and that mattered

The task came in as "migrate to the modern approach which uses fallbacks".
`RakuHighlighter.java` **already** used
`createTextAttributesKey(externalName, fallback)` for all 91 keys, and so did
`CroTemplateHighlighter.java`. There was nothing to modernize at the
declaration site.

What actually defeated the fallbacks was `colorSchemes/RakuDefault.xml` and
`RakuDarcula.xml`, which hardcoded foregrounds for ~37 of those keys. The
effect: **Raku looked one way under Default/Darcula and a completely different
way under every third-party theme**, which only ever saw the fallbacks. Nobody
had noticed because the two shipped schemes are what most people use.

If you land here with a similar-sounding request, check
`<additionalTextAttributes>` before assuming the key declarations are the
problem.

## Three hand-maintained lists that had drifted

The real defect class was one concept spread across three files with no link
between them:

| List | Count | Drift |
|---|---|---|
| Key declarations (`RakuHighlighter.java`) | 91 | — |
| Descriptors (`RakuColorSettingsPage.java`) | 89 rows / 88 keys | `HASH_COMPOSER`, `UNUSED`, `ALT_WARNING` unreachable |
| `RakuDefault.xml` / `RakuDarcula.xml` | 37 / 39 | 4 keys present in one and not the other |

Concrete bug, `RakuColorSettingsPage.java:72`:

```java
new AttributesDescriptor("Array Composer ([...])", RakuHighlighter.ARRAY_COMPOSER),
new AttributesDescriptor("Hash Composer ({...})",  RakuHighlighter.ARRAY_COMPOSER),  // <-- wrong key
```

A user editing "Hash Composer" silently recolored array composers.

The fix is structural, not a corrected constant: each key now declares its
external name, fallback, panel group and label **once**, in `RakuHighlighter.kt`,
and registers itself into `entries` as a side effect of the private `key()`
helper. `RakuColorSettingsPage` composes the whole tree from `panelEntries`.
The two lists can no longer disagree because there is one list.

## HASH_COMPOSER is dead, and stayed dead deliberately

While wiring "Hash composer" to the right key, it turned out there is no
`RakuTokenTypes.HASH_COMPOSER` at all — `{...}` lexes as
`BLOCK_CURLY_BRACKET_OPEN/CLOSE`. So the key has never been applied by anything.

Fixing that is a lexer change and out of scope. The key is kept (its external
name is frozen; deleting it would break saved schemes) but carries
`inPanel = false`, so the settings page does not offer a control that visibly
does nothing. `testHashComposerIsDeclaredButNotOffered` characterizes this so
the next person finds the reason rather than the symptom.

## What survived the scheme trim, and why

Five overrides, per `docs/color-principles.md`:

- `RAKU_TEXT_BOLD` / `RAKU_TEXT_ITALIC` / `RAKU_TEXT_UNDERLINE` — Pod `B<>`,
  `I<>`, `U<>` must render bold/italic/underlined to mean anything, and
  `DOC_COMMENT` carries a color but no font style.
- `RAKU_REGEX_SIG_SPACE` — a blank run; with no effect there is nothing to see.
- `RAKU_ALT_WARNING` — exists precisely to differ from the ordinary weak
  warning it falls back to.

`testSchemesOverrideOnlyWhatFallbacksCannotExpress` pins that set, so a sixth
override has to be argued for rather than accreted.

**User-visible consequence, accepted deliberately:** Raku code under Default and
Darcula no longer shows the old dark-red operators / purple variables / indigo
types. It now follows the active scheme, consistently with every other theme.

## Two traps worth knowing

1. **Kotlin nests block comments.** A KDoc containing the literal
   ``` `colorSchemes/*.xml` ``` opens a nested comment that never closes, and the
   compiler reports `Unclosed comment` at the *end of the file* — 700 lines from
   the cause — plus a cascade of `Unresolved reference` errors in every consumer.
   Java would have accepted it.
2. **`--` is illegal inside an XML comment.** Writing an em-dash as `--` in the
   scheme files produced a `JDOMException` at *plugin load*, which surfaced as
   21 unrelated-looking highlighting test failures
   (`TestLoggerAssertionError` → `JDOMException` → `WFCException`). The suite
   does catch a malformed scheme file; the stack trace just doesn't say so.

## Java interop

`RakuHighlighter` is a Kotlin `object` with `@JvmField` on every key, so
`RakuHighlighter.REGEX_SIG_SPACE` still resolves from Java — verified with
`javap` (91 `public static final TextAttributesKey` fields). This matters
because `editor/SigSpaceAnnotator.java` is still Java and was left alone.

## Deliberately out of scope

- `RakuHighlightVisitor.java` (458 lines) — lives in `highlighter/` and is
  registered as `<highlightVisitor>`, but references **zero** attribute keys.
  It reports duplicate declarations, and carries its own TODO about becoming an
  inspection. Not a color concern.
- `cro/template/highlighter/` — structurally identical old-Java shape, already
  using fallbacks. Same treatment would apply cleanly if someone wants it.
- The `RAKU_REGEX_SIG_SPACE` fallback is `FUNCTION_CALL`, which looks odd until
  you remember sigspace is an implicit `<.ws>` call. Left as-is; changing a
  fallback is a visual change, not a refactor.
