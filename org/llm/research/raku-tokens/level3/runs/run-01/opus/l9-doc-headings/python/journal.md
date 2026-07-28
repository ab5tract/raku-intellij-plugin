# journal — l9-doc-headings (Python)

- **attempt-01** — First and only version. Non-recursive `os.listdir("docs")`
  filtered to `.md` regular files, per-line `^(#{1,6}) ` match (greedy + the
  required trailing space makes 7-or-more `#` fail to match, as the spec wants),
  paths sorted by plain string comparison, counts joined with commas, `TOTAL`
  line last. Ran clean: empty stderr, exit 0, output verified by eye against
  `docs/completion.md` (1 level-3, 3 level-4, 6 level-5). No changes needed.
