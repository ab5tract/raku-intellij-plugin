use v6.d;

#| A byte-level BPE tokenizer, sufficient to *count* tokens for `.tiktoken`
#| vocabularies. Written in Raku on purpose: the whole point of this experiment
#| is the cost of processing text in a minority language, and measuring that
#| with a Python harness would be a joke told at our own expense.
#|
#| This is NOT Claude's tokenizer -- see 00-preregistration.md. It is a public
#| byte-level BPE of the same family, used as a proxy, and every claim built on
#| it is a claim about *relative* cost between languages, never absolute tokens.
unit module BPE;

# Base64 decoded to a latin-1 Str: one Char per byte. That representation is
# both the hash key for the merge table and the working form for merging, so
# nothing has to convert between Blob and Str in the inner loop.
my constant @B64-VAL = do {
    my @t = -1 xx 128;
    my $alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';
    @t[$alphabet.substr($_, 1).ord] = $_ for ^64;
    @t;
};

sub b64-to-bytestr(Str $s --> Str) {
    my $out = '';
    my int $acc  = 0;
    my int $bits = 0;
    for $s.comb -> $c {
        my $o = $c.ord;
        next if $o >= 128;
        my $v = @B64-VAL[$o];
        next if $v < 0;
        $acc  = ($acc +< 6) + $v;
        $bits = $bits + 6;
        if $bits >= 8 {
            $bits = $bits - 8;
            $out ~= (($acc +> $bits) +& 0xFF).chr;
        }
    }
    $out;
}

#| Ordered alternation (`||`) throughout, not Raku's default longest-token `|`.
#| tiktoken's patterns are Python `re`, which is first-match; LTM here would
#| silently produce a different -- and wrong -- pretokenization.
our sub cl100k-pattern(--> Regex) {
    rx/
        :i [ "'s" || "'t" || "'re" || "'ve" || "'m" || "'ll" || "'d" ]
        ||  <-[\r\n] -:L -:N>? <:L>+
        ||  <:N> ** 1..3
        ||  ' '? <-[\s] -:L -:N>+ <[\r\n]>*
        ||  \s* <[\r\n]>+
        ||  \s+ <!before \S>
        ||  \s+
    /;
}

our sub o200k-pattern(--> Regex) {
    rx/
        <-[\r\n] -:L -:N>? <:Lu +:Lt +:Lm +:Lo +:M>* <:Ll +:Lm +:Lo +:M>+
            [ :i "'s" || "'t" || "'re" || "'ve" || "'m" || "'ll" || "'d" ]?
        ||  <-[\r\n] -:L -:N>? <:Lu +:Lt +:Lm +:Lo +:M>+ <:Ll +:Lm +:Lo +:M>*
            [ :i "'s" || "'t" || "'re" || "'ve" || "'m" || "'ll" || "'d" ]?
        ||  <:N> ** 1..3
        ||  ' '? <-[\s] -:L -:N>+ <[\r\n/]>*
        ||  \s* <[\r\n]>+
        ||  \s+ <!before \S>
        ||  \s+
    /;
}

class Encoder is export {
    has %.rank;
    has Regex $.pattern;
    has Str $.name;

    method load(::?CLASS:U: Str :$name!, Str :$path!, Regex :$pattern! --> Encoder) {
        my %rank;
        for $path.IO.lines -> $line {
            next unless $line.chars;
            my ($b64, $r) = $line.words;
            next unless $r.defined;
            %rank{ b64-to-bytestr($b64) } = +$r;
        }
        self.bless(:%rank, :$pattern, :$name);
    }

    #| tiktoken's byte_pair_merge: repeatedly fuse the adjacent pair whose
    #| concatenation has the lowest rank in the vocabulary. Quadratic in the
    #| length of a pretoken, which is fine -- pretokens are a handful of bytes.
    method !merge-count(Str $piece --> Int) {
        return 1 if %!rank{$piece}:exists;

        my @parts = $piece.comb;
        loop {
            my $best-rank = Inf;
            my $best-i    = -1;
            for 0 ..^ @parts.end -> $i {
                my $pair = @parts[$i] ~ @parts[$i + 1];
                with %!rank{$pair} -> $r {
                    if $r < $best-rank {
                        $best-rank = $r;
                        $best-i    = $i;
                    }
                }
            }
            last if $best-i < 0;
            @parts.splice($best-i, 2, @parts[$best-i] ~ @parts[$best-i + 1]);
        }
        @parts.elems;
    }

    #| Number of tokens $text encodes to.
    method count(Str $text --> Int) {
        my $pat = $!pattern;   # regexes are Cursor methods; $!attr is not visible inside
        my int $total = 0;
        for $text ~~ m:g/<{ $pat }>/ -> $m {
            # BPE operates on UTF-8 bytes; latin-1 round-trips them one Char
            # per byte, which is the form %!rank is keyed by.
            $total = $total + self!merge-count($m.Str.encode('utf-8').decode('latin-1'));
        }
        $total;
    }

    #| Every pretoken piece must decompose into vocabulary entries whose bytes
    #| concatenate back to the input. Used by the self-test.
    method verify-roundtrip(Str $text --> Bool) {
        my $pat = $!pattern;
        my $rebuilt = '';
        for $text ~~ m:g/<{ $pat }>/ -> $m { $rebuilt ~= $m.Str }
        $rebuilt eq $text;
    }
}

our sub cl100k(--> Encoder) {
    Encoder.load(
        :name<cl100k_base>,
        :path($?FILE.IO.parent.parent.add('vocab/cl100k_base.tiktoken').Str),
        :pattern(cl100k-pattern()),
    );
}

our sub o200k(--> Encoder) {
    Encoder.load(
        :name<o200k_base>,
        :path($?FILE.IO.parent.parent.add('vocab/o200k_base.tiktoken').Str),
        :pattern(o200k-pattern()),
    );
}
