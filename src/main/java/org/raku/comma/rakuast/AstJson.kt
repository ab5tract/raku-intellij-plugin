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
    // The slot's DECLARED type, where `kind` describes only what happens to be
    // in it now. This is what a drop is checked against: see
    // [AnalyzeResult.accepts]. Defaults to "Mu" — unconstrained — for a backend
    // response predating the field.
    val type: String = "Mu",
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
    // On the root only: the file's `use`/`need` statements that were compiled
    // in front of the selection so its imports resolve. They are not part of
    // the selection, so they get no nodes in the tree -- the viewer surfaces
    // them as rows on the root instead.
    val context: List<String> = emptyList(),
    // Which attribute of the parent holds this node, and at which index when
    // that attribute is a list. Null on the root, and on any node whose slot
    // could not be identified. This is what turns a tree position into
    // something writable: without it a position is only "the Nth child", which
    // no edit can name.
    @SerialName("via-attr") val viaAttr: String? = null,
    @SerialName("via-index") val viaIndex: Int? = null,
    // Where this node's source text can be lifted from when dragged, when that
    // differs from [span]. A StrLiteral's span covers the bare `cool` inside
    // the quotes, and `cool` alone parses as a call rather than a string; this
    // points at the enclosing quoted construct instead. Null means [span] is
    // itself fine.
    @SerialName("drag-span") val dragSpan: AstSpan? = null,
) {
    /** The span to lift source from when this node is dragged. */
    fun sourceSpan(): AstSpan? = dragSpan ?: span

    /**
     * The tree label. Prefers Rakudo's own summary, which already leads with
     * the bare class name and adds the node's identity (【$x】, 【+】, 【f】)
     * plus its sink and block-statement state — exactly what a tree of bare
     * class names cannot convey, since sibling `Name` or `Infix` nodes are
     * otherwise indistinguishable. Falls back to the class name when the
     * backend could not compose a summary.
     */
    override fun toString(): String = summary ?: nodeClass.removePrefix("RakuAST::")
}

@Serializable
data class AnalyzeResult(
    val tree: AstNode? = null,
    val error: String? = null,
    // Every RakuAST class in this tree, mapped to the class names it conforms
    // to. Sent once per response keyed by class rather than repeated on every
    // node, which holds it to a few dozen short lists however large the tree.
    val conformance: Map<String, List<String>> = emptyMap(),
) {
    /**
     * Whether a node of [nodeClass] may be dropped into a slot declared to
     * hold [declaredType].
     *
     * RakuAST uses plain classes throughout — no roles in the chain — so the
     * whole check is whether the slot's type appears in the node's conformance
     * list. Pure set membership, so it can run on every drag-hover without a
     * round trip to Raku.
     *
     * `Mu` and `List` are unconstrained rather than universal matches: `Mu` is
     * what bookkeeping slots declare, and a list slot is declared `List`, which
     * says nothing about its elements. Both are permitted here and left to the
     * compile check that already guards every edit — this gate is for instant
     * feedback, not for correctness.
     *
     * An unknown [nodeClass] is permitted for the same reason: refusing on
     * missing metadata would silently disable dropping rather than explain it.
     */
    fun accepts(nodeClass: String, declaredType: String): Boolean =
        RakuAstTypes.accepts(conformance[nodeClass], declaredType)
}

/**
 * The one place the drop-legality rule lives.
 *
 * Both callers need it: [AnalyzeResult.accepts] looks the node's conformance up
 * in its own tree, while a drag that crossed panes carries the list with it —
 * the target pane cannot look up a class its own analysis never saw.
 */
object RakuAstTypes {

    /**
     * Whether a node conforming to [conforms] may be dropped into a slot
     * declared to hold [declaredType].
     *
     * A null [conforms] means we have no metadata for that class, which is
     * permitted for the same reason `Mu` is: refusing on missing information
     * would silently disable dropping rather than explain it, and the compile
     * check that already guards every edit remains the real arbiter.
     */
    fun accepts(conforms: List<String>?, declaredType: String): Boolean {
        if (declaredType in UNCONSTRAINED) return true
        if (conforms == null) return true
        return declaredType in conforms
    }

    // `Mu` is what bookkeeping slots declare. `List` is what every list-valued
    // slot declares, and it says nothing about its elements — there is no
    // element type to recover, so those defer to the compile check.
    private val UNCONSTRAINED = setOf("Mu", "List")
}

@Serializable
data class EditResult(
    val text: String? = null,
    val span: AstSpan? = null,
    val tree: AstNode? = null,
    val error: String? = null,
    val conformance: Map<String, List<String>> = emptyMap(),
)

/**
 * One node's `.gist` — Rakudo's constructor-syntax rendering, fetched on
 * demand rather than shipped with the tree. A gist nests its whole subtree,
 * so emitting one per node re-serialises every level inside every level
 * above it: measured at 160x the source size on a small snippet.
 */
@Serializable
data class GistResult(val gist: String? = null, val error: String? = null)

object AstJson {
    // Matches RakuExternalNamesParser's configuration.
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun decodeAnalyze(text: String): AnalyzeResult =
        decode(text) { AnalyzeResult(error = it) }

    fun decodeEdit(text: String): EditResult =
        decode(text) { EditResult(error = it) }

    fun decodeGist(text: String): GistResult =
        decode(text) { GistResult(error = it) }

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
