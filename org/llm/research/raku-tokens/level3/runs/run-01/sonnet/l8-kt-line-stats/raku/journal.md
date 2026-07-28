attempt-01: Recursive dir walk under `src/main/java` collecting `.kt` files, split each
file's text on `\n` (dropping the trailing empty piece only when the text actually ends
in `\n`, per spec), count blank lines with `/^ \s* $/`, compute mean as an exact Rat
(`nonblank / files-count`) and round with `.round(0.01)` (round-half-up, which equals
round-half-away-from-zero since the mean is never negative), then print with `%.2f` to
force exactly 2 decimals. Ran clean on the first try: exit 0, empty stderr, output is
exactly 4 tab-separated lines each newline-terminated, no trailing blank line. No
attempt-02 was needed.
