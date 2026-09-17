use JSON::Fast;
use Pod::To::Text;
use Documentable;
use Documentable::Registry;

die 'Cannot find documentation directory. Please, clone docs repo into the current working directory before running the script again. Exiting...' unless 'doc'.IO.e;

constant $PREFIXES = 'method '|'routine ';
constant $OP-PREFIXES = 'prefix '|'infix '|'postfix '|'circumfix '|'postcircumfix ';

class Pod::To::Comma {
    method render($pod, :$name, :$needs-prefix = False) {
        my %defs;
        my $content-current;
        my $prefix;
        my $active-prefix = $needs-prefix;

        # Iterate each page element
        for $pod.contents {
            when Pod::Heading {
                $active-prefix = False;
                # If it is a new header, finish the previous one
                with $content-current {
                    for $content-current<names> -> $name {
                        %defs{$name} = $content-current<docs>.join;
                    }
                    $content-current = Nil;
                }
                # when it is a definition we want to know about, init a new current
                with $_.contents[0].contents {
                    if so $_.Str.starts-with($PREFIXES) {
                        my $*FORMAT = False;
                        $content-current = %( names => pod2comma($_).words[1] );
                    } elsif so $_.Str.starts-with($OP-PREFIXES) {
                        my $header-str = pod2comma($_);
                        my $names = $header-str.words.elems > 2 ?? $header-str.split(',', :1limit)>>.trim !! $header-str;
                        $content-current = %( names => [ "&{$_.words[0]}:<{$_.words[1]}>" for $names ] );
                    }
                }
            }
            when Pod::Block::Named {}
            when Pod::Block::Para|Pod::Block::Code|Pod::Item|Pod::Block::Table {
                with $content-current -> $item {
                    $item<docs>.push: pod2comma($_);
                } elsif $active-prefix {
                    $prefix.push: pod2comma($_);
                }
            }
            when Pod::Block::Comment {}
            default {
                die "NYI for {$_.^name}";
            }
        }
        %( :$name, :%defs, %( $needs-prefix ?? :prefix($prefix.join) !! hash ) );
    }

    multi pod2comma(Pod::Block::Code $pod) {
        "<p><pre><code>{$pod.contents>>.&pod2comma.join}</code></pre></p>"
    }

    multi pod2comma(Pod::Block::Para $pod) {
        "<p>{$pod.contents.map({pod2comma($_)}).join}</p>";
    }

    multi pod2comma(Pod::Item $pod) {
        "* {pod2comma($pod.contents)}<br>";
    }

    multi pod2comma(Pod::Block::Table $pod) {
        my $ret = '<table class=\'sections\'>';
        $ret ~= "<tr>{$_.item.map({"<th>{$_.trans(['&', '<', '>'] => ['&amp;', '&lt;', '&gt;'])}</th>"}).join}</tr>\n" with $pod.headers;
        $ret ~= "<tr>{$_.map({"<td>{$_.trans(['&', '<', '>'] => ['&amp;', '&lt;', '&gt;'])}</tr>"}).join}</tr>\n" for $pod.contents;
        $ret ~ '</table>';
    }

    multi pod2comma(Positional $pod) {
        $pod.flat>>.&pod2comma.grep(?*).join;
    }

    multi pod2comma(Pod::FormattingCode $pod) {
        return $pod.contents>>.&pod2comma.join unless $*FORMAT;
        given $pod.type {
            when 'B' {
                "<b>{$pod.contents>>.&pod2comma.join}</b>";
            }
            when 'I' {
                "<i>{$pod.contents>>.&pod2comma.join}</i>";
            }
            when 'C' {
                "<code>{$pod.contents>>.&pod2comma.join}</code>";
            }
            when 'X'|'L'|'N' {
                $pod.contents>>.&pod2comma.join;
            }
            default {
                die "We do not support type $_";
            }
        }
    }

    multi pod2comma(Str $pod) {
        $pod.trans(['&', '<', '>'] => ['&amp;', '&lt;', '&gt;']);
    }
}

my $registry = Documentable::Registry.new(
        :!cache, topdir => 'doc/doc',
        dirs => ['Language', 'Type'], :verbose);

$registry.compose;

say "Processing docs...";

my %docs;
for $registry.documentables.kv -> $num, $doc {
    if $doc.kind eq Language && $doc.name eq 'Operators' {
        %docs<ops> := Pod::To::Comma.render($doc.pod, :!name)<defs>;
    } elsif $doc.kind eq Type {
        %docs<types>{$doc.name} = Pod::To::Comma.render($doc.pod, name => $doc.name, :needs-prefix);
    }
}

say "Creating json...";

spurt '../../resources/docs/core.json', to-json %docs, :!pretty;
say "Finished! Wrote results to $*CWD.child('../../resources/docs/core.json').resolve()";
