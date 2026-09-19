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

# Longest source excerpt still worth the width it costs in a tree label.
# Beyond this the excerpt is dropped entirely rather than truncated, since a
# half-shown fragment of source distracts more than no fragment at all.
my constant EXCERPT-MAX-CHARS = 24;

# A gist is fetched for one selected node at a time, so this can be generous
# -- but a whole file's root still gists to hundreds of kilobytes, which is
# more than a table cell can usefully show.
my constant GIST-MAX-CHARS = 100_000;
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

# The children of $node that belong in the emitted tree, in emission order.
#
# analyze and edit MUST agree on this exactly: analyze hands the IDE a path
# built from these indices, and edit walks that path back to a node. Any
# divergence in filtering or ordering silently retargets an edit, so both go
# through this one function rather than each doing its own visit-children.
#
# When context statements were prepended, the root's leading children belong
# to that prefix rather than to the user's selection, and are dropped.
sub node-children($node, $offset, $is-root) {
    my @kids;
    $node.visit-children(-> $child {
        if $child ~~ RakuAST::Node {
            my $from-prefix = False;
            if $is-root && $offset > 0 {
                my $o := $child.origin;
                $from-prefix = True if $o.defined && $o.from < $offset;
            }
            @kids.push($child) unless $from-prefix;
        }
    });
    @kids
}

sub node-json($node, @path, $offset = 0) {
    my @children;
    my $i = 0;
    for node-children($node, $offset, @path.elems == 0) -> $child {
        @children.push: node-json($child, [|@path, $i], $offset);
        $i++;
    }
    my $origin := $node.origin;
    # {from:0,to:0} would be indistinguishable from a real zero-length span
    # at offset 0 -- observed on `sub f($a) { $a * 2 }`, where a
    # RakuAST::Type::Setting node has undefined .origin but IS editable, so
    # a caller could silently apply an edit against a fake (0,0) span.
    #
    # Origins are rebased past any prepended context so the IDE keeps
    # receiving selection-relative spans. The root spans the prefix too, hence
    # the clamp: without it its `from` would go negative.
    my $span = $origin.defined
        ?? { from => max(0, $origin.from - $offset), to => $origin.to - $offset }
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

    # The source excerpt is only worth the width it costs in a tree label when
    # it is short and says something the identity marker does not. Drop it when
    # it merely repeats the marker -- `Name 【Int】 ⎡Int⎤` says the same thing
    # twice -- or when it exceeds EXCERPT-MAX-CHARS, where in a tree it pushes
    # every sibling's label off to the right for little gain.
    my $identity = $markers ~~ / '【' (.+?) '】' / ?? ~$/[0] !! Str;
    my $excerpt  = $origin  ~~ / '⎡' (.*?) '⎤' / ?? ~$/[0] !! Str;
    if $excerpt.defined
       && ($excerpt.chars > EXCERPT-MAX-CHARS
           || ($identity.defined && $identity eq $excerpt)) {
        $origin = $origin.subst(/ \s* '⎡' .*? '⎤' /, '').trim;
    }

    my $summary = $class
        ~ ($markers ?? ' ' ~ $markers !! '')
        ~ ($origin  ?? ' ' ~ $origin  !! '');
    cap-display($summary.trim);
}

sub fail-with($message) {
    say to-json({ error => $message });
    exit 0;
}

# Walks a path produced by analyze. Uses node-children so the filtering and
# ordering match analyze's exactly -- see the note there.
sub node-at($root, @path, $offset = 0) {
    my $current = $root;
    my $depth = 0;
    for @path -> $index {
        my @kids = node-children($current, $offset, $depth == 0);
        fail-with("The selected node is no longer present.") unless @kids[$index].defined;
        $current = @kids[$index];
        $depth++;
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

# Put the project's own modules on the search path.
#
# Deliberately done here rather than with -I on the command line: an -I path
# is resolved while THIS script is compiling, so a distribution whose
# META6.json names a file that does not exist takes the script down before
# any CATCH exists to report it -- the caller then sees empty stdout and a
# non-zero exit, with the real reason only on stderr. Registered at runtime,
# the identical failure surfaces as a catchable exception inside .AST, and
# the JSON contract survives.
#
# Both paths are added and neither subsumes the other: the distribution root
# resolves through META6.json's `provides`, while lib/ still finds a module
# the author has not declared there yet -- routine for a file being edited.
sub add-lib-path($path) {
    return False unless $path.IO.e;
    my $repo = try {
        CompUnit::RepositoryRegistry.repository-for-spec($path.IO.absolute, :next-repo($*REPO));
    };
    return False unless $repo;
    PROCESS::<$REPO> := $repo;
    True;
}

# A distribution whose META6.json names a file that is not there poisons every
# module lookup in the process, including lookups that have nothing to do with
# it: `use lib "/tmp/x"; use SomethingElse;` fails with "Failed to open file
# .../lib/Whatever.rakumod". That is not hypothetical -- both a scaffolded
# project and a real one in development were in exactly that state.
#
# The distribution root is only registered when its metadata is consistent.
# lib/ is registered regardless, because a plain path repository never reads
# META6.json and so cannot be broken by one.
sub distribution-is-loadable($root) {
    my $meta = $root.IO.add('META6.json');
    return False unless $meta.e;
    my $text = (try { $meta.slurp }) // return False;

    # Deliberately not a JSON parse: this script vendors an encoder, not a
    # decoder, and every path worth checking is a quoted string ending in a
    # Raku source extension. Missing one is safe -- the worst case is that we
    # register a distribution that then fails as it does today.
    for $text.match(/ '"' (<-["]>+ [ '.rakumod' | '.pm6' | '.rakutest' | '.t' ]) '"' /, :g) -> $m {
        my $path = ~$m[0];
        next if $path.starts-with('http');
        return False unless $root.IO.add($path).e;
    }
    True;
}

add-lib-path($*CWD.Str) if distribution-is-loadable($*CWD.Str);
add-lib-path($*CWD.add('lib').Str);

my $verb = @*ARGS[0] // fail-with('No verb given.');

# .AST compiles its string as a whole compilation unit, so a selection cannot
# inherit the surrounding file's imports any other way: they have to be part
# of the same text. The prefix's own nodes are then dropped from the tree (see
# node-children) and every origin rebased past it, so the IDE still receives
# spans relative to the user's selection.
#
# Returns the parsed root, the grapheme offset to rebase by, and the context
# lines to report back.
sub parse-with-context($snippet, $context-path) {
    my $context = $context-path && $context-path.IO.e ?? $context-path.IO.slurp.trim !! '';
    unless $context {
        my $ast = (try { $snippet.AST })
            // fail-with("Could not parse the selection: " ~ ($! // 'unknown error'));
        return %( ast => $ast, offset => 0, context => [] );
    }

    my $prefix = $context ~ "\n";
    # Report a compile failure rather than quietly retrying without context:
    # the selection genuinely does not compile against its own imports, and
    # saying so is more useful than a tree built from a different premise.
    my $ast = (try { ($prefix ~ $snippet).AST })
        // fail-with("The selection could not be turned into RakuAST -- it does not compile "
                     ~ "together with this file's imports: " ~ ($! // 'unknown error'));
    %( ast     => $ast,
       offset  => $prefix.chars,
       context => $context.lines.grep({ .trim }).list )
}

if $verb eq 'analyze' {
    my $source = @*ARGS[1].IO.slurp;
    my %parsed = parse-with-context($source, @*ARGS[2]);
    my %tree   = node-json(%parsed<ast>, [], %parsed<offset>);
    %tree<context> = %parsed<context>;
    say to-json({ tree => %tree });
}
elsif $verb eq 'gist' {
    # One node's .gist, fetched on demand.
    #
    # Not emitted with the tree: a node's gist contains its whole subtree
    # re-serialised, so every level repeats the levels below it. Measured on a
    # 40-character snippet, the 28 nodes summed to 6399 characters of gist --
    # a 160x blow-up of the source. Fetching the selected node alone keeps
    # that cost proportional to what is actually being looked at.
    my $source = @*ARGS[1].IO.slurp;
    my @path   = @*ARGS[2] ?? @*ARGS[2].split(',').map(*.Int) !! ();

    # Same context as analyze, or the path walks to a different node.
    my %parsed = parse-with-context($source, @*ARGS[3]);
    my $target = node-at(%parsed<ast>, @path, %parsed<offset>);

    my $gist = (try { $target.gist })
        // fail-with("This node could not be rendered as a gist.");

    # The root of a large file can gist to hundreds of kilobytes. Bound it,
    # and say so rather than truncating silently -- a gist cut mid-constructor
    # looks like malformed output instead of a deliberate limit.
    if $gist.chars > GIST-MAX-CHARS {
        $gist = $gist.substr(0, GIST-MAX-CHARS)
              ~ "\n\n... truncated at {GIST-MAX-CHARS} characters."
              ~ " Select a deeper node for a complete gist.";
    }

    say to-json({ gist => $gist });
}
elsif $verb eq 'edit' {
    my $source    = @*ARGS[1].IO.slurp;
    my @path      = @*ARGS[2] ?? @*ARGS[2].split(',').map(*.Int) !! ();
    my $attr      = @*ARGS[3];
    my $value     = @*ARGS[4].IO.slurp;
    my $kind      = @*ARGS[5];

    # Same context as analyze, or the path walked below would target a
    # different node than the one the user selected.
    my %parsed = parse-with-context($source, @*ARGS[6]);
    my $ast    = %parsed<ast>;
    my $offset = %parsed<offset>;
    my $target = node-at($ast, @path, $offset);

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
    # Rebased past any prepended context, so the caller receives a span in the
    # user's selection rather than in the combined text we actually compiled.
    my $span = $origin.defined
        ?? { from => max(0, $origin.from - $offset), to => $origin.to - $offset }
        !! Any;

    my $setter = 'set-' ~ $attr;
    # Report failure on the TRY failing, not on the setter's return value.
    #
    # `try { ... } // fail-with(...)` reads naturally but is wrong here: `//`
    # is defined-or, so a setter that simply returns Nil -- set-expression
    # does -- was treated as having failed, and editing a node-valued
    # attribute always reported "Could not set". The setter had in fact
    # already applied the change. Appending True makes the check ask "did
    # this throw?" rather than "did this return something?".
    if $target.^can($setter) {
        my $applied = try { $target."$setter"($new-value); True };
        fail-with("Could not set '$attr'.") unless $applied;
    }
    else {
        my $applied = try {
            nqp::bindattr(nqp::decont($target), $target.WHAT, '$!' ~ $attr, nqp::decont($new-value));
            True;
        };
        fail-with("'$attr' cannot be set on {$target.^name}.") unless $applied;
    }

    my $text = (try { $target.DEPARSE }) // fail-with('The edit produced source that could not be rendered.');

    # Sanity check: never hand back source that cannot be re-parsed.
    fail-with('The edit produced invalid Raku and was not applied.')
        unless (try { $text.AST; True }) // False;

    my %tree = node-json($ast, [], $offset);
    %tree<context> = %parsed<context>;
    say to-json({
        text => $text,
        span => $span,
        tree => %tree,
    });
}
else {
    fail-with("Unknown verb '$verb'.");
}
