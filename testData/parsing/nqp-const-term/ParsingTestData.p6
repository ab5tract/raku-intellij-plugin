use nqp;
my $rev = "v6";
my @parts;
if nqp::objprimspec($rev) == nqp::const::BIND_VAL_STR
  && $rev ~~ /^ v? $<vnum>=\d+ $<plus>='+'? $/ -> $m {
    @parts[0] := "x" ~ $m<plus>;
}
say nqp::const::BIND_VAL_STR && 42;
my $x = nqp::const::STAT_FILESIZE & 0xFF;
999999;
