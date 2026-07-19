class Bar {
    method a {
        sub foo($a?) {
            my $var;
            my $bar;
            self!mmm(1, 1, [1], (2), named => 1, double => 2, double => 2, foo, foo(1), Bar.value(), $var, $bar);
        }
    }
    method !mmm($p1, $p2, @p1, @p2, $foo1, $foo2, $value, $var, $bar, :$named, :$double1, :$double2) {...}
}