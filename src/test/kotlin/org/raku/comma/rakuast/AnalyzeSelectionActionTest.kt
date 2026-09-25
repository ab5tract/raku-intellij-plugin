package org.raku.comma.rakuast

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.testFramework.TestActionEvent
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

// Without this gating, "Analyze Selection as RakuAST" showed up in the
// right-click menu of every editor in the IDE -- Java, JSON, Markdown, plain
// text -- and was enabled whenever text happened to be selected, shelling
// out to Raku on non-Raku source.
class AnalyzeSelectionActionTest : CommaFixtureTestCase() {

    private fun eventFor(action: AnalyzeSelectionAction): com.intellij.openapi.actionSystem.AnActionEvent {
        val dataContext = DataContext { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.EDITOR.name -> myFixture.editor
                CommonDataKeys.PSI_FILE.name -> myFixture.file
                else -> null
            }
        }
        return TestActionEvent.createTestEvent(action, dataContext)
    }

    // update() asks for PSI_FILE, which the action system provides by rules and
    // refuses to compute on the EDT -- declaring EDT logged "'psi.File' is
    // requested on EDT ... See ActionUpdateThread javadoc" on every right-click.
    fun testUpdateRunsOnBackgroundThread() {
        assertEquals(ActionUpdateThread.BGT, AnalyzeSelectionAction().actionUpdateThread)
    }

    fun testEnabledAndVisibleOnRakuFileWithSelection() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 41;")
        myFixture.editor.selectionModel.setSelection(0, 4)

        val action = AnalyzeSelectionAction()
        val event = eventFor(action)
        action.update(event)

        assertTrue(event.presentation.isEnabledAndVisible)
    }

    fun testHiddenOnRakuFileWithoutSelection() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$x = 41;")
        // No selection made.

        val action = AnalyzeSelectionAction()
        val event = eventFor(action)
        action.update(event)

        assertFalse(event.presentation.isEnabledAndVisible)
    }

    fun testHiddenOnNonRakuFileEvenWithSelection() {
        myFixture.configureByText(PlainTextFileType.INSTANCE, "just some text")
        myFixture.editor.selectionModel.setSelection(0, 4)

        val action = AnalyzeSelectionAction()
        val event = eventFor(action)
        action.update(event)

        assertFalse(
            "the action must not be enabled or visible outside Raku files",
            event.presentation.isEnabledAndVisible
        )
    }
}
