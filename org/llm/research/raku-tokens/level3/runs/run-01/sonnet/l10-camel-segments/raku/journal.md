attempt-01: Recursively walk `src/main/java/`, strip `.java` from each basename, mark
CamelCase boundaries with a zero-width regex (`<?after lower/digit><?before upper>` for
rule 1, `<?after upper><?before upper,lower>` for rule 2) by substituting in a `\x[1]`
sentinel, split on the sentinel, lowercase each segment, tally in a Hash, then sort by
count descending / segment ascending and print the top 15 tab-separated lines. Ran
clean on the first try: 843 `.java` files found, output has exactly 15 lines each ending
in a newline. Spot-checked plausibility against independent counts (`find -name
'*Impl.java'` = 190, matching the `impl` count exactly; `find -name 'Raku*.java'` = 555,
plus 2 more files with `raku` as a non-initial segment = 557, matching the `raku` count
exactly) — no corrections needed.
