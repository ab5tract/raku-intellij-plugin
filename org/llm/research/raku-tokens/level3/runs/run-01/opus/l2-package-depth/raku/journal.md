# l2-package-depth — Raku

- attempt-01: First and only version. Recursive `.java` walk under
  `src/main/java` via a `gather`/`take` helper, first line matching
  `^ 'package ' (.*) ';' $` per file, depth = segments of the trimmed capture
  split on `.`, tallied into a hash; depths printed ascending (numeric sort),
  then the `none` line. Ran clean: exit 0, empty stderr, output in the specified
  `<key><TAB><count>` shape with a trailing newline. Nothing to fix.
