unit module P6GrammarToIdea::Tokens;
use P6GrammarToIdea::AST;

# This module walks the AST and identifies all of the tokens (that is, regions
# between a `<.start-token('FOO')>` and an `<.end-token('FOO')>`). These will
# be the tokens returned from lexer generated from IntelliJ; token definitions
# will also be automatically generated from these too.
#
# It also detects some error cases:
# * `end-token` call when no `start-token` call is outstanding
# * Mismatched name of `start-token` and `end-token`
# * `start-token` call within an existing `start-token`
# * Regions that parse something, but aren't covered by a token
# It does this by performing an abstract interpretation of the grammar, which
# means it also descends into rules.

my constant %IGNORE-NAMES = set 'start-element', 'end-element', 'alpha', 'ww',
                                'MARKER', 'MARKED', 'peek-delimiters', 'opener', 'bracket-ending',
                                'start-queue-heredoc', 'end-queue-heredoc', 'has-heredoc',
                                'dequeue-heredoc', 'opp-start-expr', 'opp-start-infix',
                                'opp-end-expr', 'opp-start-prefixes', 'opp-end-prefixes',
                                'opp-start-postfixes', 'opp-end-postfixes', 'opp-regex-mode',
                                'scope-push', 'scope-pop', 'start-symbol', 'end-symbol', 'is-name',
                                'pod-ws-start', 'pod-ws-commit', 'pod-eat-removed-ws';

class X::P6GrammarToIdea::UncoveredByToken is Exception {
    has $.production-name;
    has $.uncovered;

    method message() {
        "No token covering $!uncovered in '$!production-name'"
    }
}

class X::P6GrammarToIdea::OverlappingToken is Exception {
    has $.existing;
    has $.conflicting;
    has $.production-name;

    method message() {
        "Token '$!conflicting' in '$!production-name' opens when '$!existing' unclosed"
    }
}

class X::P6GrammarToIdea::MissingEndToken is Exception {
    has $.production-name;
    has $.token-name;

    method message() {
        "Token '$!token-name' in '$!production-name' has no end"
    }
}

sub check-and-get-tokens(Braids $braids) is export {
    my $*CURRENT-GRAMMAR = $braids.braids<MAIN>;
    my $*CURRENT-PRODUCTION = $*CURRENT-GRAMMAR.get-rule('TOP');
    my %*KNOWN-TOKENS;
    my $*CURRENT-TOKEN;
    my $*CURRENT-TOKEN-START-PRODUCTION;

    # Track seen productions to avoid infinite recursion.
    my %*SEEN-PRODUCTIONS;

    # Track productions we already decided are well nested. This factors in
    # subrules (they are applied after the walk of subrules). This helps avoid
    # a lot of duplicate path visiting in the tree walk.
    my %*WELL-NESTED-PRODUCTIONS;

    walk($*CURRENT-PRODUCTION);
    with $*CURRENT-TOKEN {
        die X::P6GrammarToIdea::MissingEndToken.new:
            token-name => $_,
            production-name => $*CURRENT-TOKEN-START-PRODUCTION.name;
    }
    return %*KNOWN-TOKENS.keys;
}

multi sub walk(Production $rule) {
    die "Handling of proto NYI" if $rule.proto;
    if %*SEEN-PRODUCTIONS{$rule.name} {
        if $*CURRENT-TOKEN {
            die "Cannot recurse within a token, but '$rule.name()' does so";
        }
    }
    elsif %*WELL-NESTED-PRODUCTIONS{$rule.name} && !$*CURRENT-TOKEN {
        # The production is known to be well nested, and we're not currently
        # in a token, so no need to revisit this part of the tree.
    }
    else {
        # If we're not currently in a token at the start of the rule, and nor
        # are we at the end of the rule, and it passed validation, then it
        # must be well nested.
        my $well-nested = not $*CURRENT-TOKEN;
        %*SEEN-PRODUCTIONS{$rule.name} = True;
        walk $rule.implementation;
        $well-nested = False if $*CURRENT-TOKEN;
        %*SEEN-PRODUCTIONS{$rule.name} = False;
        %*WELL-NESTED-PRODUCTIONS{$rule.name} = $well-nested;
    }
}

multi sub walk(SeqAlt $alt) {
    walk all $alt.alternatives;
}

multi sub walk(Alt $alt) {
    walk all $alt.alternatives;
}

multi sub walk(Concat $concat) {
    walk all $concat.terms;
}

multi sub walk(Quantifier $quant) {
    walk $quant.target;
    walk($_) with $quant.separator;
}

multi sub walk(Capture $cap) {
    walk $cap.target;
}

multi sub walk(Subrule $call) {
    given $call.name {
        when 'start-token' {
            my $token-name = get-token-name($call);
            with $*CURRENT-TOKEN {
                die X::P6GrammarToIdea::OverlappingToken.new:
                    existing => $_,
                    conflicting => $token-name,
                    production-name => $*CURRENT-PRODUCTION.name
            }
            $*CURRENT-TOKEN = $token-name;
            %*KNOWN-TOKENS{$token-name} = True;
            $*CURRENT-TOKEN-START-PRODUCTION = $*CURRENT-PRODUCTION;
        }
        when 'end-token' {
            $*CURRENT-TOKEN = Nil;
            $*CURRENT-TOKEN-START-PRODUCTION = Nil;
        }
        when 'opp-push-prefix' | 'opp-push-postfix' | 'opp-end-infix' {
            # These emit a fake token to convey precedence info to the parser,
            # so we must not already be in one.
            with $*CURRENT-TOKEN {
                die X::P6GrammarToIdea::OverlappingToken.new:
                    existing => $_,
                    conflicting => $call.name,
                    production-name => $*CURRENT-PRODUCTION.name
            }
        }
        when %IGNORE-NAMES{$_}:exists {
            # Not significant for the tokenizer
        }
        default {
            my $*CURRENT-PRODUCTION = $*CURRENT-GRAMMAR.get-rule($call.name);
            walk($*CURRENT-PRODUCTION);
        }
    }
}

sub get-token-name($call) {
    unless $call.args == 1 && $call.args[0] ~~ StrValue {
        die 'Must call start-token and end-token with one string argument';
    }
    $call.args[0].value
}

multi sub walk(Literal) {
    ensure-covered('literal');
}

multi sub walk(EnumCharList) {
    ensure-covered('enumerated character class');
}

multi sub walk(BuiltinCharClass) {
    ensure-covered('built-in character class');
}

multi sub walk(Interpolation $i) {
    ensure-covered("interpolation of '$i.variable-name()'");
}

sub ensure-covered($what --> Nil) {
    $*CURRENT-TOKEN or die X::P6GrammarToIdea::UncoveredByToken.new(
        production-name => $*CURRENT-PRODUCTION.name,
        uncovered => $what
    );
}

# Ignore lookaheads, anchors, and declarations, because they don't match any chars.
multi sub walk(Lookahead) { }
multi sub walk(AnchorLeftWordBoundary) { }
multi sub walk(AnchorRightWordBoundary) { }
multi sub walk(AnchorEnd) { }
multi sub walk(AnchorLineEnd) { }
multi sub walk(AnchorLineStart) { }
multi sub walk(AnchorPass) { }
multi sub walk(AnchorFail) { }
multi sub walk(Declaration) { }
multi sub walk(CodeBlock) { }
