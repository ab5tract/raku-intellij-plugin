# l5-trace-words (Python) — attempt journal

- **attempt-01.py** — first and only attempt. Listed `org/llm/traces/` non-recursively,
  kept plain files ending in `.md`, lowercased each file's text, pulled maximal
  `a`–`z` runs of length 4+ with `re.findall(r"[a-z]{4,}", ...)` (greedy, so each
  maximal run is matched whole and never split), dropped the 22 stopwords, and
  printed the top 12 by `(-count, word)` as `count<TAB>word`. Ran clean: exit 0,
  empty stderr, 12 newline-terminated lines. Nothing to fix.
