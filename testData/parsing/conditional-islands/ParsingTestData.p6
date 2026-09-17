my $shared = 1;
#?if moar
my $backend = "moar";
#?endif
#?if jvm
my $backend = "jvm";
#?endif
say $shared;
