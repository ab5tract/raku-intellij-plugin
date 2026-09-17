use P6GrammarToIdea::AST;
use P6GrammarToIdea::CodeGenUtil;
use P6GrammarToIdea::Elements;
use Java::Generate::Class;
use Java::Generate::CompUnit;
use Java::Generate::Interface;
use Java::Generate::JavaMethod;
use Java::Generate::JavaParameter;
use Java::Generate::JavaSignature;
use Java::Generate::Literal;
use Java::Generate::Variable;

# * Generate a method per rule and call those methods recursively as is needed
# * Methods return whether they managed a successful match or not; this is used
#   as part of compiling alternations and so forth
# * On no-match, roll the lexer back to the point before it last matched

sub production-method($meth-name, $implementation) {
    my $*MARKER-ID = 0;
    my $*TT-ID = 0;
    my $*METHOD-ID = 0;
    my @*METHODS;
    my $*NAME = %*MANGLED{$meth-name};
    my $*NEED-OPP;
    my @impl = compile($implementation);
    my $opp = local('opp', 'OPP');
    flat @*METHODS, ClassMethod.new:
        :access<private>, :name($*NAME), :signature(JavaSignature.new(parameters => [
            JavaParameter.new('builder', 'PsiBuilder')
        ])),
        :return-type<boolean>, :statements[
            decl($opp),
            assign($opp, $*NEED-OPP
                ?? new('OPP', local('builder', 'PsiBuilder'))
                !! NullLiteral.new),
            |@impl,
            ret(true-lit())
        ]
}

multi sub compile(ConcatNode $concat) {
    $concat.terms.map(&compile).flat.list
}

multi sub compile(CallNode $call) {
    unless(
        this-call(%*MANGLED{$call.production-name}, local('builder', 'PsiBuilder')),
        [ret(false-lit())])
}

multi sub compile(ElementNode $elem) {
    my $marker = local('marker' ~ ++$*MARKER-ID, 'PsiBuilder.Marker');
    my $builder = local('builder', 'PsiBuilder');
    (
        decl($marker),
        assign($marker, call($builder, 'mark')),
        |compile($elem.element-parser),
        call($marker, 'done', StaticVariable.new(:name($elem.element-name), :class($*PREFIX ~ 'ElementTypes')))
    )
}

multi sub compile(TokenNode $token) {
    my $builder = local('builder', 'PsiBuilder');
    my $cond = equal(
        call($builder, 'getTokenType'),
        StaticVariable.new(:name($token.token-name), :class($*PREFIX ~ 'TokenTypes'))
    );
    my @decl;
    with $token.literal-value {
        my $tt-var = local('tt' ~ ++$*TT-ID, 'String');
        @decl = decl($tt-var), assign($tt-var, call($builder, 'getTokenText'));
        $cond = and($cond, call($tt-var, 'equals', str-lit($_)));
    }
    flat @decl, if($cond,
        [
            call($builder, 'advanceLexer')
        ],
        [
            ret(false-lit())
        ])
}

# For quantifiers, we put the quantified expression into a new method. We then
# call that, perhaps in a loop, setting a marker before each call. If there is
# no match then we roll back to that marker, otherwise we simply drop the
# marker instead. Failure to reach the minimum causes a false return of this
# expression.
multi sub compile(QuantNode $quant) {
    my @quant-impl = compile($quant.target);
    my $name = $*NAME ~ "_quant_" ~ ++$*METHOD-ID;
    push @*METHODS, ClassMethod.new:
        :access<private>, :$name, :signature(JavaSignature.new(parameters => [
            JavaParameter.new('builder', 'PsiBuilder'),
            JavaParameter.new('opp', 'OPP'),
        ])),
        :return-type<boolean>, :statements[
            |@quant-impl,
            ret(true-lit())
        ];

    my @impl;
    my $builder = local('builder', 'PsiBuilder');
    my $opp = local('opp', 'OPP');
    for ^$quant.min {
        my $marker = local('quantMarker' ~ ++$*MARKER-ID, 'PsiBuilder.Marker');
        push @impl,
            decl($marker),
            assign($marker, call($builder, 'mark')),
            if(this-call($name, $builder, $opp),
                [
                    call($marker, 'drop')
                ],
                [
                    call($marker, 'rollbackTo'),
                    ret(false-lit())
                ]);
    }

    my $loop = $quant.max == Inf
        ?? True
        !! $quant.max == 1
            ?? False
            !! die("Quantifier maximum $quant.max() NYI");
    my $marker = local('quantMarker' ~ ++$*MARKER-ID, 'PsiBuilder.Marker');
    my @min-to-max =
        decl($marker),
        assign($marker, call($builder, 'mark')),
        if(this-call($name, $builder, $opp),
            [
                call($marker, 'drop')
            ],
            [
                call($marker, 'rollbackTo'),
                (break() if $loop)
            ]);
    if $loop {
        push @impl, Java::Generate::Statement::While.new:
            cond => true-lit(),
            body => @min-to-max;
    }
    else {
        append @impl, @min-to-max;
    }

    return @impl;
}

# Alternations are compiled by compiling each branch into a method. We then
# make a marker before each attempt, and roll back if it fails to match. If
# nothing matches, then we return false. This is done by an if/else nest (so
# it's easier to compile in reverse order).
multi sub compile(AltNode $alt) {
    my $builder = local('builder', 'PsiBuilder');
    my $opp = local('opp', 'OPP');
    my @last-else = ret(false-lit());
    for reverse $alt.alternatives {
        my @alt-impl = compile($_);
        my $name = $*NAME ~ "_alt_" ~ ++$*METHOD-ID;
        push @*METHODS, ClassMethod.new:
            :access<private>, :$name, :signature(JavaSignature.new(parameters => [
                JavaParameter.new('builder', 'PsiBuilder'),
                JavaParameter.new('opp', 'OPP')
            ])),
            :return-type<boolean>, :statements[
                |@alt-impl,
                ret(true-lit())
            ];

        my $marker = local('altMarker' ~ ++$*MARKER-ID, 'PsiBuilder.Marker');
        @last-else =
            decl($marker),
            assign($marker, call($builder, 'mark')),
            if(this-call($name, $builder, $opp),
                [
                    call($marker, 'drop')
                ],
                [
                    call($marker, 'rollbackTo'),
                    |@last-else
                ]);
    }
    return @last-else;
}

multi sub compile(OPPNode $o) {
    $*NEED-OPP = 1;
    my $name = $o.method-name.subst('opp-', '').subst(/'-'(\w)/, { $0.uc }, :g);
    call(local('opp', 'OPP'), $name)
}

multi sub compile(PassNode) {
    ()
}

multi sub compile(FailNode) {
    ret(false-lit())
}

multi sub compile($nyi) {
    die "$nyi.^name() compilation NYI in grammar generator";
}

sub braid-methods(P6GrammarToIdea::Elements::BraidModel $bm) {
    $bm.production-names.sort.map: { production-method($^name, $bm.get-production-model($^name)) }
}

sub parse-entry-method() {
    my $root-marker = local('rootMarker', 'PsiBuilder.Marker');
    my $builder = local('builder', 'PsiBuilder');
    ClassMethod.new:
        :access<public>, :name<parse>, :signature(JavaSignature.new(parameters => [
            JavaParameter.new('root', 'IElementType'),
            JavaParameter.new('builder', 'PsiBuilder')
        ])),
        :return-type<ASTNode>, :statements[
            decl($root-marker),
            assign($root-marker, call($builder, 'mark')),
            this-call(%*MANGLED<TOP>, $builder),
            call($root-marker, 'done', local('root', 'IElementType')),
            ret(call($builder, 'getTreeBuilt'))
        ]
}

sub generate-parser(P6GrammarToIdea::Elements::Model $element-model, $prefix, $package) is export {
    my $main-model = $element-model.get-braid-model('MAIN');
    my %*MANGLED = $main-model.production-names.sort.map({ $_ => mangle($_, ++$) });
    my $*PREFIX = $prefix;
    my @methods = flat parse-entry-method(), braid-methods($main-model);
    my $class = Class.new: :access<public>, :name($prefix ~ "Parser"),
        :interfaces(Interface.new(:name('PsiParser'))), :@methods;
    my $comp-unit = CompUnit.new:
        package => $package,
        imports => <
            com.intellij.lang.ASTNode
            com.intellij.psi.tree.IElementType
            com.intellij.lang.PsiBuilder
            com.intellij.lang.PsiParser
            edument.perl6idea.parsing.OPP
        >,
        type => $class;
    return $comp-unit.generate;
}

sub mangle($name, $id) {
    "$name.subst(/<-[A..Za..z_]>+/, '', :g)_$id"
}
