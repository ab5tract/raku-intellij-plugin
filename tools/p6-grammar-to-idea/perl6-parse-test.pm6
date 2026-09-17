grammar MAIN {
    token TOP {
        <statementlist>
    }

	## Lexer stuff

    token apostrophe {
        <[ ' \- ]>
    }

    token identifier {
        <.ident> [ <.apostrophe> <.ident> ]*
    }

    token name {
        [
        | <identifier> <morename>*
        | <morename>+
        ]
    }

    token deflongname {
        <name> <colonpair>*
    }

    token subshortname {
        <desigilname>
    }

    token sublongname {
        <subshortname> <sigterm>?
    }

    token deftermnow { <defterm> }

    token module_name {
        <longname>
        [ <?[[]> '[' ~ ']' <arglist> ]?
    }

    token end_keyword {
        » <!before <[ \( \\ ' \- ]> || \h* '=>'>
    }

    token end_prefix {
        <.end_keyword> \s*
    }

    token spacey { <?[\s#]> }

    token desigilname {
        [
        | <?before <.sigil> <.sigil> > <variable>
        | <?sigil>
#            [ <?{ $*IN_DECL }> <.typed_panic: 'X::Syntax::Variable::IndirectDeclaration'> ]?
            <variable>
        | <longname>
        ]
    }

    token unsp {
        \\ <?before \s | '#'>
        [
        | <.vws>
        | <.unv>
        | <.unsp>
        ]*
    }

    token unv {
        [
        | \h+
        | \h* <.comment>
#        | <?before \h* '=' [ \w | '\\'] > ^^ <.pod_content_toplevel>
        ]
    }

    token vnum {
        \w+ | '*'
    }

    token version {
        <?before 'v'\d+\w*> 'v' $<vstr>=[<vnum>+ % '.' '+'?]
        <!before '-'|\'>
    }

    ## Top-level rules

    rule statementlist {
        <.ws>
        [
        | $
        | <?before <[\)\]\}]>>
        | [ <statement> <.eat_terminator> ]*
        ]
    }

    rule semilist {
        ''
        [
        | <?[)\]}]>
        | [<statement><.eat_terminator> ]*
        ]
    }

    rule sequence {
        ''
        [
        | <?before <.[)\]}]> >
        | [<statement><.eat_terminator> ]*
        ]
    }

    token label {
        <identifier> ':' <?[\s]> <.ws>
    }

    token statement {
        <!before <[\])}]> | $ >
        <!stopper>
        [
        | <label> <statement>
        | <statement_control>
        | <EXPR>
            [
#            || <?MARKED('endstmt')>
            || <.ws> <statement_mod_cond> <statement_mod_loop>?
            || <.ws> <statement_mod_loop>
            ]?
        | <?[;]>
        | <?stopper>
#        | {} <.panic: "Bogus statement">
        ]
    }

    token eat_terminator {
        || ';'
#        || <?MARKED('endstmt')> <.ws>
        || <?before ')' | ']' | '}' >
        || $
        || <?stopper>
#        || <?before [if|while|for|loop|repeat|given|when] » > { $/.'!clear_highwater'(); self.typed_panic( 'X::Syntax::Confused', reason => "Missing semicolon" ) }
#        || { $/.typed_panic( 'X::Syntax::Confused', reason => "Confused" ) }
    }

    token lambda { '->' | '<->' }

    proto token terminator { <...> }

    token terminator:sym<;> { <?[;]> }
    token terminator:sym<)> { <?[)]> }
#    token terminator:sym<]> { <?[\]]> }
    token terminator:sym<}> { <?[}]> }
#    token terminator:sym<ang> { <?[>]> <?{ $*IN_REGEX_ASSERTION }> }
    token terminator:sym<if>     { 'if'     <.kok> }
    token terminator:sym<unless> { 'unless' <.kok> }
    token terminator:sym<while>  { 'while'  <.kok> }
    token terminator:sym<until>  { 'until'  <.kok> }
    token terminator:sym<for>    { 'for'    <.kok> }
    token terminator:sym<given>  { 'given'  <.kok> }
    token terminator:sym<when>   { 'when'   <.kok> }
    token terminator:sym<with>   { 'with'   <.kok> }
    token terminator:sym<without> { 'without' <.kok> }
    token terminator:sym<arrow>  { '-->' }

    ## Statement control

    proto rule statement_control { <...> }

    rule statement_control:sym<need> {
        <sym>
        [
        | <version>
        | <module_name>
        ] +% ','
    }

    token statement_control:sym<import> {
        <sym> <.ws>
        <module_name> [ <.spacey> <arglist> ]? <.ws>
    }

    token statement_control:sym<no> {
        <sym> <.ws>
        <module_name> [ <.spacey> <arglist> ]?
        <.ws>
    }

    rule statement_control:sym<require> {
        <sym>
        [
        | <module_name>
        | <file=.variable>
        | <!sigil> <file=.term>
        ]
        <EXPR>?
    }

    rule statement_control:sym<given> {
        <sym><.kok> <xblock(1)>
    }
    rule statement_control:sym<when> {
        <sym><.kok> <xblock>
    }
    rule statement_control:sym<default> {
        <sym><.kok> <block>
    }

    rule statement_control:sym<CATCH> {<sym> <block(1)> }
    rule statement_control:sym<CONTROL> {<sym> <block(1)> }
    rule statement_control:sym<QUIT> {<sym> <block(1)> }

    proto token statement_prefix { <...> }
    token statement_prefix:sym<BEGIN>   { <sym><.kok> <blorst> }
    token statement_prefix:sym<COMPOSE> { <sym><.kok> <blorst> }
    token statement_prefix:sym<TEMP>    { <sym><.kok> <blorst> }
    token statement_prefix:sym<CHECK>   { <sym><.kok> <blorst> }
    token statement_prefix:sym<INIT>    { <sym><.kok> <blorst> }
    token statement_prefix:sym<ENTER>   { <sym><.kok> <blorst> }
    token statement_prefix:sym<FIRST>   { <sym><.kok> <blorst> }

    token statement_prefix:sym<END>   { <sym><.kok> <blorst> }
    token statement_prefix:sym<LEAVE> { <sym><.kok> <blorst> }
    token statement_prefix:sym<KEEP>  { <sym><.kok> <blorst> }
    token statement_prefix:sym<UNDO>  { <sym><.kok> <blorst> }
    token statement_prefix:sym<NEXT>  { <sym><.kok> <blorst> }
    token statement_prefix:sym<LAST>  { <sym><.kok> <blorst> }
    token statement_prefix:sym<PRE>   { <sym><.kok> <blorst> }
    token statement_prefix:sym<POST>  { <sym><.kok> <blorst> }
    token statement_prefix:sym<CLOSE> { <sym><.kok> <blorst> }

    token statement_prefix:sym<race> {
        <sym><.kok>
        [
        | <?before 'for' <.kok>> <for=.statement_control>
        | <blorst>
        ]
    }
    token statement_prefix:sym<hyper> {
        <sym><.kok>
        [
        | <?before 'for' <.kok>> <for=.statement_control>
        | <blorst>
        ]
    }
    token statement_prefix:sym<lazy> {
        <sym><.kok>
        [
        | <?before 'for' <.kok>> <for=.statement_control>
        | <blorst>
        ]
    }
    token statement_prefix:sym<eager>   { <sym><.kok> <blorst> }
    token statement_prefix:sym<sink>    { <sym><.kok> <blorst> }
    token statement_prefix:sym<try>     { <sym><.kok> <blorst> }
    token statement_prefix:sym<quietly> { <sym><.kok> <blorst> }
    token statement_prefix:sym<gather>  { <sym><.kok> <blorst> }
    token statement_prefix:sym<once>    { <sym><.kok> <blorst> }
    token statement_prefix:sym<start>   { <sym><.kok> <blorst> }
    token statement_prefix:sym<supply>  { <sym><.kok> <blorst> }
    token statement_prefix:sym<react>   { <sym><.kok> <blorst> }
    token statement_prefix:sym<do>      { <sym><.kok> <blorst> }
    token statement_prefix:sym<DOC>     {
        <sym><.kok> $<phase>=['BEGIN' || 'CHECK' || 'INIT']<.end_keyword><.ws>
        <blorst>
    }

    ## Statement modifiers

    proto rule statement_mod_cond { <...> }

    proto rule statement_mod_loop { <...> }

    ## Terms

    proto token term { <...> }
    token term:sym<fatarrow>           { <fatarrow> }
    token term:sym<colonpair>          { <colonpair> }
    token term:sym<variable>           { <variable> }
    token term:sym<package_declarator> { <package_declarator> }
    token term:sym<scope_declarator>   { <scope_declarator> }
    token term:sym<routine_declarator> { <routine_declarator> }
    token term:sym<multi_declarator>   { <?before 'multi'|'proto'|'only'> <multi_declarator> }
    token term:sym<regex_declarator>   { <regex_declarator> }
    token term:sym<circumfix>          { <circumfix> }
    token term:sym<statement_prefix>   { <statement_prefix> }
    token term:sym<**>                 { <sym> }
    token term:sym<*>                  { <sym> }
    token term:sym<lambda>             { <?lambda> <pblock> }
    token term:sym<type_declarator>    { <type_declarator> }
    token term:sym<value>              { <value> }
    token term:sym<!!>                 { '!!' <?before \s> }
    token term:sym<∞>                  { <sym> }

    token term:sym<::?IDENT> {
        $<sym> = [ '::?' <identifier> ] »
    }

    token contextualizer {
        [
        | <sigil> '(' ~ ')'    <coercee=sequence>
        | <sigil> <?[ \[ \{ ]> <coercee=circumfix>
        ]
    }

    proto token twigil { <...> }
    token twigil:sym<.> { <sym> <?before \w> }
    token twigil:sym<!> { <sym> <?before \w> }
    token twigil:sym<^> { <sym> <?before \w> }
    token twigil:sym<:> { <sym> <?before \w> }
    token twigil:sym<*> { <sym> <?before \w> }
    token twigil:sym<?> { <sym> <?before \w> }
    token twigil:sym<=> { <sym> <?before \w> }
    token twigil:sym<~> { <sym> <?before \w> }

    proto token package_declarator { <...> }
    token package_declarator:sym<package> {
        <sym><.kok> <package_def>
    }
    token package_declarator:sym<module> {
        <sym><.kok> <package_def>
    }
    token package_declarator:sym<class> {
        <sym><.kok> <package_def>
    }
    token package_declarator:sym<grammar> {
        <sym><.kok> <package_def>
    }
    token package_declarator:sym<role> {
        <sym><.kok> <package_def>
    }
    token package_declarator:sym<knowhow> {
        <sym><.kok> <package_def>
    }
    token package_declarator:sym<native> {
        <sym><.kok> <package_def>
    }
    token package_declarator:sym<slang> {
        <sym><.kok> <package_def>
    }

    ## Terms

    token term:sym<self> { <sym> <.end_keyword> }
    token term:sym<now> { <sym> <.tok> }
    token term:sym<time> { <sym> <.tok> }
    token term:sym<empty_set> { '∅' <!before <[ \( \\ ' \- ]> || \h* '=>'> }
    token term:sym<???> { <sym> <args> }
    token term:sym<!!!> { <sym> <args> }
    token term:sym<dotty> { <dotty> }
    token term:sym<capterm> { <capterm> }
    token term:sym<onlystar> { '{*}' <?ENDSTMT> }

    token args {
        [
        | '(' ~ ')' <semiarglist>
        | <.unsp> '(' ~ ')' <semiarglist>
        | [ \s <arglist> ]
        | <?>
        ]
    }

    token semiarglist {
        <arglist>+ % ';'
        <.ws>
    }

    proto token value { <...> }
    token value:sym<quote>  { <quote> }
    token value:sym<number> { <number> }
    token value:sym<version> { <version> }

    proto token number { <...> }
    token number:sym<numish>   { <numish> }

    token signed-number { <sign> <number> }

    token numish {
        [
        | 'NaN' >>
        | <integer>
        | <dec_number>
        | <rad_number>
        | <rat_number>
        | <complex_number>
        | 'Inf' >>
        | $<uinf>='∞'
#        | <unum=:No+:Nl>
        ]
    }

    token dec_number {
        [
        | $<coeff> = [               '.' <frac=.decint> ] <escale>?
        | $<coeff> = [ <int=.decint> '.' <frac=.decint> ] <escale>?
        | $<coeff> = [ <int=.decint>                    ] <escale>
        ]
    }

    token signed-integer { <sign> <integer> }

    token radint {
        <integer>
    }

    token escale { <[Ee]> <sign> <decint> }

    token sign { '+' | '-' | '−' | '' }
}

grammar EXPR {
}

grammar Signature {
}

grammar Q {
}

grammar Regex {
}
