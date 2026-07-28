for <RakuDefault RakuDarcula> -> $scheme {
    my $xml = "src/main/resources/colorSchemes/$scheme.xml".IO.slurp;
    my @keys = $xml.match(/ '<option name="' (RAKU_\w+) '">' (.*?) '</option>' /, :g)
                   .grep({ .[1].Str.contains('EFFECT_COLOR') })
                   .map({ .[0].Str });
    say "$scheme: {@keys.sort.join(', ') || '(none)'}";
}
