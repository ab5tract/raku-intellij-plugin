package org.raku.comma.highlighter;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.extensions.InternalIgnoreDependencyViolation;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.raku.comma.RakuIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

@InternalIgnoreDependencyViolation
public class RakuColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Bad syntax", RakuHighlighter.BAD_CHARACTER),
            new AttributesDescriptor("Type name", RakuHighlighter.TYPE_NAME),
            new AttributesDescriptor("Statement terminator", RakuHighlighter.STATEMENT_TERMINATOR),
            new AttributesDescriptor("Statement control", RakuHighlighter.STATEMENT_CONTROL),
            new AttributesDescriptor("Phaser", RakuHighlighter.PHASER),
            new AttributesDescriptor("Label name", RakuHighlighter.LABEL_NAME),
            new AttributesDescriptor("Label colon", RakuHighlighter.LABEL_COLON),
            new AttributesDescriptor("Statement prefix", RakuHighlighter.STATEMENT_PREFIX),
            new AttributesDescriptor("Statement modifier", RakuHighlighter.STATEMENT_MOD),
            new AttributesDescriptor("Scope keyword", RakuHighlighter.SCOPE_DECLARATOR),
            new AttributesDescriptor("Multi keyword", RakuHighlighter.MULTI_DECLARATOR),
            new AttributesDescriptor("Routine keyword", RakuHighlighter.ROUTINE_DECLARATOR),
            new AttributesDescriptor("Package keyword", RakuHighlighter.PACKAGE_DECLARATOR),
            new AttributesDescriptor("Routine name", RakuHighlighter.ROUTINE_NAME),
            new AttributesDescriptor("Prefix operator", RakuHighlighter.PREFIX),
            new AttributesDescriptor("Infix operator", RakuHighlighter.INFIX),
            new AttributesDescriptor("Postfix operator", RakuHighlighter.POSTFIX),
            new AttributesDescriptor("Meta-operator", RakuHighlighter.METAOP),
            new AttributesDescriptor("Array indexer", RakuHighlighter.ARRAY_INDEXER),
            new AttributesDescriptor("Hash indexer", RakuHighlighter.HASH_INDEXER),
            new AttributesDescriptor("Lambda (-> and <->)", RakuHighlighter.LAMBDA),
            new AttributesDescriptor("Variable", RakuHighlighter.VARIABLE),
            new AttributesDescriptor("Contextualizer", RakuHighlighter.CONTEXTUALIZER),
            new AttributesDescriptor("Variable shape declaration", RakuHighlighter.SHAPE_DECLARATION),
            new AttributesDescriptor("Type Declarator (enum, subset, constant)", RakuHighlighter.TYPE_DECLARATOR),
            new AttributesDescriptor("Term Declaration Backslash (my \\answer = 42)", RakuHighlighter.TERM_DECLARATION_BACKSLASH),
            new AttributesDescriptor("Numeric literal", RakuHighlighter.NUMERIC_LITERAL),
            new AttributesDescriptor("Version literal", RakuHighlighter.VERSION),
            new AttributesDescriptor("Comment", RakuHighlighter.COMMENT),
            new AttributesDescriptor("Sub call name", RakuHighlighter.SUB_CALL_NAME),
            new AttributesDescriptor("Method call name", RakuHighlighter.METHOD_CALL_NAME),
            new AttributesDescriptor("Parentheses", RakuHighlighter.PARENTHESES),
            new AttributesDescriptor("Block curly braces", RakuHighlighter.BLOCK_CURLY_BRACKETS),
            new AttributesDescriptor("Whatever", RakuHighlighter.WHATEVER),
            new AttributesDescriptor("Stub Code (..., ???, !!!)", RakuHighlighter.STUB_CODE),
            new AttributesDescriptor("Argument Capture (\\$foo, \\($a, $b))", RakuHighlighter.CAPTURE_TERM),
            new AttributesDescriptor("Other terms (including user defined)", RakuHighlighter.TERM),
            new AttributesDescriptor("Current Object (self, sigil in $.foo(...))", RakuHighlighter.SELF),
            new AttributesDescriptor("Only Star (Protos)", RakuHighlighter.ONLY_STAR),
            new AttributesDescriptor("Parameter separator", RakuHighlighter.PARAMETER_SEPARATOR),
            new AttributesDescriptor("Named parameter colon and parentheses", RakuHighlighter.NAMED_PARAMETER_SYNTAX),
            new AttributesDescriptor("Named parameter name alias", RakuHighlighter.NAMED_PARAMETER_NAME_ALIAS),
            new AttributesDescriptor("Parameter quantifier (slurpy, optional, required)", RakuHighlighter.PARAMETER_QUANTIFIER),
            new AttributesDescriptor("Parameter or variable constraint (where)", RakuHighlighter.WHERE_CONSTRAINT),
            new AttributesDescriptor("Return type arrow (-->)", RakuHighlighter.RETURN_ARROW),
            new AttributesDescriptor("String literal quote", RakuHighlighter.STRING_LITERAL_QUOTE),
            new AttributesDescriptor("String literal value", RakuHighlighter.STRING_LITERAL_CHAR),
            new AttributesDescriptor("String literal escape", RakuHighlighter.STRING_LITERAL_ESCAPE),
            new AttributesDescriptor("String literal invalid escape", RakuHighlighter.STRING_LITERAL_BAD_ESCAPE),
            new AttributesDescriptor("Regex literal quote", RakuHighlighter.QUOTE_REGEX),
            new AttributesDescriptor("Quote Pair (on string and regex literals)", RakuHighlighter.QUOTE_PAIR),
            new AttributesDescriptor("Quote modifier", RakuHighlighter.QUOTE_MOD),
            new AttributesDescriptor("Array Composer ([...])", RakuHighlighter.ARRAY_COMPOSER),
            new AttributesDescriptor("Hash Composer ({...})", RakuHighlighter.ARRAY_COMPOSER),
            new AttributesDescriptor("Pair (colon pair or key before =>)", RakuHighlighter.PAIR_KEY),
            new AttributesDescriptor("Trait keyword", RakuHighlighter.TRAIT),
            new AttributesDescriptor("Type parameter brackets", RakuHighlighter.TYPE_PARAMETER_BRACKET),
            new AttributesDescriptor("Type coercion parentheses", RakuHighlighter.TYPE_COERCION_PARENTHESES),
            new AttributesDescriptor("Regex infix (alternation, conjunction, goal)", RakuHighlighter.REGEX_INFIX),
            new AttributesDescriptor("Regex anchor", RakuHighlighter.REGEX_ANCHOR),
            new AttributesDescriptor("Regex group (square brackets)", RakuHighlighter.REGEX_GROUP_BRACKET),
            new AttributesDescriptor("Regex capture", RakuHighlighter.REGEX_CAPTURE),
            new AttributesDescriptor("Regex quantifier", RakuHighlighter.REGEX_QUANTIFIER),
            new AttributesDescriptor("Regex built-in character class", RakuHighlighter.REGEX_BUILTIN_CCLASS),
            new AttributesDescriptor("Regex invalid backslash sequence", RakuHighlighter.REGEX_BACKSLASH_BAD),
            new AttributesDescriptor("Regex assertion angle brackets", RakuHighlighter.REGEX_ASSERTION_ANGLE),
            new AttributesDescriptor("Regex Lookaround (? and !)", RakuHighlighter.REGEX_LOOKAROUND),
            new AttributesDescriptor("Regex character class syntax", RakuHighlighter.REGEX_CCLASS_SYNTAX),
            new AttributesDescriptor("Regex modifier", RakuHighlighter.REGEX_MOD),
            new AttributesDescriptor("Rule Sigspace (implicit <.ws> call)", RakuHighlighter.REGEX_SIG_SPACE),
            new AttributesDescriptor("Transliteration literal character", RakuHighlighter.TRANS_CHAR),
            new AttributesDescriptor("Transliteration escape", RakuHighlighter.TRANS_ESCAPE),
            new AttributesDescriptor("Transliteration range operator", RakuHighlighter.TRANS_RANGE),
            new AttributesDescriptor("Transliteration invalid syntax", RakuHighlighter.TRANS_BAD),
            new AttributesDescriptor("Pod directive", RakuHighlighter.POD_DIRECTIVE),
            new AttributesDescriptor("Pod typename", RakuHighlighter.POD_TYPENAME),
            new AttributesDescriptor("Pod configuration", RakuHighlighter.POD_CONFIGURATION),
            new AttributesDescriptor("Pod text", RakuHighlighter.POD_TEXT),
            new AttributesDescriptor("Pod Text (Bold)", RakuHighlighter.POD_TEXT_BOLD),
            new AttributesDescriptor("Pod Text (Italic)", RakuHighlighter.POD_TEXT_ITALIC),
            new AttributesDescriptor("Pod Text (Underlined)", RakuHighlighter.POD_TEXT_UNDERLINE),
            new AttributesDescriptor("Pod code block", RakuHighlighter.POD_CODE),
            new AttributesDescriptor("Pod format code", RakuHighlighter.POD_FORMAT_CODE),
            new AttributesDescriptor("Pod format delimiters", RakuHighlighter.POD_FORMAT_QUOTES),
            new AttributesDescriptor("Quasi quote", RakuHighlighter.QUASI),
            new AttributesDescriptor("Built-in variable", RakuHighlighter.BUILTIN_VARIABLE),
            new AttributesDescriptor("Built-in call", RakuHighlighter.BUILTIN_CALL),
            new AttributesDescriptor("Reassigned local variable", RakuHighlighter.REASSIGNED_LOCAL_VARIABLE),
            new AttributesDescriptor("Reassigned parameter", RakuHighlighter.REASSIGNED_PARAMETER),
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return RakuIcons.CAMELIA;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new RakuSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return """
            use v6;
            use JSON::Tiny;

            role R[:param] {}

            grammar IPv4 is default {
                token TOP { <seg> ** 4 % '.' }
                token seg {
                    \\d+ { 0 <= $/ <= 255 }
                }

                proto method dummy() {*}
                multi method dummy(Cool(Int) $coerced) {!!!}
                multi method dummy($pos? where 1 ; :alias($named) --> Nil) {
                    constant \\foo = key => 'value';
                    LABEL: [+] self.dummy(:$named);
                }
            }

            # Find all IPv4 data sources and show them.
            my @data = from-json(slurp 'input.json');
            for @data.map(*<from>) -> $from {
                if IPv4.parse($from) {
                    do say "Address: $from";
                }
            }

            BEGIN {
                my $capture = \\("capture\\n", 42);
                quasi { ++$capture[42]++; }
                my $array[1;1] = ['composed'][0];
                say @$array, $$array; # <- contextualizers work...
               \s
            }

            # Regex fun begins!
            'foo' ~~ m:g!^ [(f) <[o]> $<foo>=[]] || 'constant' \\invalid !;

            # Semantic highlighting: built-ins and reassignment.
            sub semantics(<reassignedParameter>$p</reassignedParameter> is rw) {
                <reassignedParameter>$p</reassignedParameter> = 42;
                my $total = 0;
                <reassignedVariable>$total</reassignedVariable> += 1;
                <builtinCall>say</builtinCall>(<builtinVariable>$*OUT</builtinVariable>);
            }
            \\""";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return Map.of(
                "builtinVariable", RakuHighlighter.BUILTIN_VARIABLE,
                "builtinCall", RakuHighlighter.BUILTIN_CALL,
                "reassignedVariable", RakuHighlighter.REASSIGNED_LOCAL_VARIABLE,
                "reassignedParameter", RakuHighlighter.REASSIGNED_PARAMETER
        );
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Raku";
    }
}
