package org.raku.comma.metadata

import com.intellij.openapi.components.service
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import org.raku.comma.services.application.RakuDistroInfo

// This class is for all projects created or opened by Comma plugin.
// In order to achieve a semblance of sanity, we will unapologetically
// adjust your META6.json to the reasonable version.

@Serializable
data class MetaFile (
    val name: String? = null,
    val description: String? = null,
    val version: String? = null,
    val raku: String? = null,
    @SerialName("meta-version")
    val metaVersion: String? = null,
    val author: String? = null,
    val authors: List<String> = listOf(),
    val auth: String? = null,
    val dist: String? = null,
    @Serializable(with = DependsListSerializer::class)
    val depends: List<String> = listOf(),
    @SerialName("build-depends")
    val buildDepends: JsonElement? = null,
    @SerialName("test-depends")
    val testDepends: List<String> = listOf(),
    val provides: Map<String, String> = mapOf(),
    val resources: List<String> = listOf(),
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
    @SerialName("source-type")
    val sourceType: String? = null,
) {
    val allDepends: List<String>
        get() = listOf(depends, testDepends, simplifiedBuildDepends).flatten()

    val simplifiedBuildDepends: List<String>
        get() = getBuildDepends()

    private fun deconstructBuildDepends(): Any? {
        if (buildDepends == null) return null
        // First try the sane case
        try {
            return Json.decodeFromJsonElement<List<String>>(buildDepends)
        } catch (_: Exception) {}
        // Now try the diabolical case
        try {
            return Json.decodeFromJsonElement<ComplexBuildDepends>(buildDepends)
        } catch (_: Exception) {}
        return null
    }

    @SuppressWarnings("unchecked")
    private fun getBuildDepends(): List<String> {
        val anyBuildDepends = deconstructBuildDepends() ?: return listOf()
        return when (anyBuildDepends) {
            is List<*>              -> anyBuildDepends as List<String>
            is ComplexBuildDepends  -> anyBuildDepends.depends
            else                    -> listOf()
        }
    }
}

// TODO: Decide if/when we ever care about distro-specific dependencies. Right now
// it appears that all such uses are for external libraries.
// This information could still be useful, but we are going to ignore it for now.
@Serializable
data class ComplexBuildDepends(val requires: List<JsonElement>?, val runtime: List<JsonElement>?) {
    val depends: List<String>
        get() = depends()

    private val required: List<JsonElement>
        get() = this.requires ?: listOf()
    private val runtimed: List<JsonElement>
        get() = this.runtime ?: listOf()


    private fun depends(): List<String> {
        return listOf(runtimed, required).flatten().mapNotNull {
            when (it) {
                is JsonObject    -> resolveConditionalDependencyName(it)
                is JsonPrimitive -> if (it.isString) nativeFilteredOrNull(it.content) else null
                else             -> null
            }
        }
    }
}

// A `depends`/`build-depends` entry doesn't have to be a plain module name string. The
// ecosystem also allows conditional objects that pick the module name based on the
// current kernel or distro, e.g. `{ "by-kernel.name": { "linux": "Foo::Linux", ... } }`
// or its nested form `{ "by-kernel": { "name": { "linux": ... } } }` (same for distro).
// We resolve what we can and silently drop anything we don't recognise, matching the
// "ignore it for now" stance already taken for build-depends above.
internal fun resolveConditionalDependencyName(entry: JsonObject): String? {
    try {
        val asMap: Map<String, String> = Json.decodeFromJsonElement(entry)
        if (asMap.keys.size == 1 && asMap.keys.all { it == "name" })
            return nativeFilteredOrNull(asMap["name"] ?: "")
    } catch (_: Exception) {}

    try {
        return nativeFilteredOrNull(Json.decodeFromJsonElement<BuildDependsByDistro>(entry).distroRelevantModule)
    } catch (_: Exception) {}

    try {
        return nativeFilteredOrNull(Json.decodeFromJsonElement<BuildDependsByKernel>(entry).kernelRelevantModule)
    } catch (_: Exception) {}

    (entry["by-distro"] as? JsonObject)?.get("name")?.let {
        try {
            return nativeFilteredOrNull(Json.decodeFromJsonElement<Map<String, String>>(it)[service<RakuDistroInfo>().distroName] ?: "")
        } catch (_: Exception) {}
    }

    (entry["by-kernel"] as? JsonObject)?.get("name")?.let {
        try {
            return currentKernelName()?.let { kernel ->
                nativeFilteredOrNull(Json.decodeFromJsonElement<Map<String, String>>(it)[kernel] ?: "")
            }
        } catch (_: Exception) {}
    }

    return null
}

private fun nativeFilteredOrNull(maybeNative: String): String? {
    if (maybeNative.isEmpty()) return null
    if (Regex(".+:from<native>.*").containsMatchIn(maybeNative)) return null
    return maybeNative
}

// Best-effort mapping of the running JVM's OS to Raku's `$*KERNEL.name` values. Used only
// to pick a branch out of a `by-kernel.name` map; an unmatched/unknown OS just means we
// skip that dependency entry rather than crash, same as an unresolvable distro branch.
private fun currentKernelName(): String? {
    val osName = System.getProperty("os.name")?.lowercase() ?: return null
    return when {
        osName.contains("linux")             -> "linux"
        osName.contains("mac") ||
        osName.contains("darwin")            -> "darwin"
        osName.contains("windows")           -> "win32"
        osName.contains("freebsd")           -> "freebsd"
        osName.contains("openbsd")           -> "openbsd"
        osName.contains("netbsd")            -> "netbsd"
        osName.contains("sunos") ||
        osName.contains("solaris")           -> "solaris"
        else                                  -> null
    }
}

@Serializable
data class ComplexBuildDependsElement(val from: String, val name: BuildDependsByDistro)
@Serializable
data class BuildDependsByDistro(
    @SerialName("by-distro.name")
    val byDistroName: Map<String, String>
) {
    val distroRelevantModule: String
        get() = byDistroName[service<RakuDistroInfo>().distroName] ?: ""
}
@Serializable
data class BuildDependsByKernel(
    @SerialName("by-kernel.name")
    val byKernelName: Map<String, String>
) {
    val kernelRelevantModule: String
        get() = byKernelName[currentKernelName()] ?: ""
}

object DependsListSerializer : KSerializer<List<String>> {
    override val descriptor: SerialDescriptor = ListSerializer(String.serializer()).descriptor

    override fun serialize(encoder: Encoder, value: List<String>) {
        ListSerializer(String.serializer()).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): List<String> {
        val array = (decoder as? JsonDecoder)?.decodeJsonElement() as? JsonArray
            ?: return ListSerializer(String.serializer()).deserialize(decoder)
        return array.mapNotNull { entry ->
            when (entry) {
                is JsonPrimitive -> if (entry.isString) nativeFilteredOrNull(entry.content) else null
                is JsonObject    -> resolveConditionalDependencyName(entry)
                else             -> null
            }
        }
    }
}