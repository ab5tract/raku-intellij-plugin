#!/usr/bin/env raku
use v6.d;
use lib $*PROGRAM.parent.add('lib').Str;
use BPE;
use Corpus;

#| LEVEL 1 of the experiment: the pure tokenizer penalty.
#|
#| Measures tokens-per-byte over bodies of code *nobody in this experiment
#| wrote*. That is the point: it isolates how expensive a language's surface
#| syntax is to tokenize from how well any particular author (or model) writes
#| it. Authoring bias cannot contaminate a corpus that predates the question.
#|
#| Emits root-relative paths and a content digest per file, plus a manifest
#| recording which roots were resolved on this machine. See lib/Corpus.rakumod
#| for why, and 60-verify-corpus.raku for what that buys.

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
constant BUDGET   = 4_000_000;
constant SEED     = 20260728;
constant MAX-FILE = 60_000;

my $repo = Corpus::repo-root($*PROGRAM.IO);

sub find-files(IO::Path $dir, @exts, @exclude) {
    my @found;
    my @queue = $dir;
    while @queue {
        my $d = @queue.shift;
        for $d.dir -> $p {
            my $s = $p.Str;
            next if @exclude.first({ $s.contains($_) });
            if    $p.d                              { @queue.push($p) }
            elsif @exts.first({ $s.ends-with($_) }) { @found.push($p) }
        }
        CATCH { default { } }
    }
    @found;
}

#| Each corpus is (label, language, root, files). The root is what recorded
#| paths are relative to; a missing root means that corpus is unavailable on
#| this machine and is reported as such rather than silently skipped.
my @corpora;

my $py-root = Corpus::python-stdlib();
@corpora.push: ['python-stdlib', 'Python', $py-root,
    $py-root ?? find-files($py-root, ['.py'],
        ['/test/', '/tests/', 'site-packages', '/idlelib/', '/lib2to3/']) !! ()];

my $eco-root = Corpus::raku-ecosystem();
@corpora.push: ['raku-ecosystem', 'Raku', $eco-root,
    $eco-root ?? $eco-root.dir.grep(*.f) !! ()];

@corpora.push: ['raku-repo', 'Raku', $repo,
    find-files($repo.add('testData'), ['.raku', '.rakumod', '.p6', '.pm6'], ['/build/'])
        .append(find-files($repo.add('scripts'), ['.raku', '.rakumod', '.p6', '.pm6'], []))];

@corpora.push: ['kotlin-repo', 'Kotlin', $repo,
    find-files($repo.add('src/main/java'), ['.kt'], ['/build/'])];

@corpora.push: ['java-repo', 'Java', $repo,
    find-files($repo.add('src/main/java'), ['.java'], ['/build/', 'MAINBraid'])];

@corpora.push: ['prose-markdown', 'English', $repo,
    find-files($repo.add('org/llm/traces'), ['.md'], [])
        .append(find-files($repo.add('docs'), ['.md'], []))];

my $stem = $vocab eq 'o200k' ?? '90-corpus-per-file-o200k' !! '90-corpus-per-file';
my $out  = $*PROGRAM.parent.add("$stem.tsv").open(:w);
$out.say: join "\t", <corpus language path bytes chars lines tokens digest bytes_per_token tokens_per_line>;

my @manifest;

for @corpora -> ($label, $lang, $root, @files) {
    unless $root && @files {
        note "  $label: UNAVAILABLE on this machine";
        @manifest.push: %( :$label, language => $lang, root => 'UNAVAILABLE', files => 0, bytes => 0 );
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
        next unless $text.chars;
        my $bytes = $text.encode('utf-8').bytes;
        next if $bytes > MAX-FILE;          # skip outliers that would dominate
        my $tokens = $enc.count($text);
        next unless $tokens;
        my $lines = $text.lines.elems;
        $out.say: join "\t",
            $label, $lang, Corpus::relative-to($f, $root),
            $bytes, $text.chars, $lines, $tokens, Corpus::digest($text),
            ($bytes / $tokens).fmt('%.4f'),
            ($lines ?? ($tokens / $lines).fmt('%.4f') !! '0');
        $used += $bytes;
        $n++;
    }
    note "  $label ($lang): $n files, $used bytes";
    @manifest.push: %( :$label, language => $lang, root => $root.absolute, files => $n, bytes => $used );
}
$out.close;

my $man = $*PROGRAM.parent.add('95-corpus-manifest.txt').open(:w);
$man.say: "# Corpus roots as resolved on the machine that produced $stem.tsv.";
$man.say: "# Paths in the TSV are relative to the root of their own corpus.";
$man.say: "# Re-running elsewhere resolves different roots; 60-verify-corpus.raku";
$man.say: "# reports the delta rather than asserting the two are the same.";
$man.say: '';
$man.say: "vocabulary\t$vocab";
$man.say: "raku\t{$*RAKU.compiler.version}";
$man.say: "budget\t{BUDGET}\tseed\t{SEED}\tmax_file\t{MAX-FILE}";
$man.say: '';
$man.say: join "\t", <corpus language files bytes root>;
$man.say: join "\t", .<label>, .<language>, .<files>, .<bytes>, .<root> for @manifest;
$man.close;

note "wrote $stem.tsv and 95-corpus-manifest.txt";
