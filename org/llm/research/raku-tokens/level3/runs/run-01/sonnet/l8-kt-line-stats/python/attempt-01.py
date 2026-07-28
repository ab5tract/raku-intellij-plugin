#!/usr/bin/env python3
import os
from decimal import Decimal, ROUND_HALF_UP

def main():
    files = 0
    lines = 0
    nonblank = 0

    for root, dirs, filenames in os.walk("src/main/java"):
        for name in filenames:
            if name.endswith(".kt"):
                path = os.path.join(root, name)
                with open(path, "r", encoding="utf-8") as f:
                    text = f.read()
                files += 1
                parts = text.split("\n")
                if len(parts) > 0 and parts[-1] == "":
                    parts = parts[:-1]
                lines += len(parts)
                for line in parts:
                    if line.strip() != "":
                        nonblank += 1

    if files > 0:
        mean_val = Decimal(nonblank) / Decimal(files)
    else:
        mean_val = Decimal(0)
    mean_rounded = mean_val.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)

    print(f"files\t{files}")
    print(f"lines\t{lines}")
    print(f"nonblank\t{nonblank}")
    print(f"mean\t{mean_rounded}")

if __name__ == "__main__":
    main()
