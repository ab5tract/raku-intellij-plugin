# l3-extension-impls

## Input

Files sitting **directly** in `src/main/resources/META-INF/` (do not recurse into
subdirectories) whose name ends in `.xml`.

## Task

Find every occurrence of the attribute `implementation="VALUE"` in those files.
`VALUE` is everything between the opening and the next `"`. Collect the distinct
values.

The **simple name** of a value is the text after its last `.`; if the value
contains no `.`, the simple name is the whole value.

## Output

First line:

```
<N> distinct
```

where `<N>` is the number of distinct values. Then one line per distinct value,
sorted ascending by the full value (ordinary codepoint order):

```
<simple><TAB><full>
```

Every line ends in a newline.
