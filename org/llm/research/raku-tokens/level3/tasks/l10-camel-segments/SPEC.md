# l10-camel-segments

## Input

Every file under `src/main/java/` (searched recursively) whose name ends in `.java`.

## Task

Take each file's **basename** — the filename with its final `.java` removed. Split
that basename into CamelCase segments using exactly these two rules, applied to the
original (un-lowercased) string:

1. Split between a lowercase letter or a digit and an immediately following
   **uppercase** letter.
2. Split between an uppercase letter and an immediately following uppercase letter
   that is itself followed by a **lowercase** letter.

So `RakuPSIElement` splits into `Raku`, `PSI`, `Element`; `HTTPServer` into `HTTP`,
`Server`; `Base64Decoder` into `Base64`, `Decoder`.

Lowercase every resulting segment, then count how often each occurs. One basename
contributes each of its segments once per occurrence, and every file is processed
even if two files share a basename.

## Output

Exactly 15 lines, each ending in a newline — the 15 most frequent segments:

```
<count><TAB><segment>
```

sorted by count descending; ties broken by segment ascending (ordinary codepoint
order).
