multi m1<error descr="No multi candidates match (($): Not enough positional arguments, ($, $): Not enough positional arguments)">($x)</error> { $x }
multi m1($x, $y) { $x, $y }

sub test-multi() {
    m1();
}

sub slurpy($a, +@as) { $a, @as }
slurpy(1, 2, 3);

sub slurpy2(|c) {}
slurpy2(42, "anything", 42, "test", :test, :!testy);

class C {
    method no-args(C: ) {}
    method one-arg(C: $a) { $a }
    method two-args(C: $a, $b) { $a, $b}
}

sub test-class() {
    C.no-args(); C<error descr="Too many positional arguments">.no-args(1)</error>; C.no-args(:a);
    C.one-arg(42); C<error descr="Not enough positional arguments">.one-arg()</error>; C<error descr="Too many positional arguments">.one-arg(1, 2)</error>; C.one-arg(1, :b);
    C.two-args(1, 2); C<error descr="Not enough positional arguments">.two-args()</error>; C<error descr="Not enough positional arguments">.two-args(1)</error>; C<error descr="Too many positional arguments">.two-args(1, 2, 3)</error>;
}
