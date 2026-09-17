# Tools

Vendored 2026-09-17 from the upstream project this plugin descends from:
<https://github.com/Raku/intellij-ide-plugin/tree/main/perl6-idea-plugin/tools>
(sparse checkout of `perl6-idea-plugin/tools/` at upstream `main`).

## p6-grammar-to-idea/ — THE grammar tool

Generates the lexer/parser machines (`MAINBraid.java`, `RakuParser.java`,
token/element types) from `perl6.pm6`, and the Cro-template equivalents from
`crotmp.pm6`. This is the grammar source of truth that
`org/llm/raku/traces/parser-generated-lexer-architecture.md` previously
described as living only in an ephemeral `/tmp` checkout — it now lives here.

**Mirror status:** our fork carries three hand-edits to the vendored
`MAINBraid.java`, and all three are mirrored into this `perl6.pm6` (marked
with `MIRROR` comments above the affected rules):

1. `token infix` — the `$*PREC`/`$*PRECLIM` guard only applies outside meta
   context (reduce-metaop fix 1, `parser-reduce-metaop-mislexing.md`).
2. `token infixish_non_assignment_meta` — the incomplete-bracketed-infix
   fallback must not fire in meta context (reduce-metaop fix 2).
3. `token term_name` — `nqp::const::` names are no-argument terms emitting
   `NO_ARGS` (`parser-nqp-const-term.md`).

The modified grammar parses cleanly in the tool's own DSL parser (verified:
`raku -Ilib -e '...P6GrammarToIdea::Parser.parse(slurp("perl6.pm6")...)'`).

**Known blocker for full regeneration:** the code generators depend on the
ecosystem module `Java::Generate`, whose released version (1.0.0) fails to
compile on current Rakudo (pre-modern syntax). Porting or pinning an old
Rakudo is required before `make raku` can run. The Makefile paths have been
adapted to this repo (`org.raku.comma.parsing`, prefix `Raku`).

**WARNING:** regeneration overwrites the vendored, hand-edited machines in
`src/main/java/org/raku/comma/parsing/` and
`src/main/java/org/raku/comma/cro/template/parsing/`. Before adopting any
regenerated output: (a) confirm the three mirrors above survived, (b) diff
against the vendored files for unexpected drift (upstream's grammar may not
match the exact generation our fork's machines came from), and (c) run the
full `org.raku.comma.parsing.*` golden suite.

## collect-nqp-ops.p6 + ops.markdown

Turns the nqp repo's ops documentation (`ops.markdown` is a cached copy)
into the JSON signature data used for `nqp::` op support.

## core-docs-generator/

Builds core-documentation JSON (the shape of
`src/main/resources/docs/core.json`) from a checkout of the Raku `doc` repo.
Depends on ecosystem modules `Documentable` and `Pod::To::Text`.

## symbol-script-module/

A small fixture Raku module exercising doc-comment/symbol-extraction shapes
(`#|`, `#=`, `is export`, `is implementation-detail`) for symbol-loading
tests.
