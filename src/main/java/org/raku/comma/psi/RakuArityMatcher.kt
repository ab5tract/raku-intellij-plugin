package org.raku.comma.psi

import com.intellij.psi.PsiElement

/**
 * Matches call arguments against a routine signature's parameters: positional,
 * named, slurpy positional/named, and required/optional counting. Pulled out
 * of RakuSignature's default methods into a standalone class so the algorithm
 * has a home of its own to review and test, instead of hiding inside a
 * 90-line interface default method.
 */
object RakuArityMatcher {

    @JvmStatic
    fun acceptsArguments(
        signature: RakuSignature,
        argsArray: Array<PsiElement>,
        isCompleteCall: Boolean,
        isMethodCall: Boolean,
    ): RakuSignature.SignatureCompareResult {
        val arguments = argsArray.toList()
        val parameters = signature.parameters.toList()

        if (parameters.isEmpty() && arguments.isEmpty()) {
            return RakuSignature.SignatureCompareResult(true)
        }

        val result = RakuSignature.SignatureCompareResult(true)

        // Split all available arguments into positional and named ones.
        val positionalArgs = ArrayList<PsiElement>()
        val namedArgs = HashMap<String?, PsiElement>()
        categorizeArguments(arguments, positionalArgs, namedArgs)

        // Index of the next available positional argument.
        var posArgIndex = 0
        // Named parameters we have already matched.
        val seen = HashSet<String?>()

        for (parameterIndex in parameters.indices) {
            val parameter = parameters[parameterIndex]

            // Signature (|) or (|c) accepts everything.
            if (parameter.text.orEmpty().startsWith("|")) {
                return result
            }

            if (parameter.isPositional) {
                if (parameter.isSlurpy) {
                    // Eat the rest of the positionals, then move on to see if
                    // there is a named slurpy ahead.
                    posArgIndex = eatPositionalSlurpy(arguments, result, positionalArgs, posArgIndex, parameterIndex)
                    continue
                }

                if (positionalArgs.size > posArgIndex) {
                    val positionalArg = positionalArgs[posArgIndex++]
                    // TODO check positional argument for constraints
                    result.setParameterIndexOfArg(arguments.indexOf(positionalArg), parameterIndex)
                    result.incrementNextParameter()
                } else if (!parameter.isOptional && isCompleteCall) {
                    failMatch(result, posArgIndex, RakuSignature.MatchFailureReason.NOT_ENOUGH_ARGS)
                }
            } else if (parameter.isNamed) {
                if (parameter.isSlurpy) {
                    // Eat the rest of the named ones, then move on to see if
                    // there is a positional slurpy ahead.
                    eatNamedSlurpy(arguments, result, namedArgs, seen, parameterIndex)
                    continue
                }

                val namedParameterName = prepareParamName(parameter.variableName)
                val namedArgumentForParameter = namedArgs[namedParameterName]
                if (namedArgumentForParameter != null) {
                    // TODO Check named argument for constraints
                    seen.add(namedParameterName)
                    result.setParameterIndexOfArg(arguments.indexOf(namedArgumentForParameter), parameterIndex)
                    result.incrementNextParameter()
                } else if (!parameter.isOptional && isCompleteCall) {
                    failMatch(
                        result, posArgIndex, RakuSignature.MatchFailureReason.MISSING_REQUIRED_NAMED,
                        detail = parameter.variableName,
                    )
                }
            }
        }

        // If we iterated over all parameters but there are still arguments
        // left, disallow the match and mark them as errors. Methods allow
        // surplus named arguments.
        if (!isMethodCall) {
            seen.forEach(namedArgs::remove)
            for (name in namedArgs.keys) {
                failMatch(result, arguments.indexOf(namedArgs[name]), RakuSignature.MatchFailureReason.SURPLUS_NAMED)
            }
        }

        // Check surplus positional arguments.
        val positionalLeftovers = positionalArgs.subList(posArgIndex, positionalArgs.size)
        for (leftover in positionalLeftovers) {
            failMatch(result, arguments.indexOf(leftover), RakuSignature.MatchFailureReason.TOO_MANY_ARGS)
        }

        return result
    }

    private fun failMatch(
        result: RakuSignature.SignatureCompareResult,
        argIndex: Int,
        reason: RakuSignature.MatchFailureReason,
        detail: String? = null,
    ) {
        result.setAccepted(false)
        result.setFailureForArg(argIndex, reason)
        result.setFailureDetail(argIndex, detail)
    }

    /** Splits arguments into positional and named (RakuColonPair/RakuFatArrow) buckets. */
    private fun categorizeArguments(
        arguments: List<PsiElement>,
        positionalArgs: MutableList<PsiElement>,
        namedArgs: MutableMap<String?, PsiElement>,
    ) {
        for (arg in arguments) {
            when (arg) {
                is RakuColonPair -> namedArgs[arg.key] = arg
                is RakuFatArrow -> namedArgs[arg.key] = arg
                else -> positionalArgs.add(arg)
            }
        }
    }

    private fun eatPositionalSlurpy(
        arguments: List<PsiElement>,
        result: RakuSignature.SignatureCompareResult,
        positionalArgs: List<PsiElement>,
        posArgIndex: Int,
        parameterIndex: Int,
    ): Int {
        for (rest in positionalArgs.subList(posArgIndex, positionalArgs.size)) {
            result.setParameterIndexOfArg(arguments.indexOf(rest), parameterIndex)
        }
        result.setNextParameter(parameterIndex)
        return positionalArgs.size
    }

    private fun eatNamedSlurpy(
        arguments: List<PsiElement>,
        result: RakuSignature.SignatureCompareResult,
        namedArgs: Map<String?, PsiElement>,
        seen: MutableSet<String?>,
        parameterIndex: Int,
    ) {
        for (rest in namedArgs.keys) {
            if (rest !in seen) {
                result.setParameterIndexOfArg(arguments.indexOf(namedArgs[rest]), parameterIndex)
            }
        }
        seen.addAll(namedArgs.keys)
        result.setNextParameter(parameterIndex)
    }

    /** Strips sigils/twigils and a trailing `!`/`?` off a named parameter's variable name. */
    private fun prepareParamName(variableName: String): String {
        var start = 0
        var end = 0
        if (variableName.length <= 1) return variableName
        for (i in variableName.indices) {
            when (variableName[i]) {
                ':', '$', '@', '%', '&' -> start = i + 1
                '?', '!' -> {
                    end = i
                    return variableName.substring(start, end + 1)
                }
                else -> end = i
            }
        }
        return variableName.substring(start, end + 1)
    }
}
