.say for 'build/test-results/test'.IO.dir(test => *.ends-with('.xml'))
    .map(*.slurp)
    .map({ .match(/ '[SKIPPED]' <-[<&]>+ /, :g).map(*.Str.trim) })
    .flat.unique.sort;
