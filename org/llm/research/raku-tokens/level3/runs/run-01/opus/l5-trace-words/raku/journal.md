# journal

- attempt-01: First and only version. Reads the `.md` files directly in
  `org/llm/traces/` via `.dir(test => *.ends-with(".md"))` plus a `.f` filter so
  a directory named `*.md` could not sneak in, lowercases each file's text,
  `.comb(/ <[a..z]>+ /)` for maximal ASCII-letter runs, drops runs shorter than
  4 chars and stopwords, tallies into a hash, then sorts by `-.value, .key` and
  prints the top 12 as `count<TAB>word`. Ran clean: exit 0, empty stderr, 12
  lines each newline-terminated.
