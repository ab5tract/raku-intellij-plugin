import glob
import re

found = set()
for p in glob.glob('build/test-results/test/*.xml'):
    with open(p) as f:
        found.update(m.strip() for m in re.findall(r'\[SKIPPED\][^<&]+', f.read()))
for line in sorted(found):
    print(line)
