#!/usr/bin/env python3
"""Tally import lines across src/main/java/."""

import os
import sys
from collections import Counter

counts = Counter()
total = 0

for dirpath, dirnames, filenames in os.walk("src/main/java"):
    for filename in filenames:
        if not filename.endswith(".java"):
            continue
        path = os.path.join(dirpath, filename)
        with open(path, encoding="utf-8") as handle:
            for line in handle:
                line = line.rstrip("\r\n")
                if line.startswith("import ") and line.endswith(";"):
                    name = line[len("import "):-1].strip()
                    counts[name] += 1
                    total += 1

top = sorted(counts.items(), key=lambda item: (-item[1], item[0]))[:10]
for name, count in top:
    sys.stdout.write("%d\t%s\n" % (count, name))
sys.stdout.write("TOTAL\t%d\n" % total)
