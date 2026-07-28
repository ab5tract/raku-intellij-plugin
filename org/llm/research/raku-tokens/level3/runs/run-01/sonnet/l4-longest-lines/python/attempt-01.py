#!/usr/bin/env python3
import os

ROOT = "src/test/kotlin"

results = []

for dirpath, dirnames, filenames in os.walk(ROOT):
    for name in filenames:
        if not name.endswith(".kt"):
            continue
        path = os.path.join(dirpath, name)
        with open(path, encoding="utf-8") as f:
            text = f.read()
        lines = text.split("\n")
        maxlen = max((len(line) for line in lines), default=0)
        rel_path = path.replace(os.sep, "/")
        results.append((maxlen, rel_path))

results.sort(key=lambda t: (-t[0], t[1]))

for maxlen, path in results[:5]:
    print(f"{maxlen}\t{path}")
