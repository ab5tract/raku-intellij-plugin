use experimental :rakuast;
use nqp;

my constant @HIDDEN = <
    sunk thunks okifnil sorries worries origin
    lowered-array-init lowered-to-local initializer-in-method is-parameter
    attribute-package generics-package conflicting-type unit-package
    qualified-root original-type
>;

sub find-first($node, $cls) {
    return $node if $node.^name eq $cls;
    my $found;
    $node.visit-children(-> $c {
        if $c ~~ RakuAST::Node {
            $found //= find-first($c, $cls);
        }
    });
    $found
}

my $source = 'my Int $x = 41 + 1;';
my $ast = $source.AST;
my $n = find-first($ast, "RakuAST::VarDeclaration::Simple");

for $n.^attributes -> $a {
    my $name = $a.name.substr(2);
    next if @HIDDEN.first($name);
    my $raw := try { $a.get_value($n) };
    next if nqp::isnull(nqp::decont($raw));
    next unless (try { $raw.defined }) // False;
    my ($kind, $display);
    if $raw ~~ RakuAST::Node {
        $kind    = 'node';
        $display = (try { $raw.^name ~ " -> '" ~ $raw.DEPARSE.trim ~ "'" }) // '(unrenderable)';
    }
    elsif $raw ~~ Positional {
        $kind    = 'list';
        $display = (try { "[" ~ $raw.elems ~ " items]" }) // '(unrenderable)';
    }
    else {
        $kind    = 'scalar';
        $display = (try { $raw.gist }) // '(unrenderable)';
    }
    say "checking $name (kind=$kind)...";
    my $codes = $display.NFD;
    say "  ok: {$codes.elems} codepoints, display=[{$display}]";
}
say "ALL DONE";
