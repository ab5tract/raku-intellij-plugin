package org.raku.comma.inspection;

// This is designed to avoid reconstruction of constant values, as Kotlin is somewhat wonky about
// static declarations and the IntelliJ Platform documentation asks us not to use companion objects.

// But this is also 50% because the IDE will put it's stupid parameter name hint next to a bare string
// in holder.registerProblem(...) calls, but will omit this hint when the text is in a variable.

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import kotlin.text.Regex;
import org.raku.comma.parsing.RakuTokenTypes;
import org.raku.comma.psi.*;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public class InspectionConstants {

    public static class BuiltinSubmethod {
        public static String DESCRIPTION_FORMAT = "'%s' should be declared as a submethod";
        public static Set<String> SHOULD_BE_SUBMETHOD_NAMES = Set.of("BUILD", "TWEAK");
    }

    public static class MissingThings {
        public static final TokenSet T_PAREN_OPEN = TokenSet.create(RakuTokenTypes.PARENTHESES_OPEN);
        public static final TokenSet T_PAREN_CLOSE = TokenSet.create(RakuTokenTypes.PARENTHESES_CLOSE);
        public static final TokenSet T_ARRAY_COMP_OPEN = TokenSet.create(RakuTokenTypes.ARRAY_COMPOSER_OPEN);
        public static final TokenSet T_ARRAY_COMP_CLOSE = TokenSet.create(RakuTokenTypes.ARRAY_COMPOSER_CLOSE);
        public static final TokenSet T_ARRAY_INDEX_OPEN = TokenSet.create(RakuTokenTypes.ARRAY_INDEX_BRACKET_OPEN);
        public static final TokenSet T_ARRAY_INDEX_CLOSE = TokenSet.create(RakuTokenTypes.ARRAY_INDEX_BRACKET_CLOSE);
        public static final TokenSet T_BLOCK_OPEN = TokenSet.create(RakuTokenTypes.BLOCK_CURLY_BRACKET_OPEN);
        public static final TokenSet T_BLOCK_CLOSE = TokenSet.create(RakuTokenTypes.BLOCK_CURLY_BRACKET_CLOSE);
        public static final TokenSet T_RX_GROUP_OPEN = TokenSet.create(RakuTokenTypes.REGEX_GROUP_BRACKET_OPEN);
        public static final TokenSet T_RX_GROUP_CLOSE = TokenSet.create(RakuTokenTypes.REGEX_GROUP_BRACKET_CLOSE);
        public static final TokenSet T_RX_ASSERT_OPEN = TokenSet.create(RakuTokenTypes.REGEX_ASSERTION_ANGLE_OPEN);
        public static final TokenSet T_RX_ASSERT_CLOSE = TokenSet.create(RakuTokenTypes.REGEX_ASSERTION_ANGLE_CLOSE);
        public static final TokenSet T_RX_CAP_OPEN = TokenSet.create(RakuTokenTypes.REGEX_CAPTURE_PARENTHESES_OPEN);
        public static final TokenSet T_RX_CAP_CLOSE = TokenSet.create(RakuTokenTypes.REGEX_CAPTURE_PARENTHESES_CLOSE);

        public static final String DESCRIPTION_FORMAT = "Missing closing '%s'";
    }

    public static class RedeclaredImportedSymbol {
        public static final String DESCRIPTION_FORMAT = "Re-declaration of %s from %s:%d";
    }

    public static class MissingRoleMethod {
        public static final Regex SPLIT_REGEX = new Regex("\\s+");
        public static final String DESCRIPTION_FORMAT = "Composed roles require to implement methods: %s";
    }

    public static class UnitKeyword {
        public static final String DESCRIPTION_FORMAT = "Semicolon form of '%s' without 'unit' is not supported";
        public static final String DESCRIPTION_DEFAULT = "Semicolon form of package declaration without 'unit' is not supported";
        public static final String DESCRIPTION_BLOCK_FORM =  "Cannot use 'unit' with block form of declaration";

        public static final Predicate<PsiElement> IS_WHITESPACE = element -> (element instanceof PsiWhiteSpace)
                || (element != null && element.getNode().getElementType() == RakuTokenTypes.UNV_WHITE_SPACE);
        public static final Predicate<PsiElement> IS_COMMENT = element -> element instanceof PsiComment
                || (element instanceof RakuComment);
    }

    public static class MonitorUsage {
        public static final String DESCRIPTION = "Cannot create package of 'monitor' type without OO::Monitors imported into scope";
        public static final String OO_MONITORS = "OO::Monitors";
    }

    public static class MyScopedVariableExported {
        public static final String DESCRIPTION = "`my` scoped variable cannot be exported";
    }

    public static class NamedPairArgument {
        public static final String DESCRIPTION = "Pair literal can be simplified";

        public static String getSimplifiedPair(PsiElement pair, String key, PsiElement element) {
            if (element instanceof RakuTypeName) {
                if (!PsiTreeUtil.isAncestor(pair, element, false)) return null;
                String typeName = ((RakuTypeName)element).getTypeName();
                if (typeName.equals("True")) return key;
                if (typeName.equals("False")) return "!" + key;
            }
            if (element instanceof RakuVariable) {
                if (!PsiTreeUtil.isAncestor(pair, element, false)) return null;
                String name = ((RakuVariable)element).getVariableName();
                if (name == null || name.length() < 2) return null;

                int prefixLength = RakuVariable.getTwigil(name) == ' ' ? 1 : 2;

                if (Objects.equals(key, name.substring(prefixLength))) return name;
            }
            return null;
        }
    }

    public static class RakudoImplementationDetails {
        public static final Set<String> IGNORED_CALLS = Set.of("dd");
        public static final String DESCRIPTION_FORMAT_ROUTINE = "The '&%s' %s is implementation detail";
        public static final String DESCRIPTION_FORMAT_METHOD = "The '%s' %s is implementation detail";
    }

    public static class NoEndpointRange {
        public static final Predicate<PsiElement> ENDPOINT_RELEVANCE = element ->
               element instanceof RakuWhatever
            || element instanceof RakuIntLiteral
            || element instanceof RakuComplexLiteral
            || element instanceof RakuNumLiteral
            || element instanceof RakuRatLiteral
            || element instanceof RakuVariable
            || element instanceof RakuParenthesizedExpr
            || element instanceof RakuInfixApplication
            || element instanceof RakuStrLiteral
            || element instanceof RakuPrefixApplication
            || element instanceof RakuPostfixApplication;

        public static final String DESCRIPTION = "The range operator must have a second argument";
    }

    public static class NonInheritableComposableDeclaration {
        public static final String DESCRIPTION_FORMAT = "%s cannot %s";
    }
    
    public static class NonNilReturn {
        public static final String DESCRIPTION = "A value is returned from subroutine returning Nil";
    }

    public static class NotProgressingRegex {
        public static final String DESCRIPTION = "Quantified expression may not progress, leading to a hang";
    }

    public static class NullRegex {
        public static final String DESCRIPTION = "Empty regex is not allowed";
    }

    public static class ProblematicReturn {
        public static final String DESCRIPTION_START = "Cannot use return to produce a result in a start block";
        public static final String DESCRIPTION_REACT = "Cannot use return to exit a react block";
        public static final String DESCRIPTION_SUPPLY = "Cannot use return to exit a supply block";
        public static final String DESCRIPTION_WHENEVER = "Cannot use return in a whenever block";
        public static final String DESCRIPTION_OUTSIDE = "Return outside of routine";
    }

    public static class RakuExecutableString {
        public static final String DESCRIPTION = "If the Raku executable is meant, consider using the $*EXECUTABLE.absolute() call that supports many platforms (e.g. GNU/Linux, Windows, etc.)";
        public static final Set<String> ALIASES = Set.of("raku", "rakudo", "perl6");
    }

    public static class Signature {
        public static final String DESCRIPTION_NO_OPTIONAL_AFTER_NAMED =
            "Cannot put an optional parameter %s after a named parameter";
        public static final String DESCRIPTION_NAMED_ARE_OPTIONAL =
            "Explicit `?` on a named parameter %s is redundant, as all nameds are optional by default";
        public static final String DESCRIPTION_CANNOT_REQUIRE_DEFAULT =
            "Parameter %s has a default value and so cannot be required";
        public static final String DESCRIPTION_POS_ALREADY_REQUIRED =
            "Explicit `!` on a positional parameter %s is redundant, as all positional parameters are required by default";
        public static final String DESCRIPTION_DEFAULTS_ARE_OPTIONAL =
            "Explicit `?` on a parameter %s with default is redundant, as all parameters with default value are optional by default";
    }

    public static class SimplifiedRange {
        public static final Set<String> OPS = Set.of("..", "..^");
        public static final String DESCRIPTION = "Range can be simplified";
    }

    public static class UndeclaredPrivateMethod {
        public static final String DESCRIPTION_CANNOT_START_WITH_BANG = "Subroutine cannot start with '!'";
        public static final String DESCRIPTION_FORMAT = "Private method %s is used, but not declared";
    }

    public static class UsedModuleInspection {
        public static final String DESCRIPTION_META6_FORMAT = "Cannot find %s which is specified as a dependency in META6.json";
        public static final String DESCRIPTION_IN_META6_BUT_MISSING_FORMAT = "Module %s is in META6.json dependencies but is not installed";
        public static final String DESCRIPTION_MODULE_NOT_INSTALLED = "Module %s is is not installed for current Raku";
        public static final String DESCRIPTION_MISSING_FROM_ECO_FORMAT = "Cannot find %s in the ecosystem";
    }

    public static class UndeclaredVariable {
        public static final String DESCRIPTION_NOT_DECLARED_FORMAT = "Variable %s is not declared";
        public static final String DESCRIPTION_NOT_DECLARED_SCOPE_FORMAT = "Variable %s is not declared in this scope yet";
        public static final String DESCRIPTION_DOC_FINISH = "There is no =finish section in this file";
        public static final String DESCRIPTION_IMPLICIT_WITH_SIGNATURE_FORMAT = "Implicit variable '%s' used in combination with a signature that does not declare it as a parameter";

        public static final Set<String> ANONYMOUS_VARIABLES = Set.of("$", "@", "%", "&");
        public static final Set<String> IMPLICIT_VARIABLES = Set.of("$_", "@_", "%_");
    }

    public static class UnitSub {
        public static final String DESCRIPTION = "The unit sub syntax is only allowed for the sub MAIN";
    }

    public static class UnknownRegexMod {
        public static final String DESCRIPTION_FORMAT = "Unrecognized regex modifier '%s'";
        public static final String DESCRIPTION_PLURAL_FORMAT = "Unrecognized regex modifiers %s";
    }

    public static class UnusedRoutine {
         public static final Set<String> AUTOCALLED = ContainerUtil.newHashSet("MAIN", "USAGE", "EXPORT");
    }

    public static class UselessMethodDeclaration {
        public static final String DESCRIPTION_PACKAGE = "Useless declaration of a method outside of any package";
        public static final String DESCRIPTION_FORMAT = "Useless declaration of a method in a %s";
    }

    public static class UselessUse {
        public static final String DESCRIPTION = "Useless use of value in sink (void) context";
    }

    public static class WheneverOutsideOfReact {
        public static final String DESCRIPTION = "A whenever must be within a supply or react block";
    }

    public static class WithConstruction {
        public static final String DESCRIPTION_WITH = "'with' construction can be used instead";
        public static final String DESCRIPTION_WITHOUT = "'without' construction can be used instead";
        public final static Set<String> TERMS = Set.of("if", "elsif", "unless");
    }
}
