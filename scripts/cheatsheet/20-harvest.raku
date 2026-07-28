#!/usr/bin/env raku
use v6.d;

#| STAGE 20 -- harvest Rakudo's own adverb tables.
#|
#| Rakudo already knows which adverbs are legal on `m//`, `s///`, `tr///`, `rx//` and
#| on slices. It hard-codes them at compile time. Reading those tables beats inferring
#| anything from signatures, because for `subst` and `trans` the signatures are empty
#| -- the adverbs are read out of `%_` by hand, one hop downstream.
#|
#| Three sources, and the first two are independent implementations of the same data
#| (the legacy Perl6:: frontend and the newer RakuAST one). They are cross-checked
#| against each other here: if they ever diverge, that is a Rakudo bug or a stale
#| checkout, and either way this script should say so rather than pick a winner.
#|
#| Writes scripts/cache/adverb-tables.tsv.

my $repo  = $*PROGRAM.parent.parent.parent;
my $cache = $repo.add('scripts/cache');
my $src   = (%*ENV<RAKUDO_SRC> // $*HOME.add('code/raku/x.core/rakudo').Str).IO;

my $actions = $src.add('src/Perl6/Actions.nqp');
my $rakuast = $src.add('src/Raku/ast/code.rakumod');
my $slices  = $src.add('src/core.c/Rakudo/Internals/PostcircumfixAdverbs.rakumod');
for $actions, $rakuast, $slices -> $f {
    die "missing $f -- set RAKUDO_SRC to a Rakudo checkout" unless $f.e;
}

#| Body of `NAME := hash( ... );` as a single string.
sub hash-body(Str $text, Str $name --> Str) {
    $text ~~ / $name \s* ':=' \s* ['nqp::'?] 'hash(' $<body>=[<-[)]>*] ')' /
        or die "could not find table $name";
    ~$<body>;
}

# ---------------------------------------------------------------- Actions.nqp

my $atext = $actions.slurp;

#| `ignorecase  => 'i',`
sub arrow-pairs(Str $body) {
    my %h;
    # Bind each match: inside a `for`, a bare $<k> resolves against $/ -- which is the
    # whole match *list* here, not the current element.
    for $body.match(/ $<k>=[\w+] \s* '=>' \s* \' $<v>=[<-[']>+] \' /, :g) -> $m {
        %h{~$m<k>} = ~$m<v>;
    }
    %h;
}

my %canonical = arrow-pairs(hash-body($atext, '%REGEX_ADVERBS_CANONICAL'));
my %implies   = arrow-pairs(hash-body($atext, '%REGEX_ADVERB_IMPLIES'));
my %multiple  = arrow-pairs(hash-body($atext, '%MATCH_ADVERBS_MULTIPLE').subst(/'=>' \s* 1/, "=> '1'", :g));

die "canonical table looks empty" unless %canonical > 10;

#| The four allowed-sets live in an INIT block as `$mods := '...'` followed by the
#| hash they populate. Pair each string with the name that follows it.
sub allowed-sets(Str $text) {
    my %sets;
    my @chunks = $text.match(
        / '$mods' \s* ':=' \s* \' $<words>=[<-[']>+] \' .+? '%' $<set>=[\w+] '_ALLOWED_ADVERBS' /,
        :g);
    for @chunks -> $m {
        %sets{~$m<set>} = (~$m<words>).words.List;
    }
    %sets;
}

my %allowed = allowed-sets($atext);
for <SHARED SUBST MATCH TRANS> -> $k {
    die "missing %{$k}_ALLOWED_ADVERBS" unless %allowed{$k};
    # A set that came back as one long string means the split silently didn't happen,
    # which would emit a single bogus "adverb" containing spaces.
    die "%{$k}_ALLOWED_ADVERBS did not split into words" if %allowed{$k}.elems == 1
        && %allowed{$k}[0].contains(' ');
}
note "allowed sets: " ~ %allowed.sort(*.key).map({ "{.key}={.value.elems}" }).join(', ');

# ---------------------------------------------------------- RakuAST cross-check

my $rtext = $rakuast.slurp;

#| `'ignorecase',   'i',`
sub comma-pairs(Str $body) {
    my %h;
    for $body.match(/ \' $<k>=[<-[']>+] \' \s* ',' \s* \' $<v>=[<-[']>+] \' /, :g) -> $m {
        %h{~$m<k>} = ~$m<v>;
    }
    %h;
}

my %norms = comma-pairs(hash-body($rtext, 'NORMS'));

my @divergent = (%canonical.keys ∪ %norms.keys).keys.grep({ %canonical{$_} ne (%norms{$_} // '') });
if @divergent {
    note "WARNING: the two frontends disagree on: {@divergent.sort.join(', ')}";
    note "         legacy Perl6::Actions vs RakuAST. Treating legacy as authoritative.";
}
else {
    note "cross-check: both frontends agree on all {%canonical.elems} alias mappings";
}

#| `my constant COMPS := nqp::hash('i', 1, 'm', 1, ...)` -- names only, values are 1.
sub flag-set(Str $text, Str $name) {
    $text ~~ / 'constant' \s+ $name \s* ':=' \s* 'nqp::hash(' $<body>=[<-[)]>*] ')' /
        or die "could not find constant $name";
    (~$<body> ~~ m:g/ \' $<k>=[<-[']>+] \' \s* ',' \s* 1 /).map({ ~.<k> }).Set;
}

my $compilation = flag-set($rtext, 'COMPS');
die "compilation-adverb set looks wrong" unless $compilation ⊇ set(<i m r s P5>);

# ------------------------------------------------------------------- slices

my $slice-adverbs = $slices.lines
    .map({ $_ ~~ / 'SLICE_' \w+ \s* '=' .+? '#' \s* ':' $<neg>=['!'?] $<name>=[\w+] / ?? ~$<name> !! Empty })
    .unique
    .sort;
die "no slice adverbs found" unless $slice-adverbs;

# -------------------------------------------------------------------- output

$cache.mkdir;
my $out = $cache.add('adverb-tables.tsv').open(:w);
$out.say: join "\t", <construct adverb canonical implies kind source>;

#| Every construct also accepts the shared compilation adverbs.
# `|` rather than .flat: a List stored in a hash element is *itemized*, and .flat does
# not descend into an item. `(%allowed<SHARED>, %allowed<MATCH>).flat` therefore
# yielded two long strings instead of 26 adverbs -- and the single-set rx// case
# worked, which is exactly how the bug stayed invisible.
my %constructs =
    'rx//'  => %allowed<SHARED>.List,
    'm//'   => (|%allowed<SHARED>, |%allowed<MATCH>).unique.List,
    's///'  => (|%allowed<SHARED>, |%allowed<SUBST>).unique.List,
    'tr///' => (|%allowed<SHARED>, |%allowed<TRANS>).unique.List,
    'slice' => $slice-adverbs.List,
;

my $rows = 0;
for %constructs.sort(*.key) -> (:key($construct), :value(@adverbs)) {
    for @adverbs.sort -> $a {
        my $canon = %canonical{$a} // $a;
        # if/elsif, not when: `when EXPR` smartmatches EXPR against $_, and $_ here is
        # not the thing being tested. It silently produced inverted kinds -- :g came
        # out "runtime" and :c came out "repeatable", both backwards.
        my $kind = $construct eq 'slice'  ?? 'slice'
                !! $compilation{$canon}   ?? 'compilation-only'
                !! %multiple{$canon}      ?? 'repeatable'
                !!                           'runtime';
        $out.say: join "\t",
            $construct, $a, $canon, (%implies{$canon} // ''), $kind,
            ($construct eq 'slice' ?? 'PostcircumfixAdverbs.rakumod' !! 'Perl6/Actions.nqp');
        $rows++;
    }
}
$out.close;

note "wrote $rows rows to scripts/cache/adverb-tables.tsv";
note "constructs: {%constructs.keys.sort.join(', ')}";
note "compilation-only adverbs (inert when passed as runtime nameds): {$compilation.keys.sort.join(' ')}";
