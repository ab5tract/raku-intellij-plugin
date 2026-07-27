my class Date {

#?if !js
    my constant $valid-units = nqp::hash(
#?endif
#?if js
    my $valid-units := nqp::hash(
#?endif
      'day',    1,
      'days',   1,
    );

    method !wrong-oor(int $year, int $month, int $day) {
        1 <= $month <= 12
    }
}
