# l8-kt-line-stats — python

- **attempt-01** — First and only attempt. Ran clean (exit 0, empty stderr) and the
  output matched the spec, so no further attempts were needed.

Notes on the two things that looked like they could bite:

- The trailing-newline rule: `text.split("\n")` yields a trailing `""` for any file
  ending in `\n`, so that piece is popped before counting. This also makes a
  zero-byte file count as 0 lines rather than 1.
- The rounding rule: `round()` and `%.2f` both use banker's rounding in Python, which
  is not "half away from zero". Used `Decimal(nonblank) / Decimal(files)` with
  `quantize(Decimal("0.01"), ROUND_HALF_UP)` instead, which also gives the required
  fixed 2-decimal formatting for free.
