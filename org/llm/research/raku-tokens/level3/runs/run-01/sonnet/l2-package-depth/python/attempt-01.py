import os

root = "src/main/java"

depth_counts = {}
none_count = 0

for dirpath, dirnames, filenames in os.walk(root):
    for filename in filenames:
        if not filename.endswith(".java"):
            continue
        path = os.path.join(dirpath, filename)
        with open(path, "r", encoding="utf-8", errors="replace") as f:
            found = None
            for line in f:
                line = line.rstrip("\n")
                if line.startswith("package ") and line.endswith(";"):
                    found = line
                    break
            if found is None:
                none_count += 1
            else:
                pkg = found[len("package "):-1].strip()
                depth = len(pkg.split("."))
                depth_counts[depth] = depth_counts.get(depth, 0) + 1

for depth in sorted(depth_counts.keys()):
    print(f"{depth}\t{depth_counts[depth]}")

print(f"none\t{none_count}")
