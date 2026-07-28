# l11-xml-tag-census

## Input

Files sitting **directly** in `src/main/resources/META-INF/` (do not recurse) whose
name ends in `.xml`.

## Task

Count **element start tags**. A start tag is a `<` immediately followed by an ASCII
letter, then a maximal run of ASCII letters, digits, `-`, `_`, `.` and `:` — that run
is the tag name.

This deliberately excludes closing tags (`</foo>`), processing instructions
(`<?xml ...`) and declarations or comments (`<!-- ...`, `<!DOCTYPE`), because none of
those begin with `<` followed by a letter.

Do not parse the XML properly and do not try to exclude matches inside attribute
values or comments — a plain scan of the raw text is what is wanted here.

Count every occurrence, across all files together.

## Output

The 10 most frequent tag names, one per line:

```
<count><TAB><name>
```

sorted by count descending; ties broken by name ascending (ordinary codepoint order).

Then one final line:

```
DISTINCT<TAB><n>
```

where `<n>` is the total number of distinct tag names seen. Exactly 11 lines, each
ending in a newline.
