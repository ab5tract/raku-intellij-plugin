# Raku named arguments: what is real, what is silently ignored

**Generated — do not edit by hand.** Rebuild with the pipeline in
`org/llm/raku/traces/raku-named-args-corpus.md`.

## The one thing to know

Every Raku method carries an **implicit `*%_`**, added by the compiler unless the
signature already has a named slurpy or a `|capture`. So an unrecognised named
argument is *accepted, ignored, and never reported*:

```raku
'src'.IO.dir(:recursive)   # no error. no recursion. returns one level.
'src'.IO.dir(:R)           # no error. same.
(1..5).pick(3, :seed(42))  # no error. seed ignored, still random.
```

Measured cost of exactly this: **4 of 4 first-attempt failures** in a blind 24-arm
Raku trial, against 0 for Python, three of them exiting 0 while printing plausible
zeros (`org/llm/raku/report/raku-tokens/`).

**A declared-nameds list is a whitelist of understood adverbs, never an accept/reject
boundary.** Nothing here can tell you an adverb is *invalid* — only that this method
does not appear to understand it.

Check one before you trust it:

```bash
raku scripts/named-args.raku IO::Path dir recursive   # exit status = names not declared
```

## Adverbs that are accepted but do nothing

These were **empirically probed against the running Rakudo** and demonstrably had no
effect. They are the nastiest class: documented, correct-looking, and inert. Most are
*compilation* adverbs — they work written syntactically (`s:i/a/b/`) but not passed as
runtime named arguments.

| call | adverb | why |
|---|---|---|
| `Str.match` | `:P5` | probe returned False |
| `Str.match` | `:Perl5` | probe returned False |
| `Str.match` | `:i` | probe returned False |
| `Str.match` | `:ignorecase` | probe returned False |
| `Str.match` | `:ignoremark` | probe returned False |
| `Str.match` | `:m` | probe returned False |
| `Str.match` | `:r` | probe returned False |
| `Str.match` | `:ratchet` | probe returned False |
| `Str.match` | `:s` | probe returned False |
| `Str.match` | `:sigspace` | probe returned False |
| `Str.subst` | `:P5` | probe returned False |
| `Str.subst` | `:Perl5` | probe returned False |
| `Str.subst` | `:i` | probe returned False |
| `Str.subst` | `:ignorecase` | probe returned False |
| `Str.subst` | `:ignoremark` | probe returned False |
| `Str.subst` | `:m` | probe returned False |
| `Str.subst` | `:r` | probe returned False |
| `Str.subst` | `:ratchet` | probe returned False |
| `Str.subst-mutate` | `:i` | probe returned False |

## Methods that DO complain

Here the silent-drop hazard does not apply — pass a bad adverb and you get an
exception or a warning, not a wrong answer. Worth knowing, because it tells you where
you can rely on the language to catch you.

| call | behaviour | raises | adverbs it accepts |
|---|---|---|---|
| `Any.!first-result` | failure | `X::Adverb` | `:k` `:p` `:v` `:kv` |
| `Any.ZEN-KEY` | failure | `X::Adverb` | — |
| `Any.first` | failure | `X::Adverb` | `:k` `:p` `:v` `:kv` `:end` |
| `Any.grep` | throw | `X::Adverb` | `:k` `:v` `:kv` `:p` |
| `Array::Element.access` | failure | `X::Adverb` | `:exists` `:delete` `:kv` `:p` `:k` `:v` |
| `Array::Element.access-any` | failure | `X::Adverb` | `:exists` `:delete` `:kv` `:p` `:k` `:v` |
| `CurrentThreadScheduler.cue` | throw | `die` | `:at` `:in` `:times` `:catch` |
| `DateTime.new` | throw | `die` | `:timezone` `:formatter` |
| `Dateish.earlier` | throw | `die` | — |
| `Dateish.later` | throw | `die` | — |
| `Dateish.new` | failure | `X::AdHoc` | — |
| `GLOBAL.SLICE_MORE_HASH` | failure | `X::Adverb` | `:delete` `:exists` `:kv` `:p` `:k` `:v` |
| `GLOBAL.SLICE_ONE_HASH` | failure | `X::Adverb` | `:delete` `:exists` `:kv` `:p` `:k` `:v` |
| `IO::CatHandle.TWEAK` | throw | `X::IO::BinaryAndEncoding` | — |
| `IO::Handle.TWEAK` | throw | `X::IO::BinaryAndEncoding` | — |
| `IO::Handle.open` | throw | `X::IO::BinaryAndEncoding` | — |
| `IO::Pipe.TWEAK` | throw | `X::IO::BinaryAndEncoding` | — |
| `IO::Socket::INET.new` | failure | `fail` | — |
| `JavaScriptScheduler.cue` | throw | `die` | `:times` `:catch` |
| `Mu.clone` | throw | `die` | — |
| `Rakudo::Internals.ADVERBS_AND_NAMED_TO_DISPATCH_INDEX` | failure | `X::Adverb` | `:exists` `:delete` `:kv` `:p` `:k` `:v` |
| `Rakudo::Internals.ADVERBS_TO_DISPATCH_INDEX` | failure | `X::Adverb` | `:exists` `:delete` `:kv` `:p` `:k` `:v` |
| `Rakudo::Internals.FAIL_X_ADVERB` | failure | `X::Adverb` | — |
| `Rakudo::Internals.SLICE_HUH` | failure | `X::Adverb` | `:delete` `:exists` `:kv` `:p` `:k` `:v` |
| `Rakudo::Internals.SLICE_POSITIONS_WITH_ADVERBS` | failure | `X::Adverb` | `:exists` `:delete` `:kv` `:p` `:k` `:v` |
| `Rakudo::Internals.SLICE_WITH_ADVERBS` | failure | `X::Adverb` | `:exists` `:delete` `:kv` `:p` `:k` `:v` |
| `Str.!SUBST` | throw | `X::Str::Subst::Adverb` | — |
| `Str.!ensure-split-sanity` | throw | `X::Adverb` | `:v` `:k` `:kv` `:p` |
| `Str.!match-nth` | throw | `X::AdHoc` | — |
| `Str.!match-nth-iterator` | throw | `X::AdHoc` | — |
| `Str.!match-x` | failure | `X::Str::Match::x` | — |
| `Str.trans` | warn | `CX::Warn` | `:c` `:complement` `:d` `:delete` `:s` `:squash` |
| `ThreadPoolScheduler.cue` | throw | `die` | `:every` `:times` `:stop` `:at` `:in` `:catch` |
| `Variable.trait_mod:<is>` | throw | `X::Comp::Trait::Unknown` | `:TypeObject` `:default` `:dynamic` `:export` |
| `Variable.trait_mod:<will>` | throw | `X::Comp::Trait::Unknown` | `:begin` `:check` `:final` `:init` `:end` `:enter` `:leave` `:keep` `:undo` `:first` `:next` `:last` `:pre` `:post` `:compose` |
| `sub.postcircumfix:<[ ]>` | throw | `X::Bind::Slice` | `:UNKNOWN` |
| `sub.postcircumfix:<[; ]>` | throw | `X::Bind::Slice` | `:UNKNOWN` |
| `sub.postcircumfix:<{ }>` | failure | `X::Adverb` | `:delete` `:exists` `:kv` `:p` `:k` `:v` |
| `sub.postcircumfix:<{; }>` | failure | `X::Adverb` | `:exists` `:delete` `:k` `:kv` `:p` `:v` |
| `sub.trait_mod:<is>` | throw | `X::Inheritance::UnknownParent` | `:DEPRECATED` `:rw` `:nativesize` `:ctype` `:unsigned` `:hidden` `:array_type` `:implementation-detail` `:export` `:SYMBOL` `:leading_docs` `:trailing_docs` |
| `sub.trait_mod:<will>` | throw | `X::Comp::Trait::Unknown` | — |

## Regex-construct adverbs, from Rakudo's own tables

Harvested from `src/Perl6/Actions.nqp` and cross-checked against the RakuAST
implementation in `src/Raku/ast/code.rakumod` — both frontends agree.

`compilation-only` means: valid written into the construct (`m:i/…/`), inert as a
runtime named argument.

### `m//`

| adverb | canonical | kind |
|---|---|---|
| `:P5` | `P5` | compilation-only |
| `:Perl5` | `P5` | compilation-only |
| `:c` | `c` | runtime |
| `:continue` | `c` | runtime |
| `:ex` | `ex` | repeatable |
| `:exhaustive` | `ex` | repeatable |
| `:g` | `g` | repeatable |
| `:global` | `g` | repeatable |
| `:i` | `i` | compilation-only |
| `:ignorecase` | `i` | compilation-only |
| `:ignoremark` | `m` | compilation-only |
| `:m` | `m` | compilation-only |
| `:nd` | `nth` | runtime |
| `:nth` | `nth` | runtime |
| `:ov` | `ov` | repeatable |
| `:overlap` | `ov` | repeatable |
| `:p` | `p` | runtime |
| `:pos` | `p` | runtime |
| `:r` | `r` | compilation-only |
| `:ratchet` | `r` | compilation-only |
| `:rd` | `nth` | runtime |
| `:s` | `s` | compilation-only |
| `:sigspace` | `s` | compilation-only |
| `:st` | `nth` | runtime |
| `:th` | `nth` | runtime |
| `:x` | `x` | repeatable |

### `s///`

| adverb | canonical | kind |
|---|---|---|
| `:P5` | `P5` | compilation-only |
| `:Perl5` | `P5` | compilation-only |
| `:c` | `c` | runtime |
| `:continue` | `c` | runtime |
| `:g` | `g` | repeatable |
| `:global` | `g` | repeatable |
| `:i` | `i` | compilation-only |
| `:ignorecase` | `i` | compilation-only |
| `:ignoremark` | `m` | compilation-only |
| `:ii` | `ii` (implies `:i`) | runtime |
| `:m` | `m` | compilation-only |
| `:mm` | `mm` (implies `:m`) | runtime |
| `:nd` | `nth` | runtime |
| `:nth` | `nth` | runtime |
| `:p` | `p` | runtime |
| `:pos` | `p` | runtime |
| `:r` | `r` | compilation-only |
| `:ratchet` | `r` | compilation-only |
| `:rd` | `nth` | runtime |
| `:s` | `s` | compilation-only |
| `:samecase` | `ii` (implies `:i`) | runtime |
| `:samemark` | `mm` (implies `:m`) | runtime |
| `:samespace` | `ss` (implies `:s`) | runtime |
| `:sigspace` | `s` | compilation-only |
| `:ss` | `ss` (implies `:s`) | runtime |
| `:st` | `nth` | runtime |
| `:th` | `nth` | runtime |
| `:x` | `x` | repeatable |

### `tr///`

| adverb | canonical | kind |
|---|---|---|
| `:P5` | `P5` | compilation-only |
| `:Perl5` | `P5` | compilation-only |
| `:c` | `c` | runtime |
| `:complement` | `c` | runtime |
| `:d` | `d` | runtime |
| `:delete` | `d` | runtime |
| `:i` | `i` | compilation-only |
| `:ignorecase` | `i` | compilation-only |
| `:ignoremark` | `m` | compilation-only |
| `:m` | `m` | compilation-only |
| `:r` | `r` | compilation-only |
| `:ratchet` | `r` | compilation-only |
| `:s` | `s` | compilation-only |
| `:sigspace` | `s` | compilation-only |
| `:squash` | `s` | compilation-only |

### `rx//`

| adverb | canonical | kind |
|---|---|---|
| `:P5` | `P5` | compilation-only |
| `:Perl5` | `P5` | compilation-only |
| `:i` | `i` | compilation-only |
| `:ignorecase` | `i` | compilation-only |
| `:ignoremark` | `m` | compilation-only |
| `:m` | `m` | compilation-only |
| `:r` | `r` | compilation-only |
| `:ratchet` | `r` | compilation-only |
| `:s` | `s` | compilation-only |
| `:sigspace` | `s` | compilation-only |

### `slice`

| adverb | canonical | kind |
|---|---|---|
| `:delete` | `d` | slice |
| `:exists` | `exists` | slice |
| `:k` | `k` | slice |
| `:kv` | `kv` | slice |
| `:p` | `p` | slice |
| `:v` | `v` | slice |

## Where named arguments actually go

A method that declares nothing may still accept adverbs, by forwarding them on. This
is why "not declared" does not mean "invalid" — `Str.subst` declares no named
parameters at all, yet `:g` works, because it hands `%options` to `Str.match`.

| method | forwards via | to |
|---|---|---|
| `Allomorph.chop` | `|c` | `Str.chop` |
| `Allomorph.comb` | `|c` | `Str.comb` |
| `Allomorph.samecase` | `|c` | `Str.samecase` |
| `Allomorph.samemark` | `|c` | `Str.samemark` |
| `Allomorph.samespace` | `|c` | `Str.samespace` |
| `Allomorph.split` | `|c` | `Str.split` |
| `Allomorph.subst` | `|c` | `Str.subst` |
| `Allomorph.subst-mutate` | `|c` | `Str.subst-mutate` |
| `Allomorph.substr` | `|c` | `Str.substr` |
| `Allomorph.words` | `|c` | `Str.words` |
| `Any.!first-accepts` | `positional-%_` | `Any.!first-result` |
| `Any.!first-accepts-end` | `positional-%_` | `Any.!first-result` |
| `Any.!first-callable` | `positional-%_` | `Any.!first-result` |
| `Any.!first-callable-end` | `positional-%_` | `Any.!first-result` |
| `Any.!first-regex` | `positional-%_` | `Any.!first-result` |
| `Any.!first-regex-end` | `positional-%_` | `Any.!first-result` |
| `Any.append` | `|c` | `Array.append` |
| `Any.combinations` | `|c` | `List.combinations` |
| `Any.first` | `positional-%_` | `Any.!first-regex-end` |
| `Any.head` | `|c` | `List.head` |
| `Any.match` | `|c` | `Str.match` |
| `Any.max` | `|%_` | `Any.max` |
| `Any.min` | `|%_` | `Any.min` |
| `Any.minmax` | `|%_` | `Any.minmax` |
| `Any.permutations` | `|c` | `List.permutations` |
| `Any.prepend` | `|c` | `Array.prepend` |
| `Any.push` | `|c` | `Array.push` |
| `Any.sort` | `|c` | `List.sort` |
| `Any.tail` | `|c` | `List.tail` |
| `Any.unshift` | `|c` | `Array.unshift` |
| `Array::Shaped.BIND-POS` | `|c` | `Any.BIND-POS` |
| `Array::Shaped.DELETE-POS` | `|c` | `Any.DELETE-POS` |
| `Backtrace::Frame.new` | `positional-%_` | `Mu.POPULATE` |
| `Baggy.categorize-list` | `|c` | `Baggy.categorize-list` |
| `Baggy.classify-list` | `|c` | `Baggy.classify-list` |
| `CompUnit::PrecompilationStore::FileSystem.new-unit` | `|c` | `CompUnit::PrecompilationUnit::File.new` |
| `CompUnit::Repository::Distribution.new` | `|%_` | `CompUnit::Repository::Distribution.bless` |
| `CompUnit::Repository::Locally.new` | `|%_` | `CompUnit::Repository::Locally.bless` |
| `Cool.EVAL` | `|%opts` | `GLOBAL.EVAL` |
| `Cool.contains` | `|%_` | `Str.contains` |
| `Cool.encode` | `|c` | `Str.encode` |
| `Cool.index` | `|%_` | `Str.index` |
| `Cool.indices` | `|%_` | `Str.indices` |
| `Cool.rindex` | `|%_` | `Str.rindex` |
| `Cool.split` | `|c` | `Str.split` |
| `Cool.subst` | `|%options` | `Str.subst` |
| `Cool.subst-mutate` | `|c` | `Str.subst-mutate` |
| `Cool.substr-eq` | `|%_` | `Str.starts-with` |
| `Cool.substr-rw` | `|c` | `Str.substr-rw` |
| `Cool.trans` | `|c` | `Str.trans` |
| `Cool.wordcase` | `|%_` | `Str.wordcase` |
| `Cool.zprintf` | `|c` | `sub.zprintf` |
| `CtxSymIterator::Keys.new` | `|c` | `CtxSymIterator.new` |
| `CtxSymIterator::Values.new` | `|c` | `CtxSymIterator.new` |
| `Date.!new-from-daycount` | `positional-%_` | `Date.!populate` |
| `Date.!populate` | `positional-%_` | `Mu.POPULATE` |
| `Date.clone` | `positional-%_` | `Date.!populate` |
| `Date.new` | `positional-%_` | `Date.!populate` |
| `Date.new-from-daycount` | `positional-%_` | `Date.!new-from-daycount` |
| `Date.today` | `positional-%_` | `Date.!populate` |
| `DateTime.!clone-without-validating` | `|%_` | `DateTime.clone` |
| `DateTime.clone` | `positional-%_` | `DateTime.!populate` |
| `DateTime.new` | `positional-%_` | `DateTime.!populate` |
| `Distribution::Resource.Str` | `|c` | `IO::Path.Str` |
| `Distribution::Resource.absolute` | `|c` | `IO::Path.absolute` |
| `Distribution::Resource.basename` | `|c` | `IO::Path.basename` |
| `Distribution::Resource.comb` | `|c` | `IO::Path.comb` |
| `Distribution::Resource.copy` | `|c` | `IO::Path.copy` |
| `Distribution::Resource.dirname` | `|c` | `IO::Path.dirname` |
| `Distribution::Resource.extension` | `|c` | `IO::Path.extension` |
| `Distribution::Resource.is-absolute` | `|c` | `IO::Path.is-absolute` |
| `Distribution::Resource.is-relative` | `|c` | `IO::Path.is-relative` |
| `Distribution::Resource.lines` | `|c` | `IO::Path.lines` |
| `Distribution::Resource.open` | `|c` | `IO::Path.open` |
| `Distribution::Resource.parts` | `|c` | `IO::Path.parts` |
| `Distribution::Resource.relative` | `|c` | `IO::Path.relative` |
| `Distribution::Resource.resolve` | `|c` | `IO::Path.resolve` |
| `Distribution::Resource.slurp` | `|c` | `IO::Path.slurp` |
| `Distribution::Resource.split` | `|c` | `IO::Path.split` |
| `Distribution::Resource.volume` | `|c` | `IO::Path.volume` |
| `Distribution::Resource.words` | `|c` | `IO::Path.words` |
| `Failure.new` | `|c` | `X::AdHoc.from-slurpy` |
| `Grammar.parse` | `|%_` | `Grammar.new` |
| `Grammar.parsefile` | `|%_` | `Grammar.parse` |
| `Grammar.subparse` | `|%_` | `Grammar.new` |
| `Grammar.typed_exception` | `|%opts` | `X::Comp.new` |
| `Hash.categorize-list` | `|c` | `Hash.categorize-list` |
| `Hash.classify-list` | `|c` | `Hash.classify-list` |
| `Hash::Ordered.STORE` | `|c` | `Hash.STORE` |
| `IO::CatHandle.comb` | `|c` | `Str.comb` |
| `IO::CatHandle.lock` | `|c` | `IO::Handle.lock` |
| `IO::CatHandle.new` | `|%_` | `IO::CatHandle.bless` |
| `IO::CatHandle.seek` | `|c` | `IO::Handle.seek` |
| `IO::CatHandle.split` | `|c` | `Str.split` |
| `IO::Handle.comb` | `|c` | `Str.comb` |
| `IO::Handle.printf` | `|c` | `sub.sprintf` |
| `IO::Handle.split` | `|c` | `Str.split` |
| `IO::Path.chdir` | `|c` | `IO::Path.chdir` |
| `IO::Path.comb` | `|c` | `IO::Handle.comb` |
| `IO::Path.lines` | `|c` | `IO::Handle.lines` |
| `IO::Path.move` | `|c` | `IO::Path.copy` |
| `IO::Path.new` | `positional-%_` | `IO::Path.from-dash` |
| `IO::Path.open` | `|c` | `IO::Handle.open` |
| `IO::Path.split` | `|c` | `IO::Handle.split` |
| `IO::Path.words` | `|c` | `IO::Handle.words` |
| `IO::Path::Spec.new` | `|c` | `IO::Path.new` |
| `IO::Socket::Async::Datagram.decode` | `|c` | `Blob.decode` |
| `IO::Socket::Async::Datagram.encode` | `|c` | `Str.encode` |
| `IO::Socket::INET.new` | `|%options` | `Mu.bless` |
| `IO::Spec::Cygwin.abs2rel` | `|c` | `IO::Spec::Win32.abs2rel` |
| `IO::Spec::Cygwin.catpath` | `|c` | `IO::Spec::Win32.catpath` |
| `IO::Spec::Cygwin.join` | `|c` | `IO::Spec::Win32.join` |
| `IO::Spec::Cygwin.rel2abs` | `|c` | `IO::Spec::Win32.rel2abs` |
| `IO::Spec::Cygwin.splitpath` | `|c` | `IO::Spec::Win32.splitpath` |
| `IO::Spec::Unix.catfile` | `|c` | `IO::Spec::Unix.catdir` |
| `Map.new` | `positional-%_` | `Map.new` |
| `Mu.WALK` | `|%options` | `Mu.WALK` |
| `Mu.bless` | `positional-%_` | `Mu.POPULATE` |
| `Mu.clone` | `positional-%_` | `Mu.!clone-with-twiddles` |
| `Mu.dispatch:<.*>` | `|c` | `Mu.!batch-call` |
| `Mu.dispatch:<.+>` | `|c` | `Mu.!batch-call` |
| `Mu.new` | `|%options` | `Mu.bless` |
| `Mu.perl` | `|c` | `Mu.raku` |
| `Mu.perlseen` | `|c` | `Mu.rakuseen` |
| `Operator.prec` | `|c` | `OperatorProperties.prec` |
| `ParallelSequence.grep` | `positional-%_` | `Rakudo::Internals::HyperRaceSharedImpl.grep` |
| `ParallelSequence.map` | `positional-%_` | `Rakudo::Internals::HyperRaceSharedImpl.map` |
| `Perl.new` | `|%_` | `Raku.new` |
| `Proc::Async.Supply` | `|%_` | `Proc::Async.Supply` |
| `Proc::Async.new` | `|%_` | `Proc::Async.bless` |
| `Proc::Async.put` | `|c` | `Proc::Async.print` |
| `Proc::Async.say` | `|c` | `Proc::Async.print` |
| `Proc::Async.stderr` | `|%_` | `Proc::Async.stderr` |
| `Proc::Async.stdout` | `|%_` | `Proc::Async.stdout` |
| `PseudoStash.NEW-PACKAGE` | `|%initargs` | `PseudoStash.new` |
| `REPL.repl-loop` | `|%adverbs` | `REPL.new-repl-eval` |
| `RakuAST::Deparse.deparse` | `|%_` | `RakuAST::Deparse.deparse` |
| `RakuAST::Deparse.deparse-without-highlighting` | `|%_` | `RakuAST::Deparse.deparse` |
| `RakuAST::Doc::Block.from-alias` | `|%_` | `RakuAST::Doc::Block.new` |
| `RakuAST::Doc::Block.from-config` | `|%_` | `RakuAST::Doc::Block.new` |
| `RakuAST::Doc::Block.from-paragraphs` | `|%_` | `RakuAST::Doc::Block.new` |
| `RakuAST::Node.EVAL` | `|%options` | `CORE.EVAL` |
| `Rakudo::Internals::HyperRaceSharedImpl.grep` | `|%options` | `Any.grep` |
| `Rakudo::Internals::HyperRaceSharedImpl.map` | `|%options` | `Any.map` |
| `Rakudo::Internals::ShapedArrayCommon.ASSIGN-POS` | `|c` | `Any.ASSIGN-POS` |
| `Rakudo::Internals::ShapedArrayCommon.AT-POS` | `|c` | `Any.AT-POS` |
| `Rakudo::Internals::ShapedArrayCommon.EXISTS-POS` | `|c` | `Any.EXISTS-POS` |
| `Rakudo::Internals::ShapedArrayCommon.append` | `|c` | `Any.append` |
| `Rakudo::Internals::ShapedArrayCommon.combinations` | `|c` | `Any.combinations` |
| `Rakudo::Internals::ShapedArrayCommon.join` | `|c` | `Seq.join` |
| `Rakudo::Internals::ShapedArrayCommon.permutations` | `|c` | `Any.permutations` |
| `Rakudo::Internals::ShapedArrayCommon.pick` | `|c` | `Any.pick` |
| `Rakudo::Internals::ShapedArrayCommon.prepend` | `|c` | `Any.prepend` |
| `Rakudo::Internals::ShapedArrayCommon.push` | `|c` | `Any.push` |
| `Rakudo::Internals::ShapedArrayCommon.roll` | `|c` | `Any.roll` |
| `Rakudo::Internals::ShapedArrayCommon.sort` | `|c` | `Seq.sort` |
| `Rakudo::Internals::ShapedArrayCommon.unshift` | `|c` | `Any.unshift` |
| `Range.first` | `|%_` | `Any.first` |
| `Range.fmt` | `|c` | `List.fmt` |
| `Routine.prec` | `|c` | `OperatorProperties.prec` |
| `Seq.sort` | `|c` | `List.sort` |
| `Sequence.fmt` | `|c` | `List.fmt` |
| `Str.!SUBST` | `|%options` | `Str.match` |
| `Str.!match-cursor` | `positional-%_` | `Str.!match-nth` |
| `Str.!match-nth` | `positional-%_` | `Str.!match-cursor` |
| `Str.!match-pattern` | `positional-%_` | `Str.!match-cursor` |
| `Str.IO` | `positional-%_` | `IO::Path.from-dash` |
| `Str.contains` | `|%_` | `Str.contains` |
| `Str.ends-with` | `|%_` | `Str.ends-with` |
| `Str.index` | `|%_` | `Str.index` |
| `Str.indices` | `|%_` | `Str.indices` |
| `Str.match` | `|c` | `Str.match` |
| `Str.rindex` | `|%_` | `Str.rindex` |
| `Str.starts-with` | `|%_` | `Str.starts-with` |
| `Str.subst` | `|%options` | `Str.!SUBST` |
| `Str.subst-mutate` | `|%options` | `Str.match` |
| `Str.substr-eq` | `|%_` | `Str.starts-with` |
| `Str.trans` | `|%_` | `Str.trans` |
| `Supply.act` | `|%options` | `Supply.tap` |
| `Supply.comb` | `|c` | `Str.comb` |
| `Supply.first` | `|c` | `Supply.grep` |
| `Supply.split` | `|%_` | `Str.split` |
| `Thread.start` | `|%options` | `Thread.new` |
| `ThreadPoolScheduler.cue` | `positional-%_` | `ThreadPoolScheduler.!CUE_DELAY_TIMES` |
| `WalkList.CALL-ME` | `|c` | `WalkList.invoke` |
| `WalkList.invoke` | `|c` | `WalkList.invoke` |
| `X::Caller::NotDynamic.new` | `|%options` | `Mu.new` |
| `X::Symbol::NotDynamic.new` | `|%options` | `Mu.new` |
| `X::Symbol::NotLexical.new` | `|%options` | `Mu.new` |
| `sub.HYPER` | `|c` | `Hyper.infix` |
| `sub.comb` | `|%_` | `Cool.comb` |
| `sub.open` | `|c` | `IO::Handle.open` |
| `sub.postcircumfix:<[ ]>` | `%_` | `Array::Element.access` |
| `sub.postcircumfix:<[; ]>` | `|%_` | `sub.postcircumfix:<[; ]>` |
| `sub.postcircumfix:<{ }>` | `%_` | `sub.SLICE_ONE_HASH` |
| `sub.postcircumfix:<{; }>` | `|%_` | `sub.postcircumfix:<{; }>` |
| `sub.spurt` | `|%_` | `IO::Handle.spurt` |
| `sub.to-json` | `|c` | `Rakudo::Internals::JSON.to-json` |

## Adverbs confirmed to work

Probed against the running Rakudo and observed to change behaviour.

| call | adverbs |
|---|---|
| `Allomorph.comb` | `:match` |
| `Allomorph.split` | `:skip-empty` `:v` |
| `Allomorph.subst` | `:g` |
| `Allomorph.subst-mutate` | `:g` |
| `Any.first` | `:end` `:k` `:kv` `:p` |
| `Any.grep` | `:k` `:kv` `:p` |
| `Any.match` | `:g` `:x` |
| `Any.max` | `:k` `:kv` `:p` `:v` |
| `Any.min` | `:k` `:kv` `:p` `:v` |
| `Blob.decode` | `:replacement` `:strict` |
| `Collation.set` | `:primary` `:quaternary` `:secondary` `:tertiary` |
| `Cool.contains` | `:ignorecase` |
| `Cool.ends-with` | `:ignorecase` |
| `Cool.index` | `:ignorecase` |
| `Cool.indices` | `:ignorecase` `:overlap` |
| `Cool.split` | `:k` `:skip-empty` `:v` |
| `Cool.starts-with` | `:ignorecase` |
| `Cool.subst` | `:g` `:nth` `:th` `:x` |
| `Cool.subst-mutate` | `:g` |
| `Cool.substr-eq` | `:ignorecase` |
| `Cool.trans` | `:complement` `:squash` |
| `Date.clone` | `:day` `:formatter` `:month` `:year` |
| `Date.new` | `:formatter` |
| `Date.new-from-daycount` | `:formatter` |
| `Date.today` | `:formatter` |
| `DateTime.clone` | `:day` `:formatter` `:hour` `:minute` `:month` `:second` `:timezone` `:year` |
| `DateTime.new` | `:day` `:formatter` `:hour` `:minute` `:second` `:timezone` |
| `DateTime.posix` | `:real` |
| `Dateish.earlier` | `:day` `:month` |
| `Dateish.later` | `:day` `:hour` |
| `Encoding::Builtin.decoder` | `:strict` `:translate-nl` |
| `Encoding::Builtin.encoder` | `:replacement` |
| `Hash.categorize-list` | `:as` |
| `Hash.classify-list` | `:as` |
| `IO::Spec::Cygwin.canonpath` | `:parent` |
| `IO::Spec::Cygwin.splitpath` | `:nofile` |
| `IO::Spec::QNX.canonpath` | `:parent` |
| `IO::Spec::Unix.canonpath` | `:parent` |
| `IO::Spec::Win32.canonpath` | `:parent` |
| `IO::Spec::Win32.rel2abs` | `:omit-volume` |
| `IO::Spec::Win32.splitpath` | `:nofile` |
| `Int.Str` | `:subscript` `:superscript` |
| `Iterable.flat` | `:hammer` |
| `Mu.WALK` | `:descendant` `:methods` `:submethods` `:super` |
| `ParallelSequence.grep` | `:k` `:kv` `:p` |
| `Parameter.new` | `:default` `:sub-signature` `:type` `:where` |
| `Proc::Async.new` | `:arg0` `:enc` `:w` |
| `RakuAST::Deparse.deparse` | `:no-sink` `:skip` |
| `RakuAST::Node.grep` | `:k` `:kv` `:p` |
| `Rakudo::Internals::HyperRaceSharedImpl.grep` | `:k` `:kv` `:p` |
| `Rakudo::Internals::ShapedArrayCommon.sort` | `:k` |
| `Range.first` | `:end` `:k` `:kv` `:p` |
| `Str.IO` | `:CWD` `:SPEC` |
| `Str.subst` | `:as` `:c` `:continue` `:ex` `:exhaustive` `:g` `:global` `:ii` `:mm` `:nd` `:nth` `:ov` `:overlap` `:p` `:pos` `:rd` `:s` `:samecase` `:samemark` `:samespace` `:sigspace` `:ss` `:st` `:th` `:x` |
| `Str.subst-mutate` | `:as` `:c` `:continue` `:ex` `:exhaustive` `:g` `:global` `:nd` `:nth` `:ov` `:overlap` `:p` `:pos` `:s` `:sigspace` `:st` `:x` |
| `Str.substr-eq` | `:i` `:ignorecase` `:ignoremark` `:m` |
| `Str.trans` | `:c` `:complement` `:d` `:delete` `:s` `:squash` |
| `Supply.act` | `:done` `:quit` `:tap` |
| `Supply.split` | `:skip-empty` `:v` |
| `Thread.start` | `:app_lifetime` `:name` |
| `ThreadPoolScheduler.cue` | `:catch` |
| `X::Caller::NotDynamic.new` | `:symbol` |
| `X::Symbol::NotDynamic.new` | `:package` `:symbol` |
| `X::Symbol::NotLexical.new` | `:package` `:symbol` |
| `sub.postcircumfix:<[ ]>` | `:BIND` `:delete` `:exists` `:k` `:kv` `:p` `:v` |
| `sub.postcircumfix:<[; ]>` | `:BIND` `:delete` `:exists` `:k` `:kv` `:p` `:v` |
| `sub.postcircumfix:<{ }>` | `:BIND` `:delete` `:exists` `:k` `:kv` `:p` `:v` |
| `sub.postcircumfix:<{; }>` | `:exists` |
| `sub.trait_mod:<is>` | `:DEPRECATED` `:built` `:copy` `:default` `:hidden-from-backtrace` `:implementation-detail` `:nodal` `:pure` `:raw` `:readonly` `:required` `:rw` |

## Provenance

- Rakudo: **rakudo-2026.03**, setting source at commit
  **ad13c5f70**
- Declared parameters: live introspection of the running Rakudo (10977 effective rows)
- Construct adverbs: `Perl6/Actions.nqp`, cross-checked against `Raku/ast/code.rakumod`
- Forwarding and strictness: agent extraction from the setting source, every row
  carrying a `path:line` in `scripts/cache/forwarding.tsv` and `strictness.tsv`
- Verdicts: 234 probes executed against this Rakudo

Regenerate: `scripts/cheatsheet/10-inventory.raku` → `20-harvest` → `30-slices` →
agent fan-out → `40-merge` → `50-verify` → `60-render`.
