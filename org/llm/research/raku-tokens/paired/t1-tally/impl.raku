my $total = 0;
for 'build/test-results/test'.IO.dir(test => *.ends-with('.xml')) -> $p {
    $total += +$0 if $p.slurp.substr(0, 600) ~~ / 'tests="' (\d+) '"' /;
}
say "$total tests";
