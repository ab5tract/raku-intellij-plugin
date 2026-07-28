# journal — l4-longest-lines (Raku)

- **attempt-01** — First implementation: recursive `gather`/`take` walk of
  `src/test/kotlin` collecting `*.kt`, per-file `max 0, $file.lines.map(*.chars)`,
  sort by `(-len, path)`, print top 5. Wrong output: the first column of every
  line was the entire list of that file's line lengths instead of a single
  number. `max 0, $file.lines.map(*.chars)` passes the `Seq` as a *single*
  argument, so `max` compared `0` against the whole `Seq` and returned the `Seq`.
  (The file also carried a leftover first line, `my @files = "src/test/kotlin".IO.dir(...)`,
  left over from an abandoned non-recursive approach; it was dead code and did
  not error, but it did not belong.)
- **attempt-02** — Flattened the candidate list into `max` with a slip:
  `max 0, |$file.lines.map(*.chars)`. Also deleted the leftover `@files` line and
  renamed `dir-recursive` to `kt-files`. Correct: five lines, tab-separated,
  descending by length, trailing newline present.
