sub walk($d) { for $d.dir -> $p { $p.d ?? walk($p) !! take $p } }

my @edges = 0, 1_000, 4_000, 16_000;
my %count;
for gather walk('src/main/java'.IO) -> $f {
    %count{ @edges.first(* <= $f.s, :k, :end) }++ if $f.Str.ends-with('.kt');
}
for ^@edges -> $i {
    say ($i == @edges.end ?? "@edges[$i]+" !! "@edges[$i]..@edges[$i+1]")
        ~ "\t" ~ (%count{$i} // 0);
}
