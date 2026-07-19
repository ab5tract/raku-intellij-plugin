package org.raku.comma.intention

import com.intellij.codeInsight.intention.IntentionAction
import org.raku.comma.ALL_RAKU_INSPECTIONS
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType


class IntentionTest : CommaFixtureTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(*ALL_RAKU_INSPECTIONS)
    }

    override fun getTestDataPath(): String {
        return "testData/intention"
    }

    fun testZeroToN() {
        executeIntention("Use simpler range syntax")
    }

    fun testZeroToExclusiveN() {
        executeIntention("Use simpler range syntax")
    }

    fun testZeroToVar() {
        executeIntention("Use simpler range syntax")
    }

    fun testZeroToExclusiveVar() {
        executeIntention("Use simpler range syntax")
    }

    fun testZeroToExclusiveVarInParentheses() {
        executeIntention("Use simpler range syntax")
    }

    fun testRangeSimplificationRegress1() {
        executeIntention("Use simpler range syntax")
    }

    fun testEVALOfVariable() {
        executeIntention("Add MONKEY")
    }

    fun testEVALOfInterpolation() {
        executeIntention("Add MONKEY")
    }

    fun testRoleMethodsStubbing() {
        executeIntention("Stub")
    }

    fun testRecursiveRoleMethodsStubbing() {
        executeIntention("Stub")
    }

    fun testMyVariableExport() {
        executeIntention("Change scope")
    }

    fun testRoleDoesClassConvertedToInheritance() {
        executeIntention("Replace \"does\"")
    }

    fun testUnitPrependingForNamedClass() {
        executeIntention("Add missing")
    }

    fun testUnitRemovalForDefinedGrammar() {
        executeIntention("Remove 'unit'")
    }

    fun testPrivateMethodStubbing() {
        executeIntention("Create")
    }

    fun testPrivateMethodStubbingWithoutEnclosingRoutine() {
        executeIntention("Create")
    }

    fun testPrivateMethodStubbingInNestedRoutines() {
        executeIntention("Create")
    }

    fun testPrivateMethodStubbingSignatureGeneration() {
        executeIntention("Create")
    }

    fun testPrivateMethodStubbingSignatureGenerationForSingleArg() {
        executeIntention("Create")
    }

    fun testPrivateMethodStubbingSignatureGenerationColonpair() {
        executeIntention("Create")
    }

    fun testPrivateMethodStubbingSignatureGenerationForSingleArgColonpair() {
        executeIntention("Create")
    }

    fun testPrivateMethodStubbingSignatureGenerationForNamedParameters() {
        executeIntention("Create")
    }

    fun testPrivateMethodStubbingFromUnfinishedVariable() {
        executeIntention("Create")
    }

    fun testOrderOfNamedVariablesInCallIsFixed() {
        executeIntention("Create")
    }

    fun testConflictResolutionAfterMoveSolved() {
        executeIntention("Create")
    }

    fun testPrivateMethodStubbingReformatsOnlyAddedBlock() {
        executeIntention("Create")
    }

    fun testPrivateMethodHasPrivateVariableArgumentUpdated() {
        executeIntention("Create")
    }

    fun testColonPairSimplification() {
        executeIntention("Convert")
    }

    fun testFatArrowSimplification() {
        executeIntention("Convert")
    }

    fun testWhileOneSimplification() {
        executeIntention("Use")
    }

    fun testWhileTrueSimplification() {
        executeIntention("Use")
    }

    fun testConstConstantKeywordFix() {
        executeIntention("Use")
    }

    fun testConstConstantVarFix() {
        executeIntention("Use")
    }

    fun testConstSubFix() {
        checkIntentionAbsence("Use")
    }

    fun testWithConstructionFix() {
        executeIntention("Use")
    }

    fun testPackageTypeChangeIntention() {
        executeIntention("Change")
    }

    fun testPackageTypeChangeIntoMonitorIntention() {
        ensureModuleIsLoaded("OO::Monitors")
        executeIntention("Change")
    }

    fun testPackageTypeChangeIntoMonitorPresent() {
        ensureModuleIsLoaded("OO::Monitors")
        executeIntention("Change")
    }

    fun testPackageTypeChangeOnTypeNameIntention() {
        executeIntention("Change")
    }

    fun testPackageTypeChangesInheritanceIntention() {
        executeIntention("Change")
    }

    fun testPackageTypeChangesInheritanceComposition() {
        executeIntention("Change")
    }

    fun testAttributeRequiredOnlyHas() {
        checkIntentionAbsence("Make required")
    }

    fun testAttributeRequiredNoDoubling() {
        checkIntentionAbsence("Make required")
    }

    fun testAttributeRequiredNoTraits() {
        executeIntention("Make required")
    }

    fun testAttributeRequiredTraits() {
        executeIntention("Make required")
    }

    fun testAttributeRequiredOnName() {
        executeIntention("Make required")
    }

    fun testAttributeRequiredWithDefault() {
        checkIntentionAbsence("Make required")
    }

    fun testWithoutConstructionFix() {
        executeIntention("Use")
    }

    fun testWithConstructionMultiFix() {
        executeIntention("Use")
    }

    fun testGrepFirstFixWhateverSingle() {
        executeIntention("Replace")
    }

    fun testGrepFirstFixWhateverMany() {
        executeIntention("Replace")
    }

    fun testGrepFirstFixBlockMany() {
        executeIntention("Replace")
    }

    fun testMakeMethodPublicIntention() {
        executeIntention("Make")
    }

    fun testMakeMethodPublicOnNameIntention() {
        executeIntention("Make")
    }

    fun testMakeMethodPublicIntentionIsForPrivateOnly() {
        checkIntentionAbsence("Make method public")
    }

    fun testMakeMethodPrivateIntention() {
        executeIntention("Make method private")
    }

    fun testMakeMethodSubmethod() {
        executeIntention("Make submethod")
    }

    fun testArrayInitializationRemoval() {
        executeIntention("Remove redundant")
    }

    fun testAwaitAllOfUnwrapArray() {
        executeIntention("Unwrap Promise.allof")
    }

    fun testAwaitAllOfUnwrapInfix() {
        executeIntention("Unwrap Promise.allof")
    }

    fun testAwaitAllOfUnwrapPrefix() {
        executeIntention("Unwrap Promise.allof")
    }

    fun testPerl6ExecutableStrFix() {
        executeIntention("Use \$*EXECUTABLE")
    }

    fun testUnparenSimple() {
        executeIntention("Remove parentheses")
    }

    fun testUnparenInfix() {
        executeIntention("Remove parentheses")
    }

    fun testUnparenInitializer() {
        executeIntention("Remove parentheses")
    }

    fun testEmptyUnparenNotAllowed() {
        checkIntentionAbsence("Remove parentheses")
    }

    fun testUnparenOnColonpairNotAllowed() {
        checkIntentionAbsence("Remove parentheses")
    }

    fun testBindingDestructuringFix() {
        executeIntention("Use binding")
    }

    fun testQuotesConversion1() {
        executeIntention("Convert to double")
    }

    fun testQuotesConversion2() {
        executeIntention("Convert to single")
    }

    fun testNoQuotesConversionForStr() {
        checkIntentionAbsence("Convert to double")
        checkIntentionAbsence("Convert to single")
    }

    fun testConditionalBlockConversion1() {
        executeIntention("Convert to block")
    }

    fun testConditionalBlockConversion2() {
        executeIntention("Convert to block")
    }

    fun testNonCapturingGroupIntoPos() { executeIntention("Convert into positional"); }

    fun testNonCapturingGroupIntoPosNonFlatting() {
        executeIntention("Convert into positional")
    }

    fun testNonCapturingGroupIntoNamed() { executeIntention("Convert into named"); }

    fun testPositionalCapturingIntoNamed() {
        executeIntention("Convert")
    }

    fun testTernaryStatementConversion() {
        executeIntention("Convert")
    }

    fun testTernaryExprConversion() {
        executeIntention("Convert")
    }

    fun testNoSplittingWithoutInitializer() {
        checkIntentionAbsence("Split into")
    }

    fun testSplittingDeclaration() {
        executeIntention("Split into")
    }

    fun testASCIIToUni() {
        executeIntention("Convert to Uni")
    }

    fun testUniToASCII() {
        executeIntention("Convert to ASCII")
    }

    fun testASCIICannotBeConvertedIntoASCII() {
        checkIntentionAbsence("Convert to ASCII")
    }

    fun testNoUnicodeEditingInStrings() {
        checkIntentionAbsence("Convert to Uni")
    }

    fun testASCIITermToUni() {
        executeIntention("Convert term to Unicode")
    }

    fun testUniTermToASCII() {
        executeIntention("Convert term to ASCII")
    }

    fun testHyperOpsComplete() {
        executeIntention("Convert to Unicode")
    }
    fun testHyperOpsIncomplete() {
        checkIntentionAbsence("Convert to Unicode")
    }
    fun testHyperOpsOnMap() {
        executeIntention("Convert to ASCII")
    }

    fun testUseDirectAttributeAccess() {
        executeIntention("Replace with direct access")
    }

    fun testFatarrowToColonpair() {
        executeIntention("Convert to")
    }

    fun testColonpairToFatarrow() {
        executeIntention("Convert to")
    }

    fun testColonpairToFatarrowParen() {
        executeIntention("Convert to")
    }

    fun testColonpairForms1() { executeIntention("Convert to "); }
    fun testColonpairForms2() { executeIntention("Convert to "); }
    fun testColonpairForms3() { executeIntention("Convert to "); }

    fun testSubStubbing() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "he<caret>he(42, :two, :\$bar);")
        val availableIntentions = myFixture.filterAvailableIntentions("Create")
        assertSize(1, availableIntentions)
        myFixture.launchAction(availableIntentions.get(0))
        myFixture.checkResult("sub hehe(\$p, :\$two, :\$bar) {}\nhehe(42, :two, :\$bar);")
    }

    fun testSubroutineDeletion() {
        executeIntention("Safe delete")
    }
    fun testMethodDeletion() {
        executeIntention("Safe delete")
    }
    fun testSubDeletionRemovesDocs() {
        executeIntention("Safe delete")
    }

    fun testSubroutineExportingQuickfix() {
        executeIntention("Add export")
    }

    fun testSubroutineExportingQuickfixWithTraits() {
        executeIntention("Add export")
    }

    fun testSubroutineExportingDoesNotWorkOnMethods() {
        checkIntentionAbsence("Add export")
    }

    fun testRemoveUnusedLocal() {
        executeIntention("Safe")
    }
    fun testRemoveUnusedAttribute() {
        executeIntention("Safe")
    }
    fun testRemoveUnusedAttributeRemovesDocs() {
        executeIntention("Safe")
    }
    fun testRemoveUnusedIsNotForParameters() {
        checkIntentionAbsence("Safe")
    }
    fun testRemoveUnusedIsNotForMultiAttributes() {
        checkIntentionAbsence("Safe")
    }

    private fun checkIntentionAbsence(hint: String) {
        assertNull(prepareIntention(hint))
    }

    private fun executeIntention(hint: String) {
        val intention = prepareIntention(hint)
        assertNotNull(intention)
        myFixture.launchAction(intention!!)
        myFixture.checkResultByFile(getTestName(false) + ".p6", true)
    }

    private fun prepareIntention(hint: String): IntentionAction? {
        myFixture.configureByFile(getTestName(false) + "Before.p6")
        val availableIntentions = myFixture.filterAvailableIntentions(hint)
        if (availableIntentions.size != 1 && java.lang.Boolean.getBoolean("raku.test.dump.actual")) {
            println("INTENTIONS ${getTestName(false)} hint=[$hint] matched=${availableIntentions.map { it.text }} " +
                    "all=${myFixture.availableIntentions.map { it.text }}")
        }
        assertTrue(availableIntentions.size == 1 || availableIntentions.isEmpty())
        return if (availableIntentions.isEmpty()) null else myFixture.findSingleIntention(hint)
    }
}
