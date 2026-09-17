# Raku grammar-ish to IDEA platform Lexer/Parser

This is a tool that takes something that looks very much like a Raku grammar
and produces Java code implementing the IDEA platform `Lexer` and `PsiParser`
interfaces.

This turns out to be a hard problem, since Raku grammars, used to specify
the Raku language:

* Rely on being able to nest sub-languages as deep as needed inside each
  other (`my $rx = /"$foo."bar"()"+/;` has MAIN, Regex, Quote, MAIN, Quote
  at its deepest point).
* Don't really distinguish lexing and parsing (it "writes a lexer for you",
  but some things are handled imperatively rather than by those lexers). By
  contrast, IntelliJ strongly decouples the lexing and parsing phases, and
  the current lexer state is only recorded as an integer.

Therefore, the integer used to represent the current state consists of two
16-bit values:

* The upper 16 bits represent a braid stack, and index to resolve it
* The lower 16 bits represent a parse state within that braid

## The grammar language

The grammar language is a restricted, and in some places slightly extended,
version of the Raku grammar syntax. The input file should consist of one or
more grammars, with one of them representing the main language being called
`MAIN`. Inside each `grammar`, `rule` and `token` are supported, with `rule`
doing the automatic insertion of `<.ws>` calls.

The following constructs inside those behave as in Raku grammars:

* Single line comments with `#`
* `.` for matching any character
* `||` for sequential alternation
* `|` for LTM alternation
* `[...]` grouping
* `<foo>` and `<.foo>`
* The `*`, `+`, and `?` quantifiers 
* `%` for separators between quantifiers
* The `$` and `$$` anchors
* Single quotes (`'like this'`) for literals
* `<?foo>`, `<!foo>`, `<?before ...>`, `<!before ...>`
* `<?>` and `<!>`
* Basic character classes, without ranges, common backslash sequences, but
  only a single element and no negation
* Escape character classes: `\d`, `\w`, `\h`, `\s`, `\r`, `\n`, `\v` and
  their uppercase negative forms
* Escapes of non-word characters (like `\\`)
* Proto tokens, together with the special rule `<sym>`
* `>>`, `«'`, `<<`, and `»` word boundaries
* `$<capture>=...` for named captures
* The `<foo=.bar>` name aliasing syntax
* `~` goal matching syntax
* Signatures containing scalar dynamic variables (`token bar($*FOO) { }`)
* Passing string and integer literal arguments (single quoted strings only)

The following rule names are built-in and special:

* `start-token` - expects to be called with a string argument, specifying the
  name of the token that starts here
* `end-token` - expects to be called with a string argument, specifying the
  name of the token that ends here

Todo:
* Parameter defaults
* Passing dynamics on as arguments
* Cheating {} as an LTM terminator
* Character class ranges
* Some MARKER equivalent
* Some way to report syntax errors, but then continue
* Builtin rules: ident, sym
* A way to call into another language
* The `<:foo>` Unicode property syntax
