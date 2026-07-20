package org.raku.comma.signatures

import com.intellij.psi.util.PsiTreeUtil
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.psi.RakuElementFactory
import org.raku.comma.psi.RakuSignature
import org.raku.comma.psi.RakuSubCall

class RakuSignatureComparatorTest : CommaFixtureTestCase() {
    private fun doTest(
        sig: String,
        args: String,
        asserts: (RakuSignature.SignatureCompareResult) -> Unit,
    ) {
        doTest(sig, args, true, asserts)
    }

    private fun doTest(
        sig: String,
        args: String,
        isCompleteCall: Boolean,
        asserts: (RakuSignature.SignatureCompareResult) -> Unit,
    ) {
        val signature = PsiTreeUtil.findChildOfType(
            RakuElementFactory.createStatementFromText(project, String.format("sub (%s) {}", sig)),
            RakuSignature::class.java
        )
        assertNotNull(signature)
        val call = PsiTreeUtil.findChildOfType(
            RakuElementFactory.createStatementFromText(project, String.format("a(%s);", args)),
            RakuSubCall::class.java
        )
        val result = signature!!.acceptsArguments(call!!.callArguments, isCompleteCall, false)
        asserts(result)
    }

    private fun assertArgument(
        res: RakuSignature.SignatureCompareResult,
        argumentIndex: Int,
        parameterIndex: Int,
        reason: RakuSignature.MatchFailureReason?,
    ) {
        assertEquals(parameterIndex, res.getParameterIndexOfArg(argumentIndex))
        assertEquals(reason, res.getArgumentFailureReason(argumentIndex))
    }

    fun testSingleArgSignature() {
        doTest("\$a", "42", { res ->
            assertTrue(res.isAccepted())
            assertEquals(1, res.getNextParameterIndex())
            assertArgument(res,0,0, null)
        })
    }

    fun testNamedArgSignature() {
        doTest(":\$abc", ":abc", { res ->
            assertTrue(res.isAccepted())
            assertEquals(1, res.getNextParameterIndex())
            assertArgument(res, 0, 0, null)
        })
    }

    fun testNamedArgSignatureNoMatch() {
        doTest(":\$abc", "", { res ->
            assertTrue(res.isAccepted())
            assertEquals(0, res.getNextParameterIndex())
            assertArgument(res, 0, -1, null)
        })
    }

    fun testNamedArgSignatureSurplusArg() {
        doTest(":\$abc", ":nono", { res ->
            assertFalse(res.isAccepted())
            assertEquals(0, res.getNextParameterIndex())
            assertArgument(res, 0, -1, RakuSignature.MatchFailureReason.SURPLUS_NAMED)
        })
    }

    fun testRequiredNamed() {
        doTest(":\$abc!", "", { res ->
            assertFalse(res.isAccepted())
            assertEquals(0, res.getNextParameterIndex())
            assertArgument(res, 0, -1, RakuSignature.MatchFailureReason.MISSING_REQUIRED_NAMED)
        })
        doTest(":\$abc!", ":nonono", { res ->
            assertFalse(res.isAccepted())
            assertEquals(0, res.getNextParameterIndex())
            assertArgument(res, 0, -1, RakuSignature.MatchFailureReason.SURPLUS_NAMED)
        })
    }

    fun testTwoMissingRequiredNamedArgsReportDistinctNames() {
        // Regression: MatchFailureReason.MISSING_REQUIRED_NAMED used to carry
        // the missing parameter's name on a mutable field of the shared enum
        // singleton (MatchFailureReason.MISSING_REQUIRED_NAMED itself, one
        // instance for the whole JVM). Two missing required named parameters
        // recorded at different positions would both read back whichever name
        // was written last, since getArgumentFailureReason(k) for either
        // position returns that same shared instance. The name is now
        // tracked per argument index on the result instead.
        doTest(":\$abc!, \$b, :\$def!", "2", { res ->
            assertFalse(res.isAccepted())
            assertEquals(
                RakuSignature.MatchFailureReason.MISSING_REQUIRED_NAMED,
                res.getArgumentFailureReason(0),
            )
            assertEquals("\$abc", res.getFailureDetail(0))
            assertEquals(
                RakuSignature.MatchFailureReason.MISSING_REQUIRED_NAMED,
                res.getArgumentFailureReason(1),
            )
            assertEquals("\$def", res.getFailureDetail(1))
        })
    }

    fun testSurplus() {
        doTest("\$a, \$b?, *@a", "1, 2, 3, 4", { res ->
            assertTrue(res.isAccepted())
            assertEquals(2, res.getNextParameterIndex())
            assertArgument(res, 0, 0, null)
            assertArgument(res, 1, 1, null)
            for (i in 2 until 4)
                assertArgument(res, i, 2, null)
        })
    }

    fun testNamedSurplus() {
        doTest(":\$a!, :\$b, *%rest", ":a, :aa, :bb, :cc", { res ->
            assertTrue(res.isAccepted())
            assertEquals(2, res.getNextParameterIndex())
            assertArgument(res, 0, 0, null)
            for (i in 1 until 4)
                assertArgument(res, i, 2, null)
        })
    }

    fun testComplexSignature() {
        doTest("\$a, @b, \$d?, :\$one, :\$two!, *@rest, *%rest", "42, (1,2,3), :two, 'rest', 'rest', :rest, :rest2", { res ->
            assertTrue(res.isAccepted())
            assertEquals(6, res.getNextParameterIndex())
            assertArgument(res, 0, 0, null)
            assertArgument(res, 1, 1, null)
            assertArgument(res, 2, 4, null)
            assertArgument(res, 3, 2, null)
            assertArgument(res, 4, 5, null)
            assertArgument(res, 5, 6, null)
            assertArgument(res, 6, 6, null)
        })
        doTest("\$a, @b, :\$one, :\$two!, *@rest, *%rest", "42, (1,2,3), :two, :one, :rest1, :rest2, :!rest3, 'rest', 'rest'", { res ->
            assertTrue(res.isAccepted())
            assertEquals(5, res.getNextParameterIndex())
            assertArgument(res, 0, 0, null)
            assertArgument(res, 1, 1, null)
            assertArgument(res, 2, 3, null)
            assertArgument(res, 3,2, null)
            assertArgument(res, 4, 5, null)
            assertArgument(res, 5, 5, null)
            assertArgument(res, 6, 5, null)
            assertArgument(res, 7, 4, null)
            assertArgument(res, 8, 4, null)
        })
    }

    fun testIncompleteCalls() {
        doTest("\$a, *@foo", "", false, { res ->
            assertTrue(res.isAccepted())
            assertEquals(1, res.getNextParameterIndex())
        })
        doTest("\$a?", "", true, { res -> assertTrue(res.isAccepted()) })
        doTest("\$a", "", false, { res -> assertTrue(res.isAccepted()) })
        doTest("\$a, \$b", "", false, { res -> assertTrue(res.isAccepted()) })
        doTest("\$a, \$b", "42", false, { res -> assertTrue(res.isAccepted()) })
        doTest("\$a, :\$foo", "42", false, { res -> assertTrue(res.isAccepted()) })
        doTest(":\$a", ":a", false, { res -> assertTrue(res.isAccepted()) })
        doTest(":\$a", ":b", false, { res -> assertFalse(res.isAccepted()) })
        doTest("@a", "(1,2,3)", false, { res -> assertTrue(res.isAccepted()) })
        doTest("\$a, *%b", "1, 2, :b", false, { res -> assertFalse(res.isAccepted()) })
        doTest("\$a, :\$abc!", "1", false, { res -> assertTrue(res.isAccepted()) })
    }

    // TODO literal comparison
    // TODO constraints comparison
    // TODO type comparison
}
