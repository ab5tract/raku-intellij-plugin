sub MAIN(*@names) {
    for @names -> $name {
        my $impl = $name ~ "Impl";
        spurt "../../src/edument/perl6idea/psi/$name.java", q:b:s:to/INTERFACE/;
            package edument.perl6idea.psi;

            public interface $name {
            }
            INTERFACE
        spurt "../../src/edument/perl6idea/psi/impl/$impl.java", q:s:to/CLASS/.subst('Impl (', 'Impl(');
            package edument.perl6idea.psi.impl;

            import com.intellij.extapi.psi.ASTWrapperPsiElement;
            import com.intellij.lang.ASTNode;
            import edument.perl6idea.psi.$name;
            import org.jetbrains.annotations.NotNull;

            public class $impl extends ASTWrapperPsiElement implements $name {
                public $impl (@NotNull ASTNode node) {
                    super(node);
                }
            }
            CLASS
    }
}
