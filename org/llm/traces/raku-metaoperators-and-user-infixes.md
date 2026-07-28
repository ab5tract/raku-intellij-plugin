# Metaoperators are derived, including for operators the user just invented

Short reference, empirically checked against `moar-2026.03`. Relevant to this repo
because the lexer has already mis-handled metaops once —
`parser-reduce-metaop-mislexing.md` — and a user-defined infix multiplies the surface
the lexer must cope with.

## The point

In most languages `+=` is a token in the grammar. In Raku it is *derived* from the
operator, so the metaoperator set is closed under user extension. Declare one plain
two-argument infix and you get the whole family for free, with no further declaration:

```raku
sub infix:<smoosh>($a, $b) { "$a|$b" }   # this is the entire declaration
```

| form | result | note |
|---|---|---|
| `my $x = "a"; $x smoosh= "b"` | `a\|b` | assignment metaop |
| `[smoosh] 1,2,3` | `1\|2\|3` | reduce — **no multi or list candidate needed**, it folds pairwise |
| `[\smoosh] 1,2,3` | `(1 1\|2 1\|2\|3)` | triangle reduce |
| `(1,2) Xsmoosh (3,4)` | `(1\|3 1\|4 2\|3 2\|4)` | cross |
| `(1,2) Zsmoosh (3,4)` | `(1\|3 2\|4)` | zip |
| `1 Rsmoosh 2` | `2\|1` | reversed |
| `<a b> <<smoosh>> <c d>` | `(a\|c b\|d)` | hyper |

They compose, and the composed forms take no space: `[Rsmoosh]` → `3|2|1`,
`XRsmoosh`, `ZRsmoosh` all work. **`[R smoosh]` with a space is a parse error**
("Two terms in a row").

`.=` extends the same derivation to method dispatch rather than treating methods as a
separate syntactic category:

```raku
my @f .= push: %( :this, :that );   # @f = @f.push(...)
@acc .= push(1) .= push(2);         # chains
```

## Two constraints that are not obvious

**Negation (`!op`) needs the operator to be "iffy", and `is iffy` is not a trait.**
The trait list Rakudo will accept is `rw raw default DEPRECATED inlinable onlystar
export leading_docs trailing_docs revision-gated implementation-detail
hidden-from-backtrace hidden-from-USAGE pure nodal equiv tighter looser assoc prec`.
Iffiness arrives through the precedence spec, so you inherit it:

```raku
sub infix:<samey>($a, $b) is equiv(&infix:<eq>) { $a eq $b }
say 1 !samey 2;   # True
```

Without it: `Cannot negate samey because additive operators are not iffy enough` — an
undeclared infix defaults to additive precedence.

**But inheriting `eq`'s precedence makes it chaining, which then blocks reduce.**
`[samey] 1,1,1` fails with `Cannot reduce with samey because chaining operators are
diffy and not chaining`. So `!op` and `[op]` pull in opposite directions on the same
declaration; you cannot have both by this route.

`Ssmoosh` did not work here (`Too few positionals passed`). Not investigated further —
recorded only so the next person does not assume it should.

## Why this matters for the plugin

Every one of the above is a lexable form built from an *arbitrary user-defined
operator name*. The lexer cannot enumerate metaop tokens, because the operator half is
open-ended; it has to recognise the metaop shell around a name it has never seen.
`parser-reduce-metaop-mislexing.md` records what happened when that went wrong for
`[and]` and `[[]]`: highlighting collapsed from mid-file to EOF. The transferable rule
recorded there — "a metaop wraps a *complete* base operator regardless of precedence"
— applies unchanged to user-defined bases.

## The design observation

This uniformity is the same property that gives every method an implicit `*%_`, which
is what `raku-named-args-corpus.md` exists to work around. Dispatch is general and
uniform, so metaoperators compose freely over anything — and so an unrecognised named
argument is a perfectly well-formed dispatch that happens to mean nothing. The
generality has no "reject this" mode, because having one would break the uniformity
that makes `smoosh=` fall out for free.

Measured, that bill is small (5–12%, `org/llm/report/raku-tokens/`). It just arrives
as silence rather than as an error.

Idioms in this file came from the user; the tables were verified before being written
down.
