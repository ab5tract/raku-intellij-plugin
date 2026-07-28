# journal — l11-xml-tag-census (raku)

- **attempt-01** — first and only version. Non-recursive `.IO.dir` over
  `src/main/resources/META-INF` filtered on `.ends-with(".xml")`, a global match
  of `'<' (<[A..Za..z]> <[A..Za..z0..9\-_.:]>*)` against each file's raw text,
  tally into a hash, then `sort({ -.value, .key })` and print the top 10 as
  `count<TAB>name` followed by `DISTINCT<TAB>elems`. Ran clean: exit 0, empty
  stderr, 11 lines each newline-terminated. Output judged correct — the tag
  frequencies match the shape of the plugin descriptors (localInspection,
  action, add-to-group dominating), the four-way tie at 20 is ordered
  `category, className, intentionAction, language` as codepoint order requires,
  and `xi:include` confirms `:` is accepted inside names while `pluginIcon.svg`
  and the `meta/` subdirectory were correctly excluded. No further attempts.
