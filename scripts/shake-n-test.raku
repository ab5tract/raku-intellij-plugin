#!/usr/bin/env raku
use v6.*;

#| Randomized spot-check runner for the plugin's test suite. Enumerates every
#| test class under src/test/kotlin (files named *Test.kt, whose class name
#| matches the filename -- verified to hold across the whole tree), takes a
#| random sample of a given size, and runs just that sample through Gradle.
#| Meant for fast, continuous sampling while iterating; run with
#| --percent=1.0 (or --all) for an honest full-suite pass at a checkpoint.
#| Pass -t/--test one or more times to always include specific classes on top
#| of the random sample (e.g. -t DocumentationTest -t GoToDeclarationTest);
#| combine with --percent=0 to run *only* the classes you named.
unit sub MAIN(
    Rat() :$percent = 0.15,     #= fraction of test classes to sample (0.0-1.0)
    Bool  :$all = False,        #= shorthand for --percent=1.0
    Int   :$seed,               #= fix the RNG seed to reproduce a prior sample
    Bool  :$dry-run = False,    #= print the picked classes and gradle command, don't run it
    Str   :$root = 'src/test/kotlin',
    :t(:@test),                 #= class name(s) to always include; repeat -t/--test to add more
);

srand($seed) if $seed.defined;

my $effective-percent = $all ?? 1.0 !! $percent;
die "percent must be in [0.0, 1.0]" unless 0 <= $effective-percent <= 1.0;

my @tests = find-test-classes($root);
die "No test classes found under $root" unless @tests;

my @explicit = @test.map({ resolve-test-name($_, @tests) }).flat;

my $n = $effective-percent == 0 ?? 0 !! (ceiling(@tests.elems * $effective-percent).Int max 1);
my @to-run = (@tests.pick($n) (|) @explicit).keys.sort;

die "Nothing to run: --percent=0 and no -t/--test given" unless @to-run;

say "Picked {@to-run.elems} of {@tests.elems} test classes ({(100 * $effective-percent).round(0.01)}%"
    ~ (@explicit ?? ", plus {@explicit.elems} explicit" !! "") ~ ")"
    ~ ($seed.defined ?? " [seed=$seed]" !! " [seed=unset -- pass --seed=N to reproduce this run]");
.say for @to-run;

my $cmd = build-gradle-command(@to-run);
say "\n$cmd";

if $dry-run {
    say "(dry run, not executing)";
    exit 0;
}

my $proc = shell($cmd);
exit $proc.exitcode;

#| Walk $root for files named *Test.kt and turn each into a fully-qualified
#| class name from its `package` line + filename. Verified against this
#| project's test tree: the class name always matches the filename, and
#| there are no abstract classes among the *Test.kt files, so no further
#| filtering (e.g. requiring a `fun test` method -- many legitimate classes
#| have none of their own, inheriting parameterized tests from a shared base
#| like RakuParsingTestCase) is needed or correct.
sub find-test-classes(Str $root --> Array) {
    my @classes;
    for find-kt-test-files($root.IO) -> $file {
        my $package = $file.lines.first(*.starts-with('package '));
        next unless $package;
        my $package-name = $package.substr(8).trim.subst(/';'$/, '');
        my $class-name = $file.basename.subst('.kt', '');
        @classes.push("$package-name.$class-name");
    }
    return @classes;
}

#| Turn a user-given -t/--test value into one or more fully-qualified class
#| names. Accepts an exact FQCN, a bare class name (matched against the
#| basename of every entry in @tests -- if more than one class shares that
#| basename, e.g. same-named classes in different packages, all of them are
#| returned), or -- if nothing matches -- passes the value through unchanged,
#| on the assumption it's a deliberate gradle --tests pattern (glob, package
#| prefix, etc.) rather than a typo.
sub resolve-test-name(Str $given, @tests --> List) {
    return ($given,) if $given eq any(@tests);
    my @matches = @tests.grep({ .split('.').tail eq $given });
    if @matches {
        note "note: '$given' matches {@matches.elems} classes: {@matches.join(', ')}" if @matches > 1;
        return @matches.List;
    }
    note "warning: '$given' didn't match any known test class by name; passing it to Gradle as-is";
    return ($given,);
}

sub find-kt-test-files(IO::Path $dir --> Seq) {
    gather for $dir.dir -> $entry {
        if $entry.d {
            take $_ for find-kt-test-files($entry);
        } elsif $entry.f && $entry.basename.ends-with('Test.kt') {
            take $entry;
        }
    }
}

sub build-gradle-command(@classes --> Str) {
    my $tests-args = @classes.map({ "--tests '$_'" }).join(' ');
    my $rakubrew-init = %*ENV<SHELL>.contains('zsh')
        ?? 'eval "$(~/.rakubrew/bin/rakubrew init Zsh)"'
        !! 'eval "$(~/.rakubrew/bin/rakubrew init Bash)"';
    return "$rakubrew-init && ./gradlew test $tests-args";
}
