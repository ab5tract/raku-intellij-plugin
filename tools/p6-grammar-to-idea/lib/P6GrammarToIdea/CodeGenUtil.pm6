use Java::Generate::Expression;
use Java::Generate::Literal;
use Java::Generate::Statement;
use Java::Generate::Variable;

sub int-lit($value) is export {
    Java::Generate::Literal::IntLiteral.new(:$value)
}
sub str-lit($value) is export {
    Java::Generate::Literal::StringLiteral.new(:$value)
}
sub true-lit() is export {
    BooleanLiteral.new(:value)
}
sub false-lit() is export {
    BooleanLiteral.new(:!value)
}
sub field($name, $type) is export {
    InstanceVariable.new(:$name, :$type);
}
multi sub local($name, $type) is export {
    Java::Generate::Statement::LocalVariable.new(:$name, :$type)
}
multi sub local($name, $type, $default) is export {
    Java::Generate::Statement::LocalVariable.new(:$name, :$type, :$default)
}
sub decl($variable) is export {
    Java::Generate::Statement::VariableDeclaration.new(:$variable)
}
sub assign($left, $right) is export {
    Assignment.new(:$left, :$right)
}
sub call($object, $name, *@arguments) is export {
    MethodCall.new: :$object, :$name, :@arguments
}
sub this-call($name, *@arguments) is export {
    MethodCall.new: :object(local('this', 'Object')), :$name, :@arguments
}
sub ret($return) is export {
    Java::Generate::Statement::Return.new(:$return)
}
multi sub if($cond, @true) is export {
    Java::Generate::Statement::If.new(:$cond, :@true)
}
multi sub if($cond, @true, @false) is export {
    Java::Generate::Statement::If.new(:$cond, :@true, :@false)
}
sub unless($cond, @true) is export {
    if(PrefixOp.new(:op<!>, :right($cond)), @true)
}
sub equal($left, $right) is export {
    InfixOp.new(:op<==>, :$left, :$right)
}
sub and($left, $right) is export {
    InfixOp.new(:op<&&>, :$left, :$right)
}
sub break() is export {
    Java::Generate::Statement::Break.new
}
sub continue() is export {
    Java::Generate::Statement::Continue.new
}
sub new($name, *@arguments) is export {
    Java::Generate::Expression::ConstructorCall.new(:$name, :@arguments)
}
