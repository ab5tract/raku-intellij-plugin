package org.raku.comma.formatter

import com.intellij.formatting.*
import com.intellij.formatting.templateLanguages.BlockWithParent
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.ContainerUtil
import org.raku.comma.formatter.RakuFormattingModelBuilder.RakuSpacingRule
import org.raku.comma.parsing.RakuElementTypes
import org.raku.comma.parsing.RakuOPPElementTypes
import org.raku.comma.parsing.RakuTokenTypes
import org.raku.comma.psi.*
import org.raku.comma.utils.RakuPsiUtil
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function

internal class RakuBlock : AbstractBlock, BlockWithParent {
    private val rules: MutableMap<RakuSpacingRule, BiFunction<RakuBlock?, RakuBlock?, Spacing?>>
    private val commonSettings: CommonCodeStyleSettings?
    private val settings: RakuCodeStyleSettings
    private var parent: BlockWithParent? = null
    private val isStatementContinuation: Boolean?

    private val WHITESPACES = TokenSet.create(
        RakuTokenTypes.UNV_WHITE_SPACE,
        RakuTokenTypes.WHITE_SPACE,
        RakuTokenTypes.VERTICAL_WHITE_SPACE,
        RakuTokenTypes.UNSP_WHITE_SPACE
    )
    private var children: ArrayList<Block?>? = null

    constructor(
        node: ASTNode,
        wrap: Wrap?,
        align: Alignment?,
        commonSettings: CommonCodeStyleSettings?,
        customSettings: RakuCodeStyleSettings,
        rules: MutableMap<RakuSpacingRule, BiFunction<RakuBlock?, RakuBlock?, Spacing?>>
    ) : super(node, wrap, align) {
        this@RakuBlock.rules = rules
        this@RakuBlock.commonSettings = commonSettings
        settings = customSettings
        this.isStatementContinuation = false
    }

    private constructor(
        node: ASTNode,
        wrap: Wrap?,
        align: Alignment?,
        isStatementContinuation: Boolean?,
        commonSettings: CommonCodeStyleSettings?,
        customSettings: RakuCodeStyleSettings,
        rules: MutableMap<RakuSpacingRule, BiFunction<RakuBlock?, RakuBlock?, Spacing?>>
    ) : super(node, wrap, align) {
        this@RakuBlock.rules = rules
        this@RakuBlock.commonSettings = commonSettings
        this.settings = customSettings
        this.isStatementContinuation = isStatementContinuation
    }

    override fun buildChildren(): MutableList<Block?>? {
        if (isLeaf) return EMPTY

        val children = ArrayList<Block?>()
        val alignFunction = calculateAlignment(myNode)

        var child = node.firstChildNode
        while (child != null) {
            val elementType = child.elementType
            if (WHITESPACES.contains(elementType)) {
                child = child.treeNext
                continue
            }

            val childBlock: RakuBlock?
            var childIsStatementContinuation: Boolean? = null
            if (isStatementContinuation != null && isStatementContinuation) {
                childIsStatementContinuation = false
            } else if (nodeInStatementContinuation(child)) {
                childIsStatementContinuation = true
            }
            val align = if (alignFunction == null)
                            null
                        else
                            if (alignFunction.first!!.apply(child) == true)
                                alignFunction.second
                            else
                                null
            childBlock = RakuBlock(child,
                                   null,
                                   align,
                                   childIsStatementContinuation,
                                   commonSettings,
                                   settings,
                                   rules)
            childBlock.parent = this
            children.add(childBlock)
            child = child.treeNext
        }
        return children.also { this.children = it }
    }

    override fun getWrap(): Wrap? {
        // Basic sanity check: we don't wrap anything when interpolated in a str literal
        if (PsiTreeUtil.getParentOfType<RakuPsiElement?>(
                myNode.psi,
                RakuFile::class.java,
                RakuStrLiteral::class.java,
                RakuHeredoc::class.java
            ) !is RakuFile
        ) return null

        if (myNode.elementType === RakuElementTypes.PARAMETER && settings.PARAMETER_WRAP)
            return Wrap.createWrap(WrapType.NORMAL, true)

        if (myNode.elementType === RakuElementTypes.TRAIT && settings.TRAIT_WRAP)
            return Wrap.createWrap(WrapType.NORMAL, true)

        if (myNode.psi is RakuMethodCall
        && myNode.text.startsWith(".")
        && settings.METHOD_CALL_WRAP)
            return Wrap.createWrap(WrapType.NORMAL, false)

        if (myNode.treeParent != null
        && myNode.treeParent.psi is RakuInfixApplication
        && myNode.elementType !== RakuTokenTypes.NULL_TERM
        && myNode.elementType !== RakuElementTypes.INFIX)
        {
            val application = myNode.treeParent.psi as RakuInfixApplication
            val parent: PsiElement? = PsiTreeUtil.getParentOfType<RakuPsiElement?>(
                application, RakuSubCall::class.java, RakuMethodCall::class.java,
                RakuArrayComposer::class.java, RakuVariableDecl::class.java
            )
            if (application.getOperator() == ","
                && (parent is RakuSubCall || parent is RakuMethodCall)
            ) {
                return if (settings.CALL_ARGUMENTS_WRAP)
                    Wrap.createWrap(WrapType.NORMAL, false)
                else
                    null
            }

            if (application.getOperator() == ","
                && (parent is RakuSubCall || parent is RakuMethodCall)
                && parent is RakuArrayComposer || parent is RakuVariableDecl
            ) {
                return if (settings.ARRAY_ELEMENTS_WRAP)
                    Wrap.createWrap(WrapType.NORMAL, false)
                else
                    null
            }

            if (application.getOperator() != "." && settings.INFIX_APPLICATION_WRAP)
                return Wrap.createWrap(WrapType.NORMAL, false)
        }

        return null
    }

    private fun calculateAlignment(node: ASTNode): Pair<Function<ASTNode?, Boolean?>?, Alignment?>? {
        val type = node.elementType
        if (type === RakuElementTypes.SIGNATURE && settings.PARAMETER_ALIGNMENT) {
            return Pair.create<Function<ASTNode?, Boolean?>?, Alignment?>(
                Function { child: ASTNode? -> child!!.elementType === RakuElementTypes.PARAMETER },
                Alignment.createAlignment()
            )
        } else if (type === RakuElementTypes.ARRAY_COMPOSER && settings.ARRAY_ELEMENTS_ALIGNMENT) {
            return Pair.create<Function<ASTNode?, Boolean?>?, Alignment?>(
                Function { child: ASTNode? ->
                    child!!.elementType === RakuTokenTypes.ARRAY_COMPOSER_OPEN
                            && child.elementType === RakuTokenTypes.ARRAY_COMPOSER_CLOSE
                },
                Alignment.createAlignment()
            )
        } else if (type === RakuOPPElementTypes.INFIX_APPLICATION && node.psi.lastChild !is RakuMethodCall) {
            val infixApp = node.psi as? RakuInfixApplication ?: return null

            // TODO: Set up a rule for ternaries
            if (infixApp.operator == "??") return null // Do not align ?? !!, we'll just indent it

            if (infixApp.operator == ",") {
                // Do not touch heredoc for real
                if (ContainerUtil.exists<PsiElement?>(
                        infixApp.getOperands(),
                        Condition { s: PsiElement? -> s is RakuHeredoc })
                ) {
                    return null
                }
                // If we have a comma separated list, there can be two cases: non-literal array init or signature
                // check those cases, otherwise just do as infix guides us
                val origin = PsiTreeUtil.getParentOfType<PsiElement?>(
                    infixApp,
                    RakuVariableDecl::class.java,
                    RakuCodeBlockCall::class.java,
                    RakuArrayComposer::class.java,
                    RakuStatement::class.java
                )
                // This case is handled by another option
                if (origin is RakuArrayComposer) return null
                if (origin is RakuCodeBlockCall) {
                    return if (settings.CALL_ARGUMENTS_ALIGNMENT)
                        Pair.create<Function<ASTNode?, Boolean?>?, Alignment?>(
                            Function { child: ASTNode? ->
                                child!!.elementType !== RakuTokenTypes.INFIX
                                        && child.elementType !== RakuTokenTypes.NULL_TERM
                            },
                            Alignment.createAlignment()
                        )
                    else
                        null
                } else {
                    return if (settings.ARRAY_ELEMENTS_ALIGNMENT)
                        Pair.create<Function<ASTNode?, Boolean?>?, Alignment?>(
                            Function { child: ASTNode? ->
                                child!!.elementType !== RakuTokenTypes.INFIX
                                        && child.elementType !== RakuTokenTypes.NULL_TERM
                            },
                            Alignment.createAlignment()
                        )
                    else
                        null
                }
            }

            if (settings.INFIX_APPLICATION_ALIGNMENT) {
                return Pair.create<Function<ASTNode?, Boolean?>?, Alignment?>(
                    Function { child: ASTNode? ->
                                child!!.elementType !== RakuTokenTypes.INFIX
                                && child.elementType !== RakuTokenTypes.NULL_TERM
                    },
                    Alignment.createAlignment()
                )
            }
        } else if (TRAIT_CARRIERS.contains(type) && settings.TRAIT_ALIGNMENT) {
            return Pair.create<Function<ASTNode?, Boolean?>?, Alignment?>(
                Function { child: ASTNode? -> child!!.elementType === RakuElementTypes.TRAIT },
                Alignment.createAlignment()
            )
        }

        if (type === RakuElementTypes.BLOCKOID) {
            return Pair.create<Function<ASTNode?, Boolean?>?, Alignment?>(Function { child: ASTNode? ->
                child!!.elementType === RakuTokenTypes.BLOCK_CURLY_BRACKET_OPEN && WHITESPACES.contains(
                    child.treeNext.elementType
                ) && child.treeNext.treeNext.elementType === RakuTokenTypes.BLOCK_CURLY_BRACKET_OPEN
            }, Alignment.createAlignment())
        }

        return null
    }

    fun trimText(text: String): String {
        val lines = text.lines()
        return if (lines.size > 11) lines.subList(0, 11).joinToString("\n") + "\n..........." else text
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        if (child1 !is RakuBlock || child2 !is RakuBlock)
            return null

        if (child2.node.elementType === RakuElementTypes.REGEX_SIGSPACE)
            return null

        for (ruleKey in RakuSpacingRule.entries) {
            val rule = rules[ruleKey] ?: continue
            val result = rule.apply(child1, child2)
            if (result != null) {
                if (DEBUG_MODE) {
                    System.out.printf("Left: %s [%s]%n", child1.node.elementType, trimText(child1.node.text))
                    System.out.printf("Right: %s [%s]%n", child2.node.elementType, trimText(child2.node.text))
                    println("Applied rule: $ruleKey\n")
                }
                return result
            }
        }

        if (DEBUG_MODE) {
            System.out.printf("Left: %s [%s]%n", child1.node.elementType, trimText(child1.node.text))
            System.out.printf("Right: %s [%s]%n", child2.node.elementType, trimText(child2.node.text))
            println("==> No rule was applied.\n")
        }
        return null
    }

    override fun getIndent(): Indent? {
        if (myNode.treeParent == null) return null

        if (myNode.treeParent.psi is RakuHeredoc) {
            return Indent.getAbsoluteNoneIndent()
        }

        if (myNode.treeParent.psi is RakuBlockoid
            && myNode.treeNext != null && myNode.treePrev != null
        ) {
            return if (myNode.textLength == 0)
                        Indent.getNoneIndent() else Indent.getNormalIndent()
        }

        if (myNode.elementType === RakuElementTypes.SEMI_LIST
            && (myNode.treeParent.elementType === RakuElementTypes.ARRAY_COMPOSER
                    || myNode.treeParent.elementType === RakuElementTypes.CONTEXTUALIZER)
        ) {
            return if (myNode.textLength == 0)
                        Indent.getNoneIndent() else Indent.getNormalIndent()
        }

        if (myNode.elementType === RakuTokenTypes.STATEMENT_TERMINATOR) {
            return Indent.getContinuationIndent()
        }

        if (isStatementContinuation != null && isStatementContinuation) {
            if (
                myNode.elementType === RakuTokenTypes.PARENTHESES_CLOSE
                && myNode.treeParent.psi is RakuSignature
            ) {
                return Indent.getSpaceIndent(1, true)
            } else if (
                myNode.elementType === RakuTokenTypes.PARENTHESES_CLOSE
                && myNode.treeParent.psi is RakuSubCall
            ) {
                val subCall = myNode.treeParent.psi as RakuSubCall
                if (subCall.getCallArguments().size != 0) {
                    val infix: PsiElement? = subCall.getCallArguments()[0].getParent()
                    return Indent.getSpaceIndent(
                        if (infix is RakuInfixApplication)
                            infix.startOffsetInParent
                        else
                            0, true
                    )
                }
            } else if (myNode.elementType === RakuTokenTypes.ARRAY_COMPOSER_CLOSE) {
                return Indent.getNoneIndent()
            }
            return Indent.getContinuationIndent()
        }

        return Indent.getNoneIndent()
    }

    override fun getChildAttributes(newIndex: Int): ChildAttributes {
        val elementType = myNode.elementType
        if (elementType === RakuElementTypes.REGEX_GROUP || elementType === RakuElementTypes.ARRAY_COMPOSER) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        } else if (elementType === RakuElementTypes.BLOCKOID) {
            var subblocks: MutableList<Block?>?
            var block = getSubBlocks().get(newIndex - 1)
            while (block != null) {
                subblocks = block.subBlocks
                if (subblocks.isNotEmpty()) {
                    block = subblocks.last()
                } else if (block is RakuBlock && block.node.elementType === RakuElementTypes.UNTERMINATED_STATEMENT) {
                    return ChildAttributes(Indent.getContinuationIndent(), obtainAlign(block))
                } else {
                    return ChildAttributes(Indent.getNormalIndent(), null)
                }
            }
            return ChildAttributes(Indent.getNormalIndent(), null)
        } else if (elementType === RakuElementTypes.SIGNATURE || elementType === RakuOPPElementTypes.INFIX_APPLICATION) {
            return ChildAttributes(Indent.getContinuationIndent(), obtainAlign(this))
        } else if (elementType === RakuElementTypes.METHOD_CALL) {
            return ChildAttributes(Indent.getContinuationIndent(), null)
        } else if (myNode.psi is RakuPsiDeclaration) {
            val blocks = getSubBlocks()
            val block = blocks.get(newIndex - 1)
            if (block is RakuBlock) {
                if (block.node.psi is RakuTrait) {
                    return ChildAttributes(Indent.getContinuationIndent(), block.alignment)
                }
            }
        }
        return ChildAttributes(Indent.getNoneIndent(), null)
    }

    override fun isLeaf(): Boolean {
        return myNode.firstChildNode == null
    }

    override fun getParent(): BlockWithParent? {
        return parent
    }

    override fun setParent(newParent: BlockWithParent?) {
        parent = newParent
    }

    companion object {
        private const val DEBUG_MODE = true
        private val TRAIT_CARRIERS = TokenSet.create(
            RakuElementTypes.PACKAGE_DECLARATION,
            RakuElementTypes.ROUTINE_DECLARATION,
            RakuElementTypes.VARIABLE_DECLARATION,
            RakuElementTypes.PARAMETER,
            RakuElementTypes.REGEX_DECLARATION,
            RakuElementTypes.ENUM,
            RakuElementTypes.SUBSET,
            RakuElementTypes.CONSTANT
        )

        private fun nodeInStatementContinuation(startNode: ASTNode): Boolean {
            val startPsi = startNode.psi
            val file = startPsi.containingFile
            if (file == null || !file.isPhysical) return false

            val doc = file.viewProvider.document
            if (doc == null) return false

            // Check if the node spans over multiple lines, making us want a continuation
            if (doc.getLineNumber(startPsi.textOffset) == doc.getLineNumber(
                    startPsi.parent
                        .textOffset
                )
            ) {
                return false
            }

            // Comments can be swept under other statement nodes, but they are in no way a continuation
            if (startPsi is PodPreComment || startPsi is PodPostComment) {
                return false
            }

            // Traits have continuation indent
            if (startPsi is RakuTrait) return true

            // While infix is handled below, a special case of `my $foo = <continuation here> ...` is handled here
            if (startPsi.parent is RakuVariableDecl) {
                if (RakuPsiUtil.skipSpaces(startPsi.prevSibling, false) is RakuInfix) {
                    return true
                }
            }

            // Now checking parents
            if (startPsi.parent is RakuInfixApplication) {
                return !checkIfNonContinuatedInitializer(startPsi)
            } else if (startPsi.parent is RakuSubCall ||
                startPsi.parent is RakuSignature ||
                startPsi.parent is RakuMethodCall ||
                startPsi.parent is RakuArrayComposer
            ) {
                return true
            }
            return false
        }

        private fun checkIfNonContinuatedInitializer(startPsi: PsiElement): Boolean {
            if ((startPsi.parent as RakuInfixApplication).getOperator() == ",") {
                val contextualizer =
                    PsiTreeUtil.getParentOfType<RakuContextualizer?>(startPsi, RakuContextualizer::class.java)
                if (contextualizer != null && contextualizer.text.startsWith("%")) {
                    return true
                }

                val hopefullyStatement = startPsi.parent.parent
                if (hopefullyStatement is RakuStatement) {
                    val statementHolder = hopefullyStatement.parent
                    if (statementHolder is RakuStatementList) {
                        // Might be in a hash initializer. Check if the first child of the infix
                        // application is pair-like.
                        val hopefullyPairish = startPsi.parent.children[0]
                        if (hopefullyPairish is RakuFatArrow || hopefullyPairish is RakuColonPair) {
                            // It's fine, now just check we're in the appropriate kind of block.
                            val hopefullyBlockoid = statementHolder.parent
                            return hopefullyBlockoid is RakuBlockoid && hopefullyBlockoid.parent is RakuBlockOrHash
                        }
                    } else if (statementHolder is RakuSemiList) {
                        // Might be an array literal.
                        if (statementHolder.parent is RakuArrayComposer) {
                            return false
                        }
                    }
                }
            }
            return false
        }

        private fun obtainAlign(block: RakuBlock): Alignment? {

            val elementType = block.node.elementType
            if (elementType === RakuElementTypes.SIGNATURE) {
                return block.children!!.stream()
                    .map<Alignment?> { obj: Block? -> obj!!.alignment }
                    .filter { obj: Alignment? -> Objects.nonNull(obj) }
                    .findFirst()
                    .orElse(null)
            } else if (elementType === RakuOPPElementTypes.INFIX_APPLICATION) {
                val application = block.node.psi as RakuInfixApplication
                if (application.getOperator() == "=" && application.getOperands().size == 2 &&
                    application.getOperands()[1] is RakuInfix
                ) {
                    return null
                } else {
                    return block.children!!.stream()
                        .map<Alignment?> { obj: Block? -> obj!!.alignment }
                        .filter { obj: Alignment? -> Objects.nonNull(obj) }
                        .findFirst()
                        .orElse(null)
                }
            } else if (elementType === RakuElementTypes.UNTERMINATED_STATEMENT) {
                val base = block.parent as RakuBlock
                val blocks = base.getSubBlocks()
                for (i in blocks.indices) {
                    val temp = blocks.get(i)
                    if (temp === block) {
                        return obtainAlign((blocks.get(i - 1) as RakuBlock?)!!)
                    }
                }
            }
            return null
        }
    }
}
