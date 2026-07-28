#!/usr/bin/env python3
"""Census of XML element start tags in src/main/resources/META-INF/*.xml."""

import os
import re
import sys
from collections import Counter

DIR = "src/main/resources/META-INF"
TAG = re.compile(r"<([A-Za-z][A-Za-z0-9._:-]*)")

counts = Counter()

for name in sorted(os.listdir(DIR)):
    path = os.path.join(DIR, name)
    if not name.endswith(".xml") or not os.path.isfile(path):
        continue
    with open(path, encoding="utf-8") as f:
        text = f.read()
    counts.update(TAG.findall(text))

ranked = sorted(counts.items(), key=lambda kv: (-kv[1], kv[0]))

out = []
for tag, n in ranked[:10]:
    out.append("%d\t%s\n" % (n, tag))
out.append("DISTINCT\t%d\n" % len(counts))

sys.stdout.write("".join(out))
