use P6GrammarToIdea::AST;

class P6GrammarToIdea::Actions {
    method TOP($/) {
        make Braids.new: braids => %($<grammar>.map({ .ast.name => .ast }));
    }

    method grammar($/) {
        make Grammar.new: name => ~$<name>, productions => $<production>.map(*.ast);
    }

    method production($/) {
        my @parameters = $<parameter>.map(*.ast);
        make $<proto>
            ?? Production.new(:proto, name => ~$<name>, :@parameters)
            !! Production.new(:!proto, name => ~$<name>, :@parameters,
                    sym => ($<sym> ?? ~$<sym> !! Nil),
                    implementation => $<nibbler>.ast);
    }

    method parameter($/) {
        make ~$/;
    }

    method nibbler($/) {
        make $<seqalt>.ast;
    }

    method seqalt($/) {
        make $<alt>.elems > 1
            ?? SeqAlt.new(alternatives => $<alt>.map(*.ast))
            !! $<alt>[0].ast;
    }

    method alt($/) {
        make $<termish>.elems > 1
            ?? Alt.new(alternatives => $<termish>.map(*.ast))
            !! $<termish>[0].ast;
    }

    method termish($/) {
        make $<quantified-atom>.elems > 1
            ?? Concat.new(terms => $<quantified-atom>.map(*.ast))
            !! $<quantified-atom>[0].ast;
    }

    method quantified-atom($/) {
        if $<quantifier> {
            my $q = $<quantifier>.ast;
            make Quantifier.new: min => $q.min, max => $q.max, target => $<atom>.ast,
                separator => $<separator> ?? $<separator>.ast !! Nil;
        }
        else { 
            make $<atom>.ast;
        }
    }

    method separator($/) {
        make $<quantified-atom>.ast;
    }

    method quantifier:sym<?>($/) { make 0..1 }
    method quantifier:sym<*>($/) { make 0..* }
    method quantifier:sym<+>($/) { make 1..* }

    method atom($/) {
        make $<metachar>.ast;
    }

    method metachar:sym<.>($/) {
        make BuiltinCharClass.new: class => AnyChar, :!negative;
    }

    method metachar:sym<$>($/) {
        make AnchorEnd.new
    }

    method metachar:sym<$$>($/) {
        make AnchorLineEnd.new
    }

    method metachar:sym<^^>($/) {
        make AnchorLineStart.new
    }

    method metachar:sym<group>($/) {
        make $<nibbler>.ast;
    }

    method metachar:sym<assert>($/) {
        make $<assertion>.ast;
    }
    
    method metachar:sym<'>($/) {
        make Literal.new: value => $<single-quote-string>.ast;
    }

    method metachar:sym<var>($/) {
        make Capture.new: name => ~$<name>, target => $<quantified-atom>.ast;
    }

    method metachar:sym<bs>($/) {
        make $<backslash>.ast;
    }

    method metachar:sym<lwb>($/) {
        make AnchorLeftWordBoundary.new
    }

    method metachar:sym<rwb>($/) {
        make AnchorRightWordBoundary.new
    }

    method metachar:sym<~>($/) {
        make Concat.new: terms => (
            $<EXPR>.ast,
            SeqAlt.new: alternatives => (
                $<GOAL>.ast,
                Subrule.new: name => 'FAILGOAL'
            )
        );
    }

    method metachar:sym<variable>($/) {
        make Interpolation.new: variable-name => ~$/;
    }

    method metachar:sym<:my>($/) {
        make Declaration.new: variable-name => ~$<var>, value => $<value>.ast;
    }

    method metachar:sym<{>($/) {
        make CodeBlock.new: type => SimpleCode, statement => $<codeblock>.ast;
    }

    method codeblock($/) {
        make $<code>.ast;
    }

    method single-quote-string($/) {
        make $<single-quote-string-part>.map(*.ast).join;
    }

    method single-quote-string-part($/) {
        make $<esc> ?? ~$<esc> !! ~$/;
    }

    method backslash:sym<s>($/) {
        make BuiltinCharClass.new: class => SpaceChars, negative => $/ eq 'S';
    }
    method backslash:sym<d>($/) {
        make BuiltinCharClass.new: class => DigitChars, negative => $/ eq 'D';
    }
    method backslash:sym<w>($/) {
        make BuiltinCharClass.new: class => WordChars, negative => $/ eq 'W';
    }
    method backslash:sym<n>($/) {
        make BuiltinCharClass.new: class => NewlineChars, negative => $/ eq 'N';
    }
    method backslash:sym<h>($/) {
        make EnumCharList.new:
            chars => "\x[09,20,a0,1680,180e,2000,2001,2002,2003,2004,2005,2006,2007,2008,2009,200a,202f,205f,3000]",
            negative => $/ eq 'H';
    }
    method backslash:sym<v>($/) {
        make EnumCharList.new:
            chars => "\x[0a,0b,0c,0d,85,2028,2029]\r\n",
            negative => $/ eq 'V';
    }
    method backslash:sym<r>($/) {
        make EnumCharList.new: chars => "\r", negative => $/ eq 'R';
    }
    method backslash:sym<misc>($/) {
        make Literal.new: value => ~$/;
    }

    method assertion:sym<name>($/) {
        my $name = ~$<name>;
        if $<assertion> {
            make Capture.new: name => $name, target => $<assertion>.ast;
        }
        elsif $name eq 'sym' {
            die "Saw <sym>, but enclosing token has no :sym<...>" unless $*SYM;
            make Capture.new: name => 'sym', target =>
                Literal.new: value => $*SYM;
        }
        else {
            make Capture.new: name => $name, target =>
                Subrule.new: name => $name,
                    |(regex-arg => $<nibbler>.ast if $<nibbler>),
                    |(args => $<arglist>.ast if $<arglist>);
        }
    }

    method assertion:sym<method>($/) {
        my $target = $<assertion>.ast;
        make $target ~~ Capture ?? $target.target !! $target;
    }

    method assertion:sym<?>($/) {
        make $<assertion>
            ?? Lookahead.new(:!negative, target => tweak-lookahead($<assertion>.ast))
            !! AnchorPass.new;
    }

    method assertion:sym<!>($/) {
        make $<assertion>
            ?? Lookahead.new(:negative, target => tweak-lookahead($<assertion>.ast))
            !! AnchorFail.new;
    }

    method assertion:sym<?{ }>($/) {
        make CodeBlock.new: type => PositiveCodeAssertion, statement => $<codeblock>.ast;
    }
    method assertion:sym<!{ }>($/) {
        make CodeBlock.new: type => NegativeCodeAssertion, statement => $<codeblock>.ast;
    }

    sub tweak-lookahead($ast is copy) {
        if $ast ~~ Capture {
            $ast .= target;
        }
        if $ast ~~ Subrule && $ast.name eq 'before' {
            $ast .= regex-arg;
        }
        return $ast;
    }

    method assertion:sym<[>($/) {
        make $<cclass_elem>.ast;
    }

    method cclass_elem($/) {
        my @chars;
        my @alts;
        for $<charspec> -> $c {
            if $c[1] {
                if $c[0]<cclass_backslash> || $c[1]<cclass_backslash> {
                    die "Cannot have cclass char range using backslash sequence";
                }
                my $from := ord(~$c[0]);
                my $to := ord(~$c[1]);
                if $from > $to {
                    die "Backwards range $from..$to is illegal";
                }
                loop (my $o = $from; $o <= $to; $o++) {
                    push @chars, chr($o);
                }
            }
            elsif $c[0]<cclass_backslash> {
                my $cc = $c[0]<cclass_backslash>.ast;
                if $cc ~~ EnumCharList && !$cc.negative {
                    push @chars, $cc.chars;
                }
                else {
                    push @alts, $cc;
                }
            }
            else {
                push @chars, ~$c[0];
            }
        }
        if @chars {
            push @alts, EnumCharList.new: chars => @chars.join;
        }
        if $<sign> eq '-' {
            if @alts == 1 && @alts[0] ~~ EnumCharList {
                make EnumCharList.new: chars => @alts[0].chars, :negative; 
            }
        }
        else {
            make @alts == 1
                ?? @alts[0]
                !! Alt.new(alternatives => @alts);
        }
    }

    method cclass_backslash:sym<s>($/) {
        make BuiltinCharClass.new: class => SpaceChars, negative => $/ eq 'S';
    }
    method cclass_backslash:sym<d>($/) {
        make BuiltinCharClass.new: class => DigitChars, negative => $/ eq 'D';
    }
    method cclass_backslash:sym<w>($/) {
        make BuiltinCharClass.new: class => WordChars, negative => $/ eq 'W';
    }
    method cclass_backslash:sym<n>($/) {
        make BuiltinCharClass.new: class => NewlineChars, negative => $/ eq 'N';
    }
    method cclass_backslash:sym<h>($/) {
        make EnumCharList.new:
            chars => "\x[09,20,a0,1680,180e,2000,2001,2002,2003,2004,2005,2006,2007,2008,2009,200a,202f,205f,3000]",
            negative => $/ eq 'H';
    }
    method cclass_backslash:sym<v>($/) {
        make EnumCharList.new:
            chars => "\x[0a,0b,0c,0d,85,2028,2029]\r\n",
            negative => $/ eq 'V';
    }
    method cclass_backslash:sym<r>($/) {
        make EnumCharList.new: chars => "\r", negative => $/ eq 'R';
    }
    method cclass_backslash:sym<any>($/) {
        make EnumCharList.new: chars => ~$/;
    }

    method arglist($/) {
        make $<arg>.map(*.ast).list;
    }

    method value:sym<string>($/) {
        make StrValue.new: value => $<single-quote-string>.ast;
    }
    method value:sym<integer>($/) {
        make IntValue.new: value => $/.Int;
    }

    method code:sym<assignment>($/) {
        make DynamicAssignment.new: variable-name => ~$<var>, value => $<value>.ast;
    }
    method code:sym<op>($/) {
        make TestStr.new: left => $<left>.ast, op => ~$<op>, right => $<right>.ast;
    }
    method code:sym<lookup>($/) {
        make $<variable>.ast;
    }

    method operand($/) {
        make $<variable> ?? $<variable>.ast !! $<value>.ast;
    }

    method variable($/) {
        make DynamicLookup.new: variable-name => ~$/;
    }
}
