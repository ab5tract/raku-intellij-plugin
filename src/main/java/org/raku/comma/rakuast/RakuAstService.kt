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

    fun analyze(source: String): AnalyzeResult {
        val sourceFile = writeTemp("rakuast-source", source)
            ?: return AnalyzeResult(error = "Could not write a temporary file for the snippet.")
        return try {
            AstJson.decodeAnalyze(run(listOf("analyze", sourceFile.absolutePath)))
        } finally {
            sourceFile.delete()
        }
    }

    /** Blocking. Callers must keep this off the EDT. */
    private fun run(args: List<String>): String {
        val script = RakuUtils.getResourceAsFile(SCRIPT)
            ?: return errorJson("Could not extract $SCRIPT from the plugin.")
        return try {
            val cmd = RakuCommandLine(project)
            cmd.setWorkDirectory(project.basePath)
            cmd.addParameter(script.path)
            cmd.addParameters(args)
            // executeAndRead deletes the script file and returns an empty list
            // on a non-zero exit, so an empty result means "no usable output".
            cmd.executeAndRead(script).joinToString("\n")
        } catch (e: ExecutionException) {
            // Thrown as "No SDK for project" when the project SDK is unset.
            LOG.info("RakuAST backend could not start", e)
            errorJson(e.message ?: "Could not start Raku for this project.")
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

        @JvmStatic
        fun getInstance(project: Project): RakuAstService = project.service()
    }
}
