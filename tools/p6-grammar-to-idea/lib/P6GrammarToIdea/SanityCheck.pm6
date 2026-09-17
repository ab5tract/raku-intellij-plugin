unit module P6GrammarToIdea::SanityCheck;
use P6GrammarToIdea::AST;

# The sanity checker walks through the AST and detects:
# * Lack of MAIN braid, and lack of TOP in that braid
# * Calling of unknown rules

my constant %BUILTIN-RULES := set 'start-token', 'end-token', 'start-element', 'end-element',
                                  'alpha', 'ww', 'MARKER', 'MARKED', 'peek-delimiters',
                                  'opener', 'bracket-ending', 'start-queue-heredoc',
                                  'end-queue-heredoc', 'dequeue-heredoc', 'has-heredoc',
                                  'opp-start-expr', 'opp-start-infix', 'opp-end-infix',
                                  'opp-end-expr', 'opp-start-prefixes', 'opp-push-prefix',
                                  'opp-end-prefixes', 'opp-start-postfixes',
                                  'opp-push-postfix', 'opp-end-postfixes', 'opp-regex-mode',
                                  'scope-push', 'scope-pop', 'start-symbol',
                                  'end-symbol', 'is-name', 'pod-ws-start', 'pod-ws-commit',
                                  'pod-eat-removed-ws';

multi sub sanity-check(Braids $braids) is export {
    my @*ERRORS;
    without $braids.braids<MAIN> {
        error $braids, 'Missing MAIN language braid';
    }
    sanity-check all $braids.braids.values;
    if @*ERRORS {
        die "Sanity check failed:\n" ~ @*ERRORS.map({ "- $_" }).join("\n")
    }
}

multi sub sanity-check(Grammar $grammar) {
    my $*CURRENT-GRAMMAR = $grammar;
    if $grammar.name eq 'MAIN' && !$grammar.has-rule('TOP') {
        error $grammar, 'MAIN braid must have a TOP rule';
    }
    sanity-check all $grammar.productions;
}

multi sub sanity-check(Production $rule) {
    my $*CURRENT-PRODUCTION = $rule;
    unless $rule.proto {
        sanity-check $rule.implementation;
    }
}

multi sub sanity-check(SeqAlt $alt) {
    sanity-check all $alt.alternatives;
}

multi sub sanity-check(Alt $alt) {
    sanity-check all $alt.alternatives;
}

multi sub sanity-check(Concat $concat) {
    sanity-check all $concat.terms;
}

multi sub sanity-check(Quantifier $quant) {
    sanity-check $quant.target;
    sanity-check($_) with $quant.separator;
}

multi sub sanity-check(Capture $cap) {
    sanity-check $cap.target;
}

multi sub sanity-check(Subrule $call) {
    unless $*CURRENT-GRAMMAR.has-rule($call.name) || $call.name (elem) %BUILTIN-RULES {
        error $call, "No such rule '$call.name()' called from '$*CURRENT-PRODUCTION.name()'";
    }
    sanity-check($_) with $call.regex-arg;
}

multi sub sanity-check(Lookahead $lookahead) {
    sanity-check $lookahead.target;
}

multi sub sanity-check(Literal) { }
multi sub sanity-check(EnumCharList) { }
multi sub sanity-check(BuiltinCharClass) { }
multi sub sanity-check(AnchorLeftWordBoundary) { }
multi sub sanity-check(AnchorRightWordBoundary) { }
multi sub sanity-check(AnchorEnd) { }
multi sub sanity-check(AnchorLineEnd) { }
multi sub sanity-check(AnchorLineStart) { }
multi sub sanity-check(AnchorPass) { }
multi sub sanity-check(AnchorFail) { }
multi sub sanity-check(Interpolation) { }
multi sub sanity-check(Declaration) { }
multi sub sanity-check(CodeBlock) { }

sub error($node, $message) {
    push @*ERRORS, $message;
}
