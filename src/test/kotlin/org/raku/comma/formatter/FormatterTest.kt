package org.raku.comma.formatter

import com.intellij.application.options.CodeStyle
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.RakuLanguage
import org.raku.comma.filetypes.RakuScriptFileType

import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

private typealias StyleConfig = (CommonCodeStyleSettings, RakuCodeStyleSettings) -> Unit

class FormatterTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/formatter"
    }

    fun testIndentation() {
        // Normal indent cases
        reformatTest("my <caret>\$a;", "my <caret>\$a;")
        reformatTest("if True {\nsay 42;\n}", "if True {\n    say 42;\n}")
        reformatTest("if True {\nsay 42;\n}", "if True {\n     say 42;\n}",
                     { s1, s2 -> s1.indentOptions!!.INDENT_SIZE = 5 })
        // Continuation indent cases
        reformatTest("my \$a =\n42;", "my \$a =\n        42;")
        reformatTest("my \$a = 42\n.bar().baz();", "my \$a = 42\n        .bar().baz();")
        reformatTest("push\n@foo, 42;", "push\n        @foo, 42;")
        reformatTest("@foo.push:\n42;", "@foo.push:\n        42;")
        reformatTest("basic-indent")
        reformatTest("basic-class")
        reformatTest("class ABC\n  is export {}", "class ABC\n        is export {}")
        reformatTest("unit module A;\nrole Foo {}", "unit module A;\nrole Foo {}")
        reformatTest("42\n?? 100\n!! 88;", "42\n        ?? 100\n        !! 88;")
        reformatTest("constant @a = [\n    'Doctor',\n    'Master',\n];", "constant @a = [\n    'Doctor',\n    'Master',\n];")
    }

    fun testSpacing() {
        // Comma operator
        reformatTest("1,2", "1, 2")
        // Comma operator, dangling
        reformatTest("1,2,", "1, 2,")
        // Fat arrow
        reformatTest("a=>42", "a => 42")
        // Assignment and binding
        reformatTest("\$a= 42; \$b=51;", "\$a = 42;\n\$b = 51;")
        // Non-word infix operators
        reformatTest("42+632    + 23 +1+ 53;", "42 + 632 + 23 + 1 + 53;")
        // Infix in whatever star
        reformatTest("say *   +   3", "say *+ 3")
        // Non-word prefix operators
        reformatTest("!  42 eq !55", "!42 eq !55")
        // Stubs
        reformatTest("sub a {...}; proto b() {*}", "sub a {...};\nproto b() {*}")
        // Lambda
        reformatTest("->\$a, \$b {}; ->    \$a {}", "-> \$a, \$b {};\n-> \$a {}")
        // Regex infix; TODO & and &&
        reformatTest("/a|d||e/", "/a | d || e/")
        // Regex quantifier
        reformatTest("/4 * 3  ?/", "/4* 3?/")
        // Regex separator
        reformatTest("/ ^ [\\w+] ** 1%',' \$ /", "/ ^ [\\w+] ** 1 % ',' \$ /")
        // Spacing withing different types of parentheses and brackets
        reformatTest("foo( 42 )", "foo(42)")
        reformatTest("(   1 + 3 ) * 4", "(1 + 3) * 4")
        reformatTest("[ 42, 53   ]", "[42, 53]")
        reformatTest("/( a || b ) | cbd/", "/(a || b) | cbd/")
        reformatTest("/(    \\d + )/", "/(\\d+)/")
        reformatTest("/ [<var=.foo>] /", "/ [<var=.foo>] /")
        reformatTest("token foo { \\d \\w }", "token foo {\n    \\d \\w\n}")
        reformatTest("42 += 15", "42 += 15")
        reformatTest("foobarcall(\na => 42,\nb => 50);", "foobarcall(\n        a => 42,\n        b => 50);")
        reformatTest("token foo { <iteration-v=.p(:a)> }", "token foo { <iteration-v=.p(:a)> }")
        reformatTest("first(* !~~ 'rows')", "first(* !~~ 'rows')")
        reformatTest("/<?{ \$*lone-start-line }>/", "/<?{ \$*lone-start-line }>/")
        reformatTest("/<{'abc'}>/", "/<{ 'abc' }>/")
        reformatTest("/<!{'abc'}>/", "/<!{ 'abc' }>/")
        reformatTest("42 if not .IO.d", "42 if not .IO.d")
        reformatTest("“{\$CONFIG<mothership>}/\$full-commit-hash?type=\$backend&arch=\$arch”",
                     "“{ \$CONFIG<mothership> }/\$full-commit-hash?type=\$backend&arch=\$arch”")
        reformatTest(
            "“{\$t.our-nick}, I cannot recognize this command. See wiki for some examples: https://github.com/perl6/whateverable/wiki/Committable”",
            "“{ \$t.our-nick }, I cannot recognize this command. See wiki for some examples: https://github.com/perl6/whateverable/wiki/Committable”")
        reformatTest("my @a[1;2];",
                     "my @a[1;2];")
        reformatTest("%a ,= %b;", "%a ,= %b;")
    }

    fun testAlignment() {
        // Expressions
        reformatTest("say 1 +\n4;", "say 1 +\n        4;")
        reformatTest("foobaz 1 +\n4;", "foobaz 1 +\n        4;")
        // Array, hash literals
        reformatTest("my @ab = 12,2333,3,4,\n5,6,7;", "my @ab = 12, 2333, 3, 4,\n         5, 6, 7;")
        // TODO when we have a better processing of this literal
        // reformatTest("my @ab = <12 2333 3 4\n5 6 7>;", "my @ab = <12 2333 3 4\n         5 6 7>;")
        reformatTest("my %long-hash = a => 42,\nb => 50;", "my %long-hash = a => 42,\n                b => 50;")
        reformatTest("basic-hash")
        // traits
        reformatTest("traits")
        // parameters, calls
        reformatTest("sub foobar(\$first-one,\n\$second-one) {}", "sub foobar(\$first-one,\n           \$second-one) {}")
        reformatTest("push 42,\n42", "push 42,\n        42")
        // call chains
        reformatTest("Foo\n.method(42)\n.method2(24)", "Foo\n        .method(42)\n        .method2(24)")
    }

    fun testWrapping() {
        // literals
        reformatTest("my @abc = 1,2,3,4,5,6;", "my @abc = 1, 2,\n          3, 4,\n          5, 6;",
                     { s1, s2 -> s1.RIGHT_MARGIN = 16 })
        // call chains
        reformatTest("TypeFoo.a-very-long-method-call;", "TypeFoo\n        .a-very-long-method-call;",
                     { s1, s2 -> s1.RIGHT_MARGIN = 14 })
        // parameters
        reformatTest("sub abcd(\$aaaaaa, \$bbbbbb) {}", "sub abcd(\n        \$aaaaaa,\n        \$bbbbbb) {}",
                     { s1, s2 -> s1.RIGHT_MARGIN = 14 })
        // args
        reformatTest("say 424242424242424242, True", "say\n        424242424242424242,\n        True",
                     { s1, s2 -> s1.RIGHT_MARGIN = 14 })
        // traits
        reformatTest("class Name is trait1 is trait2 is trait3 {}",
                     "class Name\n        is trait1\n        is trait2\n        is trait3 {}",
                     { s1, s2 -> s1.RIGHT_MARGIN = 14 })
    }

    fun testEnterIndentation() {
        enterTest("", "\n")
        enterTest("if True {<caret>\n}", "if True {\n    <caret>\n}")
        enterTest("if True {\n    say 42;<caret>\n}", "if True {\n    say 42;\n    <caret>\n}")
        enterTest("say 1 +<caret>", "say 1 +\n        <caret>")
        enterTest("sub abcd(\$abc,<caret>)", "sub abcd(\$abc,\n         <caret>)")
        enterTest("abcd(42,<caret>)", "abcd(42,\n     <caret>)")
        enterTest("my @a = [42, 42,<caret>]", "my @a = [42, 42,\n<caret>]")
        enterTest("my @ab = 42, 42,<caret>", "my @ab = 42, 42,\n         <caret>")
        enterTest("class AAAA is export<caret>", "class AAAA is export\n           <caret>")
        // With block indent
        enterTest("if True {\n    say 42,<caret>\n    say 42;\n}", "if True {\n    say 42,\n            <caret>\n    say 42;\n}")
        enterTest("if True {\n    say 42,<caret>\n}", "if True {\n    say 42,\n        <caret>\n}")
        enterTest("Int.foo:<caret>", "Int.foo:\n        <caret>")
        enterTest("has \$.a is rw<caret>;", "has \$.a is rw\n        ;")
        enterTest("constant @a = [\n    'Doctor',<caret>\n    'Master',\n];",
                  "constant @a = [\n    'Doctor',\n    <caret>\n    'Master',\n];")
    }

    fun testIntegrationCases() {
        reformatTest("basic")
        reformatTest("assorted")
        reformatTest("grammar-basic")
        reformatTest("break-lines")
        reformatTest("blocks", { s1, s2 -> s1.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE = false })
        reformatTest("class A {};", "class A {\n\n};", { s1, s2 -> s2.PACKAGE_DECLARATION_IN_ONE_LINE = false })
        reformatTest("space-before-semi")
        reformatTest("hash")
        reformatTest("hash-multiline-values")
        reformatTest("array", { s1, s2 -> s1.indentOptions!!.CONTINUATION_INDENT_SIZE = 4 })
        reformatTest("trailing-comma")
        reformatTest("comments-left-intact")
        reformatTest("contextualizers")
        reformatTest("heredoc")
        reformatTest("comment")
        reformatTest("escapes")
        reformatTest("call-staircase")
        reformatTest("heredoc-with-indent")
    }

    fun testSettingsApplication() {
        // Indentation tab
        reformatTest("indentation")
        reformatTest("indentation-reverse", { common, custom ->
            val indentOptions = common.indentOptions!!
            indentOptions.INDENT_SIZE = 3
            indentOptions.CONTINUATION_INDENT_SIZE = 4
        })
        reformatTest("indentation-tab", { common, custom ->
            val indentOptions = common.indentOptions!!
            indentOptions.TAB_SIZE = 5
            indentOptions.USE_TAB_CHARACTER = true
        })
        // Braces and Wrapping tab
        reformatTest("braces")
        reformatTest("braces-reverse", { common, custom ->
            common.KEEP_LINE_BREAKS = !common.KEEP_LINE_BREAKS
            common.KEEP_MULTIPLE_EXPRESSIONS_IN_ONE_LINE = !common.KEEP_MULTIPLE_EXPRESSIONS_IN_ONE_LINE
            common.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE = !common.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE
            custom.ROUTINES_DECLARATION_IN_ONE_LINE = !custom.ROUTINES_DECLARATION_IN_ONE_LINE
            custom.REGEX_DECLARATION_IN_ONE_LINE = !custom.REGEX_DECLARATION_IN_ONE_LINE
            custom.PACKAGE_DECLARATION_IN_ONE_LINE = !custom.PACKAGE_DECLARATION_IN_ONE_LINE
            custom.POINTY_BLOCK_IN_ONE_LINE = !custom.POINTY_BLOCK_IN_ONE_LINE
        })
        reformatTest("braces-style", { common, custom ->
            custom.PACKAGE_DECL_BRACE_STYLE = 2
            custom.ROUTINE_DECL_BRACE_STYLE = 2
            custom.REGEX_DECL_BRACE_STYLE = 2
            custom.PHASER_BRACE_STYLE = 2
            custom.OTHER_BRACE_STYLE = 2
        })
        reformatTest("spacing")
        reformatTest("spacing-reverse", { common, custom ->
            custom.BEFORE_COMMA = !custom.BEFORE_COMMA
            custom.AFTER_COMMA = !custom.AFTER_COMMA
            custom.BEFORE_FATARROW = !custom.BEFORE_FATARROW
            custom.AFTER_FATARROW = !custom.AFTER_FATARROW
            custom.BEFORE_ASSIGNMENT = !custom.BEFORE_ASSIGNMENT
            custom.AFTER_ASSIGNMENT = !custom.AFTER_ASSIGNMENT
            custom.BEFORE_INFIX = !custom.BEFORE_INFIX
            custom.AFTER_INFIX = !custom.AFTER_INFIX
            custom.BEFORE_WHATEVER_STAR = !custom.BEFORE_WHATEVER_STAR
            custom.AFTER_WHATEVER_STAR = !custom.AFTER_WHATEVER_STAR
            custom.AFTER_PREFIX_OPS = !custom.AFTER_PREFIX_OPS
            custom.AFTER_LAMBDA = !custom.AFTER_LAMBDA
            custom.BEFORE_REGEX_INFIX_SPACING = !custom.BEFORE_REGEX_INFIX_SPACING
            custom.AFTER_REGEX_INFIX_SPACING = !custom.AFTER_REGEX_INFIX_SPACING
            custom.BEFORE_REGEX_QUANTIFIER_SPACING = !custom.BEFORE_REGEX_QUANTIFIER_SPACING
            custom.AFTER_REGEX_QUANTIFIER_SPACING = !custom.AFTER_REGEX_QUANTIFIER_SPACING
            custom.BEFORE_REGEX_SEPARATOR_SPACING = !custom.BEFORE_REGEX_SEPARATOR_SPACING
            custom.AFTER_REGEX_SEPARATOR_SPACING = !custom.AFTER_REGEX_SEPARATOR_SPACING
            custom.CALL_PARENS_SPACING = !custom.CALL_PARENS_SPACING
            custom.GROUPING_PARENS_SPACING = !custom.GROUPING_PARENS_SPACING
            custom.ARRAY_LITERAL_PARENS_SPACING = !custom.ARRAY_LITERAL_PARENS_SPACING
            custom.REGEX_GROUP_PARENS_SPACING = !custom.REGEX_GROUP_PARENS_SPACING
            custom.REGEX_POSITIONAL_PARENS_SPACING = !custom.REGEX_POSITIONAL_PARENS_SPACING
        })
        // Alignment
        reformatTest("align", { common, custom -> common.RIGHT_MARGIN = 20 })
        reformatTest("align-wrap", { common, custom ->
            common.RIGHT_MARGIN = 20
            // Wrap
            custom.PARAMETER_WRAP = !custom.PARAMETER_WRAP
            custom.TRAIT_WRAP = !custom.TRAIT_WRAP
            custom.CALL_ARGUMENTS_WRAP = !custom.CALL_ARGUMENTS_WRAP
            custom.ARRAY_ELEMENTS_WRAP = !custom.ARRAY_ELEMENTS_WRAP
            custom.METHOD_CALL_WRAP = !custom.METHOD_CALL_WRAP
            custom.INFIX_APPLICATION_WRAP = !custom.INFIX_APPLICATION_WRAP
            // Align
            custom.PARAMETER_ALIGNMENT = !custom.PARAMETER_ALIGNMENT
            custom.TRAIT_ALIGNMENT = !custom.TRAIT_ALIGNMENT
            custom.CALL_ARGUMENTS_ALIGNMENT = !custom.CALL_ARGUMENTS_ALIGNMENT
            custom.ARRAY_ELEMENTS_ALIGNMENT = !custom.ARRAY_ELEMENTS_ALIGNMENT
        })
        reformatTest("align-reverse", { common, custom ->
            common.RIGHT_MARGIN = 26
            // Align
            custom.PARAMETER_ALIGNMENT = !custom.PARAMETER_ALIGNMENT
            custom.TRAIT_ALIGNMENT = !custom.TRAIT_ALIGNMENT
            custom.CALL_ARGUMENTS_ALIGNMENT = !custom.CALL_ARGUMENTS_ALIGNMENT
            custom.ARRAY_ELEMENTS_ALIGNMENT = !custom.ARRAY_ELEMENTS_ALIGNMENT
        })
    }

    /* -- HELPERS -- */

    /**
     * These methods are used to supplement testing of implicit reformatting (guided by an enter pressing)
     */
    private fun enterTest(input: String, output: String) {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, input)
        executeEnter()
        myFixture.checkResult(output)
    }

    private fun executeEnter() {
        CommandProcessor.getInstance().executeCommand(project, {
            val actionManager = EditorActionManager.getInstance()
            val enterHandler = actionManager.getActionHandler(IdeActions.ACTION_EDITOR_ENTER)
            try {
                enterHandler.execute(
                    myFixture.editor,
                    null,
                    DataManager.getInstance().dataContextFromFocusAsync.blockingGet(5, TimeUnit.SECONDS)
                )
            } catch (ignored: TimeoutException) {
            } catch (ignored: ExecutionException) {
            }
        }, "", null)
    }

    /**
     * These methods are used to supplement testing of explicit reformatting (guided by an action).
     */
    private fun reformatTest(input: String, output: String, config: StyleConfig = { _, _ -> }) {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, input)
        reformat(config)
        try {
            myFixture.checkResult(output)
        } catch (error: Throwable) {
            println("REFORMAT-FAIL input=<<<${input}>>> expected=<<<${output}>>> actual=<<<${myFixture.editor.document.text}>>>")
            throw error
        }
    }

    private fun reformatTest(filename: String, config: StyleConfig = { _, _ -> }) {
        myFixture.configureByFiles("$filename.in.p6")
        reformat(config)
        checkResultByFileDumpable("$filename.out.p6")
    }

    private fun reformat(config: StyleConfig) {
        WriteCommandAction.runWriteCommandAction(null) {
            val settingsManager = CodeStyleSettingsManager.getInstance(myFixture.project)
            val temp = CodeStyle.createTestSettings(CodeStyle.getSettings(myFixture.project))
            val common = temp.getCommonSettings(RakuLanguage.INSTANCE)
            println("DIAG margin=${common.RIGHT_MARGIN} effective=${temp.getRightMargin(RakuLanguage.INSTANCE)} " +
                    "indent=${common.indentOptions?.INDENT_SIZE} cont=${common.indentOptions?.CONTINUATION_INDENT_SIZE} " +
                    "keepLineBreaks=${common.KEEP_LINE_BREAKS}")
            config(
                temp.getCommonSettings(RakuLanguage.INSTANCE),
                temp.getCustomSettings(RakuCodeStyleSettings::class.java)
            )
            settingsManager.setTemporarySettings(temp)
            try {
                CodeStyleManager.getInstance(myFixture.project).reformat(myFixture.file)
            } finally {
                settingsManager.dropTemporarySettings()
            }
        }
    }
}
