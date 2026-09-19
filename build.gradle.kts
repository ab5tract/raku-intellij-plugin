
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
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
    failIfReleasingWithoutAnExplicitVersion()
    return safeDetermineCurrentRakuBetaPluginVersion(determineCurrentGitBranch())
}

// The fallback below reads .versions/raku-beta-version, which is a record
// rather than a source of truth -- the git tag is. That file drifts whenever
// a tag is cut by hand, and it currently reads months behind the latest tag.
// Falling back to it locally is harmless; doing so while building something
// we are about to publish would ship an artifact named after the wrong
// version, silently. So refuse, rather than guess, in that one situation.
//
// Scoped deliberately: only when packaging a plugin, and only on CI. A
// developer running buildPlugin locally still gets the fallback.
fun failIfReleasingWithoutAnExplicitVersion() {
    val onGitHubActions = providers.environmentVariable("GITHUB_ACTIONS").orNull == "true"
    if (!onGitHubActions) return

    val packagingAPlugin = gradle.startParameter.taskNames.any {
        it.substringAfterLast(':') in setOf("buildPlugin", "publishPlugin", "signPlugin")
    }
    if (!packagingAPlugin) return

    error(
        """
        Refusing to package a plugin on CI without -PpluginVersion.

        The version would otherwise come from .versions/raku-beta-version,
        which is only a record of the last bump and drifts whenever a tag is
        cut by hand -- so the published artifact would carry a stale version
        in its filename and its plugin.xml, with nothing to flag it.

        Pass the tag being built, as the release workflow does:
            ./gradlew buildPlugin -PpluginVersion=${'$'}{{ github.ref_name }}
        """.trimIndent()
    )
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
            // Same double-dash separator as RakuPluginBetaVersion.toString --
            // these two must agree, or the version a fresh branch reports
            // would not match the one it bumps to.
            "$idea-beta${ formatBranch(currentGitBranch, "--%s") }.1"
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

    // A branch beta reads 2026.2-beta--some-branch.3.
    //
    // The separator is a double dash because this string becomes a git tag,
    // a -P property value, a published filename and a download URL. The
    // parentheses it used to use broke the release workflow outright -- bash
    // reads `(` as a subshell -- and would have needed quoting or
    // percent-encoding everywhere afterwards. A single dash would be
    // ambiguous, since branch names contain dashes themselves; a double dash
    // marks where the branch name starts without introducing a character
    // anything has to escape.
    override fun toString(): String = "$idea-beta${ maybeBranch("--%s") }.$beta"
}

// TODO: Make this support branches other that 'main'
abstract class FetchGitTagRakuPluginBetaVersion : IdeaVersionTask() {
    @get:Input
    val gitBranch: Property<String> = project.objects.property<String>()

    @get:Input
    val gitTag: Property<String> = project.objects.property<String>()

    @Internal
    // TODO|XXX : this will break for beta releases > 10
    // The beta number is everything after the final dot, not the final
    // character: `2026.2-beta.13`.last() is '3', so a two-digit beta parsed
    // as 3 and bumpBetaVersion would have proposed 4 -- walking the series
    // backwards from 13. Betas passed 9 some time ago.
    val version = gitTag.map { it.substringAfterLast('.').toInt() }
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
    // The beta number is everything after the final dot, not the final
    // character: `2026.2-beta.13`.last() is '3', so a two-digit beta parsed
    // as 3 and bumpBetaVersion would have proposed 4 -- walking the series
    // backwards from 13. Betas passed 9 some time ago.
    val version = gitTag.map { it.substringAfterLast('.').toInt() }
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
    id("org.jetbrains.intellij.platform") version "2.18.1"

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
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

kotlin {
    // Compile Kotlin interface methods-with-bodies as real JVM 8 default
    // methods (instead of Kotlin's historical $DefaultImpls indirection), so
    // Java classes implementing a Kotlin interface inherit its defaults the
    // same way they inherit a Java interface's -- without this, converting
    // any Java interface with default methods (e.g. RakuPsiElement) to
    // Kotlin would break every Java implementor that doesn't override them.
    compilerOptions {
        freeCompilerArgs.add("-jvm-default=enable")
    }
}

// The published artifact's filename. Gradle's Zip task always joins its
// name parts with "-", so the underscore has to come from setting
// archiveFileName outright rather than from archiveBaseName/archiveVersion.
//
// Read into a local val first: referring to `project` or `version` from
// inside the provider would capture the Project at execution time, which
// this build forbids (org.gradle.configuration-cache=true).
val artifactVersion = versionFromPropertyPossibly()
tasks.named<Zip>("buildPlugin") {
    archiveFileName.set("${rootProject.name}_$artifactVersion.zip")
}

intellijPlatform {
    pluginConfiguration {
        id = "org.raku.comma"
        name = "Raku"
        version = versionFromPropertyPossibly()

        ideaVersion {
            sinceBuild = "261"
        }
    }

    pluginVerification {
        ides {
            select {
                types = listOf(IntelliJPlatformType.values().toList().random())
                channels = listOf(ProductRelease.Channel.RELEASE)
                sinceBuild = "261"
            }
        }
    }
}

// The platform provides the Kotlin stdlib to every plugin; keep transitive
// copies (e.g. via kotlinx-serialization) out of the shipped distribution.
configurations.runtimeClasspath {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
}

dependencies {
    intellijPlatform {
        intellijIdeaUltimate(ideaBuildVersion)
        bundledPlugin("com.intellij.java")
        bundledModule("intellij.spellchecker")
        bundledPlugin("com.intellij.modules.jcef")

        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.opentest4j:opentest4j:1.3.0")
    // kotlin.stdlib.default.dependency=false keeps the stdlib out of the shipped
    // zip, but the in-process test IDE still needs one that matches the Kotlin
    // compiler version; otherwise coroutine debug metadata (v2) fails to load.
    testRuntimeOnly(kotlin("stdlib"))
    implementation(files("libs/xchart-3.8.0.jar"))
    implementation(files("libs/moarvmremote.jar"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    implementation("io.airlift:aircompressor:2.0.2")
    implementation("org.json:json:20240303")
    // TODO: Remove this due to multiple unpatch CVEs
    implementation("org.tap4j:tap4j:4.4.2")
}

tasks.test {
    // Gradle's 512m default is far too tight for an in-process IntelliJ test
    // application; it GC-thrashes long before it OOMs, which is invisible except
    // as a slow suite.
    maxHeapSize = "3g"
    // Run with -Didea.tests.overwrite.data=true to regenerate golden test data.
    systemProperty("idea.tests.overwrite.data", System.getProperty("idea.tests.overwrite.data", "false"))
    // Run with -Draku.test.dump.actual=true to dump actual output of failing
    // golden-file comparisons under build/actual-dumps for review.
    systemProperty("raku.test.dump.actual", System.getProperty("raku.test.dump.actual", "false"))
    // ParserChangeVersionGuardTest hashes these SOURCES at runtime; without
    // declaring them as inputs, a comment-only parser edit (identical
    // bytecode) would leave the test task up-to-date and silently skip the
    // guard.
    inputs.files(
        "src/main/java/org/raku/comma/parsing/MAINBraid.java",
        "src/main/java/org/raku/comma/parsing/RakuParser.java",
        "src/main/java/org/raku/comma/parsing/RakuConditionalCompilation.java",
    )
}
