use v6;
use JSON::Tiny;

role R[:param] {}

grammar IPv4 is default {
    token TOP { <seg> ** 4 % '.' }
    token seg {
        \d+ { 0 <= $/ <= 255 }
    }

    proto method dummy() {*}
    multi method dummy(Cool(Int) $coerced) {!!!}
    multi method dummy($pos? where 1 ; :alias($named) --> Nil) {
        constant \foo = key => 'value';
        LABEL: [+] self.dummy(:$named);
    }
}

# Find all IPv4 data sources and show them.
my @data = from-json(slurp 'input.json');
for @data.map(*<from>) -> $from {
    if IPv4.parse($from) {
        do say "Address: $from";
    }
}

BEGIN {
    my $capture = \("capture\n", 42);
    quasi { ++$capture[42]++; }
    my $array[1;1] = ['composed'][0];
    say @$array, $$array; # <- contextualizers work...
}

# Regex fun begins!
'foo' ~~ m:g!^ [(f) <[o]> $<foo>=[]] || 'constant' \invalid !;

# Transliteration.
my $shifted = 'abc'.trans('a..c' => 'x..z');

# Semantic highlighting: built-ins and reassignment.
sub semantics(<reassignedParameter>$p</reassignedParameter> is rw) {
    <reassignedParameter>$p</reassignedParameter> = 42;
    my $total = 0;
    <reassignedVariable>$total</reassignedVariable> += 1;
    <builtinCall>say</builtinCall>(<builtinVariable>$*OUT</builtinVariable>);
}

=begin pod

=head1 A Pod block

Text with B<bold>, I<italic> and U<underlined> formatting.

    my $code = 'in a code block';

=end pod
