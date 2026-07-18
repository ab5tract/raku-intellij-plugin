package org.raku.comma

import com.intellij.psi.PsiElement
import org.raku.comma.refactoring.introduce.IntroduceOperation
import org.raku.comma.refactoring.introduce.IntroduceValidator
import org.raku.comma.refactoring.introduce.constant.RakuIntroduceConstantHandler

class RakuConstantExtractionHandlerMock(validator: IntroduceValidator?, private val myName: String) :
    RakuIntroduceConstantHandler(validator, "Extract mock") {

    override fun performActionOnElementOccurrences(operation: IntroduceOperation) {
        operation.name = myName
        operation.isReplaceAll = true
        val anchors: List<PsiElement> = calculateAnchors(operation, operation.element, operation.occurrences)
        operation.anchor = anchors[0]
        val declaration = performRefactoring(operation)
        removeLeftoverStatement(operation)
        val editor = operation.editor
        editor.caretModel.moveToOffset(declaration.textRange.endOffset)
        editor.selectionModel.removeSelection()
    }
}
