# l7-duplicate-basenames

## Input

Every file under `src/main/java/` (searched recursively) whose name ends in
`.java` **or** `.kt`.

## Task

A file's **basename** is its filename with the final extension removed —
`RakuBlock.java` and `RakuBlock.kt` both have basename `RakuBlock`.

Find the basenames that belong to more than one input file.

## Output

First line:

```
<N> duplicated
```

where `<N>` is how many distinct basenames belong to more than one file. Then one
line per such basename:

```
<count><TAB><basename>
```

where `<count>` is how many input files share it. Sort by `<count>` descending;
ties broken by `<basename>` ascending (ordinary codepoint order).

Every line ends in a newline.
