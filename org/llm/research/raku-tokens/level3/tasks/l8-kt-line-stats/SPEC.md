# l8-kt-line-stats

## Input

Every file under `src/main/java/` (searched recursively) whose name ends in `.kt`.
(Yes, `.kt` under `src/main/java` — that is how this repository is laid out.)

## Task

Split each file's text on `\n`. A trailing empty piece after a final `\n` is **not**
a line: a file whose text is `"a\nb\n"` has exactly 2 lines.

A line is **blank** if it is empty or contains only whitespace.

Compute:

- `files` — the number of input files
- `lines` — total lines across all of them
- `nonblank` — total non-blank lines
- `mean` — `nonblank / files`, **rounded half away from zero to exactly 2 decimal
  places** and always printed with exactly 2 decimals (`12.30`, not `12.3`)

## Output

Exactly 4 lines, in this order, each ending in a newline:

```
files<TAB><n>
lines<TAB><n>
nonblank<TAB><n>
mean<TAB><x>
```
