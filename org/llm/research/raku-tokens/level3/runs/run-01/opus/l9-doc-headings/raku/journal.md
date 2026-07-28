# journal — l9-doc-headings (raku)

- **attempt-01** — first and only version. Non-recursive `"docs".IO.dir`, filtered to
  regular files ending in `.md`, sorted by the stringified path (Raku's `Str` `cmp` is
  codepoint order, so `docs/test-plan.md` correctly precedes `docs/testing.md`).
  Per line, matched `/ ^ ('#' ** 1..6) ' ' /` and bumped the bucket at `$0.chars - 1`;
  the quantifier backtracks, so a 7-`#` line fails at every arity and is correctly not
  a heading. No fenced-block handling, per the spec. Ran clean, empty stderr, output
  matched expectations on a spot check of `docs/testing.md` (one level-3 heading).
  Nothing to fix, so there is no attempt-02.
