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

`colorSchemes/*.xml` retains exactly one override:

* `RAKU_ALT_WARNING` — its whole purpose is to be distinguishable from the
  ordinary weak warning it falls back to.

`RAKU_TEXT_BOLD` and `RAKU_TEXT_ITALIC` used to be here too. They are gone: an
entry in `colorSchemes/` is only seen by schemes derived from Default or
Darcula, so under any other theme Pod `B<>` fell through to `DOC_COMMENT` and
rendered in the doc-comment color and *not* bold — wrong twice over.
`RakuHighlighter.Style` carries the font style as the key's own default
attributes instead, which every scheme sees. Reach for that before reaching for
this file.

`RakuColorSettingsPageTest` pins the set. Adding a second needs the same kind of
justification.

### Why font style can live in the XML but an effect cannot

A scheme entry is all or nothing. `EditorColorsSchemeImpl.getAttributes`
returns any directly defined attributes whole and consults
`getFallbackAttributeKey` only when there are none, so a `<value>` block — even
a one-line `FONT_TYPE` — takes that key out of the fallback path. (The one
inheritance form the XML has, `baseAttributes` on a `<value>`-less option, is a
plain "use the fallback" marker and cannot be combined with anything.)

Font style survives that anyway, because composition happens a layer up.
`PodFormatterInspection` adds `B<>`/`I<>` as range highlighters over text the
lexer already colored, and `TextAttributes.merge` keeps the lower layer's
foreground when the upper one is null and ORs the two font types. So a
colorless `FONT_TYPE` renders as bold *in the active theme's Pod color*.

That is why the style can live on a key of its own rather than in this file at
all: `RakuHighlighter.Style.BOLD` and `.ITALIC` are keys whose *default*
attributes are nothing but a font style, so they layer the same way and apply
under every scheme. A fallback cannot express "this color, plus italic" — the
moment a key defines anything it leaves the fallback path — so layering a
second key is the only way to say it.

Effects do not survive it: `TextAttributesEffectsBuilder` drops an effect whose
color is null, so an underline needs a literal color, and a literal in
`colorSchemes/` is exactly the theme-specific hardcode this document argues
against. `RAKU_TEXT_UNDERLINE` and `RAKU_REGEX_SIG_SPACE` used to ship one each
(the underline was `202020` under Default and `d0d0d0` under Darcula, and no
third-party theme saw either). Both now go through
`RakuHighlighter.StyleEffect`, which resolves the key against the active
scheme and derives the effect color from the foreground the fallback already
gives it. A user who configures an effect of their own in the color panel still
wins.

**If you need an effect, add it there, not to `colorSchemes/`.** And note there
is deliberately no `Style.UNDERLINE` or `Style.BOLD_DOTTED_LINE`. The reason is
in how `TextAttributes.merge` treats the two kinds: `fontType` is an `int` and
merge **ORs** it, so layers combine; an effect is a color-and-type pair and
merge **picks**, so the layer with a non-null `effectColor` supplies both halves
and the other layer's effect is discarded whole. Measured, a type-only modifier
over a base that *already has* an effect color does not pair with it — the base
wins and the type is thrown away. So an effect cannot be encoded in a static
key at all.

That is the whole split — **`Style` is the static half, `StyleEffect` the
dynamic half**, and which one a thing needs is decided by whether it requires a
color to draw. `StyleEffect.BOLD_DOTTED_LINE.of(scheme, key)` is the
`Style.BOLD_DOTTED_LINE` you were reaching for; it is an enum value routing to a
call rather than a key, because a key's default attributes are fixed at
construction and so cannot reference a scheme they have not seen.

Adding an effect means adding a `StyleEffect` value, not threading another
`EffectType` through call sites. `of(scheme, key)` is the common case;
`addedTo(attributes, scheme)` layers one over a run you already composed, which
is what lets an effect stack on top of a `Style`.

A corollary worth remembering when picking a fallback: a platform key that
*already carries an effect* will defeat `StyleEffect`, because a non-null
effect color is the only evidence it has that a user configured one. That is
why Pod `U<>` does not fall back to `REASSIGNED_LOCAL_VARIABLE`, tempting as
the name is — the platform paints reassigned variables underlined, so the key
ships an `EFFECT_COLOR` of its own.

## Worked example: the Pod formatting codes

`PodFormatterInspection` is the reference for both helpers, because `B<>`,
`I<>` and `U<>` want the same thing — the colour of the surrounding prose,
plus a decoration — and the platform cannot express that on a key.

The decoration is declared beside the key, not at the point of application:

```kotlin
val POD_TEXT_BOLD      = key("RAKU_TEXT_BOLD",      DOC_COMMENT_MARKUP, POD, "Text (Bold)",       fontStyle = Font.BOLD)
val POD_TEXT_ITALIC    = key("RAKU_TEXT_ITALIC",    DOC_COMMENT_MARKUP, POD, "Text (Italic)",     fontStyle = Font.ITALIC)
val POD_TEXT_UNDERLINE = key("RAKU_TEXT_UNDERLINE", DOC_COMMENT_MARKUP, POD, "Text (Underlined)", effect = StyleEffect.LINE_UNDERSCORE)
```

so the caller only has to know *which key*, never how it is drawn:

```kotlin
val key = when (element.getFormatCode()) {
    "B" -> POD_TEXT_BOLD
    "I" -> POD_TEXT_ITALIC
    "U" -> POD_TEXT_UNDERLINE
    else -> return
}
customHighlight(editor, range, decoratedAttributes(scheme, key), SYNTAX)
```

One shape: **the key supplies the colour, its declaration supplies the
decoration, and `decoratedAttributes` puts them together.** This is the same
rule `entries` already enforces for the colour panel — an appearance that
lives anywhere but the declaration becomes a second hand-maintained list, and
those drift. An earlier version named `Font.BOLD` in the inspection's `when`,
which is exactly that.

All three keys fall back alongside `RAKU_TEXT`, so left alone a formatted run
reads as the text around it and differs only in how it is drawn — and all
three stay live controls, so anyone who wants bold Pod text in its own colour
has one.

Why a helper at all, rather than putting the decoration on the key: a
fallback resolves to another key's attributes *whole*, so the moment a key
defines a font style it stops inheriting the colour. There is no way to write
"this colour, plus bold".

Why `U<>` differs: `styledAttributes` returns the key's attributes with a
font style OR-ed in, which is enough for bold and italic. An effect needs a
*colour of its own*, which cannot be a constant, so it has to be computed
against a live scheme — that is `StyleEffect`. The two are the static and
dynamic halves of the same idea.

Three traps this example encodes, each of which cost a debugging session:

* **Do not point one of these at a key carrying its own effect.** `U<>` used
  `REASSIGNED_LOCAL_VARIABLE` once, which the platform draws underlined, so it
  ships an `EFFECT_COLOR`; `StyleEffect` reads that as "the user configured an
  effect" and stops deriving anything.
* **Keep them aligned with `RAKU_TEXT`.** When `B<>` and `I<>` briefly used a
  colourless `Style` overlay, `U<>` kept reading `DOC_COMMENT` and drew its
  underline in a different colour from the text above it.
* **A key with no foreground is normal.** `DOC_COMMENT_MARKUP` sets none in the
  default scheme, so the derivation falls through to the scheme's default
  foreground — which is also what the Pod text renders in, so they still match.

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
* Sigspace is marked with a dotted underline in the color its `FUNCTION_CALL`
  fallback resolves to (see "Why font style can live in the XML" above)
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
