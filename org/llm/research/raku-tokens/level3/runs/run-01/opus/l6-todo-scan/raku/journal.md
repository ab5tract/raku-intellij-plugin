# l6-todo-scan — Raku

- **attempt-01** — First and only version. Recursive `.dir` walk from
  `src/main/java`, filtering on `.extension eq 'java' | 'kt'`, counting lines
  that `.contains('TODO') || .contains('FIXME')`, read with `:enc<utf8-c8>` so a
  stray non-UTF-8 byte could not abort the run. Sorted with
  `.sort({ -.value, .key })` for count-descending / path-ascending, emitted with
  `say` (which supplies the trailing newline on every line, including `TOTAL`).
  Ran clean: exit 0, empty stderr, 48 file lines plus `TOTAL\t59`. Spot-checked
  the tie ordering against codepoint order — uppercase filenames sorting before
  lowercase subdirectory names (`RakuIcons.java` before `actions/…`) and the
  space in `RakuFileCreationDialog .kt` are both consistent with plain codepoint
  comparison, so no fix was needed.
