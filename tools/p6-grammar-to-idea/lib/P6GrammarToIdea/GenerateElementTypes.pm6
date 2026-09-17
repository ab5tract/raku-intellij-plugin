use Java::Generate::CompUnit;
use Java::Generate::Expression;
use Java::Generate::Interface;
use Java::Generate::Literal;
use Java::Generate::Variable;

sub generate-perl6-element-types(@element-names, $prefix, $package) is export {
    my @fields;
    my %custom;

    if $prefix eq 'Perl6' {
        push @fields, InterfaceField.new: :type<IStubFileElementType>, :name<FILE>, :default(
            ConstructorCall.new(:name<Perl6FileElementType>, :arguments()));

        %custom := set 'PACKAGE_DECLARATION', 'ROUTINE_DECLARATION', 'SUBSET', 'ENUM',
            'CONSTANT', 'REGEX_DECLARATION', 'VARIABLE_DECLARATION', 'SCOPED_DECLARATION',
            'USE_STATEMENT', 'NEED_STATEMENT', 'TYPE_NAME', 'TRAIT', 'SUB_CALL';
        push @fields, InterfaceField.new: :type<IStubElementType>, :name<PACKAGE_DECLARATION>,
            :default(ConstructorCall.new(:name<Perl6PackageDeclStubElementType>));
        push @fields, InterfaceField.new: :type<IStubElementType>, :name<ROUTINE_DECLARATION>,
            :default(ConstructorCall.new(:name<Perl6RoutineDeclStubElementType>));
        push @fields, InterfaceField.new: :type<IStubElementType>, :name<ENUM>,
            :default(ConstructorCall.new(:name<Perl6EnumStubElementType>));
        push @fields, InterfaceField.new: :type<IStubElementType>, :name<SUBSET>,
            :default(ConstructorCall.new(:name<Perl6SubsetStubElementType>));
        push @fields, InterfaceField.new: :type<IStubElementType>, :name<CONSTANT>,
            :default(ConstructorCall.new(:name<Perl6ConstantStubElementType>));
        push @fields, InterfaceField.new: :type<IStubElementType>, :name<REGEX_DECLARATION>,
            :default(ConstructorCall.new(:name<Perl6RegexDeclStubElementType>));
        push @fields, InterfaceField.new: :type<IStubElementType>, :name<VARIABLE_DECLARATION>,
            :default(ConstructorCall.new(:name<Perl6VariableDeclStubElementType>));
        push @fields, InterfaceField.new: :type<IStubElementType>, :name<SCOPED_DECLARATION>,
            :default(ConstructorCall.new(:name<Perl6ScopedDeclStubElementType>));
        push @fields, InterfaceField.new: :type<IStubElementType>, :name<USE_STATEMENT>,
            :default(ConstructorCall.new(:name<Perl6UseStatementStubElementType>));
        push @fields, InterfaceField.new: :type<IStubElementType>, :name<NEED_STATEMENT>,
            :default(ConstructorCall.new(:name<Perl6NeedStatementStubElementType>));
        push @fields, InterfaceField.new: :type<IStubElementType>, :name<TYPE_NAME>,
            :default(ConstructorCall.new(:name<Perl6TypeNameStubElementType>));
        push @fields, InterfaceField.new: :type<IStubElementType>, :name<TRAIT>,
            :default(ConstructorCall.new(:name<Perl6TraitStubElementType>));
        push @fields, InterfaceField.new: :type<IStubElementType>, :name<SUB_CALL>,
            :default(ConstructorCall.new(:name<Perl6SubCallStubElementType>));
    }
    elsif $prefix eq 'CroTemplate' {
        push @fields, InterfaceField.new: :type<IStubFileElementType>, :name<FILE>, :default(
            ConstructorCall.new(:name<CroTemplateFileElementType>, :arguments()));

        %custom := set 'MACRO', 'SUB', 'PART';
        push @fields, InterfaceField.new: :type<IStubElementType>, :name<MACRO>,
            :default(ConstructorCall.new(:name<CroTemplateMacroStubElementType>));
        push @fields, InterfaceField.new: :type<IStubElementType>, :name<SUB>,
            :default(ConstructorCall.new(:name<CroTemplateSubStubElementType>));
        push @fields, InterfaceField.new: :type<IStubElementType>, :name<PART>,
            :default(ConstructorCall.new(:name<CroTemplatePartStubElementType>));
    }
    else {
        push @fields, InterfaceField.new: :type<IFileElementType>, :name<FILE>, :default(
            ConstructorCall.new(:name($prefix ~ 'FileElementType'), :arguments()));
    }

    for @element-names.sort -> $name {
        next if %custom{$name};
        push @fields, InterfaceField.new: :type<IElementType>, :$name, :default(
            ConstructorCall.new(:name($prefix ~ 'ElementType'), :arguments(
                StringLiteral.new(:value($name)))));
    }
    my $interface = Interface.new: :access<public>, :name($prefix ~ 'ElementTypes'), :@fields;
    my $comp-unit = CompUnit.new:
        package => $package,
        imports => <
            com.intellij.psi.tree.IFileElementType
            com.intellij.psi.tree.IElementType
            com.intellij.psi.tree.IStubFileElementType
            com.intellij.psi.stubs.IStubElementType
            edument.perl6idea.psi.stub.*
            edument.perl6idea.cro.template.psi.stub.*
        >,
        type => $interface;
    return $comp-unit.generate;
}
