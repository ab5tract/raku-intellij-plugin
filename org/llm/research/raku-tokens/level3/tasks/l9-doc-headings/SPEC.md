# l9-doc-headings

## Input

Files sitting **directly** in `docs/` (do not recurse) whose name ends in `.md`.

## Task

A line is a **heading** if it begins with 1 to 6 `#` characters followed by a single
space. Its **level** is the number of leading `#`. A line beginning with 7 or more
`#`, or with `#` not followed by a space, is not a heading.

Do not attempt to skip fenced code blocks — treat every line of the file the same way.

For each file, count its headings at each level 1 through 6.

## Output

One line per input file, sorted by path ascending (ordinary codepoint order):

```
<path><TAB><c1>,<c2>,<c3>,<c4>,<c5>,<c6>
```

`<path>` is relative to the repository root, e.g. `docs/testing.md`. The six counts
are always all printed, joined by commas with no spaces, including zeros.

Then one final line, with the same six counts summed over every file:

```
TOTAL<TAB><c1>,<c2>,<c3>,<c4>,<c5>,<c6>
```

Every line ends in a newline.
