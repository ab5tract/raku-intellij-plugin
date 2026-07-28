# l12-first-line-census — python

- **attempt-01** — First and only attempt. Walked `src/main/java` recursively with
  `os.walk`, took `*.java` files, read just the first line with `readline()`,
  stripped the trailing `\n` and then a trailing `\r` (so CRLF files are not
  misread), and tested blank → `package ` → `//` or `/*` → other in the spec's
  order. An empty file yields `""` from `readline()`, which falls into the blank
  branch, matching "a file with no lines at all counts as `blank`". Printed the
  four fixed labels in order as `label<TAB><count>\n`. Ran clean (exit 0, empty
  stderr) and the output looked right, so no further attempts.
