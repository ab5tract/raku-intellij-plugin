package org.raku.comma.sdk

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import java.util.concurrent.atomic.AtomicBoolean
import org.raku.comma.RakuIcons
import org.raku.comma.project.RakuProjectKind
import org.raku.comma.psi.RakuFile
import org.raku.comma.psi.RakuPackageDecl
import org.raku.comma.psi.symbols.MOPSymbolsAllowed
import org.raku.comma.psi.symbols.RakuSingleResolutionSymbolCollector
import org.raku.comma.psi.symbols.RakuSymbolCollector
import org.raku.comma.psi.symbols.RakuSymbolKind
import org.raku.comma.services.project.RakuModuleInstallPrompt
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import javax.swing.Icon


class RakuSdkUtil {

    companion object {
        val LOG = Logger.getInstance(RakuSdkUtil::class.java)

        @JvmStatic
        val ICON: Icon = RakuIcons.CAMELIA

        @JvmStatic
        val BINARY_NAMES: Set<String> = setOf(
            "raku",
            "raku.exe",
            "rakudo",
            "rakudo.exe",
            "perl6",
            "perl6.bat",
            "perl6.exe",
        )

        @JvmStatic
        private var alreadyPrompted: AtomicBoolean = AtomicBoolean(false)

        @JvmStatic
        fun findRakuInSdkHome(home: String): String? {
            for (command in BINARY_NAMES) {
                val file = File(home, command)
                if (file.exists() && file.isFile && file.canExecute()) return file.absolutePath
            }
            return null
        }

        @JvmStatic
        fun isValidRakuSdkHome(home: String): Boolean {
            return findRakuInSdkHome(home) != null
        }

        @JvmStatic
        fun versionString(path: String): String? {
            val binPath = findRakuInSdkHome(path) ?: return null
            val command = arrayOf(binPath, "-e", "say $*RAKU.compiler.version")
            var line: String? = null
            val processBuilder = ProcessBuilder(*command)

            try {
                val process = processBuilder.start()
                InputStreamReader(process.inputStream, StandardCharsets.UTF_8).use { inIo ->
                    BufferedReader(inIo).use { processOutputReader ->
                        line = processOutputReader.readLine()
                        if (process.waitFor() != 0) return null
                    }
                }
            } catch (_: IOException) {
            } catch (_: InterruptedException) {
            }
            return line!!
        }

        @JvmStatic
        fun fileChooserDescriptor(): FileChooserDescriptor {
            return FileChooserDescriptor(false, true, false, false, false, false)
        }

        @JvmStatic
        @Synchronized
        fun reactToSdkIssue(project: Project?, title: String, message: String? = null, ex: Exception? = null) {
            val finalMessage = message ?: if (ex != null) ex.message!! else ""

            if (ex != null) {
                LOG.error(ex)
            }

            // Only the notification is suppressed, and only where we can see
            // it is not a Raku project -- the exception above is still logged,
            // because a log entry is diagnostics rather than noise. A null
            // project cannot be attributed to one, and silencing that would
            // hide a genuine global SDK problem.
            if (project != null && ! RakuProjectKind.hasRakuFiles(project)) return

            if (! alreadyPrompted.get()) {
                alreadyPrompted.set(true)
                val notification = NotificationGroupManager.getInstance()
                    .getNotificationGroup("raku.sdk.errors.group")
                    .createNotification(title, finalMessage, NotificationType.WARNING)

                Notifications.Bus.notify(notification, project)
            }
        }

        @JvmStatic
        fun triggerCodeAnalysis(project: Project) {
            ApplicationManager.getApplication().runReadAction {
                if (project.isDisposed) return@runReadAction
                val editors = FileEditorManager.getInstance(project).selectedEditors
                for (editor in editors) {
                    if (editor != null && editor.file != null) {
                        val psiFile = PsiManager.getInstance(project).findFile(editor.file)
                        if (psiFile != null) {
                            DaemonCodeAnalyzer.getInstance(project).restart(psiFile)
                        }
                    }
                }
            }
        }

        // This doesn't require any direct access to the underlying caching layers, so we have put it here instead
        // of in RakuGlobalSdkSymbolCache or RakuProjectSdkService
        @JvmStatic
        fun contributeParentSymbolsFromCore(
            collector: RakuSymbolCollector,
            coreSetting: RakuFile,
            parentName: String,
            allowed: MOPSymbolsAllowed
        ) {
            val parentCollector = RakuSingleResolutionSymbolCollector(parentName, RakuSymbolKind.TypeOrConstant)

            coreSetting.contributeGlobals(parentCollector, HashSet())
            if (parentCollector.isSatisfied) {
                val decl = parentCollector.result.psi as RakuPackageDecl
                decl.contributeMOPSymbols(collector, allowed)
            }
        }
    }
}