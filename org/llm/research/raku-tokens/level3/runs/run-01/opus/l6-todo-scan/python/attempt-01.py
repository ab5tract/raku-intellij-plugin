#!/usr/bin/env python3
"""Count lines containing TODO or FIXME in the Java/Kotlin sources."""

import os
import sys

ROOT = "src/main/java"

results = []
total = 0

for dirpath, dirnames, filenames in os.walk(ROOT):
    dirnames.sort()
    for name in sorted(filenames):
        if not (name.endswith(".java") or name.endswith(".kt")):
            continue
        path = os.path.join(dirpath, name).replace(os.sep, "/")
        with open(path, encoding="utf-8", errors="replace") as handle:
            count = sum(
                1 for line in handle if "TODO" in line or "FIXME" in line
            )
        if count:
            results.append((count, path))
        total += count

results.sort(key=lambda pair: (-pair[0], pair[1]))

out = sys.stdout
for count, path in results:
    out.write("%d\t%s\n" % (count, path))
out.write("TOTAL\t%d\n" % total)
