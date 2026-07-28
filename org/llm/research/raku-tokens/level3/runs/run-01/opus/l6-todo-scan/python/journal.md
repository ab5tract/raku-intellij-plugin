# l6-todo-scan — Python

- **attempt-01** — First and only attempt. Walked `src/main/java` with `os.walk`,
  filtered `.java`/`.kt`, counted lines containing `TODO` or `FIXME`
  (case-sensitive substring, one count per line via `or`), emitted
  `<count>\t<path>` sorted by count descending then path ascending, then
  `TOTAL\t<n>`. Ran clean: exit 0, empty stderr, 48 file lines plus TOTAL 59,
  trailing newline present. No correction needed.
