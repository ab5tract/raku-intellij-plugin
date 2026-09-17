class C {
    method ::($meth)($/) {
        42
    }
    method ::('lit')($x) { $x }
    method normal($y) { $y }
}
my role TermAction[$meth, $subname] {
    method ::($meth)($/) {
        self.attach: $/, self.r('Term::Named').new($subname)
    }
}
999999;
