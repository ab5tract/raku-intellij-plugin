import os
import re
import sys

META_INF = "src/main/resources/META-INF"

ATTR = re.compile(r'\bimplementation="([^"]*)"')

values = set()
for name in os.listdir(META_INF):
    path = os.path.join(META_INF, name)
    if not name.endswith(".xml") or not os.path.isfile(path):
        continue
    with open(path, encoding="utf-8") as f:
        text = f.read()
    values.update(ATTR.findall(text))

out = sys.stdout
out.write("%d distinct\n" % len(values))
for value in sorted(values):
    simple = value.rsplit(".", 1)[-1]
    out.write("%s\t%s\n" % (simple, value))
