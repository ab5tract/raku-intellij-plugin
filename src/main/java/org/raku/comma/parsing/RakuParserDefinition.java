package org.raku.comma.parsing;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.extensions.InternalIgnoreDependencyViolation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.raku.comma.psi.impl.*;
import org.jetbrains.annotations.NotNull;

@InternalIgnoreDependencyViolation
public class RakuParserDefinition implements ParserDefinition {
    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new RakuCondBranchMergingLexer(new RakuLexer());
    }

    @Override
    public PsiParser createParser(Project project) {
        return new RakuParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return RakuElementTypes.FILE;
    }

    // Whitespace tokens are empty, and comment tokens hold only the
    // CONDITIONAL_BRANCH element, as we want the parser itself to match
    // everything else. An inactive #?if branch is
    // carried as a single CONDITIONAL_BRANCH token that the generated parser
    // must never see; registering it as a comment token makes PsiBuilder
    // place it into the tree as a lazy-parseable island behind the parser's
    // back. It cannot be registered as whitespace instead: PsiBuilderImpl's
    // leaf-building code checks the whitespace set first and unconditionally
    // collapses a match to a plain PsiWhiteSpaceImpl, before it ever gets to
    // the check for ILazyParseableElementType -- so a whitespace-registered
    // lazy token can never become a real chameleon node. The comment set has
    // no such short-circuit; JavaDoc's DOC_COMMENT (also a lazy parseable
    // element registered as a comment token) is the platform's own precedent
    // for this exact pattern.
    @NotNull
    @Override
    public TokenSet getWhitespaceTokens() {
        return TokenSet.EMPTY;
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return TokenSet.create(RakuElementTypes.CONDITIONAL_BRANCH);
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return TokenSet.EMPTY;
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode astNode) {
        IElementType type = astNode.getElementType();
        if (type == RakuElementTypes.STATEMENT_LIST)
            return new RakuStatementListImpl(astNode);
        if (type == RakuElementTypes.CONDITIONAL_BRANCH)
            return new RakuCondBranchImpl(astNode);
        if (type == RakuElementTypes.UNTERMINATED_STATEMENT)
            return new RakuUnterminatedStatementImpl(astNode);
        if (type == RakuElementTypes.SEMI_LIST)
            return new RakuSemiListImpl(astNode);
        if (type == RakuElementTypes.STATEMENT)
            return new RakuStatementImpl(astNode);
        if (type == RakuElementTypes.LABEL)
            return new RakuLabelImpl(astNode);
        if (type == RakuElementTypes.TERM_DEFINITION)
            return new RakuTermDefinitionImpl(astNode);
        if (type == RakuElementTypes.BLOCK)
            return new RakuBlockImpl(astNode);
        if (type == RakuElementTypes.BLOCKOID)
            return new RakuBlockoidImpl(astNode);
        if (type == RakuElementTypes.COMMENT)
            return new RakuCommentImpl(astNode);
        if (type == RakuElementTypes.IF_STATEMENT)
            return new RakuIfStatementImpl(astNode);
        if (type == RakuElementTypes.UNLESS_STATEMENT)
            return new RakuUnlessStatementImpl(astNode);
        if (type == RakuElementTypes.WITHOUT_STATEMENT)
            return new RakuWithoutStatementImpl(astNode);
        if (type == RakuElementTypes.WHILE_STATEMENT)
            return new RakuWhileStatementImpl(astNode);
        if (type == RakuElementTypes.UNTIL_STATEMENT)
            return new RakuUntilStatementImpl(astNode);
        if (type == RakuElementTypes.REPEAT_STATEMENT)
            return new RakuRepeatStatementImpl(astNode);
        if (type == RakuElementTypes.FOR_STATEMENT)
            return new RakuForStatementImpl(astNode);
        if (type == RakuElementTypes.WHENEVER_STATEMENT)
            return new RakuWheneverStatementImpl(astNode);
        if (type == RakuElementTypes.LOOP_STATEMENT)
            return new RakuLoopStatementImpl(astNode);
        if (type == RakuElementTypes.NEED_STATEMENT)
            return new RakuNeedStatementImpl(astNode);
        if (type == RakuElementTypes.IMPORT_STATEMENT)
            return new RakuImportStatementImpl(astNode);
        if (type == RakuElementTypes.NO_STATEMENT)
            return new RakuNoStatementImpl(astNode);
        if (type == RakuElementTypes.USE_STATEMENT)
            return new RakuUseStatementImpl(astNode);
        if (type == RakuElementTypes.REQUIRE_STATEMENT)
            return new RakuRequireStatementImpl(astNode);
        if (type == RakuElementTypes.GIVEN_STATEMENT)
            return new RakuGivenStatementImpl(astNode);
        if (type == RakuElementTypes.WHEN_STATEMENT)
            return new RakuWhenStatementImpl(astNode);
        if (type == RakuElementTypes.DEFAULT_STATEMENT)
            return new RakuDefaultStatementImpl(astNode);
        if (type == RakuElementTypes.CATCH_STATEMENT)
            return new RakuCatchStatementImpl(astNode);
        if (type == RakuElementTypes.CONTROL_STATEMENT)
            return new RakuControlStatementImpl(astNode);
        if (type == RakuElementTypes.QUIT_STATEMENT)
            return new RakuQuitStatementImpl(astNode);
        if (type == RakuElementTypes.PHASER)
            return new RakuPhaserImpl(astNode);
        if (type == RakuElementTypes.RACE)
            return new RakuRaceImpl(astNode);
        if (type == RakuElementTypes.HYPER)
            return new RakuHyperImpl(astNode);
        if (type == RakuElementTypes.LAZY)
            return new RakuLazyImpl(astNode);
        if (type == RakuElementTypes.EAGER)
            return new RakuEagerImpl(astNode);
        if (type == RakuElementTypes.SINK)
            return new RakuSinkImpl(astNode);
        if (type == RakuElementTypes.TRY)
            return new RakuTryImpl(astNode);
        if (type == RakuElementTypes.QUIETLY)
            return new RakuQuietlyImpl(astNode);
        if (type == RakuElementTypes.GATHER)
            return new RakuGatherImpl(astNode);
        if (type == RakuElementTypes.ONCE)
            return new RakuOnceImpl(astNode);
        if (type == RakuElementTypes.START)
            return new RakuStartImpl(astNode);
        if (type == RakuElementTypes.SUPPLY)
            return new RakuSupplyImpl(astNode);
        if (type == RakuElementTypes.REACT)
            return new RakuReactImpl(astNode);
        if (type == RakuElementTypes.DO)
            return new RakuDoImpl(astNode);
        if (type == RakuElementTypes.STATEMENT_MOD_COND)
            return new RakuStatementModCondImpl(astNode);
        if (type == RakuElementTypes.STATEMENT_MOD_LOOP)
            return new RakuStatementModLoopImpl(astNode);
        if (type == RakuElementTypes.SCOPED_DECLARATION)
            return new RakuScopedDeclImpl(astNode);
        if (type == RakuElementTypes.VARIABLE_DECLARATION)
            return new RakuVariableDeclImpl(astNode);
        if (type == RakuElementTypes.PACKAGE_DECLARATION)
            return new RakuPackageDeclImpl(astNode);
        if (type == RakuElementTypes.ALSO)
            return new RakuAlsoImpl(astNode);
        if (type == RakuElementTypes.TRUSTS)
            return new RakuTrustsImpl(astNode);
        if (type == RakuElementTypes.ROLE_SIGNATURE)
            return new RakuRoleSignatureImpl(astNode);
        if (type == RakuElementTypes.POINTY_BLOCK)
            return new RakuPointyBlockImpl(astNode);
        if (type == RakuElementTypes.PREFIX)
            return new RakuPrefixImpl(astNode);
        if (type == RakuElementTypes.INFIX)
            return new RakuInfixImpl(astNode);
        if (type == RakuElementTypes.POSTFIX)
            return new RakuPostfixImpl(astNode);
        if (type == RakuElementTypes.OPERATOR_ADVERB)
            return new RakuOperatorAdverbImpl(astNode);
        if (type == RakuOPPElementTypes.PREFIX_APPLICATION)
            return new RakuPrefixApplicationImpl(astNode);
        if (type == RakuOPPElementTypes.POSTFIX_APPLICATION || type == RakuElementTypes.POSTFIX_APPLICATION)
            return new RakuPostfixApplicationImpl(astNode);
        if (type == RakuOPPElementTypes.INFIX_APPLICATION)
            return new RakuInfixApplicationImpl(astNode);
        if (type == RakuOPPElementTypes.ADVERB_APPLICATION)
            return new RakuOperatorAdverbApplicationImpl(astNode);
        if (type == RakuElementTypes.REDUCE_METAOP)
            return new RakuReduceMetaOpImpl(astNode);
        if (type == RakuElementTypes.CROSS_METAOP)
            return new RakuCrossMetaOpImpl(astNode);
        if (type == RakuElementTypes.NEGATION_METAOP)
            return new RakuNegationMetaOpImpl(astNode);
        if (type == RakuElementTypes.REVERSE_METAOP)
            return new RakuReverseMetaOpImpl(astNode);
        if (type == RakuElementTypes.SEQUENTIAL_METAOP)
            return new RakuSequentialMetaOpImpl(astNode);
        if (type == RakuElementTypes.ZIP_METAOP)
            return new RakuZipMetaOpImpl(astNode);
        if (type == RakuElementTypes.HYPER_METAOP)
            return new RakuHyperMetaOpImpl(astNode);
        if (type == RakuElementTypes.ASSIGN_METAOP)
            return new RakuAssignMetaOpImpl(astNode);
        if (type == RakuElementTypes.BRACKETED_INFIX)
            return new RakuBracketedInfixImpl(astNode);
        if (type == RakuElementTypes.ARRAY_INDEX)
            return new RakuArrayIndexImpl(astNode);
        if (type == RakuElementTypes.HASH_INDEX)
            return new RakuHashIndexImpl(astNode);
        if (type == RakuElementTypes.CALL)
            return new RakuCallImpl(astNode);
        if (type == RakuElementTypes.VARIABLE)
            return new RakuVariableImpl(astNode);
        if (type == RakuElementTypes.CONTEXTUALIZER)
            return new RakuContextualizerImpl(astNode);
        if (type == RakuElementTypes.INTEGER_LITERAL)
            return new RakuIntLiteralImpl(astNode);
        if (type == RakuElementTypes.NUMBER_LITERAL)
            return new RakuNumLiteralImpl(astNode);
        if (type == RakuElementTypes.RAT_LITERAL)
            return new RakuRatLiteralImpl(astNode);
        if (type == RakuElementTypes.COMPLEX_LITERAL)
            return new RakuComplexLiteralImpl(astNode);
        if (type == RakuElementTypes.RADIX_NUMBER)
            return new RakuRadixNumberImpl(astNode);
        if (type == RakuElementTypes.STRING_LITERAL)
            return new RakuStrLiteralImpl(astNode);
        if (type == RakuElementTypes.HEREDOC)
            return new RakuHeredocImpl(astNode);
        if (type == RakuElementTypes.QUOTE_REGEX)
            return new RakuQuoteRegexImpl(astNode);
        if (type == RakuElementTypes.REGEX_CALL)
            return new RakuRegexCallImpl(astNode);
        if (type == RakuElementTypes.QUOTE_PAIR)
            return new RakuQuotePairImpl(astNode);
        if (type == RakuElementTypes.VERSION)
            return new RakuVersionImpl(astNode);
        if (type == RakuElementTypes.SUB_CALL)
            return new RakuSubCallImpl(astNode);
        if (type == RakuElementTypes.SUB_CALL_NAME)
            return new RakuSubCallNameImpl(astNode);
        if (type == RakuElementTypes.METHOD_CALL)
            return new RakuMethodCallImpl(astNode);
        if (type == RakuElementTypes.TYPE_NAME)
            return new RakuTypeNameImpl(astNode);
        if (type == RakuElementTypes.LONG_NAME)
            return new RakuLongNameImpl(astNode);
        if (type == RakuElementTypes.MODULE_NAME)
            return new RakuModuleNameImpl(astNode);
        if (type == RakuElementTypes.IS_TRAIT_NAME)
            return new RakuIsTraitNameImpl(astNode);
        if (type == RakuElementTypes.SELF)
            return new RakuSelfImpl(astNode);
        if (type == RakuElementTypes.WHATEVER)
            return new RakuWhateverImpl(astNode);
        if (type == RakuElementTypes.HYPER_WHATEVER)
            return new RakuHyperWhateverImpl(astNode);
        if (type == RakuElementTypes.TERM)
            return new RakuTermImpl(astNode);
        if (type == RakuElementTypes.NULL_TERM)
            return new RakuNullTermImpl(astNode);
        if (type == RakuElementTypes.STUB_CODE)
            return new RakuStubCodeImpl(astNode);
        if (type == RakuElementTypes.CAPTURE)
            return new RakuCaptureImpl(astNode);
        if (type == RakuElementTypes.ONLY_STAR)
            return new RakuOnlyStarImpl(astNode);
        if (type == RakuElementTypes.FATARROW)
            return new RakuFatArrowImpl(astNode);
        if (type == RakuElementTypes.COLON_PAIR)
            return new RakuColonPairImpl(astNode);
        if (type == RakuElementTypes.ROUTINE_DECLARATION)
            return new RakuRoutineDeclImpl(astNode);
        if (type == RakuElementTypes.MULTI_DECLARATION)
            return new RakuMultiDeclImpl(astNode);
        if (type == RakuElementTypes.REGEX_DECLARATION)
            return new RakuRegexDeclImpl(astNode);
        if (type == RakuElementTypes.SIGNATURE)
            return new RakuSignatureImpl(astNode);
        if (type == RakuElementTypes.PARAMETER)
            return new RakuParameterImpl(astNode);
        if (type == RakuElementTypes.PARAMETER_VARIABLE)
            return new RakuParameterVariableImpl(astNode);
        if (type == RakuElementTypes.NAMED_PARAMETER)
            return new RakuNamedParameterImpl(astNode);
        if (type == RakuElementTypes.ARRAY_SHAPE)
            return new RakuArrayShapeImpl(astNode);
        if (type == RakuElementTypes.WHERE_CONSTRAINT)
            return new RakuWhereConstraintImpl(astNode);
        if (type == RakuElementTypes.VALUE_CONSTRAINT)
            return new RakuValueConstraintImpl(astNode);
        if (type == RakuElementTypes.PARAMETER_DEFAULT)
            return new RakuParameterDefaultImpl(astNode);
        if (type == RakuElementTypes.RETURN_CONSTRAINT)
            return new RakuReturnConstraintImpl(astNode);
        if (type == RakuElementTypes.TRAIT)
            return new RakuTraitImpl(astNode);
        if (type == RakuElementTypes.PARENTHESIZED_EXPRESSION)
            return new RakuParenthesizedExprImpl(astNode);
        if (type == RakuElementTypes.ARRAY_COMPOSER)
            return new RakuArrayComposerImpl(astNode);
        if (type == RakuElementTypes.BLOCK_OR_HASH)
            return new RakuBlockOrHashImpl(astNode);
        if (type == RakuElementTypes.ENUM)
            return new RakuEnumImpl(astNode);
        if (type == RakuElementTypes.SUBSET)
            return new RakuSubsetImpl(astNode);
        if (type == RakuElementTypes.CONSTANT)
            return new RakuConstantImpl(astNode);
        if (type == RakuElementTypes.REGEX)
            return new RakuRegexImpl(astNode);
      if (type == RakuOPPElementTypes.REGEX_INFIX_APPLICATION)
        return new RakuRegexInfixApplicationImpl(astNode);
        if (type == RakuElementTypes.REGEX_ATOM)
            return new RakuRegexAtomImpl(astNode);
        if (type == RakuElementTypes.REGEX_SIGSPACE)
            return new RakuRegexSigspaceImpl(astNode);
        if (type == RakuElementTypes.REGEX_LITERAL)
            return new RakuRegexLiteralImpl(astNode);
        if (type == RakuElementTypes.REGEX_ANCHOR)
            return new RakuRegexAnchorImpl(astNode);
        if (type == RakuElementTypes.REGEX_GROUP)
            return new RakuRegexGroupImpl(astNode);
        if (type == RakuElementTypes.REGEX_CAPTURE_POSITIONAL)
            return new RakuRegexCapturePositionalImpl(astNode);
        if (type == RakuElementTypes.REGEX_QUANTIFIER)
            return new RakuRegexQuantifierImpl(astNode);
        if (type == RakuElementTypes.REGEX_SEPARATOR)
            return new RakuRegexSeparatorImpl(astNode);
        if (type == RakuElementTypes.REGEX_BUILTIN_CCLASS)
            return new RakuRegexBuiltinCClassImpl(astNode);
        if (type == RakuElementTypes.REGEX_ASSERTION)
            return new RakuRegexAssertionImpl(astNode);
        if (type == RakuElementTypes.REGEX_GOAL)
            return new RakuRegexGoalImpl(astNode);
        if (type == RakuElementTypes.REGEX_CCLASS)
            return new RakuRegexCClassImpl(astNode);
        if (type == RakuElementTypes.REGEX_CCLASS_ELEM)
            return new RakuRegexCClassElemImpl(astNode);
        if (type == RakuElementTypes.REGEX_MOD_INTERNAL)
            return new RakuRegexModInternalImpl(astNode);
        if (type == RakuElementTypes.REGEX_VARIABLE)
            return new RakuRegexVariableImpl(astNode);
        if (type == RakuElementTypes.TRANSLITERATION)
            return new RakuTransliterationImpl(astNode);
        if (type == RakuElementTypes.POD_BLOCK_FINISH)
            return new PodBlockFinishImpl(astNode);
        if (type == RakuElementTypes.POD_BLOCK_DELIMITED)
            return new PodBlockDelimitedImpl(astNode);
        if (type == RakuElementTypes.POD_BLOCK_PARAGRAPH)
            return new PodBlockParagraphImpl(astNode);
        if (type == RakuElementTypes.POD_BLOCK_ABBREVIATED)
            return new PodBlockAbbreviatedImpl(astNode);
        if (type == RakuElementTypes.POD_CONFIGURATION)
            return new PodConfigurationImpl(astNode);
        if (type == RakuElementTypes.POD_FORMATTED)
            return new PodFormattedImpl(astNode);
        if (type == RakuElementTypes.POD_TEXT)
            return new PodTextImpl(astNode);
        if (type == RakuElementTypes.QUASI)
            return new RakuQuasiImpl(astNode);
        if (type == RakuElementTypes.POD_PRE_COMMENT)
            return new PodPreCommentImpl(astNode);
        if (type == RakuElementTypes.POD_POST_COMMENT)
            return new PodPostCommentImpl(astNode);
        return null;
    }

    @Override
    public PsiFile createFile(FileViewProvider fileViewProvider) {
        return new RakuFileImpl(fileViewProvider);
    }

    @Override
    public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode astNode, ASTNode astNode1) {
        return SpaceRequirements.MAY;
    }
}
