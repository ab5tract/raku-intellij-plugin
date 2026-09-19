package org.raku.comma.rakuast

import com.intellij.execution.ExecutionException
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.raku.comma.utils.RakuCommandLine
import org.raku.comma.utils.RakuUtils
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

@Service(Service.Level.PROJECT)
class RakuAstService(private val project: Project) {

    /**
     * True when the project root is usable as a Raku distribution root, which
     * is what `-I.` and `-Ilib` need in order to resolve the selection's
     * imports. Both are relative to the working directory, which [run] sets to
     * the project base path -- so if that is not a distribution root, context
     * cannot work and the caller should fall back to a bare selection.
     */
    fun supportsFileContext(): Boolean {
        val base = project.basePath?.let(::File) ?: return false
        if (!base.isDirectory) return false
        // META6.json lets `-I.` resolve through the distribution's `provides`;
        // a lib/ directory lets `-Ilib` resolve by convention. Either will do.
        return File(base, "META6.json").isFile || File(base, "lib").isDirectory
    }

    /**
     * @param context the file's `use`/`need` statements, compiled in front of
     *   the selection so its imports resolve. Pass an empty list for a bare,
     *   context-free analysis.
     */
    fun analyze(source: String, context: List<String> = emptyList()): AnalyzeResult {
        val sourceFile = writeTemp("rakuast-source", source)
            ?: return AnalyzeResult(error = "Could not write a temporary file for the snippet.")
        val contextFile = context
            .takeIf { it.isNotEmpty() }
            ?.let { statements ->
                writeTemp("rakuast-context", statements.joinToString("\n"))
                    ?: run {
                        sourceFile.delete()
                        return AnalyzeResult(
                            error = "Could not write a temporary file for the file context.")
                    }
            }
        return try {
            val args = mutableListOf("analyze", sourceFile.absolutePath)
            contextFile?.let { args.add(it.absolutePath) }
            AstJson.decodeAnalyze(run(args))
        } finally {
            sourceFile.delete()
            contextFile?.delete()
        }
    }

    /**
     * One node's `.gist`.
     *
     * @param context MUST be the same list passed to the [analyze] call that
     *   produced [path], for the same reason as [edit]: a different context
     *   yields a different tree, and the path would walk elsewhere.
     */
    fun gist(source: String, path: List<Int>, context: List<String> = emptyList()): GistResult {
        val sourceFile = writeTemp("rakuast-source", source)
            ?: return GistResult(error = "Could not write a temporary file for the snippet.")
        val contextFile = context
            .takeIf { it.isNotEmpty() }
            ?.let { statements ->
                writeTemp("rakuast-context", statements.joinToString("\n"))
                    ?: run {
                        sourceFile.delete()
                        return GistResult(
                            error = "Could not write a temporary file for the file context.")
                    }
            }
        return try {
            val args = mutableListOf("gist", sourceFile.absolutePath, path.joinToString(","))
            contextFile?.let { args.add(it.absolutePath) }
            AstJson.decodeGist(run(args))
        } finally {
            sourceFile.delete()
            contextFile?.delete()
        }
    }

    /**
     * @param context MUST be the same list passed to the [analyze] call that
     *   produced [path]. The context is compiled in front of the selection, so
     *   a different context yields a different tree and the path would walk to
     *   a different node than the user selected.
     */
    fun edit(
        source: String,
        path: List<Int>,
        attr: String,
        value: String,
        valueKind: String,
        context: List<String> = emptyList(),
    ): EditResult {
        val sourceFile = writeTemp("rakuast-source", source)
            ?: return EditResult(error = "Could not write a temporary file for the snippet.")
        val valueFile = writeTemp("rakuast-value", value)
            ?: run {
                sourceFile.delete()
                return EditResult(error = "Could not write a temporary file for the new value.")
            }
        val contextFile = context
            .takeIf { it.isNotEmpty() }
            ?.let { statements ->
                writeTemp("rakuast-context", statements.joinToString("\n"))
                    ?: run {
                        sourceFile.delete()
                        valueFile.delete()
                        return EditResult(
                            error = "Could not write a temporary file for the file context.")
                    }
            }
        return try {
            val args = mutableListOf(
                "edit",
                sourceFile.absolutePath,
                path.joinToString(","),
                attr,
                valueFile.absolutePath,
                valueKind,
            )
            contextFile?.let { args.add(it.absolutePath) }
            AstJson.decodeEdit(run(args))
        } finally {
            sourceFile.delete()
            valueFile.delete()
            contextFile?.delete()
        }
    }

    /**
     * Inserts into, or replaces part of, the list-valued [attr] of the node at
     * [path].
     *
     * The [edit] verb sets a whole attribute, which cannot name "the third
     * statement" — so adding, reordering and replacing list items come through
     * here. [count] of 0 inserts at [index]; 1 or more replaces that many items
     * starting there.
     *
     * @param context MUST be the same list passed to the [analyze] call that
     *   produced [path], for the reason given on [edit].
     */
    fun splice(
        source: String,
        path: List<Int>,
        attr: String,
        index: Int,
        count: Int,
        value: String,
        context: List<String> = emptyList(),
    ): EditResult {
        val sourceFile = writeTemp("rakuast-source", source)
            ?: return EditResult(error = "Could not write a temporary file for the snippet.")
        val valueFile = writeTemp("rakuast-value", value)
            ?: run {
                sourceFile.delete()
                return EditResult(error = "Could not write a temporary file for the new value.")
            }
        val contextFile = context
            .takeIf { it.isNotEmpty() }
            ?.let { statements ->
                writeTemp("rakuast-context", statements.joinToString("\n"))
                    ?: run {
                        sourceFile.delete()
                        valueFile.delete()
                        return EditResult(
                            error = "Could not write a temporary file for the file context.")
                    }
            }
        return try {
            val args = mutableListOf(
                "splice",
                sourceFile.absolutePath,
                path.joinToString(","),
                attr,
                index.toString(),
                count.toString(),
                valueFile.absolutePath,
            )
            contextFile?.let { args.add(it.absolutePath) }
            AstJson.decodeEdit(run(args))
        } finally {
            sourceFile.delete()
            valueFile.delete()
            contextFile?.delete()
        }
    }

    /** Blocking. Callers must keep this off the EDT. */
    private fun run(args: List<String>): String {
        val script = RakuUtils.getResourceAsFile(SCRIPT)
            ?: return errorJson("Could not extract $SCRIPT from the plugin.")
        return try {
            val cmd = RakuCommandLine(project)
            cmd.setWorkDirectory(project.basePath)
            // Deliberately NOT -I: an -I path is resolved while the SCRIPT
            // itself is compiling, so a distribution whose META6.json names a
            // file that does not exist kills the script before its CATCH can
            // report anything, and the caller sees only empty output. The
            // script registers these at runtime instead, where the same
            // failure is catchable. See add-lib-path in rakuast-tool.raku.
            cmd.addParameter(script.path)
            cmd.addParameters(args)
            // Capture stderr and the exit code rather than using
            // executeAndRead, which can only report "nothing came back". When
            // the backend dies before printing its JSON, stderr holds the only
            // explanation there is, and "The Raku backend produced no output"
            // is a dead end for the user.
            val output = cmd.executeAndCapture(script, PROCESS_TIMEOUT_MS)
            // The script's JSON contract owns exactly one stdout line; joining
            // ALL lines would corrupt the payload if the snippet itself prints
            // -- a `BEGIN { say ... }` block, or a `use` of a module that
            // prints at load time. Take the last non-blank line instead.
            val json = output.stdoutLines.lastOrNull { it.isNotBlank() }
            when {
                json != null -> json
                output.isTimeout ->
                    errorJson("Raku did not finish within ${PROCESS_TIMEOUT_MS / 1000} seconds. " +
                              "A module that loops or blocks at load time can do this.")
                output.stderr.isNotBlank() -> errorJson(output.stderr.trim())
                else -> errorJson("Raku exited with code ${output.exitCode} and produced no output.")
            }
        } catch (e: ExecutionException) {
            // Thrown as "No SDK for project" when the project SDK is unset.
            LOG.info("RakuAST backend could not start", e)
            errorJson(e.message ?: "Could not start Raku for this project.")
        } finally {
            // executeAndRead already deletes `script` on both its success and
            // non-zero-exit branches. This only covers the case where
            // RakuCommandLine's own constructor throws before executeAndRead
            // ever runs (e.g. the "No SDK for project" branch above) -- an
            // easily hit path, since getResourceAsFile extracts a fresh temp
            // file on every analyze() call, so without this an unconfigured
            // SDK leaks one orphaned rakuast-tool.raku per call.
            if (script.exists()) script.delete()
        }
    }

    private fun writeTemp(prefix: String, content: String): File? = try {
        val f = Files.createTempFile(prefix, ".raku").toFile()
        f.writeText(content, StandardCharsets.UTF_8)
        f
    } catch (e: Exception) {
        LOG.warn("Could not write RakuAST temp file", e)
        null
    }

    // Built through the serializer so quotes and backslashes in the message
    // cannot produce malformed JSON. Both result types carry `error`, and the
    // decoder ignores unknown keys, so one shape serves both.
    private fun errorJson(message: String) =
        kotlinx.serialization.json.Json.encodeToString(AnalyzeResult(error = message))

    companion object {
        private val LOG = Logger.getInstance(RakuAstService::class.java)
        private const val SCRIPT = "rakuast/rakuast-tool.raku"

        // A selection's own analysis is fast, but it now loads the file's
        // imports, and a module can block or loop at load time. Without a
        // bound that hangs the background task forever with no way out.
        private const val PROCESS_TIMEOUT_MS = 30_000

        @JvmStatic
        fun getInstance(project: Project): RakuAstService = project.service()
    }
}
