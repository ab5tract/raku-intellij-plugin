#!/usr/bin/env raku
use v6.d;
use LibXML;
use LibXML::Document;

#| Print failed <testcase> entries from Gradle JUnit XML result files:
#| classname.name, then the failure message.
unit sub MAIN(
    Str :$dir = 'build/test-results/test',  #= directory of TEST-*.xml files
);

my @files = $dir.IO.dir.grep(*.basename.ends-with('.xml'));
die "No result XML files found under $dir" unless @files;

my $total-failed = 0;
for @files.sort(*.basename) -> $file {
    my LibXML::Document $doc = LibXML.parse: file => $file.Str;
    for $doc.findnodes('//testcase') -> $testcase {
        my $failure = $testcase.findnodes('./failure').first;
        next unless $failure;
        $total-failed++;
        my $classname = $testcase.getAttribute('classname');
        my $name = $testcase.getAttribute('name');
        my $message = $failure.getAttribute('message') // $failure.textContent.lines.first // '';
        say "$classname.$name";
        say "    $message.trim()";
    }
}
say "\n$total-failed failure(s) across {@files.elems} result file(s)." if $total-failed;
say "No failures." unless $total-failed;
