package org.raku.comma.actions

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.Pair
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.Function
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.swing.JComponent
import javax.swing.JPanel

class UpdateExtensionsAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project: Project = event.project ?: error("Action event not associated with a project: $event")

        val filesToUpdate: MutableMap<String, MutableList<File>> = collectFilesWithLegacyNames(project)
        if (filesToUpdate.isEmpty()) {
            Notifications.Bus.notify(
                Notification(
                    "raku.messages",
                    "No legacy extensions detected",
                    NotificationType.INFORMATION
                )
            )
            return
        }

        RakuChooseExtensionsToUpdateDialog(project, filesToUpdate).show()
    }

    override fun update(event: AnActionEvent) {
        event.presentation.setEnabled(event.project != null)
    }

    class RakuChooseExtensionsToUpdateDialog(
        private val myProject: Project?,
        private val filesToUpdate: MutableMap<String, MutableList<File>>
    ) : DialogWrapper(
        myProject, true
    ) {
        private val exts: MutableList<StringItem?> = ContainerUtil.map<String, StringItem?>(
            filesToUpdate.keys, Function { file: String -> UpdateExtensionsAction.StringItem(file) })
        private var myTable: JBTable? = null
        private var myModel: ListTableModel<StringItem?>? = null


        init {
            init()
        }

        override fun createCenterPanel(): JComponent? {
            val panel = JPanel(MigLayout())
            myModel = ListTableModel<StringItem?>(
                arrayOf<ColumnInfo<*, *>>(
                    object : ColumnInfo<StringItem, Boolean>("Update") {
                        override fun valueOf(item: StringItem): Boolean {
                            return item.isSelected
                        }

                        override fun isCellEditable(item: StringItem?): Boolean {
                            return true
                        }

                        override fun getColumnClass(): Class<*> {
                            return Boolean::class.java
                        }

                        override fun setValue(item: StringItem, value: Boolean) {
                            item.isSelected = value
                        }
                    },
                    object : ColumnInfo<StringItem, String>("Name") {
                        override fun valueOf(item: StringItem): String {
                            return String.format(".%s -> .%s", item.ext, nonLegacyExts[item.ext])
                        }
                    }
                ), this.exts
            )
            myTable = JBTable(myModel)
            panel.minimumSize = Dimension(400, 300)
            panel.add(JBScrollPane(myTable), "growx, growy, pushx, pushy")
            return panel
        }

        override fun doOKAction() {
            val failedToProcess: MutableList<Pair<Path?, String?>> = ArrayList<Pair<Path?, String?>>()
            WriteCommandAction.runWriteCommandAction(myProject, "Renaming...", null, Runnable {
                for (i in 0..<myTable!!.getRowCount()) {
                    if (myTable!!.getValueAt(i, 0) is Boolean && (myTable!!.getValueAt(i, 0) as Boolean)) {
                        val ext = myModel!!.getItem(i)!!.ext
                        val files: MutableList<File> = filesToUpdate[ext]!!
                        for (file in files) {
                            val target = file.toPath()
                            val newName =
                                target.fileName.toString().replace(".$ext", "." + nonLegacyExts[ext])
                            try {
                                val vf = LocalFileSystem.getInstance().findFileByIoFile(file)
                                vf?.rename(this, newName)
                            } catch (e: IOException) {
                                failedToProcess.add(Pair.create<Path?, String?>(target, newName))
                            }
                        }
                    }
                }
            })
            LocalFileSystem.getInstance().refresh(false)
            close(0)
            if (!failedToProcess.isEmpty()) FailureDialog(failedToProcess).show()
        }

        private inner class FailureDialog(private val filesToShow: MutableList<Pair<Path?, String?>>) :
            DialogWrapper(myProject, true) {
            init {
                init()
            }

            override fun createCenterPanel(): JComponent? {
                val joiner = StringJoiner("<br>")
                joiner.add("Could not rename those files:<br>")
                filesToShow.forEach { pair ->
                    joiner.add(pair.first.toString() + " -> " + pair.second)
                }
                return JBLabel("<html>$joiner</html>")
            }
        }
    }

    private class StringItem(val ext: String) {
        var isSelected: Boolean = ext != "pod" && ext != "pm"
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    companion object {
        // One source of truth: the keys drive the extension list, which drives
        // the pattern. Previously all three were maintained by hand, and a
        // legacy extension missing from this map renamed a file to `*.null` --
        // no exception, and irreversible. Declaration order matters:
        // companion properties initialise top to bottom, and the alternation
        // order matters too (pm6 before pm, pod6 before pod), which is why
        // this is a linkedMapOf.
        private val nonLegacyExts: Map<String, String> = linkedMapOf(
            "p6" to "raku", "pl6" to "raku",
            "pm6" to "rakumod", "pm" to "rakumod",
            "pod6" to "rakudoc", "pod" to "rakudoc",
            "t" to "rakutest",
        )
        private val LEGACY_EXTENSIONS = nonLegacyExts.keys.toList()

        val FULL_LEGACY_EXTENSION_PATTERN: Pattern =
            Pattern.compile(".+?\\.(" + LEGACY_EXTENSIONS.joinToString("|") + ")")

        /**
         * Index-backed, so it honours excluded folders. The previous
         * implementation handed a java.io.File to FileUtil.findFilesByMask,
         * which cannot know what IntelliJ excludes -- on a rakudo checkout `t`
         * is a source root, so it walked all 2158 files of the excluded
         * t/spec.
         */
        @JvmStatic
        fun collectFilesWithLegacyNames(project: Project): MutableMap<String, MutableList<File>> {
            // FilenameIndex asserts a read action, and this is public: the
            // startup detector calls it off a coroutine dispatcher and
            // actionPerformed calls it from the EDT. Held here so no caller
            // has to know. Nested read actions are reentrant, so the
            // detector's own smartReadAction costs nothing extra.
            return ReadAction.compute<MutableMap<String, MutableList<File>>, RuntimeException> {
                val filesToUpdate: MutableMap<String, MutableList<File>> = HashMap()
                val scope = GlobalSearchScope.projectScope(project)
                for (ext in LEGACY_EXTENSIONS) {
                    for (vf in FilenameIndex.getAllFilesByExt(project, ext, scope)) {
                        val matcher: Matcher = FULL_LEGACY_EXTENSION_PATTERN.matcher(vf.name)
                        if (matcher.matches()) {
                            val key = matcher.group(1)
                            filesToUpdate.computeIfAbsent(key) { mutableListOf() }.add(File(vf.path))
                        }
                    }
                }
                filesToUpdate
            }
        }
    }
}
