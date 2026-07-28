attempt-01: Implemented boundary-based CamelCase splitting (scan adjacent character pairs, insert a
split point per rule 1 or rule 2, then cut the string at those points), lowercase each segment, tally
in a dict, sort by (-count, segment) and print the top 15 as `<count>\t<segment>`. Ran clean on the
first try: exit 0, empty stderr, 15 lines of output, trailing newline present. Manually verified the
split logic against the three worked examples in the spec (RakuPSIElement -> Raku/PSI/Element,
HTTPServer -> HTTP/Server, Base64Decoder -> Base64/Decoder) before running, and spot-checked the
output afterward (count of files matching *Cro* roughly matches the reported count for the "cro"
segment). No issues found; only one attempt was needed.
