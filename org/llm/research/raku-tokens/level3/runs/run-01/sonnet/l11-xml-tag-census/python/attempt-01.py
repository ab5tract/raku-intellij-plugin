#!/usr/bin/env python3
import os
import re
from collections import Counter

INPUT_DIR = "src/main/resources/META-INF"
TAG_RE = re.compile(r'<([A-Za-z][A-Za-z0-9\-_.:]*)')


def main():
    counts = Counter()

    entries = sorted(os.listdir(INPUT_DIR))
    for name in entries:
        path = os.path.join(INPUT_DIR, name)
        if not os.path.isfile(path):
            continue
        if not name.endswith(".xml"):
            continue
        with open(path, "r", encoding="utf-8") as f:
            text = f.read()
        for m in TAG_RE.finditer(text):
            counts[m.group(1)] += 1

    ranked = sorted(counts.items(), key=lambda kv: (-kv[1], kv[0]))

    out_lines = []
    for tag, count in ranked[:10]:
        out_lines.append(f"{count}\t{tag}")
    out_lines.append(f"DISTINCT\t{len(counts)}")

    print("\n".join(out_lines))


if __name__ == "__main__":
    main()
