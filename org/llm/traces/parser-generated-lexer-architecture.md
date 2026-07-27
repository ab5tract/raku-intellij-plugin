# The generated lexer/parser: architecture, and how to change it safely

**Read this before touching anything under `src/main/java/org/raku/comma/parsing/`.**
The most important fact in this whole directory: `MAINBraid.java` is **generated
code**, and the generator's source is **not in this repo**. Edit it wrong and you
either corrupt a 54,000-line file by hand or silently lose your fix on the next
regeneration. This doc exists so the next agent doesn't learn that the hard way.

## What is generated, and from what

- **`MAINBraid.java`** (~54K lines) — the Raku lexer state machine. `MAINBraid
  extends Cursor<MAINBraid>`. Generated.
- **`Cursor.java`**, **`CursorStack.java`** — hand-written runtime support for the
  generated machine (backtracking, dynamic variables, the lookahead trampoline).
  These are real source you can edit normally.
- **`RakuLexer.java`** — the `LexerBase` adapter IntelliJ actually calls; drives
  `MAINBraid` via a trampoline (`advance()`, lines ~82-115). Real source.

The generator is **`p6-grammar-to-idea`**, a Raku program that transpiles a
restricted-Raku-grammar DSL into these Java files. Its input is a ~162 KB grammar
file `perl6.pm6`. As of this writing that tool lived at (an ephemeral, machine-local
checkout):

```
/tmp/intellij-ide-plugin/perl6-idea-plugin/tools/p6-grammar-to-idea/
    perl6.pm6                      # the grammar source of truth
    lib/P6GrammarToIdea/*.pm6      # GenerateLexer.pm6, GenerateParser.pm6, etc.
    README.md
```

That path is **not** guaranteed to exist. The upstream is the `perl6-idea-plugin`
project (the original Comma plugin this is forked from). If you need to regenerate,
locate that tooling first; do not assume `/tmp` still has it.

`git log -- MAINBraid.java` shows only cosmetic/rename commits historically — it has
never been regenerated in this repo. Treat it as vendored.

## The mirror-fix discipline (do not skip)

Because the grammar source is external and `MAINBraid.java` is what actually ships,
a fix has to live in **both** places:

1. **Patch `MAINBraid.java` directly** — surgically, minimal diff, with a comment
   explaining what and why (it's generated, so the comment is the only signal to a
   future reader that this line is intentional and not machine output).
2. **Mirror the same change into `perl6.pm6`** in the external tooling, so a future
   regeneration preserves it.

Both fixes recorded in `parser-reduce-metaop-mislexing.md` were applied this way.
If the external tooling is unavailable when you fix something, say so loudly in the
commit message so the grammar-side change can be reconciled later.

## Runtime model (enough to debug it)

The lexer is a backtracking PEG-ish machine, flattened into integer-state switches.

- Each grammar rule becomes a `_NNN_rulename()` method containing
  `while (true) { switch (this.state) { case 0: ...; case 1: ...; } }`. State is an
  `int`; each `case` advances `this.state` and either `continue`s, `return`s an
  outcome code, or calls a sub-rule.
- **Outcome codes** returned to the driving trampoline: `-1` = rule succeeded (pop
  frame), `-2` = rule failed (pop frame), `-3` = emitted one token (yield), any other
  value = "push this rule number as a new sub-frame".
- **`Cursor` / `CursorStack`**: the rule-call stack is a stack of `Cursor` frames.
  `RakuLexer.advance()` drives the top frame via `stack.peek().runRule()`.
- **Backtracking** (`Cursor.java`): `bsMark(state)` / `bsFailMark(state)` push marks
  onto a per-frame `backtrackStack` (4 ints per mark: state, pos, rep, flag);
  `backtrack()` restores `state`+`pos` to the nearest real mark (fail-marks, pos<0,
  are boundaries that just get popped). **`backtrack()` does NOT restore dynamic
  variables** — only state and pos. Remember that when reasoning about side effects.
- **Dynamic variables** (`$*IN_META`, `$*IN_REDUCE`, `$*PRECLIM`, `$*GOAL`, ...):
  stored in a per-frame `Map<String,Object>` (`Cursor.dynamicVariables`).
  `declareDynamicVariable` sets it on the current frame; `findDynamicVariable` walks
  the frame stack top-down for the first frame that has it; `assignDynamicVariable`
  walks down and mutates the frame that declared it. Dynamic scope = frame lifetime.
  Truthiness (`isValueTruthy`): empty string and integer 0 are **falsy**; any
  non-empty string and non-zero int are truthy. This matters — `$*IN_META` defaults
  to `""` (falsy) and is set to `"red"`/`"X"`/... (truthy) inside meta contexts.

## How to debug it (the methodology that worked)

Pure static reading of a 54K-line generated file is slow and error-prone. Instrument
instead. This is exactly how the reduce-metaop bug was cracked:

1. **Token-stream dump.** `RakuLexer` is a standalone `LexerBase` — you can drive it
   from a plain `junit.framework.TestCase` with no IDE fixture:
   ```kotlin
   val lexer = RakuLexer(); lexer.start(text, 0, text.length, 0)
   while (lexer.tokenType != null) { /* print tokenStart/End/type/text */; lexer.advance() }
   ```
   Compare the token stream of a working input vs a broken one side by side. A
   catastrophic mis-lex shows up as a giant `BAD_CHARACTER` token swallowing the rest
   of the input.

2. **Targeted `System.err` tracing inside a rule.** Add a `public static boolean
   RD_DEBUG` flag to `MAINBraid` and print `state`/`pos`/relevant dyn-vars at the top
   of the suspect `_NNN_rule()`. Toggle it from the test via `MAINBraid.RD_DEBUG =
   true` and capture `System.err`. This tells you which state the machine gets stuck
   in and whether a lookahead/args sub-rule fails. **Remove the instrumentation before
   committing** (the two fixes' final diffs are guard-only, no debug left behind).

3. **Canary-based swallow detection** (statement-boundary lens, PSI level): append a
   distinctive trailing statement (`999999;`) after the suspect construct and check
   whether it gets its own tiny `RakuStatement` node at the exact offset, or gets
   swallowed into a larger containing statement. Clean, binary signal for "does this
   construct eat what follows." Used to minimize both bugs to their smallest trigger.

4. **Confirm a fix doesn't regress adjacent forms.** Temporarily revert just the guard
   (a `/*TEMP-REVERT*/` one-liner) and re-run the minimization to prove the bug is
   pre-existing / that your change is the thing that fixes it and nothing else.

## Verifying parser changes

- The parser-sensitive suites that do **not** route through the broken
  `checkHighlighting` pipeline (see `test-harness-and-environment.md`) are the real
  signal: `org.raku.comma.parsing.*`, `org.raku.comma.cro.parsing.*`,
  `org.raku.comma.folding.*`, `org.raku.comma.formatter.*`.
- House-style parser regression tests: `class FooTest : RakuParsingTestCase("dir")`
  with `testData/parsing/dir/ParsingTestData.p6` (input) + `.txt` (golden PSI tree).
  `doTest(true)` generates the `.txt` on first run (the run "fails" writing it), then
  it passes. A clean golden tree has **no `PsiErrorElement` / `BAD_CHARACTER` nodes** —
  that absence is itself the regression assertion. See
  `testData/parsing/reduce-metaop-bug/` for a worked example.

## See also

- `parser-reduce-metaop-mislexing.md` — two real worked fixes to this machine.
- `test-harness-and-environment.md` — how to run tests here at all.
