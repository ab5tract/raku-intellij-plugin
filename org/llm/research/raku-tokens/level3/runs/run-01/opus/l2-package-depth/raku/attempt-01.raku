sub walk(IO::Path $dir) {
    gather for $dir.dir -> $p {
        if $p.d {
            take walk($p).Slip;
        }
        elsif $p.Str.ends-with('.java') {
            take $p;
        }
    }
}

my %tally;
my $none = 0;

for walk('src/main/java'.IO) -> $file {
    my $depth;
    for $file.lines -> $line {
        if $line ~~ / ^ 'package ' (.*) ';' $ / {
            $depth = $0.trim.split('.').elems;
            last;
        }
    }
    with $depth {
        %tally{$_}++;
    }
    else {
        $none++;
    }
}

for %tally.keys.sort(+*) -> $d {
    say "$d\t%tally{$d}";
}
say "none\t$none";
