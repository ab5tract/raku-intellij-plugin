import os
from collections import Counter

ROOT = "src/main/java"

counts = Counter()
for dirpath, dirnames, filenames in os.walk(ROOT):
    for name in filenames:
        if name.endswith(".java") or name.endswith(".kt"):
            counts[os.path.splitext(name)[0]] += 1

dupes = [(basename, n) for basename, n in counts.items() if n > 1]
dupes.sort(key=lambda pair: (-pair[1], pair[0]))

print("%d duplicated" % len(dupes))
for basename, n in dupes:
    print("%d\t%s" % (n, basename))
