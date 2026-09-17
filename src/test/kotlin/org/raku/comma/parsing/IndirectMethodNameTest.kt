package org.raku.comma.parsing

// `method ::($meth)($/) {...}` declares a method with an indirect
// (runtime-computed) name -- Rakudo's grammar accepts the syntax (its
// compile-time rejection is semantic, and the NQP dialect of the Rakudo
// sources uses it heavily). The name rule used to stop at '::', mistake
// ($meth) for the signature, and collapse the block into BAD_CHARACTER.
// The golden tree having no error or BAD_CHARACTER nodes -- and the
// trailing 999999 canary parsing on its own -- is the assertion.
class IndirectMethodNameTest : RakuParsingTestCase("indirect-method-name")
