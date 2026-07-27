# Raku Color Scheme Principles

## Where colors come from

Raku ships almost no colors of its own. Every attribute key in
`RakuHighlighter.kt` declares a *fallback* — a platform key such as
`OPERATION_SIGN`, `LOCAL_VARIABLE` or `CLASS_NAME` — and its appearance is
whatever the user's active scheme gives that fallback. Retheming Raku is
therefore a matter of choosing a scheme, and Raku looks consistent under
third-party themes rather than only under Default and Darcula.

This used not to be true. `colorSchemes/RakuDefault.xml` and `RakuDarcula.xml`
hardcoded ~37 foregrounds, so those two schemes showed the colors below while
every other theme quietly showed the fallbacks instead. The two files had also
drifted apart from each other (37 keys vs 39).

The groups below still describe the *intent* — which things should read alike,
and which should read differently. That intent is now expressed by **choosing
the fallback**, not by setting a color. If two groups look the same under some
scheme, the fix is a better fallback for one of them.

### The exceptions

`colorSchemes/*.xml` retains exactly five overrides, each encoding something no
platform key expresses:

* `RAKU_TEXT_BOLD`, `RAKU_TEXT_ITALIC`, `RAKU_TEXT_UNDERLINE` — Pod `B<>`,
  `I<>` and `U<>` have to render bold/italic/underlined to mean anything.
* `RAKU_REGEX_SIG_SPACE` — sigspace is a blank run, so without an effect there
  is nothing to see.
* `RAKU_ALT_WARNING` — its whole purpose is to be distinguishable from the
  ordinary weak warning it falls back to.

`RakuColorSettingsPageTest` pins that set. Adding a sixth needs the same kind of
justification.

## The basic idea

* Brackets, braces, parentheses, etc. are neutrel color
* All kinds of keyword, even if they can be customized, default to the same
  color, which is unused for anything else
* Variables have a distinct color
* Names of things that are callable, both usage and declaration wise, have a
  distinct color
* Types and terms have a distinct color
* Operators have a distinct color, including regex things that feel quite
  operator-like
* Comments have a distinct color
* Literals have a distinct color
* Literal escapes have a distinct color
* Numeric literals have a distinct color
* Sigspace is marked with an underline effect (see "The exceptions" above)
* Bad escapes inherit INVALID_STRING_ESCAPE

This gives us these colors for the elements:

* Neutrel (inherits the platform bracket/brace/comma keys)
    * Argument capture
    * Array composer
    * Array indexer
    * Block clurly brackets
    * Hash indexer
    * Lambda
    * Named parameter colon and parentheses
    * Only Star (protos)
    * Parameter separator
    * Parentheses
    * Regex assertion angle brackets
    * Regex character class syntax
    * Regex Group
    * Return type arrow
    * Statement terminaotr
    * Term declaration backslash
    * Type coercion parentheses
* Keyword
    * Multi keyword
    * Package keyword
    * Parameter or variable constraint
    * Phaser
    * Routine keyword
    * Scope keyword
    * Statement control
    * Statement modifier
    * Statement prefix
    * Trait keyword
    * Type declarator
* Variable (via fallback)
    * Current object
    * Named parameter name alias
    * Regex capture
    * Variable
    * Variable shape declaration
* Callable (via fallback)
    * Method call name
    * Routine name
    * Sub call name
* Types and terms (via fallback)
    * Other term
    * Type name
    * Type parameter brackets
    * Whatever
* Operator (via fallback)
    * Contextualizer
    * Infix operator
    * Metaoperator
    * Parameter quantifier
    * Postix operator
    * Prefix operator
    * Regex anchor
    * Regex modifier
    * Regex infix
    * Regex lookaround
    * Regex quantifier
    * Transliteration range syntax
* Comment
    * Comment
    * Stub code
* String Literal
    * Pair (colon pair or key before =>)
    * Quote modifer
    * Quote pair
    * Regex literal quote
    * String literal quote
    * String literal value
    * Transliteration literal character
* Numeric literal
    * Numeric literal
    * Version literal
* String literal escape
    * Regex built-in character class
    * Regex invalid backslash (plus background)
    * String literal escape
    * String literal invalid escape (plus background)
    * Transliteration escape
    * Transliteration invalid syntax (plus background)
