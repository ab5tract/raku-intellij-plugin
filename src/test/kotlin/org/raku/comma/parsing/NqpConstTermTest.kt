package org.raku.comma.parsing

// nqp::const::FOO is a no-argument term (Rakudo's term:sym<nqp::const>), so an
// infix `&&` or `&` after it must lex as an operator. It used to be treated as
// a listop whose argument list swallowed the bare `&` as a term, turning the
// rest of the statement into BAD_CHARACTER. The golden tree having no error or
// BAD_CHARACTER nodes -- and the trailing 999999 canary statement parsing on
// its own -- is the assertion.
class NqpConstTermTest : RakuParsingTestCase("nqp-const-term")
