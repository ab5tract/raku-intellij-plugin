use P6GrammarToIdea::AST;
use P6GrammarToIdea::CodeGenUtil;
use Java::Generate::Class;
use Java::Generate::CompUnit;
use Java::Generate::Expression;
use Java::Generate::JavaMethod;
use Java::Generate::JavaParameter;
use Java::Generate::JavaSignature;
use Java::Generate::Variable;

my class GrammarCompiler {
    my constant PASS = -1;
    my constant FAIL = -2;
    my constant END-TOKEN = -3;

    has $.grammar is required;
    has $.prefix is required;
    has Int %!rule-numbers = $!grammar.productions.map(*.name).antipairs;
    has Str %!rule-methods = $!grammar.productions.map:
        { .name => "_%!rule-numbers{.name}_&mangle(.name)" };

    method compile-run-rule() {
        my $result = local('result', 'int');
        return ClassMethod.new:
            :access<public>, :name<runRule>, :signature(JavaSignature.new),
            :return-type<int>, :statements[
                decl($result),
                Java::Generate::Statement::Switch.new(
                    switch => field('ruleNumber', 'int'),
                    branches => %!rule-numbers.keys.sort({ %!rule-numbers{$_} }).map(-> $name {
                        int-lit(%!rule-numbers{$name}) => [
                            assign($result, this-call(%!rule-methods{$name}))
                        ]
                    }),
                    default => [
                        Java::Generate::Statement::Throw.new(:exception('RuntimeException'))
                    ]
                ),
                ret($result)
            ]
    }

    method compile-rules() {
        my @*LOOKAHEADS;
        my @compiled-rules = $!grammar.productions.map({ self!compile-rule($_) });
        return flat @compiled-rules, @*LOOKAHEADS;
    }

    method !compile-rule($rule) {
        self!compile-tree($rule.implementation, $rule.name, :parameters($rule.parameters))
    }

    method !compile-tree($implementation, $name, :$parameters) {
        # The methods that each production rule or lookahead compiles into are
        # state machines, the current state being in the cursor (which is
        # `this`). The top level is a switch over the states.
        my @statements;
        my @*STATE-STATEMENTS;
        my $*CUR-STATEMENTS;
        my $*NEED-REP = False;
        self!new-state();
        with $parameters -> @parameters {
            $*CUR-STATEMENTS.push: this-call('checkArgs', int-lit(@parameters.elems));
            for @parameters.kv -> $idx, $name {
                $*CUR-STATEMENTS.push: this-call('declareDynamicVariable',
                    str-lit($name),
                    this-call('getArg', int-lit($idx)));
            }
        }
        self.compile($implementation);
        $*CUR-STATEMENTS.push(ret(int-lit(PASS)));
        if $*NEED-REP {
            push @statements, decl(local('rep', 'int'));
        }
        push @statements, Java::Generate::Statement::While.new:
            cond => true-lit(),
            body => Java::Generate::Statement::Switch.new:
                switch => field('state', 'int'),
                branches => @*STATE-STATEMENTS.pairs.map({ int-lit(.key) => .value });
        return ClassMethod.new:
            :access<private>, :name(%!rule-methods{$name}),
            :signature(JavaSignature.new), :return-type<int>, :@statements
    }

    method !new-state() {
        my $state = +@*STATE-STATEMENTS;
        @*STATE-STATEMENTS.push($*CUR-STATEMENTS = []);
        return $state;
    }

    multi method compile(Concat $conc) {
        for $conc.terms {
            self.compile($_);
        }
    }

    multi method compile(SeqAlt $alt) {
        my @alts = $alt.alternatives;
        my @insert-goto-statements;
        my $insert-initial-mark-statements = $*CUR-STATEMENTS;
        my $insert-initial-mark-index = $*CUR-STATEMENTS.elems;
        while @alts > 1 {
            # We need to insert a push onto the backtracking stack for the
            # case where a non-final element of the alternation fails. Save
            # the point to insert the push, then compile, then create a new
            # state, then push it. Also save places that need to have the
            # final state, after the alternation, to jump to.
            my $insert = $*CUR-STATEMENTS;
            my $insert-at = $*CUR-STATEMENTS.elems;
            self.compile(@alts.shift);
            push @insert-goto-statements, $*CUR-STATEMENTS;
            my $failed-state = self!new-state();
            $insert.splice($insert-at, 0, [this-call('bsMark', int-lit($failed-state))]);
        }
        self.compile(@alts.shift);
        my $after-last-statement = $*CUR-STATEMENTS;
        my $success = self!new-state();
        $after-last-statement.append: [
            assign(field('state', 'int'), int-lit($success)),
            continue()
        ];
        $insert-initial-mark-statements.splice($insert-initial-mark-index, 0, [
            this-call('bsFailMark', int-lit($success))
        ]);
        for @insert-goto-statements -> $stmts {
            $stmts.append: [
                this-call('bsCommit', int-lit($success)),
                assign(field('state', 'int'), int-lit($success)),
                continue()
            ];
        }
    }

    multi method compile(Quantifier $quant) {
        my ($min, $max) = $quant.min, $quant.max;
        return if $min == $max == 0;

        # Save point that we should insert push of initial backtrack mark.
        my $insert-initial-mark-statements = $*CUR-STATEMENTS;
        my $insert-initial-mark-index = $*CUR-STATEMENTS.elems;

        # Need a new state which will be used as the quantifier loop.
        my $loop = self!new-state();

        # Compile the target being quantified.
        self.compile($quant.target);

        # We need a done state, but then to insert things before it.
        my $pre-sep-statements = $*CUR-STATEMENTS;
        my ($sep, $done, $pre-done-statements);
        if $max != 1 && $quant.separator {
            $sep = self!new-state();
            self.compile($quant.separator);
            $pre-done-statements = $*CUR-STATEMENTS;
            $done = self!new-state();
        }
        else {
            $done = self!new-state();
            $pre-done-statements = $pre-sep-statements;
        }

        # Insert initial backtrack mark, then continue to loop state.
        $insert-initial-mark-statements.splice($insert-initial-mark-index, 0, [
            this-call($min == 0 ?? 'bsMark' !! 'bsFailMark', int-lit($done)),
            assign(field('state', 'int'), int-lit($loop))
        ]);

        # Retrieve repeition counter and increment it (unless `?`); also
        # commit.
        unless $min == 0 && $max == 1 {
            $*NEED-REP = True;
            $pre-sep-statements.append: [
                assign(local('rep', 'int'), this-call('peekRep', int-lit($done))),
                PrefixOp.new(op => '++', right => local('rep', 'int'))
            ];
        }
        $pre-sep-statements.push: this-call('bsCommit', int-lit($done));

        # If we have a maximum to reach, enforce it.
        if $max != Inf && $max > 1 {
            $pre-sep-statements.push: if(
                cond => InfixOp.new(
                    left => local('rep', 'int'),
                    op => '>=',
                    right => int-lit($max)
                ),
                then => [
                    assign(field('state', 'int'), int-lit($done)),
                    continue()
                ]);
        }

        # Unless one match is what we want, loop.
        if $max == 1 {
            $pre-sep-statements.append: [
                assign(field('state', 'int'), int-lit($done)),
                continue()
            ];
        }
        elsif $sep {
            $pre-sep-statements.append: [
                this-call('bsMark', int-lit($done), local('rep', 'int')),
                assign(field('state', 'int'), int-lit($sep)),
                continue()
            ];
            $pre-done-statements.append: [
                assign(field('state', 'int'), int-lit($loop)),
                continue()
            ];
        }
        else {
            $pre-sep-statements.append: [
                this-call('bsMark', int-lit($done), local('rep', 'int')),
                assign(field('state', 'int'), int-lit($loop)),
                continue()
            ];
        }

        # Enforce minimum.
        if $min > 1 {
            die "Compiling quantifier with minimum > 1 NYI";
        }
    }

    multi method compile(Subrule $rule) {
        given $rule.name {
            my $append-to = $*CUR-STATEMENTS;
            when 'start-token' {
                $append-to.push: this-call 'startToken',
                    StaticVariable.new(:name($rule.args[0].value), :class($!prefix ~ 'TokenTypes'));
            }
            when 'end-token' {
                my $next = self!new-state();
                $append-to.push: assign(field('state', 'int'), int-lit($next));
                $append-to.push: ret(int-lit(END-TOKEN));
            }
            when 'start-element' | 'end-element' {
                # These aren't relevant for lexer generation
            }
            when 'opp-push-prefix' | 'opp-push-postfix' | 'opp-end-infix' {
                my $next = self!new-state();
                $append-to.push: this-call('precInfoToken');
                $append-to.push: assign(field('state', 'int'), int-lit($next));
                $append-to.push: ret(int-lit(END-TOKEN));
            }
            when /^ 'opp-'/ {
                # Other OPP calls are only important to the parser
            }
            when 'scope-push' {
                $append-to.push: this-call 'scopePush';
            }
            when 'scope-pop' {
                $append-to.push: this-call 'scopePop';
            }
            when 'start-symbol' {
                $append-to.push: this-call 'startSymbol';
            }
            when 'end-symbol' {
                unless $rule.args.elems == 1 && $rule.args[0] ~~ StrValue {
                    die "end-symbol must be called with a single string argument";
                }
                my $sym-type = $rule.args[0].value;
                my constant @valid-types = <term type routine>;
                unless $sym-type eq any(@valid-types) {
                    die "end-symbol argument must be one of @valid-types.join(", ")";
                }
                $append-to.push: this-call 'endSymbol', str-lit($sym-type);
            }
            when 'is-name' {
                $append-to.push: unless(this-call('isName'), [backtrack()]);
            }
            when 'pod-ws-start' {
                $append-to.push: this-call 'startPodWhitespacePrefix';
            }
            when 'pod-ws-commit' {
                $append-to.push: this-call 'commitPodWhitespacePrefix';
            }
            when 'pod-eat-removed-ws' {
                $append-to.push: this-call 'eatPodWhitespacePrefix';
            }
            when 'alpha' {
                $append-to.push: unless(this-call('alphaChar'), [backtrack()]);
            }
            when 'ww' {
                $append-to.push: unless(this-call('ww'), [backtrack()]);
            }
            when 'peek-delimiters' {
                $append-to.push: unless(this-call('peekDelimiters'), [backtrack()]);
            }
            when 'opener' {
                $append-to.push: unless(this-call('opener'), [backtrack()]);
            }
            when 'bracket-ending' {
                $append-to.push: unless(this-call('bracketEnding'), [backtrack()]);
            }
            when 'start-queue-heredoc' {
                $append-to.push: this-call('startQueueHeredoc');
            }
            when 'end-queue-heredoc' {
                $append-to.push: this-call('endQueueHeredoc');
            }
            when 'dequeue-heredoc' {
                $append-to.push: unless(this-call('dequeueHeredoc'), [backtrack()]);
            }
            when 'has-heredoc' {
                $append-to.push: unless(this-call('hasHeredoc'), [backtrack()]);
            }
            when 'MARKER' {
                unless $rule.args.elems == 1 && $rule.args[0] ~~ StrValue {
                    die "MARKER must be called with a single string argument";
                }
                $append-to.push: this-call('marker', str-lit($rule.args[0].value));
            }
            when 'MARKED' {
                unless $rule.args.elems == 1 && $rule.args[0] ~~ StrValue {
                    die "MARKED must be called with a single string argument";
                }
                $append-to.push: unless(
                    this-call('marked', str-lit($rule.args[0].value)),
                    [backtrack()]);
            }
            default {
                my $next = self!new-state();
                my $rule-number = %!rule-numbers{$rule.name};
                my @args = $rule.args.map({ self.compile-value($_) });
                $append-to.push: this-call('setArgs', |@args);
                $append-to.push: assign(field('state', 'int'), int-lit($next));
                $append-to.push: ret(int-lit($rule-number));
                $*CUR-STATEMENTS.push: if(
                    call(field('lastResult', 'cursor'), 'isFailed'),
                    [backtrack()],
                    [assign(field('pos', 'int'), call(field('lastResult', 'cursor'), 'getPos'))]
                );
            }
        }
    }

    multi method compile(Literal $lit) {
        $*CUR-STATEMENTS.push: unless(this-call('literal', str-lit($lit.value)), [backtrack()]);
    }

    multi method compile(EnumCharList $enum) {
        my $method = $enum.negative ?? 'notInCharList' !! 'inCharList';
        $*CUR-STATEMENTS.push: unless(this-call($method, str-lit($enum.chars)), [backtrack()]);
    }

    multi method compile(BuiltinCharClass $cclass) {
        my $charType = do given $cclass.class {
            when AnyChar {
                die "Cannot have negated AnyChar" if $cclass.negative;
                "any"
            }
            when WordChars {
                "word"
            }
            when DigitChars {
                "digit"
            }
            when SpaceChars {
                "space"
            }
            when NewlineChars {
                "newline"
            }
        }
        my $method = ($cclass.negative ?? "not$charType.tc()" !! $charType) ~ "Char";
        $*CUR-STATEMENTS.push: unless(this-call($method), [backtrack()]);
    }

    multi method compile(AnchorRightWordBoundary) {
        $*CUR-STATEMENTS.push: unless(this-call('rightWordBoundary'), [backtrack()]);
    }

    multi method compile(AnchorLineEnd) {
        $*CUR-STATEMENTS.push: unless(this-call('endOfLine'), [backtrack()]);
    }

    multi method compile(AnchorLineStart) {
        $*CUR-STATEMENTS.push: unless(this-call('startOfLine'), [backtrack()]);
    }

    multi method compile(AnchorEnd) {
        $*CUR-STATEMENTS.push: unless(this-call('endOfString'), [backtrack()]);
    }

    multi method compile(AnchorPass) {
        # Just continue onwards past this point, no action needed
    }

    multi method compile(AnchorFail) {
        $*CUR-STATEMENTS.push: backtrack();
    }

    multi method compile(Lookahead $lookahead) {
        # Generate a name for this lookahead and register it an index (claim
        # the index right away in case of compiling recursive lookaheads).
        my $index = @*LOOKAHEADS.elems;
        @*LOOKAHEADS[$index] = Nil;
        my $name = "___lookahead_" ~ $index;
        my $rule-number = %!rule-numbers{$name} = %!rule-numbers.elems;
        %!rule-methods{$name} = $name;

        # Compile the lookahead as a separate method.
        @*LOOKAHEADS[$index] = self!compile-tree($lookahead.target, $name);

        # Generate code in the current method, depending on positive or
        # negative lookahead.
        $*CUR-STATEMENTS.push: $lookahead.negative
            ?? if(this-call('lookahead', int-lit($rule-number)), [backtrack()])
            !! unless(this-call('lookahead', int-lit($rule-number)), [backtrack()])
    }

    multi method compile(Interpolation $i) {
        $*CUR-STATEMENTS.push: unless(
            this-call('interpolate', str-lit($i.variable-name)),
            [backtrack()]);
    }

    multi method compile(Declaration $d) {
        $*CUR-STATEMENTS.push: this-call('declareDynamicVariable',
            str-lit($d.variable-name),
            self.compile-value($d.value));
    }

    multi method compile(CodeBlock $c) {
        given $c.type {
            when SimpleCode {
                $*CUR-STATEMENTS.push: self.compile-code($c.statement);
            }
            when PositiveCodeAssertion {
                $*CUR-STATEMENTS.push: unless(
                    this-call('isValueTruthy', self.compile-code($c.statement)),
                    [backtrack()]);
            }
            when NegativeCodeAssertion {
                $*CUR-STATEMENTS.push: if(
                    this-call('isValueTruthy', self.compile-code($c.statement)),
                    [backtrack()]);
            }
            default {
                die "Unknown code block type $_";
            }
        }
    }

    multi method compile-code(DynamicLookup $a) {
        this-call('findDynamicVariable',
            str-lit($a.variable-name));
    }

    multi method compile-code(DynamicAssignment $a) {
        this-call('assignDynamicVariable',
            str-lit($a.variable-name),
            self.compile-value($a.value));
    }

    multi method compile-code(TestStr $t) {
        my $op-name = "testStr$t.op.uc()";
        this-call($op-name, self.compile-code($t.left), self.compile-code($t.right))
    }

    multi method compile-code(StrValue $s) {
        str-lit($s.value)
    }

    multi method compile-code(IntValue $i) {
        int-lit($i.value)
    }

    multi method compile($unknown) {
        die "Unimplemented compilation of node type $unknown.^name()";
    }

    method compile-value($_) {
        when StrValue { str-lit(.value) }
        when IntValue { int-lit(.value) }
        when DynamicLookup { self.compile-code($_) }
        default { die "Unknown argument type $_.^name()" }
    }

    sub backtrack() {
        if(this-call("backtrack"),
            [continue()],
            [ret(int-lit(FAIL))])
    }

    sub mangle($name) {
        $name.subst(/<-alpha>+/, '_', :g)
    }
}

sub generate-lexer(Grammar $grammar, $prefix, $package) is export {
    my $class-name = "$grammar.name()Braid";
    my $compiler = GrammarCompiler.new(:$grammar, :$prefix);
    my @rule-methods = $compiler.compile-rules;
    my @methods = flat $compiler.compile-run-rule, @rule-methods,
        ClassMethod.new:
            :access<public>, :name<createInstance>, :signature(JavaSignature.new),
            :return-type($class-name), :statements[
                ret(new($class-name))
            ];
    my $class = Class.new: :access<public>, :name($class-name),
        :super(Class.new(:name("Cursor<$grammar.name()Braid>"))), :@methods;
    my $comp-unit = CompUnit.new:
        package => $package,
        imports => <
            edument.perl6idea.parsing.Cursor
        >,
        type => $class;
    return $comp-unit.generate;
}
