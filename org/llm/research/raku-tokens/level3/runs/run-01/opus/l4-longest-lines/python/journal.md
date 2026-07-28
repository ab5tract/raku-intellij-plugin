# journal — l4-longest-lines (Python)

- **attempt-01** — First and only version. Walked `src/test/kotlin` with
  `os.walk`, kept `*.kt`, read each file as bytes and decoded UTF-8 so lengths
  count Unicode characters rather than bytes, split on an explicit
  `\r\n|\r|\n` regex (deliberately not `str.splitlines()`, which also breaks on
  vertical tab, form feed, the file/group/record separators, NEL, LINE
  SEPARATOR and PARAGRAPH SEPARATOR, and so would under-report the max for any
  line containing one of them), took the max segment length, sorted by
  `(-maxlen, path)`, and printed the top five as `maxlen<TAB>path\n`. An empty
  file splits to `[""]`, giving 0 as the spec requires. Ran clean, exit 0, empty
  stderr; output accepted.
