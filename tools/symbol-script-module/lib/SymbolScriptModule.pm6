#| Zero line.
#|( First line.
Second line. )
our $test-variable is export; #= Inline doc line.

#| Foo
class Foo {
  #| one
  has $.one; #= one

  #| m
  method m(Int $foo) {} #= m

  method impl is implementation-detail {}
}

#| subset 1
subset Subset of Int; #= subset 2

#| enum 1
enum Enum <A B>; #= enum 2

#| subroutine
sub subroutine() {}
#| subroutine