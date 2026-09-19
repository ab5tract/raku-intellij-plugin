package org.raku.comma.rakuast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AstSpan(val from: Int, val to: Int)

@Serializable
data class AstAttr(
    val name: String,
    val kind: String,          // "scalar" | "node" | "list" | "null"
    val display: String,
    val editable: Boolean = false,
)

@Serializable
data class AstNode(
    // `class` is a Kotlin keyword, so the wire name is mapped explicitly.
    @SerialName("class") val nodeClass: String,
    val path: List<Int> = emptyList(),
    // Null when the backend's .origin was undefined (e.g. a synthetic
    // RakuAST::Type::Setting node) -- NOT the same as a real zero-length
    // span at offset 0. Callers must treat null as "no location", never
    // default it to (0,0).
    val span: AstSpan? = null,
    val attrs: List<AstAttr> = emptyList(),
    val children: List<AstNode> = emptyList(),
    // Rakudo's own one-line node summary -- the primary line of
    // RakuAST::Node.dump. Carries what the tree label cannot: the node's
    // identity markers (【$x】, 【+】, 【f】), its sink and block-statement
    // state (⚓ ▪), and a source excerpt. Null if the backend could not
    // compose one. Declared last so positional construction keeps working.
    val summary: String? = null,
) {
    override fun toString(): String = nodeClass.removePrefix("RakuAST::")
}

@Serializable
data class AnalyzeResult(val tree: AstNode? = null, val error: String? = null)

@Serializable
data class EditResult(
    val text: String? = null,
    val span: AstSpan? = null,
    val tree: AstNode? = null,
    val error: String? = null,
)

object AstJson {
    // Matches RakuExternalNamesParser's configuration.
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun decodeAnalyze(text: String): AnalyzeResult =
        decode(text) { AnalyzeResult(error = it) }

    fun decodeEdit(text: String): EditResult =
        decode(text) { EditResult(error = it) }

    private inline fun <reified T> decode(text: String, onError: (String) -> T): T =
        try {
            json.decodeFromString<T>(text)
        } catch (e: Exception) {
            onError(
                if (text.isBlank()) "The Raku backend produced no output."
                else "Could not read the Raku backend's response: ${e.message}"
            )
        }
}
