# l4-longest-lines

## Input

Every file under `src/test/kotlin/` (searched recursively) whose name ends in `.kt`.

## Task

For each file, find the length of its longest line, measured in **Unicode
characters** and **excluding** the line terminator. A file with no lines at all has
a longest-line length of 0.

## Output

Exactly 5 lines, each ending in a newline — the five files with the greatest
longest-line length:

```
<maxlen><TAB><path>
```

`<path>` is the file's path relative to the repository root, using `/` as the
separator, e.g. `src/test/kotlin/org/raku/comma/Foo.kt`.

Sort by `<maxlen>` descending; ties broken by `<path>` ascending (ordinary
codepoint order).
