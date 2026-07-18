package org.raku.comma

import com.intellij.psi.PsiElement
import org.raku.comma.refactoring.introduce.IntroduceOperation
import org.raku.comma.refactoring.introduce.IntroduceValidator
import org.raku.comma.refactoring.introduce.variable.RakuIntroduceVariableHandler

class RakuVariableExtractionHandlerMock(validator: IntroduceValidator?, private val myName: String) :
    RakuIntroduceVariableHandler(validator, "Extract Mock") {

    var replaceAll: Boolean = true

    override fun performActionOnElementOccurrences(operation: IntroduceOperation) {
        operation.name = myName
        operation.isReplaceAll = replaceAll
        val anchors: List<PsiElement> = calculateAnchors(operation, operation.element, operation.occurrences)
        operation.anchor = anchors[0]
        val declaration = performRefactoring(operation)
        removeLeftoverStatement(operation)
        val editor = operation.editor
        editor.caretModel.moveToOffset(declaration.textRange.endOffset)
        editor.selectionModel.removeSelection()
    }
}
