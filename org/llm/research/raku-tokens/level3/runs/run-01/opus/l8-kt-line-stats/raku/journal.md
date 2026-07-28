# l8-kt-line-stats — Raku

- **attempt-01** — Collected files with `'src/main/java'.IO.dir(:recursive)`. There is no
  `:recursive` adverb on `IO::Path.dir`; Raku silently accepts it as an unused named argument,
  so `dir` returned only the immediate children of `src/main/java`, all of which are directories,
  and the `.f` filter left zero files. Output was `files 0 / lines 0 / nonblank 0` and then the
  mean line died with "Attempt to divide by zero when calling .floor on Rational" (exit 1).

- **attempt-02** — Replaced the fake `:recursive` with an explicit worklist walk (`@todo` stack,
  push `.dir` for directories, keep paths whose `.basename` ends with `.kt`). Nothing else changed.
  Ran clean: 253 files, 18454 lines, 16000 non-blank, mean 63.24. Verified the output is 4
  tab-separated lines each terminated by a newline (`cat -A`), and that the mean is rendered from
  exact integer hundredths (`floor(nonblank/files * 100 + 1/2)`), so half-away-from-zero rounding
  and the trailing zero in cases like `12.30` are both guaranteed rather than left to `%.2f`.
