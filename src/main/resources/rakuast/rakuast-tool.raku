use MONKEY;
use experimental :rakuast;
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

# ========== END OF JSON CODE ==========

my constant @HIDDEN = <
    sunk thunks okifnil sorries worries origin
    lowered-array-init lowered-to-local initializer-in-method is-parameter
    attribute-package generics-package conflicting-type unit-package
    qualified-root original-type
>;

# Some RakuAST attributes can be a Str whose underlying MVMString is a null
# pointer, while every high-level Raku check on it (.defined, type-match,
# even printing it) still succeeds. It only crashes MoarVM inside a *real*
# string operation — .gist, .DEPARSE, .NFD (the JSON encoder's own string
# escaper uses .NFD), even .chars. This is an upstream MoarVM bug (missing
# null guard in MVM_unicode_string_to_codepoints and friends); a fix is in
# progress upstream. It is not tied to one attribute name: the attribute
# that carries it is compiler-version-specific (`lowered-local-name` on one
# Rakudo revision, `storage-name` on another, `ins-lexical-name` on a third
# node type entirely — see task-2-report.md), so denylisting individual
# names is whack-a-mole against a moving target. Detect it structurally
# instead, by unboxing to a native str and asking nqp whether that came back
# null — unboxing does not touch the buffer, so it is safe even on the bad
# value.
sub safe-str(Mu $raw) {
    # Mu, not Any: attribute values include NQP-level objects (e.g.
    # ContainerDescriptor) that are not Any-rooted, and an Any-typed
    # parameter dies on those with "Type check failed in binding to
    # parameter '$raw'; expected Any but got ContainerDescriptor" before we
    # ever get a chance to check anything.
    #
    # This is empirically, not structurally, complete: it only checks
    # whether the top-level attribute value is itself a null-backed Str.
    # A 'node' attribute still gets rendered via $raw.DEPARSE below, which
    # can walk arbitrarily deep into that node's own subtree and touch a
    # null-backed Str nested several levels down -- a case this guard does
    # not see and cannot catch, since that segfault happens inside DEPARSE
    # itself, at the VM level, where no Raku `try` can intervene. The
    # 12-shapes x 4-builds sweep in task-2-report.md found no such case, but
    # that is evidence of absence, not a proof it can't happen on a node
    # type nobody has exercised yet.
    return True unless nqp::istype($raw, Str);
    !nqp::isnull_s(nqp::unbox_s($raw));
}

sub attrs-of($node) {
    my %setters = $node.^methods.map(*.name).grep(*.starts-with('set-'))
                       .map({ .substr(4) => True }).Hash;
    my @out;
    for $node.^attributes -> $a {
        my $name = $a.name.substr(2);
        next if @HIDDEN.first($name);

        # Bind, never assign: a Scalar container defeats the null guard and
        # .defined/.elems/.gist then die on VMNull.
        my $raw := try { $a.get_value($node) };
        next if nqp::isnull(nqp::decont($raw));
        next unless (try { $raw.defined }) // False;

        unless safe-str($raw) {
            @out.push: {
                name     => $name,
                kind     => 'null',
                display  => '(unset)',
                editable => (%setters{$name} ?? True !! False),
            };
            next;
        }

        my ($kind, $display);
        if $raw ~~ RakuAST::Node {
            $kind    = 'node';
            $display = (try { $raw.^name ~ " -> '" ~ $raw.DEPARSE.trim ~ "'" }) // '(unrenderable)';
        }
        elsif $raw ~~ Positional {
            $kind    = 'list';
            $display = (try { "[" ~ $raw.elems ~ " items]" }) // '(unrenderable)';
        }
        else {
            $kind    = 'scalar';
            $display = (try { $raw.gist }) // '(unrenderable)';
        }

        @out.push: {
            name     => $name,
            kind     => $kind,
            display  => $display,
            editable => (%setters{$name} ?? True !! False),
        };
    }
    @out
}

sub node-json($node, @path) {
    my @children;
    my $i = 0;
    $node.visit-children(-> $child {
        if $child ~~ RakuAST::Node {
            @children.push: node-json($child, [|@path, $i]);
            $i++;
        }
    });
    my $origin := $node.origin;
    my %span = $origin.defined ?? { from => $origin.from, to => $origin.to }
                               !! { from => 0, to => 0 };
    %(
        class    => $node.^name,
        path     => @path,
        span     => %span,
        attrs    => attrs-of($node),
        children => @children,
    )
}

sub fail-with($message) {
    say to-json({ error => $message });
    exit 0;
}

# Errors must reach the caller as JSON on stdout: RakuCommandLine reads stdout
# only and discards everything on a non-zero exit.
CATCH { default { fail-with(.message // .gist); } }

my $verb = @*ARGS[0] // fail-with('No verb given.');

if $verb eq 'analyze' {
    my $source = @*ARGS[1].IO.slurp;
    my $ast = (try { $source.AST }) // fail-with("Could not parse the selection: " ~ ($! // 'unknown error'));
    say to-json({ tree => node-json($ast, []) });
}
else {
    fail-with("Unknown verb '$verb'.");
}
