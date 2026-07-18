sub foobar($one,
           $two,
           $three) {
    my $x = 0;
    my $y = 0;
    my $t = '';
    A: while $x++ < 2 {
	   $t ~= "A$x";
	   B: while $y++ < 2 {
		  $t ~= "B$y";
		  redo A if $y++ ==
				1;
		  last A
	   }
    }
    say $t;

    # OUTPUT: «A1B1A1A2»
}
