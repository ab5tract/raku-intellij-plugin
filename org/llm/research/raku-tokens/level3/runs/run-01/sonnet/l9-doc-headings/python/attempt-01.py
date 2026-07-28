import os
import re

DOCS_DIR = "docs"

heading_re = re.compile(r'^(#{1,6}) ')

def count_headings(path):
    counts = [0, 0, 0, 0, 0, 0]
    with open(path, "r", encoding="utf-8") as f:
        for line in f:
            m = heading_re.match(line)
            if m:
                level = len(m.group(1))
                counts[level - 1] += 1
    return counts

def main():
    entries = os.listdir(DOCS_DIR)
    files = []
    for name in entries:
        if not name.endswith(".md"):
            continue
        full = os.path.join(DOCS_DIR, name)
        if not os.path.isfile(full):
            continue
        files.append(full)

    files.sort()

    totals = [0, 0, 0, 0, 0, 0]
    for path in files:
        counts = count_headings(path)
        for i in range(6):
            totals[i] += counts[i]
        print(f"{path}\t{','.join(str(c) for c in counts)}")

    print(f"TOTAL\t{','.join(str(c) for c in totals)}")

if __name__ == "__main__":
    main()
