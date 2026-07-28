my $dir = "src/main/resources/META-INF".IO;

my %seen;
for $dir.dir(test => *.ends-with(".xml")).grep(*.f) -> $file {
    for $file.slurp.match(/ 'implementation="' (<-["]>*) '"' /, :g) -> $m {
        %seen{$m[0].Str} = True;
    }
}

my @values = %seen.keys.sort;

say "{+@values} distinct";
for @values -> $v {
    my $simple = $v.contains(".") ?? $v.substr($v.rindex(".") + 1) !! $v;
    say "$simple\t$v";
}
