# l6-todo-scan

## Input

Every file under `src/main/java/` (searched recursively) whose name ends in
`.java` **or** `.kt`.

## Task

Count the lines that contain the substring `TODO` or the substring `FIXME`, both
matched **case-sensitively**. A single line containing either or both counts
exactly once.

## Output

One line per input file whose count is 1 or more:

```
<count><TAB><path>
```

`<path>` is the file's path relative to the repository root, using `/` as the
separator. Sort by `<count>` descending; ties broken by `<path>` ascending
(ordinary codepoint order).

Then one final line:

```
TOTAL<TAB><n>
```

where `<n>` is the sum of all the counts. Every line ends in a newline.
