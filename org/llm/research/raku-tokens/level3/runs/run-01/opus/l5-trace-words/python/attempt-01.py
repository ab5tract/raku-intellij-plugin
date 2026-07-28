#!/usr/bin/env python3
"""Count the most frequent words across the trace notes in org/llm/traces/."""

import os
import re
from collections import Counter

TRACES = "org/llm/traces"

STOPWORDS = set("""
that this with from have what which when they then than been
were will into more only does also some such
""".split())

counts = Counter()

for name in sorted(os.listdir(TRACES)):
    path = os.path.join(TRACES, name)
    if not name.endswith(".md") or not os.path.isfile(path):
        continue
    with open(path, encoding="utf-8") as f:
        text = f.read().lower()
    for word in re.findall(r"[a-z]{4,}", text):
        if word not in STOPWORDS:
            counts[word] += 1

top = sorted(counts.items(), key=lambda kv: (-kv[1], kv[0]))[:12]
for word, count in top:
    print(f"{count}\t{word}")
