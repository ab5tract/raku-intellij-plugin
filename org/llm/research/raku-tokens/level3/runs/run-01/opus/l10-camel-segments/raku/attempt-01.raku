sub walk(IO::Path $dir) {
    for $dir.dir -> $p {
        if $p.d {
            walk($p);
        }
        elsif $p.basename.ends-with('.java') {
            take $p;
        }
    }
}

sub split-camel(Str $name) {
    my @chars = $name.comb;
    my @segments;
    my $start = 0;
    for 1 ..^ @chars.elems -> $i {
        my $prev = @chars[$i - 1];
        my $cur  = @chars[$i];
        my $next = $i + 1 < @chars.elems ?? @chars[$i + 1] !! '';
        my $break = ($prev ~~ /^<[a..z0..9]>$/ && $cur ~~ /^<[A..Z]>$/)
                 || ($prev ~~ /^<[A..Z]>$/ && $cur ~~ /^<[A..Z]>$/ && $next ~~ /^<[a..z]>$/);
        if $break {
            @segments.push: @chars[$start ..^ $i].join;
            $start = $i;
        }
    }
    @segments.push: @chars[$start ..^ @chars.elems].join;
    @segments;
}

my %counts;
for gather walk('src/main/java'.IO) -> $file {
    my $base = $file.basename.substr(0, *-5);
    %counts{$_}++ for split-camel($base).map(*.lc);
}

for %counts.sort({ (-.value, .key) }).head(15) -> $pair {
    say $pair.value ~ "\t" ~ $pair.key;
}
