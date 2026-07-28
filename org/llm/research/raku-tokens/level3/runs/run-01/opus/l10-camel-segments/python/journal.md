# l10-camel-segments — Python

- **attempt-01** — First and only attempt. Walked `src/main/java/` with `os.walk`,
  stripped the trailing `.java`, split each basename by scanning for the two
  documented boundaries (lower-or-digit followed by upper; upper followed by
  upper-then-lower), lowercased the segments into a `Counter`, sorted by
  `(-count, segment)` and printed the top 15 as `count<TAB>segment`. Ran clean,
  empty stderr, 15 lines with a trailing newline. Verified the splitter by hand
  against the three spec examples (`RakuPSIElement`, `HTTPServer`,
  `Base64Decoder`); nothing to fix.
