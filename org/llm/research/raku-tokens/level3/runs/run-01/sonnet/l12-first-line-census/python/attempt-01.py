import os

counts = {"package": 0, "comment": 0, "blank": 0, "other": 0}

for root, dirs, files in os.walk("src/main/java"):
    for name in files:
        if not name.endswith(".java"):
            continue
        path = os.path.join(root, name)
        with open(path, "r", encoding="utf-8", errors="replace") as f:
            first_line = f.readline()

        if first_line == "":
            category = "blank"
        else:
            stripped = first_line.rstrip("\n").rstrip("\r")
            if stripped.strip() == "":
                category = "blank"
            elif stripped.startswith("package "):
                category = "package"
            elif stripped.startswith("//") or stripped.startswith("/*"):
                category = "comment"
            else:
                category = "other"

        counts[category] += 1

for key in ("package", "comment", "blank", "other"):
    print(f"{key}\t{counts[key]}")
