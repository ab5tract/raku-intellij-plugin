package org.raku.comma.sdk

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import org.raku.comma.psi.RakuFile
import org.raku.comma.psi.RakuPackageDecl
import org.raku.comma.psi.RakuRoutineDecl
import org.raku.comma.psi.RakuVariableDecl
import org.raku.comma.psi.external.ExternalRakuPackageDecl
import org.raku.comma.psi.external.ExternalRakuRoutineDecl
import org.raku.comma.psi.external.ExternalRakuVariableDecl
import org.raku.comma.psi.symbols.RakuExplicitSymbol
import org.raku.comma.psi.symbols.RakuSymbol
import org.raku.comma.psi.symbols.RakuSymbolKind

class RakuExternalNamesParser(
    private val myProject: Project,
    private val myFile: RakuFile,
    private val myEntries: List<ExternalSymbolEntry>,
) {
    private val result = ArrayList<RakuSymbol>()
    private val externalClasses = HashMap<String, RakuPackageDecl>()
    private val metamodelCache = HashMap<String, RakuPackageDecl>()

    constructor(project: Project, file: RakuFile, json: String)
        : this(project, file, decodeOrWarn(json, file))

    fun parse(): RakuExternalNamesParser {
        for (entry in myEntries) {
            when (entry) {
                is NativeTypeEntry -> {
                    val psi = ExternalRakuPackageDecl(myProject, myFile, "", entry.n, entry.t, "")
                    result.add(RakuExplicitSymbol(RakuSymbolKind.TypeOrConstant, psi))
                }
                is VariableEntry -> {
                    val decl = ExternalRakuVariableDecl(myProject, myFile, entry.n, "our", entry.t)
                    entry.d?.let(decl::setDocs)
                    result.add(RakuExplicitSymbol(RakuSymbolKind.Variable, decl))
                }
                is RoutineEntry -> {
                    val psi = makeRoutineDecl(entry, myFile, if (entry.k == "m") "has" else "our")
                    result.add(RakuExplicitSymbol(RakuSymbolKind.Routine, psi))
                }
                is EnumOrSubsetEntry -> {
                    val psi = ExternalRakuPackageDecl(myProject, myFile, "c", entry.n, entry.t, "A")
                    entry.d?.let(psi::setDocs)
                    result.add(RakuExplicitSymbol(RakuSymbolKind.TypeOrConstant, psi))
                }
                is PackageEntry -> if (entry.k == "mm") {
                    val key = entry.key ?: continue
                    val psi = parsePackageDeclaration(entry, emptyList())
                    // Add to a metamodel cache to apply to users
                    metamodelCache[key] = psi
                    psi.setName(key)
                    externalClasses[psi.name] = psi
                    result.add(RakuExplicitSymbol(RakuSymbolKind.TypeOrConstant, psi))
                } else {
                    val psi = parsePackageDeclaration(entry, entry.mro)
                    // The cache is keyed by declarator ("class"/"role"), which is
                    // exactly what packageKind holds for "c"/"ro" entries.
                    val metamodel = metamodelCache[psi.packageKind]
                    if (metamodel != null) {
                        psi.setMetaClass(metamodel)
                    }
                    externalClasses[psi.name] = psi
                    result.add(RakuExplicitSymbol(RakuSymbolKind.TypeOrConstant, psi))
                }
            }
        }
        return this
    }

    private fun parsePackageDeclaration(entry: PackageEntry, mro: List<String>): ExternalRakuPackageDecl {
        val psi = ExternalRakuPackageDecl(
            myProject, myFile, entry.k, entry.n, entry.t, entry.b,
            ArrayList(), ArrayList(), mro, null)
        entry.d?.let(psi::setDocs)

        val routines = ArrayList<RakuRoutineDecl>()
        for (routine in entry.m.orEmpty()) {
            routines.add(makeRoutineDecl(routine, psi, "has"))
        }

        val attrs = ArrayList<RakuVariableDecl>()
        for (attribute in entry.a) {
            val attributeDecl = ExternalRakuVariableDecl(myProject, psi, attribute.n, "has", attribute.t)
            attribute.d?.let(attributeDecl::setDocs)
            attrs.add(attributeDecl)
        }

        psi.setRoutines(routines)
        psi.setAttributes(attrs)
        return psi
    }

    private fun makeRoutineDecl(entry: RoutineEntry, parent: PsiElement, scope: String): ExternalRakuRoutineDecl {
        val psi = ExternalRakuRoutineDecl(
            myProject, parent, entry.k, scope, entry.n, entry.multiness,
            entry.x, entry.s, entry.p)
        entry.d?.let(psi::setDocs)
        if (entry.rakudo) {
            psi.isImplementationDetail = true
        }
        return psi
    }

    fun result(): List<RakuSymbol> = result

    val packages: Map<String, RakuPackageDecl>
        get() = externalClasses

    companion object {
        private val LOG = Logger.getInstance(RakuExternalNamesParser::class.java)
        private val json = Json { ignoreUnknownKeys = true; isLenient = true }

        private val KNOWN_KINDS = setOf("n", "v", "m", "s", "r", "e", "ss", "mm", "c", "ro")

        private fun decodeOrWarn(text: String, file: RakuFile): List<ExternalSymbolEntry> =
            tryDecode(text) ?: run {
                LOG.warn("Tried to parse a JSON array out of [$text]; for file '${file.name}'")
                emptyList()
            }

        /**
         * Decodes emitted symbol JSON into typed entries, or returns null when
         * the text is not a JSON array at all (the caller decides whether that
         * warrants a warning or an empty result). Unknown "k" values are
         * skipped silently, matching the old switch default. An element that
         * fails to decode is warned about and skipped, which — deliberately,
         * unlike the org.json predecessor — leaves the rest of the array
         * intact instead of aborting it.
         */
        @JvmStatic
        fun tryDecode(text: String): List<ExternalSymbolEntry>? {
            val array = try {
                json.parseToJsonElement(text) as? JsonArray
            } catch (e: Exception) {
                null
            } ?: return null

            val entries = ArrayList<ExternalSymbolEntry>(array.size)
            for (element in array) {
                val obj = element as? JsonObject ?: continue
                val kind = (obj["k"] as? JsonPrimitive)?.contentOrNull
                if (kind !in KNOWN_KINDS) continue
                try {
                    entries.add(json.decodeFromJsonElement<ExternalSymbolEntry>(obj))
                } catch (e: SerializationException) {
                    LOG.warn("Skipping malformed external symbol entry (k=$kind)", e)
                }
            }
            return entries
        }
    }
}
