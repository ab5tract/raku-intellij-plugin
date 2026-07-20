package org.raku.comma.inspection.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.containers.stream
import org.raku.comma.inspection.InspectionConstants.MissingRoleMethod.DESCRIPTION_FORMAT
import org.raku.comma.inspection.InspectionConstants.MissingRoleMethod.SPLIT_REGEX
import org.raku.comma.inspection.RakuInspection
import org.raku.comma.inspection.fixes.StubMissingMethodsFix
import org.raku.comma.psi.*

class MissingRoleMethodInspection : RakuInspection() {

    override fun provideVisitFunction(holder: ProblemsHolder, element: PsiElement) {
        if (element !is RakuPackageDecl) return

        if (element.packageKind != "class") return

        val traits = element.traits
        if (traits.size == 0) return

        val methodsToImplement: MutableMap<String, Pair<Int, String>> = HashMap()
        val seen: MutableMap<String, Int> = HashMap()
        gatherRoleStubs(traits, methodsToImplement, seen, 0)

        val declarations = element.getDeclarations()
        for (decl in declarations) {
            if (decl is RakuRoutineDecl && decl.routineKind == "method") {
                methodsToImplement.remove(decl.routineName)
            } else if (decl is RakuVariableDecl) {
                if (decl.getScope() != "has") continue
                val names = decl.variableNames
                names.stream().filter  { name -> RakuVariable.getTwigil(name) == '.' }
                              .forEach { name -> methodsToImplement.remove(name.substring(2)) }

                val attrTraits = decl.getTraits()
                attrTraits.stream().filter { trait -> trait.traitModifier == "handles" }
                    .forEach { trait ->
                        val handleMethods = trait.traitName
                        handleMethods.split(SPLIT_REGEX)
                                     .dropLastWhile { it.isEmpty() }
                                     .forEach { method -> methodsToImplement.remove(method) }
                    }
            }
        }

        if (methodsToImplement.isNotEmpty()) {
            listOf("a","b").joinToString()
            val names = methodsToImplement.keys.joinToString(", ")
            // Get the end position of the declaration statement itself, rather than highlighting the entire class
            val lastChild = element.children.stream().filter { it !is RakuBlockoid }.toList().lastOrNull() ?: return
            // rangeInElement is relative to the element, not the file
            val end = lastChild.textRange.endOffset - element.textRange.startOffset
            // Block is not yet typed

            val description = DESCRIPTION_FORMAT.format(names)
            val toImplement = methodsToImplement.values.stream().map { it.second }.toList()
            holder.registerProblem(element, TextRange(0, end), description, StubMissingMethodsFix(toImplement))
        }
    }

    private fun gatherRoleStubs(
        traits: List<RakuTrait>,
        methodsToImplement: MutableMap<String, Pair<Int, String>>,
        seen: MutableMap<String, Int>,
        level: Int
    ) {
        for (trait in traits) {
            if (trait.traitModifier == "does") {
                val type = trait.compositionTypeName ?: continue
                val ref = type.reference ?: continue
                val roleDeclaration = ref.resolve() as? RakuPackageDecl ?: continue
                val declarations = roleDeclaration.getDeclarations()
                for (maybeMethod in declarations) {
                    if (maybeMethod !is RakuRoutineDecl) continue
                    if (maybeMethod.routineKind != "method" || maybeMethod.getParent() is RakuMultiDecl) continue
                    if (maybeMethod.isStubbed) {
                        // If method is not indexed or we saw it, and it was not closer to root class than current stub,
                        // add it to candidates for stubbing
                        val isIndexed = seen.containsKey(maybeMethod.routineName)
                        if ((!isIndexed || seen[maybeMethod.routineName]!! > level)) {
                            methodsToImplement[maybeMethod.routineName] = Pair.create(level, maybeMethod.text)
                        }
                    } else {
                        // If this method was planned to be stubbed, but now we see an implementation
                        // with level closer to equal relatively to root class, do not stub it
                        val value = methodsToImplement[maybeMethod.routineName]
                        if (value != null && value.first >= level) {
                            methodsToImplement.remove(maybeMethod.routineName)
                        }
                        seen[maybeMethod.routineName] = level
                    }
                }
                val innerTraits = roleDeclaration.traits
                gatherRoleStubs(innerTraits, methodsToImplement, seen, level + 1)
            }
        }
    }
}