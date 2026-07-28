my @files;
my @todo = 'src/main/java'.IO;
while @todo {
    my $path = @todo.pop;
    if $path.d {
        @todo.append($path.dir);
    }
    elsif $path.basename.ends-with('.kt') {
        @files.push($path);
    }
}

my $lines = 0;
my $nonblank = 0;

for @files -> $file {
    my $text = $file.slurp;
    my @l = $text.split("\n");
    @l.pop if $text.ends-with("\n");
    $lines += @l.elems;
    $nonblank += @l.grep({ .trim ne '' }).elems;
}

my $hundredths = floor($nonblank / @files.elems * 100 + 1/2);

say "files\t{@files.elems}";
say "lines\t$lines";
say "nonblank\t$nonblank";
say "mean\t{$hundredths div 100}.{sprintf '%02d', $hundredths mod 100}";
