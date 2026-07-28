# journal — l10-camel-segments (raku)

- attempt-01: First and only version. Recursive `gather`/`take` walk of `src/main/java`
  collecting `.java` files, basename stripped of the trailing 5 characters, split by an
  index-based scan applying the two boundary rules (lower/digit → upper; upper → upper
  followed by lower), segments lowercased into a hash, sorted by `(-.value, .key)` and
  the top 15 printed as `count<TAB>segment`. Ran clean: exit 0, empty stderr, 15 lines
  with a trailing newline.
