#!/usr/bin/env python3
"""Count markdown headings by level in the files directly under docs/."""

import os
import re

HEADING = re.compile(r"^(#{1,6}) ")

totals = [0] * 6
lines = []

for name in sorted(os.listdir("docs")):
    path = os.path.join("docs", name)
    if not name.endswith(".md") or not os.path.isfile(path):
        continue

    counts = [0] * 6
    with open(path, encoding="utf-8") as handle:
        for line in handle:
            match = HEADING.match(line)
            if match:
                counts[len(match.group(1)) - 1] += 1

    for i, n in enumerate(counts):
        totals[i] += n
    lines.append((path, counts))

lines.sort(key=lambda item: item[0])

for path, counts in lines:
    print(path + "\t" + ",".join(str(n) for n in counts))
print("TOTAL\t" + ",".join(str(n) for n in totals))
