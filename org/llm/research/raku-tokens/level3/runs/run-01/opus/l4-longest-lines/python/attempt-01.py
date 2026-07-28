#!/usr/bin/env python3
"""Report the five src/test/kotlin/**/*.kt files with the longest lines."""

import os
import re
import sys

ROOT = "src/test/kotlin"
LINE_BREAK = re.compile(r"\r\n|\r|\n")

results = []

for dirpath, dirnames, filenames in os.walk(ROOT):
    dirnames.sort()
    for name in sorted(filenames):
        if not name.endswith(".kt"):
            continue
        path = os.path.join(dirpath, name).replace(os.sep, "/")
        with open(path, "rb") as fh:
            text = fh.read().decode("utf-8")
        maxlen = max(len(line) for line in LINE_BREAK.split(text))
        results.append((maxlen, path))

results.sort(key=lambda item: (-item[0], item[1]))

for maxlen, path in results[:5]:
    sys.stdout.write("%d\t%s\n" % (maxlen, path))
