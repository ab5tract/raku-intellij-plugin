my %counts = package => 0, comment => 0, blank => 0, other => 0;

sub classify($line) {
    return 'blank'   if !$line.defined || $line ~~ /^ \s* $/;
    return 'package' if $line.starts-with('package ');
    return 'comment' if $line.starts-with('//') || $line.starts-with('/*');
    return 'other';
}

for 'src/main/java'.IO.dir(:r).grep(*.ends-with('.java')) -> $file {
    %counts{ classify($file.lines[0]) }++;
}

for <package comment blank other> -> $kind {
    say "$kind\t%counts{$kind}";
}
