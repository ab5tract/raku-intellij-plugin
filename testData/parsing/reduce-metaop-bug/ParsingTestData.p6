# Wordy reduce metaops must be recognised (precedence guard must not apply
# when identifying a meta-operator's base operator).
my $rowwise = [and] map { $_ > 0 }, @values;
my $anyneg  = [or]  map { $_ < 0 }, @values;
my $x       = [xor] @flags;

# The same, with a nested map inside the reduce's block argument -- this is
# the construct that used to collapse highlighting for the rest of the file.
my $deep = [and] map { g(map { abs $_ }, @b) }, ^5;

# Symbolic reduces must keep working unchanged.
say [+] 1, 2, 3;
say [*] 1, 2, 3;

# An empty inner array must parse as nested array composers, NOT as a reduce
# metaop wrapping an empty/incomplete bracketed infix.
my @empty = [[]];
my @grid  = [[] xx 3];
my @rows  = [[], [], []];

# A *complete* bracketed infix reduce must still be recognised.
my $r = [[+]] @a, @b;
