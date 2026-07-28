# journal — l1-import-tally (Python)

- **attempt-01.py** — first and only version. `os.walk` over `src/main/java`,
  filter `*.java`, strip only the line terminator (`rstrip("\r\n")`) so leading
  whitespace stays significant and `endswith(";")` still works on the last line
  of a file without a trailing newline; test `startswith("import ")` and
  `endswith(";")`; name is `line[7:-1].strip()`. Counted in a `Counter`, sorted
  by `(-count, name)` so ties fall to ascending codepoint order (Python's
  default `str` comparison), took the first 10, then the `TOTAL` line. Ran
  clean: empty stderr, exit 0, 11 lines each newline-terminated. Nothing to fix,
  so there is no attempt-02.
