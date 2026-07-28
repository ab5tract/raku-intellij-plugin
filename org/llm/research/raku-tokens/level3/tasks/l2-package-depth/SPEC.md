# l2-package-depth

## Input

Every file under `src/main/java/` (searched recursively) whose name ends in `.java`.

## Task

For each file, find the **first** line that begins with exactly `package ` (no
leading whitespace) and ends with `;`. The package name is the text between,
trimmed. Its **depth** is the number of `.`-separated segments — `org.raku.comma`
has depth 3.

Tally how many files fall at each depth.

## Output

One line per depth that occurs at least once, in ascending order of depth:

```
<depth><TAB><count>
```

Then always one final line, printed even when the count is zero:

```
none<TAB><count>
```

where `<count>` is the number of input files with no such package line.

Every line ends in a newline.
