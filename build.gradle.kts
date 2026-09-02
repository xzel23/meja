// Copyright 2019 Axel Howind
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

@file:Suppress("UnstableApiUsage")

import com.adarshr.gradle.testlogger.theme.ThemeType
import com.dua3.cabe.processor.Configuration
import com.dua3.meja.release.PrepareReleaseTask
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.MessageDigest
import java.util.Properties
import org.gradle.internal.extensions.stdlib.toDefaultLowerCase

plugins {
    id("java-library")
    id("jvm-test-suite")
    id("version-catalog")
    id("signing")
    id("idea")
    id("jacoco-report-aggregation")
    alias(libs.plugins.jdk)
    alias(libs.plugins.test.logger)
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.cabe)
    alias(libs.plugins.jmh)
    alias(libs.plugins.sonar)
    alias(libs.plugins.jreleaser)
}

/////////////////////////////////////////////////////////////////////////////
// Meta data object
/////////////////////////////////////////////////////////////////////////////
object Meta {
    const val DESCRIPTION = "Meja spreadsheet library."
    const val INCEPTION_YEAR = "2015"
    const val GROUP = "com.dua3.meja"
    const val SCM = "https://github.com/xzel23/meja.git"
    const val LICENSE_NAME = "The Apache Software License, Version 2.0"
    const val LICENSE_URL = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    const val DEVELOPER_ID = "axh"
    const val DEVELOPER_NAME = "Axel Howind"
    const val DEVELOPER_EMAIL = "axh@dua3.com"
    const val ORGANIZATION_NAME = "dua3"
    const val ORGANIZATION_URL = "https://www.dua3.com"
}

private val publishableModuleNames = listOf(
    "meja-core", "meja-db", "meja-fx", "meja-generic", "meja-poi", "meja-swing", "meja-ui"
)

private data class PreparedModule(val version: String, val sourceRevision: String, val selected: Boolean)
private data class PreparedPlan(val type: String, val bomVersion: String, val sourceRevision: String, val modules: Map<String, PreparedModule>)

private fun parseReleaseToml(file: File): Map<String, Map<String, String>> {
    var section = ""
    val values = mutableMapOf<String, MutableMap<String, String>>()
    val table = Regex("""^\[([A-Za-z0-9_.-]+)]$""")
    val value = Regex("""^([A-Za-z][A-Za-z0-9_-]*)\s*=\s*(.+)$""")
    file.forEachLine { raw ->
        val line = raw.substringBefore('#').trim()
        if (line.isEmpty()) return@forEachLine
        table.matchEntire(line)?.let { section = it.groupValues[1]; values.getOrPut(section) { mutableMapOf() }; return@forEachLine }
        value.matchEntire(line)?.let {
            values.getOrPut(section) { mutableMapOf() }[it.groupValues[1]] = it.groupValues[2].trim().removeSurrounding("\"")
            return@forEachLine
        }
        throw GradleException("unsupported release TOML syntax in ${file.path}: $line")
    }
    return values
}

private fun readPreparedPlan(file: File): PreparedPlan {
    check(file.isFile) { "no prepared release plan exists at ${file.path}" }
    val values = parseReleaseToml(file)
    val release = values["release"] ?: error("[release] table missing from ${file.path}")
    val modules = publishableModuleNames.associateWith { name ->
        val module = values["modules.$name"] ?: error("[modules.$name] table missing from ${file.path}")
        PreparedModule(
            module["version"] ?: error("modules.$name.version missing"),
            module["sourceRevision"] ?: error("modules.$name.sourceRevision missing"),
            module["selected"]?.toBooleanStrictOrNull() ?: error("modules.$name.selected missing or invalid")
        )
    }
    return PreparedPlan(
        release["releaseType"] ?: error("release.releaseType missing"),
        release["bomVersion"] ?: error("release.bomVersion missing"),
        release["sourceRevision"] ?: error("release.sourceRevision missing"), modules
    )
}

private fun git(vararg args: String): Pair<Int, String> {
    val output = ByteArrayOutputStream()
    val process = ProcessBuilder(listOf("git") + args).directory(rootDir).redirectErrorStream(true).start()
    process.inputStream.copyTo(output)
    return process.waitFor() to output.toString(StandardCharsets.UTF_8).trim()
}

private fun requireGit(description: String, vararg args: String): String {
    val (status, output) = git(*args)
    check(status == 0) { "$description failed: $output" }
    return output
}

private fun sha256(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().use { input ->
        val buffer = ByteArray(64 * 1024)
        while (true) { val count = input.read(buffer); if (count < 0) break; digest.update(buffer, 0, count) }
    }
    return digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
}

private fun isMavenCentralCoordinatePublished(artifactId: String, version: String): Boolean {
    val path = "${Meta.GROUP.replace('.', '/')}/$artifactId/$version/$artifactId-$version.pom"
    val connection = (URI("https://repo1.maven.org/maven2/$path").toURL().openConnection() as HttpURLConnection).apply {
        requestMethod = "HEAD"; connectTimeout = 10_000; readTimeout = 10_000
    }
    return try {
        when (connection.responseCode) {
            HttpURLConnection.HTTP_NOT_FOUND -> false
            in 200..399 -> true
            else -> throw GradleException("could not check Maven Central for $artifactId:$version (HTTP ${connection.responseCode})")
        }
    } finally { connection.disconnect() }
}

/////////////////////////////////////////////////////////////////////////////
// Root project configuration
/////////////////////////////////////////////////////////////////////////////

project.description = Meta.DESCRIPTION

private val japicmpTool = configurations.create("japicmpTool") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    add(japicmpTool.name, "com.github.siom79.japicmp:japicmp:0.26.1:jar-with-dependencies") {
        isTransitive = false
    }
}

tasks.register("printVersion") {
    description = "Print the project version to stdout."
    group = HelpTasksPlugin.HELP_GROUP
    val version = project.version.toString()
    doLast { println(version) }
}

@Suppress("UNCHECKED_CAST")
private val configuredModuleVersions = gradle.extra["releaseModuleVersions"] as Map<String, String>
@Suppress("UNCHECKED_CAST")
private val selectedReleaseModules = gradle.extra["releaseSelectedModules"] as Set<String>
private val releasePlanPresent = gradle.extra["releasePlanPresent"] as Boolean
private val releaseStateFile = gradle.extra["releaseStateFile"] as File
private val preparedReleasePlanFile = gradle.extra["preparedReleasePlanFile"] as File
private val ciReleaseBundleMode = providers.gradleProperty("ciReleaseBundle").map(String::toBoolean).orElse(false).get()
private val prebuiltReleaseBundleMode = providers.gradleProperty("prebuiltReleaseBundle").map(String::toBoolean).orElse(false).get()

tasks.register<PrepareReleaseTask>("prepareRelease") {
    group = "release"
    description = "Plans a selective release; add -PconfirmRelease=true to write gradle/prepared-release.toml."
    repositoryDirectory.set(layout.projectDirectory)
    releaseStateFile.set(layout.projectDirectory.file("gradle/release-state.toml"))
    preparedReleasePlanPath.set(layout.projectDirectory.file("gradle/prepared-release.toml").asFile.absolutePath)
    releaseType.convention(providers.gradleProperty("releaseType").orElse(""))
    requestedReleaseVersion.convention(providers.gradleProperty("releaseVersion").orElse(""))
    additionalReleaseModules.convention(providers.gradleProperty("additionalReleaseModules").orElse(""))
    confirmRelease.convention(providers.gradleProperty("confirmRelease").map { it == "true" }.orElse(false))
}

tasks.register("verifyPreparedRelease") {
    group = "release"
    description = "Validates the persisted prepared plan, configured versions, and selected modules."
    notCompatibleWithConfigurationCache("The task reads release state and Git history at execution time.")
    doLast {
        check(releasePlanPresent) { "no prepared release plan exists at ${preparedReleasePlanFile.path}" }
        val plan = readPreparedPlan(preparedReleasePlanFile)
        check(plan.type in setOf("patch", "minor", "major")) { "invalid prepared release type: ${plan.type}" }
        check(plan.modules.filterValues { it.selected }.keys == selectedReleaseModules) { "prepared plan selection changed during configuration" }
        check(project.version.toString() == plan.bomVersion) { "configured BOM version does not match prepared plan" }
        check(git("merge-base", "--is-ancestor", plan.sourceRevision, "HEAD").first == 0) { "prepared source revision is not an ancestor of HEAD" }
        plan.modules.forEach { (name, module) -> check(configuredModuleVersions[name] == module.version) { "configured version differs for $name" } }
        logger.lifecycle("Prepared release plan is valid.")
    }
}

tasks.register("checkReleaseCompatibility") {
    group = "verification"
    description = "Checks selected patch-release modules against their last published binary API."
    dependsOn("verifyPreparedRelease")
    doLast {
        val plan = readPreparedPlan(preparedReleasePlanFile)
        if (plan.type != "patch") {
            logger.lifecycle("Skipping binary compatibility enforcement for ${plan.type} release ${plan.bomVersion}.")
            return@doLast
        }
        val state = parseReleaseToml(releaseStateFile)
        plan.modules.filterValues { it.selected }.forEach { (module, candidate) ->
            val oldVersion = state.getValue("modules.$module").getValue("version")
            val previous = layout.buildDirectory.file("release-compatibility/$module-$oldVersion.jar").get().asFile
            if (!previous.isFile) {
                previous.parentFile.mkdirs()
                URI("https://repo1.maven.org/maven2/$groupPath/$module/$oldVersion/$module-$oldVersion.jar").toURL()
                    .openStream().use { Files.copy(it, previous.toPath()) }
            }
            val current = if (prebuiltReleaseBundleMode) {
                stagingDirectory.resolve("$groupPath/$module/${candidate.version}/$module-${candidate.version}.jar")
            } else {
                project(":$module").layout.buildDirectory.file("libs/$module-${candidate.version}.jar").get().asFile
            }
            check(current.isFile) { "candidate artifact was not built: ${current.path}" }
            val process = ProcessBuilder(
                File(System.getProperty("java.home"), "bin/java").absolutePath,
                "-jar", japicmpTool.singleFile.absolutePath,
                "--old", previous.absolutePath, "--new", current.absolutePath,
                "--only-modified", "--error-on-binary-incompatibility",
                "--error-on-source-incompatibility", "--ignore-missing-classes"
            ).inheritIO().start()
            check(process.waitFor() == 0) { "binary compatibility check failed for $module" }
        }
    }
}

// Aggregate all subprojects for JaCoCo report aggregation

dependencies {
    // Aggregate all subprojects for JaCoCo report aggregation
    jacocoAggregation(rootProject)
    jacocoAggregation(project(":meja-core"))
    jacocoAggregation(project(":meja-generic"))
    jacocoAggregation(project(":meja-poi"))
    jacocoAggregation(project(":meja-ui"))
    jacocoAggregation(project(":meja-fx"))
    jacocoAggregation(project(":meja-swing"))
    jacocoAggregation(project(":meja-db"))
}

tasks.named<JacocoReport>("testCodeCoverageReport") {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    // use Cabe instrumented classes if they exist
    classDirectories.setFrom(project.provider {
        val aggregatedProjectPaths = listOf(
            ":",
            ":meja-core",
            ":meja-generic",
            ":meja-poi",
            ":meja-ui",
            ":meja-fx",
            ":meja-swing",
            ":meja-db"
        )

        aggregatedProjectPaths.flatMap { path ->
            val p = project.project(path)
            val cabeClasses = p.layout.buildDirectory.dir("classes-cabe/main").get().asFile
            val mainClasses = p.layout.buildDirectory.dir("classes/java/main").get().asFile
            if (cabeClasses.exists()) {
                listOf<File>(cabeClasses)
            } else if (mainClasses.exists()) {
                listOf<File>(mainClasses)
            } else {
                emptyList<File>()
            }
        }
    })
}

// SonarQube root project config
sonar {
    properties {
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "${layout.buildDirectory.get()}/reports/jacoco/testCodeCoverageReport/testCodeCoverageReport.xml"
        )
        property("sonar.coverage.exclusions", "**/samples/**")

        // use Cabe instrumented classes if they exist
        val cabeClassesDir = project.layout.buildDirectory.dir("classes-cabe/main").get().asFile
        if (cabeClassesDir.exists()) {
            property("sonar.java.binaries", "build/classes-cabe/main")
        }
    }
}

// check for development/release version
fun isDevelopmentVersion(versionString: String): Boolean {
    val v = versionString.toDefaultLowerCase()
    val markers = listOf("snapshot", "alpha", "beta")
    return markers.any { marker -> v.contains("-$marker") || v.contains(".$marker") }
}

val isReleaseVersion = !isDevelopmentVersion(project.version.toString())
val isSnapshot = project.version.toString().toDefaultLowerCase().contains("snapshot")

allprojects {
    dependencyLocking {
        lockAllConfigurations()
    }

    if (!project.name.endsWith("-bom")) {
        apply(plugin = "java-library")
        apply(plugin = rootProject.libs.plugins.jdk.get().pluginId)

        jdk {
            version = rootProject.libs.versions.jdkVersion.get().toInt()
            langVersion = rootProject.libs.versions.jdkVersion.get().toInt()
            vendor = JvmVendorSpec.BELLSOFT
            javaFxBundled = true
            nativeImageCapable = false
        }
    }
}

/////////////////////////////////////////////////////////////////////////////
// Subprojects configuration
/////////////////////////////////////////////////////////////////////////////

subprojects {

    // Apply common plugins
    apply(plugin = "maven-publish")
    apply(plugin = "version-catalog")
    apply(plugin = "signing")
    apply(plugin = "idea")
    apply(plugin = rootProject.libs.plugins.jdk.get().pluginId)
    apply(plugin = rootProject.libs.plugins.test.logger.get().pluginId)

    // Skip some plugins for BOM project
    if (!project.name.endsWith("-bom")) {
        apply(plugin = "jacoco")
        apply(plugin = "jvm-test-suite")
        apply(plugin = rootProject.libs.plugins.spotbugs.get().pluginId)
        apply(plugin = rootProject.libs.plugins.cabe.get().pluginId)
        apply(plugin = rootProject.libs.plugins.jmh.get().pluginId)
    }

    // Java configuration for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        java {
            withJavadocJar()
            withSourcesJar()
        }

        cabe {
            if (isReleaseVersion) {
                config.set(Configuration.parse("publicApi=THROW_NPE:privateApi=ASSERT:strict=true"))
            } else {
                config.set(Configuration.DEVELOPMENT.withStrict(true))
            }
        }

        // JaCoCo
        tasks.withType<JacocoReport> {
            reports {
                xml.required.set(true)
                html.required.set(false)
            }

            // use Cabe instrumented classes if they exist
            val cabeClasses = project.layout.buildDirectory.dir("classes-cabe/main")
            classDirectories.setFrom(project.provider {
                if (cabeClasses.get().asFile.exists()) {
                    val mainClassesDir = project.layout.buildDirectory.dir("classes/java/main").get().asFile
                    sourceSets.main.get().output.classesDirs.filter { it != mainClassesDir } + cabeClasses.get().asFile
                } else {
                    sourceSets.main.get().output.classesDirs
                }
            })
        }

        tasks.withType<Test> {
            useJUnitPlatform()
            finalizedBy(tasks.jacocoTestReport)
        }
    }

    // SonarQube properties
    sonar {
        properties {
            property("sonar.coverage.jacoco.xmlReportPaths", "**/build/reports/jacoco/test/jacocoTestReport.xml")
            property("sonar.coverage.exclusions", "**/samples/**")

            // use Cabe instrumented classes if they exist
            val cabeClassesDir = project.layout.buildDirectory.dir("classes-cabe/main").get().asFile
            if (cabeClassesDir.exists()) {
                property("sonar.java.binaries", "build/classes-cabe/main")
            }
        }
    }

    // Dependencies for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        dependencies {
            api(rootProject.libs.dua3.utility)
            implementation(platform(rootProject.libs.dua3.utility.bom))
            implementation(rootProject.libs.jspecify)
            implementation(platform(rootProject.libs.log4j.bom))
            implementation(rootProject.libs.log4j.api)

            testImplementation(platform(rootProject.libs.junit.bom))
            testImplementation(rootProject.libs.junit.jupiter.api)
            testRuntimeOnly(rootProject.libs.junit.jupiter.engine)
        }

        idea {
            module {
                inheritOutputDirs = false
                outputDir = project.layout.buildDirectory.file("classes/java/main/").get().asFile
                testOutputDir = project.layout.buildDirectory.file("classes/java/test/").get().asFile
            }
        }

        testing {
            suites {
                val test by getting(JvmTestSuite::class) {
                    useJUnitJupiter()
                    dependencies {
                        implementation(platform(rootProject.libs.slb4j.bom))
                        implementation(rootProject.libs.slb4j)
                    }
                    targets {
                        all {
                            testTask {
                                // enable assertions and use headless mode for AWT in unit tests
                                jvmArgs(
                                    "-ea",
                                    "-Djava.awt.headless=true",
                                    "-Dprism.order=sw",
                                    "-Dsun.java2d.d3d=false",
                                    "-Dsun.java2d.opengl=false",
                                    "-Dsun.java2d.pmoffscreen=false"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    testlogger {
        theme = ThemeType.MOCHA_PARALLEL
    }

    // Java compilation and Javadoc config for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        tasks.compileJava {
            options.encoding = "UTF-8"
            options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:-module"))
            options.javaModuleVersion.set(provider { project.version as String })
        }
        tasks.compileTestJava {
            options.encoding = "UTF-8"
            options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:-module"))
        }
        tasks.javadoc {
            (options as StandardJavadocDocletOptions).apply {
                encoding = "UTF-8"
                addStringOption("Xdoclint:all,-missing/private")
                locale = "en_US"
            }
        }
    }

    // JMH config for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        jmh {
            warmupIterations = 2
            iterations = 5
            fork = 1
        }
    }

    // SpotBugs for non-BOM projects
    if (!project.name.endsWith("-bom")) {

        // === SPOTBUGS ===
        spotbugs {
            excludeFilter.set(rootProject.file("spotbugs-exclude.xml"))
        }

        tasks.named<com.github.spotbugs.snom.SpotBugsTask>("spotbugsMain") {
            reports.create("html") {
                required.set(true)
                outputLocation.set(layout.buildDirectory.file("reports/spotbugs/main.html"))
                setStylesheet("fancy-hist.xsl")
            }
        }

        tasks.named<com.github.spotbugs.snom.SpotBugsTask>("spotbugsTest") {
            reports.create("html") {
                required.set(true)
                outputLocation.set(layout.buildDirectory.file("reports/spotbugs/test.html"))
                setStylesheet("fancy-hist.xsl")
            }
        }
    }

    // Jar duplicates strategy for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        tasks.withType<Jar> {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }

    // --- PUBLISHING ---

    if (name == "meja-bom" || name in publishableModuleNames) {
    configure<PublishingExtension> {
        // Repositories for publishing
        repositories {
            // Sonatype snapshots for snapshot versions
            if (isSnapshot) {
                maven {
                    name = "sonatypeSnapshots"
                    url = uri("https://central.sonatype.com/repository/maven-snapshots/")
                    credentials {
                        username = System.getenv("SONATYPE_USERNAME")
                        password = System.getenv("SONATYPE_PASSWORD")
                    }
                }
            }

            // Always add root-level staging directory for JReleaser
            maven {
                name = "stagingDirectory"
                url = rootProject.layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
            }
        }

        // Publications for non-BOM projects
        if (!project.name.endsWith("-bom")) {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])

                    groupId = Meta.GROUP
                    artifactId = project.name
                    version = project.version.toString()

                    pom {
                        name.set(project.name)
                        description.set(project.description)
                        url.set(Meta.SCM)

                        licenses {
                            license {
                                name.set(Meta.LICENSE_NAME)
                                url.set(Meta.LICENSE_URL)
                            }
                        }

                        developers {
                            developer {
                                id.set(Meta.DEVELOPER_ID)
                                name.set(Meta.DEVELOPER_NAME)
                                email.set(Meta.DEVELOPER_EMAIL)
                                organization.set(Meta.ORGANIZATION_NAME)
                                organizationUrl.set(Meta.ORGANIZATION_URL)
                            }
                        }

                        scm {
                            connection.set("scm:git:${Meta.SCM}")
                            developerConnection.set("scm:git:${Meta.SCM}")
                            url.set(Meta.SCM)
                        }

                        withXml {
                            val root = asNode()
                            root.appendNode("inceptionYear", "2015")
                        }
                    }
                }
            }
        }
    }

    // Task to publish to staging directory per subproject
    val publishToStagingDirectory by tasks.registering {
        group = "publishing"
        description = "Publish artifacts to root staging directory for JReleaser"

        dependsOn(tasks.withType<PublishToMavenRepository>().matching {
            it.repository.name == "stagingDirectory"
        })
    }

    // A prepared plan may stage only its selected library modules and the BOM.
    if (releasePlanPresent && name != "meja-bom" && name !in selectedReleaseModules) {
        tasks.withType<PublishToMavenRepository>().configureEach {
            if (repository.name == "stagingDirectory") {
                onlyIf("module is not selected by the prepared release plan") { false }
            }
        }
    }
    if (ciReleaseBundleMode) {
        tasks.withType<org.gradle.plugins.signing.Sign>().configureEach {
            onlyIf("signing is deferred to the protected release workflow") { false }
        }
    }
    }

    // Signing configuration deferred until after evaluation
    afterEvaluate {
        configure<SigningExtension> {
            val shouldSign = !project.version.toString().lowercase().contains("snapshot")
            setRequired(shouldSign && gradle.taskGraph.hasTask("publish"))

            val publishing = project.extensions.getByType<PublishingExtension>()

            if (project.name.endsWith("-bom")) {
                if (publishing.publications.names.contains("bomPublication")) {
                    sign(publishing.publications["bomPublication"])
                }
            } else {
                if (publishing.publications.names.contains("mavenJava")) {
                    sign(publishing.publications["mavenJava"])
                }
            }
        }
    }

    // set the project description after evaluation because it is not yet visible when the POM is first created
    afterEvaluate {
        project.extensions.configure<PublishingExtension> {
            publications.withType<MavenPublication> {
                pom {
                    if (description.orNull.isNullOrBlank()) {
                        description.set(project.description ?: "No description provided")
                    }
                }
            }
        }
    }
}

/////////////////////////////////////////////////////////////////////////////
// Root project tasks and JReleaser configuration
/////////////////////////////////////////////////////////////////////////////

// Aggregate all subprojects' publishToStagingDirectory tasks into a root-level task
tasks.register("publishToStagingDirectory") {
    group = "publishing"
    description = "Publish all subprojects' artifacts to root staging directory for JReleaser"

    dependsOn(subprojects.filter { it.name == "meja-bom" || it.name in publishableModuleNames }
        .filter { !releasePlanPresent || it.name == "meja-bom" || it.name in selectedReleaseModules }
        .mapNotNull { it.tasks.findByName("publishToStagingDirectory") })
}

private val stagingDirectory = layout.buildDirectory.dir("staging-deploy").get().asFile
private val releaseBundleDirectory = layout.buildDirectory.dir("release-bundle").get().asFile
private val releaseBundleManifest = releaseBundleDirectory.resolve("manifest.sha256")
private val releaseBundleMetadata = releaseBundleDirectory.resolve("metadata.properties")
private val groupPath = Meta.GROUP.replace('.', '/')

private fun stagingFiles() = stagingDirectory.walkTopDown().filter(File::isFile).sortedBy { it.relativeTo(stagingDirectory).path }.toList()

private fun validateReleaseBundle(plan: PreparedPlan) {
    check(stagingDirectory.isDirectory && releaseBundleManifest.isFile && releaseBundleMetadata.isFile) { "CI release bundle is incomplete" }
    val metadata = Properties().also { releaseBundleMetadata.inputStream().use(it::load) }
    check(metadata.getProperty("commit") == requireGit("resolving current revision", "rev-parse", "HEAD")) { "release bundle belongs to a different commit" }
    check(metadata.getProperty("sourceRevision") == plan.sourceRevision && metadata.getProperty("bomVersion") == plan.bomVersion) { "release bundle does not match the prepared plan" }
    check(metadata.getProperty("selectedModules") == selectedReleaseModules.sorted().joinToString(",")) { "release bundle selection does not match the prepared plan" }
    val manifest = releaseBundleManifest.readLines().associate { line ->
        val separator = line.indexOf("  ")
        check(separator == 64) { "invalid release bundle manifest" }
        val path = line.substring(separator + 2)
        check(path.startsWith("staging-deploy/")) { "release bundle manifest path is outside staging: $path" }
        val relativePath = path.removePrefix("staging-deploy/")
        check(relativePath.isNotBlank() && !relativePath.startsWith("/") && !relativePath.split('/').contains("..")) {
            "invalid release bundle manifest path: $path"
        }
        relativePath to line.substring(0, separator)
    }
    check(manifest.keys == stagingFiles().map { it.relativeTo(stagingDirectory).invariantSeparatorsPath }.toSet()) { "release bundle manifest does not match staging" }
    manifest.forEach { (path, digest) -> check(sha256(stagingDirectory.resolve(path)) == digest) { "release bundle checksum mismatch: $path" } }
    fun required(module: String, version: String, suffix: String) = stagingDirectory.resolve("$groupPath/$module/$version/$module-$version$suffix").isFile
    check(required("meja-bom", plan.bomVersion, ".pom") && required("meja-bom", plan.bomVersion, ".module")) { "release bundle is missing the BOM" }
    plan.modules.filterValues { it.selected }.forEach { (module, item) ->
        listOf(".jar", "-sources.jar", "-javadoc.jar", ".pom", ".module").forEach { suffix -> check(required(module, item.version, suffix)) { "release bundle is missing $module$item$suffix" } }
    }
    check(stagingFiles().none { it.name.endsWith(".asc") }) { "CI release bundle must be unsigned" }
}

val cleanPreparedReleaseStaging = tasks.register<Delete>("cleanPreparedReleaseStaging") {
    group = "release"
    delete(stagingDirectory)
}

val prepareCiReleaseBundle = tasks.register("prepareCiReleaseBundle") {
    group = "release"
    description = "Creates an unsigned, checksummed Maven bundle from the CI build outputs."
    dependsOn("verifyPreparedRelease", cleanPreparedReleaseStaging, "publishToStagingDirectory")
    doLast {
        check(ciReleaseBundleMode) { "prepareCiReleaseBundle requires -PciReleaseBundle=true" }
        val plan = readPreparedPlan(preparedReleasePlanFile)
        releaseBundleDirectory.deleteRecursively(); releaseBundleDirectory.mkdirs()
        releaseBundleMetadata.writeText("commit=${requireGit("resolving bundle revision", "rev-parse", "HEAD")}\nsourceRevision=${plan.sourceRevision}\nbomVersion=${plan.bomVersion}\nselectedModules=${selectedReleaseModules.sorted().joinToString(",")}\n")
        // The artifact is restored into build/, so preserve staging-deploy in manifest paths.
        // This is the same bundle layout consumed by the utility release workflow.
        releaseBundleManifest.writeText(stagingFiles().joinToString("\n", postfix = "\n") { "${sha256(it)}  staging-deploy/${it.relativeTo(stagingDirectory).invariantSeparatorsPath}" })
        validateReleaseBundle(plan)
    }
}

val verifyCiReleaseBundle = tasks.register("verifyCiReleaseBundle") {
    group = "release"
    description = "Verifies the checksummed Maven bundle produced by CI."
    doLast {
        check(prebuiltReleaseBundleMode) { "verifyCiReleaseBundle requires -PprebuiltReleaseBundle=true" }
        validateReleaseBundle(readPreparedPlan(preparedReleasePlanFile))
    }
}

tasks.register("publishSnapshotsToMavenLocal") {
    group = "publishing"
    onlyIf { isSnapshot }
    dependsOn(publishableModuleNames.map { ":$it:publishToMavenLocal" } + ":meja-bom:publishToMavenLocal")
}

val jreleaserDeploy = tasks.named("jreleaserDeploy")
tasks.register("stagePreparedRelease") { dependsOn("verifyPreparedRelease", "checkReleaseCompatibility", cleanPreparedReleaseStaging, "publishToStagingDirectory") }
tasks.register("publishPreparedRelease") { dependsOn("stagePreparedRelease", jreleaserDeploy) }
tasks.register("publishPreparedReleaseFromCi") { dependsOn("verifyPreparedRelease", verifyCiReleaseBundle, "checkReleaseCompatibility", jreleaserDeploy) }
jreleaserDeploy.configure { mustRunAfter("verifyPreparedRelease", verifyCiReleaseBundle, "checkReleaseCompatibility") }

tasks.register("finalizeRelease") {
    group = "release"
    description = "Records a published prepared release, advances development, and creates its Git tag."
    notCompatibleWithConfigurationCache("Finalization modifies release files and Git state.")
    doLast {
        check(providers.gradleProperty("confirmFinalize").orNull == "true") { "re-run with -PconfirmFinalize=true" }
        val plan = readPreparedPlan(preparedReleasePlanFile)
        buildList {
            add("meja-bom" to plan.bomVersion)
            plan.modules.filterValues { it.selected }.forEach { (name, module) -> add(name to module.version) }
        }.forEach { (artifact, version) ->
            check(isMavenCentralCoordinatePublished(artifact, version)) { "Maven Central does not yet expose $artifact:$version" }
        }
        val releaseState = parseReleaseToml(releaseStateFile)
        val stateText = buildString {
            appendLine("[release]"); appendLine("schemaVersion = 2"); appendLine("bomVersion = \"${plan.bomVersion}\"")
            appendLine("publishedRevision = \"${plan.sourceRevision}\"")
            publishableModuleNames.forEach { module ->
                val old = releaseState.getValue("modules.$module")
                val current = plan.modules.getValue(module)
                appendLine(); appendLine("[modules.$module]")
                appendLine("version = \"${if (current.selected) current.version else old.getValue("version")}\"")
                appendLine("publishedRevision = \"${if (current.selected) current.sourceRevision else old.getValue("publishedRevision")}\"")
                appendLine("paths = ${old.getValue("paths")}")
            }
        }
        releaseStateFile.writeText(stateText)
        val nextVersion = plan.bomVersion.split('.').let { "${it[0]}.${it[1]}.${it[2].toInt() + 1}-SNAPSHOT" }
        val versionCatalog = file("gradle/version.toml")
        versionCatalog.writeText(versionCatalog.readText().replace(Regex("""(?m)^(\\s*projectVersion\\s*=\\s*")[^"]+(".*)$"""), "${'$'}1$nextVersion${'$'}2"))
        Files.delete(preparedReleasePlanFile.toPath())
        requireGit("staging release state", "add", "gradle/release-state.toml", "gradle/version.toml", "gradle/prepared-release.toml")
        requireGit("committing release state", "commit", "-m", "Release ${plan.bomVersion}")
        val tag = "v${plan.bomVersion}"
        requireGit("creating release tag", "tag", "-a", tag, "-m", "Release ${plan.bomVersion}")
        if (providers.gradleProperty("pushReleaseTag").orNull == "true") {
            val branch = providers.gradleProperty("releaseBranch").orNull ?: error("supply -PreleaseBranch when pushing from detached HEAD")
            requireGit("pushing finalized release", "push", "origin", "HEAD:refs/heads/$branch")
            requireGit("pushing release tag", "push", "origin", tag)
        }
    }
}

// add a task to create aggregate javadoc in the root projects build/docs/javadoc folder
tasks.register<Javadoc>("aggregateJavadoc") {
    group = "documentation"
    description = "Generates aggregated Javadoc for all subprojects"
    executable = jdk.jdkHome.get()
        .file("bin/javadoc${if (System.getProperty("os.name").contains("Windows", ignoreCase = true)) ".exe" else ""}")
        .toString()
    setDestinationDir(layout.buildDirectory.dir("docs/javadoc").get().asFile)
    setTitle("${rootProject.name} ${project.version} API")

    // Disable module path inference
    modularity.inferModulePath.set(false)

    // Configure the task to depend on all subprojects' javadoc tasks
    val filteredProjects = subprojects.filter {
        !it.name.endsWith("-bom") && !it.name.contains("samples")
    }

    dependsOn(filteredProjects.map { it.tasks.named("javadoc") })

    // Collect all Java source directories from subprojects, excluding module-info.java files
    source(filteredProjects.flatMap { project ->
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val main = sourceSets.findByName("main")
        main?.allJava?.filter { file ->
            !file.name.equals("module-info.java")
        } ?: files()
    })

    // Collect all classpaths from subprojects
    classpath = files(filteredProjects.flatMap { project ->
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val main = sourceSets.findByName("main")
        main?.compileClasspath ?: files()
    })

    // Add runtime classpath to ensure all dependencies are available
    classpath += files(filteredProjects.flatMap { project ->
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val main = sourceSets.findByName("main")
        main?.runtimeClasspath ?: files()
    })

    // Apply the same Javadoc options as in subprojects
    (options as StandardJavadocDocletOptions).apply {
        encoding = "UTF-8"
        addStringOption("Xdoclint:all,-missing/private")
        links("https://docs.oracle.com/en/java/javase/21/docs/api/")
        use(true)
        noTimestamp(true)
        windowTitle = "${rootProject.name} ${project.version} API"
        docTitle = "${rootProject.name} ${project.version} API"
        header = "${rootProject.name} ${project.version} API"
        // Set locale to English to ensure consistent language in generated documentation
        locale = "en_US"
        // Disable module path to avoid module-related errors
        addBooleanOption("module-path", false)
    }
}

val jreleaserProjectVersion = rootProject.version.toString()

jreleaser {
    project {
        name.set(rootProject.name)
        version.set(jreleaserProjectVersion)
        group = Meta.GROUP
        authors.set(listOf(Meta.DEVELOPER_NAME))
        license.set(Meta.LICENSE_NAME)
        links {
            homepage.set(Meta.ORGANIZATION_URL)
        }
        inceptionYear.set(Meta.INCEPTION_YEAR)
        gitRootSearch.set(true)
    }

    signing {
        active.set(org.jreleaser.model.Active.ALWAYS)
        pgp {
            armored.set(true)
            secretKey.set(System.getenv("SIGNING_SECRET_KEY"))
            passphrase.set(System.getenv("SIGNING_PASSWORD"))
        }
    }

    deploy {
        maven {
            if (!isSnapshot) {
                println("adding release-deploy")
                mavenCentral {
                    create("release-deploy") {
                        active.set(org.jreleaser.model.Active.RELEASE)
                        url.set("https://central.sonatype.com/api/v1/publisher")
                        stagingRepositories.add("build/staging-deploy")
                        username.set(System.getenv("SONATYPE_USERNAME"))
                        password.set(System.getenv("SONATYPE_PASSWORD"))
                    }
                }
            }
        }
    }
}

/////////////////////////////////////////////////////////////////////////////
// Versions plugin configuration for all projects
/////////////////////////////////////////////////////////////////////////////

allprojects {
    fun isStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
        val regex = "[0-9,.v-]+-(rc|ea|alpha|beta|b|M|SNAPSHOT)([+-]?[0-9]*)?".toRegex(RegexOption.IGNORE_CASE)
        return stableKeyword || !regex.matches(version)
    }

    tasks.withType<DependencyUpdatesTask> {
        // refuse non-stable versions
        rejectVersionIf {
            !isStable(candidate.version)
        }

        // dependencyUpdates fails in parallel mode with Gradle 9+ (https://github.com/ben-manes/gradle-versions-plugin/issues/968)
        doFirst {
            gradle.startParameter.isParallelProjectExecutionEnabled = false
        }
    }
}
