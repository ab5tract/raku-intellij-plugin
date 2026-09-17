use P6GrammarToIdea::Parser;
use P6GrammarToIdea::Actions;
use P6GrammarToIdea::Elements;
use Test;

my $ast = P6GrammarToIdea::Parser.parse(Q:to/GRAMMAR/, :actions(P6GrammarToIdea::Actions)).ast;
    grammar MAIN {
        token TOP {
            <.start-element('FILE')>
            <.statementlist>
            <.end-element('FILE')>
        }

        token statementlist {
            <.start-element('STATEMENT_LIST')>
            <.statement>*
            <.end-element('STATEMENT_LIST')>
        }

        token statement {
            <.ws>?
            <.start-element('STATEMENT')>
            [
            || <.statement_control>
            || <?>
            ]
            <.bogus_statement>?
            <.start-token('STATEMENT_TERMINATOR')>
            ';'
            <.end-token('STATEMENT_TERMINATOR')>
            <.end-element('STATEMENT')>
            <.ws>?
        }

        token bogus_statement {
            <.start-token('BAD_CHARACTER')>
            <-[;]>+
            <.end-token('BAD_CHARACTER')>
        }

        token ws {
            <.start-token('WHITE_SPACE')>
            \s+
            <.end-token('WHITE_SPACE')>
        }

        token statement_control {
            || <.statement_control_use>
        }

        token statement_control_use {
            <.start-element('USE_STATEMENT')>
            <.start-token('STATEMENT_CONTROL')>
            'use'
            <.end-token('STATEMENT_CONTROL')>
            <.ws>
            <.name>
            <.end-element('USE_STATEMENT')>
        }

        token name {
            <.start-token('NAME')>
            \w+
            <.end-token('NAME')>
        }
    }
    GRAMMAR

my $model;
lives-ok { $model = build-element-model($ast) },
    'Can build element model';

my $bm;
lives-ok { $bm = $model.get-braid-model('MAIN') },
    'Can get braid model for MAIN braid';

subtest {
    given $bm.get-production-model('TOP') {
        isa-ok $_, ElementNode;
        is .element-name, 'FILE', 'Correct element name';
        given .element-parser {
            isa-ok $_, CallNode, 'Element parser is a call';
            is .production-name, 'statementlist', 'Call is to expected production';
        }
    }
}, 'TOP model';

subtest {
    given $bm.get-production-model('statementlist') {
        isa-ok $_, ElementNode;
        is .element-name, 'STATEMENT_LIST', 'Correct element name';
        given .element-parser {
            isa-ok $_, QuantNode, 'Element parser is a quantified node';
            is .min, 0, 'Correct min';
            is .max, Inf, 'Correct max';
            given .target {
                isa-ok $_, CallNode, 'Quantified thing is a call node';
                is .production-name, 'statement', 'Call is to expected production';
            }
        }
    }
}, 'statementlist model';

subtest {
    given $bm.get-production-model('statement') {
        isa-ok $_, ConcatNode;
        is .terms.elems, 3, 'Expected number of top-level nodes in concat';
        given .terms.[0] {
            isa-ok $_, QuantNode;
            is .min, 0, 'First quant min ok';
            is .max, 1, 'First quant max ok';
            given .target {
                isa-ok $_, CallNode;
                is .production-name, 'ws', 'Call is to ws rule';
            }
        }
        given .terms.[1] {
            isa-ok $_, ElementNode;
            is .element-name, 'STATEMENT', 'Element name is correct';
            given .element-parser {
                isa-ok $_, ConcatNode;
                is .terms.elems, 3, 'Element parser is 3-node concat';
                given .terms.[0] {
                    isa-ok $_, AltNode;
                    is .alternatives.elems, 2, 'Correct number of alternation elements';
                    given .alternatives[0] {
                        isa-ok $_, CallNode;
                        is .production-name, 'statement_control', 'Correct alt call node';
                    }
                    given .alternatives[1] {
                        isa-ok $_, PassNode;
                    }
                }
                given .terms.[1] {
                    isa-ok $_, QuantNode;
                    is .min, 0, 'Element parser with quant min ok';
                    is .max, 1, 'Element parser with quant max ok';
                    given .target {
                        isa-ok $_, CallNode;
                        is .production-name, 'bogus_statement', 'Correct production name';
                    }
                }
                given .terms.[2] {
                    isa-ok $_, TokenNode;
                    is .token-name, 'STATEMENT_TERMINATOR', 'Correct token name';
                    is .literal-value, ';', 'Correct literal value';
                }
            }
        }
        given .terms.[2] {
            isa-ok $_, QuantNode;
            is .min, 0, 'Second quant min ok';
            is .max, 1, 'Second quant max ok';
            given .target {
                isa-ok $_, CallNode;
                is .production-name, 'ws', 'Call is to ws rule';
            }
        }
    }
}, 'statement model';

subtest {
    given $bm.get-production-model('bogus_statement') {
        isa-ok $_, TokenNode;
        is .token-name, 'BAD_CHARACTER', 'Correct token name';
        nok .literal-value.defined, 'No literal value';
    }
}, 'bogus_statement model';

subtest {
    given $bm.get-production-model('ws') {
        isa-ok $_, TokenNode;
        is .token-name, 'WHITE_SPACE', 'Correct token name';
        nok .literal-value.defined, 'No literal value';
    }
}, 'ws model';

subtest {
    given $bm.get-production-model('statement_control') {
        isa-ok $_, CallNode;
        is .production-name, 'statement_control_use', 'Correct production name';
    }
}, 'statement_control model';

subtest {
    given $bm.get-production-model('statement_control_use') {
        isa-ok $_, ElementNode;
        is .element-name, 'USE_STATEMENT', 'Correct element name at top level';
        given .element-parser {
            isa-ok $_, ConcatNode;
            is .terms.elems, 3, 'Expected number of elems in concat node';
            given .terms.[0] {
                isa-ok $_, TokenNode;
                is .token-name, 'STATEMENT_CONTROL', 'First node is expected token';
                is .literal-value, 'use', 'Token has expected literal value';
            }
            given .terms.[1] {
                isa-ok $_, CallNode;
                is .production-name, 'ws', 'Have expected ws call';
            }
            given .terms.[2] {
                isa-ok $_, CallNode;
                is .production-name, 'name', 'Have expected name call';
            }
        }
    }
}, 'statement_control_use model';

subtest {
    given $bm.get-production-model('name') {
        isa-ok $_, TokenNode;
        is .token-name, 'NAME', 'Correct token name';
        nok .literal-value.defined, 'No literal value';
    }
}, 'name model';

done-testing;
