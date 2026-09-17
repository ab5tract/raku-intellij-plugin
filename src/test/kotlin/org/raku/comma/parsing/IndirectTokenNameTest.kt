package org.raku.comma.parsing

// `token ::($meth_name) {...}` declares a token with an indirect name (used
// throughout Rakudo's NQP-dialect sources). The plain routine_name rule
// stopped at '::', so `($meth_name)` mis-parsed as the token's SIGNATURE --
// a shadowing parameter declaration instead of a usage, which cascaded into
// bogus unused-parameter inspections. The golden pins `::($meth_name)` as a
// single ROUTINE_NAME with no signature, while `token normal($sig)` keeps
// its real signature.
class IndirectTokenNameTest : RakuParsingTestCase("indirect-token-name")
