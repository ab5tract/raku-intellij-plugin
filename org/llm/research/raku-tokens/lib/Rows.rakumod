use v6.d;
use Corpus;

#| Shared reader for 90-corpus-per-file.tsv, so every consumer resolves
#| root-relative paths the same way and none of them re-derives which corpus
#| lives under which root.
unit module Rows;

our sub roots(IO::Path $script --> Hash) {
    my $repo = Corpus::repo-root($script);
    %(
        'python-stdlib'  => Corpus::python-stdlib(),
        'raku-ecosystem' => Corpus::raku-ecosystem(),
        'raku-repo'      => $repo,
        'kotlin-repo'    => $repo,
        'java-repo'      => $repo,
        'prose-markdown' => $repo,
    );
}

#| Each row gains `file`, the absolute path on *this* machine, which is
#| IO::Path if the corpus root does not resolve here.
our sub read(IO::Path $tsv, IO::Path $script --> Seq) {
    my %root = roots($script);
    $tsv.lines.skip(1).map: -> $line {
        my @f = $line.split("\t");
        my $r = %root{@f[0]};
        %(
            corpus   => @f[0],
            language => @f[1],
            path     => @f[2],
            bytes    => +@f[3],
            chars    => +@f[4],
            lines    => +@f[5],
            tokens   => +@f[6],
            digest   => @f[7],
            file     => $r ?? $r.add(@f[2]) !! IO::Path,
        );
    }
}
