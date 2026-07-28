#!/usr/bin/env raku
use v6;

sub find-files(IO::Path $dir) {
    gather for $dir.dir -> $path {
        if $path.d {
            take slip find-files($path);
        } elsif $path.f && $path.basename ~~ /\.(java|kt)$/ {
            take $path;
        }
    }
}

my @files = find-files('src/main/java'.IO);

my %by-basename;
for @files -> $path {
    my $name = $path.basename;
    my $base = $name.subst(/\.<-[.]>+$/, '');
    %by-basename{$base}.push($name);
}

my @dups = %by-basename.pairs.grep({ .value.elems > 1 });

my @sorted = @dups.sort(-> $a, $b {
    $b.value.elems <=> $a.value.elems || $a.key cmp $b.key
});

say "{@sorted.elems} duplicated";
for @sorted -> $pair {
    say "{$pair.value.elems}\t{$pair.key}";
}
