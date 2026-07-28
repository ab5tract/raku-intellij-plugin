import os


def split_camel(name):
    n = len(name)
    splits = []
    for i in range(n - 1):
        c1 = name[i]
        c2 = name[i + 1]
        if (c1.islower() or c1.isdigit()) and c2.isupper():
            splits.append(i + 1)
        elif c1.isupper() and c2.isupper() and i + 2 < n and name[i + 2].islower():
            splits.append(i + 1)
    segments = []
    start = 0
    for sp in splits:
        segments.append(name[start:sp])
        start = sp
    segments.append(name[start:])
    return segments


def main():
    counts = {}
    for root, dirs, files in os.walk('src/main/java'):
        for f in files:
            if f.endswith('.java'):
                basename = f[:-len('.java')]
                for seg in split_camel(basename):
                    seg = seg.lower()
                    counts[seg] = counts.get(seg, 0) + 1

    items = sorted(counts.items(), key=lambda kv: (-kv[1], kv[0]))
    for seg, cnt in items[:15]:
        print(f"{cnt}\t{seg}")


if __name__ == '__main__':
    main()
