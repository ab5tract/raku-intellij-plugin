my @names = 'src/test/kotlin/org/raku/comma/highlighting/RakuColorSettingsPageTest.kt'.IO
    .slurp.match(/ '"' (RAKU_\w+) '"' /, :g).map({ .[0].Str }).unique.sort;
say "{@names.elems} names";
.say for @names;
