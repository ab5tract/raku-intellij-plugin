# journal — l2-package-depth (python)

- **attempt-01** — First and only version. Walked `src/main/java` with `os.walk`,
  took `*.java` files, scanned each line for the first one starting with
  `package ` and ending in `;` (after stripping the trailing newline / CR),
  split the trimmed name on `.` to get the depth, tallied, and printed depths in
  ascending order followed by the mandatory `none<TAB><count>` line. Ran clean:
  exit 0, empty stderr, output as expected.
