#!/usr/bin/env python3
"""l8-kt-line-stats: line counts over the Kotlin sources under src/main/java."""

import sys
from decimal import Decimal, getcontext, ROUND_HALF_UP
from pathlib import Path

getcontext().prec = 50

ROOT = Path("src/main/java")

files = 0
lines = 0
nonblank = 0

for path in sorted(ROOT.rglob("*.kt")):
    if not path.is_file():
        continue
    text = path.read_text(encoding="utf-8")
    pieces = text.split("\n")
    if pieces and pieces[-1] == "":
        pieces.pop()
    files += 1
    lines += len(pieces)
    nonblank += sum(1 for piece in pieces if piece.strip() != "")

mean = (Decimal(nonblank) / Decimal(files)).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)

sys.stdout.write("files\t%d\n" % files)
sys.stdout.write("lines\t%d\n" % lines)
sys.stdout.write("nonblank\t%d\n" % nonblank)
sys.stdout.write("mean\t%s\n" % mean)
