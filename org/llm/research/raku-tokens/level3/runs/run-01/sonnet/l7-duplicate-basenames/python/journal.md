attempt-01: os.walk over src/main/java, filter names ending in .java/.kt, strip
extension with os.path.splitext, tally basenames, keep those with count > 1,
sort by (-count, basename). Ran clean, output looked correct on inspection
(3 duplicated basenames, each shared by exactly 2 files, alphabetically
ordered within the tied count group). No issues found; only one attempt was
needed.
