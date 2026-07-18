package org.raku.comma.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.extensions.InternalIgnoreDependencyViolation
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.formatter.DocumentBasedFormattingModel
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.RakuLanguage
import org.raku.comma.parsing.RakuElementTypes
import org.raku.comma.parsing.RakuTokenTypes
import org.raku.comma.psi.*
import java.util.function.BiFunction

@InternalIgnoreDependencyViolation
class RakuFormattingModelBuilder : FormattingModelBuilder {
    var CONSTANT_EMPTY_SPACING: Spacing? = null
    var EMPTY_SPACING: Spacing? = null
    var SINGLE_SPACE_SPACING: Spacing? = null
    var SINGLE_LINE_BREAK: Spacing? = null
    var DOUBLE_LINE_BREAK: Spacing? = null

    private val STATEMENTS = TokenSet.create(
        RakuElementTypes.STATEMENT,
        RakuElementTypes.COMMENT,
        RakuElementTypes.HEREDOC
    )
    private val OPENERS = TokenSet.create(
        RakuTokenTypes.ARRAY_COMPOSER_OPEN,
        RakuTokenTypes.ARRAY_INDEX_BRACKET_OPEN,
        RakuTokenTypes.HASH_INDEX_BRACKET_OPEN,
        RakuTokenTypes.SIGNATURE_BRACKET_OPEN
    )
    private val CLOSERS = TokenSet.create(
        RakuTokenTypes.ARRAY_COMPOSER_CLOSE,
        RakuTokenTypes.ARRAY_INDEX_BRACKET_CLOSE,
        RakuTokenTypes.HASH_INDEX_BRACKET_CLOSE,
        RakuTokenTypes.SIGNATURE_BRACKET_CLOSE
    )

    private fun isDanglingComma(node: ASTNode?): Boolean =
        node != null && node.elementType === RakuElementTypes.NULL_TERM

    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val psiFile = formattingContext.containingFile
        val rules: MutableMap<RakuSpacingRule, BiFunction<RakuBlock?, RakuBlock?, Spacing?>> = mutableMapOf()
        val settings = formattingContext.codeStyleSettings
        val commonSettings = settings.getCommonSettings(RakuLanguage.INSTANCE)
        val customSettings = settings.getCustomSettings<RakuCodeStyleSettings>(RakuCodeStyleSettings::class.java)
        initRules(rules, commonSettings, customSettings)
        val block = RakuBlock(psiFile.node, null, null, commonSettings, customSettings, rules)
        // Raku uses custom whitespace tokens (UNV/VERTICAL/UNSP), which the
        // PSI-based formatting model mangles; apply changes at document level.
        return DocumentBasedFormattingModel(block, formattingContext.project, settings, psiFile.fileType, psiFile)
    }

    private fun initRules(
        rules: MutableMap<RakuSpacingRule, BiFunction<RakuBlock?, RakuBlock?, Spacing?>>,
        commonSettings: CommonCodeStyleSettings,
        customSettings: RakuCodeStyleSettings
    ) {
        // Prepare fast constants for common cases
        EMPTY_SPACING = Spacing.createSpacing(0,
                                              0,
                                              0,
                                              commonSettings.KEEP_LINE_BREAKS,
                                              if (commonSettings.KEEP_LINE_BREAKS) 3 else 1)
        SINGLE_SPACE_SPACING = Spacing.createSpacing(1,
                                                     1,
                                                     0,
                                                     commonSettings.KEEP_LINE_BREAKS,
                                                     if (commonSettings.KEEP_LINE_BREAKS) 3 else 1)
        SINGLE_LINE_BREAK = Spacing.createSpacing(0,
                                                  0,
                                                  1,
                                                  commonSettings.KEEP_LINE_BREAKS,
                                                  if (commonSettings.KEEP_LINE_BREAKS) 3 else 1)
        DOUBLE_LINE_BREAK = Spacing.createSpacing(0,
                                                  0,
                                                  2,
                                                  commonSettings.KEEP_LINE_BREAKS,
                                                  if (commonSettings.KEEP_LINE_BREAKS) 3 else 1)
        CONSTANT_EMPTY_SPACING = Spacing.createSpacing(0, 0, 0, false, 0)

        // Init spacing rule sets
        initLineBreakRules(commonSettings,
                           customSettings,
                           rules as MutableMap<RakuSpacingRule, BiFunction<RakuBlock?, RakuBlock?, Spacing?>?>)
        initSpacingRules(commonSettings, customSettings, rules)
    }

    // Rule evaluation is first-match-wins in enum order: the line-break
    // (brace style) rules must run before the spacing rules, mirroring the
    // registration order of the original implementation.
    enum class RakuSpacingRule {
        BraceStylePackage,
        BraceStyleRoutine,
        BraceStyleRegex,
        BraceStylePhasersEtc,
        BraceStyleEtc,
        BraceStyleEtcSemiList,
        StatementTerminator,
        UnterminatedStatement,
        BracesParentheses,
        BracesArray,
        RegexGroup,
        RegexCapture,
        NothingInsideBraces,
        OperatorCommaSpaceAfter,
        OperatorCommaNothingBefore,
        FatArrow,
        NoExcessiveSpaceTrait,
        OperatorPrefix,
        OperatorInfix,
        LambdaRules,
        RegexInfix,
        RegexSeparator,
        RegexQuantifier
    }

    private fun initSpacingRules(
        commonSettings: CommonCodeStyleSettings?,
        customSettings: RakuCodeStyleSettings,
        rules: MutableMap<RakuSpacingRule, BiFunction<RakuBlock?, RakuBlock?, Spacing?>>
    ) {

        // Nothing between statement and its ;
        rules.put(RakuSpacingRule.StatementTerminator, BiFunction { left: RakuBlock?, right: RakuBlock? ->
            if (right!!.node.elementType === RakuTokenTypes.STATEMENT_TERMINATOR)
                 CONSTANT_EMPTY_SPACING
            else null
        })
        // Nothing between statement and its absence of ;
        rules.put(RakuSpacingRule.UnterminatedStatement, BiFunction { left: RakuBlock?, right: RakuBlock? ->
            if (right!!.node.elementType === RakuElementTypes.UNTERMINATED_STATEMENT)
                 CONSTANT_EMPTY_SPACING
            else null
        })

        // In-Parentheses
        rules.put(RakuSpacingRule.BracesParentheses, BiFunction func@{ left: RakuBlock?, right: RakuBlock? ->
            if (left!!.node.elementType === RakuTokenTypes.PARENTHESES_OPEN
                || right!!.node.elementType === RakuTokenTypes.PARENTHESES_CLOSE) {
                val parent = left.node.treeParent.psi
                if (parent is RakuSubCall || parent is RakuMethodCall)
                    return@func if (customSettings.CALL_PARENS_SPACING)
                                    SINGLE_SPACE_SPACING
                                else EMPTY_SPACING
                return@func if (customSettings.GROUPING_PARENS_SPACING)
                                SINGLE_SPACE_SPACING
                            else EMPTY_SPACING
            }
            null
        })

        // In array braces
        rules.put(RakuSpacingRule.BracesArray, BiFunction { left: RakuBlock?, right: RakuBlock? ->
            if (left!!.node.elementType === RakuTokenTypes.ARRAY_COMPOSER_OPEN
            || right!!.node.elementType === RakuTokenTypes.ARRAY_COMPOSER_CLOSE)
                if (customSettings.ARRAY_LITERAL_PARENS_SPACING)
                     SINGLE_SPACE_SPACING
                else EMPTY_SPACING
            else null
        })

        // Regex group
        rules.put(RakuSpacingRule.RegexGroup, BiFunction { left: RakuBlock?, right: RakuBlock? ->
            if (left!!.node.elementType === RakuTokenTypes.REGEX_GROUP_BRACKET_OPEN
            || right!!.node.elementType === RakuTokenTypes.REGEX_GROUP_BRACKET_CLOSE)
                if (customSettings.REGEX_GROUP_PARENS_SPACING)
                     SINGLE_SPACE_SPACING
                else EMPTY_SPACING
            else null
        })


        // Regex capture
        rules.put(RakuSpacingRule.RegexCapture, BiFunction { left: RakuBlock?, right: RakuBlock? ->
            if (left!!.node.elementType === RakuTokenTypes.REGEX_CAPTURE_PARENTHESES_OPEN
            || right!!.node.elementType === RakuTokenTypes.REGEX_CAPTURE_PARENTHESES_CLOSE)
                if (customSettings.REGEX_POSITIONAL_PARENS_SPACING) SINGLE_SPACE_SPACING
                else EMPTY_SPACING
            else null
        })

        // Nothing inside different types of braces, parens etc. (block ones are handled in line break rules set
        rules.put(RakuSpacingRule.NothingInsideBraces,  BiFunction { left: RakuBlock?, right: RakuBlock? ->
            if (OPENERS.contains(left!!.node.elementType) || CLOSERS.contains(right!!.node.elementType))
                EMPTY_SPACING
            else null
        })

        // Comma operator, a space after one if it's not a dangling one
        rules.put(RakuSpacingRule.OperatorCommaSpaceAfter, BiFunction { left: RakuBlock?, right: RakuBlock? ->
            if (left!!.node.elementType === RakuElementTypes.INFIX
            && left.node.text == ","
            && right!!.node.elementType !== RakuTokenTypes.METAOP)
                if (isDanglingComma(left.node.treeNext))
                     EMPTY_SPACING
                else
                if (customSettings.AFTER_COMMA)
                     SINGLE_SPACE_SPACING
                else CONSTANT_EMPTY_SPACING
            else null
        })
        // Comma operator, nothing before one
        rules.put(RakuSpacingRule.OperatorCommaNothingBefore, BiFunction { left: RakuBlock?, right: RakuBlock? ->
            if (right!!.node.elementType === RakuElementTypes.INFIX
            && right.node.text == ",")
                if (customSettings.BEFORE_COMMA)
                     SINGLE_SPACE_SPACING
                else CONSTANT_EMPTY_SPACING
            else null
        })

        // Fatarrow
        rules.put(RakuSpacingRule.FatArrow, BiFunction { left: RakuBlock?, right: RakuBlock? ->
            val after  = left!!.node.elementType === RakuTokenTypes.INFIX && left.node.text == "=>"
            val before = right!!.node.elementType === RakuTokenTypes.INFIX && right.node.text == "=>"
            if ((after && customSettings.AFTER_FATARROW)
            || (before && customSettings.BEFORE_FATARROW))
                SINGLE_SPACE_SPACING
            else
                (if (before || after) CONSTANT_EMPTY_SPACING else null)
        })

        // No excessive whitespace around trait
        rules.put(RakuSpacingRule.NoExcessiveSpaceTrait, BiFunction { left: RakuBlock?, right: RakuBlock? ->
            if (right!!.node.elementType === RakuElementTypes.TRAIT
            || left!!.node.elementType === RakuElementTypes.TRAIT)
                SINGLE_SPACE_SPACING
            else null
        })

        // Prefix operators
        rules.put(RakuSpacingRule.OperatorPrefix, BiFunction { left: RakuBlock?, right: RakuBlock? ->
            if (left!!.node.elementType === RakuElementTypes.PREFIX
            && !left.node.text.trim { it <= ' ' }.matches("^[a-zA-Z]*$".toRegex()))
                if (customSettings.AFTER_PREFIX_OPS) SINGLE_SPACE_SPACING
                else CONSTANT_EMPTY_SPACING
            else null
        })

        // Various infix operator related rules
        rules.put(RakuSpacingRule.OperatorInfix, BiFunction func@{ left: RakuBlock?, right: RakuBlock? ->
            val isLeftInfix = left!!.node.elementType === RakuTokenTypes.INFIX
                                || left.node.elementType === RakuElementTypes.INFIX
            val isRightInfix = right!!.node.elementType === RakuTokenTypes.INFIX
                                || right.node.elementType === RakuElementTypes.INFIX
            if (!isLeftInfix && !isRightInfix) return@func null

            // Don't play with regexes, they have separate rules
            if (PsiTreeUtil.getParentOfType<RakuPsiElement?>(
                left.node.psi,
                RakuQuoteRegex::class.java,
                RakuRegexDecl::class.java,
                RakuStatementList::class.java
            ) !is RakuStatementList)
                return@func null

            // Keep metaop like `+=` close
            if (left.node.elementType === RakuElementTypes.INFIX
                && right.node.elementType === RakuTokenTypes.METAOP
            ||  right.node.elementType === RakuElementTypes.INFIX
                && left.node.elementType === RakuTokenTypes.METAOP)
                    return@func CONSTANT_EMPTY_SPACING

            // Binding ops are here
            val leftText = left.node.text
            val rightText = right.node.text
            if (leftText == "=" || leftText == ":=")
                return@func if (customSettings.AFTER_ASSIGNMENT) SINGLE_SPACE_SPACING
                            else CONSTANT_EMPTY_SPACING
            else if (rightText == "=" || rightText == ":=")
                return@func if (customSettings.BEFORE_ASSIGNMENT) SINGLE_SPACE_SPACING
                            else CONSTANT_EMPTY_SPACING

            // Whatever-related expressions
            if (left.node.elementType === RakuElementTypes.WHATEVER)
                return@func if (customSettings.AFTER_WHATEVER_STAR) SINGLE_SPACE_SPACING
                            else CONSTANT_EMPTY_SPACING
            if (right.node.elementType === RakuElementTypes.WHATEVER)
                return@func if (customSettings.BEFORE_WHATEVER_STAR) SINGLE_SPACE_SPACING
                            else CONSTANT_EMPTY_SPACING

            // If it is a dot in a method call, no spacing
            if (leftText == "." || rightText == ".")
                return@func EMPTY_SPACING

            if (isLeftInfix)
                return@func if (if (isLeftInfix) customSettings.AFTER_INFIX else customSettings.BEFORE_INFIX)
                                 SINGLE_SPACE_SPACING
                            else CONSTANT_EMPTY_SPACING
            else
                return@func if (customSettings.BEFORE_INFIX)
                                 SINGLE_SPACE_SPACING
                            else CONSTANT_EMPTY_SPACING
        })

        // Lambda rules
        rules.put(RakuSpacingRule.LambdaRules, BiFunction { left: RakuBlock?, right: RakuBlock? ->
            if (left!!.node.elementType === RakuTokenTypes.LAMBDA)
                if (customSettings.AFTER_LAMBDA)
                    SINGLE_SPACE_SPACING else CONSTANT_EMPTY_SPACING
            else null
        })

        /** Regex related ones  */
        // Regex infix
        rules.put(RakuSpacingRule.RegexInfix, BiFunction func@{ left: RakuBlock?, right: RakuBlock? ->
            val leftInfix = left!!.node.elementType === RakuTokenTypes.REGEX_INFIX
            val rightInfix = right!!.node.elementType === RakuTokenTypes.REGEX_INFIX
            if (!leftInfix && !rightInfix) return@func null

            // FIXME we handle only || here, because e.g. `&& a` turned into `&&a` becomes a call
            if (leftInfix && left.node.text == "||")
                return@func if (customSettings.AFTER_REGEX_INFIX_SPACING)
                                 SINGLE_SPACE_SPACING else CONSTANT_EMPTY_SPACING
            else if (rightInfix && right.node.text == "||")
                return@func if (customSettings.BEFORE_REGEX_INFIX_SPACING)
                                 SINGLE_SPACE_SPACING else CONSTANT_EMPTY_SPACING

            return@func SINGLE_SPACE_SPACING
        })

        // Regex separator
        rules.put(RakuSpacingRule.RegexSeparator, BiFunction func@{ left: RakuBlock?, right: RakuBlock? ->
            val isLeft = left!!.node.elementType === RakuElementTypes.REGEX_QUANTIFIER
                            || left.node.elementType === RakuTokenTypes.REGEX_QUANTIFIER
            val isRight = right!!.node.elementType === RakuElementTypes.REGEX_QUANTIFIER
                            || right.node.elementType === RakuTokenTypes.REGEX_QUANTIFIER
            if (!isLeft && !isRight) return@func null
            val text = if (isLeft) left.node.text else right.node.text
            if (!(text.startsWith("**") || text.startsWith("%") || text.startsWith("%%")))
                return@func null

            if (isLeft)
                return@func if (customSettings.AFTER_REGEX_SEPARATOR_SPACING)
                                SINGLE_SPACE_SPACING else CONSTANT_EMPTY_SPACING
            else
                return@func if (customSettings.BEFORE_REGEX_SEPARATOR_SPACING)
                                SINGLE_SPACE_SPACING else CONSTANT_EMPTY_SPACING
        })

        // Regex quantifier
        rules.put(RakuSpacingRule.RegexQuantifier, BiFunction func@{ left: RakuBlock?, right: RakuBlock? ->
            if (left!!.node.elementType === RakuElementTypes.REGEX_QUANTIFIER)
                return@func if (customSettings.AFTER_REGEX_QUANTIFIER_SPACING)
                                SINGLE_SPACE_SPACING else CONSTANT_EMPTY_SPACING
            if (right!!.node.elementType === RakuElementTypes.REGEX_QUANTIFIER)
                return@func if (customSettings.BEFORE_REGEX_QUANTIFIER_SPACING)
                                SINGLE_SPACE_SPACING else CONSTANT_EMPTY_SPACING
            return@func null
        })
    }

    //TODO: Migrate every rule to a generator sub
    private fun initLineBreakRules(
        commonSettings: CommonCodeStyleSettings,
        customSettings: RakuCodeStyleSettings,
        rules: MutableMap<RakuSpacingRule, BiFunction<RakuBlock?, RakuBlock?, Spacing?>?>
    ) {
        /** Brace style related rules  */
        // Brace style for package
        rules.put(RakuSpacingRule.BraceStylePackage, BiFunction { left: RakuBlock?, right: RakuBlock? ->
            if (right!!.node.elementType === RakuElementTypes.BLOCKOID
            && right.node.treeParent.elementType === RakuElementTypes.PACKAGE_DECLARATION)
                    if (customSettings.PACKAGE_DECL_BRACE_STYLE == 1)
                        SINGLE_SPACE_SPACING else SINGLE_LINE_BREAK
            else null
        })

        // Brace style for routine
        rules.put(RakuSpacingRule.BraceStyleRoutine, BiFunction { left: RakuBlock?, right: RakuBlock? ->
            if (right!!.node.elementType === RakuElementTypes.BLOCKOID
            && right.node.treeParent.elementType === RakuElementTypes.ROUTINE_DECLARATION)
                    if (customSettings.ROUTINE_DECL_BRACE_STYLE == 1)
                        SINGLE_SPACE_SPACING else SINGLE_LINE_BREAK
            else null
        })

        // Brace style for regex
        rules.put(RakuSpacingRule.BraceStyleRegex, BiFunction { left: RakuBlock?, right: RakuBlock? ->
            if (right!!.node.elementType === RakuElementTypes.BLOCKOID
            && right.node.treeParent.elementType === RakuElementTypes.REGEX_DECLARATION)
                    if (customSettings.REGEX_DECL_BRACE_STYLE == 1)
                        SINGLE_SPACE_SPACING else SINGLE_LINE_BREAK
            else null
        })

        // Brace style for phasers and everything else
        rules.put(RakuSpacingRule.BraceStylePhasersEtc,  BiFunction func@{ left: RakuBlock?, right: RakuBlock? ->
            if (PsiTreeUtil.getParentOfType<RakuPsiElement?>(
                    right!!.node.psi, RakuQuoteRegex::class.java, RakuRegexDecl::class.java,
                    RakuStatement::class.java, RakuStrLiteral::class.java, RakuHeredoc::class.java
                ) !is RakuStatement
            ) return@func null
            if (right.node.elementType === RakuElementTypes.BLOCK)
                (if (right.node.treeParent.elementType === RakuElementTypes.PHASER)
                    (if     (customSettings.PHASER_BRACE_STYLE == 1)
                        SINGLE_SPACE_SPACING else SINGLE_LINE_BREAK)
                else
                    (if     (customSettings.OTHER_BRACE_STYLE == 1)
                        SINGLE_SPACE_SPACING else SINGLE_LINE_BREAK))
            else null
        })

        rules.put(RakuSpacingRule.BraceStyleEtc, BiFunction func@{ left: RakuBlock?, right: RakuBlock? ->
            val isOpener = left!!.node.elementType === RakuTokenTypes.BLOCK_CURLY_BRACKET_OPEN
            val isCloser = right!!.node.elementType === RakuTokenTypes.BLOCK_CURLY_BRACKET_CLOSE
            if (!isOpener && !isCloser) return@func null

            // Do not play with {} in regexes
            if (left.node.elementType === RakuTokenTypes.REGEX_LOOKAROUND) return@func null

            if (left.node.elementType === RakuTokenTypes.ONLY_STAR
            || right.node.elementType === RakuTokenTypes.ONLY_STAR) return@func Spacing.createSpacing(0, 0, 0, false, 0)

            val blockoid = left.node.treeParent
            if (blockoid.elementType === RakuElementTypes.BLOCKOID) {
                val source: PsiElement? = PsiTreeUtil.getParentOfType<RakuPsiElement?>(
                    blockoid.psi, RakuPackageDecl::class.java,
                    RakuRoutineDecl::class.java, RakuRegexDecl::class.java,
                    RakuPointyBlock::class.java, RakuStatement::class.java,
                    RakuBlockOrHash::class.java, org.raku.comma.psi.RakuBlock::class.java
                )
                val inner: PsiElement? = PsiTreeUtil.getChildOfAnyType<RakuPsiElement?>(
                    blockoid.psi,
                    RakuStatementList::class.java,
                    RakuRegex::class.java
                )
                val children = inner?.children ?: PsiElement.EMPTY_ARRAY
                val statementCount = children.size
                if (statementCount == 0) {
                    //TODO: Break this down to conceptually absorbable pieces
                    if (source is RakuPackageDecl && customSettings.PACKAGE_DECLARATION_IN_ONE_LINE
                    ||  source is RakuRoutineDecl && customSettings.ROUTINES_DECLARATION_IN_ONE_LINE
                    ||  source is RakuRegexDecl   && customSettings.REGEX_DECLARATION_IN_ONE_LINE
                    ||  source is RakuPointyBlock && customSettings.POINTY_BLOCK_IN_ONE_LINE
                    || (source is RakuStatement || source is RakuBlockOrHash || source is org.raku.comma.psi.RakuBlock)
                    && commonSettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE)
                        return@func Spacing.createSpacing(0, 0, 0, true, 1)

                } else if (statementCount == 1) {
                    if (source is RakuStatement || source is RakuBlockOrHash || source is org.raku.comma.psi.RakuBlock)
                        if (commonSettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE
                        || source is RakuPointyBlock
                        && customSettings.POINTY_BLOCK_IN_ONE_LINE)
                            return@func Spacing.createSpacing(1, 1, 0, true, 1)

                    if (source is RakuRegexDecl && customSettings.REGEX_DECLARATION_IN_ONE_LINE) {
                        val spaces = if (left.node.elementType === RakuElementTypes.REGEX
                                     &&  left.node.text.endsWith("  ")) 0 else 1
                        return@func Spacing.createSpacing(spaces, spaces, 0, true, 1)
                    }

                    if (source is RakuRoutineDecl
                    && PsiTreeUtil.findChildOfType<RakuStubCode?>(
                            source,
                            RakuStubCode::class.java
                        ) != null
                    ) return@func Spacing.createSpacing(0, 0, 0, false, 0)
                }
                if (statementCount < 2) {
                    return@func Spacing.createSpacing(0, 0, 1, false, 0)
                }
            }
            var lineFeeds = 1
            val lhsElementIsRegex = left.node.elementType === RakuElementTypes.REGEX
            val lhsElementHasNewline = left.node.text.replace(" ".toRegex(), "").endsWith("\n")
            if (lhsElementIsRegex && lhsElementHasNewline) {
                lineFeeds = 0
            }

            return@func Spacing.createSpacing(0, 0, lineFeeds, true, 1)
        })

        rules.put(RakuSpacingRule.BraceStyleEtcSemiList, semiListRule())
    }

    private fun semiListRule(): BiFunction<RakuBlock?, RakuBlock?, Spacing?> {
        return BiFunction { left: RakuBlock?, right: RakuBlock? ->
            if ((STATEMENTS.contains(left!!.node.elementType)
            || STATEMENTS.contains(right!!.node.elementType))
            && !(left.node.treeParent.elementType === RakuElementTypes.SEMI_LIST
            || right!!.node.treeParent.elementType === RakuElementTypes.SEMI_LIST))
                    SINGLE_LINE_BREAK else null
        }
    }
}
