use Java::Generate::CompUnit;
use Java::Generate::Expression;
use Java::Generate::Interface;
use Java::Generate::Literal;
use Java::Generate::Variable;

sub generate-perl6-token-types(@token-names, $prefix, $package) is export {
    my @fields;
    for @token-names.sort -> $name {
        my $default = $name eq 'WHITE_SPACE' | 'BAD_CHARACTER'
            ?? StaticVariable.new(:$name, :type<IElementType>, :class<TokenType>, :access<public>)
            !! ConstructorCall.new(:name($prefix ~ 'ElementType'), :arguments(
                    StringLiteral.new(:value($name))));
        push @fields, InterfaceField.new(:type<IElementType>, :$name, :$default);
    }
    my $interface = Interface.new: :access<public>, :name($prefix ~ 'TokenTypes'), :@fields;
    my $comp-unit = CompUnit.new:
        package => $package,
        imports => <
            com.intellij.psi.TokenType
            com.intellij.psi.tree.IElementType
        >,
        type => $interface;
    return $comp-unit.generate;
}
