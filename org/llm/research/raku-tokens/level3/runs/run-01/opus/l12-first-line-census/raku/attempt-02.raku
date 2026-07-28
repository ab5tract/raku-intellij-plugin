my %counts = package => 0, comment => 0, blank => 0, other => 0;

sub java-files($dir) {
    gather for $dir.dir -> $path {
        if $path.d {
            take slip java-files($path);
        }
        elsif $path.basename.ends-with('.java') {
            take $path;
        }
    }
}

sub classify($line) {
    return 'blank'   if !$line.defined || $line.trim eq '';
    return 'package' if $line.starts-with('package ');
    return 'comment' if $line.starts-with('//') || $line.starts-with('/*');
    return 'other';
}

for java-files('src/main/java'.IO) -> $file {
    %counts{ classify($file.lines[0]) }++;
}

for <package comment blank other> -> $kind {
    say "$kind\t%counts{$kind}";
}
