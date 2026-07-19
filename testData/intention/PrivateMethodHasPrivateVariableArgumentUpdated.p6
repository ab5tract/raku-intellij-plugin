class A {
  has $.a;

  method a {
    self!foo($!a);
  }
  method !foo($a) {...}
}