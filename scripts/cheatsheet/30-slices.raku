#!/usr/bin/env raku
use v6.d;

#| STAGE 30 -- carve the setting sources into agent-sized slices.
#|
#| The forwarding and strictness facts cannot be introspected: they live in method
#| *bodies* (`self.match($matcher, |%options)`, `X::Adverb.new(...).throw`). Reading
#| those is the one part of this pipeline that needs an agent rather than a parser.
#|
#| This stage exists so that fan-out is reproducible rather than improvised: it emits
#| a stable manifest of slice -> files, so a re-run on a new Rakudo assigns the same
#| work in the same shape, and a slice that failed can be re-run alone.
#|
#| Files are ordered so related types land together (Str beside Cool, all of IO/,
#| the Any-* family), because a forwarding edge is usually resolved by reading the
#| target in the same slice.
#|
#| Writes scripts/cache/slices.tsv.

my $repo  = $*PROGRAM.parent.parent.parent;
my $cache = $repo.add('scripts/cache');
my $src   = (%*ENV<RAKUDO_SRC> // $*HOME.add('code/raku/x.core/rakudo').Str).IO;
die "no Rakudo checkout at $src" unless $src.add('src/core.c').d;

constant SLICES = 14;

sub dir-r(IO::Path $d) {
    gather for $d.dir -> $p { $p.d ?? (take $_ for dir-r($p)) !! take $p }
}

my @files = <core.c core.d core.e>
    .map({ $src.add("src/$_") }).grep(*.d)
    .map({ dir-r($_) }).flat
    .grep({ .f && .Str.ends-with('.rakumod') })
    .map({ .relative($src) })
    .sort;

die "expected ~268 setting files, found {@files.elems}" unless 200 < @files < 400;

# Group by directory first, then by name. Sorting the relative paths already does
# this: core.c/IO/* stay adjacent, as do the Any-*/List/Str cluster.
my $per = ceiling(@files / SLICES);

$cache.mkdir;
my $out = $cache.add('slices.tsv').open(:w);
$out.say: join "\t", <slice file>;

my $n = 0;
for @files.rotor($per, :partial).kv -> $i, @chunk {
    my $slice = sprintf('s%02d', $i + 1);
    for @chunk -> $f {
        $out.say: join "\t", $slice, $f;
        $n++;
    }
}
$out.close;

note "wrote $n file assignments across {ceiling(@files / $per)} slices -> scripts/cache/slices.tsv";
for $cache.add('slices.tsv').lines.skip(1).map(*.split("\t")[0]).squish -> $s {
    my @f = $cache.add('slices.tsv').lines.skip(1).grep(*.starts-with("$s\t")).map(*.split("\t")[1]);
    note sprintf("  %s  %2d files  %s .. %s", $s, @f.elems, @f.head.subst('core.c/',''), @f.tail.subst('core.c/',''));
}
