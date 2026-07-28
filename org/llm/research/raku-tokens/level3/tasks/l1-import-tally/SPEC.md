# l1-import-tally

## Input

Every file under `src/main/java/` (searched recursively) whose name ends in `.java`.

## Task

A line is an **import line** if the line — with no whitespace stripped from the
front — begins with exactly `import ` and ends with `;`. The **name** is the text
between that leading `import ` and the final `;`, with surrounding whitespace
trimmed. (So `import static a.b.C;` yields the name `static a.b.C`. Keep it as-is.)

Count how many import lines carry each distinct name.

## Output

Exactly 11 lines, each ending in a newline.

The 10 most frequent names, one per line:

```
<count><TAB><name>
```

sorted by count descending; ties broken by name ascending (ordinary codepoint
order). Then one final line:

```
TOTAL<TAB><n>
```

where `<n>` is the total number of import lines found across all files.
