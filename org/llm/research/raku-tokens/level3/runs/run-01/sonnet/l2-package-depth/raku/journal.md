attempt-01: Initial implementation. Recursively walks `src/main/java` (custom
recursive helper, since core `dir()` has no `:recurse` option), finds the
first line matching `^package (.*);$` per file, computes depth as the number
of `.`-separated segments in the trimmed package name, tallies counts per
depth, and prints ascending depth lines followed by the `none` line. Ran
clean on the first try: output summed to 843 (matching the total `.java`
file count under `src/main/java`), and the depth-3 bucket count matched a
manual spot-check of `RakuIcons.java` (`package org.raku.comma;`). No
changes needed.
