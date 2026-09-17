grammar MAIN {
    token TOP {
        :my $*GOAL = '';
        :my $*IN_DECL = '';
        :my $*IN_REGEX_ASSERTION = 0;
        :my $*QSIGIL = '';
        :my $*DELIM = '';
        :my $*LEFTSIGIL = '';
        :my $*IN_META = '';
        :my $*IN_REDUCE = 0;
        :my $*IN_UNIT = 0;
        <.scope-push>
        <.statementlist>
        [
        || $
        || [
               # Try to recover from excess closing }
               <.start-token('BAD_CHARACTER')> '}' <.end-token('BAD_CHARACTER')>
               <.ws>?
               <.statementlist>
           ]*
           <.bogus_end>?
        ]
        <.scope-pop>
    }

    token bogus_end {
        <.start-token('BAD_CHARACTER')>
        .+
        <.end-token('BAD_CHARACTER')>
    }

    ## Lexer stuff

    token apostrophe {
        <[ ' \- ]>
    }

    token ident {
        [<.alpha> || '_'] \w*
    }

    token identifier {
        <.ident> [ <.apostrophe> <.ident> ]*
    }

    token name {
        [
        || <.identifier> <.morename>*
        || <.morename>+
        ]
    }

    token morename {
        :my $*QSIGIL = '';
        '::'
        [
        || <.identifier>
        ]?
    }

    token defterm {
        <.start-element('TERM_DEFINITION')>
        <.start-token('TERM')>
        <.start-symbol>
        <.identifier>
        <.end-symbol('term')>
        <.end-token('TERM')>
        <.end-element('TERM_DEFINITION')>
    }

    token longname_colonpairs {
        [
            <?before [':' [<.alpha> || <[<[«]>]]>
            <.start-token('LONGNAME_COLONPAIR')> <?> <.end-token('LONGNAME_COLONPAIR')>
            <.colonpair>
        ]*
    }

    token routine_name {
        <.start-element('LONG_NAME')>
        <.start-token('ROUTINE_NAME')>
        <.name>
        <.end-token('ROUTINE_NAME')>
        <.longname_colonpairs>
        <.end-element('LONG_NAME')>
    }

    token method_name {
        <.start-element('LONG_NAME')>
        <.start-token('ROUTINE_NAME')>
        [
        || <[ ! ^ ]> <.name>?
        || <.name>
        ]
        <.end-token('ROUTINE_NAME')>
        <.longname_colonpairs>
        <.end-element('LONG_NAME')>
    }

    token module_name {
        <.start-element('MODULE_NAME')>
        <.start-element('LONG_NAME')>
        <.start-token('NAME')>
        <.name>
        <.end-token('NAME')>
        <.longname_colonpairs>
        <.end-element('LONG_NAME')>
        <.end-element('MODULE_NAME')>
    }

    token end_keyword {
        >>
        <!before <[ \( \\ ' \- ]> || \h* '=>'>
    }

    token end_prefix {
        <.end_keyword> <.start-token('WHITE_SPACE')> \s* <.end-token('WHITE_SPACE')>
    }

    token spacey { <?[\s]> || <?[#]> }

    token kok {
        <.end_keyword>
        <?before \s || \# || $ > <.ws>
    }

    token tok {
        <.end_keyword>
    }

    token ENDSTMT {
        [
        || <?before [\h* $$ <.ws> <?MARKER('endstmt')>]>
        || <?before [<.unv>? $$ <.ws> <?MARKER('endstmt')>]>
        ]?
    }

    token ws {
        <!ww>
        [
        || <.start-token('WHITE_SPACE')>
           [\r\n || \v]
           <.end-token('WHITE_SPACE')>
           <.heredoc>
        || <.unv>
        || <.unsp>
        ]*
    }

    token heredoc {
        :my $*Q_Q = 0;
        :my $*Q_QQ = 0;
        :my $*Q_BACKSLASH = 0;
        :my $*Q_QBACKSLASH = 0;
        :my $*Q_QQBACKSLASH = 0;
        :my $*Q_CLOSURES = 0;
        :my $*Q_SCALARS = 0;
        :my $*Q_ARRAYS = 0;
        :my $*Q_HASHES = 0;
        :my $*Q_FUNCTIONS = 0;
        :my $*Q_TO = 0;
        :my $*DELIM = '';
        [
            <.dequeue-heredoc>
            <.start-element('HEREDOC')>
            <.start-token('HEREDOC')> <?> <.end-token('HEREDOC')>
            <.quote_nibbler>
            [
                <.start-token('STRING_LITERAL_QUOTE_CLOSE')>
                <.stopper>
                <.end-token('STRING_LITERAL_QUOTE_CLOSE')>
            ]?
            <.end-element('HEREDOC')>
        ]*
    }

    token unsp {
        <.start-token('UNSP_WHITE_SPACE')>
        '\\' <?before \s || '#'>
        <.end-token('UNSP_WHITE_SPACE')>
        [
        || <.vws>
        || <.unv>
        || <.unsp>
        ]*
    }

    token vws {
        <.start-token('VERTICAL_WHITE_SPACE')>
        \v+
        <.end-token('VERTICAL_WHITE_SPACE')>
    }

    token unv {
        [
        || <.comment>
        || <?before [\h* '=' [ \w || '\\']]> ^^ <.pod_content_toplevel>
        || <.start-token('UNV_WHITE_SPACE')> \h+ <.end-token('UNV_WHITE_SPACE')>
           <.comment>?
        ]
    }

    token comment {
        [
        || <.pre-comment-multi>
        || <.pre-comment>
        || <.post-comment-multi>
        || <.post-comment>
        || <.multiline-comment>
        || <.plain-comment>
        ]
    }

    token pre-comment-multi {
        <?before '#|' <.opener>>
        :my $*STARTER = '';
        :my $*STOPPER = '';
        :my $*ALT_STOPPER = '';
        :my $*DELIM = '';
        <.start-element('POD_PRE_COMMENT')>
        <.start-token('COMMENT_STARTER')>
        '#|'
        <.end-token('COMMENT_STARTER')>
        <.peek-delimiters>
        <.start-token('COMMENT_QUOTE_OPEN')>
        $*STARTER
        <.end-token('COMMENT_QUOTE_OPEN')>
        <.multiline-comment-nibbler>
        [
            <.start-token('COMMENT_QUOTE_CLOSE')>
            <.stopper>
            <.end-token('COMMENT_QUOTE_CLOSE')>
        ]?
        <.end-element('POD_PRE_COMMENT')>
    }

    token pre-comment {
        <?before '#|' \s>
        <.start-element('POD_PRE_COMMENT')>
        <.start-token('COMMENT_STARTER')>
        '#|'
        <.end-token('COMMENT_STARTER')>
        <.start-token('COMMENT')>        
        \N*
        <.end-token('COMMENT')>
        <.end-element('POD_PRE_COMMENT')>
    }

    token post-comment {
        <?before '#=' \s>
        <.start-element('POD_POST_COMMENT')>
        <.start-token('COMMENT_STARTER')>
        '#='
        <.end-token('COMMENT_STARTER')>
        <.start-token('COMMENT')>
        \N*
        <.end-token('COMMENT')>
        <.end-element('POD_POST_COMMENT')>
    }

    token post-comment-multi {
        <?before '#=' <.opener>>
        :my $*STARTER = '';
        :my $*STOPPER = '';
        :my $*ALT_STOPPER = '';
        :my $*DELIM = '';
        <.start-element('POD_POST_COMMENT')>
        <.start-token('COMMENT_STARTER')>
        '#='
        <.end-token('COMMENT_STARTER')>
        <.peek-delimiters>
        <.start-token('COMMENT_QUOTE_OPEN')>
        $*STARTER
        <.end-token('COMMENT_QUOTE_OPEN')>
        <.multiline-comment-nibbler>
        [
            <.start-token('COMMENT_QUOTE_CLOSE')>
            <.stopper>
            <.end-token('COMMENT_QUOTE_CLOSE')>
        ]?
        <.end-element('POD_POST_COMMENT')>
    }

    token multiline-comment {
        <?before '#`' <.has-delimiter>>
        :my $*STARTER = '';
        :my $*STOPPER = '';
        :my $*ALT_STOPPER = '';
        :my $*DELIM = '';
        <.start-element('COMMENT')>
        <.start-token('COMMENT_STARTER')>
        '#`'
        <.end-token('COMMENT_STARTER')>
        <.peek-delimiters>
        <.start-token('COMMENT_QUOTE_OPEN')>
        $*STARTER
        <.end-token('COMMENT_QUOTE_OPEN')>
        <.multiline-comment-nibbler>
        [
            <.start-token('COMMENT_QUOTE_CLOSE')>
            <.stopper>
            <.end-token('COMMENT_QUOTE_CLOSE')>
        ]?
        <.end-element('COMMENT')>
    }

    token multiline-comment-nibbler {
        [
            <!stopper>
            [
            || <.start-token('COMMENT_QUOTE_OPEN')> <.starter> <.end-token('COMMENT_QUOTE_OPEN')>
               <.multiline-comment-nibbler>
               [<.start-token('COMMENT_QUOTE_CLOSE')> <.stopper> <.end-token('COMMENT_QUOTE_CLOSE')>]?
            || <.start-token('COMMENT')> [\w+ || \d+ || \s+]+ <.end-token('COMMENT')>
            || <.start-token('COMMENT')> . <.end-token('COMMENT')>
            ]
        ]*
    }

    token plain-comment {
        <.start-element('COMMENT')>
        <.start-token('COMMENT_STARTER')>
        '#'
        <.end-token('COMMENT_STARTER')>
        <.start-token('COMMENT')>
        \N*
        <.end-token('COMMENT')>
        <.end-element('COMMENT')>
    }

    token pod_content_toplevel {
        <.pod_block>
    }

    token pod_block {
        || <.pod_block_finish>
        || <.pod_block_delimited>
        || <.pod_block_paragraph>
        || <.pod_block_abbreviated>
    }

    token pod_block_finish {
        ^^
        <?before [\h* [ [ ['=begin' || '=for' ] \h+ 'finish' ] || '=finish' ] ]>
        <.start-element('POD_BLOCK_FINISH')>
        <.start-token('POD_WHITESPACE')> \h* <.end-token('POD_WHITESPACE')>
        [
        || <.start-token('POD_DIRECTIVE')> '=begin' <.end-token('POD_DIRECTIVE')>
           <.start-token('POD_WHITESPACE')> \h+ <.end-token('POD_WHITESPACE')>
           <.start-token('POD_TYPENAME')> 'finish' <.end-token('POD_TYPENAME')>
        || <.start-token('POD_DIRECTIVE')> '=for' <.end-token('POD_DIRECTIVE')>
           <.start-token('POD_WHITESPACE')> \h+ <.end-token('POD_WHITESPACE')>
           <.start-token('POD_TYPENAME')> 'finish' <.end-token('POD_TYPENAME')>
        || <.start-token('POD_DIRECTIVE')> '=finish' <.end-token('POD_DIRECTIVE')>
        ]
        <.pod_newline>?
        <.start-token('POD_FINISH_TEXT')> .* <.end-token('POD_FINISH_TEXT')>
        <.end-element('POD_BLOCK_FINISH')>
    }

    token pod_block_delimited {
        ^^
        <?before [\h* '=begin']>
        :my $*POD_WS_PREFIX = 0;
        :my $*POD_CODE_BLOCK = '';
        <.start-element('POD_BLOCK_DELIMITED')>
        <.start-token('POD_REMOVED_WHITESPACE')>
        <.pod-ws-start>
        \h*
        <.pod-ws-commit>
        <.end-token('POD_REMOVED_WHITESPACE')>
        <.start-token('POD_DIRECTIVE')> '=begin' <.end-token('POD_DIRECTIVE')>
        [
            <?before [\h+ <.ident>]>
            <.start-token('POD_WHITESPACE')> \h+ <.end-token('POD_WHITESPACE')>
            <.pod_code_check>
            <.start-token('POD_TYPENAME')> <.ident> <.end-token('POD_TYPENAME')>
            <.pod_configuration>?
            [
                <.pod_newline>
                [
                    <.pod_block_content>
                    [
                        <?before [\h* '=end']>
                        <.start-token('POD_WHITESPACE')> \h* <.end-token('POD_WHITESPACE')>
                        <.start-token('POD_DIRECTIVE')> '=end' <.end-token('POD_DIRECTIVE')>
                        [
                            <?before [\h+ <.ident>]>
                            <.start-token('POD_WHITESPACE')> \h+ <.end-token('POD_WHITESPACE')>
                            <.start-token('POD_TYPENAME')> <.ident> <.end-token('POD_TYPENAME')>
                            <.pod_newline>?
                        ]?
                    ]?
                ]?
            ]?
        ]?
        <.end-element('POD_BLOCK_DELIMITED')>
    }

    token pod_block_content {
        [
            # For code, we are very strict about the whitespace prior to the =end
            # and also what is being closed. We're a bit sloppier with the rest, to
            # allow for better recovery during editing.
            [
            || <!{$*POD_CODE_BLOCK}> <!before \h* '=end' [\s || $] || $>
            || <?{$*POD_CODE_BLOCK}> <!before [
                 <.pod_removed_whitespace> '=end' \h+
                 [
                 || <?{$*POD_CODE_BLOCK eq 'code'}> 'code'
                 || <?{$*POD_CODE_BLOCK eq 'input'}> 'input'
                 || <?{$*POD_CODE_BLOCK eq 'output'}> 'output'
                 ]
                 >>
               ] || $>
            ]
            <.start-token('POD_HAVE_CONTENT')> <?> <.end-token('POD_HAVE_CONTENT')>
            [
            || <!{$*POD_CODE_BLOCK}> <.pod_block>
            || <.pod_removed_whitespace>
               [
               || [<?[\h]> || <?{$*POD_CODE_BLOCK}>]
                  <.start-token('POD_CODE')> \N+ <.end-token('POD_CODE')>
               || [
                  || <.start-token('POD_TEXT')>
                     [\h+ || \d+ || <[a..z]>+ || <!before <[A..Z]> <[<«]>> \N]+
                     <.end-token('POD_TEXT')>
                  || <.pod_formatting_code>
                  ]+
               ]
               <.pod_newline>?
            || <.pod_newline>
            ]
        ]*
    }

    token pod_block_paragraph {
        ^^
        <?before [\h* '=for']>
        :my $*POD_WS_PREFIX = 0;
        :my $*POD_CODE_BLOCK = '';
        <.start-element('POD_BLOCK_PARAGRAPH')>
        <.start-token('POD_REMOVED_WHITESPACE')>
        <.pod-ws-start>
        \h*
        <.pod-ws-commit>
        <.end-token('POD_REMOVED_WHITESPACE')>
        <.start-token('POD_DIRECTIVE')> '=for' <.end-token('POD_DIRECTIVE')>
        [
            <?before [\h+ <.ident>]>
            <.start-token('POD_WHITESPACE')> \h+ <.end-token('POD_WHITESPACE')>
            <.pod_code_check>
            <.start-token('POD_TYPENAME')> <.ident> <.end-token('POD_TYPENAME')>
            <.pod_configuration>?
            [
                <.pod_newline>
                <.pod_para_content>
                <.pod_newline>?
            ]?
        ]?
        <.end-element('POD_BLOCK_PARAGRAPH')>
    }

    token pod_para_content {
        [
            <!before ^^ \h* ['=' || \n || $]>
            <.pod_removed_whitespace>
            [
            || [<?[\h]> || <?{$*POD_CODE_BLOCK}>]
               <.start-token('POD_CODE')> \N+ <.end-token('POD_CODE')>
            || [
               || <.start-token('POD_TEXT')>
                  [\h+ || \d+ || <[a..z]>+ || <!before <[A..Z]> <[<«]>> \N]+
                  <.end-token('POD_TEXT')>
               || <.pod_formatting_code>
               ]+
            ]
            <.pod_newline>?
        ]*
    }

    token pod_block_abbreviated {
        ^^
        <?before [\h* '=' <.ident>]>
        :my $*POD_WS_PREFIX = 0;
        :my $*POD_CODE_BLOCK = '';
        <.start-element('POD_BLOCK_ABBREVIATED')>
        <.start-token('POD_REMOVED_WHITESPACE')>
        <.pod-ws-start>
        \h*
        <.pod-ws-commit>
        <.end-token('POD_REMOVED_WHITESPACE')>
        <.start-token('POD_DIRECTIVE')> '=' <.end-token('POD_DIRECTIVE')>
        <.pod_code_check>
        <.start-token('POD_TYPENAME')> <.ident> <.end-token('POD_TYPENAME')>
        [
            <.start-token('POD_WHITESPACE')> [\h*\n || \h+] <.end-token('POD_WHITESPACE')>
            <.pod_para_content>
            <.pod_newline>?
        ]?
        <.end-element('POD_BLOCK_ABBREVIATED')>
    }

    token pod_formatting_code {
        :my $*STARTER = '';
        :my $*STOPPER = '';
        :my $*ALT_STOPPER = '';
        :my $*DELIM = '';
        :my $*SEPARATED = 0;
        <?before <[A..Z]> <[<«]>>
        [ <?[LX]> { $*SEPARATED = 1 } ]?
        <.start-element('POD_FORMATTED')>
        <.start-token('FORMAT_CODE')> <[A..Z]> <.end-token('FORMAT_CODE')>
        <.peek-delimiters>
        <.start-token('POD_FORMAT_STARTER')>
        $*STARTER
        <.end-token('POD_FORMAT_STARTER')>
        <.pod_formatted_text>
        [
            <.start-token('POD_FORMAT_STOPPER')>
            $*STOPPER
            <.end-token('POD_FORMAT_STOPPER')>
        ]?
        <.end-element('POD_FORMATTED')>
    }

    token pod_formatted_text {
        <.start-element('POD_TEXT')>
        [
        || <.pod_formatting_code>
        || <.start-token('POD_TEXT')>
           [
           || \d+ || \h+ || <[a..z]>+
           || '|' <!{ $*SEPARATED }>
           || <!before '|' || $*STOPPER || <[A..Z]> <[<«]>> \N
           ]+
           <.end-token('POD_TEXT')>
        || <.start-token('POD_FORMAT_SEPARATOR')>
           '|'
           <.end-token('POD_FORMAT_SEPARATOR')>
           { $*SEPARATED = 0 }
        || <.pod_newline>
        ]*
        <.end-element('POD_TEXT')>
    }

    token pod_newline {
        <.start-token('POD_NEWLINE')> \h* \n <.end-token('POD_NEWLINE')>
    }

    token pod_removed_whitespace {
        [
        <?[\h]>
        <.start-token('POD_REMOVED_WHITESPACE')>
        <.pod-eat-removed-ws>
        <.end-token('POD_REMOVED_WHITESPACE')>
        ]?
    }

    token pod_code_check {
        || <?before 'code'> { $*POD_CODE_BLOCK = 'code' }
        || <?before 'input'> { $*POD_CODE_BLOCK = 'input' }
        || <?before 'output'> { $*POD_CODE_BLOCK = 'output' }
        || <?>
    }

    # XXX Total cheat, no multi-line configuration parsing yet
    token pod_configuration {
        <?before [\h* \S]>
        <.start-token('POD_WHITESPACE')> \h* <.end-token('POD_WHITESPACE')>
        <.start-element('POD_CONFIGURATION')>
        <.start-token('POD_CONFIGURATION')>
        [ \S+ || <!before [\h+ \n]> \h+ ]+
        <.end-token('POD_CONFIGURATION')>
        <.end-element('POD_CONFIGURATION')>
    }

    token vnum {
        \w+ || '*'
    }

    token version {
        <?before 'v'\d+\w*>
        <.start-element('VERSION')>
        <.start-token('VERSION')>
        'v' [<.vnum>+ % '.' '+'?]
        <.end-token('VERSION')>
        <.end-element('VERSION')>
    }

    ## Top-level structure

    token statementlist {
        [
            <?before [\s+ '}']>
            <.start-token('WS_OUTSIDE_LIST')> <?> <.end-token('WS_OUTSIDE_LIST')>
            <.ws>
        ]?
        <.start-element('STATEMENT_LIST')>
        [<.ws> || $]
        [
            <!before $ || <[\)\]\}]> >
            <.start-element('STATEMENT')>
            <.statement>
            <.eat_terminator>
            <.end-element('STATEMENT')>
            <.ws>?
        ]*
        <.end-element('STATEMENT_LIST')>
    }

    token semilist {
        <.ws>
        <.start-element('SEMI_LIST')>
        [
        || <?before <[)\]}]> > <.start-token('SEMI_LIST_END')> <?> <.end-token('SEMI_LIST_END')>
        || [
               <!before $ || <[\)\]\}]> >
               <.start-element('STATEMENT')>
               <.statement>
               <.eat_terminator>
               <.end-element('STATEMENT')>
               <.ws>?
           ]*
        ]
        <.end-element('SEMI_LIST')>
    }

    token label {
        <?before <.identifier> ':' [\s || $]>
        <.start-element('LABEL')>
        <.start-token('LABEL_NAME')>
        <.identifier>
        <.end-token('LABEL_NAME')>
        <.start-token('LABEL_COLON')>
        ':'
        <.end-token('LABEL_COLON')>
        <.end-element('LABEL')>
        <.ws>
    }

    token statement {
        :my $*QSIGIL = '';
        <!before <[\])}]> || $ >
        [
        || <.label> <.statement>?
        || <.statement_control>
        || <.EXPR('')>
            [
                || <?before <.ws> <?MARKED('endstmt')>>
                   <.start-token('END_OF_STATEMENT')> <?> <.end-token('END_OF_STATEMENT')>
                || <?before <.ws> <.statement_mod_cond_keyword> <.kok>>
                   <.ws>
                   <.statement_mod_cond>
                   [
                        <?before <.ws> <.statement_mod_loop_keyword> <.kok>>
                        <.ws>
                        <.statement_mod_loop>
                   ]?
                || <?before <.ws> <.statement_mod_loop_keyword> <.kok>>
                   <.ws>
                   <.statement_mod_loop>
            ]?
        || [ <?[;]> || <?stopper> ]
           <.start-token('EMPTY_STATEMENT')> <?> <.end-token('EMPTY_STATEMENT')>
        || <.bogus_statement>
        ]
    }

    token bogus_statement {
        <.start-token('BAD_CHARACTER')>
        [ <-[;}=]>+ || <!before ^^ '='> '=' ]+
        <.end-token('BAD_CHARACTER')>
    }

    token eat_terminator {
        [
            <.has-heredoc>
            <.start-token('EAT_TERMINATOR_HAS_HEREDOC')> <?> <.end-token('EAT_TERMINATOR_HAS_HEREDOC')>
            <.ws>?
        ]?
        [
        || <?[;]>
           <.start-token('STATEMENT_TERMINATOR')>
           ';'
           <.end-token('STATEMENT_TERMINATOR')>
        || <?before [<.ws> ';']>
           <.ws>
           <.start-token('STATEMENT_TERMINATOR')>
           ';'
           <.end-token('STATEMENT_TERMINATOR')>
        || <?before <.ws> <?MARKED('endstmt')>>
           <.start-token('END_OF_STATEMENT_MARK')> <?> <.end-token('END_OF_STATEMENT_MARK')>
        || <?before <.ws>? [<[)}]> || ']' || <?stopper>]>
           <.start-token('END_OF_STATEMENT_STOPPER')> <?> <.end-token('END_OF_STATEMENT_STOPPER')>
           <.unv>*
           <.start-element('UNTERMINATED_STATEMENT')> <?> <.end-element('UNTERMINATED_STATEMENT')>
        || <?before <.unv>* <.bogus_statement>>
           <.unv>*
           <.start-element('UNTERMINATED_STATEMENT')> <?> <.end-element('UNTERMINATED_STATEMENT')>
           <.bogus_statement>
           [
           || <.start-token('STATEMENT_TERMINATOR')>
              ';'
              <.end-token('STATEMENT_TERMINATOR')>
           || <.start-token('END_OF_STATEMENT')> <?> <.end-token('END_OF_STATEMENT')>
           ]
        || <.ws> $
        ]?
    }

    token xblock {
        :my $*GOAL = '{';
        <.EXPR('')> [<.ws> <.pblock>?]?
    }

    token pblock {
        || <.start-element('POINTY_BLOCK')>
           <.start-token('LAMBDA')>
           <.lambda>
           <.end-token('LAMBDA')>
           :my $*GOAL = '{';
           <.ws>
           <.scope-push>
           <.start-element('SIGNATURE')>
           <.signature>
           <.end-element('SIGNATURE')>
           <.blockoid>?
           <.scope-pop>
           <.end-element('POINTY_BLOCK')>
        || <?[{]>
           <.start-element('BLOCK')>
           <.scope-push>
           <.blockoid>
           <.scope-pop>
           <.end-element('BLOCK')>
        || <.start-token('MISSING_BLOCK')> <?> <.end-token('MISSING_BLOCK')>
    }

    token lambda { '->' || '<->' }

    token block {
        <.start-element('BLOCK')>
        <.scope-push>
        <.blockoid>
        <.scope-pop>
        <.end-element('BLOCK')>
    }

    token terminator {
        || <?[;)\]}]>
        || <?[>]> <?{ $*IN_REGEX_ASSERTION }>
        || [ 'if' || 'unless' || 'while' || 'until' || 'for' || 'given' || 'when' || 'without' || 'with' ]
           <.kok>
        || '-->'
    }

    token blockoid {
        <.start-element('BLOCKOID')>
        <.start-token('BLOCK_CURLY_BRACKET_OPEN')>
        '{'
        <.end-token('BLOCK_CURLY_BRACKET_OPEN')>
        [
            <?before [ <.unv>? [\r\n || \v] ]>
            <.unv>?
            <.start-token('WHITE_SPACE')>
            [\r\n || \v]
            <.end-token('WHITE_SPACE')>
        ]?
        <.statementlist>
        [
        <.start-token('BLOCK_CURLY_BRACKET_CLOSE')>
        '}'
        <.end-token('BLOCK_CURLY_BRACKET_CLOSE')>
        <?ENDSTMT>
        ]?
        <.end-element('BLOCKOID')>
    }

    token stdstopper {
        || <?MARKED('endstmt')>
        || <?terminator>
        || $
    }

    ## Statement controls

    token statement_control {
        || <.statement_control_if>
        || <.statement_control_unless>
        || <.statement_control_without>
        || <.statement_control_while>
        || <.statement_control_until>
        || <.statement_control_repeat>
        || <.statement_control_for>
        || <.statement_control_whenever>
        || <.statement_control_loop>
        || <.statement_control_need>
        || <.statement_control_import>
        || <.statement_control_no>
        || <.statement_control_use>
        || <.statement_control_require>
        || <.statement_control_given>
        || <.statement_control_when>
        || <.statement_control_default>
        || <.statement_control_CATCH>
        || <.statement_control_CONTROL>
        || <.statement_control_QUIT>
    }

    token statement_control_if {
        <?before ['if'||'with'] <.kok>>
        <.start-element('IF_STATEMENT')>
        [
        || <.start-token('STATEMENT_CONTROL')>
           'if'
           <.end-token('STATEMENT_CONTROL')>
        || <.start-token('STATEMENT_CONTROL')>
           'with'
           <.end-token('STATEMENT_CONTROL')>
        ]
        <.kok>
        <.ws>
        [
            [<.xblock> <.ws>?]
            [
                [
                    <?before [ 'elsif' || 'orwith'] <.ws>>
                    [
                    || <.start-token('STATEMENT_CONTROL')>
                       'elsif'
                       <.end-token('STATEMENT_CONTROL')>
                    || <.start-token('STATEMENT_CONTROL')>
                       'orwith'
                       <.end-token('STATEMENT_CONTROL')>
                    ]
                    <.ws>
                    [ <.xblock> <.ws>? ]?
                ]*
                [
                    <?before 'else' <.ws>>
                    <.start-token('STATEMENT_CONTROL')>
                    'else'
                    <.end-token('STATEMENT_CONTROL')>
                    <.ws>
                    [ <.pblock> <.ws>? ]?
                ]?
            ]?
        ]?
        <.end-element('IF_STATEMENT')>
    }

    token statement_control_unless {
        <?before 'unless' <.kok>>
        <.start-element('UNLESS_STATEMENT')>
        <.start-token('STATEMENT_CONTROL')>
        'unless'
        <.end-token('STATEMENT_CONTROL')>
        <.kok>
        <.ws>
        <.xblock>?
        <.end-element('UNLESS_STATEMENT')>
    }

    token statement_control_without {
        <?before 'without' <.kok>>
        <.start-element('WITHOUT_STATEMENT')>
        <.start-token('STATEMENT_CONTROL')>
        'without'
        <.end-token('STATEMENT_CONTROL')>
        <.kok>
        <.ws>
        <.xblock>?
        <.end-element('WITHOUT_STATEMENT')>
    }

    token statement_control_while {
        <?before 'while' <.kok>>
        <.start-element('WHILE_STATEMENT')>
        <.start-token('STATEMENT_CONTROL')>
        'while'
        <.end-token('STATEMENT_CONTROL')>
        <.kok>
        <.ws>
        <.xblock>?
        <.end-element('WHILE_STATEMENT')>
    }

    token statement_control_until {
        <?before 'until' <.kok>>
        <.start-element('UNTIL_STATEMENT')>
        <.start-token('STATEMENT_CONTROL')>
        'until'
        <.end-token('STATEMENT_CONTROL')>
        <.kok>
        <.ws>
        <.xblock>?
        <.end-element('UNTIL_STATEMENT')>
    }

    token statement_control_repeat {
        <?before 'repeat' <.kok>>
        <.start-element('REPEAT_STATEMENT')>
        <.start-token('STATEMENT_CONTROL')>
        'repeat'
        <.end-token('STATEMENT_CONTROL')>
        <.kok>
        <.ws>
        [
        || <?before ['while'||'until'] <.kok>>
           [
           || <.start-token('STATEMENT_CONTROL')>
              'while'
              <.end-token('STATEMENT_CONTROL')>
           || <.start-token('STATEMENT_CONTROL')>
              'until'
               <.end-token('STATEMENT_CONTROL')>
           ]
           <.kok>
           [ <.ws> <.xblock>? ]?
        || <.pblock>
           [
               <.ws>
               [
                   <?before ['while'||'until'] <.kok>>
                   [
                   || <.start-token('STATEMENT_CONTROL')>
                      'while'
                      <.end-token('STATEMENT_CONTROL')>
                   || <.start-token('STATEMENT_CONTROL')>
                      'until'
                       <.end-token('STATEMENT_CONTROL')>
                   ]
                   <.ws>?
                   <.EXPR('')>?
               ]?
           ]?
        || <?>
        ]
        <.end-element('REPEAT_STATEMENT')>
    }

    token statement_control_for {
        <?before 'for' <.kok>>
        <.start-element('FOR_STATEMENT')>
        <.start-token('STATEMENT_CONTROL')>
        'for'
        <.end-token('STATEMENT_CONTROL')>
        <.kok>
        <.ws>
        <.xblock>?
        <.end-element('FOR_STATEMENT')>
    }

    token statement_control_whenever {
        <?before 'whenever' <.kok>>
        <.start-element('WHENEVER_STATEMENT')>
        <.start-token('STATEMENT_CONTROL')>
        'whenever'
        <.end-token('STATEMENT_CONTROL')>
        <.kok>
        <.ws>
        <.xblock>?
        <.end-element('WHENEVER_STATEMENT')>
    }

    token statement_control_loop {
        <?before 'loop' <.kok>>
        <.start-element('LOOP_STATEMENT')>
        <.start-token('STATEMENT_CONTROL')>
        'loop'
        <.end-token('STATEMENT_CONTROL')>
        <.kok>
        <.ws>
        [
            <.start-token('PARENTHESES_OPEN')> '(' <.end-token('PARENTHESES_OPEN')>
            <.ws>
            <.EXPR('')>?
            [
                <.ws>
                [
                    <.start-token('STATEMENT_TERMINATOR')>
                    ';'
                    <.end-token('STATEMENT_TERMINATOR')>
                    <.ws>
                    <.EXPR('')>?
                    [
                        <.ws>
                        [
                            <.start-token('STATEMENT_TERMINATOR')>
                            ';'
                            <.end-token('STATEMENT_TERMINATOR')>
                            <.ws>
                            <.EXPR('')>?
                            <.ws>?
                        ]?
                    ]?
                ]?
                [<.start-token('PARENTHESES_CLOSE')> ')' <.end-token('PARENTHESES_CLOSE')>]?
                <.ws>?
            ]?
        ]?
        <.block>?
        <.end-element('LOOP_STATEMENT')>
    }

    token statement_control_need {
        <?before ['need' <.ws>]>
        <!before 'need' <[-']>>
        <.start-element('NEED_STATEMENT')>
        <.start-token('STATEMENT_CONTROL')>
        'need'
        <.end-token('STATEMENT_CONTROL')>
        <.ws>
        [
            [
            || <.version>
            || <.module_name>
            ]
            [
                <.ws>?
                [
                    <.start-token('INFIX')>
                    ','
                    <.end-token('INFIX')>
                    <.ws>
                    [
                    || <.version>
                    || <.module_name>
                    ]?
                    <.ws>?
                ]*
            ]?
        ]?
        <.end-element('NEED_STATEMENT')>
    }

    token statement_control_import {
        :my $*IN_DECL = 'import';
        <?before ['import' <.ws>]>
        <!before 'import' <[-']>>
        <.start-element('IMPORT_STATEMENT')>
        <.start-token('STATEMENT_CONTROL')>
        'import'
        <.end-token('STATEMENT_CONTROL')>
        <.ws>
        [
            <.module_name>
            [ <.spacey> <.arglist> ]?
            <.ws>?
        ]?
        <.end-element('IMPORT_STATEMENT')>
    }

    token statement_control_no {
        :my $*IN_DECL = 'no';
        <?before ['no' <.ws>]>
        <!before 'no' <[-']>>
        <.start-element('NO_STATEMENT')>
        <.start-token('STATEMENT_CONTROL')>
        'no'
        <.end-token('STATEMENT_CONTROL')>
        <.ws>
        [
            <.module_name>
            [ <.spacey> <.arglist> ]?
            <.ws>?
        ]?
        <.end-element('NO_STATEMENT')>
    }

    token statement_control_use {
        <?before ['use' <.ws>]>
        <!before 'use' <[-']>>
        :my $*IN_DECL = 'use';
        <.start-element('USE_STATEMENT')>
        <.start-token('STATEMENT_CONTROL')>
        'use'
        <.end-token('STATEMENT_CONTROL')>
        <.ws>
        [
        || <.version>
        || <.module_name> [ <.spacey> <.arglist> ]?
        ]?
        <.ws>?
        <.end-element('USE_STATEMENT')>
    }

    token statement_control_require {
        <?before ['require' <.ws>]>
        <!before 'require' <[-']>>
        <.start-element('REQUIRE_STATEMENT')>
        <.start-token('STATEMENT_CONTROL')>
        'require'
        <.end-token('STATEMENT_CONTROL')>
        <.ws>
        [
            [
            || <.module_name>
            || <.variable>
            || <!sigil> <.term>
            ]
            [ <.ws> <.EXPR('')>? ]?
            <.ws>?
        ]?
        <.end-element('REQUIRE_STATEMENT')>
    }

    token statement_control_given {
        <?before 'given' <.kok>>
        <.start-element('GIVEN_STATEMENT')>
        <.start-token('STATEMENT_CONTROL')>
        'given'
        <.end-token('STATEMENT_CONTROL')>
        <.kok>
        <.ws>
        <.xblock>?
        <.end-element('GIVEN_STATEMENT')>
    }

    token statement_control_when {
        <?before 'when' <.kok>>
        <.start-element('WHEN_STATEMENT')>
        <.start-token('STATEMENT_CONTROL')>
        'when'
        <.end-token('STATEMENT_CONTROL')>
        <.kok>
        <.ws>
        <.xblock>?
        <.end-element('WHEN_STATEMENT')>
    }

    token statement_control_default {
        <?before 'default' <.kok>>
        <.start-element('DEFAULT_STATEMENT')>
        <.start-token('STATEMENT_CONTROL')>
        'default'
        <.end-token('STATEMENT_CONTROL')>
        <.kok>
        <.ws>
        <.block>?
        <.end-element('DEFAULT_STATEMENT')>
    }

    token statement_control_CATCH {
        <?before 'CATCH' <.kok>>
        <.start-element('CATCH_STATEMENT')>
        <.start-token('STATEMENT_CONTROL')>
        'CATCH'
        <.end-token('STATEMENT_CONTROL')>
        <.kok>
        <.ws>
        <.block>?
        <.end-element('CATCH_STATEMENT')>
    }

    token statement_control_CONTROL {
        <?before 'CONTROL' <.kok>>
        <.start-element('CONTROL_STATEMENT')>
        <.start-token('STATEMENT_CONTROL')>
        'CONTROL'
        <.end-token('STATEMENT_CONTROL')>
        <.kok>
        <.ws>
        <.block>?
        <.end-element('CONTROL_STATEMENT')>
    }

    token statement_control_QUIT {
        <?before 'QUIT' <.kok>>
        <.start-element('QUIT_STATEMENT')>
        <.start-token('STATEMENT_CONTROL')>
        'QUIT'
        <.end-token('STATEMENT_CONTROL')>
        <.kok>
        <.ws>
        <.block>?
        <.end-element('QUIT_STATEMENT')>
    }

    ## Statement prefixes

    token statement_prefix {
        || <.statement_prefix_DOC>
        || <.statement_prefix_phaser>
        || <.statement_prefix_race>
        || <.statement_prefix_hyper>
        || <.statement_prefix_lazy>
        || <.statement_prefix_eager>
        || <.statement_prefix_sink>
        || <.statement_prefix_try>
        || <.statement_prefix_quietly>
        || <.statement_prefix_gather>
        || <.statement_prefix_once>
        || <.statement_prefix_start>
        || <.statement_prefix_supply>
        || <.statement_prefix_react>
        || <.statement_prefix_do>
    }

    token phaser_name {
        || 'BEGIN'
        || 'COMPOSE'
        || 'TEMP'
        || 'CHECK'
        || 'INIT'
        || 'ENTER'
        || 'FIRST'
        || 'END'
        || 'LEAVE'
        || 'KEEP'
        || 'UNDO'
        || 'NEXT'
        || 'LAST'
        || 'PRE'
        || 'POST'
        || 'CLOSE'
    }

    token statement_prefix_DOC {
        <?before 'DOC' <.kok>>
        <.start-element('PHASER')>
        <.start-token('PHASER')>
        'DOC'
        <.end-token('PHASER')>
        <.kok>
        <.ws>
        [
            <?before ['BEGIN' || 'CHECK' || 'INIT'] <.end_keyword>>
            <.statement_prefix>
        ]?
        <.end-element('PHASER')>
    }

    token statement_prefix_phaser {
        <?before <.phaser_name> <.kok>>
        <.start-element('PHASER')>
        <.start-token('PHASER')>
        <.phaser_name>
        <.end-token('PHASER')>
        <.kok>
        <.blorst>
        <.end-element('PHASER')>
    }

    token statement_prefix_race {
        <?before 'race' <.kok>>
        <.start-element('RACE')>
        <.start-token('STATEMENT_PREFIX')>
        'race'
        <.end-token('STATEMENT_PREFIX')>
        <.kok>
        <.blorst>
        <.end-element('RACE')>
    }

    token statement_prefix_hyper {
        <?before 'hyper' <.kok>>
        <.start-element('HYPER')>
        <.start-token('STATEMENT_PREFIX')>
        'hyper'
        <.end-token('STATEMENT_PREFIX')>
        <.kok>
        <.blorst>
        <.end-element('HYPER')>
    }

    token statement_prefix_lazy {
        <?before 'lazy' <.kok>>
        <.start-element('LAZY')>
        <.start-token('STATEMENT_PREFIX')>
        'lazy'
        <.end-token('STATEMENT_PREFIX')>
        <.kok>
        <.blorst>
        <.end-element('LAZY')>
    }

    token statement_prefix_eager {
        <?before 'eager' <.kok>>
        <.start-element('EAGER')>
        <.start-token('STATEMENT_PREFIX')>
        'eager'
        <.end-token('STATEMENT_PREFIX')>
        <.kok>
        <.blorst>
        <.end-element('EAGER')>
    }

    token statement_prefix_sink {
        <?before 'sink' <.kok>>
        <.start-element('SINK')>
        <.start-token('STATEMENT_PREFIX')>
        'sink'
        <.end-token('STATEMENT_PREFIX')>
        <.kok>
        <.blorst>
        <.end-element('SINK')>
    }

    token statement_prefix_try {
        <?before 'try' <.kok>>
        <.start-element('TRY')>
        <.start-token('STATEMENT_PREFIX')>
        'try'
        <.end-token('STATEMENT_PREFIX')>
        <.kok>
        <.blorst>
        <.end-element('TRY')>
    }

    token statement_prefix_quietly {
        <?before 'quietly' <.kok>>
        <.start-element('QUIETLY')>
        <.start-token('STATEMENT_PREFIX')>
        'quietly'
        <.end-token('STATEMENT_PREFIX')>
        <.kok>
        <.blorst>
        <.end-element('QUIETLY')>
    }

    token statement_prefix_gather {
        <?before 'gather' <.kok>>
        <.start-element('GATHER')>
        <.start-token('STATEMENT_PREFIX')>
        'gather'
        <.end-token('STATEMENT_PREFIX')>
        <.kok>
        <.blorst>
        <.end-element('GATHER')>
    }

    token statement_prefix_once {
        <?before 'once' <.kok>>
        <.start-element('ONCE')>
        <.start-token('STATEMENT_PREFIX')>
        'once'
        <.end-token('STATEMENT_PREFIX')>
        <.kok>
        <.blorst>
        <.end-element('ONCE')>
    }

    token statement_prefix_start {
        <?before 'start' <.kok>>
        <.start-element('START')>
        <.start-token('STATEMENT_PREFIX')>
        'start'
        <.end-token('STATEMENT_PREFIX')>
        <.kok>
        <.blorst>
        <.end-element('START')>
    }

    token statement_prefix_supply {
        <?before 'supply' <.kok>>
        <.start-element('SUPPLY')>
        <.start-token('STATEMENT_PREFIX')>
        'supply'
        <.end-token('STATEMENT_PREFIX')>
        <.kok>
        <.blorst>
        <.end-element('SUPPLY')>
    }

    token statement_prefix_react {
        <?before 'react' <.kok>>
        <.start-element('REACT')>
        <.start-token('STATEMENT_PREFIX')>
        'react'
        <.end-token('STATEMENT_PREFIX')>
        <.kok>
        <.blorst>
        <.end-element('REACT')>
    }

    token statement_prefix_do {
        <?before 'do' <.kok>>
        <.start-element('DO')>
        <.start-token('STATEMENT_PREFIX')>
        'do'
        <.end-token('STATEMENT_PREFIX')>
        <.kok>
        <.blorst>
        <.end-element('DO')>
    }

    token blorst {
        [
        || <?[{]> <.block>
        || <.statement>
        || <.start-token('MISSING_BLORST')> <?> <.end-token('MISSING_BLORST')>
        ]
    }

    ## Statement modifiers

    token statement_mod_cond_keyword {
        'if' || 'unless' || 'when' || 'without' || 'with'
    }

    token statement_mod_cond {
        <?before <.statement_mod_cond_keyword> <.kok>>
        <.start-element('STATEMENT_MOD_COND')>
        <.start-token('STATEMENT_MOD_COND')>
        <.statement_mod_cond_keyword>
        <.end-token('STATEMENT_MOD_COND')>
        <.kok>
        <.ws>
        <.EXPR('')>?
        <.end-element('STATEMENT_MOD_COND')>
    }

    token statement_mod_loop_keyword {
        'while' || 'until' || 'for' || 'given'
    }

    token statement_mod_loop {
        <?before <.statement_mod_loop_keyword> <.kok>>
        <.start-element('STATEMENT_MOD_LOOP')>
        <.start-token('STATEMENT_MOD_LOOP')>
        <.statement_mod_loop_keyword>
        <.end-token('STATEMENT_MOD_LOOP')>
        <.kok>
        <.ws>
        <.EXPR('')>?
        <.end-element('STATEMENT_MOD_LOOP')>
    }

    ## Terms

    token term {
        || <.fatarrow>
        || <.colonpair>
        || <.term_self>
        || <.variable>
        || <.term_ident>
        || <.value>
        || <.scope_declarator>
        || <.routine_declarator>
        || <.regex_declarator>
        || <?before ['multi' || 'proto' || 'only'] <.kok>>
           <.start-token('TERM_IS_MULTI')> <?> <.end-token('TERM_IS_MULTI')>
           <.multi_declarator>
        || <.type_declarator>
        || <.statement_prefix>
        || <.package_declarator>
        || <.term_onlystar>
        || <.term_reduce>
        || <.circumfix>
        || <.term_stub_code>
        || <.dotty>
        || <?lambda> <.pblock>
        || <.term_now>
        || <.term_time>
        || <.term_empty_set>
        || <.term_rand>
        || <.term_whatever>
        || <.term_hyperwhatever>
        || <.term_type_const>
        || <.term_name>
        || <.capterm>
    }

    token term_ident {
        <?before <.identifier> [ <.unsp>? '(' || \\ '(' ]>
        <.start-element('SUB_CALL')>
        <.start-element('SUB_CALL_NAME')>
        <.start-token('SUB_CALL_NAME')>
        <.identifier>
        <.end-token('SUB_CALL_NAME')>
        <.end-element('SUB_CALL_NAME')>
        [ <?before '\\('> <.start-token('WHITE_SPACE')> '\\' <.end-token('WHITE_SPACE')> ]?
        <.args>
        <.end-element('SUB_CALL')>
    }

    # MIRROR of MAINBraid.java hand-edit (_105_term_name case 10, and see
    # org/llm/raku/traces/parser-nqp-const-term.md): Rakudo's dedicated
    # term:sym<nqp::const> takes NO argument list, unlike ordinary listop
    # names -- hence the nqp::const:: alternative below that emits NO_ARGS
    # instead of attempting <.args>.
    token term_name {
        || <.start-element('TYPE_NAME')>
           <.start-element('LONG_NAME')>
           <.start-token('NAME')>
           <.start-symbol>
           <.name>
           <.is-name>
           <.end-token('NAME')>
           <.longname_colonpairs>
           <.end-element('LONG_NAME')>
           <.end-element('TYPE_NAME')>
        || <?before 'nqp::const::'>
           <.start-element('SUB_CALL')>
           <.start-element('LONG_NAME')>
           <.start-element('SUB_CALL_NAME')>
           <.start-token('SUB_CALL_NAME')>
           <.name>
           <.end-token('SUB_CALL_NAME')>
           <.end-element('SUB_CALL_NAME')>
           <.longname_colonpairs>
           <.end-element('LONG_NAME')>
           <.start-token('NO_ARGS')> <?> <.end-token('NO_ARGS')>
           <.end-element('SUB_CALL')>
        || <.start-element('SUB_CALL')>
           <.start-element('LONG_NAME')>
           <.start-element('SUB_CALL_NAME')>
           <.start-token('SUB_CALL_NAME')>
           <.name>
           <.end-token('SUB_CALL_NAME')>
           <.end-element('SUB_CALL_NAME')>
           <.longname_colonpairs>
           <.end-element('LONG_NAME')>
           [ <?before '\\('> <.start-token('WHITE_SPACE')> '\\' <.end-token('WHITE_SPACE')> ]?
           <.args>
           <.end-element('SUB_CALL')>
    }

    token term_self {
        <?before ['self' <.end_keyword>]>
        <.start-element('SELF')>
        <.start-token('SELF')>
        'self'
        <.end-token('SELF')>
        <.end-element('SELF')>
        <.end_keyword>
    }

    token term_whatever {
        <.start-element('WHATEVER')>
        <.start-token('WHATEVER')>
        '*'
        <.end-token('WHATEVER')>
        <.end-element('WHATEVER')>
    }

    token term_hyperwhatever {
        <.start-element('HYPER_WHATEVER')>
        <.start-token('HYPER_WHATEVER')>
        '*'
        <.end-token('HYPER_WHATEVER')>
        <.end-element('HYPER_WHATEVER')>
    }

    token term_type_const {
        <.start-element('TYPE_NAME')>
        <?before '::?' <.ident>>
        <.start-token('TYPE_CONST')> <?> <.end-token('TYPE_CONST')>
        <.start-token('NAME')>
        '::?' <.identifier> >>
        <.end-token('NAME')>
        <.end-element('TYPE_NAME')>
    }

    token term_now {
        <?before 'now' <.tok>>
        <.start-element('TERM')>
        <.start-token('TERM')>
        'now'
        <.end-token('TERM')>
        <.tok>
        <.end-element('TERM')>
    }

    token term_time {
        <?before 'time' <.tok>>
        <.start-element('TERM')>
        <.start-token('TERM')>
        'time'
        <.end-token('TERM')>
        <.tok>
        <.end-element('TERM')>
    }

    token term_empty_set {
        <?before ['∅' <!before <[ \( \\ ' \- ]> || \h* '=>'>]>
        <.start-element('TERM')>
        <.start-token('TERM')>
        '∅'
        <.end-token('TERM')>
        <.end-element('TERM')>
    }

    token term_rand {
        <?before 'rand' >> <.end_keyword>>
        <.start-element('TERM')>
        <.start-token('TERM')>
        'rand'
        <.end-token('TERM')>
        <.end_keyword>
        <.end-element('TERM')>
    }

    token term_stub_code {
        <.start-element('STUB_CODE')>
        [
        || <.start-token('STUB_CODE')> '…' <.end-token('STUB_CODE')>
        || <.start-token('STUB_CODE')> '...' <.end-token('STUB_CODE')>
        || <.start-token('STUB_CODE')> '???' <.end-token('STUB_CODE')>
        || <.start-token('STUB_CODE')> '!!!' <.end-token('STUB_CODE')>
        ]
        <.args>
        <.end-element('STUB_CODE')>
    }

    token term_onlystar {
        <.start-element('ONLY_STAR')>
        <.start-token('ONLY_STAR')>
        '{*}'
        <.end-token('ONLY_STAR')>
        <.end-element('ONLY_STAR')>
        <?ENDSTMT>
    }

    token fatarrow {
        <?before <.identifier> \h* '=>' <.ws>>
        <.start-element('FATARROW')>
        <.start-token('PAIR_KEY')>
        <.identifier>
        <.end-token('PAIR_KEY')>
        <.start-token('WHITE_SPACE')>
        \h*
        <.end-token('WHITE_SPACE')>
        <.start-token('INFIX')>
        '=>'
        <.end-token('INFIX')>
        <.ws>
        <.EXPR('i<=')>?
        <.end-element('FATARROW')>
    }

    token args {
        :my $*GOAL = '';
        [
        || <.start-token('PARENTHESES_OPEN')> '(' <.end-token('PARENTHESES_OPEN')>
           <.semiarglist>
           [ <.start-token('PARENTHESES_CLOSE')> ')' <.end-token('PARENTHESES_CLOSE')> ]?
        || <.unsp>
           <.start-token('PARENTHESES_OPEN')> '(' <.end-token('PARENTHESES_OPEN')>
           <.semiarglist>
           [ <.start-token('PARENTHESES_CLOSE')> ')' <.end-token('PARENTHESES_CLOSE')> ]?
        || <.start-token('WHITE_SPACE')> \s <.end-token('WHITE_SPACE')> <.arglist>
        || <.start-token('NO_ARGS')> <?> <.end-token('NO_ARGS')>
        ]
    }

    token colonpair {
        <.start-element('COLON_PAIR')>
        [
        || <?before [':' \d+ <.identifier>]>
           <.start-token('COLON_PAIR')>
           ':'
           <.end-token('COLON_PAIR')>
           <.start-token('INTEGER_LITERAL')>
           \d+
           <.end-token('INTEGER_LITERAL')>
           <.start-token('COLON_PAIR')>
           <.identifier>
           <.end-token('COLON_PAIR')>
        || <?before [':!' <.identifier>]>
           <.start-token('COLON_PAIR')>
           ':!'
           <.end-token('COLON_PAIR')>
           <.start-token('COLON_PAIR')>
           <.identifier>
           <.end-token('COLON_PAIR')>
        || <.start-token('COLON_PAIR')>
           ':('
           <.end-token('COLON_PAIR')>
           <.start-element('SIGNATURE')>
           <.signature>
           <.end-element('SIGNATURE')>
           [
               <.start-token('COLON_PAIR')>
               ')'
               <.end-token('COLON_PAIR')>
           ]?
        || <?before [':' <.sigil> [ <.twigil>? <.identifier> || '<']]>
           <.start-token('COLON_PAIR')>
           ':'
           <.end-token('COLON_PAIR')>
           <.colonpair_variable>
        || <?before [':' <.identifier>]>
           <.start-token('COLON_PAIR')>
           ':'
           <.end-token('COLON_PAIR')>
           <.start-token('COLON_PAIR')>
           <.identifier>
           <.end-token('COLON_PAIR')>
           [
               <?before [<.unsp>? <[[{<«(]>]>
               <.start-token('COLON_PAIR_HAS_VALUE')> <?> <.end-token('COLON_PAIR_HAS_VALUE')>
               <.unsp>?
               <.coloncircumfix>
           ]?
        || <?before [':' <[[{<«(]>]>
           <.start-token('COLON_PAIR')>
           ':'
           <.end-token('COLON_PAIR')>
           <.coloncircumfix>
        ]
        <.end-element('COLON_PAIR')>
    }

    token coloncircumfix {
        <.circumfix>
    }

    token colonpair_variable {
        <.start-element('VARIABLE')>
        [
        || <.start-token('VARIABLE')>
           <.sigil> <.twigil>? <.desigilname>
           <.end-token('VARIABLE')>
        || <.start-token('REGEX_CAPTURE_NAME')>
           <.sigil> '<' [ <.desigilname> '>'? ]?
           <.end-token('REGEX_CAPTURE_NAME')>
        ]
        <.end-element('VARIABLE')>
    }

    # XXX Cheat
    token semiarglist {
        <.arglist>
        <.ws>?
        [
           <.start-token('STATEMENT_TERMINATOR')>
           ';'
           <.end-token('STATEMENT_TERMINATOR')>
           <.arglist>?
           <.ws>?
        ]*
    }

    token arglist {
        :my $*QSIGIL = '';
        :my $*GOAL = 'endargs';
        <.ws>
        <.start-token('ARGLIST_START')> <?> <.end-token('ARGLIST_START')>
        [
        || <?stdstopper> <.start-token('ARGLIST_EMPTY')> <?> <.end-token('ARGLIST_EMPTY')>
        || <.EXPR('e=')>?
        ]
        <.start-token('ARGLIST_END')> <?> <.end-token('ARGLIST_END')>
    }

    token variable {
        :my $*Q_BACKSLASH = 0;
        :my $*Q_QBACKSLASH = 0;
        :my $*Q_QQBACKSLASH = 0;
        :my $*Q_CLOSURES = 0;
        :my $*Q_SCALARS = 0;
        :my $*Q_ARRAYS = 0;
        :my $*Q_HASHES = 0;
        :my $*Q_FUNCTIONS = 0;
        :my $*IN_META = '';
        [
            <!{ $*LEFTSIGIL }>
            [
            || <?[$]> { $*LEFTSIGIL = '$' }
            || <?[@]> { $*LEFTSIGIL = '@' }
            || <?[%]> { $*LEFTSIGIL = '%' }
            || <?[&]> { $*LEFTSIGIL = '&' }
            ]
        ]?
        [
        || <?before [<.sigil> <?sigil> <.variable>]>
           <.start-token('SIMPLE_CONTEXTUALIZER')> <?> <.end-token('SIMPLE_CONTEXTUALIZER')>
           <.start-element('CONTEXTUALIZER')>
           <.start-token('CONTEXTUALIZER')>
           <.sigil>
           <.end-token('CONTEXTUALIZER')>
           <.variable>
           <.end-element('CONTEXTUALIZER')>
        || <.start-element('VARIABLE')>
           <.start-token('VARIABLE')>
           '&['
           <.end-token('VARIABLE')>
           <.start-token('INFIX_NOUN_VARIABLE')> <?> <.end-token('INFIX_NOUN_VARIABLE')>
           [
               <.infixish('[]')>
               [
                   <.start-token('VARIABLE')>
                   ']'
                   <.end-token('VARIABLE')>
               ]?
           ]?
           <.end-element('VARIABLE')>
        || <!{ $*IN_DECL }> <?before <.sigil> '.' [<.desigilname> || \s || $]>
           <.start-token('SELF_CALL_VARIABLE')> <?> <.end-token('SELF_CALL_VARIABLE')>
           <.start-element('POSTFIX_APPLICATION')>
           <.start-element('SELF')>
           <.start-token('SELF')>
           <.sigil>
           <.end-token('SELF')>
           <.end-element('SELF')>
           <.start-element('METHOD_CALL')>
           <.start-token('METHOD_CALL_OPERATOR')>
           '.'
           <.end-token('METHOD_CALL_OPERATOR')>
           [
               <.start-element('LONG_NAME')>
               <.start-token('METHOD_CALL_NAME')>
               <.desigilname>
               <.end-token('METHOD_CALL_NAME')>
               <.end-element('LONG_NAME')>
               [
                   <?before [ <.unsp> || '\\' || <?> ] '('>
                   <.start-token('SELF_CALL_VARIABLE_ARGS')> <?> <.end-token('SELF_CALL_VARIABLE_ARGS')>
                   [
                   || <.unsp>
                   || <.start-token('WHITE_SPACE')>
                      '\\'
                      <.end-token('WHITE_SPACE')>
                   ]?
                   <.postcircumfix>
               ]?
           ]?
           <.end-element('METHOD_CALL')>
           <.end-element('POSTFIX_APPLICATION')>
        || <.start-element('VARIABLE')>
           <.start-token('VARIABLE')>
           <.sigil> <.twigil>? <.desigilname>
           <.end-token('VARIABLE')>
           <.longname_colonpairs>
           <.end-element('VARIABLE')>
        || <.start-element('VARIABLE')>
           <.start-token('VARIABLE')>
           '$' <[/_!¢]>
           <.end-token('VARIABLE')>
           <.end-element('VARIABLE')>
        || <?before [<.sigil> '<']>
           <.start-token('VARIABLE_REGEX_NAMED_CAPTURE')>
           <?>
           <.end-token('VARIABLE_REGEX_NAMED_CAPTURE')>
           <.start-element('VARIABLE')>
           <.start-token('REGEX_CAPTURE_NAME')>
           <.sigil> '<'
           <.end-token('REGEX_CAPTURE_NAME')>
           <.quote_q('<', '>', '>')>
           [
               <.start-token('REGEX_CAPTURE_NAME')>
               '>'
               <.end-token('REGEX_CAPTURE_NAME')>
           ]?
           <.end-element('VARIABLE')>
        || <.start-element('VARIABLE')>
           <.start-token('REGEX_CAPTURE_NAME')>
           <.sigil> \d+
           <.end-token('REGEX_CAPTURE_NAME')>
           <.end-element('VARIABLE')>
        || <?before <.sigil> <?[ ( [ { ]>> <!{ $*IN_DECL }> <.contextualizer>
        || <!{ $*QSIGIL }>
           <.start-element('VARIABLE')>
           <.start-token('VARIABLE')>
           <.sigil>
           <.end-token('VARIABLE')>
           <.end-element('VARIABLE')>
           <?MARKER('baresigil')>
        ]
    }

    token contextualizer {
        <.start-element('CONTEXTUALIZER')>
        [
        || <?before [<.sigil> <?[ \[ \{ ]>]>
           <.start-token('CIRCUMFIX_CONTEXTUALIZER')> <?> <.end-token('CIRCUMFIX_CONTEXTUALIZER')>
           <.start-token('CONTEXTUALIZER')>
           <.sigil>
           <.end-token('CONTEXTUALIZER')>
           <.circumfix>
        || <.start-token('CONTEXTUALIZER')>
           <.sigil>
           <.end-token('CONTEXTUALIZER')>
           <.start-token('CONTEXTUALIZER_OPEN')>
           '('
           <.end-token('CONTEXTUALIZER_OPEN')>
           <.semilist>
           [
               <.start-token('CONTEXTUALIZER_CLOSE')>
               ')'
               <.end-token('CONTEXTUALIZER_CLOSE')>
           ]?
        ]
        <.end-element('CONTEXTUALIZER')>
    }

    token scope_declarator {
        :my $*IN_UNIT = 0;
        <.start-element('SCOPED_DECLARATION')>
        <.start-token('SCOPE_DECLARATOR')>
        [
        'my' || 'our' || 'has' || 'HAS' || 'augment' || 'anon' || 'state' || 'supersede' ||
        'unit' { $*IN_UNIT = 1 }
        ]
        <.end_keyword>
        <.end-token('SCOPE_DECLARATOR')>
        <.ws>
        [
        || <.declarator>
        || <.package_declarator>
        || [
               <!before [<.sigil> || <[\\(]> || 'sub' || 'method' ||
                         'regex' || 'token' || 'rule' ||
                         'multi' || 'proto' || 'only' ||
                         'constant' || 'enum' || 'subset' ]>
               <.typename> <.ws>?
           ]+
           <.multi_declarator>?
        || <.multi_declarator>
        || <.start-token('INCOMPLETE_SCOPED_DECLARATION')>
           <?>
           <.end-token('INCOMPLETE_SCOPED_DECLARATION')>
        ]
        <.end-element('SCOPED_DECLARATION')>
    }

    token declarator {
        :my $*LEFTSIGIL = '';
        [
        || <?before '\\' <.defterm>>
           <.start-element('VARIABLE_DECLARATION')>
           <.start-token('TERM_DECLARATION_BACKSLASH')>
           '\\'
           <.end-token('TERM_DECLARATION_BACKSLASH')>
           <.defterm>
           [ <.ws> <.initializer>? ]?
           <.end-element('VARIABLE_DECLARATION')>
        || <.start-element('VARIABLE_DECLARATION')>
           <.variable_declarator>
           [ <.ws> <.initializer>? ]?
           <.end-element('VARIABLE_DECLARATION')>
        || <.start-element('VARIABLE_DECLARATION')>
           <.start-token('PARENTHESES_OPEN')>
           '('
           <.end-token('PARENTHESES_OPEN')>
           <.start-element('SIGNATURE')>
           <.signature>
           <.end-element('SIGNATURE')>
           [
               <.start-token('PARENTHESES_CLOSE')>
               ')'
               <.end-token('PARENTHESES_CLOSE')>
               <.ws> <.trait>*
               [ <.ws> <.initializer>? ]?
           ]?
           <.end-element('VARIABLE_DECLARATION')>
        || <.routine_declarator>
        || <.regex_declarator>
        || <.type_declarator>
        ]
    }

    token multi_declarator {
        || <?before ['multi' || 'proto' || 'only'] <.kok>>
           <.start-element('MULTI_DECLARATION')>
           <.start-token('MULTI_DECLARATOR')>
           ['multi' || 'proto' || 'only']
           <.end-token('MULTI_DECLARATOR')>
           <.kok>
           [
           || <.declarator>
           || <.start-element('ROUTINE_DECLARATION')>
              <.routine_def>
              <.end-element('ROUTINE_DECLARATION')>
           || <?>
           ]
           <.end-element('MULTI_DECLARATION')>
        || <.declarator>
    }

    token variable_declarator {
        :my $*IN_DECL = 'variable';
        <.variable>
        { $*IN_DECL = '' }
        [
            <?before [<.unsp>? <[[{]>]>
            <.unsp>?
            [
            || <.start-token('SHAPE_DECLARATION')>
               '['
               <.end-token('SHAPE_DECLARATION')>
               <.semilist>
               [
                   <.start-token('SHAPE_DECLARATION')>
                   ']'
                   <.end-token('SHAPE_DECLARATION')>
               ]?
            || <.start-token('SHAPE_DECLARATION')>
               '{'
               <.end-token('SHAPE_DECLARATION')>
               <.semilist>
               [
                   <.start-token('SHAPE_DECLARATION')>
                   '}'
                   <.end-token('SHAPE_DECLARATION')>
               ]?
            ]+
        ]?
        <.ws>?
        <.trait>*
        [ <.ws> <.post_constraint>* ]?
    }

    token routine_declarator {
        <.start-element('ROUTINE_DECLARATION')>
        [
        || <.start-token('ROUTINE_DECLARATOR')>
           'sub' <.end_keyword>
           <.end-token('ROUTINE_DECLARATOR')>
           <.routine_def>
        || <.start-token('ROUTINE_DECLARATOR')>
           'method' <.end_keyword>
           <.end-token('ROUTINE_DECLARATOR')>
           <.method_def>
        || <.start-token('ROUTINE_DECLARATOR')>
           'submethod' <.end_keyword>
           <.end-token('ROUTINE_DECLARATOR')>
           <.method_def>
        || <.start-token('ROUTINE_DECLARATOR')>
           'macro' <.end_keyword>
           <.end-token('ROUTINE_DECLARATOR')>
           <.method_def>
        ]
        <.end-element('ROUTINE_DECLARATION')>
    }

    token routine_def {
        :my $*IN_DECL = 'sub';
        <.ws>
        <.routine_name>?
        <.ws>
        <.scope-push>
        [
            <.start-element('SIGNATURE')>
            <.start-token('PARENTHESES_OPEN')>
            '('
            <.end-token('PARENTHESES_OPEN')>
            <.signature>
            [
            <.start-token('PARENTHESES_CLOSE')>
            ')'
            <.end-token('PARENTHESES_CLOSE')>
            ]?
            <.end-element('SIGNATURE')>
        ]?
        <.ws>?
        <.trait>*
        { $*IN_DECL = '' }
        [
        || <.onlystar>
        || <.blockoid>
        || <?{ $*IN_UNIT }>
           <.start-token('STATEMENT_TERMINATOR')>
           ';'
           <.end-token('STATEMENT_TERMINATOR')>
           <.ws>?
           <.statementlist>?
        # Allow for body not written yet
        || <?>
        ]
        <.scope-pop>
    }

    token method_def {
        :my $*IN_DECL = 'method';
        <.ws>
        <.method_name>?
        <.ws>
        <.scope-push>
        [
            <.start-element('SIGNATURE')>
            <.start-token('PARENTHESES_OPEN')>
            '('
            <.end-token('PARENTHESES_OPEN')>
            <.signature>
            [
            <.start-token('PARENTHESES_CLOSE')>
            ')'
            <.end-token('PARENTHESES_CLOSE')>
            ]?
            <.end-element('SIGNATURE')>
        ]?
        <.ws>?
        <.trait>*
        { $*IN_DECL = '' }
        [
        || <.onlystar>
        || <.blockoid>
        # Allow for body not written yet
        || <?>
        ]
        <.scope-pop>
    }

    token onlystar {
        <?before '{' <.ws> '*' <.ws> '}'>
        <.start-token('BLOCK_CURLY_BRACKET_OPEN')>
        '{'
        <.end-token('BLOCK_CURLY_BRACKET_OPEN')>
        <.ws>
        <.start-token('ONLY_STAR')>
        '*'
        <.end-token('ONLY_STAR')>
        <.ws>
        <.start-token('BLOCK_CURLY_BRACKET_CLOSE')>
        '}'
        <.end-token('BLOCK_CURLY_BRACKET_CLOSE')>
        <?ENDSTMT>
    }

    ## Captures and Signatures

    token capterm {
        <.start-element('CAPTURE')>
        <.start-token('CAPTURE_TERM')>
        '\\' <?before \S>
        <.end-token('CAPTURE_TERM')>
        [
        || <.start-token('CAPTURE_TERM')> '(' <.end-token('CAPTURE_TERM')>
           <.semiarglist>
           [ <.start-token('CAPTURE_TERM')> ')' <.end-token('CAPTURE_TERM')> ]?
        || <.termish>
        || <.start-token('CAPTURE_INVALID')> <?> <.end-token('CAPTURE_INVALID')>
        ]
        <.end-element('CAPTURE')>
    }

    token param_sep {
        <?before <.ws> [','||':'||';;'||';']>
        <.ws>
        <.start-token('PARAMETER_SEPARATOR')>
        [','||':'||';;'||';']
        <.end-token('PARAMETER_SEPARATOR')>
        <.ws>
    }

    token signature {
        :my $*IN_DECL = 'sig';
        <.ws>
        [
        || <?before '-->' || ')' || ']' || '{' || ':'\s || ';;' >
           <.start-token('END_OF_PARAMETERS')> <?> <.end-token('END_OF_PARAMETERS')>
        || <.parameter>
           [
               <.param_sep>
               [
               || <?before '-->' || ')' || ']' || '{' || ':'\s || ';;' >
                  <.start-token('END_OF_PARAMETERS')> <?> <.end-token('END_OF_PARAMETERS')>
               || <.parameter>
               ]?
           ]*
        || <?>
        ]
        <.ws>?
        { $*IN_DECL = '' }
        [
            <.start-element('RETURN_CONSTRAINT')>
            <.start-token('RETURN_ARROW')>
            '-->'
            <.end-token('RETURN_ARROW')>
            <.ws>
            [
            || [ <.typename> || <.value> ] <.ws>?
            || <.start-token('MISSING_RETURN_CONSTRAINT')>
               <?>
               <.end-token('MISSING_RETURN_CONSTRAINT')>
            ]
            <.end-element('RETURN_CONSTRAINT')>
        ]?
        { $*LEFTSIGIL = '@' }
    }

    token parameter {
        <.start-element('PARAMETER')>
        [
        ||  [
            <!before ['is' || 'hides' || 'does' || 'will' || 'of' || 'returns' || 'handles']>
            <.type_constraint>
            ]+
            [
            || [
               || <.start-token('TERM_DECLARATION_BACKSLASH')>
                  '\\'
                  <.end-token('TERM_DECLARATION_BACKSLASH')>
               || <.start-token('PARAMETER_QUANTIFIER')> '|' <.end-token('PARAMETER_QUANTIFIER')>
               || <?before '+' <.ident>>
                  <.start-token('IS_PARAM_TERM_QUANT')> <?> <.end-token('IS_PARAM_TERM_QUANT')>
                  <.start-token('PARAMETER_QUANTIFIER')> '+' <.end-token('PARAMETER_QUANTIFIER')>
               ]
               <.param_term>
            || <.start-token('PARAMETER_QUANTIFIER')>
               ['**'||'*'||'+']
               <.end-token('PARAMETER_QUANTIFIER')>
               [
               || <.param_var>
               || <.start-token('PARAMETER_INCOMPLETE')> <?> <.end-token('PARAMETER_INCOMPLETE')>
               ]
            || [ <.param_var> || <.named_param> ]
               [
                   <.start-token('PARAMETER_QUANTIFIER')>
                   <[?!]>
                   <.end-token('PARAMETER_QUANTIFIER')>
               ]?
            || <.start-token('PARAMETER_ANON')> <?> <.end-token('PARAMETER_ANON')>
            ]
        || [
           || <.start-token('PARAMETER_QUANTIFIER')> '\\' <.end-token('PARAMETER_QUANTIFIER')>
           || <.start-token('PARAMETER_QUANTIFIER')> '|' <.end-token('PARAMETER_QUANTIFIER')>
           || <?before '+' <.ident>>
              <.start-token('IS_PARAM_TERM_QUANT')> <?> <.end-token('IS_PARAM_TERM_QUANT')>
              <.start-token('PARAMETER_QUANTIFIER')> '+' <.end-token('PARAMETER_QUANTIFIER')>
           ]
           <.param_term>
        || <.start-token('PARAMETER_QUANTIFIER')>
           ['**'||'*'||'+']
           <.end-token('PARAMETER_QUANTIFIER')>
           [
           || <.param_var>
           || <.start-token('PARAMETER_INCOMPLETE')> <?> <.end-token('PARAMETER_INCOMPLETE')>
           ]
        || [ <.param_var> || <.named_param> ]
           [
               <.start-token('PARAMETER_QUANTIFIER')>
               <[?!]>
               <.end-token('PARAMETER_QUANTIFIER')>
           ]?
        ]
        <.ws>
        <.trait>*
        <.post_constraint>*
        <.default_value>?
        <.end-element('PARAMETER')>
    }

    token param_var {
        [
        || <.start-element('SIGNATURE')>
           <.start-token('SIGNATURE_BRACKET_OPEN')>
           '['
           <.end-token('SIGNATURE_BRACKET_OPEN')>
           <.signature>
           [
           <.start-token('SIGNATURE_BRACKET_CLOSE')>
           ']'
           <.end-token('SIGNATURE_BRACKET_CLOSE')>
           ]?
           <.end-element('SIGNATURE')>
        || <.start-element('SIGNATURE')>
           <.start-token('PARENTHESES_OPEN')>
           '('
           <.end-token('PARENTHESES_OPEN')>
           <.signature>
           [
           <.start-token('PARENTHESES_CLOSE')>
           ')'
           <.end-token('PARENTHESES_CLOSE')>
           ]?
           <.end-element('SIGNATURE')>
        || <.start-element('PARAMETER_VARIABLE')>
           <.variable>
           [
               <?before '['>
               <.start-token('PARAM_ARRAY_SHAPE')> <?> <.end-token('PARAM_ARRAY_SHAPE')>
               <.start-element('ARRAY_SHAPE')>
               <.postcircumfix>
               <.end-element('ARRAY_SHAPE')>
           ]?
           <.end-element('PARAMETER_VARIABLE')>
       ]
    }

    token param_term {
        <.defterm>?
    }

    token named_param {
        :my $*GOAL = ')';
        <?before ':' \S>
        <.start-element('NAMED_PARAMETER')>
        <.start-token('NAMED_PARAMETER_SYNTAX')>
        ':'
        <.end-token('NAMED_PARAMETER_SYNTAX')>
        [
        || <.start-token('NAMED_PARAMETER_NAME_ALIAS')>
           <.identifier>
           <.end-token('NAMED_PARAMETER_NAME_ALIAS')>
           [
               <.start-token('NAMED_PARAMETER_SYNTAX')>
               '('
               <.end-token('NAMED_PARAMETER_SYNTAX')>
               <.ws>
               [
                   [ <.named_param> || <.param_var> ]
                   [
                       <.ws>
                       [
                           <.start-token('NAMED_PARAMETER_SYNTAX')>
                           ')'
                           <.end-token('NAMED_PARAMETER_SYNTAX')>
                       ]?
                   ]?
               ]?
           ]?
        || <.param_var>
        ]?
        <.end-element('NAMED_PARAMETER')>
    }

    token default_value {
        :my $*IN_DECL = '';
        <.start-element('PARAMETER_DEFAULT')>
        <.start-token('INFIX')>
        '='
        <.end-token('INFIX')>
        <.ws>
        [ <.EXPR('i=')> <.ws>? ]?
        <.end-element('PARAMETER_DEFAULT')>
    }

    token type_constraint {
        :my $*IN_DECL = '';
        [
        || <?before 'where' <.ws>>
           <.start-element('WHERE_CONSTRAINT')>
           <.start-token('WHERE_CONSTRAINT')>
           'where'
           <.end-token('WHERE_CONSTRAINT')>
           <.ws>
           <.EXPR('i=')>?
           <.end-element('WHERE_CONSTRAINT')>
        || <.start-element('VALUE_CONSTRAINT')>
           <.value>
           <.end-element('VALUE_CONSTRAINT')>
        || <?before <[-−i+]> <.numish>>
           <.start-element('VALUE_CONSTRAINT')>
           <.start-token('PREFIX')> <[-−i+]> <.end-token('PREFIX')>
           <.numish>
           <.end-element('VALUE_CONSTRAINT')>
        || <.typename>
        ]
        <.ws>?
    }

    token post_constraint {
        :my $*IN_DECL = '';
        [
        || <.start-element('SIGNATURE')>
           <.start-token('SIGNATURE_BRACKET_OPEN')>
           '['
           <.end-token('SIGNATURE_BRACKET_OPEN')>
           <.signature>
           [
           <.start-token('SIGNATURE_BRACKET_CLOSE')>
           ']'
           <.end-token('SIGNATURE_BRACKET_CLOSE')>
           ]?
           <.end-element('SIGNATURE')>
        || <.start-element('SIGNATURE')>
           <.start-token('PARENTHESES_OPEN')>
           '('
           <.end-token('PARENTHESES_OPEN')>
           <.signature>
           [
           <.start-token('PARENTHESES_CLOSE')>
           ')'
           <.end-token('PARENTHESES_CLOSE')>
           ]?
           <.end-element('SIGNATURE')>
        || <?before 'where' <.ws>>
           <.start-element('WHERE_CONSTRAINT')>
           <.start-token('WHERE_CONSTRAINT')>
           'where'
           <.end-token('WHERE_CONSTRAINT')>
           <.ws>
           <.EXPR('i=')>?
           <.end-element('WHERE_CONSTRAINT')>
        ]
        <.ws>?
    }

    token initializer {
        :my $*EXPR_PREC = 'e=';
        :my $*DOTTY = 0;
        <?before ['=' || ':=' || '::=' || '.='] <.ws>>
        <.start-token('PARSING_INITIALIZER')> <?> <.end-token('PARSING_INITIALIZER')>
        <.start-element('INFIX')>
        [
        || <.start-token('INFIX')> '=' <.end-token('INFIX')>
           [ <?{ $*LEFTSIGIL eq '$' }> { $*EXPR_PREC = 'i<=' } ]?
        || <.start-token('INFIX')> ':=' <.end-token('INFIX')>
        || <.start-token('INFIX')> '::=' <.end-token('INFIX')>
        || <.start-token('INFIX')> '.=' <.end-token('INFIX')> { $*DOTTY = 1 }
        ]
        <.end-element('INFIX')>
        <.ws>
        [
        || <?{ $*DOTTY }>
           <.start-token('IS_DOTTY')> <?> <.end-token('IS_DOTTY')>
           [
           || <.dottyop>
           || <.start-token('INITIALIZER_MISSING')> <?> <.end-token('INITIALIZER_MISSING')>
           ]
        || <!{ $*DOTTY }>
           <.start-token('NOT_DOTTY')> <?> <.end-token('NOT_DOTTY')>
           [
           || <.EXPR($*EXPR_PREC)>
           || <.start-token('INITIALIZER_MISSING')> <?> <.end-token('INITIALIZER_MISSING')>
           ]
        ]
    }

    token trait {
        <.trait_mod> <.ws>?
    }

    token trait_mod {
        <.start-element('TRAIT')>
        [
        || <?before 'is' <.ws>>
           <.start-token('TRAIT')>
           'is'
           <.end-token('TRAIT')>
           <.ws>
           [
           || <.start-element('IS_TRAIT_NAME')>
              <.start-element('LONG_NAME')>
              <.start-token('NAME')>
              <.name>
              <.end-token('NAME')>
              <.longname_colonpairs>
              <.end-element('LONG_NAME')>
              <.end-element('IS_TRAIT_NAME')>
              <.circumfix>?
           || <.start-token('TRAIT_INCOMPLETE')> <?> <.end-token('TRAIT_INCOMPLETE')>
           ]
        || <?before 'hides' <.ws>>
           <.start-token('TRAIT')>
           'hides'
           <.end-token('TRAIT')>
           <.ws>
           [
           || <.typename>
           || <.start-token('TRAIT_INCOMPLETE')> <?> <.end-token('TRAIT_INCOMPLETE')>
           ]
        || <?before 'does' <.ws>>
           <.start-token('TRAIT')>
           'does'
           <.end-token('TRAIT')>
           <.ws>
           [
           || <.typename>
           || <.start-token('TRAIT_INCOMPLETE')> <?> <.end-token('TRAIT_INCOMPLETE')>
           ]
        || <?before 'will' <.ws>>
           <.start-token('TRAIT')>
           'will'
           <.end-token('TRAIT')>
           <.ws>
           [
           || <.start-token('NAME')>
              <.identifier>
              <.end-token('NAME')>
              <.ws>
              [
              || <.pblock>
              || <.start-token('TRAIT_INCOMPLETE')> <?> <.end-token('TRAIT_INCOMPLETE')>
              ]
           || <.start-token('TRAIT_INCOMPLETE')> <?> <.end-token('TRAIT_INCOMPLETE')>
           ]
        || <?before 'of' <.ws>>
           <.start-token('TRAIT')>
           'of'
           <.end-token('TRAIT')>
           <.ws>
           [
           || <.typename>
           || <.start-token('TRAIT_INCOMPLETE')> <?> <.end-token('TRAIT_INCOMPLETE')>
           ]
        || <?before 'returns' <.ws>>
           <.start-token('TRAIT')>
           'returns'
           <.end-token('TRAIT')>
           <.ws>
           [
           || <.typename>
           || <.start-token('TRAIT_INCOMPLETE')> <?> <.end-token('TRAIT_INCOMPLETE')>
           ]
        || <?before 'handles' <.ws>>
           <.start-token('TRAIT')>
           'handles'
           <.end-token('TRAIT')>
           <.ws>
           [
           || <.term>
           || <.start-token('TRAIT_INCOMPLETE')> <?> <.end-token('TRAIT_INCOMPLETE')>
           ]
        ]
        <.end-element('TRAIT')>
    }

    token regex_declarator {
        <.start-element('REGEX_DECLARATION')>
        [
        || <?before 'regex' <.kok>>
           <.start-token('REGEX_DECLARATOR')>
           'regex'
           <.end-token('REGEX_DECLARATOR')>
           <.kok>
           :my $*IN_DECL = 'regex';
           :my $*RX_S = 0;
           <.regex_def>
        || <?before 'rule' <.kok>>
           <.start-token('REGEX_DECLARATOR')>
           'rule'
           <.end-token('REGEX_DECLARATOR')>
           <.kok>
           :my $*IN_DECL = 'rule';
           :my $*RX_S = 1;
           <.regex_def>
        || <?before 'token' <.kok>>
           <.start-token('REGEX_DECLARATOR')>
           'token'
           <.end-token('REGEX_DECLARATOR')>
           <.kok>
           :my $*IN_DECL = 'token';
           :my $*RX_S = 0;
           <.regex_def>
        ]
        <.end-element('REGEX_DECLARATION')>
    }

    token regex_def {
        <.ws>
        <.routine_name>?
        <.ws>
        <.scope-push>
        [
            <.start-element('SIGNATURE')>
            <.start-token('PARENTHESES_OPEN')>
            '('
            <.end-token('PARENTHESES_OPEN')>
            <.signature>
            [
            <.start-token('PARENTHESES_CLOSE')>
            ')'
            <.end-token('PARENTHESES_CLOSE')>
            ]?
            <.end-element('SIGNATURE')>
        ]?
        <.ws>?
        <.trait>*
        { $*IN_DECL = '' }
        [
            <.start-element('BLOCKOID')>
            <.start-token('BLOCK_CURLY_BRACKET_OPEN')>
            '{'
            <.end-token('BLOCK_CURLY_BRACKET_OPEN')>
            <.ws>
            [
            || <.start-token('ONLY_STAR')> ['*'||'<...>'||'<*>'] <.end-token('ONLY_STAR')>
            || <.enter_regex_nibbler('{', '}')>
            || <.start-token('MISSING_REGEX')> <?> <.end-token('MISSING_REGEX')>
            ]
            <.ws>
            [
                <.start-token('BLOCK_CURLY_BRACKET_CLOSE')>
                '}'
                <.end-token('BLOCK_CURLY_BRACKET_CLOSE')>
                <?ENDSTMT>
            ]?
            <.end-element('BLOCKOID')>
        ]?
        <.scope-pop>
    }

    token type_declarator {
        || <?before ['enum' <.kok>]>
           <.start-element('ENUM')>
           <.start-token('TYPE_DECLARATOR')>
           'enum'
           <.end-token('TYPE_DECLARATOR')>
           <.kok>
           :my $*IN_DECL = 'enum';
           [
               [
               || <.start-token('NAME')> <.name> <.end-token('NAME')>
               || <.variable>
               || <.start-token('ENUM_ANON')> <?> <.end-token('ENUM_ANON')>
               ]
               { $*IN_DECL = '' }
               <.ws>
               <.trait>*
               [
                   <.ws>
                   [
                   || <![<(«]> <.start-token('ENUM_INCOMPLETE')> <?> <.end-token('ENUM_INCOMPLETE')>
                   || <.term>
                   ]
               ]?
               <.ws>?
           ]?
           <.end-element('ENUM')>
        || <?before ['subset' <.kok>]>
           <.start-element('SUBSET')>
           <.start-token('TYPE_DECLARATOR')>
           'subset'
           <.end-token('TYPE_DECLARATOR')>
           <.kok>
           :my $*IN_DECL = 'subset';
           [
               [
               || <.start-token('NAME')> <.name> <.end-token('NAME')>
               || <.start-token('SUBSET_ANON')> <?> <.end-token('SUBSET_ANON')>
               ]
               { $*IN_DECL = '' }
               <.ws>
               <.trait>*
               [
                   <.ws>
                   [
                       <?before ['where' <.ws>]>
                       <.start-token('WHERE_CONSTRAINT')>
                       'where'
                       <.end-token('WHERE_CONSTRAINT')>
                       <.ws>
                       [
                       || <.EXPR('e=')>
                       || <.start-token('SUBSET_INCOMPLETE')> <?> <.end-token('SUBSET_INCOMPLETE')>
                       ]
                   ]?
               ]?
               <.ws>?
           ]?
           <.end-element('SUBSET')>
        || <?before ['constant' <.kok>]>
           <.start-element('CONSTANT')>
           <.start-token('TYPE_DECLARATOR')>
           'constant'
           <.end-token('TYPE_DECLARATOR')>
           <.kok>
           :my $*IN_DECL = 'constant';
           [
           || <?before '\\'? <.defterm>>
              [
                  <.start-token('TERM_DECLARATION_BACKSLASH')>
                  '\\'
                  <.end-token('TERM_DECLARATION_BACKSLASH')>
              ]?
              <.defterm>
           || <.variable>
           || <.start-token('CONSTANT_ANON')> <?> <.end-token('CONSTANT_ANON')>
           ]
           { $*IN_DECL = '' }
           <.ws>
           <.trait>*
           [
           || <.initializer>
           || <.start-token('CONSTANT_MISSING_INITIALIZER')>
              <?>
              <.end-token('CONSTANT_MISSING_INITIALIZER')>
           ]
           <.end-element('CONSTANT')>
    }

    token sigil { <[$@%&]> }

    token twigil { <[.!^:*?=~]> <?before \w> }

    token package_declarator {
        :my $*IS_ROLE = 0;
        [
        || <?before <.package_kind> <.kok>>
           <.start-element('PACKAGE_DECLARATION')>
           <.start-token('PACKAGE_DECLARATOR')>
           <.package_kind>
           <.end-token('PACKAGE_DECLARATOR')>
           <.kok>
           <.package_def>
           <.end-element('PACKAGE_DECLARATION')>
        || <?before 'also' <.kok>>
           <.start-element('ALSO')>
           <.start-token('ALSO')> 'also' <.end-token('ALSO')> <.kok>
           <.ws>?
           <.trait>*
           <.ws>?
           <.end-element('ALSO')>
        || <?before 'trusts' <.kok>>
           <.start-element('TRUSTS')>
           <.start-token('TRUSTS')> 'trusts' <.end-token('TRUSTS')> <.kok>
           <.ws>?
           <.typename>?
           <.ws>?
           <.end-element('TRUSTS')>
        ]
    }

    token package_kind {
        'package' || 'module' || 'class' || 'grammar' || 'role' { $*IS_ROLE = 1 } ||
        'knowhow' || 'native' || 'slang' || 'monitor' || 'actor' || 'model'
    }

    token package_def {
        :my $*IN_DECL = 'package';
        <.ws>
        [
            <.start-element('LONG_NAME')>
            <.start-token('PACKAGE_NAME')>
            <.name>
            <.end-token('PACKAGE_NAME')>
            <.longname_colonpairs>
            <.end-element('LONG_NAME')>
            <.ws>
            [
                <?{ $*IS_ROLE }>
                <.start-element('ROLE_SIGNATURE')>
                <.start-token('TYPE_PARAMETER_BRACKET')>
                '['
                <.end-token('TYPE_PARAMETER_BRACKET')>
                <.signature>
                [
                    <.start-token('TYPE_PARAMETER_BRACKET')>
                    ']'
                    <.end-token('TYPE_PARAMETER_BRACKET')>
                ]?
                <.end-element('ROLE_SIGNATURE')>
                <.ws>?
            ]?
        ]?
        { $*IN_DECL = '' }
        <.trait>*
        <.scope-push>
        [
        || <?[{]> <.blockoid>
        || <?[;]>
           <.start-token('STATEMENT_TERMINATOR')>
           ';'
           <.end-token('STATEMENT_TERMINATOR')>
           <.ws>
           <.statementlist>?
        || <?>
        ]
        <.scope-pop>
    }

    # XXX Hack
    token desigilname { <.name> }

    token value {
        || <.number>
        || <.quote>
        || <.version>
    }

    token number {
        <.numish>
    }

    token numish {
        [
        || <.complex_number>
        || <.rat_number>
        || <.rad_number>
        || <.dec_number>
        || <.integer>
        || <.start-element('NUMBER_LITERAL')>
           <.start-token('NUMBER_LITERAL')>
           [ '∞' || 'NaN' >> || 'Inf' >> ]
           <.end-token('NUMBER_LITERAL')>
           <.end-element('NUMBER_LITERAL')>
        ]
    }

    token dec_number {
        ||  [
            <.start-element('NUMBER_LITERAL')>
            <.start-token('NUMBER_LITERAL')>
            [
            || [           '.' <.decint> ] <.escale>
            || [ <.decint> '.' <.decint> ] <.escale>
            || [ <.decint>               ] <.escale>
            ]
            <.end-token('NUMBER_LITERAL')>
            <.end-element('NUMBER_LITERAL')>
            ]
        ||  [
            <.start-element('RAT_LITERAL')>
            <.start-token('RAT_LITERAL')>
            [
            || [           '.' <.decint> ]
            || [ <.decint> '.' <.decint> ]
            ]
            <.end-token('RAT_LITERAL')>
            <.end-element('RAT_LITERAL')>
            ]
    }

    token escale { <[Ee]> <.sign> <.decint> }

    token sign { '+' || '-' || '−' || '' }

    token rat_number {
        <?before [ '<' <.bare_rat_number> '>']>
        <.start-element('RAT_LITERAL')>
        <.start-token('RAT_LITERAL')>
        '<'
        <.end-token('RAT_LITERAL')>
        <.bare_rat_number>
        <.start-token('RAT_LITERAL')>
        '>'
        <.end-token('RAT_LITERAL')>
        <.end-element('RAT_LITERAL')>
    }
    token bare_rat_number {
        <.signed_integer>
        <.start-token('RAT_LITERAL')>
        '/'
        <.end-token('RAT_LITERAL')>
        <.integer>
    }

    token complex_number {
        <?before [ '<' <.bare_complex_number> '>']>
        <.start-element('COMPLEX_LITERAL')>
        <.start-token('COMPLEX_LITERAL')>
        '<'
        <.end-token('COMPLEX_LITERAL')>
        <.bare_complex_number>
        <.start-token('COMPLEX_LITERAL')>
        '>'
        <.end-token('COMPLEX_LITERAL')>
        <.end-element('COMPLEX_LITERAL')>
    }

    token bare_complex_number {
        <.signed_number>
        <.start-token('COMPLEX_LITERAL')>
        <?[-−+]>
        <.end-token('COMPLEX_LITERAL')>
        <.signed_number>
        <.start-token('COMPLEX_LITERAL')>
        '\\'? 'i'
        <.end-token('COMPLEX_LITERAL')>
    }

    token signed_number {
        <.start-token('COMPLEX_LITERAL')>
        <.sign>
        <.end-token('COMPLEX_LITERAL')>
        <.number>
    }

    token rad_number {
        <?before [':' \d+ <.unsp>? <[<[(]>]>
        <.start-element('RADIX_NUMBER')>
        <.start-token('RADIX_NUMBER')>
        ':'
        <.end-token('RADIX_NUMBER')>
        <.start-token('RADIX_NUMBER')>
        \d+
        <.end-token('RADIX_NUMBER')>
        <.unsp>?
        [
        || <.start-token('RADIX_NUMBER')>
           '<'
           <.end-token('RADIX_NUMBER')>
           [
           || <.start-token('RADIX_NUMBER')> '0x' <.end-token('RADIX_NUMBER')>
           || <.start-token('RADIX_NUMBER')> '0o' <.end-token('RADIX_NUMBER')>
           || <.start-token('RADIX_NUMBER')> '0d' <.end-token('RADIX_NUMBER')>
           || <.start-token('RADIX_NUMBER')> '0b' <.end-token('RADIX_NUMBER')>
           ]?
           <.start-token('RADIX_NUMBER')>
           [
               <.rad_digits>
               ['.' <.rad_digits>]?
               ['*' <.radint> '**' <.radint>]?
           ]
           <.end-token('RADIX_NUMBER')>
           [
               <.start-token('RADIX_NUMBER')>
               '>'
               <.end-token('RADIX_NUMBER')>
           ]?
        || <.start-token('RADIX_NUMBER')>
           '['
           <.end-token('RADIX_NUMBER')>
           <.semilist>
           [
               <.start-token('RADIX_NUMBER')>
               ']'
               <.end-token('RADIX_NUMBER')>
           ]?
        || <.start-token('RADIX_NUMBER')>
           '('
           <.end-token('RADIX_NUMBER')>
           <.semilist>
           [
               <.start-token('RADIX_NUMBER')>
               ')'
               <.end-token('RADIX_NUMBER')>
           ]?
        ]
        <.end-element('RADIX_NUMBER')>
    }

    token rad_digits { <.rad_digit>+ [ '_' <.rad_digit>+ ]* }
    token rad_digit  { \d || <[ a..z A..Z ａ..ｚ Ａ..Ｚ ]> }
    token radint    { <.integer_lex> }

    token integer {
        <.start-element('INTEGER_LITERAL')>
        <.start-token('INTEGER_LITERAL')>
        <.integer_lex>
        <.end-token('INTEGER_LITERAL')>
        <.end-element('INTEGER_LITERAL')>
    }

    token signed_integer {
        <.start-token('RAT_LITERAL')>
        <.sign>
        <.end-token('RAT_LITERAL')>
        <.integer>
    }

    token integer_lex {
        || '0'
            [
            || 'b' '_'? <.binint>
            || 'o' '_'? <.octint>
            || 'x' '_'? <.hexint>
            || 'd' '_'? <.decint>
            || <.decint>
            ]
        || <.decint>
    }

    token decint { [\d+]+ % '_' }
    token hexint { [[\d||<[ a..f A..F ａ..ｆ Ａ..Ｆ ]>]+]+ % '_' }
    token octint { [\d+]+ % '_' }
    token binint { [\d+]+ % '_' }
    token charname {
        || <.integer_lex>
        || <.alpha> [<!before \s* <[ \] , # ]> > .]*
    }

    # XXX These are slightly cheating in that they \s* instead of <.ws>, to
    # avoid token nesting issues in the places using them. It's most likely
    # that we'll get away with that.
    token hexints { [\s*<.hexint>\s*]+ % ',' }
    token octints { [\s*<.octint>\s*]+ % ',' }
    token charnames { [\s*<.charname>\s*]+ % ',' }

    token charspec {
        [
        || '[' <.charnames> ']'
        || \d+ [ '_' \d+]*
        || <[ ?..Z ]>
        ]
    }

    token typename {
        <.start-element('TYPE_NAME')>
        <.start-element('LONG_NAME')>
        [
        || <.start-token('NAME')>
           '::?' <.identifier>
           <.end-token('NAME')>
           <.longname_colonpairs>
        || <.start-token('NAME')>
           <.name>
           <.end-token('NAME')>
           <.longname_colonpairs>
        ]
        <.end-element('LONG_NAME')>
        <.unsp>?
        [
            <.start-token('TYPE_PARAMETER_BRACKET')>
            '['
            <.end-token('TYPE_PARAMETER_BRACKET')>
            <.arglist>
            [
                <.ws>?
                <.start-token('TYPE_PARAMETER_BRACKET')>
                ']'
                <.end-token('TYPE_PARAMETER_BRACKET')>
            ]?
        ]?
        <.unsp>?
        [
            <.start-token('TYPE_COERCION_PARENTHESES_OPEN')>
            '('
            <.end-token('TYPE_COERCION_PARENTHESES_OPEN')>
            <.ws>
            <.typename>?
            <.ws>
            [
                || <.start-token('TYPE_COERCION_PARENTHESES_CLOSE')>
                   ')'
                   <.end-token('TYPE_COERCION_PARENTHESES_CLOSE')>
                || <.start-token('INCOMPLETE_TYPE_NAME')> <?> <.end-token('INCOMPLETE_TYPE_NAME')>
            ]
        ]?
        [
            <?before <.ws> 'of' <.ws>>
            <.ws>
            <.start-token('NAME')>
            'of'
            <.end-token('NAME')>
            <.ws>
            [
            || <.typename>
            || <.start-token('INCOMPLETE_TYPE_NAME')> <?> <.end-token('INCOMPLETE_TYPE_NAME')>
            ]
        ]?
        <.end-element('TYPE_NAME')>
    }

    token quote {
         <.quote_quasi> || <.quote_rxlang> || <.quote_tr> || <.quote_qlang> 
    }

    token quote_qlang {
        :my $*Q_Q = 0;
        :my $*Q_QQ = 0;
        :my $*Q_BACKSLASH = 0;
        :my $*Q_QBACKSLASH = 0;
        :my $*Q_QQBACKSLASH = 0;
        :my $*Q_CLOSURES = 0;
        :my $*Q_SCALARS = 0;
        :my $*Q_ARRAYS = 0;
        :my $*Q_HASHES = 0;
        :my $*Q_FUNCTIONS = 0;
        :my $*Q_TO = 0;
        <.start-element('STRING_LITERAL')>
        [
        || <?before ['Q' <.quote_mod>? <.ws> [<.has-delimiter> || <.quotepair>]]>
           <.start-token('STRING_LITERAL_QUOTE_SYNTAX')> 'Q' <.end-token('STRING_LITERAL_QUOTE_SYNTAX')>
           <.quote_mod_Q>?
           <.quibble>
        || <?before ['qq' <.quote_mod>? <.ws> [<.has-delimiter> || <.quotepair>]]>
           <.start-token('STRING_LITERAL_QUOTE_SYNTAX')> 'qq' <.end-token('STRING_LITERAL_QUOTE_SYNTAX')>
           { $*Q_QQ = 1 }
           { $*Q_BACKSLASH = 1 }
           { $*Q_QQBACKSLASH = 1 }
           { $*Q_CLOSURES = 1 }
           { $*Q_SCALARS = 1 }
           { $*Q_ARRAYS = 1 }
           { $*Q_HASHES = 1 }
           { $*Q_FUNCTIONS = 1 }
           <.quote_mod_Q>?
           <.quibble>
        || <?before ['q' <.quote_mod>? <.ws> [<.has-delimiter> || <.quotepair>]]>
           <.start-token('STRING_LITERAL_QUOTE_SYNTAX')> 'q' <.end-token('STRING_LITERAL_QUOTE_SYNTAX')>
           { $*Q_Q = 1 }
           { $*Q_QBACKSLASH = 1 }
           <.quote_mod_Q>?
           <.quibble>
        || <.start-token('STRING_LITERAL_QUOTE_OPEN')> '\'' <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           <.quote_q('\'', '\'', '\'')>
           [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> '\'' <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
        || <.start-token('STRING_LITERAL_QUOTE_OPEN')> '‘' <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           <.quote_q('‘', '’', '’')>
           [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> '’' <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
        || <.start-token('STRING_LITERAL_QUOTE_OPEN')> '‚' <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           <.quote_q('‚', '’', '‘')>
           [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> <[’‘]> <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
        || <.start-token('STRING_LITERAL_QUOTE_OPEN')> '’' <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           <.quote_q('’', '’', '‘')>
           [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> <[’‘]> <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
        || <.start-token('STRING_LITERAL_QUOTE_OPEN')> '"' <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           <.quote_qq('"', '"', '"')>
           [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> '"' <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
        || <.start-token('STRING_LITERAL_QUOTE_OPEN')> '“' <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           <.quote_qq('“', '”', '”')>
           [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> '”' <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
        || <.start-token('STRING_LITERAL_QUOTE_OPEN')> '„' <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           <.quote_qq('„', '”', '“')>
           [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> <[”“]> <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
        || <.start-token('STRING_LITERAL_QUOTE_OPEN')> '”' <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           <.quote_qq('”', '”', '“')>
           [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> <[”“]> <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
        || <.start-token('STRING_LITERAL_QUOTE_OPEN')> '｢' <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           <.quote_Q('｢', '｣', '｣')>
           [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> '｣' <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
        ]
        <.end-element('STRING_LITERAL')>
    }

   token quote_rxlang {
        <.start-element('QUOTE_REGEX')>
        :my $*RX_S = 0;
        [
        || <.start-token('QUOTE_REGEX')> '/' <.end-token('QUOTE_REGEX')>
           [
           || <.enter_regex_nibbler('/', '/')>
           || <.start-token('MISSING_REGEX')> <?> <.end-token('MISSING_REGEX')>
           ]
           [<.start-token('QUOTE_REGEX')> '/' <.end-token('QUOTE_REGEX')>]?
        || <?before ['rx' <.ws> [<.has-delimiter> || <.quotepair>]]>
           <.start-token('QUOTE_REGEX')> 'rx' <.end-token('QUOTE_REGEX')>
           <.quibble_rx>
        || <?before ['ms' <.ws> [<.has-delimiter> || <.quotepair>]]>
           <.start-token('QUOTE_REGEX')> 'ms' <.end-token('QUOTE_REGEX')>
           { $*RX_S = 1 }
           <.quibble_rx>
        || <?before ['m' <.ws> [<.has-delimiter> || <.quotepair>]]>
           <.start-token('QUOTE_REGEX')> 'm' <.end-token('QUOTE_REGEX')>
           <.quibble_rx>
        || <?before ['Ss' [<.has-delimiter> || <.quotepair>]]>
           <.start-token('QUOTE_REGEX')> 'Ss' <.end-token('QUOTE_REGEX')>
           { $*RX_S = 1 }
           [
           || [ <.quotepair_rx> <.ws> ]+ <.sibble>?
           || <.sibble>
           ]
        || <?before ['ss' [<.has-delimiter> || <.quotepair>]]>
           <.start-token('QUOTE_REGEX')> 'ss' <.end-token('QUOTE_REGEX')>
           { $*RX_S = 1 }
           [
           || [ <.quotepair_rx> <.ws> ]+ <.sibble>?
           || <.sibble>
           ]
        || <?before ['S' [<.has-delimiter> || <.quotepair>]]>
           <.start-token('QUOTE_REGEX')> 'S' <.end-token('QUOTE_REGEX')>
           [
           || [ <.quotepair_rx> <.ws> ]+ <.sibble>?
           || <.sibble>
           ]
        || <?before ['s' [<.has-delimiter> || <.quotepair>]]>
           <.start-token('QUOTE_REGEX')> 's' <.end-token('QUOTE_REGEX')>
           [
           || [ <.quotepair_rx> <.ws> ]+ <.sibble>?
           || <.sibble>
           ]
        ]
        <.end-element('QUOTE_REGEX')>
    }

    token quote_tr {
        <?before [['tr'||'TR'] <.ws> [<.has-delimiter> || <.quotepair>]]>
        <.start-element('TRANSLITERATION')>
        [
        || <.start-token('QUOTE_REGEX')> 'tr' <.end-token('QUOTE_REGEX')>
        || <.start-token('QUOTE_REGEX')> 'TR' <.end-token('QUOTE_REGEX')>
        ]
        <.ws>
        [
        || [ <.quotepair_rx> <.ws> ]+ <.tribble>
        || <.tribble>
        ]
        <.end-element('TRANSLITERATION')>
    }

    token quote_Q($*STARTER, $*STOPPER, $*ALT_STOPPER) {
        :my $*DELIM = '';
        <.quote_nibbler>
    }

    token quote_q($*STARTER, $*STOPPER, $*ALT_STOPPER) {
        :my $*DELIM = '';
        { $*Q_QBACKSLASH = 1 }
        <.quote_nibbler>
    }

    token quote_qq($*STARTER, $*STOPPER, $*ALT_STOPPER) {
        :my $*DELIM = '';
        { $*Q_BACKSLASH = 1 }
        { $*Q_QQBACKSLASH = 1 }
        { $*Q_CLOSURES = 1 }
        { $*Q_SCALARS = 1 }
        { $*Q_ARRAYS = 1 }
        { $*Q_HASHES = 1 }
        { $*Q_FUNCTIONS = 1 }
        <.quote_nibbler>
    }

    token has-delimiter {
        :my $*STARTER = '';
        :my $*STOPPER = '';
        :my $*ALT_STOPPER = '';
        <.peek-delimiters>
    }

    token quibble {
        :my $*STARTER = '';
        :my $*STOPPER = '';
        :my $*ALT_STOPPER = '';
        :my $*DELIM = '';
        <.ws>
        [
        || [ <.quotepair_Q> <.ws> ]+
           [
               <.peek-delimiters>
               <.start-token('STRING_LITERAL_QUOTE_OPEN')>
               $*STARTER
               <.end-token('STRING_LITERAL_QUOTE_OPEN')>
               [ <?{ $*Q_TO }> <.start-queue-heredoc> ]?
               <.quote_nibbler>
               [ <?{ $*Q_TO }> <.end-queue-heredoc> ]?
               [
                   <.start-token('STRING_LITERAL_QUOTE_CLOSE')>
                   <.stopper>
                   <.end-token('STRING_LITERAL_QUOTE_CLOSE')>
               ]?
           ]?
        || <.peek-delimiters>
           <.start-token('STRING_LITERAL_QUOTE_OPEN')>
           $*STARTER
           <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           <.quote_nibbler>
           [
               <.start-token('STRING_LITERAL_QUOTE_CLOSE')>
               $*STOPPER
               <.end-token('STRING_LITERAL_QUOTE_CLOSE')>
           ]?
        ]
    }

    token quibble_rx {
        :my $*STARTER = '';
        :my $*STOPPER = '';
        :my $*ALT_STOPPER = '';
        :my $*DELIM = '';
        <.ws>
        [
        || [ <.quotepair_rx> <.ws> ]+
           [
               <.peek-delimiters>
               <.start-token('QUOTE_REGEX')>
               $*STARTER
               <.end-token('QUOTE_REGEX')>
               <.enter_regex_nibbler($*STARTER, $*STOPPER)>
               [
                   <.start-token('QUOTE_REGEX')>
                   $*STOPPER
                   <.end-token('QUOTE_REGEX')>
               ]?
           ]?
        || <.peek-delimiters>
           <.start-token('QUOTE_REGEX')>
           $*STARTER
           <.end-token('QUOTE_REGEX')>
           <.enter_regex_nibbler($*STARTER, $*STOPPER)>
           [
               <.start-token('QUOTE_REGEX')>
               $*STOPPER
               <.end-token('QUOTE_REGEX')>
           ]?
        ]
    }

    token sibble {
        :my $*STARTER = '';
        :my $*STOPPER = '';
        :my $*ALT_STOPPER = '';
        :my $*DELIM = '';
        :my $*Q_Q = 0;
        :my $*Q_QQ = 1;
        :my $*Q_BACKSLASH = 1;
        :my $*Q_QBACKSLASH = 0;
        :my $*Q_QQBACKSLASH = 1;
        :my $*Q_CLOSURES = 1;
        :my $*Q_SCALARS = 1;
        :my $*Q_ARRAYS = 1;
        :my $*Q_HASHES = 1;
        :my $*Q_FUNCTIONS = 1;
        <.peek-delimiters>
        <.start-token('QUOTE_REGEX')>
        $*STARTER
        <.end-token('QUOTE_REGEX')>
        <.enter_regex_nibbler($*STARTER, $*STOPPER)>
        [
            <.start-token('QUOTE_REGEX')>
            $*STOPPER
            <.end-token('QUOTE_REGEX')>
            [
            || <?{ $*STARTER ne $*STOPPER }>
               <.start-token('SUBST_ASSIGNISH')> <?> <.end-token('SUBST_ASSIGNISH')>
               <.ws>
               [
                   [
                   || <?before [<![!]> <.infixish_non_assignment_meta> '=']>
                      <.start-element('ASSIGN_METAOP')>
                      <.start-token('ASSIGN_METAOP')> <?> <.end-token('ASSIGN_METAOP')>
                      <.infixish_non_assignment_meta>
                      <.start-token('METAOP')>
                      '='
                      <.end-token('METAOP')>
                      <.end-element('ASSIGN_METAOP')>
                   || <.start-element('INFIX')>
                      <.start-token('INFIX')>
                      '='
                      <.end-token('INFIX')>
                      <.end-element('INFIX')>
                   ]
                   [ <.ws> <.EXPR('i')>? ]?
               ]?
            || <.quote_nibbler>
               [
                   <.start-token('QUOTE_REGEX')>
                   $*STOPPER
                   <.end-token('QUOTE_REGEX')>
               ]?
            ]?
        ]?
    }

    token tribble {
        :my $*STARTER = '';
        :my $*STOPPER = '';
        :my $*ALT_STOPPER = '';
        :my $*DELIM = '';
        <.peek-delimiters>
        <.start-token('QUOTE_REGEX')>
        $*STARTER
        <.end-token('QUOTE_REGEX')>
        <.tribbler>
        [
            <.start-token('QUOTE_REGEX')>
            $*STOPPER
            <.end-token('QUOTE_REGEX')>
            [
            || <?{ $*STARTER ne $*STOPPER }>
               <?before $*STARTER>
               <.start-token('TR_DISTINCT_START_STOP')> <?> <.end-token('TR_DISTINCT_START_STOP')>
               <.start-token('QUOTE_REGEX')>
               $*STARTER
               <.end-token('QUOTE_REGEX')>
               <.tribbler>
               [
                   <.start-token('QUOTE_REGEX')>
                   $*STOPPER
                   <.end-token('QUOTE_REGEX')>
               ]?
            || <.tribbler>
               [
                   <.start-token('QUOTE_REGEX')>
                   $*STOPPER
                   <.end-token('QUOTE_REGEX')>
               ]?
            ]?
        ]?
    }

    # The tr nibbler doesn't really use anything of the standard nibbler, so
    # we just write the stop logic inline here.
    token tribbler {
        :my $*CCSTATE = '';
        [
            <!stopper>
            [
            || <.start-token('WHITE_SPACE')> \s+ <.end-token('WHITE_SPACE')>
               [ <?[#]> <.ws> ]?
            || <.start-token('TRANS_ESCAPE')>
               '\\'
               [
               || $*STARTER
               || $*STOPPER
               || <[\\aAbBeEfFnNrRtT0]>
               || <[dDhHsSvVwW]> { $*CCSTATE = '' }
               || <[oO]> [ <.octint> || '[' <.octints> ']' ]
               || <[xX]> [ <.hexint> || '[' <.hexints> ']' ]
               || <[cC]> <.charspec>
               || \W
               ]
               <.end-token('TRANS_ESCAPE')>
               <.ccstate>
            || <.start-token('TRANS_BAD')>
               '\\' .
               <.end-token('TRANS_BAD')>
            || <?before '..'>
               [
               || <?{ $*CCSTATE }> <?{ $*CCSTATE ne '..' }>
                  <.start-token('TRANS_RANGE')> '..' <.end-token('TRANS_RANGE')>
                  { $*CCSTATE = '..' }
               || <.start-token('TRANS_BAD')> '..' <.end-token('TRANS_BAD')>
               ]
            || <.start-token('TRANS_CHAR')> \S <.end-token('TRANS_CHAR')> <.ccstate>
            ]
        ]*
    }

    token ccstate {
        || <?{ $*CCSTATE eq '..' }> { $*CCSTATE = '' }
        || { $*CCSTATE = 'ok' }
    }

    # This delegates to quote_mod to actually lex/parse, but looks ahead
    # first to see if we need to tweak the parse state for the Q language.
    token quote_mod_Q {
        [
        || <?before 's'> { $*Q_SCALARS = 1 }
        || <?before 'a'> { $*Q_ARRAYS = 1 }
        || <?before 'h'> { $*Q_HASHES = 1 }
        || <?before 'f'> { $*Q_FUNCTIONS = 1 }
        || <?before 'c'> { $*Q_CLOSURES = 1 }
        || <?before 'b'> { $*Q_BACKSLASHES = 1 }
        ]?
        <.quote_mod>
    }

    token quote_mod {
        <.start-token('QUOTE_MOD')>
        [ 'ww' || 'to' || <[wxsahfcb]> ]
        <.end-token('QUOTE_MOD')>
    }

    # This delegates to quotepair to actually lex/parse, but looks ahead
    # first to see if we need to tweak the parse state for the Q language.
    token quotepair_Q {
        [
        || <?before ':b''ackslash'? >>> { $*Q_BACKSLASH = 1 }
        || <?before ':!b''ackslash'? >>> { $*Q_BACKSLASH = 0 }
        || <?before ':s''calar'? >>> { $*Q_SCALARS = 1 }
        || <?before ':!s''calar'? >>> { $*Q_SCALARS = 0 }
        || <?before ':a''rray'? >>> { $*Q_ARRAYS = 1 }
        || <?before ':!a''rray'? >>> { $*Q_ARRAYS = 0 }
        || <?before ':h''ash'? >>> { $*Q_HASHES = 1 }
        || <?before ':!h''ash'? >>> { $*Q_HASHES = 0 }
        || <?before ':f''unction'? >>> { $*Q_FUNCTIONS = 1 }
        || <?before ':!f''unction'? >>> { $*Q_FUNCTIONS = 0 }
        || <?before ':c''losure'? >>> { $*Q_CLOSURES = 1 }
        || <?before ':!c''losure'? >>> { $*Q_CLOSURES = 0 }
        || <?before ':to' >>> { $*Q_TO = 1 }
        || <!{ $*Q_QQ }> <!{ $*Q_Q }>
           [
           || <?before ':qq' >>>
              { $*Q_QQ = 1 }
              { $*Q_BACKSLASH = 1 }
              { $*Q_QQBACKSLASH = 1 }
              { $*Q_CLOSURES = 1 }
              { $*Q_SCALARS = 1 }
              { $*Q_ARRAYS = 1 }
              { $*Q_HASHES = 1 }
              { $*Q_FUNCTIONS = 1 }
           || <?before ':q' >>>
              { $*Q_Q = 1 }
              { $*Q_QBACKSLASH = 1 }
           ]
        ]?
        <.quotepair>
    }

    # This delegates to quotepair to actually lex/parse, but looks ahead
    # first to see if we need to tweak the parse state for the regex
    # language.
    token quotepair_rx {
        [
        || <?before ':s''igspace'? >>> { $*RX_S = 1 }
        || <?before ':!s''igspace'? >>> { $*RX_S = 0 }
        ]?
        <.quotepair>
    }

    # General case of quotepair parses any pair, but has no effect on the
    # parse state.
    token quotepair {
        <.start-element('QUOTE_PAIR')>
        [
        || <?before [':' \d+ <.identifier>]>
           <.start-token('QUOTE_PAIR')>
           ':'
           <.end-token('QUOTE_PAIR')>
           <.start-token('INTEGER_LITERAL')>
           \d+
           <.end-token('INTEGER_LITERAL')>
           <.start-token('QUOTE_PAIR')>
           <.identifier>
           <.end-token('QUOTE_PAIR')>
        || <?before [':!' <.identifier>]>
           <.start-token('QUOTE_PAIR')>
           ':!'
           <.end-token('QUOTE_PAIR')>
           <.start-token('QUOTE_PAIR')>
           <.identifier>
           <.end-token('QUOTE_PAIR')>
        || <?before [':' <.identifier>]>
           <.start-token('QUOTE_PAIR')>
           ':'
           <.end-token('QUOTE_PAIR')>
           <.start-token('QUOTE_PAIR')>
           <.identifier>
           <.end-token('QUOTE_PAIR')>
           [
               <?[(]>
               <.start-token('COLON_PAIR_HAS_VALUE')> <?> <.end-token('COLON_PAIR_HAS_VALUE')>
               <.circumfix>
           ]?
        ]
        <.end-element('QUOTE_PAIR')>
    }

    token quote_nibbler {
        [
            <!stopper>
            [
            || <?{ $*STARTER }>
               <.start-token('STRING_LITERAL_QUOTE_OPEN')> <.starter> <.end-token('STRING_LITERAL_QUOTE_OPEN')>
               <.quote_nibbler>
               [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> <.stopper> <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
            || <.quote_escape>
            || <.start-token('STRING_LITERAL_CHAR')> <.alpha>+ <.end-token('STRING_LITERAL_CHAR')>
            || <.start-token('STRING_LITERAL_CHAR')> . <.end-token('STRING_LITERAL_CHAR')>
            ]
        ]*
    }

    token starter {
        $*STARTER
    }

    token stopper {
        || <?{ $*DELIM }> ^^ \h* $*DELIM \h* $$ [\r\n || \v]?
        || <!{ $*DELIM }>
           [
           || $*STOPPER
           || $*ALT_STOPPER
           ]
    }

    token quote_escape {
        || <?[$]> <?{ $*Q_SCALARS }>
            :my $*QSIGIL = '$';
            [
            || <?variable>
               <.start-token('ESCAPE_SCALAR')> <?> <.end-token('ESCAPE_SCALAR')>
               [
               || <.variable> <.quote_interpolation_postfix>*
               || <.start-token('BAD_ESCAPE')> '$' <.end-token('BAD_ESCAPE')>
               ]
            || <.start-token('BAD_ESCAPE')> '$' <.end-token('BAD_ESCAPE')>
            ]
        || <?[@]> <?{ $*Q_ARRAYS }>
            :my $*QSIGIL = '@';
            <?before [<.variable> <.quote_interpolation_postfix>]>
            <.start-token('ESCAPE_ARRAY')> <?> <.end-token('ESCAPE_ARRAY')>
            <.variable>
            <.quote_interpolation_postfix>+
        || <?[%]> <?{ $*Q_HASHES }>
            :my $*QSIGIL = '%';
            <?before [<.variable> <.quote_interpolation_postfix>]>
            <.start-token('ESCAPE_HASH')> <?> <.end-token('ESCAPE_HASH')>
            <.variable>
            <.quote_interpolation_postfix>+
        || <?[&]> <?{ $*Q_FUNCTIONS }>
            :my $*QSIGIL = '&';
            <?before [<.variable> <.quote_interpolation_postfix>]>
            <.start-token('ESCAPE_FUNCTION')> <?> <.end-token('ESCAPE_FUNCTION')>
            <.variable>
            <.quote_interpolation_postfix>+
        || <?[\\]> <?{ $*Q_BACKSLASH }>
            <.start-token('STRING_LITERAL_ESCAPE')>
            '\\'
            [
            || <[abefnrt0\\]>
            || 'o' [ <.octint> || '[' <.octints> ']' ]
            || 'x' [ <.hexint> || '[' <.hexints> ']' ]
            || 'c' <.charspec>
            || <.starter>
            || <.stopper>
            ]
            <.end-token('STRING_LITERAL_ESCAPE')>
        || <?[\\]> <?{ $*Q_BACKSLASH }>
           <.start-token('BAD_ESCAPE')> <[1..9]>\d* <.end-token('BAD_ESCAPE')>
        || <?[\\]> <?{ $*Q_QQBACKSLASH }>
           <.start-token('STRING_LITERAL_ESCAPE')>
           '\\' \W
           <.end-token('STRING_LITERAL_ESCAPE')>
        || <?before '\\' 'q'> [<?{ $*Q_QBACKSLASH }> || <?{ $*Q_QQBACKSLASH }>]
           <.start-token('STRING_LITERAL_REQUOTE_ESCAPE')>
           '\\'
           <.end-token('STRING_LITERAL_REQUOTE_ESCAPE')>
           <.quote>?
        || <?[\\]> <?{ $*Q_QQBACKSLASH }>
           <.start-token('BAD_ESCAPE')> '\\' \w <.end-token('BAD_ESCAPE')>
        || <?[\\]> <?{ $*Q_QBACKSLASH }>
            <.start-token('STRING_LITERAL_ESCAPE')>
            '\\'
            [
            || '\\'
            || <.starter>
            || <.stopper>
            ]
            <.end-token('STRING_LITERAL_ESCAPE')>
        || <?[{]> <?{ $*Q_CLOSURES }> <.block>
    }

    token quote_quasi {
        <?before 'quasi' <.ws> '{'>
        <.start-element('QUASI')>
        <.start-token('QUASI')>
        'quasi'
        <.end-token('QUASI')>
        <.ws>
        <.block>?
        <.end-element('QUASI')>
    }

    token quote_interpolation_postfix {
        :my $*PREC = '';
        :my $*SUB_PREC = '';
        :my $*ASSOC = '';
        <?before [<.interpolation_opener> || <.postfixish> <?bracket-ending>]>
        <.start-token('POSTFIX_INTERPOLATIN')> <?> <.end-token('POSTFIX_INTERPOLATIN')>
        <.postfixish>
    }

    # We want to provide better feedback when the user starts to type some
    # kind of postcircumfix that has an opening thingy. Thus we recognize
    # various "openers", on the basis that Raku will give a parse fail if
    # they are not closed later anyway.
    token interpolation_opener {
        || <?[([{<]>
        || '.' <[^&*+?$@&]>? [ <.name> <.longname_colonpairs> || <?["']> <.quote> ] '('
    }

    token circumfix {
        :my $*Q_BACKSLASH = 0;
        :my $*Q_QBACKSLASH = 0;
        :my $*Q_QQBACKSLASH = 0;
        :my $*Q_CLOSURES = 0;
        :my $*Q_SCALARS = 0;
        :my $*Q_ARRAYS = 0;
        :my $*Q_HASHES = 0;
        :my $*Q_FUNCTIONS = 0;
        [
        || <.start-element('PARENTHESIZED_EXPRESSION')>
           <.start-token('PARENTHESES_OPEN')> '(' <.end-token('PARENTHESES_OPEN')>
           <.semilist>
           [ <.start-token('PARENTHESES_CLOSE')> ')' <.end-token('PARENTHESES_CLOSE')> ]?
           <.end-element('PARENTHESIZED_EXPRESSION')>
        || <.start-element('ARRAY_COMPOSER')>
           <.start-token('ARRAY_COMPOSER_OPEN')> '[' <.end-token('ARRAY_COMPOSER_OPEN')>
           <.semilist>
           [ <.start-token('ARRAY_COMPOSER_CLOSE')> ']' <.end-token('ARRAY_COMPOSER_CLOSE')> ]?
           <.end-element('ARRAY_COMPOSER')>
        || <?[{]>
           <.start-token('BARE_BLOCK')> <?> <.end-token('BARE_BLOCK')>
           <.start-element('BLOCK_OR_HASH')> <.scope-push> <.blockoid> <.scope-pop> <.end-element('BLOCK_OR_HASH')>
        || <.start-element('STRING_LITERAL')>
           <.start-token('STRING_LITERAL_QUOTE_OPEN')>
           '<<'
           <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           [
               <.quote_qq('<<', '>>', '>>')>
               [
                   <.start-token('STRING_LITERAL_QUOTE_CLOSE')>
                   '>>'
                   <.end-token('STRING_LITERAL_QUOTE_CLOSE')>
               ]?
           ]?
           <.end-element('STRING_LITERAL')>
        || <.start-element('STRING_LITERAL')>
           <.start-token('STRING_LITERAL_QUOTE_OPEN')>
           '«'
           <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           [
               <.quote_qq('«', '»', '»')>
               [
                   <.start-token('STRING_LITERAL_QUOTE_CLOSE')>
                   '»'
                   <.end-token('STRING_LITERAL_QUOTE_CLOSE')>
               ]?
           ]?
           <.end-element('STRING_LITERAL')>
        || <.start-element('STRING_LITERAL')>
           <.start-token('STRING_LITERAL_QUOTE_OPEN')>
           '<'
           <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           [
               <.quote_q('<', '>', '>')>
               [
                   <.start-token('STRING_LITERAL_QUOTE_CLOSE')>
                   '>'
                   <.end-token('STRING_LITERAL_QUOTE_CLOSE')>
               ]?
           ]?
           <.end-element('STRING_LITERAL')>
        ]
    }

    token EXPR($*PRECLIM) {
        :my $*LEFTSIGIL = '';
        :my $*PREC = '';
        :my $*SUB_PREC = '';
        :my $*ASSOC = '';
        :my $*NEXT_TERM = '';
        :my $*FAKE = 0;

        <.opp-start-expr>

        <.opp-start-prefixes>
        [
        || [<.prefixish> <.opp-push-prefix>]+ <.opp-end-prefixes> <.term>?
        || <.opp-end-prefixes> <.term>
        ]
        <.opp-start-postfixes>
        [<.postfixish> <.opp-push-postfix>]*
        <.opp-end-postfixes>

        [
            <?before <.ws> <.infixish('')> <.ws>>
            <.ws>
            <.opp-start-infix>
            { $*NEXT_TERM = '' } { $*FAKE = 0 }
            <.infixish('')>
            <.opp-end-infix>
            { $*SUB_PREC = '' }
            [<!before <.ws> <[\]})]>> <.ws>]?

            [
            || <?{ $*FAKE }> <.start-token('FAKE_INFIX')> <?> <.end-token('FAKE_INFIX')>
            || <.opp-start-prefixes>
               [
               || [<.prefixish> <.opp-push-prefix>]+ <.opp-end-prefixes>
                  [
                  || <.nextterm>
                     <.opp-start-postfixes>
                     [<.postfixish> <.opp-push-postfix>]*
                  || <.opp-start-postfixes>
                  ]
               || <.opp-end-prefixes>
                  [
                  || <.nextterm>
                     <.opp-start-postfixes>
                     [<.postfixish> <.opp-push-postfix>]*
                  || <.opp-start-postfixes>
                  ]
               ]
               <.opp-end-postfixes>
            ]
        ]*
        <.opp-end-expr>

        # Zero-width marker token to get nesting correct.
        [
        || <!before $>
           <.start-token('END_OF_EXPR')>
           <?>
           <.end-token('END_OF_EXPR')>
        || <?>
        ]
    }

    token nextterm {
        || <?{ $*NEXT_TERM eq 'nulltermish' }>
           [
           || <!terminator> <.term>
           || <.start-element('NULL_TERM')>
              <.start-token('NULL_TERM')>
              <?>
              <.end-token('NULL_TERM')>
              <.end-element('NULL_TERM')>
           ]
        || <?{ $*NEXT_TERM eq 'dotty' }>
           <.start-element('METHOD_CALL')>
           <.start-token('DOTTY_NEXT_TERM')> <?> <.end-token('DOTTY_NEXT_TERM')>
           <.dottyop>
           <.end-element('METHOD_CALL')>
        || <.term>
    }

    token prefixish {
        [
        || <?before [<.prefix> ['«'||'<<']]>
           <.start-element('HYPER_METAOP')>
           <.prefix>
           <.start-token('METAOP')>
           ['«'||'<<']
           <.end-token('METAOP')>
           <.end-element('HYPER_METAOP')>
        || <.start-element('PREFIX')>
           <.prefix>
           <.end-element('PREFIX')>
       ]
       <.ws>?
    }

    token prefix {
        || <?before ['let'<.kok>]>
           <.start-token('PREFIX')> 'let' <.end-token('PREFIX')>
           <.kok>
           { $*PREC = 'o=' } { $*ASSOC = 'unary' }
        || <?before ['temp'<.kok>]>
           <.start-token('PREFIX')> 'temp' <.end-token('PREFIX')>
           <.kok>
           { $*PREC = 'o=' } { $*ASSOC = 'unary' }
        || <?before ['so'<.end_prefix>]>
           <.start-token('PREFIX')> 'so' <.end-token('PREFIX')>
           <.end_prefix>
           { $*PREC = 'h=' } { $*ASSOC = 'unary' }
        || <?before ['not'<.end_prefix>]>
           <.start-token('PREFIX')> 'not' <.end-token('PREFIX')>
           <.end_prefix>
           { $*PREC = 'h=' } { $*ASSOC = 'unary' }
        || <.start-token('PREFIX')>
           [
           || '++⚛' { $*PREC = 'x=' } { $*ASSOC = 'unary' }
           || '--⚛' { $*PREC = 'x=' } { $*ASSOC = 'unary' }
           || '++' { $*PREC = 'x=' } { $*ASSOC = 'unary' }
           || '--' { $*PREC = 'x=' } { $*ASSOC = 'unary' }
           || '+^' { $*PREC = 'v=' } { $*ASSOC = 'unary' }
           || '~^' { $*PREC = 'v=' } { $*ASSOC = 'unary' }
           || '?^' { $*PREC = 'v=' } { $*ASSOC = 'unary' }
           || '+' { $*PREC = 'v=' } { $*ASSOC = 'unary' }
           || '~' { $*PREC = 'v=' } { $*ASSOC = 'unary' }
           || '-' <![>]> { $*PREC = 'v=' } { $*ASSOC = 'unary' }
           || '−' { $*PREC = 'v=' } { $*ASSOC = 'unary' }
           || '?' <!before '??'> { $*PREC = 'v=' } { $*ASSOC = 'unary' }
           || '!' <!before '!!'> { $*PREC = 'v=' } { $*ASSOC = 'unary' }
           || '|' { $*PREC = 'v=' } { $*ASSOC = 'unary' }
           || '^' { $*PREC = 'v=' } { $*ASSOC = 'unary' }
           || '⚛' { $*PREC = 'v=' } { $*ASSOC = 'unary' }
           ]
           <.end-token('PREFIX')>
    }

    token postfixish {
        <!stdstopper>
        [
            <!{ $*QSIGIL }>
            <?before [[<.unsp> || '\\'] [['»' || '>>'] || <.postfixish_nometa>]]>
            <.start-token('UNSPACED_POSTFIX')> <?> <.end-token('UNSPACED_POSTFIX')>
            [
            || <.unsp>
            || <.start-token('WHITE_SPACE')> '\\' <.end-token('WHITE_SPACE')>
            ]
        ]?
        [
        || <?before [ ['.' <.unsp>?]? ['»' || '>>'] [ <!{ $*QSIGIL }> || <![(]> ] ]>
           <.start-element('HYPER_METAOP')>
           [
               <?before ['.' <.unsp>?]>
               <.start-token('METAOP')> '.' <.end-token('METAOP')>
               <.unsp>?
           ]?
           <.start-token('METAOP')>
           ['»' || '>>']
           <.end-token('METAOP')>
           [
           || <.postfixish_nometa>
           || <.start-token('HYPER_METAOP_MISSING')> <?> <.end-token('HYPER_METAOP_MISSING')>
              { $*PREC = 'y=' } { $*ASSOC = 'unary' }
           ]
           <.end-element('HYPER_METAOP')>
        || <.postfixish_nometa>
        ]
        { $*LEFTSIGIL = '@' }
    }

    token postfixish_nometa {
        || <?before ['.' \W]> <?before ['.' <.postfix>]>
           <.start-element('POSTFIX')>
           <.start-token('POSTFIX')> '.' <.end-token('POSTFIX')>
           <.postfix>
           <.end-element('POSTFIX')>
        || <.start-element('POSTFIX')>
           <.postfix>
           <.end-element('POSTFIX')>
        || <.postcircumfix> { $*PREC = 'y=' } { $*ASSOC = 'unary' }
        || <?before ['.' <?[ [ { < ]>]>
           <.start-element('METHOD_CALL')>
           <.start-token('METHOD_CALL_OPERATOR')>
           '.'
           <.end-token('METHOD_CALL_OPERATOR')>
           <.postcircumfix> { $*PREC = 'y=' } { $*ASSOC = 'unary' }
           <.end-element('METHOD_CALL')>
        || <.dotty> { $*PREC = 'y=' } { $*ASSOC = 'unary' }
        || <.privop> { $*PREC = 'y=' } { $*ASSOC = 'unary' }
    }

    token postfix {
        <.start-token('POSTFIX')>
        [
        || 'i' { $*PREC = 'y=' } { $*ASSOC = 'unary' }
        || '⚛++' { $*PREC = 'x=' } { $*ASSOC = 'unary' }
        || '⚛--' { $*PREC = 'x=' } { $*ASSOC = 'unary' }
        || '++' { $*PREC = 'x=' } { $*ASSOC = 'unary' }
        || '--' { $*PREC = 'x=' } { $*ASSOC = 'unary' }
        || <[⁻⁺¯]>? <[⁰¹²³⁴⁵⁶⁷⁸⁹]>+ { $*PREC = 'x=' } { $*ASSOC = 'unary' }
        ]
        <.end-token('POSTFIX')>
    }

    token dotty {
        # We try to highlight . as a method call operator when there's not
        # something after it that could not possibly result in parsing a
        # method call. If this causes too many issues, we can switch to a
        # further lookahead and not trying to do that.
        <!before ['.' [ '.' || <?{ $*QSIGIL }> \s || \d ]]>
        <.start-element('METHOD_CALL')>
        <.start-token('METHOD_CALL_OPERATOR')>
        '.' [ <[+*?=]> || '^' ]?
        <.end-token('METHOD_CALL_OPERATOR')>
        <.dottyop>
        <.end-element('METHOD_CALL')>
    }

    token dottyop {
        <.unsp>?
        [
        || <.methodop>
        ]?
    }

    token privop {
        <.start-element('METHOD_CALL')>
        <.start-token('METHOD_CALL_OPERATOR')>
        '!' <![!]>
        <.end-token('METHOD_CALL_OPERATOR')>
        <.methodop>?
        <.end-element('METHOD_CALL')>
    }

    token methodop {
        [
        || <.start-element('LONG_NAME')>
           <.start-token('METHOD_CALL_NAME')>
           <.name>
           <.end-token('METHOD_CALL_NAME')>
           <.longname_colonpairs>
           <.end-element('LONG_NAME')>
        || <?[$@&]> <.variable>
        || <?['"]>
           [ <!{$*QSIGIL}> || <!before ['"' [<!["]>\S]* [\s||$] ]> ]
           <.quote>
        ] <.unsp>?
        [
            [
            || <?[(]> <.args>
            || <!{ $*QSIGIL }>
               <?before ':' [ \s || '{']>
               <.start-token('INVOCANT_MARKER')>
               ':'
               <.end-token('INVOCANT_MARKER')>
               <.arglist>
            ]
            || <?>
        ] <.unsp>?
    }

    token postcircumfix {
        :my $*QSIGIL = '';
        :my $*Q_BACKSLASH = 0;
        :my $*Q_QBACKSLASH = 0;
        :my $*Q_QQBACKSLASH = 0;
        :my $*Q_CLOSURES = 0;
        :my $*Q_SCALARS = 0;
        :my $*Q_ARRAYS = 0;
        :my $*Q_HASHES = 0;
        :my $*Q_FUNCTIONS = 0;
        [
        || <.start-element('ARRAY_INDEX')>
           <.start-token('ARRAY_INDEX_BRACKET_OPEN')>
           '['
           <.end-token('ARRAY_INDEX_BRACKET_OPEN')>
           [
               <.semilist>
               [
                   <.start-token('ARRAY_INDEX_BRACKET_CLOSE')>
                   ']'
                   <.end-token('ARRAY_INDEX_BRACKET_CLOSE')>
               ]?
           ]?
           <.end-element('ARRAY_INDEX')>
        || <.start-element('HASH_INDEX')>
           <.start-token('HASH_INDEX_BRACKET_OPEN')>
           '{'
           <.end-token('HASH_INDEX_BRACKET_OPEN')>
           [
               <.semilist>
               [
                   <.start-token('HASH_INDEX_BRACKET_CLOSE')>
                   '}'
                   <.end-token('HASH_INDEX_BRACKET_CLOSE')>
               ]?
           ]?
           <.end-element('HASH_INDEX')>
        || <.start-element('HASH_INDEX')>
           <.start-token('HASH_INDEX_BRACKET_OPEN')>
           '<<'
           <.end-token('HASH_INDEX_BRACKET_OPEN')>
           [
               <.quote_qq('<<', '>>', '>>')>
               [
                   <.start-token('HASH_INDEX_BRACKET_CLOSE')>
                   '>>'
                   <.end-token('HASH_INDEX_BRACKET_CLOSE')>
               ]?
           ]?
           <.end-element('HASH_INDEX')>
        || <.start-element('HASH_INDEX')>
           <.start-token('HASH_INDEX_BRACKET_OPEN')>
           '«'
           <.end-token('HASH_INDEX_BRACKET_OPEN')>
           [
               <.quote_qq('«', '»', '»')>
               [
                   <.start-token('HASH_INDEX_BRACKET_CLOSE')>
                   '»'
                   <.end-token('HASH_INDEX_BRACKET_CLOSE')>
               ]?
           ]?
           <.end-element('HASH_INDEX')>
        || <.start-element('HASH_INDEX')>
           <.start-token('HASH_INDEX_BRACKET_OPEN')>
           '<'
           <.end-token('HASH_INDEX_BRACKET_OPEN')>
           [
               <.quote_q('<', '>', '>')>
               [
                   <.start-token('HASH_INDEX_BRACKET_CLOSE')>
                   '>'
                   <.end-token('HASH_INDEX_BRACKET_CLOSE')>
               ]?
           ]?
           <.end-element('HASH_INDEX')>
        || <.start-element('CALL')>
           <.start-token('PARENTHESES_OPEN')> '(' <.end-token('PARENTHESES_OPEN')>
           <.arglist> <.ws>?
           [ <.start-token('PARENTHESES_CLOSE')> ')' <.end-token('PARENTHESES_CLOSE')> ]?
           <.end-element('CALL')>
       ]
    }

    token infixish($*IN_META) {
        <!stdstopper>
        <!infixstopper>
        [
        || <!{ $*IN_REDUCE }>
           <.start-element('OPERATOR_ADVERB')>
           <.colonpair>
           <.end-element('OPERATOR_ADVERB')>
           { $*PREC = 'i=' } { $*ASSOC = 'unary' } { $*FAKE = 1 }
        || <?before [<![!]> <.infixish_non_assignment_meta> '=']>
           <.start-element('ASSIGN_METAOP')>
           <.start-token('ASSIGN_METAOP')> <?> <.end-token('ASSIGN_METAOP')>
           <.infixish_non_assignment_meta>
           <.start-token('METAOP')>
           '='
           <.end-token('METAOP')>
           <.end-element('ASSIGN_METAOP')>
           [ <?{ $*PREC le 'g=' }> { $*SUB_PREC = 'e=' } ]?
           { $*PREC = 'i=' } { $*ASSOC = 'right' }
        || <.infixish_non_assignment_meta>
        ]
    }

    # MIRRORS of MAINBraid.java hand-edits (see org/llm/raku/traces/
    # parser-reduce-metaop-mislexing.md): a meta-operator wraps a COMPLETE
    # base infix regardless of the surrounding precedence limit, so (1) the
    # $*PREC/$*PRECLIM guard in token infix only applies when $*IN_META is
    # empty, and (2) the incomplete-bracketed-infix editor-tolerance
    # fallback below must not fire in meta context.
    token infixish_non_assignment_meta {
        || <.start-element('BRACKETED_INFIX')>
           <.start-token('INFIX')>
           '['
           <.end-token('INFIX')>
           [
           || <?before ['&' <.twigil>? [<.alpha>||'(']]>
              <.start-token('INFIX_FUNCTION')> <?> <.end-token('INFIX_FUNCTION')>
              <.variable>
              [
              || <.start-token('INFIX')>
                 ']'
                 <.end-token('INFIX')>
              || <.start-token('BRACKETED_INFIX_INCOMPLETE')>
                 <?>
                 <.end-token('BRACKETED_INFIX_INCOMPLETE')>
              ]
              { $*PREC = 't=' } { $*ASSOC = 'left' }
           || <.infixish('[]')>
              [
              || <.start-token('INFIX')>
                 ']'
                 <.end-token('INFIX')>
              || <.start-token('BRACKETED_INFIX_INCOMPLETE')>
                 <?>
                 <.end-token('BRACKETED_INFIX_INCOMPLETE')>
              ]
           || <!{ $*IN_META }>
              <.start-token('BRACKETED_INFIX_INCOMPLETE')>
              <?>
              <.end-token('BRACKETED_INFIX_INCOMPLETE')>
              { $*PREC = 't=' } { $*ASSOC = 'left' }
           ]
           <.end-element('BRACKETED_INFIX')>
        || <.infix_prefix_meta_operator>
        || <.infix_circumfix_meta_operator>
        || <.start-element('INFIX')>
           <.infix>
           <.end-element('INFIX')>
    }

    token infixstopper {
        [
        || <?before '!!'> <?{ $*GOAL eq '!!' }>
        || <?before '{' || <.lambda> > [ <?{ $*GOAL eq '{' }> || <?{ $*GOAL eq 'endargs' }> ]
        ]
    }

    token infix {
        { $*SUB_PREC = '' }
        [
        || <?before ['??' <.ws>]>
           <.start-token('INFIX')> '??' <.end-token('INFIX')>
           <.ws>
           :my $*GOAL = '!!';
           [
           || <.EXPR('i=')>
              [
              || <?before <.ws> '!!'>
                 <.ws> <.start-token('INFIX')> '!!' <.end-token('INFIX')>
              || <.start-token('COND_OP_INCOMPLETE')> <?> <.end-token('COND_OP_INCOMPLETE')>
              ]
           || <.start-token('COND_OP_INCOMPLETE')> <?> <.end-token('COND_OP_INCOMPLETE')>
           ]
           { $*PREC = 'j=' } { $*ASSOC = 'right' }
        || <.start-token('INFIX')>
           [
           || 'notandthen' { $*PREC = 'd=' } { $*ASSOC = 'list' }
           || 'andthen' { $*PREC = 'd=' } { $*ASSOC = 'list' }
           || '(elem)' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '(cont)' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || 'orelse' { $*PREC = 'c=' } { $*ASSOC = 'list' }
           || 'unicmp' { $*PREC = 'n=' } { $*ASSOC = 'non' }
           || 'minmax' { $*PREC = 'f=' } { $*ASSOC = 'list' }
           || 'before' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || 'after' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '^fff^' { $*PREC = 'j=' } { $*ASSOC = 'right' }
           || '...^' { $*PREC = 'f=' } { $*ASSOC = 'list' }
           || '^ff^' { $*PREC = 'j=' } { $*ASSOC = 'right' }
           || '^fff' { $*PREC = 'j=' } { $*ASSOC = 'right' }
           || 'fff^' { $*PREC = 'j=' } { $*ASSOC = 'right' }
           || '<<==' { $*PREC = 'b=' } { $*ASSOC = 'list' }
           || '==>>' { $*PREC = 'b=' } { $*ASSOC = 'list' }
           || '^..^' { $*PREC = 'n=' } { $*ASSOC = 'left' }
           || 'coll' { $*PREC = 'n=' } { $*ASSOC = 'non' }
           || 'does' { $*PREC = 'n=' } { $*ASSOC = 'non' }
           || 'div' { $*PREC = 'u=' } { $*ASSOC = 'left' }
           || 'gcd' { $*PREC = 'u=' } { $*ASSOC = 'left' }
           || 'lcm' { $*PREC = 'u=' } { $*ASSOC = 'left' }
           || 'mod' { $*PREC = 'u=' } { $*ASSOC = 'left' }
           || '(&)' { $*PREC = 'q=' } { $*ASSOC = 'list' }
           || '(.)' { $*PREC = 'q=' } { $*ASSOC = 'list' }
           || '(|)' { $*PREC = 'p=' } { $*ASSOC = 'list' }
           || '(^)' { $*PREC = 'p=' } { $*ASSOC = 'list' }
           || '(+)' { $*PREC = 'p=' } { $*ASSOC = 'list' }
           || '(-)' { $*PREC = 'p=' } { $*ASSOC = 'list' }
           || '=~=' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '=:=' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '===' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || 'eqv' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '!~~' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '(<)' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '(>)' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '(<=)' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '(>=)' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '(<+)' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '(>+)' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '(==)' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || 'min' { $*PREC = 'k=' } { $*ASSOC = 'list' }
           || 'max' { $*PREC = 'k=' } { $*ASSOC = 'list' }
           || '::=' { $*PREC = 'i=' } { $*ASSOC = 'right' }
           || '...' { $*PREC = 'f=' } { $*ASSOC = 'list' }
           || '^ff' { $*PREC = 'j=' } { $*ASSOC = 'right' }
           || 'ff^' { $*PREC = 'j=' } { $*ASSOC = 'right' }
           || 'fff' { $*PREC = 'j=' } { $*ASSOC = 'right' }
           || '⚛+=' { $*PREC = 'i=' } { $*ASSOC = 'right' }
           || '⚛-=' { $*PREC = 'i=' } { $*ASSOC = 'right' }
           || '⚛−=' { $*PREC = 'i=' } { $*ASSOC = 'right' }
           || 'and' { $*PREC = 'd=' } { $*ASSOC = 'left' }
           || 'xor' { $*PREC = 'c=' } { $*ASSOC = 'list' }
           || '<==' { $*PREC = 'b=' } { $*ASSOC = 'list' }
           || '==>' { $*PREC = 'b=' } { $*ASSOC = 'list' }
           || '^..' { $*PREC = 'n=' } { $*ASSOC = 'non' }
           || '..^' { $*PREC = 'n=' } { $*ASSOC = 'non' }
           || 'leg' { $*PREC = 'n=' } { $*ASSOC = 'non' }
           || 'cmp' { $*PREC = 'n=' } { $*ASSOC = 'non' }
           || '<=>' { $*PREC = 'n=' } { $*ASSOC = 'non' }
           || 'but' { $*PREC = 'n=' } { $*ASSOC = 'non' }
           || '**' { $*PREC = 'w=' } { $*ASSOC = 'right' }
           || '%%' { $*PREC = 'u=' } { $*ASSOC = 'left' }
           || '+&' { $*PREC = 'u=' } { $*ASSOC = 'left' }
           || '~&' { $*PREC = 'u=' } { $*ASSOC = 'left' }
           || '?&' { $*PREC = 'u=' } { $*ASSOC = 'left' }
           || '+<' [ <!{ $*IN_META }> || <?before '<<'> || <![<]> ]
                   { $*PREC = 'u=' } { $*ASSOC = 'left' }
           || '+>' [ <!{ $*IN_META }> || <?before '>>'> || <![>]> ]
                   { $*PREC = 'u=' } { $*ASSOC = 'left' }
           || '~<' [ <!{ $*IN_META }> || <?before '<<'> || <![<]> ]
                   { $*PREC = 'u=' } { $*ASSOC = 'left' }
           || '~>' [ <!{ $*IN_META }> || <?before '>>'> || <![>]> ]
                   { $*PREC = 'u=' } { $*ASSOC = 'left' }
           || '+|' { $*PREC = 't=' } { $*ASSOC = 'left' }
           || '+^' { $*PREC = 't=' } { $*ASSOC = 'left' }
           || '~|' { $*PREC = 't=' } { $*ASSOC = 'left' }
           || '~^' { $*PREC = 't=' } { $*ASSOC = 'left' }
           || '?|' { $*PREC = 't=' } { $*ASSOC = 'left' }
           || '?^' { $*PREC = 't=' } { $*ASSOC = 'left' }
           || 'xx' { $*PREC = 's=' } { $*ASSOC = 'left' }
           || '==' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '!=' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '<=' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '>=' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || 'eq' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || 'ne' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || 'le' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || 'ge' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || 'lt' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || 'gt' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '~~' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '&&' { $*PREC = 'l=' } { $*ASSOC = 'left' }
           || '||' { $*PREC = 'k=' } { $*ASSOC = 'left' }
           || '^^' { $*PREC = 'k=' } { $*ASSOC = 'list' }
           || '//' { $*PREC = 'k=' } { $*ASSOC = 'left' }
           || ':=' { $*PREC = 'i=' } { $*ASSOC = 'right' }
           || '.=' { $*PREC = 'v=' } { $*ASSOC = 'left' }
                   { $*SUB_PREC = 'z=' } { $*NEXT_TERM = 'dotty' }
           || '…^' { $*PREC = 'f=' } { $*ASSOC = 'list' }
           || 'ff' { $*PREC = 'j=' } { $*ASSOC = 'right' }
           || '⚛=' { $*PREC = 'i=' } { $*ASSOC = 'right' }
           || 'or' { $*PREC = 'c=' } { $*ASSOC = 'left' }
           || '..' { $*PREC = 'n=' } { $*ASSOC = 'non' }
           || '=>' { $*PREC = 'i=' } { $*ASSOC = 'right' }
           || '.' <!{ $*IN_REDUCE }> <?before [<.ws> <.alpha>]>
                  { $*PREC = 'v=' } { $*ASSOC = 'left' }
                  { $*SUB_PREC = 'z=' } { $*NEXT_TERM = 'dotty' }
           || '*' { $*PREC = 'u=' } { $*ASSOC = 'left' }
           || '×' { $*PREC = 'u=' } { $*ASSOC = 'left' }
           || '/' { $*PREC = 'u=' } { $*ASSOC = 'left' }
           || '÷' { $*PREC = 'u=' } { $*ASSOC = 'left' }
           || '%' { $*PREC = 'u=' } { $*ASSOC = 'left' }
           || '+' { $*PREC = 't=' } { $*ASSOC = 'left' }
           || '-' [<?before '>>'> || <![>]>] { $*PREC = 't=' } { $*ASSOC = 'left' }
           || '−' { $*PREC = 't=' } { $*ASSOC = 'left' }
           || 'x' { $*PREC = 's=' } { $*ASSOC = 'left' }
           || '~' { $*PREC = 'r=' } { $*ASSOC = 'left' }
           || '∘' { $*PREC = 'r=' } { $*ASSOC = 'left' }
           || 'o' { $*PREC = 'r=' } { $*ASSOC = 'left' }
           || '&' { $*PREC = 'q=' } { $*ASSOC = 'list' }
           || '∩' { $*PREC = 'q=' } { $*ASSOC = 'list' }
           || '⊍' { $*PREC = 'q=' } { $*ASSOC = 'list' }
           || '|' { $*PREC = 'p=' } { $*ASSOC = 'list' }
           || '^' { $*PREC = 'p=' } { $*ASSOC = 'list' }
           || '∪' { $*PREC = 'p=' } { $*ASSOC = 'list' }
           || '⊖' { $*PREC = 'p=' } { $*ASSOC = 'list' }
           || '⊎' { $*PREC = 'p=' } { $*ASSOC = 'list' }
           || '∖' { $*PREC = 'p=' } { $*ASSOC = 'list' }
           || '≅' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '≠' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '≤' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '≥' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '<' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '>' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '∈' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '∉' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '∋' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '∌' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '⊂' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '⊄' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '⊃' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '⊅' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '⊆' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '⊈' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '⊇' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '⊉' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '≼' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '≽' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '≡' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || '≢' { $*PREC = 'm=' } { $*ASSOC = 'left' }
           || ',' { $*PREC = 'g=' } { $*ASSOC = 'list' } { $*NEXT_TERM = 'nulltermish' }
           || 'Z' { $*PREC = 'f=' } { $*ASSOC = 'list' }
           || 'X' { $*PREC = 'f=' } { $*ASSOC = 'list' }
           || '…' { $*PREC = 'f=' } { $*ASSOC = 'list' }
           || '=' { $*PREC = 'i=' } { $*ASSOC = 'right' }
              [ <?{ $*LEFTSIGIL ne '$' }> <!{ $*IN_META }> { $*SUB_PREC = 'e=' } ]?
           ]
           [ <?{ $*IN_META }> || <!{ $*PREC le $*PRECLIM }> ]
           <.end-token('INFIX')>
        ]
    }

    token infix_prefix_meta_operator {
        || <?before ['!' <![!]> <.infixish('neg')>]>
           <!before ['!=' <![=]>]>
           <.start-element('NEGATION_METAOP')>
           <.start-token('METAOP')>
           '!'
           <.end-token('METAOP')>
           <.infixish('neg')>
           <.end-element('NEGATION_METAOP')>
        || <?before ['R' <.infixish('R')>]>
           <.start-element('REVERSE_METAOP')>
           <.start-token('METAOP')>
           'R'
           <.end-token('METAOP')>
           <.infixish('R')>
           <.end-element('REVERSE_METAOP')>
           [
           || <?{ $*ASSOC eq 'left' }> { $*ASSOC = 'right' }
           || <?{ $*ASSOC eq 'right' }> { $*ASSOC = 'left' }
           || <?>
           ]
        || <?before ['S' <.infixish('S')>]>
           <.start-element('SEQUENTIAL_METAOP')>
           <.start-token('METAOP')>
           'S'
           <.end-token('METAOP')>
           <.infixish('S')>
           <.end-element('SEQUENTIAL_METAOP')>
        || <?before ['X' <.infixish('X')>]>
           <.start-element('CROSS_METAOP')>
           <.start-token('METAOP')>
           'X'
           <.end-token('METAOP')>
           <.infixish('X')>
           <.end-element('CROSS_METAOP')>
           { $*PREC = 'f=' } { $*ASSOC = 'list' }
        || <?before ['Z' <.infixish('Z')>]>
           <.start-element('ZIP_METAOP')>
           <.start-token('METAOP')>
           'Z'
           <.end-token('METAOP')>
           <.infixish('Z')>
           <.end-element('ZIP_METAOP')>
           { $*PREC = 'f=' } { $*ASSOC = 'list' }
    }

    token infix_circumfix_meta_operator {
        || <?before [<[«»]> <.infixish('hyper')>]>
           <.start-element('HYPER_METAOP')>
           <.start-token('METAOP')>
           <[«»]>
           <.end-token('METAOP')>
           <.infixish('hyper')>
           [
           || <.start-token('METAOP')>
              <[«»]>
              <.end-token('METAOP')>
           || <.start-token('HYPER_METAOP_MISSING')> <?> <.end-token('HYPER_METAOP_MISSING')>
           ]
           <.end-element('HYPER_METAOP')>
        || <?before [[ '<<' || '>>' ] <.infixish('hyper')>]>
           <.start-element('HYPER_METAOP')>
           <.start-token('METAOP')>
           [ '<<' || '>>' ]
           <.end-token('METAOP')>
           <.infixish('hyper')>
           [
           || <.start-token('METAOP')>
              [ '<<' || '>>' ]
              <.end-token('METAOP')>
           || <.start-token('HYPER_METAOP_MISSING')> <?> <.end-token('HYPER_METAOP_MISSING')>
           ]
           <.end-element('HYPER_METAOP')>
    }

    token termish {
        <.term>
    }

    token term_reduce {
        :my $*IN_REDUCE = 1;
        <!before [ '[' <[ - + ? ~ ^ ]> [\w || <[$@]>] ]>
        <?before [ '[' [ <.infixish('red')> || '\\' <.infixish('tri')> ] ']' ]>

        <.start-element('REDUCE_METAOP')>
        <.start-token('METAOP')>
        '['
        <.end-token('METAOP')>
        [
        || <.start-token('METAOP')> '\\' <.end-token('METAOP')> <.infixish('tri')>
        || <.infixish('red')>
        ]
        <.start-token('METAOP')>
        ']'
        <.end-token('METAOP')>
        { $*IN_REDUCE = 0 }
        <.args>
        <.end-element('REDUCE_METAOP')>
    }

    token enter_regex_nibbler($*STARTER, $*STOPPER) {
        :my $*DELIM = '';
        <.start-element('REGEX')>
        <.regex_nibbler>
        <.end-element('REGEX')>
    }

    token regex_nibbler_fresh_rx($*RX_S) {
        <.regex_nibbler>
    }

    token rxws {
        [
        || <.start-token('WHITE_SPACE')>
           [\r\n || \v]
           <.end-token('WHITE_SPACE')>
        || <.vws>
        || <.start-token('UNV_WHITE_SPACE')> \h+ <.end-token('UNV_WHITE_SPACE')>
        || <.plain-comment>
        ]*
    }

    token regex_nibbler {
        <.rxws>
        [
            <!rxstopper>
            [
            || <.start-token('REGEX_INFIX')> '||' <.end-token('REGEX_INFIX')>
            || <.start-token('REGEX_INFIX')> '|' <.end-token('REGEX_INFIX')>
            || <.start-token('REGEX_INFIX')> '&&' <.end-token('REGEX_INFIX')>
            || <.start-token('REGEX_INFIX')> '&' <.end-token('REGEX_INFIX')>
            ]
            <.rxws>
        ]?
        <.termseq>?
    }

    token rxstopper { <.stopper> }

    token rxinfixstopper {
        [
        || <?before <[\) \} \]]> >
        || <?before '>' <-[>]> >
        || <?rxstopper>
        ]
    }

    token termseq {
        :my $*PREC = '';
        :my $*SUB_PREC = '';
        :my $*ASSOC = '';
        :my $*NEXT_TERM = '';
        :my $*FAKE = 0;

        <.opp-start-expr>
        <.opp-regex-mode>
        
        <.opp-start-prefixes><.opp-end-prefixes>
        <.rxtermish>
        <.opp-start-postfixes><.opp-end-postfixes>

        [
            <!rxinfixstopper>
            <.opp-start-infix>
            [
            || <.start-token('REGEX_INFIX')> '||' <.end-token('REGEX_INFIX')>
               { $*PREC = 'a=' } { $*ASSOC = 'list' }
            || <.start-token('REGEX_INFIX')> '&&' <.end-token('REGEX_INFIX')>
               { $*PREC = 'b=' } { $*ASSOC = 'list' }
            || <.start-token('REGEX_INFIX')> '|' <.end-token('REGEX_INFIX')>
               { $*PREC = 'c=' } { $*ASSOC = 'list' }
            || <.start-token('REGEX_INFIX')> '&' <.end-token('REGEX_INFIX')>
               { $*PREC = 'd=' } { $*ASSOC = 'list' }
            ]
            <.opp-end-infix>
            <.rxws>

            [
            || <.opp-start-prefixes><.opp-end-prefixes>
               <.rxtermish>
               <.opp-start-postfixes><.opp-end-postfixes>
            || <.start-token('REGEX_MISSING_TERM')> <?> <.end-token('REGEX_MISSING_TERM')>
            ]
        ]*

        <.opp-end-expr>
    }

    token rxtermish {
        :my $*SIGOK = 0;
        <.quantified_atom>*
    }

    token SIGOK {
        [ <?{ $*RX_S }> { $*SIGOK = 1 } ]?
    }

    token sigmaybe {
        || <?{ $*SIGOK }>
           <.start-element('REGEX_SIGSPACE')>
           <.start-token('REGEX_SIGSPACE')> <?> <.end-token('REGEX_SIGSPACE')>
           <.normspace>
           <.end-element('REGEX_SIGSPACE')>
        || <.normspace>
    }

    token normspace { <?before \s || '#'> <.ws> }

    token quantified_atom {
        <.start-element('REGEX_ATOM')>
        <.atom>
        <.sigmaybe>?
        [
            [
            || <!rxstopper> <.quantifier>
            || <?before <?[:]> <.backmod> \W>
               <.start-element('REGEX_QUANTIFIER')>
               <.start-token('REGEX_QUANTIFIER')>
               <.backmod>
               <.end-token('REGEX_QUANTIFIER')>
               <.end-element('REGEX_QUANTIFIER')>
            ]
            [ <.SIGOK> <.sigmaybe> ]?
            [
                <?before <.rxws> '%''%'? <.rxws>>
                <.rxws> <.separator>
            ]?
        ]?
        <.end-element('REGEX_ATOM')>
        { $*SIGOK = 0 }
    }

    token separator {
        <.start-element('REGEX_SEPARATOR')>
        <.start-token('REGEX_QUANTIFIER')>
        '%''%'?
        <.end-token('REGEX_QUANTIFIER')>
        :my $*SIGOK = 0;
        <.rxws>
        <.quantified_atom>?
        <.rxws>
        <.end-element('REGEX_SEPARATOR')>
    }

    token atom {
        [
        || <.start-element('REGEX_LITERAL')>
           <.start-token('STRING_LITERAL_CHAR')> \w <.end-token('STRING_LITERAL_CHAR')>
           <.end-element('REGEX_LITERAL')>
           <.SIGOK>
        || <.metachar>
        ]
    }

    token quantifier {
        <.start-element('REGEX_QUANTIFIER')>
        [
        || <.start-token('REGEX_QUANTIFIER')> '**' <.end-token('REGEX_QUANTIFIER')>
           <.normspace>?
           <.start-token('REGEX_QUANTIFIER')> <.backmod> <.end-token('REGEX_QUANTIFIER')>
           <.normspace>?
           [
           || <?[{]> <.rxcodeblock>
           || <.start-token('PREFIX')> '^' <.end-token('PREFIX')> <.integer>
           || <.integer>
              [
                  <.start-token('INFIX')>
                  '^'? '..' '^'?
                  <.end-token('INFIX')>
                  [
                      || <.integer>
                      || <.start-token('WHATEVER')> '*' <.end-token('WHATEVER')>
                  ]?
              ]?
           || <.start-token('REGEX_QUANTIFIER_MISSING')> <?> <.end-token('REGEX_QUANTIFIER_MISSING')>
           ]
        || <.start-token('REGEX_QUANTIFIER')>
           [ '*' || '+' || '?' ] <.backmod>
           <.end-token('REGEX_QUANTIFIER')>
       ]
        <.end-element('REGEX_QUANTIFIER')>
    }

    token rxcodeblock {
        <.pblock>
    }

    token backmod { ':'? [ '?' || '!' || <!before ':'> ] }

    token metachar {
        || <?before [<.sigil> [<.alpha> || \W<.alpha> || '(' || \d]]>
           <!before [<.sigil> <.rxstopper>]>
           <.start-element('REGEX_VARIABLE')>
           <.start-token('REGEX_VARIABLE')> <?> <.end-token('REGEX_VARIABLE')>
           <.variable>
           [
               <?before [\s*'='\s*]>
               <.start-token('REGEX_VARIABLE_BINDING')>
               <?>
               <.end-token('REGEX_VARIABLE_BINDING')>
               <.start-token('WHITE_SPACE')>
               \s*
               <.end-token('WHITE_SPACE')>
               <.start-token('INFIX')>
               '='
               <.end-token('INFIX')>
               <.start-token('WHITE_SPACE')>
               \s*
               <.end-token('WHITE_SPACE')>
               [
               || <.quantified_atom>
               || <.start-token('REGEX_VARIABLE_BINDING_INCOMPLETE')>
                  <?>
                  <.end-token('REGEX_VARIABLE_BINDING_INCOMPLETE')>
               ]
           ]?
           <.end-element('REGEX_VARIABLE')>
        || <.start-element('REGEX_ANCHOR')>
           [
           || <.start-token('REGEX_ANCHOR')> '^^' <.end-token('REGEX_ANCHOR')> <.SIGOK>
           || <.start-token('REGEX_ANCHOR')> '^' <.end-token('REGEX_ANCHOR')> <.SIGOK>
           || <.start-token('REGEX_ANCHOR')> '$$' <.end-token('REGEX_ANCHOR')> <.SIGOK>
           || <.start-token('REGEX_ANCHOR')> '$' <.end-token('REGEX_ANCHOR')> <.SIGOK>
           || <.start-token('REGEX_ANCHOR')> '<<' <.end-token('REGEX_ANCHOR')> <.SIGOK>
           || <.start-token('REGEX_ANCHOR')> '«' <.end-token('REGEX_ANCHOR')> <.SIGOK>
           || <.start-token('REGEX_ANCHOR')> '>>' <.end-token('REGEX_ANCHOR')> <.SIGOK>
           || <.start-token('REGEX_ANCHOR')> '»' <.end-token('REGEX_ANCHOR')> <.SIGOK>
           || <.start-token('REGEX_ANCHOR')> '<(' <.end-token('REGEX_ANCHOR')> <.SIGOK>
           || <.start-token('REGEX_ANCHOR')> ')>' <.end-token('REGEX_ANCHOR')> <.SIGOK>
           ]
           <.end-element('REGEX_ANCHOR')>
        || <.start-element('REGEX_BUILTIN_CCLASS')>
           <.start-token('REGEX_BUILTIN_CCLASS')>
           '.'
           <.end-token('REGEX_BUILTIN_CCLASS')>
           <.end-element('REGEX_BUILTIN_CCLASS')>
        || <.start-element('REGEX_GROUP')>
           <.start-token('REGEX_GROUP_BRACKET_OPEN')>
           '['
           <.end-token('REGEX_GROUP_BRACKET_OPEN')>
           <.regex_nibbler_fresh_rx($*RX_S)>?
           [
               <.start-token('REGEX_GROUP_BRACKET_CLOSE')>
               ']'
               <.end-token('REGEX_GROUP_BRACKET_CLOSE')>
           ]?
           <.end-element('REGEX_GROUP')>
        || <.start-element('REGEX_CAPTURE_POSITIONAL')>
           <.start-token('REGEX_CAPTURE_PARENTHESES_OPEN')>
           '('
           <.end-token('REGEX_CAPTURE_PARENTHESES_OPEN')>
           <.regex_nibbler_fresh_rx($*RX_S)>?
           [
               <.start-token('REGEX_CAPTURE_PARENTHESES_CLOSE')>
               ')'
               <.end-token('REGEX_CAPTURE_PARENTHESES_CLOSE')>
           ]?
           <.end-element('REGEX_CAPTURE_POSITIONAL')>
        || <?before '\\' .> <.backslash> <.SIGOK>
        || <?before '<' \s > <.rxqw> <.SIGOK>
        || <.start-element('REGEX_ASSERTION')>
           <.start-token('REGEX_ASSERTION_ANGLE_OPEN')>
           '<'
           <.end-token('REGEX_ASSERTION_ANGLE_OPEN')>
           <.assertion(0)>
           [
           <.start-token('REGEX_ASSERTION_ANGLE_CLOSE')>
           '>'
           <.end-token('REGEX_ASSERTION_ANGLE_CLOSE')>
           <.SIGOK>
           ]?
           <.end-element('REGEX_ASSERTION')>
        || <?[｢]> <.rxQ> <.SIGOK>
        || <?['‘‚]> <.rxq> <.SIGOK>
        || <?["“„]> <.rxqq> <.SIGOK>
        || <?[{]> <.rxcodeblock>
        || <?before ':' ['my'||'constant'||'state'||'our'||'temp'||'let'] <.end_keyword>>
           <.start-token('SCOPE_DECLARATOR')>
           ':'
           <.end-token('SCOPE_DECLARATOR')>
           <.statement> <.eat_terminator>
        || <.start-element('REGEX_GOAL')>
           <.start-token('REGEX_INFIX')>
           '~'
           <.end-token('REGEX_INFIX')>
           <.rxws>
           [
               <.quantified_atom>
               [ <.rxws> <.quantified_atom>? ]
           ]?
           <.end-element('REGEX_GOAL')>
        || <.mod_internal>
    }

    token rxQ {
        :my $*Q_BACKSLASH = 0;
        :my $*Q_QBACKSLASH = 0;
        :my $*Q_QQBACKSLASH = 0;
        :my $*Q_CLOSURES = 0;
        :my $*Q_SCALARS = 0;
        :my $*Q_ARRAYS = 0;
        :my $*Q_HASHES = 0;
        :my $*Q_FUNCTIONS = 0;
        <.start-token('STRING_LITERAL_QUOTE_OPEN')> '｢' <.end-token('STRING_LITERAL_QUOTE_OPEN')>
        <.quote_Q('｢', '｣', '｣')>
        [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> '｣' <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
    }

    token rxq {
        :my $*Q_BACKSLASH = 0;
        :my $*Q_QBACKSLASH = 0;
        :my $*Q_QQBACKSLASH = 0;
        :my $*Q_CLOSURES = 0;
        :my $*Q_SCALARS = 0;
        :my $*Q_ARRAYS = 0;
        :my $*Q_HASHES = 0;
        :my $*Q_FUNCTIONS = 0;
        <.start-element('STRING_LITERAL')>
        [
        || <.start-token('STRING_LITERAL_QUOTE_OPEN')> '\'' <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           <.quote_q('\'', '\'', '\'')>
           [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> '\'' <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
        || <.start-token('STRING_LITERAL_QUOTE_OPEN')> '‘' <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           <.quote_q('‘', '’', '’')>
           [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> '’' <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
        || <.start-token('STRING_LITERAL_QUOTE_OPEN')> '‚' <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           <.quote_q('‚', '’', '‘')>
           [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> <[’‘]> <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
        || <.start-token('STRING_LITERAL_QUOTE_OPEN')> '’' <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           <.quote_q('’', '’', '‘')>
           [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> <[’‘]> <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
        ]
        <.end-element('STRING_LITERAL')>
    }

    token rxqq {
        :my $*Q_BACKSLASH = 0;
        :my $*Q_QBACKSLASH = 0;
        :my $*Q_QQBACKSLASH = 0;
        :my $*Q_CLOSURES = 0;
        :my $*Q_SCALARS = 0;
        :my $*Q_ARRAYS = 0;
        :my $*Q_HASHES = 0;
        :my $*Q_FUNCTIONS = 0;
        <.start-element('STRING_LITERAL')>
        [
        || <.start-token('STRING_LITERAL_QUOTE_OPEN')> '"' <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           <.quote_qq('"', '"', '"')>
           [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> '"' <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
        || <.start-token('STRING_LITERAL_QUOTE_OPEN')> '“' <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           <.quote_qq('“', '”', '”')>
           [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> '”' <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
        || <.start-token('STRING_LITERAL_QUOTE_OPEN')> '„' <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           <.quote_qq('„', '”', '“')>
           [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> <[”“]> <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
        || <.start-token('STRING_LITERAL_QUOTE_OPEN')> '”' <.end-token('STRING_LITERAL_QUOTE_OPEN')>
           <.quote_qq('”', '”', '“')>
           [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> <[”“]> <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
        ]
        <.end-element('STRING_LITERAL')>
    }

    token rxqw {
        :my $*Q_BACKSLASH = 0;
        :my $*Q_QBACKSLASH = 0;
        :my $*Q_QQBACKSLASH = 0;
        :my $*Q_CLOSURES = 0;
        :my $*Q_SCALARS = 0;
        :my $*Q_ARRAYS = 0;
        :my $*Q_HASHES = 0;
        :my $*Q_FUNCTIONS = 0;
        <.start-element('STRING_LITERAL')>
        <.start-token('STRING_LITERAL_QUOTE_OPEN')> '<' <.end-token('STRING_LITERAL_QUOTE_OPEN')>
        <.quote_q('<', '>', '>')>
        [<.start-token('STRING_LITERAL_QUOTE_CLOSE')> '>' <.end-token('STRING_LITERAL_QUOTE_CLOSE')>]?
        <.end-element('STRING_LITERAL')>
    }

    token backslash {
        <.start-element('REGEX_BUILTIN_CCLASS')>
        [
        || <.start-token('REGEX_BUILTIN_CCLASS')>
           '\\'
           [
           || <[dDnNsSwWeEfFhHrRtTvV0]>
           || 'o' [ <.octint> || '[' <.octints> ']' ]
           || 'x' [ <.hexint> || '[' <.hexints> ']' ]
           || 'c' <.charspec>
           || \W
           ]
           <.end-token('REGEX_BUILTIN_CCLASS')>
        || <.start-token('REGEX_BACKSLASH_BAD')>
           '\\' \w
           <.end-token('REGEX_BACKSLASH_BAD')>
        ]
        <.end-element('REGEX_BUILTIN_CCLASS')>
    }

    token assertion($*METHOD_CALL) {
        || <.start-token('REGEX_ANCHOR')>
           '?' <?before '>'>
           <.end-token('REGEX_ANCHOR')>
        || <.start-token('REGEX_ANCHOR')>
           '!' <?before '>'>
           <.end-token('REGEX_ANCHOR')>
        || <.start-token('METHOD_CALL_OPERATOR')>
           '.'
           <.end-token('METHOD_CALL_OPERATOR')>
           <.assertion(1)>
        || <.start-token('REGEX_LOOKAROUND')>
           '?'
           <.end-token('REGEX_LOOKAROUND')>
           <.assertion(1)>
        || <.start-token('REGEX_LOOKAROUND')>
           '!'
           <.end-token('REGEX_LOOKAROUND')>
           <.assertion(1)>
        || <?before <.name>>
           [
           || <?{ $*METHOD_CALL }>
              <.start-element('REGEX_CALL')>
              <.start-token('METHOD_CALL_NAME')>
              <.name>
              <.end-token('METHOD_CALL_NAME')>
              <.longname_colonpairs>
              <.end-element('REGEX_CALL')>
           || <.start-element('REGEX_CALL')>
              <.start-token('REGEX_CAPTURE_NAME')>
              <.name>
              <.end-token('REGEX_CAPTURE_NAME')>
              <.longname_colonpairs>
              <.end-element('REGEX_CALL')>
           ]
           [
           || <?before '>'>
              <.start-token('REGEX_ASSERTION_END')> <?> <.end-token('REGEX_ASSERTION_END')>
           || <.start-token('INFIX')> '=' <.end-token('INFIX')> <.assertion(0)>
           || <.start-token('INVOCANT_MARKER')>
              ':'
              <.end-token('INVOCANT_MARKER')>
              <.rxarglist>
           || <.start-token('PARENTHESES_OPEN')> '(' <.end-token('PARENTHESES_OPEN')>
              <.rxarglist>
              [ <.start-token('PARENTHESES_CLOSE')> ')' <.end-token('PARENTHESES_CLOSE')> ]?
           || <.normspace> <.regex_nibbler>
           ]?
        || <.start-token('REGEX_ANCHOR')>
           '|' <.identifier>
           <.end-token('REGEX_ANCHOR')>
        || <?[{]> <.rxcodeblock>
        || <?[&]> <.variable>
           [
           || <.start-token('INVOCANT_MARKER')>
              ':'
              <.end-token('INVOCANT_MARKER')>
              <.rxarglist>
           || <.start-token('PARENTHESES_OPEN')> '(' <.end-token('PARENTHESES_OPEN')>
              <.rxarglist>
              [ <.start-token('PARENTHESES_CLOSE')> ')' <.end-token('PARENTHESES_CLOSE')> ]?
           ]?
        || <?sigil> <.variable>
        || <.start-token('REGEX_INFIX')>
           '~~'
           <.end-token('REGEX_INFIX')>
           [
           || <?before '>'>
              <.start-token('REGEX_ASSERTION_END')> <?> <.end-token('REGEX_ASSERTION_END')>
           || <.start-token('REGEX_CAPTURE_NAME')>
              \d+
              <.end-token('REGEX_CAPTURE_NAME')>
           || <.start-token('REGEX_CAPTURE_NAME')>
              <.desigilname>
              <.end-token('REGEX_CAPTURE_NAME')>
           ]?
        || <?before '['||'+'||'-'||':'>
           <.start-element('REGEX_CCLASS')>
           <.cclass_elem>+
           <.end-element('REGEX_CCLASS')>
        || <.start-token('REGEX_MISSING_ASSERTION')>
           <?>
           <.end-token('REGEX_MISSING_ASSERTION')>
    }

    # MIRROR of MAINBraid.java hand-edit (_254_rxarglist): a colon-argument
    # list in a regex assertion may be followed by whitespace -- including a
    # newline -- before the closing '>' (Rakudo's own Grammar.nqp does this).
    token rxarglist {
        :my $*IN_REGEX_ASSERTION = 1;
        <.arglist>
        <.ws>
    }

    token cclass_elem {
        :my $*SIGN = 0;
        <.start-element('REGEX_CCLASS_ELEM')>
        [
            [
            || <.start-token('REGEX_CCLASS_SYNTAX')>
               '+'
               <.end-token('REGEX_CCLASS_SYNTAX')>
            || <.start-token('REGEX_CCLASS_SYNTAX')>
               '-'
               <.end-token('REGEX_CCLASS_SYNTAX')>
            ]
            { $*SIGN = 1 }
        ]?
        <.normspace>?
        [
        || <.start-token('REGEX_CCLASS_SYNTAX')>
           '['
           <.end-token('REGEX_CCLASS_SYNTAX')>
           [
               <?before \s* ['\\'. || <-[\]\\]>]>
               <.start-token('REGEX_CCLASS_ATOM')> <?> <.end-token('REGEX_CCLASS_ATOM')>
               [
                   <.start-token('WHITE_SPACE')>
                   \s+
                   <.end-token('WHITE_SPACE')>
               ]?
               [
               || <.cclass_backslash>
               || <.start-token('STRING_LITERAL_CHAR')>
                  <-[\]\\]>
                  <.end-token('STRING_LITERAL_CHAR')>
               ]
               [
                   <?before \s* '..'>
                   [
                       <.start-token('WHITE_SPACE')>
                       \s+
                       <.end-token('WHITE_SPACE')>
                   ]?
                   <.start-token('REGEX_CCLASS_SYNTAX')>
                   '..'
                   <.end-token('REGEX_CCLASS_SYNTAX')>
                   [
                       || <?before \s* ['\\'. || <-[\]\\]>]>
                          [
                              <.start-token('WHITE_SPACE')>
                              \s+
                              <.end-token('WHITE_SPACE')>
                          ]?
                          [
                          || <.cclass_backslash>
                          || <.start-token('STRING_LITERAL_CHAR')>
                             <-[\]\\]>
                             <.end-token('STRING_LITERAL_CHAR')>
                          ]
                          || <.start-token('REGEX_CCLASS_INCOMPLETE')>
                             <?>
                             <.end-token('REGEX_CCLASS_INCOMPLETE')>
                   ]
               ]?
           ]*
           [
               <.start-token('WHITE_SPACE')>
               \s+
               <.end-token('WHITE_SPACE')>
           ]?
           [
               || <.start-token('REGEX_CCLASS_SYNTAX')>
                  ']'
                  <.end-token('REGEX_CCLASS_SYNTAX')>
               || <.start-token('REGEX_CCLASS_INCOMPLETE')>
                  <?>
                  <.end-token('REGEX_CCLASS_INCOMPLETE')>
           ]
        || <.start-token('METHOD_CALL_NAME')>
           <.identifier>
           <.end-token('METHOD_CALL_NAME')>
        || <.start-token('REGEX_BUILTIN_CCLASS')>
           ':' '!'? <.identifier>
           # XXX optional coloncircumfix here, when MAIN has that
           <.end-token('REGEX_BUILTIN_CCLASS')>
        || <.start-token('REGEX_CCLASS_INCOMPLETE')>
           <?{ $*SIGN }> <?>
           <.end-token('REGEX_CCLASS_INCOMPLETE')>
        ]
        <.normspace>?
        <.end-element('REGEX_CCLASS_ELEM')>
    }

    token cclass_backslash {
        <.start-element('REGEX_BUILTIN_CCLASS')>
        [
        || <.start-token('REGEX_BUILTIN_CCLASS')>
           '\\'
           [
           || 'o' [ <.octint> || '[' <.octints> ']' ]
           || 'x' [ <.hexint> || '[' <.hexints> ']' ]
           || 'c' <.charspec>
           || .
           ]
           <.end-token('REGEX_BUILTIN_CCLASS')>
        ]
        <.end-element('REGEX_BUILTIN_CCLASS')>
    }

    token mod_internal {
        <?before [':' ['!'||\d+]? <.identifier> >> ]>
        [
        || <?before [':s' 'igspace'? >>]> { $*RX_S = 1 }
        || <?before [':!s' 'igspace'? >>]> { $*RX_S = 0 }
        ]?
        <.start-element('REGEX_MOD_INTERNAL')>
        <.start-token('REGEX_MOD_INTERNAL')>
        ':'
        <.end-token('REGEX_MOD_INTERNAL')>
        [
        || <.start-token('REGEX_MOD_INTERNAL')>
           '!'
           <.end-token('REGEX_MOD_INTERNAL')>
           <.mod_ident>
        || <?before \d+>
           <.start-token('REGEX_MOD_INTERNAL_NUMERIC')>
           <?>
           <.end-token('REGEX_MOD_INTERNAL_NUMERIC')>
           <.start-token('REGEX_MOD_INTERNAL')>
           \d+
           <.end-token('REGEX_MOD_INTERNAL')>
           <.mod_ident>
        || <.mod_ident>
           [
               <.start-token('REGEX_MOD_INTERNAL')>
               '('
               <.end-token('REGEX_MOD_INTERNAL')>
               [
                   <.value>
                   [
                       <.start-token('REGEX_MOD_INTERNAL')>
                       ')'
                       <.end-token('REGEX_MOD_INTERNAL')>
                   ]?
               ]?
           ]?
        ]
        <.end-element('REGEX_MOD_INTERNAL')>
    }

    token mod_ident {
        || <.start-token('REGEX_MOD_INTERNAL')>
           [
           || 'i' 'gnorecase'? >>
           || 'm' >>
           || 'ignoremark' >>
           || 'r' 'atchet'? >>
           || 's' 'igspace'? >>
           || 'dba' >>
           ]
           <.end-token('REGEX_MOD_INTERNAL')>
       || <.start-token('REGEX_MOD_UNKNOWN')>
          <.identifier> >>
          <.end-token('REGEX_MOD_UNKNOWN')>
    }
}
