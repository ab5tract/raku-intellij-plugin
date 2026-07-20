unit module Module::Walks;

our sub walk-our-sub() {}
our sub walk-our-other() {}
sub walk-exported() is export {}
my sub walk-hidden() {}

our class Walker {
    method walk-method() {}
}
our class Walkest {}

my enum WalkColor is export <wred wgreen wblue>;

our subset WalkSubset of Int where * > 0;
