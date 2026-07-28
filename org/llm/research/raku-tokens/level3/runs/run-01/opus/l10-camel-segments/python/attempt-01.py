import os
from collections import Counter


def segments(name):
    parts = []
    start = 0
    for i in range(1, len(name)):
        prev = name[i - 1]
        cur = name[i]
        nxt = name[i + 1] if i + 1 < len(name) else ""
        split = False
        if (prev.islower() or prev.isdigit()) and cur.isupper():
            split = True
        elif prev.isupper() and cur.isupper() and nxt.islower():
            split = True
        if split:
            parts.append(name[start:i])
            start = i
    parts.append(name[start:])
    return parts


counts = Counter()
for root, dirs, files in os.walk("src/main/java"):
    for f in files:
        if f.endswith(".java"):
            base = f[: -len(".java")]
            for seg in segments(base):
                counts[seg.lower()] += 1

ordered = sorted(counts.items(), key=lambda kv: (-kv[1], kv[0]))
for seg, n in ordered[:15]:
    print("%d\t%s" % (n, seg))
