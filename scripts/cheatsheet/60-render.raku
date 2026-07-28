#!/usr/bin/env raku
use v6.d;

#| STAGE 60 -- render the cheat sheet a human or an agent actually reads.
#|
#| Everything upstream is TSV for machines. This is the page you look at before
#| writing `.dir(:recursive)`.
#|
#| Writes docs/raku-named-args.md.

my $repo  = $*PROGRAM.parent.parent.parent;
my $cache = $repo.add('scripts/cache');
my $doc   = $repo.add('docs/raku-named-args.md');

sub tsv(Str $name, Int $cols) {
    my $f = $cache.add($name);
    return () unless $f.e;
    $f.lines.skip(1).grep(*.trim.chars).map({ .split("\t") }).grep(*.elems >= $cols);
}

my @adverbs   = tsv('adverb-tables.tsv', 6);
my @strict    = tsv('strictness.tsv',    6);
my @verdicts  = tsv('verdicts.tsv',      4);
my @effective = tsv('effective.tsv',     5);
my @forward   = tsv('forwarding.tsv',    6);

my %source = $cache.add('SOURCE.txt').e
    ?? $cache.add('SOURCE.txt').lines.map({ my ($k,$v) = .split("\t"); $k => $v }).Hash
    !! {};

my @inert    = @verdicts.grep({ .[3] eq 'inert' });
my @verified = @verdicts.grep({ .[3] eq 'verified' });

my $md = q:to/HEAD/;
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

HEAD

# ---------------------------------------------------------------- inert trap

if @inert {
    $md ~= q:to/SEC/;
## Adverbs that are accepted but do nothing

These were **empirically probed against the running Rakudo** and demonstrably had no
effect. They are the nastiest class: documented, correct-looking, and inert. Most are
*compilation* adverbs — they work written syntactically (`s:i/a/b/`) but not passed as
runtime named arguments.

| call | adverb | why |
|---|---|---|
SEC
    for @inert.sort({ (.[0], .[1], .[2]) }) -> @r {
        $md ~= "| `{@r[0]}.{@r[1]}` | `:{@r[2]}` | {@r[4] || 'probe showed no behavioural change'} |\n";
    }
    $md ~= "\n";
}

# ------------------------------------------------------------------- strict

if @strict {
    $md ~= q:to/SEC/;
## Methods that DO complain

Here the silent-drop hazard does not apply — pass a bad adverb and you get an
exception or a warning, not a wrong answer. Worth knowing, because it tells you where
you can rely on the language to catch you.

| call | behaviour | raises | adverbs it accepts |
|---|---|---|---|
SEC
    my %seen;
    for @strict.sort({ (.[0], .[1]) }) -> @r {
        next if %seen{"{@r[0]}.{@r[1]}"}++;
        $md ~= "| `{@r[0]}.{@r[1]}` | {@r[2]} | `{@r[3]}` | "
             ~ (@r[4] ?? @r[4].words.map({ "`:$_`" }).join(' ') !! '—') ~ " |\n";
    }
    $md ~= "\n";
}

# ------------------------------------------------------- construct adverbs

if @adverbs {
    $md ~= q:to/SEC/;
## Regex-construct adverbs, from Rakudo's own tables

Harvested from `src/Perl6/Actions.nqp` and cross-checked against the RakuAST
implementation in `src/Raku/ast/code.rakumod` — both frontends agree.

`compilation-only` means: valid written into the construct (`m:i/…/`), inert as a
runtime named argument.

SEC
    for <m// s/// tr/// rx// slice> -> $c {
        my @rows = @adverbs.grep(*.[0] eq $c);
        next unless @rows;
        $md ~= "### `$c`\n\n| adverb | canonical | kind |\n|---|---|---|\n";
        for @rows.sort(*.[1]) -> @r {
            $md ~= "| `:{@r[1]}` | `{@r[2]}`" ~ (@r[3] ?? " (implies `:{@r[3]}`)" !! '')
                 ~ " | {@r[4]} |\n";
        }
        $md ~= "\n";
    }
}

# ---------------------------------------------------------------- forwarding

if @forward {
    $md ~= q:to/SEC/;
## Where named arguments actually go

A method that declares nothing may still accept adverbs, by forwarding them on. This
is why "not declared" does not mean "invalid" — `Str.subst` declares no named
parameters at all, yet `:g` works, because it hands `%options` to `Str.match`.

| method | forwards via | to |
|---|---|---|
SEC
    my %seen;
    for @forward.sort({ (.[0], .[1]) }) -> @r {
        my $k = "{@r[0]}.{@r[1]}";
        next if %seen{$k}++;
        next if @r[3] eq 'UNKNOWN';
        my $target = (@r[3] eq 'SELF' ?? @r[0] !! @r[3]) ~ '.' ~ @r[4];
        $md ~= "| `$k` | `{@r[2]}` | `$target` |\n";
    }
    $md ~= "\n";
}

# ------------------------------------------------------------------ verified

if @verified {
    $md ~= "## Adverbs confirmed to work\n\n";
    $md ~= "Probed against the running Rakudo and observed to change behaviour.\n\n";
    $md ~= "| call | adverbs |\n|---|---|\n";
    my %by;
    %by{"{.[0]}.{.[1]}"}.push(.[2]) for @verified;
    for %by.keys.sort -> $k {
        $md ~= "| `$k` | " ~ %by{$k}.sort.map({ "`:$_`" }).join(' ') ~ " |\n";
    }
    $md ~= "\n";
}

$md ~= qq:to/FOOT/;
## Provenance

- Rakudo: **{%source<rakudo-id> // 'unknown'}**, setting source at commit
  **{%source<setting-commit> // 'unknown'}**
- Declared parameters: live introspection of the running Rakudo ({@effective.elems} effective rows)
- Construct adverbs: `Perl6/Actions.nqp`, cross-checked against `Raku/ast/code.rakumod`
- Forwarding and strictness: agent extraction from the setting source, every row
  carrying a `path:line` in `scripts/cache/forwarding.tsv` and `strictness.tsv`
- Verdicts: {@verdicts.elems} probes executed against this Rakudo

Regenerate: `scripts/cheatsheet/10-inventory.raku` → `20-harvest` → `30-slices` →
agent fan-out → `40-merge` → `50-verify` → `60-render`.
FOOT

$doc.parent.mkdir;
$doc.spurt($md);
note "wrote docs/raku-named-args.md ({$md.lines.elems} lines)";
