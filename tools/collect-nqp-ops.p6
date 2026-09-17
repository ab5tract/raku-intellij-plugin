#!/usr/bin/env perl6

use JSON::Fast;

# Run with markdown file describing nqp ops from nqp repo

sub MAIN($file where *.IO.e = 'ops.markdown') {
    my $ops = slurp $file;
    my @matches = $ops ~~ m:g/ ^^ '* `' (\N+?) '(' (\N+?) ')' '`' \n /;
    my @json;
    for @matches -> $match {
        my $name =  "nqp::$match[0].Str()";
        my $r = ($match[1] ~~ /'--> ' \N+ /) // 'Mu';
        my $signature = $match[1].Str.subst(/'-->' .*/, '');
        my $params = $signature.split(',');
        my %signature = r => $r.Str, p => $params;
        @json.push: %( :k('r'), n => $name , s => %signature, m => 0 );
    }
    say to-json(@json, :!pretty);
}
