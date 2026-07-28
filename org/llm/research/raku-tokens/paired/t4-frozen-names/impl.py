import re

with open('src/test/kotlin/org/raku/comma/highlighting/RakuColorSettingsPageTest.kt') as f:
    names = sorted(set(re.findall(r'"(RAKU_\w+)"', f.read())))
print(f'{len(names)} names')
for n in names:
    print(n)
