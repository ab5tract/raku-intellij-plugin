# l5-trace-words

## Input

Files sitting **directly** in `org/llm/traces/` (do not recurse) whose name ends
in `.md`.

## Task

Lowercase the entire text of every input file. Then extract every **maximal run of
the ASCII letters `a`–`z`** of length 4 or greater. Nothing else counts as a word
character — digits, underscores, hyphens and punctuation all break a run.

Discard any word appearing in this stopword list:

```
that this with from have what which when they then than been
were will into more only does also some such
```

Count the remaining words.

## Output

Exactly 12 lines, each ending in a newline — the 12 most frequent words:

```
<count><TAB><word>
```

sorted by count descending; ties broken by word ascending (ordinary codepoint
order).
