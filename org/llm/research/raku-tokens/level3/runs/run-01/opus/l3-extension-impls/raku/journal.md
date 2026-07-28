# journal — l3-extension-impls (raku)

- **attempt-01** — First and only attempt. Non-recursive `.dir` over
  `src/main/resources/META-INF` filtered to `.xml` files, `:g` match on
  `'implementation="' (<-["]>*) '"'`, distinct via a hash, `.sort` (Str cmp is
  codepoint order), simple name via `rindex(".")`. Ran clean: empty stderr,
  `80 distinct` plus 80 `simple<TAB>full` lines, trailing newline from `say`.
  Checked the two shapes that could have bitten: `implementationClass="` and
  `serviceImplementation="` both fail a literal `implementation="` match
  (next char is `C`, and the capital `I`), so no spurious hits.
