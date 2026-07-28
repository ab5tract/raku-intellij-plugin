import os
import re

INPUT_DIR = "src/main/resources/META-INF"

def main():
    values = set()
    for name in os.listdir(INPUT_DIR):
        if not name.endswith(".xml"):
            continue
        path = os.path.join(INPUT_DIR, name)
        if not os.path.isfile(path):
            continue
        with open(path, "r", encoding="utf-8") as f:
            text = f.read()
        for m in re.finditer(r'implementation="([^"]*)"', text):
            values.add(m.group(1))

    sorted_values = sorted(values)
    print(f"{len(sorted_values)} distinct")
    for v in sorted_values:
        last_dot = v.rfind(".")
        simple = v[last_dot + 1:] if last_dot != -1 else v
        print(f"{simple}\t{v}")

if __name__ == "__main__":
    main()
