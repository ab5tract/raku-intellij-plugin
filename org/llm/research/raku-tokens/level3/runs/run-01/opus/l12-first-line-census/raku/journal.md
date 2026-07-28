# l12-first-line-census — Raku

- **attempt-01** — First try. Used `'src/main/java'.IO.dir(:r)` believing `:r` made
  `.dir` recursive. It does not: `IO::Path.dir` is single-level, and because every
  Raku method accepts an implicit `*%_`, the bogus `:r` was silently swallowed
  instead of erroring. The only entry under `src/main/java` is the directory `org`,
  which fails the `.ends-with('.java')` filter, so the program exited 0 with all
  four counts at `0`.

- **attempt-02** — Replaced the `.dir(:r)` call with an explicit recursive walk
  (`gather`/`take slip` over `$path.d`), collecting paths whose basename ends in
  `.java`. Also swapped the whitespace-only test from a `/^ \s* $/` match to
  `.trim eq ''` for readability. Found 843 files, totals summing correctly:
  `package 840`, `comment 3`, `blank 0`, `other 0`. Accepted.
