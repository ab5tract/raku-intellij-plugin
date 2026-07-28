import re
import os
from collections import Counter

TRACES_DIR = "org/llm/traces"

STOPWORDS = set("""
that this with from have what which when they then than been
were will into more only does also some such
""".split())

counts = Counter()

for name in os.listdir(TRACES_DIR):
    if not name.endswith(".md"):
        continue
    path = os.path.join(TRACES_DIR, name)
    if not os.path.isfile(path):
        continue
    with open(path, "r", encoding="utf-8") as f:
        text = f.read()
    text = text.lower()
    for word in re.findall(r"[a-z]+", text):
        if len(word) < 4:
            continue
        if word in STOPWORDS:
            continue
        counts[word] += 1

top12 = sorted(counts.items(), key=lambda kv: (-kv[1], kv[0]))[:12]

for word, count in top12:
    print(f"{count}\t{word}")
