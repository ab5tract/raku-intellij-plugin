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
    lexical-lookup-hash owner outer origin-comp-unit resolution routine
>;

# Bound how much text a single attribute can contribute to the payload.
# A real-world 378-line file produced a single `display` value of 105,117
# characters (a compiler lookup hash falling through to .gist because Hash
# is not Positional) and 421 KB of `display` text overall out of 958 KB
# total. This caps every display string regardless of which bookkeeping
# attribute shows up next on a future Rakudo.
my constant DISPLAY-LIMIT = 512;
sub cap-display($s) {
    $s.chars > DISPLAY-LIMIT ?? $s.substr(0, DISPLAY-LIMIT) ~ '…' !! $s;
}

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
            # Class name on its own line, deparsed source indented beneath it.
            # The panel renders attribute cells as wrapped text, so the newlines
            # survive; indenting every line (not just the first) keeps a
            # multi-line deparse -- a block or routine body -- readable rather
            # than running it up against the node class name.
            $display = (try {
                my $deparsed = $raw.DEPARSE.trim;
                $raw.^name ~ " ->\n" ~ $deparsed.lines.map({ '  ' ~ $_ }).join("\n");
            }) // '(unrenderable)';
        }
        elsif $raw ~~ Positional {
            $kind    = 'list';
            $display = (try { "[" ~ $raw.elems ~ " items]" }) // '(unrenderable)';
        }
        elsif $raw ~~ Associative {
            # Hash is not Positional, so without this it falls through to the
            # scalar branch below and .gist's the whole hash -- a compiler
            # lookup hash rendered that way produced a single 105,117-char
            # display value on a real file. Render it like a list instead.
            $kind    = 'scalar';
            $display = (try { "{$raw.elems} entries" }) // '(unrenderable)';
        }
        else {
            $kind    = 'scalar';
            $display = (try { $raw.gist }) // '(unrenderable)';
        }
        $display = cap-display($display);

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
    # {from:0,to:0} would be indistinguishable from a real zero-length span
    # at offset 0 -- observed on `sub f($a) { $a * 2 }`, where a
    # RakuAST::Type::Setting node has undefined .origin but IS editable, so
    # a caller could silently apply an edit against a fake (0,0) span.
    my $span = $origin.defined ?? { from => $origin.from, to => $origin.to }
                               !! Any;
    %(
        class    => $node.^name,
        path     => @path,
        span     => $span,
        summary  => summary-of($node),
        attrs    => attrs-of($node),
        children => @children,
    )
}

# Rakudo's own one-line node summary -- the primary line of RakuAST::Node.dump,
# composed here from its public parts rather than by calling .dump and taking
# .lines[0], because .dump recurses through every child to build a whole
# subtree we would immediately throw away.
#
# It carries what the tree label cannot: the node's identity (the 【$x】/【+】
# /【f】 markers that per-class dump-markers overrides supply), its sink and
# block-statement state (⚓ ▪), whether its origin is a key (𝄞), and a source
# excerpt Rakudo truncates at 50 characters.
sub summary-of($node) {
    my $class   = $node.^name.substr('RakuAST::'.chars);
    # .trim each part before joining: some dump-markers overrides already end
    # in a space, which would otherwise double up. Only the joints are
    # normalised -- the source excerpt inside ⎡⎤ keeps its own spacing.
    my $markers = ((try { $node.dump-markers() }) // '').trim;
    my $origin  = ((try { $node.dump-origin()  }) // '').trim;
    my $summary = $class
        ~ ($markers ?? ' ' ~ $markers !! '')
        ~ ($origin  ?? ' ' ~ $origin  !! '');
    cap-display($summary.trim);
}

sub fail-with($message) {
    say to-json({ error => $message });
    exit 0;
}

sub node-at($root, @path) {
    my $current = $root;
    for @path -> $index {
        my @kids;
        $current.visit-children(-> $c { @kids.push($c) if $c ~~ RakuAST::Node });
        fail-with("The selected node is no longer present.") unless @kids[$index].defined;
        $current = @kids[$index];
    }
    $current
}

# Reads $node's current value for attribute $name, using the same
# bind-not-assign and null-Str guard as attrs-of (safe-str), so this can
# never trip the same MoarVM segfault Task 2 worked around. Returns an
# undefined Mu when the attribute is unknown, unset, or unsafe to inspect;
# callers fall back to a heuristic in that case rather than failing.
sub current-value($node, $name) {
    my $a = $node.^attributes.first(*.name eq '$!' ~ $name) // return Mu;
    my $raw := try { $a.get_value($node) };
    return Mu if nqp::isnull(nqp::decont($raw));
    return Mu unless (try { $raw.defined }) // False;
    return Mu unless safe-str($raw);
    $raw
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
elsif $verb eq 'edit' {
    my $source    = @*ARGS[1].IO.slurp;
    my @path      = @*ARGS[2] ?? @*ARGS[2].split(',').map(*.Int) !! ();
    my $attr      = @*ARGS[3];
    my $value     = @*ARGS[4].IO.slurp;
    my $kind      = @*ARGS[5];

    my $ast    = (try { $source.AST }) // fail-with('Could not parse the selection.');
    my $target = node-at($ast, @path);

    # Validate before mutating, so a bad snippet never reaches the file.
    my $new-value = do if $kind eq 'node' {
        my $parsed = (try { $value.AST }) // fail-with("Could not parse '$value' as Raku.");
        # A bare expression arrives wrapped in StatementList/Statement::Expression.
        my $inner = $parsed;
        while $inner ~~ RakuAST::StatementList | RakuAST::Statement::Expression {
            my @kids;
            $inner.visit-children(-> $c { @kids.push($c) if $c ~~ RakuAST::Node });
            last unless @kids;
            $inner = @kids[0];
        }
        $inner
    }
    else {
        # Scalars: coerce to match the attribute's *current* type on
        # $target, so e.g. editing a Str-typed attribute to "42" produces a
        # Str, not an Int -- pattern-matching the input string's shape
        # alone can't tell those apart. Bool is checked before Int because
        # True/False smart-match Int too. Only fall back to guessing from
        # the input string's shape when the current value is unset or its
        # type can't be determined safely (current-value returns an
        # undefined Mu in that case).
        my $current := current-value($target, $attr);
        if $current.defined {
            given $current {
                when Bool { $value.lc eq 'true' ?? True !! False }
                when Str  { $value }
                when Int  { (try { $value.Int }) // $value }
                default   { $value ~~ /^ '-'? \d+ $/ ?? $value.Int !! $value }
            }
        }
        else {
            $value ~~ /^ '-'? \d+ $/ ?? $value.Int !! $value
        }
    };

    # Read .origin BEFORE mutating: hoisted so that if some setter ever
    # incidentally resets .origin, the span returned still reflects the
    # node's pre-edit location rather than silently going stale or null.
    my $origin := $target.origin;
    my $span = $origin.defined ?? { from => $origin.from, to => $origin.to } !! Any;

    my $setter = 'set-' ~ $attr;
    if $target.^can($setter) {
        try { $target."$setter"($new-value) } // fail-with("Could not set '$attr'.");
    }
    else {
        try { nqp::bindattr(nqp::decont($target), $target.WHAT, '$!' ~ $attr, nqp::decont($new-value)) } // fail-with("'$attr' cannot be set on {$target.^name}.");
    }

    my $text = (try { $target.DEPARSE }) // fail-with('The edit produced source that could not be rendered.');

    # Sanity check: never hand back source that cannot be re-parsed.
    fail-with('The edit produced invalid Raku and was not applied.')
        unless (try { $text.AST; True }) // False;

    say to-json({
        text => $text,
        span => $span,
        tree => node-json($ast, []),
    });
}
else {
    fail-with("Unknown verb '$verb'.");
}
