say 42;

# Check it doesn't pass too few or too many positional arguments (remember to account for all the kinds of slurpy)
# If it is a sub, check it doesn't pass any named arguments that don't have matching parameters, if there is no named slurpy
sub zero {}
<error descr="No such named parameter in signature">zero(none-allowed => 42)</error>;

sub a($a, $b) {
    $a + $b;
}

<error descr="Not enough positional arguments">a</error>;
<error descr="Not enough positional arguments">a(42)</error>;
a(42, 42);
<error descr="Too many positional arguments">a(24, 24, 24)</error>;
<error descr="No such named parameter in signature">a(42, 42, test => 555)</error>;

sub b($a, *%h) { $a; %h }

b(42);
<error descr="Too many positional arguments">b(42, 42)</error>;
b(42, a => 42, b => 42);

sub c($a, $b, *@p) { $a; $b; @p;}

<error descr="Not enough positional arguments">c(42)</error>;
c(42, 42);
c(42, 42, 42, 42, 42);
<error descr="No such named parameter in signature">c(42, 42, a => 42)</error>;

sub d($a, $b, *@p, *%h) { $a, $b, @p, %h; }

<error descr="Not enough positional arguments">d(42)</error>;
d(42, 42);
d(42, 42, 42, 42);
d(42, 42, 42, 42, a => 42, b => 42);

# If it's a sub, check that all required named arguments are passed
class A {
    method a($a) {
        self.b(42, b => $a);
    }
    method m($b, $c) {
        $b + $c;
    }
    method b($int, :$a, :$b!) {
        if $int + $a + $b == 42 {
            return;
        } else {
            self.d(not-used => 'value');
            self.a(3, not-used => 'value') + self<error descr="Not enough positional arguments">.m(42)</error>;
        }
    }
    method d {}
}

sub e($a?, :$b!) {
    say $a; $b;
}

<error descr="This call misses a required named argument: $b">e()</error>;
e(b => 42);
<error descr="This call misses a required named argument: $b">e(555)</error>;
<error descr="No such named parameter in signature">e(555, b => 42, d => 42)</error>;
e(555, b => 42);
