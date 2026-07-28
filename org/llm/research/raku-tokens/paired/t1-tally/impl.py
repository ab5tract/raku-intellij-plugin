import glob
import re

total = 0
for p in glob.glob('build/test-results/test/*.xml'):
    with open(p) as f:
        m = re.search(r'tests="(\d+)"', f.read(600))
    if m:
        total += int(m.group(1))
print(f'{total} tests')
