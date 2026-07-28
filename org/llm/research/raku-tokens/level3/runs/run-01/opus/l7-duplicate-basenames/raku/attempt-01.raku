sub walk(IO::Path $dir) {
    gather for $dir.dir -> $p {
        if $p.d {
            take $_ for walk($p);
        }
        else {
            take $p;
        }
    }
}

my %count;
for walk("src/main/java".IO) -> $p {
    my $name = $p.basename;
    next unless $name.ends-with(".java") || $name.ends-with(".kt");
    %count{$name.substr(0, $name.rindex("."))}++;
}

my @dups = %count.grep(*.value > 1).sort({ -.value, .key });

say "{+@dups} duplicated";
for @dups -> $d {
    say "$d.value()\t$d.key()";
}
