#!/usr/bin/env python3
"""Classify every .java file under src/main/java/ by its first line."""

import os
import sys

ROOT = "src/main/java"

counts = {"package": 0, "comment": 0, "blank": 0, "other": 0}

for dirpath, dirnames, filenames in os.walk(ROOT):
    for name in sorted(filenames):
        if not name.endswith(".java"):
            continue
        path = os.path.join(dirpath, name)
        with open(path, "r", encoding="utf-8", errors="replace") as fh:
            first = fh.readline()
        line = first.rstrip("\n").rstrip("\r")

        if line.strip() == "":
            counts["blank"] += 1
        elif line.startswith("package "):
            counts["package"] += 1
        elif line.startswith("//") or line.startswith("/*"):
            counts["comment"] += 1
        else:
            counts["other"] += 1

for key in ("package", "comment", "blank", "other"):
    sys.stdout.write("%s\t%d\n" % (key, counts[key]))
