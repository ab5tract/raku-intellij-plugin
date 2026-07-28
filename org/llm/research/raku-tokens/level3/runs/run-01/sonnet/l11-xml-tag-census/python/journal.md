# Journal

- attempt-01: Regex `<([A-Za-z][A-Za-z0-9\-_.:]*)` scanning raw text of the
  `.xml` files listed directly (non-recursively, via `os.listdir` + `isfile`)
  in `src/main/resources/META-INF/`; tallied with `collections.Counter`,
  sorted by `(-count, name)`, top 10 printed as `<count>\t<name>`, followed by
  `DISTINCT\t<n>`. Ran clean on the first try: 11 lines out, empty stderr,
  real tab separators, ties within the count-20 group correctly ordered
  alphabetically (category, className, intentionAction, language). No
  changes needed.
