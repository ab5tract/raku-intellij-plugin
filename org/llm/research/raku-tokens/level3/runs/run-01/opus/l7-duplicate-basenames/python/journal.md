# journal — l7-duplicate-basenames (Python)

- attempt-01: First and only version. Recursive `os.walk` over `src/main/java`,
  filter on `.java`/`.kt`, tally `os.path.splitext(name)[0]`, keep counts > 1,
  sort by `(-count, basename)`, print header then `count<TAB>basename` lines.
  Ran clean (exit 0, empty stderr) and the output matched the spec's format and
  ordering, so no further attempts were needed.
