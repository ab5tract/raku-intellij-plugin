import re

for scheme in ('RakuDefault', 'RakuDarcula'):
    with open(f'src/main/resources/colorSchemes/{scheme}.xml') as f:
        xml = f.read()
    keys = [name for name, body in
            re.findall(r'<option name="(RAKU_\w+)">(.*?)</option>', xml, re.S)
            if 'EFFECT_COLOR' in body]
    print(f'{scheme}: {", ".join(sorted(keys)) or "(none)"}')
