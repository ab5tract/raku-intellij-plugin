package org.raku.comma.sdk

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Typed model of the JSON emitted by symbols/raku-core-symbols.raku and
 * symbols/raku-module-symbols.raku (their pack-variable / pack-code /
 * describe-OOP subs). The wire format is a JSON array of objects
 * discriminated by the "k" key:
 *
 *   "n"            native type
 *   "v"            our-scoped variable
 *   "m", "s", "r"  routine (method / sub / regex-ish; raw kind is preserved)
 *   "e", "ss"      enum / subset
 *   "mm"           metamodel (EXPORTHOW) entry, carries "key"
 *   "c", "ro"      class / role, carries "mro"
 *
 * The same routine shape appears both at top level and nested inside a
 * package's "m" array, so routines are decoded concretely there rather than
 * through the polymorphic serializer. Several "k" values sharing one shape is
 * also why this uses JsonContentPolymorphicSerializer instead of
 * classDiscriminator-based polymorphism.
 */
@Serializable(with = ExternalSymbolEntrySerializer::class)
sealed interface ExternalSymbolEntry {
    val k: String
    val n: String
}

@Serializable
data class NativeTypeEntry(
    override val k: String,
    override val n: String,
    val t: String,
) : ExternalSymbolEntry

@Serializable
data class VariableEntry(
    override val k: String,
    override val n: String,
    val t: String,
    val d: String? = null,
) : ExternalSymbolEntry

@Serializable
data class EnumOrSubsetEntry(
    override val k: String,
    override val n: String,
    val t: String,
    val d: String? = null,
) : ExternalSymbolEntry

@Serializable
data class RoutineEntry(
    override val k: String,
    override val n: String,
    val m: Int,
    val s: SignatureJson,
    val d: String? = null,
    val x: String? = null,
    val p: Boolean = false,
    val rakudo: Boolean = false,
) : ExternalSymbolEntry {
    val multiness: String get() = if (m == 0) "only" else "multi"
}

@Serializable
data class PackageEntry(
    override val k: String,
    override val n: String,
    val t: String,
    val b: String,
    val key: String? = null,
    val d: String? = null,
    val mro: List<String> = emptyList(),
    // "mm" entries emit a literal "m":null, so absence and null must both work
    val m: List<RoutineEntry>? = null,
    val a: List<AttributeEntry> = emptyList(),
) : ExternalSymbolEntry

@Serializable
data class AttributeEntry(
    val n: String,
    val t: String,
    val d: String? = null,
)

@Serializable
data class SignatureJson(
    val r: String,
    @Serializable(with = ParameterListSerializer::class)
    val p: List<ParameterJson> = emptyList(),
)

/**
 * symbols/nqp.ops writes signature params as plain strings ("p":["int $i "])
 * rather than {n,t} objects. The org.json parser skipped anything that was
 * not an object; keep that tolerance by filtering the array down to objects
 * before normal decoding.
 */
private object ParameterListSerializer
    : JsonTransformingSerializer<List<ParameterJson>>(ListSerializer(ParameterJson.serializer())) {

    override fun transformDeserialize(element: JsonElement): JsonElement =
        if (element is JsonArray) JsonArray(element.filterIsInstance<JsonObject>()) else element
}

@Serializable
data class ParameterJson(
    val n: String,
    val t: String,
    val nn: List<String> = emptyList(),
)

object ExternalSymbolEntrySerializer
    : JsonContentPolymorphicSerializer<ExternalSymbolEntry>(ExternalSymbolEntry::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ExternalSymbolEntry> =
        when (val kind = element.jsonObject["k"]?.jsonPrimitive?.contentOrNull) {
            "n" -> NativeTypeEntry.serializer()
            "v" -> VariableEntry.serializer()
            "m", "s", "r" -> RoutineEntry.serializer()
            "e", "ss" -> EnumOrSubsetEntry.serializer()
            "mm", "c", "ro" -> PackageEntry.serializer()
            else -> throw SerializationException("Unknown external symbol kind '$kind'")
        }
}
