# journal

- **attempt-01** — First and only attempt. Listed `src/main/resources/META-INF/`
  non-recursively, kept regular files ending in `.xml`, scanned each with
  `\bimplementation="([^"]*)"` (the `\b` keeps `implementationClass="` and
  `serviceImplementation="` from matching), collected the values in a set,
  printed `<N> distinct` then `simple<TAB>full` for each value sorted with
  Python's default string ordering (codepoint order). Ran clean: exit 0, empty
  stderr, 80 distinct values, header plus 80 lines each newline-terminated.
  Nothing was wrong with it, so there was no attempt-02.
