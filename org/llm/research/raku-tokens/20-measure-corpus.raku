#!/usr/bin/env raku
use v6.d;
use lib $*PROGRAM.parent.add('lib').Str;
use BPE;

#| LEVEL 1 of the experiment: the pure tokenizer penalty.
#|
#| Measures tokens-per-byte over bodies of code *nobody in this experiment
#| wrote*. That is the point: it isolates how expensive a language's surface
#| syntax is to tokenize from how well any particular author (or model) writes
#| it. Authoring bias cannot contaminate a corpus that predates the question.
#|
#| Emits one TSV row per file to 90-corpus-per-file.tsv.

#| VOCAB=o200k re-runs the whole measurement against a second, independently
#| trained byte-level BPE. If the ranking of languages survives that, it is a
#| property of the languages rather than of one vocabulary's merge table.
my $vocab = %*ENV<VOCAB> // 'cl100k';
my $enc   = $vocab eq 'o200k' ?? BPE::o200k() !! BPE::cl100k();
note "loaded {$enc.name}: {$enc.rank.elems} entries";

#| Per-corpus byte budget. Sampling is seeded so the run reproduces.
#|
#| This started at 400 kB, which was far too small: three different samples put
#| Raku at -5.7%, -7.2% and -10.3% against Python. That spread is larger than
#| the effect being measured, so the budget is now set to consume essentially
#| the whole available population of the scarcer corpora.
constant BUDGET  = 4_000_000;
constant SEED    = 20260728;

my $repo = $*PROGRAM.parent.parent.parent.parent.parent;   # .../raku-intellij-plugin

sub gather(@roots, @exts, :@exclude = ()) {
    my @out;
    for @roots -> $r {
        next unless $r.IO.e;
        for $r.IO.dir(:!absolute).kv -> $, $ { }   # force existence check
        @out.append: find-files($r.IO, @exts, @exclude);
    }
    @out;
}

sub find-files(IO::Path $dir, @exts, @exclude) {
    my @found;
    my @queue = $dir;
    while @queue {
        my $d = @queue.shift;
        for $d.dir -> $p {
            my $s = $p.Str;
            next if @exclude.first({ $s.contains($_) });
            if $p.d {
                @queue.push($p);
            }
            elsif @exts.first({ $s.ends-with($_) }) {
                @found.push($p);
            }
        }
        CATCH { default { } }
    }
    @found;
}

#| Corpora. Each is (label, language, files).
my @corpora;

@corpora.push: ['python-stdlib', 'Python',
    find-files('/usr/lib/python3.14'.IO, ['.py'],
               ['/test/', '/tests/', 'site-packages', '/idlelib/', '/lib2to3/'])];

@corpora.push: ['raku-ecosystem', 'Raku',
    "$*HOME/.rakubrew/versions/moar-2026.03/share/perl6/site/sources".IO.e
        ?? "$*HOME/.rakubrew/versions/moar-2026.03/share/perl6/site/sources".IO.dir.grep(*.f)
        !! ()];

@corpora.push: ['raku-repo', 'Raku',
    find-files($repo.add('testData').IO, ['.raku', '.rakumod', '.p6', '.pm6'], ['/build/'])
        .append(find-files($repo.add('scripts').IO, ['.raku', '.rakumod', '.p6', '.pm6'], []))];

@corpora.push: ['kotlin-repo', 'Kotlin',
    find-files($repo.add('src/main/java').IO, ['.kt'], ['/build/'])];

@corpora.push: ['java-repo', 'Java',
    find-files($repo.add('src/main/java').IO, ['.java'], ['/build/', 'MAINBraid'])];

@corpora.push: ['prose-markdown', 'English',
    find-files($repo.add('org/llm/traces').IO, ['.md'], [])
        .append(find-files($repo.add('docs').IO, ['.md'], []))];

my $out = $*PROGRAM.parent.add('90-corpus-per-file.tsv').open(:w);
$out.say: join "\t", <corpus language file bytes chars lines tokens bytes_per_token tokens_per_line>;

for @corpora -> ($label, $lang, @files) {
    unless @files {
        note "  $label: NO FILES -- skipped";
        next;
    }
    # `.pick` takes no :seed -- passing one is silently ignored and every run
    # reshuffles, which moved the headline figure by ~1.5 points between runs
    # before this was caught. srand() is what actually makes Raku's PRNG
    # reproducible; sort first so the input order is deterministic too.
    srand(SEED);
    my @sample = @files.sort(*.Str).pick(*);
    my $used = 0;
    my $n    = 0;
    for @sample -> $f {
        last if $used >= BUDGET;
        my $text = try $f.slurp;
        next without $text;
        next if $text.chars == 0;
        my $bytes = $text.encode('utf-8').bytes;
        next if $bytes > 60_000;          # skip outliers that would dominate
        my $tokens = $enc.count($text);
        next unless $tokens;
        my $lines = $text.lines.elems;
        $out.say: join "\t",
            $label, $lang, $f.Str, $bytes, $text.chars, $lines, $tokens,
            ($bytes / $tokens).fmt('%.4f'),
            ($lines ?? ($tokens / $lines).fmt('%.4f') !! '0');
        $used += $bytes;
        $n++;
    }
    note "  $label ($lang): $n files, {$used} bytes";
}

$out.close;
note "wrote 90-corpus-per-file.tsv";
