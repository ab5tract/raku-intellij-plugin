#!/usr/bin/env python3
"""Tally Java files under src/main/java/ by the depth of their package declaration."""

import os
import sys

ROOT = "src/main/java"

counts = {}
missing = 0

for dirpath, dirnames, filenames in os.walk(ROOT):
    for name in filenames:
        if not name.endswith(".java"):
            continue
        path = os.path.join(dirpath, name)
        depth = None
        with open(path, encoding="utf-8") as fh:
            for line in fh:
                line = line.rstrip("\n").rstrip("\r")
                if line.startswith("package ") and line.endswith(";"):
                    package = line[len("package "):-1].strip()
                    depth = len(package.split("."))
                    break
        if depth is None:
            missing += 1
        else:
            counts[depth] = counts.get(depth, 0) + 1

out = sys.stdout
for depth in sorted(counts):
    out.write("%d\t%d\n" % (depth, counts[depth]))
out.write("none\t%d\n" % missing)
