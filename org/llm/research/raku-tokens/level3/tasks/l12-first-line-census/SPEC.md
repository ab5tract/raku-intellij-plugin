# l12-first-line-census

## Input

Every file under `src/main/java/` (searched recursively) whose name ends in `.java`.

## Task

Classify each file by its **first line**, testing in this order and stopping at the
first match:

- `blank` — the line is empty or contains only whitespace
- `package` — the line starts with `package ` (no leading whitespace)
- `comment` — the line starts with `//` or `/*` (no leading whitespace)
- `other` — anything else

A file with no lines at all counts as `blank`.

## Output

Exactly 4 lines, always all four printed even when a count is zero, in this fixed
order, each ending in a newline:

```
package<TAB><count>
comment<TAB><count>
blank<TAB><count>
other<TAB><count>
```
