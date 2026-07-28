import os
from collections import Counter

ROOT = "src/main/java"

counts = Counter()
total = 0

for dirpath, dirnames, filenames in os.walk(ROOT):
    for fname in filenames:
        if not fname.endswith(".java"):
            continue
        path = os.path.join(dirpath, fname)
        with open(path, "r", encoding="utf-8", errors="replace") as f:
            for line in f:
                stripped_eol = line.rstrip("\n").rstrip("\r")
                if stripped_eol.startswith("import ") and stripped_eol.endswith(";"):
                    name = stripped_eol[len("import "):-1].strip()
                    counts[name] += 1
                    total += 1

top10 = sorted(counts.items(), key=lambda kv: (-kv[1], kv[0]))[:10]

out_lines = []
for name, count in top10:
    out_lines.append(f"{count}\t{name}")
out_lines.append(f"TOTAL\t{total}")

print("\n".join(out_lines))
