import os

ROOT = "src/main/java"

results = []
total = 0

for dirpath, dirnames, filenames in os.walk(ROOT):
    for name in filenames:
        if name.endswith(".java") or name.endswith(".kt"):
            path = os.path.join(dirpath, name).replace(os.sep, "/")
            count = 0
            with open(path, "r", encoding="utf-8", errors="replace") as f:
                for line in f:
                    if "TODO" in line or "FIXME" in line:
                        count += 1
            if count >= 1:
                results.append((count, path))
                total += count

results.sort(key=lambda x: (-x[0], x[1]))

for count, path in results:
    print(f"{count}\t{path}")

print(f"TOTAL\t{total}")
