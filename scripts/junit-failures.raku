#!/usr/bin/env raku
use v6.d;
use LibXML;
use LibXML::Document;

#| Print failed <testcase> entries from Gradle JUnit XML result files:
#| classname.name, then the failure message.
unit sub MAIN(
    Str :$dir = 'build/test-results/test',  #= directory of TEST-*.xml files
    Int :$max-chars = 300,                  #= truncate each message to this many characters
);

my @files = $dir.IO.dir.grep(*.basename.ends-with('.xml'));
die "No result XML files found under $dir" unless @files;

my $total-failed = 0;
for @files.sort(*.basename) -> $file {
    # :huge -- some failures (e.g. the leak-detector flake) embed enormous
    # stack traces that trip LibXML's default hardcoded size limits.
    my LibXML::Document $doc = LibXML.parse: file => $file.Str, :huge;
    for $doc.findnodes('//testcase') -> $testcase {
        my $failure = $testcase.findnodes('./failure').first;
        next unless $failure;
        $total-failed++;
        my $classname = $testcase.getAttribute('classname');
        my $name = $testcase.getAttribute('name');
        my $message = ($failure.getAttribute('message') // $failure.textContent.lines.first // '').trim;
        # Some failures (e.g. the leak-detector flake) embed a whole stack
        # trace directly in the message attribute -- truncate for readability.
        $message = $message.substr(0, $max-chars) ~ "… ({$message.chars} chars total, use --max-chars to see more)"
            if $message.chars > $max-chars;
        say "$classname.$name";
        say "    $message";
    }
}
say "\n$total-failed failure(s) across {@files.elems} result file(s)." if $total-failed;
say "No failures." unless $total-failed;
