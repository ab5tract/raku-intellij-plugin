package org.raku.comma.psi;

import com.intellij.psi.PsiElement;
import org.raku.comma.psi.type.RakuType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public interface RakuSignature extends RakuPsiElement {
    String summary(RakuType type);
    RakuParameter[] getParameters();

    default SignatureCompareResult acceptsArguments(PsiElement[] argsArray, boolean isCompleteCall, boolean isMethodCall) {
        return RakuArityMatcher.acceptsArguments(this, argsArray, isCompleteCall, isMethodCall);
    }

    enum MatchFailureReason {
        NOT_ENOUGH_ARGS,
        TOO_MANY_ARGS,
        SURPLUS_NAMED,
        TYPE_MISMATCH,
        CONSTRAINT_MISMATCH,
        MISSING_REQUIRED_NAMED
    }

    class SignatureCompareResult {
        private boolean isAccepted;
        private final Map<Integer, Integer> argToParam = new HashMap<>();
        private final Map<Integer, MatchFailureReason> failures = new HashMap<>();
        // Extra detail for a failure, e.g. the name of a missing required
        // named parameter. Keyed by argument index like `failures`, rather
        // than stashed on the (singleton, shared) MatchFailureReason enum
        // constant itself -- a signature with two missing required named
        // parameters used to have the second overwrite the first's name
        // before it was ever read.
        private final Map<Integer, String> failureDetails = new HashMap<>();
        private int nextParameterIndex;

        public SignatureCompareResult(boolean isAccepted) {
            this.isAccepted = isAccepted;
        }

        public void setAccepted(boolean accepted) {
            isAccepted = accepted;
        }

        public boolean isAccepted() {
            return isAccepted;
        }

        public void setParameterIndexOfArg(int argIndex, int paramIndex) {
            argToParam.put(argIndex, paramIndex);
        }

        public int getParameterIndexOfArg(int argumentIndex) {
            return argToParam.getOrDefault(argumentIndex, -1);
        }

        public void setFailureForArg(int argIndex, MatchFailureReason reason) {
            failures.put(argIndex, reason);
        }

        @Nullable
        public MatchFailureReason getArgumentFailureReason(int argumentIndex) {
            return failures.getOrDefault(argumentIndex, null);
        }

        public void setFailureDetail(int argIndex, @Nullable String detail) {
            if (detail != null) {
                failureDetails.put(argIndex, detail);
            }
        }

        @Nullable
        public String getFailureDetail(int argumentIndex) {
            return failureDetails.getOrDefault(argumentIndex, null);
        }

        public void incrementNextParameter() {
            nextParameterIndex++;
        }

        public void setNextParameter(int index) {
            nextParameterIndex = index;
        }

        public int getNextParameterIndex() {
            return nextParameterIndex;
        }
    }
}
