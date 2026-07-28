import os

edges = [0, 1_000, 4_000, 16_000]
count = {}
for root, _, files in os.walk('src/main/java'):
    for name in files:
        if name.endswith('.kt'):
            size = os.path.getsize(os.path.join(root, name))
            i = max(j for j, e in enumerate(edges) if e <= size)
            count[i] = count.get(i, 0) + 1
for i, e in enumerate(edges):
    label = f'{e}+' if i == len(edges) - 1 else f'{e}..{edges[i+1]}'
    print(f'{label}\t{count.get(i, 0)}')
