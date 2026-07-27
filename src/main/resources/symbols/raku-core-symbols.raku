use nqp;

# ========== JSON CODE FROM JSON::Fast ==========

multi sub to-surrogate-pair(Int $ord) {
    my int $base   = $ord - 0x10000;
    my int $top    = $base +& 0b1_1111_1111_1100_0000_0000 +> 10;
    my int $bottom = $base +&               0b11_1111_1111;
    Q/\u/ ~ (0xD800 + $top).base(16) ~ Q/\u/ ~ (0xDC00 + $bottom).base(16);
}

multi sub to-surrogate-pair(Str $input) {
    to-surrogate-pair(nqp::ordat($input, 0));
}

my $tab := nqp::list_i(92,116); # \t
my $lf  := nqp::list_i(92,110); # \n
my $cr  := nqp::list_i(92,114); # \r
my $qq  := nqp::list_i(92, 34); # \"
my $bs := nqp::list_i(92, 92); # \\

my sub str-escape(\text) {
    my $codes := text.NFD;
    my int $i = -1;

    nqp::while(
      nqp::islt_i(++$i,nqp::elems($codes)),
      nqp::if(
        nqp::isle_i((my int $code = nqp::atpos_i($codes,$i)),92)
          || nqp::isge_i($code,128),
        nqp::if(                                           # not ascii
          nqp::isle_i($code,31),
          nqp::if(                                          # control
            nqp::iseq_i($code,10),
            nqp::splice($codes,$lf,$i++,1),                  # \n
            nqp::if(
              nqp::iseq_i($code,13),
              nqp::splice($codes,$cr,$i++,1),                 # \r
              nqp::if(
                nqp::iseq_i($code,9),
                nqp::splice($codes,$tab,$i++,1),               # \t
                nqp::stmts(                                    # other control
                  nqp::splice($codes,$code.fmt(Q/\u%04x/).NFD,$i,1),
                  ($i = nqp::add_i($i,5))
                )
              )
            )
          ),
          nqp::if(                                          # not control
            nqp::iseq_i($code,34),
            nqp::splice($codes,$qq,$i++,1),                  # "
            nqp::if(
              nqp::iseq_i($code,92),
              nqp::splice($codes,$bs,$i++,1),                 # \
              nqp::if(
                nqp::isge_i($code,0x10000),
                nqp::stmts(                                    # surrogates
                  nqp::splice(
                    $codes,
                    (my $surrogate := to-surrogate-pair($code.chr).NFD),
                    $i,
                    1
                  ),
                  ($i = nqp::sub_i(nqp::add_i($i,nqp::elems($surrogate)),1))
                )
              )
            )
          )
        )
      )
    );

    nqp::strfromcodes($codes)
}

my sub to-json(
  \obj,
  Int  :$level          = 0,
  int  :$spacing        = 2,
  Bool :$sorted-keys    = False,
  Bool :$enums-as-value = False,
) {

    my str @out;
    my str $spaces = ' ' x $spacing;
    my str $comma  = ",\n" ~ $spaces x $level;

#-- helper subs from here, with visibility to the above lexicals
    sub unpretty-positional(\positional --> Nil) {
        nqp::push_s(@out,'[');
        my int $before = nqp::elems(@out);
        for positional.list {
            jsonify($_);
            nqp::push_s(@out,",");
        }
        nqp::pop_s(@out) if nqp::elems(@out) > $before;  # lose last comma
        nqp::push_s(@out,']');
    }

    sub unpretty-associative(\associative --> Nil) {
        nqp::push_s(@out,'{');
        my \pairs := $sorted-keys
          ?? associative.sort(*.key)
          !! associative.list;

        my int $before = nqp::elems(@out);
        for pairs {
            jsonify(.key);
            nqp::push_s(@out,":");
            jsonify(.value);
            nqp::push_s(@out,",");
        }
        nqp::pop_s(@out) if nqp::elems(@out) > $before;  # lose last comma
        nqp::push_s(@out,'}');
    }

    sub jsonify(\obj --> Nil) {

        with obj {

            # basic ones
            if nqp::istype($_, Bool) {
                nqp::push_s(@out,obj ?? "true" !! "false");
            }
            elsif nqp::istype($_, IntStr) {
                jsonify(.Int);
            }
            elsif nqp::istype($_, RatStr) {
                jsonify(.Rat);
            }
            elsif nqp::istype($_, NumStr) {
                jsonify(.Num);
            }
            elsif nqp::istype($_, Enumeration) {
                if $enums-as-value {
                    jsonify(.value);
                }
                else {
                    nqp::push_s(@out,'"');
                    nqp::push_s(@out,str-escape(.key));
                    nqp::push_s(@out,'"');
                }
            }
            # Str and Int go below Enumeration, because there
            # are both Str-typed enums and Int-typed enums
            elsif nqp::istype($_, Str) {
                nqp::push_s(@out,'"');
                nqp::push_s(@out,str-escape($_));
                nqp::push_s(@out,'"');
            }

            # numeric ones
            elsif nqp::istype($_, Int) {
                nqp::push_s(@out,.Str);
            }
            elsif nqp::istype($_, Rat) {
                nqp::push_s(@out,.contains(".") ?? $_ !! "$_.0")
                  given .Str;
            }
            elsif nqp::istype($_, FatRat) {
                nqp::push_s(@out,.contains(".") ?? $_ !! "$_.0")
                  given .Str;
            }
            elsif nqp::istype($_, Num) {
                if nqp::isnanorinf($_) {
                    nqp::push_s(
                      @out,
                      $*JSON_NAN_INF_SUPPORT ?? obj.Str !! "null"
                    );
                }
                else {
                    nqp::push_s(@out,.contains("e") ?? $_ !! $_ ~ "e0")
                      given .Str;
                }
            }

            # iterating ones
            elsif nqp::istype($_, Seq) {
                jsonify(.cache);
            }
            elsif nqp::istype($_, Positional) {
                  unpretty-positional($_);
            }
            elsif nqp::istype($_, Associative) {
                  unpretty-associative($_);
            }

            # rarer ones
            elsif nqp::istype($_, Dateish) {
                nqp::push_s(@out,qq/"$_"/);
            }
            elsif nqp::istype($_, Instant) {
                nqp::push_s(@out,qq/"{.DateTime}"/);
            }
            elsif nqp::istype($_, Version) {
                jsonify(.Str);
            }

            # huh, what?
            else {
                die "Don't know how to jsonify {.^name}";
            }
        }
        else {
            nqp::push_s(@out,'null');
        }
    }

#-- do the actual work

    jsonify(obj);
    nqp::join("",@out)
}

my $ws := nqp::list_i;
nqp::bindpos_i($ws,$_ + 1,1) for 9,10,13,32;  # allow for -1 as value
my sub nom-ws(str $text, int $pos is rw --> Nil) {
    nqp::while(
            nqp::atpos_i($ws,nqp::ordat($text,$pos) + 1),
            $pos = $pos + 1
            );
    die "reached end of string when looking for something"
            if $pos == nqp::chars($text);
}

my sub tear-off-combiners(\text, \pos) {
    text.substr(pos,1).NFD.skip.map( {
        $^ord > 0x10000
                ?? to-surrogate-pair($^ord)
                !! $^ord.chr
    } ).join
}

my Mu $hexdigits := nqp::hash(
        '97', 1, '98', 1, '99', 1, '100', 1, '101', 1, '102', 1,
        '48', 1, '49', 1, '50', 1, '51', 1, '52', 1, '53', 1, '54', 1, '55', 1, '56', 1, '57', 1,
        '65', 1, '66', 1, '67', 1, '68', 1, '69', 1, '70', 1);

my Mu $escapees := nqp::hash(
        "34", '"', "47", "/", "92", "\\", "98", "\b", "102", "\f", "110", "\n", "114", "\r", "116", "\t");

my sub parse-string(str $text, int $pos is rw) {
    # first we gallop until the end of the string
    my int $startpos = $pos;
    my int $endpos;
    my int $textlength = nqp::chars($text);

    my int $ord;
    my int $has_hexcodes;
    my int $has_treacherous;
    my str $startcombiner = "";
    my Mu $treacherous;
    my Mu $escape_counts := nqp::hash();

    unless nqp::eqat($text, '"', $startpos - 1) {
        $startcombiner = tear-off-combiners($text, $startpos - 1);
    }

    loop {
        $ord = nqp::ordat($text, $pos);
        $pos = $pos + 1;

        if $pos > $textlength {
            die "unexpected end of document in string";
        }

        if nqp::eqat($text, '"', $pos - 1) {
            $endpos = $pos - 1;
            last;
        } elsif $ord == 92 {
            if     nqp::eqat($text, '"', $pos) or nqp::eqat($text, '\\', $pos) or nqp::eqat($text, 'b', $pos)
            or nqp::eqat($text, 'f', $pos) or nqp::eqat($text,  'n', $pos) or nqp::eqat($text, 'r', $pos)
            or nqp::eqat($text, 't', $pos) or nqp::eqat($text,  '/', $pos) {
                my str $character = nqp::substr($text, $pos, 1);
                if nqp::existskey($escape_counts, $character) {
                    nqp::bindkey($escape_counts, $character, nqp::atkey($escape_counts, $character) + 1);
                } else {
                    nqp::bindkey($escape_counts, $character, 1);
                }
                $pos = $pos + 1;
            } elsif nqp::eqat($text, 'u', $pos) {
                loop {
                    die "unexpected end of document; was looking for four hexdigits." if $textlength - $pos < 5;
                    if      nqp::existskey($hexdigits, nqp::ordat($text, $pos + 1))
                    and nqp::existskey($hexdigits, nqp::ordat($text, $pos + 2))
                    and nqp::existskey($hexdigits, nqp::ordat($text, $pos + 3))
                    and nqp::existskey($hexdigits, nqp::ordat($text, $pos + 4)) {
                        $pos = $pos + 4;
                    } else {
                        die "expected hexadecimals after \\u, but got \"{ nqp::substr($text, $pos - 1, 6) }\" at $pos";
                    }
                    $pos++;
                    if nqp::eqat($text, '\u', $pos) {
                        $pos++;
                    } else {
                        last
                                }
                }
                $has_hexcodes++;
            } elsif nqp::existskey($escapees, nqp::ordat($text, $pos)) {
                # treacherous!
                $has_treacherous++;
                $treacherous := nqp::hash() unless $treacherous;
                my int $treach_ord = nqp::ordat($text, $pos);
                if nqp::existskey($treacherous, $treach_ord) {
                    nqp::bindkey($treacherous, $treach_ord, nqp::atkey($treacherous, $treach_ord) + 1)
                } else {
                    nqp::bindkey($treacherous, $treach_ord, 1)
                }
                $pos++;
            } else {
                die "don't understand escape sequence '\\{ nqp::substr($text, $pos, 1) }' at $pos";
            }
        } elsif $ord == 9 or $ord == 10 {
            die "this kind of whitespace is not allowed in a string: { nqp::substr($text, $pos - 1, 1).raku } at {$pos - 1}";
        }
    }

    $pos = $pos + 1;

    my str $raw = nqp::substr($text, $startpos, $endpos - $startpos);
    if $startcombiner {
        $raw = $startcombiner ~ $raw
    }
    if not $has_treacherous and not $has_hexcodes and $escape_counts {
        my str @a;
        my str @b;
        if nqp::existskey($escape_counts, "n") and nqp::existskey($escape_counts, "r") {
            @a.push("\\r\\n"); @b.push("\r\n");
        }
        if nqp::existskey($escape_counts, "n") {
            @a.push("\\n"); @b.push("\n");
        }
        if nqp::existskey($escape_counts, "r") {
            @a.push("\\r"); @b.push("\r");
        }
        if nqp::existskey($escape_counts, "t") {
            @a.push("\\t"); @b.push("\t");
        }
        if nqp::existskey($escape_counts, '"') {
            @a.push('\\"'); @b.push('"');
        }
        if nqp::existskey($escape_counts, "/") {
            @a.push("\\/"); @b.push("/");
        }
        if nqp::existskey($escape_counts, "\\") {
            @a.push("\\\\"); @b.push("\\");
        }
        $raw .= trans(@a => @b) if @a;
    } elsif $has_hexcodes or nqp::elems($escape_counts) {
        $raw = $raw.subst(/ \\ (<-[uU]>) || [\\ (<[uU]>) (<[a..f 0..9 A..F]> ** 3)]+ %(<[a..f 0..9 A..F]>) (:m <[a..f 0..9 A..F]>) /,
                -> $/ {
                    if $0.elems > 1 || $0.Str eq "u" || $0.Str eq "U" {
                        my str @caps = $/.caps>>.value>>.Str;
                        my $result = $/;
                        my str $endpiece = "";
                        if (my $lastchar = nqp::chr(nqp::ord(@caps.tail))) ne @caps.tail {
                            $endpiece = tear-off-combiners(@caps.tail, 0);
                            @caps.pop;
                            @caps.push($lastchar);
                        }
                        my int @hexes;
                        for @caps -> $u, $first, $second {
                            @hexes.push(:16($first ~ $second).self);
                        }

                        CATCH {
                            die "Couldn't decode hexadecimal unicode escape { $result.Str } at { $startpos + $result.from }";
                        }

                        utf16.new(@hexes).decode ~ $endpiece;
                    } else {
                        if nqp::existskey($escapees, nqp::ordat($0.Str, 0)) {
                            my str $replacement = nqp::atkey($escapees, nqp::ordat($0.Str, 0));
                            $replacement ~ tear-off-combiners($0.Str, 0);
                        } else {
                            die "stumbled over unexpected escape code \\{ chr(nqp::ordat($0.Str, 0)) } at { $startpos + $/.from }";
                        }
                    }
                }, :g);
    }

    $pos = $pos - 1;

    $raw;
}

my sub parse-numeric(str $text, int $pos is rw) {
    my int $startpos = $pos;

    $pos = $pos + 1 while nqp::iscclass(nqp::const::CCLASS_NUMERIC, $text, $pos);

    my $residual := nqp::substr($text, $pos, 1);

    if $residual eq '.' {
        $pos = $pos + 1;

        $pos = $pos + 1 while nqp::iscclass(nqp::const::CCLASS_NUMERIC, $text, $pos);

        $residual := nqp::substr($text, $pos, 1);
    }

    if $residual eq 'e' || $residual eq 'E' {
        $pos = $pos + 1;

        if nqp::eqat($text, '-', $pos) || nqp::eqat($text, '+', $pos) {
            $pos = $pos + 1;
        }

        $pos = $pos + 1 while nqp::iscclass(nqp::const::CCLASS_NUMERIC, $text, $pos);
    }

    +(my $result := nqp::substr($text, $startpos - 1, $pos - $startpos + 1)) // die "at $pos: invalid number token $result.raku()";
}

my sub parse-obj(str $text, int $pos is rw) {
    my %result;

    my $key;
    my $value;

    nom-ws($text, $pos);

    if nqp::eqat($text, '}', $pos) {
        $pos = $pos + 1;
        %();
    } else {
        my $thing;
        loop {
            $thing = Any;

            if $key.DEFINITE {
                $thing = parse-thing($text, $pos)
            } else {
                nom-ws($text, $pos);

                if nqp::ordat($text, $pos) == 34 { # "
                    $pos = $pos + 1;
                    $thing = parse-string($text, $pos)
                } else {
                    die "at end of string: expected a quoted string for an object key" if $pos == nqp::chars($text);
                    die "at $pos: json requires object keys to be strings";
                }
            }
            nom-ws($text, $pos);

            #my str $partitioner = nqp::substr($text, $pos, 1);

            if      nqp::eqat($text, ':', $pos) and   !($key.DEFINITE or      $value.DEFINITE) {
                $key = $thing;
            } elsif nqp::eqat($text, ',', $pos) and     $key.DEFINITE and not $value.DEFINITE {
                $value = $thing;

                %result{$key} = $value;

                $key   = Any;
                $value = Any;
            } elsif nqp::eqat($text, '}', $pos) and     $key.DEFINITE and not $value.DEFINITE {
                $value = $thing;

                %result{$key} = $value;
                $pos = $pos + 1;
                last;
            } else {
                die "at end of string: unexpected end of object." if $pos == nqp::chars($text);
                die "unexpected { nqp::substr($text, $pos, 1) } in an object at $pos";
            }

            $pos = $pos + 1;
        }

        %result;
    }
}

my sub parse-array(str $text, int $pos is rw) {
    my @result;

    nom-ws($text, $pos);

    if nqp::eqat($text, ']', $pos) {
        $pos = $pos + 1;
        [];
    } else {
        my $thing;
        my str $partitioner;
        loop {
            $thing = parse-thing($text, $pos);
            nom-ws($text, $pos);

            $partitioner = nqp::substr($text, $pos, 1);
            $pos = $pos + 1;

            if $partitioner eq ']' {
                @result.push: $thing;
                last;
            } elsif $partitioner eq "," {
                @result.push: $thing;
            } else {
                die "at $pos, unexpected $partitioner inside list of things in an array";
            }
        }
        @result;
    }
}

my sub parse-thing(str $text, int $pos is rw) {
    nom-ws($text, $pos);

    my str $initial = nqp::substr($text, $pos, 1);

    $pos = $pos + 1;

    if nqp::ord($initial) == 34 { # "
        parse-string($text, $pos);
    } elsif $initial eq '[' {
        parse-array($text, $pos);
    } elsif $initial eq '{' {
        parse-obj($text, $pos);
    } elsif nqp::iscclass(nqp::const::CCLASS_NUMERIC, $initial, 0) || $initial eq '-' {
        parse-numeric($text, $pos);
    } elsif $initial eq 'n' {
        if nqp::eqat($text, 'ull', $pos) {
            $pos += 3;
            Any;
        } else {
            die "at $pos: i was expecting a 'null' but there wasn't one: { nqp::substr($text, $pos - 1, 10) }"
        }
    } elsif $initial eq 't' {
        if nqp::eqat($text, 'rue', $pos) {
            $pos = $pos + 3;
            True
        } else {
            die "at $pos: expected 'true', found { $initial ~ nqp::substr($text, $pos, 3) } instead.";
        }
    } elsif $initial eq 'f' {
        if nqp::eqat($text, 'alse', $pos) {
            $pos = $pos + 4;
            False
        } else {
            die "at $pos: expected 'false', found { $initial ~ nqp::substr($text, $pos, 4) } instead.";
        }
    } else {
        my str $rest = nqp::substr($text, $pos - 1, 8).raku;
        die "at $pos: expected a json object, but got $initial (context: $rest)"
    }
}

my sub from-json(Str() $text) is export {
    my str $ntext = $text;
    my int $length = $text.chars;

    my int $pos = 0;

    my $result = parse-thing($text, $pos);

    my int $parsed-length = $pos;

    try nom-ws($text, $pos);

    if $pos != nqp::chars($text) {
        X::JSON::AdditionalContent.new(parsed => $result, :$parsed-length, rest-position => $pos).throw
    }

    $result;
}

# ========== END OF JSON CODE ==========

# Prepare documentation archive
my %CORE-DOCS = from-json @*ARGS[0].IO.slurp;

my @EXTERNAL_COMMA_ELEMS;

my $new-param-API = so Parameter.^can('suffix');

# Collect archetypes first
pack-package(@EXTERNAL_COMMA_ELEMS, 'EXPORTHOW', CORE::EXPORTHOW);

for CORE::.keys(:implementation-detail) -> $_ {
    # Ignore a few things.
    when 'EXPORTHOW'|'Rakudo' { }
    # Collect all top-level subs and EVAL.
    when /^"&"<:Ll>/ | '&EVAL' | '&EVALFILE' {
        @EXTERNAL_COMMA_ELEMS.push: pack-variable($_, CORE::{$_}, |%( :d($_) with %CORE-DOCS<ops>{$_} ) );
        if ($_.starts-with('&')) {
            my $name = .substr(1);
            @EXTERNAL_COMMA_ELEMS.push: pack-code($_, $_.multi ?? 1 !! 0, $name, :!is-method) for CORE::{$_}.candidates;
        }
    }
    # Collect all types.
    when /^<:L>/ {
        pack-package(@EXTERNAL_COMMA_ELEMS, $_, CORE::{$_});
    }
}

put to-json @EXTERNAL_COMMA_ELEMS;

sub pack-variable($name, \object, :$is-attribute = False, :$d) {
    %( k => "v", n => $name, t => $is-attribute ?? object.type.^name !! object.^name, %( :$d if $d ) );
}

sub pack-code(Mu $code, Int $multiness, Str $name?, :$docs, :$is-method) {
    my $s = $code.signature;
    my @params = $s ~~ Any ?? $s.params !! ();
    @params .= skip(1) if $is-method && @params.elems > 0;
    my @parameters = @params.map({
        $new-param-API
        ?? %( t => .type.^name,
           n => "{.prefix ?? .prefix !! .named ?? ':' !! ''}" ~
                   "{ .sigil eq '|' ?? .sigil ~ .name !! (.name ?? .name !! .sigil) }" ~
                   "{ .default ?? '?' !! '' }{.suffix}"
        )
        !! %( t => .type.^name, n => .gist )
    }).List;
    my %signature = r => $s ~~ Any ?? $s.returns.^name !! 'Mu', p => @parameters;
    my $kind = $code.^name.comb.head.lc;
    my $deprecation = try { ~$code.DEPRECATED };
    my $pure = so try { so $code.?is-pure || $code.dispatcher.is-pure };
    my $impl-detail = so try { so $code.?is-implementation-detail };
    %( k => $kind, n => $name // $code.name, s => %signature, m => $multiness, |(:p if $pure), |(:rakudo if $impl-detail),
       |(:d($_) with $docs), |(:x($_) with $deprecation));
}

sub pack-package(@elems, $name, Mu \object) {
    describer(@elems, $name, object);
    # Don't traverse concrete constants
    unless object.DEFINITE {
        try for object.WHO.keys {
            when /^<:L>/ {
                pack-package(@elems, $name ~ '::' ~ $_, object.WHO{$_});
            }
        }
    }
}

sub describer(@elems, $name, Mu \object) {
    if object.HOW.WHAT ~~ Metamodel::NativeHOW {
        @elems.push: %( k => "n", n => $name, t => object.^name );
    } elsif object.HOW.WHAT ~~ Metamodel::ClassHOW {
        describe-OOP(@elems, $name, "c", object);
    } elsif object.HOW.WHAT ~~ Metamodel::ParametricRoleGroupHOW {
        describe-OOP(@elems, $name, "ro", object);
    } elsif object.HOW.WHAT ~~ Metamodel::EnumHOW {
        @elems.push: %( k => "e", n => $name, t => object.^name );
    } elsif object.HOW.WHAT ~~ Metamodel::SubsetHOW {
        @elems.push: %( k => "ss", n => $name, t => object.^refinee.^name );
    } elsif object.HOW.WHAT ~~ Metamodel::ModuleHOW {
    } elsif $name.starts-with('EXPORTHOW') {
       my @dummy;
       my $description = describe-OOP(@dummy, $name, 'mm', object)[0];
       @elems.push: %( k => "mm", n => object.^name, t => object.^name, key => $name.subst('EXPORTHOW::', ''), b => 'Mu', m => $description<m> );
    }
}

sub describe-OOP(@elems, $name, $kind, Mu \object) {
    use nqp;
    my $b = nqp::istype(object, Cool) ?? 'C' !! nqp::istype(object, Any) ?? 'A' !! 'M';
    my %class = k => $kind, n => $name, t => object.^name, :$b;
    %class<mro> = (try flat object.^roles.map(*.^name), object.^parents(:local).map(*.^name)) // ();
    my %methods;
    with %CORE-DOCS<types>{$name} {
        %class<d> = $_<prefix>;
        %methods := $_<defs>;
    }
    my @privates;
    if $kind eq "ro" {
        @privates = object.^candidates[0].^private_method_table.values;
    } else {
        try @privates = object.^private_method_table.values;
    }

    # Newer Rakudo (observed on 2025.08) returns ONLY the hidden methods when
    # the :implementation-detail adverb is passed, where older releases
    # returned the ordinary methods plus the hidden ones. Take the union of
    # both calls so the full list is captured either way.
    # (identity comparator: NQP-level routines in the list lack .WHICH)
    my @methods = ( |((try object.^methods(:local, :implementation-detail)) // ()),
                    |((try object.^methods(:local)) // ()) ).unique(:with(&[=:=]));

    try for @methods -> $method {
        if $kind eq 'mm' {
            %class<m>.push: pack-code($method, 0, '^'~ $method.name, :is-method) unless $method.name.starts-with('!');
        } elsif $method !~~ ForeignCode {
            for $method.candidates {
                next if $kind ~~ 'c' && .?package !=:= object;
                try %class<m>.push: pack-code($_, $_.multi ?? 1 !! 0, |(:docs($_) with %methods{$method.name} ), :is-method);
            }
        }
    }
    try for @privates -> $method {
        try %class<m>.push: pack-code($_, $_.multi ?? 1 !! 0, '!' ~ $method.name, :is-method) for $method.candidates;
    }
    for object.^attributes -> $a {
        try %class<a>.push: pack-variable($a.has_accessor ?? $a.name.subst('!', '.') !! $a.name, $a, :is-attribute);
    }
    @elems.push: %class;
}
