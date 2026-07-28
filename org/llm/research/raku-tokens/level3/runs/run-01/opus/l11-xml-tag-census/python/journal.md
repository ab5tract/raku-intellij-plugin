# l11-xml-tag-census — python

- **attempt-01** — First and only version. Non-recursive `os.listdir` over
  `src/main/resources/META-INF`, filtered to names ending `.xml` that are regular
  files (this drops the `meta/` subdirectory and `pluginIcon.svg`). Scanned each
  file's raw text with `<([A-Za-z][A-Za-z0-9._:-]*)`, tallied into a `Counter`,
  sorted by `(-count, name)` so ties fall back to codepoint-ascending name, and
  printed the top 10 as `count<TAB>name` followed by `DISTINCT<TAB>109`. Ran
  clean: exit 0, empty stderr, 11 newline-terminated lines. Nothing to fix.
