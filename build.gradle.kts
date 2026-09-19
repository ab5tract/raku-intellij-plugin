
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
    return safeDetermineCurrentRakuPluginVersion(determineCurrentGitBranch())
}

// The fallback below reads .versions/plugin-version, which is a record
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

        The version would otherwise come from .versions/plugin-version,
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

// The newest beta tag reachable from main, or null when there are none --
// a fresh clone with no tags, or a shallow CI checkout.
//
// `.lines().last()` on empty output yields "" rather than nothing, so the
// blank filter is load-bearing: without it an untagged repo reports a version
// of "" and everything downstream silently builds something unnamed.
fun gitCurrentRakuPluginVersion(): String? =
    providers.exec {
             commandLine("git", "tag", "--merged", "main", "--sort=taggerdate")
         }.standardOutput.asText.get()
          .trim()
          .lines()
          .lastOrNull { it.isNotBlank() }

fun safeDetermineCurrentRakuPluginVersion(currentGitBranch: String): String {
    // On main, reconcile the file against the tags rather than trusting
    // either blindly.
    //
    // The file is the record we keep, but it only advances when
    // bumpPluginVersion runs, so a tag cut by hand leaves it behind -- it had
    // drifted to 2026.1-beta.2 against a latest tag of 2026.2-beta.13, which
    // would have bumped the series backwards by ten. A released tag is
    // evidence that a version exists; the file is not evidence that one does
    // not. So when the tag is ahead, the tag wins, and the next
    // retrieve/bump writes the agreed value straight back into the file,
    // which is what keeps the two from drifting again.
    //
    // Branches keep reading the file alone: `--merged main` cannot see a tag
    // that has not reached main yet.
    if (currentGitBranch == "main") {
        gitCurrentRakuPluginVersion()?.let { return it }
    }

    val pluginVersionPath = Path("${project.projectDir.path}/.versions/plugin-version${ formatBranch(currentGitBranch, ".%s") }")

    return when(pluginVersionPath.exists()) {
        true  -> pluginVersionPath.toFile().readText().trim()
        false -> {
            val idea = File("${project.projectDir.path}/.versions/idea-version").readText(Charsets.UTF_8).trimEnd()
            // Same shape as RakuPluginVersion.toString -- these two must
            // agree, or the version a fresh branch reports would not match
            // the one it bumps to.
            "$idea${ formatBranch(currentGitBranch, "--%s") }.1"
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

    /**
     * The release number: everything after the final dot.
     *
     * Not the final character. `2026.2-beta.13`.last() is '3', so reading it
     * that way parsed a two-digit release as 3 and would have bumped the
     * series backwards from 13 to 4.
     */
    fun releaseNumberOf(version: String): Int = version.substringAfterLast('.').toInt()

    /**
     * The IDEA version a stored version string was cut against, so a bump can
     * tell "next release for this platform" from "first release for a new
     * one". Handles all four shapes this project has produced:
     *
     *     2026.2.13              -> 2026.2
     *     2026.2--a-branch.3     -> 2026.2
     *     2026.2-beta.13         -> 2026.2   (legacy)
     *     2026.2-beta--branch.3  -> 2026.2   (legacy)
     *
     * Order matters: the trailing release number goes first, because in a
     * branch version it sits after the branch name rather than before it.
     */
    fun ideaPartOf(version: String): String =
        version.substringBeforeLast('.')
               .substringBefore("--")
               .removeSuffix("-beta")
}

data class RakuPluginVersion(
    val idea: String,
    // The release number within an IDEA version: the 14 in 2026.2.14. Resets
    // to 1 whenever `idea` changes, since it counts releases against that
    // platform version rather than across all time.
    val release: Int,
    val branch: String,
    val basePath: String
) {
    fun fileName(): String = "$basePath/.versions/plugin-version${ maybeBranch(".%s") }"
    fun maybeBranch(format: String = "%s") = if (branch != "main") format.format(branch) else ""

    // main reads 2026.2.14; a branch reads 2026.2--some-branch.3.
    //
    // The branch separator is a double dash because this string becomes a git
    // tag, a -P property value, a published filename and a download URL. The
    // parentheses it used to use broke the release workflow outright -- bash
    // reads `(` as a subshell -- and would have needed quoting or
    // percent-encoding everywhere afterwards. A single dash would be
    // ambiguous, since branch names contain dashes themselves; a double dash
    // marks where the branch name starts without introducing a character
    // anything has to escape.
    //
    // The release number stays behind a dot in both shapes, so parsing it is
    // one rule rather than two -- see releaseNumberOf below.
    override fun toString(): String = "$idea${ maybeBranch("--%s") }.$release"
}

// TODO: Make this support branches other that 'main'
abstract class FetchGitTagRakuPluginVersion : IdeaVersionTask() {
    @get:Input
    val gitBranch: Property<String> = project.objects.property<String>()

    @get:Input
    val gitTag: Property<String> = project.objects.property<String>()

    @Internal
    val version = gitTag.map { releaseNumberOf(it) }
    @Internal
    val pluginVersion: Provider<RakuPluginVersion> = version.map { determinePluginVersion(it) }

    fun determinePluginVersion(version: Int): RakuPluginVersion
                =   RakuPluginVersion(
                        idea     = ideaVersion,
                        release  = version,
                        branch   = gitBranch.get(),
                        basePath = basePath)

    @TaskAction
    override fun action() {
        println(pluginVersion.get().toString())
    }
}

abstract class GetRakuPluginVersion : IdeaVersionTask() {
    @get:Input
    val gitBranch: Property<String> = project.objects.property<String>()

    @get:Input
    val gitTag: Property<String> = project.objects.property<String>()

    @Internal
    val version = gitTag.map { releaseNumberOf(it) }
    @Internal
    val pluginVersion: Provider<RakuPluginVersion> = version.map { determinePluginVersion(it) }

    @get:OutputFile
    val pluginVersionFile: Provider<File> = pluginVersion.map { File(it.fileName()) }

    fun determinePluginVersion(version: Int): RakuPluginVersion
                =   RakuPluginVersion(
                        idea   = ideaVersion,
                        release = version,
                        branch = gitBranch.get(),
                        basePath = basePath)

    @TaskAction
    override fun action() {
        pluginVersionFile.get().parentFile.mkdirs()
        pluginVersionFile.get().writeText(pluginVersion.get().toString())
        println(pluginVersion.get().toString())
    }
}

abstract class BumpRakuPluginVersion: GetRakuPluginVersion() {
    @TaskAction
    override fun action() {
        // Compare against the IDEA version parsed out of the STORED version,
        // not against pluginVersion.idea -- determinePluginVersion always
        // fills that in from the current idea-version file, so the two were
        // trivially equal and the reset below could never fire. It matters
        // now that bumpIdeaVersion exists: the first plugin release after a
        // platform bump should be .1, not a continuation of the old series.
        val stored = gitTag.get()
        val continuingSamePlatform = ideaPartOf(stored) == ideaVersion

        val newPluginVersion = RakuPluginVersion(
            idea     = ideaVersion,
            release  = if (continuingSamePlatform) releaseNumberOf(stored) + 1 else 1,
            branch   = gitBranch.get(),
            basePath = basePath)

        pluginVersionFile.get().parentFile.mkdirs()
        pluginVersionFile.get().writeText(newPluginVersion.toString())
        println(newPluginVersion)
    }
}
abstract class BumpIdeaVersion : IdeaVersionTask() {
    @get:OutputFile
    val outputFile: File = File(ideaFileName)

    /** -PideaVersion=2027.1 -- set it outright, for anything unusual. */
    @get:Input
    @get:Optional
    val explicitVersion: Property<String> = project.objects.property<String>()

    /** -PnewYear -- roll the year and restart the minor at 1. */
    @get:Input
    val startNewYear: Property<Boolean> = project.objects.property<Boolean>().convention(false)

    /**
     * The next platform version.
     *
     * The minor is NOT capped. JetBrains currently ship three releases a year,
     * but that is a habit rather than a rule -- they have shipped other counts
     * before and nothing promises they will not again. A build that "knew" the
     * cadence would silently produce 2027.1 in a year with a fourth release,
     * so rolling the year is something the caller states rather than something
     * this infers.
     */
    fun next(current: String): String {
        explicitVersion.orNull?.takeIf { it.isNotBlank() }?.let { return it }

        val year = current.substringBefore('.').toIntOrNull()
        val minor = current.substringAfter('.', "").toIntOrNull()
        require(year != null && minor != null) {
            "Cannot read an IDEA version from '$current'; expected something like 2026.2."
        }
        return if (startNewYear.get()) "${year!! + 1}.1" else "$year.${minor!! + 1}"
    }

    @TaskAction
    override fun action() {
        val bumped = next(ideaVersion)
        outputFile.writeText(bumped + "\n")
        println(bumped)
        // Deliberately not touching the plugin version file here. The next
        // bumpPluginVersion sees that the stored version was cut against the
        // old platform and restarts the series at .1 by itself, so doing it
        // here as well would just be a second place to get it wrong.
        logger.lifecycle(
            "IDEA version is now $bumped. The next bumpPluginVersion will restart at $bumped.1.")
    }
}
////// END VERSION STUFF

tasks.register<IdeaVersionTask>("retrieveIdeaVersion") {
    group = "version"
    description = "Retrieve IntelliJ IDEA version"
}

tasks.register<BumpIdeaVersion>("bumpIdeaVersion") {
    group = "version"
    description = "Bump IntelliJ IDEA version (-PnewYear to roll the year, " +
                  "-PideaVersion=X to set it outright)"

    explicitVersion = providers.gradleProperty("ideaVersion").orNull
    startNewYear = project.hasProperty("newYear")
}

tasks.register<FetchGitTagRakuPluginVersion>("findVersionFromGitTag") {
    group = "version"
    description = "Determine plugin beta version based on git tags"

    gitTag = gitCurrentRakuPluginVersion()
    gitBranch = "main"
}

tasks.register<GetRakuPluginVersion>("retrievePluginVersion") {
    group = "version"
    description = "Retrieve plugin version"

    gitTag = safeDetermineCurrentRakuPluginVersion(currentGitBranch = determineCurrentGitBranch())
    gitBranch = determineCurrentGitBranch()
}

tasks.register<BumpRakuPluginVersion>("bumpPluginVersion") {
    group = "version"
    description = "Bump plugin version"

    gitTag = safeDetermineCurrentRakuPluginVersion(currentGitBranch = determineCurrentGitBranch())
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
