grammar P6GrammarToIdea::Parser {
    rule TOP {
        '' [<grammar> ]*
        [ $ || <.panic('Confused')> ]
    }

    rule grammar {
        'grammar' <name> '{'
            [<production> ]*
        '}'
    }

    rule production {
        :my $*PROTO = False;
        :my $*SYM;
        [$<proto>='proto' { $*PROTO = True } ]?
        $<kind>=< token rule >
        [
        || <name>[<!{$*PROTO}>':sym<'$<sym>=[<-[>]>+]'>'{$*SYM = ~$<sym>}]?
            [ '(' ~ ')' <parameter>+ % [<.ws> ',' ] ]?
            '{'
            [
            || <!{$*PROTO}> <nibbler>
            || <?{$*PROTO}> ['<...>' || <.panic('Body of proto must be <...>')>]
            || <.panic('Syntax error in production rule')>
            ]
            '}'
        || <.panic('Malformed production rule')>
        ]
    }

    token parameter {
        '$*' <name>
    }

    token nibbler {
        <.ws>
        [
          <!rxstopper>
          [
          |  '||'
          |  '|'
          |  '&&'
          |  '&'
          ] <.ws>
        ]?
        <seqalt>
        [
        || <?infixstopper>
        || $$ <.panic('Regex not terminated')>
        || (\W) {} <.panic("Unrecognized metachar: $/[0]")>
        || <.panic('Regex not terminated')>
        ]
    }

    rule seqalt {
        <alt>+ % [ '||' ]
    }

    rule alt {
        <termish>+ % [ '|' ]
    }

    rule termish {
        [<quantified-atom> ]+
    }

    rule quantified-atom {
        <atom>
        [
            <quantifier>
            [ <.ws> <separator> ]?
        ]?
    }

    rule separator {
        '%' <quantified-atom>
    }

    token atom {
        <metachar>
    }

    proto token quantifier {*}
    token quantifier:sym<?> { <sym> }
    token quantifier:sym<*> { <sym> }
    token quantifier:sym<+> { <sym> }

    proto token metachar {*}
    token metachar:sym<.> { <sym> }
    token metachar:sym<$> { <sym> }
    token metachar:sym<$$> { <sym> }
    token metachar:sym<^^> { <sym> }
    rule metachar:sym<group> {
        '[' <nibbler> ']'
    }
    token metachar:sym<assert> {
        '<' ~ '>' <assertion>
    }
    token metachar:sym<'> {
        <single-quote-string>
    }
    token metachar:sym<var> {
        '$<' $<name>=[<-[>]>+] '>'
        <.ws> '=' <.ws>
        <quantified-atom>
    }
    token metachar:sym<bs> { \\ <backslash> }
    token metachar:sym<lwb> { $<sym>=['<<'|'«'] }
    token metachar:sym<rwb> { $<sym>=['>>'|'»'] }
    token metachar:sym<~> {
        <sym>
        <.ws> <GOAL=.quantified-atom>
        <.ws> <EXPR=.quantified-atom>
    }
    token metachar:sym<variable> {
        '$*' <.ident>
    }
    token metachar:sym<:my> {
        <.sym> <.ws> $<var>=['$*'<.ident>] <.ws> '=' <.ws> <value> <.ws> ';'
    }
    token metachar:sym<{> {
        <codeblock>
    }

    rule codeblock {
        '{'
        [ <code> || <.panic("Code expression not understood or supported")> ]
        '}'
    }

    token single-quote-string {
        "'"
        <single-quote-string-part>*
        [ "'" || <.panic: "Cannot find closing '"> ]
    }

    token single-quote-string-part {
        <!before "'">
        [
        || '\\' $<esc>=.
        || <-[\\']>+
        ]
    }

    proto token backslash { <...> }
    token backslash:sym<s> { $<sym>=[<[sS]>] }
    token backslash:sym<d> { $<sym>=[<[dD]>] }
    token backslash:sym<w> { $<sym>=[<[wW]>] }
    token backslash:sym<n> { $<sym>=[<[nN]>] }
    token backslash:sym<h> { $<sym>=[<[hH]>] }
    token backslash:sym<v> { $<sym>=[<[vV]>] }
    token backslash:sym<r> { $<sym>=[<[rR]>] }
    token backslash:sym<misc> { \W }

    proto token assertion {*}
    token assertion:sym<name> {
        <name>
            [
            | <?before '>'>
            | '=' <assertion>
            | ':' <arglist>
            | '(' <arglist> ')'
            | <.normspace> <nibbler>
            ]?
    }
    token assertion:sym<method> {
        '.' <assertion>
    }
    token assertion:sym<?> {
        '?' [ <?before '>' > | <assertion> ]
    }
    token assertion:sym<!> {
        '!' [ <?before '>' > | <assertion> ]
    }
    token assertion:sym<[> {
        <?before '['|'+'|'-'>
        <cclass_elem>
    }
    token assertion:sym<?{ }> {
        '?' <?before '{'> <codeblock>
    }
    token assertion:sym<!{ }> {
        '!' <?before '{'> <codeblock>
    }

    token cclass_elem {
        :my $*key;
        $<sign>=['+'|'-'|<?>]
        <.normspace>?
        [
        | '[' $<charspec>=(
                  || \s* ( '\\' <cclass_backslash> || (<-[\]\\]>) )
                     [
                         \s* '..' \s*
                         ( '\\' <cclass_backslash> || (<-[\]\\]>) )
                     ]**0..1
              )*
          \s* ']'
#        | $<name>=<identifier>
        ]
        <.normspace>?
    }

    proto token cclass_backslash {*}
    token cclass_backslash:sym<nyi> {
        <[bBeEfFtToOxXcC0]> {}
        <.panic("Backslash sequence $/ not implemented")>
    }
    token cclass_backslash:sym<s> { $<sym>=[<[sS]>] }
    token cclass_backslash:sym<d> { $<sym>=[<[dD]>] }
    token cclass_backslash:sym<w> { $<sym>=[<[wW]>] }
    token cclass_backslash:sym<n> { $<sym>=[<[nN]>] }
    token cclass_backslash:sym<h> { $<sym>=[<[hH]>] }
    token cclass_backslash:sym<v> { $<sym>=[<[vV]>] }
    token cclass_backslash:sym<r> { $<sym>=[<[rR]>] }
    token cclass_backslash:sym<any> {
        .
    }

    rule arglist { '' <arg=.operand> +% [',' ] }

    proto token value {*}
    token value:sym<string> {
        <single-quote-string>
    }
    token value:sym<integer> {
        \d+
    }

    proto token code {*}
    rule code:sym<assignment> {
        $<var>=['$*'<.ident>] '=' <value>
    }
    rule code:sym<op> {
        <left=.operand> $<op>=< le eq ne > <right=.operand>
    }
    rule code:sym<lookup> {
        <variable>
    }

    token operand {
        <variable> | <value>
    }

    token variable {
        '$*' <.ident> <!before <.ws> '='>
    }

    token name {
        <ident>+ % <[-']>
    }

    method panic($message) {
        die "$message near " ~ self.orig.substr(self.pos, 30).perl
    }

    regex infixstopper {
        [
        | <?before <.[\) \} \]]> >
        | <?before '>' <.-[>]> >
        | <?rxstopper>
        ]
    }

    token rxstopper { $ }

    token ws {
        | <!ww>
        | [
            | \h+
            | ^^ \h* '#' \N*
            | \n
          ]*
    }

    token normspace { <?[\s#]> <.ws> }
}
