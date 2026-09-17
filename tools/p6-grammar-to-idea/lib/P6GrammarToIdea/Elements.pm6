unit module P6GrammarToIdea::Elements;
use P6GrammarToIdea::AST;

# This walks the AST and builds up an element model. This will be used to
# generate an element list and a parser. The parser works in terms of tokens,
# which are produced by the lexer, which actually does the hard work of Raku
# parsing (far more than a "traditional" lexer).
#
# The element model is a projection of the AST, which only contains the parts
# that are relevant for parsing. The insides of a token are not considered,
# beyond tokens that consist entirely of a literal. This means any quantifiers
# inside of a token are not considered, for example. However, a quantifier or
# alternation falling outside of any token will be part of the model.

my constant %IGNORE-NAMES = set 'alpha', 'ww', 'MARKER', 'MARKED', 'peek-delimiters', 'opener',
                                'bracket-ending', 'start-queue-heredoc', 'has-heredoc',
                                'end-queue-heredoc', 'dequeue-heredoc', 'scope-push',
                                'scope-pop', 'start-symbol', 'end-symbol', 'is-name',
                                'pod-ws-start', 'pod-ws-commit', 'pod-eat-removed-ws';

role ParseNode is export {}

class TokenNode does ParseNode is export {
    has $.token-name is required;
    has $.literal-value;
}

class ElementNode does ParseNode is export {
    has $.element-name is required;
    has ParseNode $.element-parser is required;
}

class CallNode does ParseNode is export {
    has $.production-name is required;
}

class ConcatNode does ParseNode is export {
    has ParseNode @.terms;
}

class AltNode does ParseNode is export {
    has ParseNode @.alternatives;
}

class QuantNode does ParseNode is export {
    has $.min;
    has $.max;
    has ParseNode $.target;
}

class OPPNode does ParseNode is export {
    has $.method-name;
}

class PassNode does ParseNode is export { }

class FailNode does ParseNode is export { }

class BraidModel {
    has %!production-models;
    has %!element-names;

    method add-production(Production $p) {
        unless $p.proto {
            %!production-models{$p.name} = self.walk($p.implementation);
        }
    }

    method element-names() {
        %!element-names.keys
    }

    method production-names() {
        %!production-models.keys
    }

    method get-production-model($name) {
        %!production-models{$name}:exists
            ?? %!production-models{$name}
            !! die("No production structure for $name")
    }

    multi method walk(Alt $a) {
        self!alt($a);
    }

    multi method walk(SeqAlt $a) {
        self!alt($a);
    }

    method !alt($a) {
        AltNode.new: alternatives => $a.alternatives.map({ self.walk($_) })
    }

    multi method walk(Concat $c) {
        self!concat($c.terms)
    }

    method !concat(@terms-in) {
        my @top-terms;
        my $in-element;
        my $in-token;
        my @sub-terms;
        for @terms-in -> $term {
            if $term ~~ Subrule {
                given $term.name {
                    when 'start-element' {
                        my $element-name = get-name($term);
                        %!element-names{$element-name}++;
                        if $in-token {
                            die "Unexpected start of element $element-name inside of token";
                        }
                        elsif $in-element {
                            push @sub-terms, $term;
                        }
                        else {
                            $in-element = $element-name;
                        }
                    }
                    when 'end-element' {
                        my $element-name = get-name($term);
                        unless $in-element {
                            die "Saw end of element $element-name but was not inside of it";
                        }
                        if $element-name eq $in-element {
                            push @top-terms, ElementNode.new: :$element-name,
                                element-parser => self!concat(@sub-terms);
                            $in-element = Nil;
                            @sub-terms = ();
                        }
                        else {
                            push @sub-terms, $term;
                        }
                    }
                    when 'start-token' {
                        my $token-name = get-name($term);
                        if $in-element {
                            push @sub-terms, $term;
                        }
                        elsif $in-token {
                            die "Cannot start $token-name while in $in-token";
                        }
                        else {
                            $in-token = $token-name;
                        }
                    }
                    when 'end-token' {
                        my $token-name = get-name($term);
                        if $in-element {
                            push @sub-terms, $term;
                        }
                        elsif $in-token {
                            unless $in-token eq $token-name {
                                die "End of token $token-name but expected $in-token";
                            }
                            my $literal-value = @sub-terms == 1 && @sub-terms[0] ~~ Literal
                                ?? @sub-terms[0].value
                                !! Nil;
                            push @top-terms, TokenNode.new: :$token-name, :$literal-value;
                            @sub-terms = ();
                            $in-token = Nil;
                        }
                        else {
                            die "Saw end of token $token-name but was not inside of it";
                        }
                    }
                    default {
                        if $in-token || $in-element {
                            push @sub-terms, $term;
                        }
                        else {
                            push @top-terms, self.walk($term);
                        }
                    }
                }
            }
            else {
                if $in-token || $in-element {
                    push @sub-terms, $term;
                }
                else {
                    push @top-terms, self.walk($term);
                }
            }
        }
        if $in-element {
            die "Unterminated element $in-element";
        }
        if $in-token {
            die "Unterminated element $in-token";
        }
        return @top-terms == 1 ?? @top-terms[0] !! ConcatNode.new(:terms(@top-terms));
    }

    sub get-name($call) {
        unless $call.args == 1 && $call.args[0] ~~ StrValue {
            die 'Must call $call.name() with one string argument';
        }
        $call.args[0].value
    }

    multi method walk(Quantifier $q) {
        QuantNode.new: min => $q.min, max => $q.max, target => self.walk($q.target);
    }

    multi method walk(Capture $c) {
        self.walk($c.target);
    }

    multi method walk(Subrule $s) {
        given $s.name {
            when 'start-element' | 'end-element' | 'start-token' | 'end-token' {
                die "Oops, should never make it to walk subrule with $_";
            }
            when %IGNORE-NAMES{$_}:exists {
                PassNode.new
            }
            when /^ 'opp-'/ {
                OPPNode.new: method-name => $_
            }
            default {
                CallNode.new: production-name => $_
            }
        }
    }

    multi method walk(Literal) { PassNode.new }
    multi method walk(Lookahead) { PassNode.new }
    multi method walk(EnumCharList) { PassNode.new }
    multi method walk(BuiltinCharClass) { PassNode.new }
    multi method walk(AnchorLeftWordBoundary) { PassNode.new }
    multi method walk(AnchorRightWordBoundary) { PassNode.new }
    multi method walk(AnchorEnd) { PassNode.new }
    multi method walk(AnchorLineEnd) { PassNode.new }
    multi method walk(AnchorLineStart) { PassNode.new }
    multi method walk(AnchorPass) { PassNode.new }
    multi method walk(AnchorFail) { FailNode.new }
    multi method walk(Interpolation) { PassNode.new }
    multi method walk(Declaration) { PassNode.new }
    multi method walk(CodeBlock) { PassNode.new }
}

class Model {
    has %!braids;

    method add-braid-model(Str $key, BraidModel $bm) {
        %!braids{$key} = $bm;
    }

    method element-names() {
        %!braids.values.map({ |.element-names }).unique
    }

    method braid-names() {
        %!braids.keys
    }

    method get-braid-model($name) {
        %!braids{$name} // die "No model for braid '$name'"
    }
}

sub build-element-model(Braids $braids) is export {
    my $model = Model.new;
    for $braids.braids.kv -> $name, $grammar {
        my $bm = BraidModel.new;
        for $grammar.productions {
            $bm.add-production($_);
        }
        $model.add-braid-model($name, $bm);
    }
    return $model;
}
