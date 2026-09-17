package org.raku.comma.parsing

// A regex assertion's colon-argument list may be followed by whitespace --
// including a newline -- before the closing '>' (Rakudo's own Grammar.nqp
// writes <.malformed: "..."\n> across lines). The arglist used to end without
// consuming that whitespace, the '>' never matched, and everything to EOF
// collapsed into BAD_CHARACTER. The golden tree having no error or
// BAD_CHARACTER nodes -- and the trailing 999999 canary statement parsing on
// its own -- is the assertion.
class RegexAssertionArgWsTest : RakuParsingTestCase("regex-assertion-arg-ws")
