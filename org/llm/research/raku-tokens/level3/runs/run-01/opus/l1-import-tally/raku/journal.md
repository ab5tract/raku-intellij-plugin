# l1-import-tally — Raku

- **attempt-01** — First and only version. Recursive `gather`/`take` directory walk
  over `src/main/java`, filtering on `.basename.ends-with('.java')`; per line,
  `starts-with('import ')` and `ends-with(';')`, name taken as
  `$line.substr(7, *-1).trim`; tallied into a plain `%counts` hash and sorted with
  `.sort({ (-.value, .key) })` so count descends and `cmp` on the name string gives
  codepoint-ascending tie-breaks. `say` supplies the trailing newline on all 11
  lines. Ran clean on the first go (empty stderr, exit 0) and the output matched the
  spec's shape, so no further attempts were needed.
