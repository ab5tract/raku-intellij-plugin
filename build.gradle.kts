
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease
import kotlin.io.path.Path
import kotlin.io.path.exists


fun properties(key: String) = project.findProperty(key).toString()

// TODO: Don't include all of this mess in one file
//////// VERSION STUFF

fun formatBranch(
    gitBranch: String,
    format: String = "%s"
) = if (gitBranch != "main") format.format(gitBranch) else ""

val ideaBuildVersion = File("${project.projectDir.path}/.versions/idea-version").readText(Charsets.UTF_8).trimEnd()

// In GitHub Actions we pass the current tag (aka the version) via a property
fun versionFromPropertyPossibly(): String {
    if (project.hasProperty("pluginVersion")) {
        return project.property("pluginVersion").toString()
    }
    return safeDetermineCurrentRakuBetaPluginVersion(determineCurrentGitBranch())
}

fun determineCurrentGitBranch(): String {
    return  providers.exec {
                     commandLine("git", "branch", "--show-current")
            }.standardOutput.asText.get().trim().lines().last()
}

fun gitCurrentRakuBetaPluginVersion(): String {
    return providers.exec {
                 commandLine("git", "tag", "--merged", "main", "--sort=taggerdate")
             }.standardOutput.asText.get().trim().lines().last()
}

fun safeDetermineCurrentRakuBetaPluginVersion(currentGitBranch: String): String {
    val betaVersionPath = Path("${project.projectDir.path}/.versions/raku-beta-version${ formatBranch(currentGitBranch, ".%s") }")

    return when(betaVersionPath.exists()) {
        true  -> betaVersionPath.toFile().readText().trim()
        false -> {
            val idea = File("${project.projectDir.path}/.versions/idea-version").readText(Charsets.UTF_8).trimEnd()
            "$idea-beta${ formatBranch(currentGitBranch, "(%s)") }.1"
        }
    }
}

abstract class IdeaVersionTask : DefaultTask() {
    @Input
    val basePath: String = project.projectDir.path

    @Input
    val ideaFileName: String = "$basePath/.versions/idea-version"

    @InputFile
    val ideaVersionFile = File(ideaFileName)

    @Input
    val ideaVersion: String = ideaVersionFile.readText().trimEnd()

    @TaskAction
    open fun action() {
        println(ideaVersion)
    }
}

data class RakuPluginBetaVersion(
    val idea: String,
    val beta: Int,
    val branch: String,
    val basePath: String
) {
    fun fileName(): String = "$basePath/.versions/raku-beta-version${ maybeBranch(".%s") }"
    fun maybeBranch(format: String = "%s") = if (branch != "main") format.format(branch) else ""

    override fun toString(): String = "$idea-beta${ maybeBranch("(%s)") }.$beta"
}

// TODO: Make this support branches other that 'main'
abstract class FetchGitTagRakuPluginBetaVersion : IdeaVersionTask() {
    @get:Input
    val gitBranch: Property<String> = project.objects.property<String>()

    @get:Input
    val gitTag: Property<String> = project.objects.property<String>()

    @Internal
    // TODO|XXX : this will break for beta releases > 10
    val version = gitTag.map { it.last().digitToInt() }
    @Internal
    val pluginBetaVersion: Provider<RakuPluginBetaVersion> = version.map { determinePluginVersion(it) }

    fun determinePluginVersion(version: Int): RakuPluginBetaVersion
                =   RakuPluginBetaVersion(
                        idea     = ideaVersion,
                        beta     = version,
                        branch   = gitBranch.get(),
                        basePath = basePath)

    @TaskAction
    override fun action() {
        println(pluginBetaVersion.get().toString())
    }
}

abstract class GetRakuPluginBetaVersion : IdeaVersionTask() {
    @get:Input
    val gitBranch: Property<String> = project.objects.property<String>()

    @get:Input
    val gitTag: Property<String> = project.objects.property<String>()

    @Internal
    val version = gitTag.map { it.last().digitToInt() }
    @Internal
    val pluginBetaVersion: Provider<RakuPluginBetaVersion> = version.map { determinePluginVersion(it) }

    @get:OutputFile
    val betaVersionFile: Provider<File> = pluginBetaVersion.map { File(it.fileName()) }

    fun determinePluginVersion(version: Int): RakuPluginBetaVersion
                =   RakuPluginBetaVersion(
                        idea   = ideaVersion,
                        beta   = version,
                        branch = gitBranch.get(),
                        basePath = basePath)

    @TaskAction
    override fun action() {
        betaVersionFile.get().parentFile.mkdirs()
        betaVersionFile.get().writeText(pluginBetaVersion.get().toString())
        println(pluginBetaVersion.get().toString())
    }
}

abstract class BumpRakuPluginBetaVersion: GetRakuPluginBetaVersion() {
    @TaskAction
    override fun action() {
        val oldPluginVersion = pluginBetaVersion.get()

        val newPluginVersion = when (ideaVersion == oldPluginVersion.idea) {
            true  -> RakuPluginBetaVersion(oldPluginVersion.idea,
                                           oldPluginVersion.beta + 1,
                                           gitBranch.get(),
                                           basePath)
            false -> RakuPluginBetaVersion(ideaVersion, 1, gitBranch.get(), basePath)
        }

        betaVersionFile.get().writeText(newPluginVersion.toString())
        println(newPluginVersion)
    }
}
////// END VERSION STUFF

tasks.register<IdeaVersionTask>("retrieveIdeaVersion") {
    group = "version"
    description = "Retrieve IntelliJ IDEA version"
}

tasks.register<FetchGitTagRakuPluginBetaVersion>("findVersionFromGitTag") {
    group = "version"
    description = "Determine plugin beta version based on git tags"

    gitTag = gitCurrentRakuBetaPluginVersion()
    gitBranch = "main"
}

tasks.register<GetRakuPluginBetaVersion>("retrieveBetaVersion") {
    group = "version"
    description = "Retrieve plugin beta version"

    gitTag = safeDetermineCurrentRakuBetaPluginVersion(currentGitBranch = determineCurrentGitBranch())
    gitBranch = determineCurrentGitBranch()
}

tasks.register<BumpRakuPluginBetaVersion>("bumpBetaVersion") {
    group = "version"
    description = "Bump plugin beta version"

    gitTag = safeDetermineCurrentRakuBetaPluginVersion(currentGitBranch = determineCurrentGitBranch())
    gitBranch = determineCurrentGitBranch()
}

plugins {
    // Java support
    id("java")
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij.platform") version "2.16.0"

    kotlin("jvm") version "2.3.20"
    kotlin("plugin.serialization") version "2.3.20"
}

group   = properties("pluginGroup")
version = versionFromPropertyPossibly()
// Configure project's dependencies
repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "org.raku.comma"
        name = "Raku"
        version = versionFromPropertyPossibly()

        ideaVersion {
            sinceBuild = "242.0"
        }
    }

    pluginVerification {
        ides {
            select {
                types = listOf(IntelliJPlatformType.values().toList().random())
                channels = listOf(ProductRelease.Channel.RELEASE)
                sinceBuild = "242.0"
            }
        }
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaUltimate(ideaBuildVersion)
        bundledPlugin("com.intellij.java")

        pluginVerifier()
        zipSigner()
    }
    implementation(files("libs/xchart-3.8.0.jar"))
    implementation(files("libs/moarvmremote.jar"))

    implementation("com.jetbrains.intellij.spellchecker:spellchecker:261.23567.145")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    implementation("io.airlift:aircompressor:2.0.2")
    implementation("org.json:json:20240303")
    // TODO: Remove this due to multiple unpatch CVEs
    implementation("org.tap4j:tap4j:4.4.2")
}
