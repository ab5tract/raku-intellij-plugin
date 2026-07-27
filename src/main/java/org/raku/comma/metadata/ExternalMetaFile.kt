package org.raku.comma.metadata

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class ExternalMetaFile (
    val name: String? = null,
    val description: String? = null,
    val version: String? = null,
    @JsonNames("perl")
    val raku: String? = null,
    @SerialName("meta-version")
    val metaVersion: String? = null,
    val author: String? = null,
    @Serializable(with =  StringListSerializer::class)
    val authors: List<String> = listOf(),
    val auth: String? = null,
    val dist: String? = null,
    @Serializable(with =  DependsSerializer::class)
    val depends: List<String> = listOf(),
    @SerialName("build-depends")
    @Serializable(with =  DependsSerializer::class)
    val buildDepends: List<String> = listOf(),
    @SerialName("test-depends")
    @Serializable(with =  DependsSerializer::class)
    val testDepends: List<String> = listOf(),
    val provides: Map<String, String> = mapOf(),
    @Serializable(with =  MapListSerializer::class)
    val resources: List<Map<String, String>> = listOf(),
    val documentation: Map<String, String> = mapOf(),
    val support: Support? = null,
    val license: String? = null,
    val tags: List<String> = listOf(),
    val api: String? = null,
    val path: String? = null,
    val production: Boolean? = null,
    val emulates: Map<String, String> = mapOf(),
    val supersedes: Map<String, String> = mapOf(),
    @SerialName("superseded-by")
    val supersededBy: Map<String, String> = mapOf(),
    val excludes: Map<String, String> = mapOf(),
    @SerialName("source-url")
    val sourceUrl: String? = null,
    @JsonNames("repo-type")
    @SerialName("source-type")
    val sourceType: String? = null,
)

object StringListSerializer : JsonTransformingSerializer<List<String>>(ListSerializer(String.serializer())) {
    // If response is not an array, then it is a single object that should be wrapped into the array
    override fun transformDeserialize(element: JsonElement): JsonElement =
        if (element !is JsonArray) JsonArray(listOf(element)) else element
}

object MapListSerializer : JsonTransformingSerializer<List<String>>(ListSerializer(String.serializer())) {
    // If response is not an array, then it is a single object that should be wrapped into the array
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return when (element) {
            is JsonObject -> JsonArray(element.map { JsonPrimitive("${it.key} => ${it.value}") }.toList()) // invalid usage pattern
            !is JsonArray -> JsonArray(listOf(element))
            else -> element
        }
    }
}

/**
 * `depends` is not always a list of module names. META6 also allows a hash keyed by
 * phase, whose values carry the actual list under `requires`:
 *
 * ```json
 * "depends": { "runtime": { "requires": [ "Foo", "Bar" ] } }
 * ```
 *
 * Flattening that hash the way [MapListSerializer] does yields one bogus "module name"
 * per key -- literally `runtime => {"requires":[...]}`. That string then travels all the
 * way into ProjectModelSync, which formats every dependency into a `raku://` library
 * URL, and the VFS ends up trying to stat a file named after the whole JSON blob
 * ("File name too long" in the log). Anything we cannot resolve to a name is dropped
 * instead, which is also how conditional entries have always been treated.
 */
object DependsSerializer : JsonTransformingSerializer<List<String>>(ListSerializer(String.serializer())) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        JsonArray(entriesOf(element).mapNotNull(::dependencyName).map(::JsonPrimitive))

    private fun entriesOf(element: JsonElement): List<JsonElement> = when (element) {
        is JsonArray  -> element
        is JsonObject -> requiresOf(element)
        else          -> listOf(element)
    }

    // The phase hash. Only `runtime` belongs in `depends`; `build` and `test` have their
    // own META6 fields and are read there. Both the nested form and the flat
    // `{ "requires": [...] }` some distributions use are accepted.
    private fun requiresOf(element: JsonObject): List<JsonElement> {
        val sections = listOfNotNull(element["runtime"], element)
        return sections.flatMap { section ->
            when (section) {
                is JsonArray  -> section
                is JsonObject -> (section["requires"] as? JsonArray) ?: emptyList()
                else          -> emptyList()
            }
        }
    }

    private fun dependencyName(entry: JsonElement): String? = when (entry) {
        is JsonPrimitive -> if (entry.isString) entry.content else null
        is JsonObject    -> resolveConditionalDependencyName(entry)
        else             -> null
    }
}

// TODO: The following is all related to (trying) to get the serialization to work for MetaFile.
//       It gets pretty far along except the @Serializable cases are not evaluated by the XML
//       serializer, so we end up dying when authors = "one person" instead of authors = [ "multiple" ]
//
//@Serializable
//data class Support(
//    @Attribute(value = "email") val email: String? = null,
//    @SerialName(value = "maillinglist")
//    @Attribute(value = "mailinglist") val mailingList: String? = null,
//    @SerialName(value = "bugtracker")
//    @Attribute(value = "bugTracker") val bugTracker: String? = null,
//    @Attribute(value = "source") val source: String? = null,
//    @Attribute(value = "irc") val irc: String? = null,
//    @Attribute(value = "phone") val phone: String? = null
//)
//
//@Serializable
//@Tag("metafile")
//data class MetaFile @OptIn(ExperimentalSerializationApi::class) constructor(
//    @Attribute(value = "name")val name: String? = null,
//    @Attribute(value = "description") val description: String? = null,
//    @Attribute(value = "version") val version: String? = null,
//    @JsonNames("perl")
//    @Attribute(value = "raku") val raku: String? = null,
//    @SerialName("meta-version")
//    @Attribute(value = "metaVersion") val metaVersion: String? = null,
//    @Attribute(value = "author") val author: String? = null,
//    @Serializable(with =  StringListSerializer::class)
//    @Attribute(value = "authors") val authors: List<String> = listOf(),
//    @Attribute(value = "auth") val auth: String? = null,
//    @Attribute(value = "dist") val dist: String? = null,
//    @Serializable(with =  MapListSerializer::class)
//    @Attribute(value = "depends") val depends: List<String> = listOf(),
//    @SerialName("build-depends")
//    @Attribute(value = "buildDepends") val buildDepends: List<String> = listOf(),
//    @SerialName("test-depends")
//    @Attribute(value = "testDepends") val testDepends: List<String> = listOf(),
//    @Attribute(value = "provides") val provides: Map<String, String> = mapOf(),
//    @Serializable(with =  MapListSerializer::class)
//    @Attribute(value = "resources") val resources: List<Map<String, String>> = listOf(),
//    @Attribute(value = "documentation") val documentation: Map<String, String> = mapOf(),
//    @Attribute(value = "support") val support: Support? = null,
//    @Attribute(value = "license") val license: String? = null,
//    @Attribute(value = "tags") val tags: List<String> = listOf(),
//    @Attribute(value = "api") val api: String? = null,
//    @Attribute(value = "path") val path: String? = null,
//    @Attribute(value = "production") val production: Boolean? = null,
//    @Attribute(value = "emulates") val emulates: Map<String, String> = mapOf(),
//    @Attribute(value = "supersedes") val supersedes: Map<String, String> = mapOf(),
//    @SerialName("superseded-by")
//    @Attribute(value = "supersededBy") val supersededBy: Map<String, String> = mapOf(),
//    @Attribute(value = "excludes") val excludes: Map<String, String> = mapOf(),
//    @SerialName("source-url")
//    @Attribute(value = "sourceUrl") val sourceUrl: String? = null,
//    @JsonNames("repo-type")
//    @SerialName("source-type")
//    @Attribute(value = "sourceType") val sourceType: String? = null,
//)