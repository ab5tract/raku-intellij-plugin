# l7-duplicate-basenames — Raku

- **attempt-01**: First and only version. Recursive `gather`/`take` walk over
  `src/main/java/`, filter to names ending in `.java`/`.kt`, strip the final
  extension with `substr(0, rindex("."))`, tally into a hash, keep entries with
  count > 1, sort by `(-.value, .key)` so count descends and basename ascends in
  codepoint order, print the header and one `count<TAB>basename` line each via
  `say`. Ran clean: exit 0, empty stderr, output as expected.
