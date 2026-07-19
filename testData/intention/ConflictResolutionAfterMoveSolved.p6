class Bar {
    method a {
        self!foo(1, 2, c => 3, d => 4, 5);
    }
    method !foo($p1, $p2, $p3, :$c, :$d) {...}
}