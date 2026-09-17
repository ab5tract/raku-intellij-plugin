use Test;

plan *;

constant $HARNESS-SCRIPT = '../../resources/testing/perl6-test-harness.p6';

# duplicated args here is a horrible workaround,
# as older rakudo doesn't understand a single argument being matched as Positional
my $proc = run $*EXECUTABLE, $HARNESS-SCRIPT,
        '--paths=t', '--paths=t',
        '--cwd=.', '--cwd=.',
        '--args=-Ilib -Ilib', '--args=-Ilib -Ilib',
        :out;
my $output = $proc.out.slurp(:close);
like $output, / 'ok 1 - a' /;
like $output, / 'ok 1 - b' /;
like $output, / 'ok 1 - Nested'/;

done-testing;
