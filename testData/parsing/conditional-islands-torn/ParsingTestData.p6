#?if !js
my $valid-units = foo(
#?endif
#?if js
my $valid-units = bar(
#?endif
  'day', 1,
);
say "after";
