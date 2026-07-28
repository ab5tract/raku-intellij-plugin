#!/usr/bin/env python3
import os

root = "src/main/java"

counts = {}

for dirpath, dirnames, filenames in os.walk(root):
    for name in filenames:
        if name.endswith(".java") or name.endswith(".kt"):
            basename, ext = os.path.splitext(name)
            counts[basename] = counts.get(basename, 0) + 1

dupes = [(count, basename) for basename, count in counts.items() if count > 1]
dupes.sort(key=lambda x: (-x[0], x[1]))

print(f"{len(dupes)} duplicated")
for count, basename in dupes:
    print(f"{count}\t{basename}")
