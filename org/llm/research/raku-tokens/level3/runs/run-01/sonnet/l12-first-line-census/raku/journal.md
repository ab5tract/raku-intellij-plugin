- attempt-01: Recursive walk of `src/main/java` collecting `.java` files, classify by
  first line (blank/package/comment/other) in spec order, print fixed 4-line report.
  Ran cleanly on first try: exit 0, empty stderr, output `package 840 / comment 3 /
  blank 0 / other 0` summing to 843, matching the total `.java` file count found
  independently. No issues found; no further attempts needed.
